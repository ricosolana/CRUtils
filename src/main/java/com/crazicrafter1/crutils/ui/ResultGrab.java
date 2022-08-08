package com.crazicrafter1.crutils.ui;

import org.bukkit.event.inventory.InventoryClickEvent;

class ResultGrab extends Result {

    @Override
    public void invoke(AbstractMenu menu, InventoryClickEvent event) {
        event.setCancelled(false);
    }
}
