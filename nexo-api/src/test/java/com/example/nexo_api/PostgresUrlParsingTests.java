package com.example.nexo_api;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class PostgresUrlParsingTests {
    private String[] parse(String url) throws Exception {
        Method method = Class.forName("com.example.nexo_api.service.PostgresPersistence").getDeclaredMethod("parse", String.class, String.class, String.class);
        method.setAccessible(true);
        return (String[]) method.invoke(null, url, null, null);
    }

    @Test void acceptsPasswordsWithAtSignWhetherEncodedOrNot() throws Exception {
        String[] raw = parse("postgresql://postgres.ref:p@ss-1@aws-1-us-east-1.pooler.supabase.com:5432/postgres");
        assertEquals("jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres", raw[0]);
        assertEquals("postgres.ref", raw[1]);
        assertEquals("p@ss-1", raw[2]);
        String[] encoded = parse("postgresql://postgres.ref:p%40ss-1@aws-1-us-east-1.pooler.supabase.com:5432/postgres");
        assertEquals("p@ss-1", encoded[2]);
        assertEquals(raw[0], encoded[0]);
    }

    @Test void keepsJdbcUrlsAndRejectsUrlsWithoutCredentials() throws Exception {
        assertArrayEquals(new String[] {"jdbc:postgresql://h:5432/db", "u", "p"}, parse2("jdbc:postgresql://h:5432/db", "u", "p"));
        assertThrows(Exception.class, () -> parse("postgresql://host:5432/postgres"));
    }

    private String[] parse2(String url, String user, String pass) throws Exception {
        Method method = Class.forName("com.example.nexo_api.service.PostgresPersistence").getDeclaredMethod("parse", String.class, String.class, String.class);
        method.setAccessible(true);
        return (String[]) method.invoke(null, url, user, pass);
    }
}
