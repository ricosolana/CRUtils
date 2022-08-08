package com.crazicrafter1.crutils.ui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

class ResultRefresh extends Result {

    @Override
    public void invoke(AbstractMenu menu, InventoryClickEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                menu.inventory.clear();
                menu.openInventory(false);
            }
        }.runTaskLater(com.crazicrafter1.crutils.Main.getInstance(), 0);
    }
}
