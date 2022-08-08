package com.crazicrafter1.crutils.ui;

import org.bukkit.event.inventory.InventoryClickEvent;

class ResultMessage extends Result {

    public String message;

    public ResultMessage(String message) {
        this.message = message;
    }

    @Override
    public void invoke(AbstractMenu menu, InventoryClickEvent event) {
        menu.player.sendMessage(message);
    }
}
