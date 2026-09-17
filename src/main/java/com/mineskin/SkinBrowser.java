package com.mineskin;

import com.mineskin.ui.UiHandle;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 一次皮肤浏览器会话：左侧分页列表 + 右侧 3D 预览，动作全部回服务端校验。
 * <p>
 * 交互：悬停条目即时预览；点击选中（再次点击取消），选中后锁定预览、悬浮不再切换；
 * 「应用该皮肤」应用当前预览的那一款（未选中时即最近悬停的）。
 * <p>
 * 与客户端页面 {@code assets/mineskin/ui/skin/browser.json} 的槽位数量保持一致（{@value #PAGE_SIZE}）。
 */
public final class SkinBrowser {

    /** 每页槽位数（须与 browser.json 的 slot_0..slot_N 数量一致）。 */
    public static final int PAGE_SIZE = 8;

    private final MineSkinPlugin plugin;
    private final UiHandle ui;
    private final Player player;
    private String filter;

    private List<SkinEntry> results;
    private int page;
    /** 点击选中的条目（锁定预览）；-1 表示未选中。 */
    private int selected = -1;
    /** 最近悬停的条目；-1 表示无。 */
    private int hovered = -1;

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

        ui.state("title", "皮肤浏览器");

        for (int i = 0; i < PAGE_SIZE; i++) {
            final int slot = i;
            ui.on("slot_" + i, action -> toggleSelect(page * PAGE_SIZE + slot));
            ui.on("hover_" + i, action -> hover(page * PAGE_SIZE + slot));
        }
        ui.on("prev", action -> turn(-1));
        ui.on("next", action -> turn(1));
        ui.on("apply", action -> applyPreviewed());
        ui.on("clear", action -> clearSkin());
        ui.on("close", action -> ui.close());
        // 搜索栏（需要 MineUI input 控件支持；回车提交 payload.text）
        ui.on("search", action -> applySearch(action.getString("text", "")));

        pushSubtitle();
        pushSlots();
        pushPreview();
        pushPageLabel();
        pushStatus(results.isEmpty()
                ? "试试 /skins <英文关键词>（如 fox、girl、creeper）"
                : "悬停条目即时预览 · 点击选中后锁定预览 · 再点取消");
        ui.snapshot();

        player.sendMessage(net.kyori.adventure.text.Component.text(
                "[MineSkin] 已打开皮肤浏览器" + (filter.isEmpty() ? "" : "（关键词：" + filter + "）"),
                net.kyori.adventure.text.format.NamedTextColor.GREEN));
    }

    // ---------- 动作 ----------

    /** 鼠标悬停：未选中时即时预览该条目。 */
    private void hover(int index) {
        if (selected >= 0 || index < 0 || index >= results.size() || hovered == index) {
            return;
        }
        hovered = index;
        pushPreview();
    }

    /** 点击条目：选中/取消选中（选中后锁定预览，悬浮不再切换）。 */
    private void toggleSelect(int index) {
        if (index < 0 || index >= results.size()) {
            return;
        }
        if (selected == index) {
            selected = -1;
            pushStatus("已取消选择，恢复悬停预览");
        } else {
            selected = index;
            pushStatus("已选择：" + results.get(index).display() + " · 点「应用该皮肤」生效（再次点击取消）");
        }
        pushSlots();
        pushPreview();
    }

    private void turn(int delta) {
        int pages = pageCount();
        int next = Math.max(0, Math.min(page + delta, pages - 1));
        if (next == page) {
            return;
        }
        page = next;
        hovered = -1;
        pushSlots();
        pushPreview();
        pushPageLabel();
    }

    private void applyPreviewed() {
        int index = previewIndex();
        if (index < 0 || index >= results.size()) {
            pushStatus("请先悬停或点击选择一款皮肤");
            return;
        }
        long remaining = plugin.applyCooldownRemainingMillis(player);
        if (remaining > 0) {
            pushStatus(String.format(java.util.Locale.ROOT,
                    "换肤冷却中：还需 %.1f 秒", remaining / 1000.0));
            return;
        }
        SkinEntry entry = results.get(index);
        try {
            plugin.applySkin(player, entry);
            plugin.markApplied(player);
            pushStatus("已应用：" + entry.display() + "（/skin " + entry.id() + "）");
        } catch (Exception e) {
            plugin.getLogger().warning("为 " + player.getName() + " 应用皮肤 " + entry.id() + " 失败: " + e.getMessage());
            pushStatus("换肤失败：" + e.getMessage());
        }
    }

    /** 清除当前皮肤，恢复默认外观。 */
    private void clearSkin() {
        long remaining = plugin.applyCooldownRemainingMillis(player);
        if (remaining > 0) {
            pushStatus(String.format(java.util.Locale.ROOT,
                    "换肤冷却中：还需 %.1f 秒", remaining / 1000.0));
            return;
        }
        try {
            plugin.clearSkin(player);
            plugin.markApplied(player);
            selected = -1;
            hovered = -1;
            pushSlots();
            pushPreview();
            pushStatus("已清除皮肤，恢复默认外观");
        } catch (Exception e) {
            plugin.getLogger().warning("为 " + player.getName() + " 清除皮肤失败: " + e.getMessage());
            pushStatus("清除失败：" + e.getMessage());
        }
    }

    // ---------- 状态下发 ----------

    /** 当前预览展示的条目：选中锁定 > 最近悬停 > 本页第一项。 */
    private int previewIndex() {
        if (selected >= 0) {
            return selected;
        }
        if (hovered >= 0) {
            return hovered;
        }
        return page * PAGE_SIZE;
    }

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
        int index = previewIndex();
        if (index >= 0 && index < results.size()) {
            SkinEntry entry = results.get(index);
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

    /** 搜索栏提交：重新过滤并回到第一页（等 MineUI input 能力到位后自动生效）。 */
    private void applySearch(String keyword) {
        filter = keyword == null ? "" : keyword.trim();
        results = plugin.catalog().search(filter);
        page = 0;
        selected = -1;
        hovered = -1;
        pushSubtitle();
        pushSlots();
        pushPreview();
        pushPageLabel();
        pushStatus(results.isEmpty()
                ? "没有匹配「" + filter + "」的皮肤"
                : "找到 " + results.size() + " 款皮肤");
    }

    private void pushSubtitle() {
        ui.state("subtitle", results.isEmpty()
                ? "没有匹配的皮肤"
                : "共 " + results.size() + " 款" + (filter.isEmpty() ? "" : " · 关键词：" + filter));
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
