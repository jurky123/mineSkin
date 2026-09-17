package com.mineskin.ui;

/**
 * 一次 MineUI 会话的轻量封装（只暴露业务需要的能力）。
 * 所有方法必须在主线程调用。
 */
public interface UiHandle {

    UiHandle state(String key, Object value);

    UiHandle on(String actionId, UiActionHandler handler);

    void snapshot();

    void close();

    boolean closed();
}
