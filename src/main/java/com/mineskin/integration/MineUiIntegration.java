package com.mineskin.integration;

import com.mineui.api.MineUi;
import com.mineui.api.MineUiProvider;
import com.mineui.api.MineUiSession;
import com.mineskin.ui.UiAction;
import com.mineskin.ui.UiActionHandler;
import com.mineskin.ui.UiAdapter;
import com.mineskin.ui.UiHandle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * {@link UiAdapter} 的 MineUI 实现。
 * <p>
 * <b>只有确认 MineUI API 可用后才会加载本类</b>（见 {@code MineSkinPlugin#hookMineUi}）：
 * MineUI 未安装或版本过旧时，本类不会被触碰，避免 NoClassDefFoundError。
 */
public final class MineUiIntegration implements UiAdapter {

    private final Plugin owner;
    private final MineUi api;

    public MineUiIntegration(Plugin owner) {
        this.owner = owner;
        this.api = MineUiProvider.get();
    }

    @Override
    public boolean available() {
        return api != null;
    }

    @Override
    public boolean hasClient(Player player) {
        return api != null && api.hasClient(player);
    }

    @Override
    public UiHandle open(Player player, String app, String view) {
        return new SessionHandle(api.open(owner, player, app, view));
    }

    private static final class SessionHandle implements UiHandle {

        private final MineUiSession session;

        private SessionHandle(MineUiSession session) {
            this.session = session;
        }

        @Override
        public UiHandle state(String key, Object value) {
            session.state(key, value);
            return this;
        }

        @Override
        public UiHandle on(String actionId, UiActionHandler handler) {
            session.on(actionId, action -> handler.handle(new UiAction(action.player(), action.id())));
            return this;
        }

        @Override
        public void snapshot() {
            session.snapshot();
        }

        @Override
        public void close() {
            session.close();
        }

        @Override
        public boolean closed() {
            return session.closed();
        }
    }
}
