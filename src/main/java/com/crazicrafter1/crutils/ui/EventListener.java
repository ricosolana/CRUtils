package com.crazicrafter1.crutils.ui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EventListener implements Listener {

    public EventListener(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void event(InventoryClickEvent event) {
        AbstractMenu menu = AbstractMenu.openMenus.get(event.getWhoClicked().getUniqueId());

        if (menu != null) {
            // Cancel double clicks, because they are just too destructive and complicated to handle
            if (event.getClick() == ClickType.DOUBLE_CLICK) {
                event.setCancelled(true);
                return;
            }

            // Performing a direct '==' check doesn't work, because spigot == dumb
            //if (event.getClickedInventory() != menu.inventory)
            if (!menu.inventory.equals(event.getClickedInventory())) {
                // Player can shift-click items INTO menu, which is bad
                if (event.isShiftClick()) {
                    event.setCancelled(true);
                }
                return;
            }

            menu.onInventoryClick(event);
        }
    }

    @EventHandler
    private void event(InventoryCloseEvent event) {
        AbstractMenu menu = AbstractMenu.openMenus.get(event.getPlayer().getUniqueId());

        // If the event is stupid
        // in the case of where the menu was
        // closed completely or a new menu was opened
        if (menu != null) {
            menu.onInventoryClose(event);
        }
    }

    @EventHandler
    private void event(InventoryDragEvent event) {
        Player p = (Player) event.getWhoClicked();

        AbstractMenu menu = AbstractMenu.openMenus.get(p.getUniqueId());
        if (menu != null) {
            // Mainly for performing a selective drag event (allow, but under supervision)
            if (event.getInventory().equals(menu.inventory))
                menu.onInventoryDrag(event);
        }
    }

}
