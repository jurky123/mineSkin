package com.mineskin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 皮肤目录：读取 SkinsRestorer 的自定义皮肤目录（{@code plugins/SkinsRestorer/skins/*.customskin}）。
 * <p>
 * 每个文件是 JSON：{@code skinName / displayName / value / signature}。
 * 目录内容启动时载入内存，预览与换肤都不再读盘。
 */
public final class SkinCatalog {

    private final MineSkinPlugin plugin;

    private volatile List<SkinEntry> entries = List.of();

    public SkinCatalog(MineSkinPlugin plugin) {
        this.plugin = plugin;
    }

    /** 重新扫描皮肤目录（主线程调用）。 */
    public void reload() {
        Path dir = plugin.getDataFolder().getParentFile().toPath()
                .resolve("SkinsRestorer").resolve("skins");
        List<SkinEntry> loaded = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            plugin.getLogger().warning("找不到 SkinsRestorer 皮肤目录: " + dir);
            entries = List.of();
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.customskin")) {
            for (Path file : stream) {
                SkinEntry entry = read(file);
                if (entry != null) {
                    loaded.add(entry);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("扫描皮肤目录失败: " + e.getMessage());
        }
        loaded.sort(Comparator.comparing(SkinEntry::display, String.CASE_INSENSITIVE_ORDER));
        entries = List.copyOf(loaded);
        plugin.getLogger().info("皮肤目录已载入 " + entries.size() + " 款自定义皮肤");
    }

    private SkinEntry read(Path file) {
        String fileName = file.getFileName().toString();
        String fallbackId = fileName.substring(0, fileName.length() - ".customskin".length());
        try {
            JsonObject json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            String id = string(json, "skinName", fallbackId);
            if (id.startsWith("sr-recommendation-")) {
                // SkinsRestorer 推荐皮肤的缓存副本，已在目录中对应正常皮肤
                return null;
            }
            String display = decodeDisplay(string(json, "displayName", null), id);
            String value = string(json, "value", null);
            if (value == null || value.isEmpty()) {
                plugin.getLogger().warning("皮肤文件缺少 value，已跳过: " + fileName);
                return null;
            }
            String signature = string(json, "signature", null);
            return new SkinEntry(id, display, value, signature == null || signature.isBlank() ? null : signature);
        } catch (IOException | JsonSyntaxException | IllegalStateException e) {
            plugin.getLogger().warning("皮肤文件解析失败，已跳过: " + fileName + " (" + e.getMessage() + ")");
            return null;
        }
    }

    /**
     * displayName 兼容两种格式：
     * <ul>
     *   <li>SkinsRestorer 规范格式：JSON 组件字符串（{@code "\"Chicken boss\""} 或 {@code {"text":"..."}}）</li>
     *   <li>旧数据：纯文本（仅作兼容，多词会破坏 SR 自带 GUI）</li>
     * </ul>
     */
    static String decodeDisplay(String raw, String fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("\"") || trimmed.startsWith("{")) {
            try {
                JsonElement parsed = JsonParser.parseString(trimmed);
                if (parsed.isJsonPrimitive()) {
                    return parsed.getAsString();
                }
                if (parsed.isJsonObject() && parsed.getAsJsonObject().has("text")) {
                    return parsed.getAsJsonObject().get("text").getAsString();
                }
            } catch (JsonSyntaxException | IllegalStateException ignored) {
                // 解析失败按纯文本处理
            }
        }
        return raw;
    }

    private static String string(JsonObject json, String key, String fallback) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        return json.get(key).getAsString();
    }

    public List<SkinEntry> all() {
        return entries;
    }

    /** 关键词匹配展示名与 id（大小写不敏感，空关键词返回全部）。 */
    public List<SkinEntry> search(String filter) {
        if (filter == null || filter.isBlank()) {
            return entries;
        }
        String needle = filter.trim().toLowerCase(Locale.ROOT);
        List<SkinEntry> result = new ArrayList<>();
        for (SkinEntry entry : entries) {
            if (entry.id().toLowerCase(Locale.ROOT).contains(needle)
                    || entry.display().toLowerCase(Locale.ROOT).contains(needle)) {
                result.add(entry);
            }
        }
        return result;
    }

    public int size() {
        return entries.size();
    }
}
