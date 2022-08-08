package com.crazicrafter1.crutils.ui;

import org.bukkit.event.inventory.InventoryClickEvent;

class ResultClose extends Result {

    @Override
    public void invoke(AbstractMenu menu, InventoryClickEvent event) {
        menu.closeInventory(true);
    }
}
