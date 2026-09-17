package com.mineskin;

/**
 * 目录里的一款皮肤。
 *
 * @param id        SkinsRestorer 自定义皮肤名（{@code /skin <id>}）
 * @param display   展示名
 * @param value     纹理 value（Base64）
 * @param signature 纹理签名（可为空）
 */
public record SkinEntry(String id, String display, String value, String signature) {
}
