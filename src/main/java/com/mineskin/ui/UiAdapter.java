package com.mineskin.ui;

import org.bukkit.entity.Player;

/**
 * MineUI 适配层：把业务代码与 MineUI 类隔离开。
 * <p>
 * MineUI 未安装（或版本过旧没有 {@code com.mineui.api}）时，{@link #ui()} 为 null，
 * 业务自动退化为聊天列表，不会因类缺失导致插件加载失败。
 */
public interface UiAdapter {

    /** MineUI API 是否真正可用（类已加载且 provider 已注册）。 */
    boolean available();

    boolean hasClient(Player player);

    UiHandle open(Player player, String app, String view);
}
