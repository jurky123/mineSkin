package com.mineskin.ui;

@FunctionalInterface
public interface UiActionHandler {

    void handle(UiAction action);
}
