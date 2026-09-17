package com.mineskin;

import com.mineskin.ui.UiHandle;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 一次皮肤浏览器会话：左侧分页列表 + 右侧 3D 预览，动作全部回服务端校验。
 * <p>
 * 与客户端 {@code assets/mineui/ui/skin/browser.json} 的槽位数量保持一致（{@value #PAGE_SIZE}）。
 */
public final class SkinBrowser {

    /** 每页槽位数（须与 browser.json 的 slot_0..slot_N 数量一致）。 */
    public static final int PAGE_SIZE = 8;

    private final MineSkinPlugin plugin;
    private final UiHandle ui;
    private final Player player;
    private final String filter;

    private List<SkinEntry> results;
    private int page;
    private int selected = -1;

    public SkinBrowser(MineSkinPlugin plugin, UiHandle ui, Player player, String filter) {
        this.plugin = plugin;
        this.ui = ui;
        this.player = player;
        this.filter = filter == null ? "" : filter.trim();
    }

    public void start(int requestedPage) {
        results = plugin.catalog().search(filter);
        int pages = pageCount();
        page = Math.max(0, Math.min(requestedPage - 1, pages - 1));
        selected = results.isEmpty() ? -1 : page * PAGE_SIZE;

        ui.state("title", "皮肤浏览器");
        ui.state("subtitle", results.isEmpty()
                ? "没有匹配的皮肤"
                : "共 " + results.size() + " 款" + (filter.isEmpty() ? "" : " · 关键词：" + filter));
        ui.state("footer", "数据：" + plugin.catalog().size() + " 款 SkinsRestorer 自定义皮肤");

        for (int i = 0; i < PAGE_SIZE; i++) {
            final int slot = i;
            ui.on("slot_" + i, action -> select(page * PAGE_SIZE + slot));
        }
        ui.on("prev", action -> turn(-1));
        ui.on("next", action -> turn(1));
        ui.on("apply", action -> applySelected());
        ui.on("close", action -> ui.close());

        pushSlots();
        pushPreview();
        pushPageLabel();
        pushStatus(results.isEmpty()
                ? "试试 /skinui <英文关键词>（如 fox、girl、creeper）"
                : "点击左侧皮肤，右侧拖动可旋转 3D 预览");
        ui.snapshot();

        player.sendMessage(net.kyori.adventure.text.Component.text(
                "[MineSkin] 已打开皮肤浏览器" + (filter.isEmpty() ? "" : "（关键词：" + filter + "）"),
                net.kyori.adventure.text.format.NamedTextColor.GREEN));
    }

    // ---------- 动作 ----------

    private void select(int index) {
        if (index < 0 || index >= results.size()) {
            return;
        }
        selected = index;
        pushSlots();
        pushPreview();
        pushStatus("已选择：" + results.get(index).display() + " · 点「应用该皮肤」生效");
    }

    private void turn(int delta) {
        int pages = pageCount();
        int next = Math.max(0, Math.min(page + delta, pages - 1));
        if (next == page) {
            return;
        }
        page = next;
        int first = page * PAGE_SIZE;
        if (selected < first || selected >= Math.min(first + PAGE_SIZE, results.size())) {
            selected = results.isEmpty() ? -1 : first;
        }
        pushSlots();
        pushPreview();
        pushPageLabel();
    }

    private void applySelected() {
        if (selected < 0 || selected >= results.size()) {
            pushStatus("请先点击左侧列表选择一款皮肤");
            return;
        }
        long remaining = plugin.applyCooldownRemainingMillis(player);
        if (remaining > 0) {
            pushStatus(String.format(java.util.Locale.ROOT,
                    "换肤冷却中：还需 %.1f 秒", remaining / 1000.0));
            return;
        }
        SkinEntry entry = results.get(selected);
        try {
            plugin.applySkin(player, entry);
            plugin.markApplied(player);
            pushStatus("已应用：" + entry.display() + "（/skin " + entry.id() + "）");
        } catch (Exception e) {
            plugin.getLogger().warning("为 " + player.getName() + " 应用皮肤 " + entry.id() + " 失败: " + e.getMessage());
            pushStatus("换肤失败：" + e.getMessage());
        }
    }

    // ---------- 状态下发 ----------

    private void pushSlots() {
        Map<String, Object> slots = new LinkedHashMap<>();
        for (int i = 0; i < PAGE_SIZE; i++) {
            int index = page * PAGE_SIZE + i;
            Map<String, Object> slot = new LinkedHashMap<>();
            if (index < results.size()) {
                SkinEntry entry = results.get(index);
                slot.put("name", entry.display());
                slot.put("tooltip", entry.display() + " · /skin " + entry.id());
                slot.put("selected", index == selected);
            } else {
                slot.put("name", "");
                slot.put("tooltip", "");
                slot.put("selected", false);
            }
            slots.put(String.valueOf(i), slot);
        }
        ui.state("slots", slots);
    }

    private void pushPreview() {
        Map<String, Object> preview = new LinkedHashMap<>();
        if (selected >= 0 && selected < results.size()) {
            SkinEntry entry = results.get(selected);
            preview.put("name", entry.display());
            preview.put("value", entry.value());
            preview.put("signature", entry.signature());
        } else {
            preview.put("name", "（暂无选择）");
            preview.put("value", "");
            preview.put("signature", "");
        }
        ui.state("preview", preview);
    }

    private void pushPageLabel() {
        ui.state("page", (page + 1) + " / " + pageCount());
    }

    private void pushStatus(String text) {
        ui.state("status", text);
    }

    private int pageCount() {
        return Math.max(1, (results.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }
}
