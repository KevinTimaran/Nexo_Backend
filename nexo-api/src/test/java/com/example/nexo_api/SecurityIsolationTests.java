package com.example.nexo_api;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "nexo.auth.disabled=false",
                "nexo.auth.hs256-secret=" + SecurityIsolationTests.SECRET,
        })
class SecurityIsolationTests {
    static final String SECRET = "test-secret-test-secret-test-secret-32chars!!";

    @Value("${local.server.port}") int port;
    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeAll static void isolateStorage() throws Exception {
        System.setProperty("nexo.data.file", Files.createTempDirectory("nexo-sec").resolve("data.json").toString());
    }

    private String token(String subject, String secret) {
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")));
        JwtClaimsSet claims = JwtClaimsSet.builder().subject(subject).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private HttpResponse<String> call(String method, String path, String token, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).header("Content-Type", "application/json");
        if (token != null) request.header("Authorization", "Bearer " + token);
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test void healthIsPublicAndEverythingElseNeedsAValidToken() throws Exception {
        assertEquals(200, call("GET", "/api/v1/health", null, null).statusCode());
        assertEquals(401, call("GET", "/api/v1/projects", null, null).statusCode());
        assertEquals(401, call("GET", "/api/v1/projects", token("user-a", "another-secret-another-secret-123456"), null).statusCode(), "A token signed with a different key is rejected");
        assertEquals(401, call("POST", "/api/v1/ai/reply", null, "{\"text\":\"hola\"}").statusCode(), "AI endpoints are protected too");
        assertEquals(401, call("POST", "/api/v1/research/search", null, "{\"query\":\"x\"}").statusCode());
    }

    @Test void usersNeverSeeOrChangeEachOthersProjects() throws Exception {
        String a = token("user-a", SECRET);
        String b = token("user-b", SECRET);
        HttpResponse<String> created = call("POST", "/api/v1/projects", a, "{\"name\":\"Proyecto de A\",\"description\":\"\"}");
        assertEquals(201, created.statusCode());
        String id = created.body().replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
        assertTrue(created.body().contains("\"ownerId\":\"user-a\""));

        assertTrue(call("GET", "/api/v1/projects", a, null).body().contains(id));
        assertFalse(call("GET", "/api/v1/projects", b, null).body().contains(id), "B does not see A's project in the list");
        assertEquals(404, call("GET", "/api/v1/projects/" + id, b, null).statusCode());
        assertEquals(404, call("GET", "/api/v1/projects/" + id + "/workspace", b, null).statusCode());
        assertEquals(404, call("PATCH", "/api/v1/projects/" + id, b, "{\"name\":\"Robado\"}").statusCode());
        assertEquals(404, call("PUT", "/api/v1/projects/" + id + "/workspace", b, "{\"expectedRevision\":0,\"operationId\":\"x\"}").statusCode());
        assertEquals(404, call("POST", "/api/v1/projects/" + id + "/documents/generate", b, "{}").statusCode());
        assertEquals(204, call("DELETE", "/api/v1/projects/" + id, b, null).statusCode(), "Delete is idempotent but must not remove A's project");
        assertEquals(200, call("GET", "/api/v1/projects/" + id, a, null).statusCode(), "A's project survived B's delete attempt");
        assertEquals(204, call("DELETE", "/api/v1/projects/" + id, a, null).statusCode());
        assertEquals(404, call("GET", "/api/v1/projects/" + id, a, null).statusCode());
    }

    @Test void ownerIdInTheBodyIsIgnored() throws Exception {
        String a = token("user-a", SECRET);
        HttpResponse<String> created = call("POST", "/api/v1/projects", a, "{\"name\":\"Suplantación\",\"ownerId\":\"user-b\"}");
        assertEquals(201, created.statusCode());
        assertTrue(created.body().contains("\"ownerId\":\"user-a\""));
    }

    @Test void corsAllowsTheConfiguredOriginsOnly() throws Exception {
        HttpRequest allowed = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/projects"))
                .header("Origin", "http://localhost:1420").header("Access-Control-Request-Method", "GET").header("Access-Control-Request-Headers", "authorization")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> ok = http.send(allowed, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, ok.statusCode());
        assertEquals("http://localhost:1420", ok.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        HttpRequest denied = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/projects"))
                .header("Origin", "https://evil.example").header("Access-Control-Request-Method", "GET")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        assertNotEquals(200, http.send(denied, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}
