package com.mineskin.ui;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

/**
 * MineUI 适配层：把业务代码与 MineUI 类隔离开。
 * <p>
 * MineUI 未安装（或版本过旧没有 {@code com.mineui.api}）时适配器不可用，
 * 业务自动退化为聊天列表，不会因类缺失导致插件加载失败。
 */
public interface UiAdapter {

    /** MineUI API 是否真正可用（类已加载且 provider 已注册）。 */
    boolean available();

    /**
     * 客户端是否支持随 OPEN 下发界面定义（{@code server_ui} 能力，MineUI 0.6.4+）。
     * 仅 {@code hasClient}（装了 mod）不够：旧客户端会开出一个加载失败的页面，必须回退。
     */
    boolean supportsServerUi(Player player);

    /**
     * 打开 MineUI 会话，并下发本插件自带的界面定义。
     *
     * @param definition 界面定义（{@code assets/mineskin/ui/<app>/<view>.json}）
     */
    UiHandle open(Player player, String app, String view, JsonObject definition);
}
