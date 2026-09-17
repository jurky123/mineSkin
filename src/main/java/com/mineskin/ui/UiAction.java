package com.mineskin.ui;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

/** 一次界面动作。 */
public record UiAction(Player player, String id, JsonObject payload) {

    public UiAction {
        payload = payload == null ? new JsonObject() : payload;
    }

    /** 读取字符串载荷字段。 */
    public String getString(String key, String defaultValue) {
        if (!payload.has(key) || payload.get(key).isJsonNull()) {
            return defaultValue;
        }
        return payload.get(key).getAsString();
    }
}
