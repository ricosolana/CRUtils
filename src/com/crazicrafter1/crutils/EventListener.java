package com.crazicrafter1.crutils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;

public class EventListener implements Listener {

    public EventListener(JavaPlugin javaPlugin) {
        Bukkit.getPluginManager().registerEvents(this, javaPlugin);
    }

    /*
     * Drag can still happen even on one slot which is extremely fucking annoying
     * for some reason someone thought this was a good idea
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void event(InventoryDragEvent e) {
        Player p = (Player)e.getWhoClicked();

        if (e.getRawSlots().size() == 1) {
            int rawSlot = 0;
            for (int slot : e.getRawSlots()) {
                rawSlot = slot;
            }

            ClickType clickType = e.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;

            // dispatch a normal click event, because most plugins are probably not taking advantage
            // of this little weird annoying quirk
            e.setCancelled(false);

            InventoryClickEvent event = new InventoryClickEvent(e.getView(), e.getView().getSlotType(rawSlot),
                    rawSlot, clickType, InventoryAction.PLACE_ALL);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled())
                e.setCancelled(true);
        }
    }

}
