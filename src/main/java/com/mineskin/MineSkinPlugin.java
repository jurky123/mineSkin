package com.mineskin;

import com.mineskin.command.SkinUiCommand;
import com.mineskin.ui.UiAdapter;
import com.mineskin.ui.UiHandle;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinApplier;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MineSkin：皮肤目录 + MineUI 3D 预览。
 * <p>
 * 换肤本身仍由 SkinsRestorer 负责（皮肤存储、刷新、离线模式恢复），
 * 本插件只做目录/搜索/选择/预览，并通过 SkinsRestorer API 应用皮肤。
 */
public final class MineSkinPlugin extends JavaPlugin {

    /** 与 SkinsRestorer config.yml 的换肤冷却保持一致。 */
    public static final long APPLY_COOLDOWN_MILLIS = 5000L;

    private final Map<UUID, Long> lastApply = new ConcurrentHashMap<>();

    private SkinCatalog catalog;
    private UiAdapter ui;

    @Override
    public void onEnable() {
        catalog = new SkinCatalog(this);
        catalog.reload();
        hookMineUi();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                event -> new SkinUiCommand(this).register(event.registrar()));

        getLogger().info("MineSkin 已启用（MineUI 3D 预览："
                + (ui != null && ui.available() ? "可用" : "不可用，将使用聊天列表") + "）");
    }

    /**
     * 可选集成 MineUI API：只有 API 确认可用才实例化适配器，
     * 未安装/旧版 MineUI 时保持 null，插件照常工作。
     */
    private void hookMineUi() {
        if (getServer().getPluginManager().getPlugin("MineUI") == null) {
            return;
        }
        try {
            Class<?> integration = Class.forName("com.mineskin.integration.MineUiIntegration");
            UiAdapter adapter = (UiAdapter) integration
                    .getConstructor(org.bukkit.plugin.Plugin.class)
                    .newInstance(this);
            if (adapter.available()) {
                ui = adapter;
            }
        } catch (Throwable t) {
            getLogger().warning("MineUI API 不匹配（可能是旧版 MineUI），退回聊天列表：" + t);
        }
    }

    public SkinCatalog catalog() {
        return catalog;
    }

    public void reloadCatalog() {
        catalog.reload();
    }

    /** 打开浏览器（mod 客户端）或发送聊天列表（原版客户端）。 */
    public void openBrowser(Player player, String filter, int page) {
        if (ui != null && ui.available() && ui.hasClient(player)) {
            UiHandle handle = ui.open(player, "skin", "browser");
            new SkinBrowser(this, handle, player, filter).start(page);
        } else {
            ChatSkinList.send(this, player, filter, page);
        }
    }

    /** 剩余换肤冷却毫秒数（0 表示可换）。 */
    public long applyCooldownRemainingMillis(Player player) {
        Long last = lastApply.get(player.getUniqueId());
        if (last == null) {
            return 0L;
        }
        long remaining = APPLY_COOLDOWN_MILLIS - (System.currentTimeMillis() - last);
        return Math.max(0L, remaining);
    }

    public void markApplied(Player player) {
        lastApply.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /** 通过 SkinsRestorer API 应用目录里的皮肤（存储 + 立即刷新）。 */
    public void applySkin(Player player, SkinEntry entry) {
        SkinsRestorer skinsRestorer = SkinsRestorerProvider.get();
        if (skinsRestorer == null) {
            throw new IllegalStateException("SkinsRestorer 未就绪");
        }
        skinsRestorer.getPlayerStorage()
                .setSkinIdOfPlayer(player.getUniqueId(), SkinIdentifier.ofCustom(entry.id()));
        SkinApplier<Player> applier = skinsRestorer.getSkinApplier(Player.class);
        applier.applySkin(player, SkinProperty.of(entry.value(), entry.signature()));
    }
}
