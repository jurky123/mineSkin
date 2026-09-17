package com.mineskin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 守护业务页面资源：MineUI 解析前先保证结构无损坏。 */
class SkinBrowserUiResourceTest {

    private static final String RESOURCE = "assets/mineskin/ui/skin/browser.json";

    private static JsonObject loadRoot() throws Exception {
        try (InputStream in = SkinBrowserUiResourceTest.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            assertNotNull(in, "缺少 " + RESOURCE);
            return JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    @Test
    void hasColumnRootAndChildren() throws Exception {
        JsonObject root = loadRoot();
        assertEquals("column", root.get("type").getAsString());
        assertTrue(root.has("children"));
    }

    @Test
    void hasEightClickableSlots() throws Exception {
        assertEquals(8, countField(loadRoot(), "action", "slot_"));
    }

    @Test
    void hasEightHoverSlots() throws Exception {
        assertEquals(8, countField(loadRoot(), "hoverAction", "hover_"));
    }

    @Test
    void hasPreviewPlayerBoundToSkinState() throws Exception {
        assertTrue(hasSkinPlayer(loadRoot()));
    }

    @Test
    void hasSearchInput() throws Exception {
        assertTrue(hasNode(loadRoot(), "input", "search"));
    }

    @Test
    void hasClearSkinButton() throws Exception {
        assertTrue(hasNode(loadRoot(), "button", "clear"));
    }

    private static boolean hasNode(JsonElement element, String type, String action) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("type") && type.equals(obj.get("type").getAsString())
                    && obj.has("action") && action.equals(obj.get("action").getAsString())) {
                return true;
            }
            for (var entry : obj.entrySet()) {
                if (hasNode(entry.getValue(), type, action)) {
                    return true;
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (hasNode(child, type, action)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int countField(JsonElement element, String field, String prefix) {
        int count = 0;
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has(field) && obj.get(field).getAsString().startsWith(prefix)) {
                count++;
            }
            for (var entry : obj.entrySet()) {
                count += countField(entry.getValue(), field, prefix);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                count += countField(child, field, prefix);
            }
        }
        return count;
    }

    private static boolean hasSkinPlayer(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("type") && "player".equals(obj.get("type").getAsString()) && obj.has("skin")) {
                return true;
            }
            for (var entry : obj.entrySet()) {
                if (hasSkinPlayer(entry.getValue())) {
                    return true;
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (hasSkinPlayer(child)) {
                    return true;
                }
            }
        }
        return false;
    }
}
