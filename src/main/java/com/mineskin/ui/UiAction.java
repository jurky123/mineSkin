package com.mineskin.ui;

import org.bukkit.entity.Player;

/** 一次界面动作。 */
public record UiAction(Player player, String id) {
}
