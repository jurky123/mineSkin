package com.mineskin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;

/** 无 MineUI 客户端玩家的聊天列表回退（支持点击换肤与翻页）。 */
public final class ChatSkinList {

    private ChatSkinList() {
    }

    public static void send(MineSkinPlugin plugin, Player player, String filter, int requestedPage) {
        List<SkinEntry> results = plugin.catalog().search(filter);
        if (results.isEmpty()) {
            player.sendMessage(Component.text("没有找到匹配的皮肤，试试英文关键词（fox / girl / creeper）", NamedTextColor.GRAY));
            return;
        }
        int pageSize = SkinBrowser.PAGE_SIZE;
        int pages = Math.max(1, (results.size() + pageSize - 1) / pageSize);
        int page = Math.max(0, Math.min(requestedPage - 1, pages - 1));
        int from = page * pageSize;
        int to = Math.min(from + pageSize, results.size());

        player.sendMessage(Component.text()
                .append(Component.text("皮肤列表", NamedTextColor.GOLD))
                .append(Component.text("（第 " + (page + 1) + " / " + pages + " 页，共 " + results.size()
                        + " 款" + (filter.isBlank() ? "" : "，关键词：" + filter) + "）", NamedTextColor.GRAY))
                .build());

        for (int i = from; i < to; i++) {
            SkinEntry entry = results.get(i);
            player.sendMessage(Component.text()
                    .append(Component.text("[换] ", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.runCommand("/skin " + entry.id()))
                            .hoverEvent(HoverEvent.showText(Component.text("点击应用 /skin " + entry.id()))))
                    .append(Component.text(entry.display(), NamedTextColor.WHITE))
                    .build());
        }

        Component nav = Component.text("翻页：", NamedTextColor.GRAY);
        if (page > 0) {
            nav = nav.append(pageButton("上一页", page, filter));
        }
        if (page + 1 < pages) {
            nav = nav.append(Component.text("  ")).append(pageButton("下一页", page + 2, filter));
        }
        player.sendMessage(nav);
        player.sendMessage(Component.text("提示：安装 MineUI 客户端可使用 3D 预览界面（/skinui）", NamedTextColor.DARK_GRAY));
    }

    private static Component pageButton(String label, int page, String filter) {
        String command = "/skinui page " + page + (filter.isBlank() ? "" : " " + filter);
        return Component.text("[" + label + "]", NamedTextColor.AQUA)
                .clickEvent(ClickEvent.runCommand(command));
    }
}
