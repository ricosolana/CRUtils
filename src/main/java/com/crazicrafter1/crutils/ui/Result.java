package com.crazicrafter1.crutils.ui;

import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class Result {

    abstract void invoke(AbstractMenu menu, InventoryClickEvent event);

    /**
     * Let the player take the item
     * @return new {@link ResultGrab}
     */
    public static Result GRAB() {
        return new ResultGrab();
    }

    /**
     * Open a menu
     * @param builder a menu builder {@link AbstractMenu.Builder}
     * @return new {@link ResultOpen}
     */
    public static Result OPEN(AbstractMenu.Builder builder) {
        return new ResultOpen(builder);
    }

    /**
     * Close the menu
     * @return new {@link ResultClose}
     */
    public static Result CLOSE() {
        return new ResultClose();
    }

    /**
     * Return to the parent menu if the player closed the menu
     * @return new {@link ResultParent}
     */
    public static Result PARENT() {
        return new ResultParent();
    }

    /**
     * Refresh the menu and its contents
     * @return {@link ResultRefresh}
     */
    public static Result REFRESH() {
        return new ResultRefresh();
    }

    /**
     * Display a message in a {@link TextMenu}
     * @param text the text
     * @return new {@link ResultText}
     */
    public static Result TEXT(String text) {
        return new ResultText(text);
    }

    /**
     * Send the player a message
     * @param message the message
     * @return new {@link ResultMessage}
     */
    public static Result MESSAGE(String message) {
        return new ResultMessage(message);
    }

    public static Result REFRESH_GRAB() {
        return new ResultRefreshGrab();
    }

}
