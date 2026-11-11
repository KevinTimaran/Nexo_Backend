package com.example.nexo_api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "nexo.auth.disabled=true")
class NexoApiApplicationTests {

	@BeforeAll
	static void isolateStorage() throws Exception {
		System.setProperty("nexo.data.file", java.nio.file.Files.createTempDirectory("nexo-ctx").resolve("data.json").toString());
	}

	@Test
	void contextLoads() {
	}

}
