package com.mineskin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinCatalogTest {

    @Test
    void decodesSkinsRestorerJsonDisplayName() {
        assertEquals("Chicken boss", SkinCatalog.decodeDisplay("\"Chicken boss\"", "chicken-boss"));
    }

    @Test
    void decodesJsonComponentObject() {
        assertEquals("Fox", SkinCatalog.decodeDisplay("{\"text\":\"Fox\"}", "fox"));
    }

    @Test
    void decodesEscapes() {
        assertEquals("Bob \"The\" Builder", SkinCatalog.decodeDisplay("\"Bob \\\"The\\\" Builder\"", "bob"));
    }

    @Test
    void keepsLegacyPlainName() {
        assertEquals("Chicken boss", SkinCatalog.decodeDisplay("Chicken boss", "chicken-boss"));
    }

    @Test
    void fallsBackWhenMissing() {
        assertEquals("chicken-boss", SkinCatalog.decodeDisplay(null, "chicken-boss"));
        assertEquals("chicken-boss", SkinCatalog.decodeDisplay("  ", "chicken-boss"));
    }
}
