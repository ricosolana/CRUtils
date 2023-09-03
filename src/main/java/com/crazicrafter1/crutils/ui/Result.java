package com.crazicrafter1.crutils.ui;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import org.apache.commons.lang3.Validate;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiConsumer;

// TODO use blank enum and functional interfaces instead
public enum Result {
    ;

    // Allow the player to equip the item onto their cursor
    public static BiConsumer<AbstractMenu, InventoryClickEvent> grab() {
        return (menu, e) -> e.setCancelled(false);
    }

    // Open a menu by MenuBuilder
    public static BiConsumer<AbstractMenu, InventoryClickEvent> open(AbstractMenu.Builder builder) {
        return (menu, e) -> builder.open(menu.player);
    }

    // Close the currently opened menu
    public static BiConsumer<AbstractMenu, InventoryClickEvent> close() {
        return (menu, e) -> menu.closeInventory(true);
    }

    // Open the parent menu of the current menu
    public static BiConsumer<AbstractMenu, InventoryClickEvent> parent() {
        return (menu, e) -> new BukkitRunnable() {
            @Override
            public void run() {
                menu.status = AbstractMenu.Status.REROUTING;
                menu.builder.parentMenuBuilder.open(menu.player);
            }
        }.runTaskLater(com.crazicrafter1.crutils.Main.getInstance(), 0);
    }

    // Regenerate elements in the current menu
    public static BiConsumer<AbstractMenu, InventoryClickEvent> refresh() {
        return (menu, e) -> new BukkitRunnable() {
            @Override
            public void run() {
                menu.inventory.clear();
                menu.openInventory(false);
            }
        }.runTaskLater(com.crazicrafter1.crutils.Main.getInstance(), 0);
    }

    // Edits the text within the left slot of a text/anvil menu
    public static BiConsumer<AbstractMenu, InventoryClickEvent> text(String text) {
        return (menu, e) -> {
            Validate.isTrue(menu instanceof TextMenu, "Must be used with text menu");

            menu.inventory.setItem(TextMenu.SLOT_LEFT,
                    ItemBuilder.copy(Objects.requireNonNull(
                                    menu.inventory.getItem(TextMenu.SLOT_LEFT)))
                            .name(text, ColorUtil.STRIP_RENDERED).build());
        };
    }

    // Send a message to the player
    public static BiConsumer<AbstractMenu, InventoryClickEvent> message(String message) {
        return (menu, e) -> menu.player.sendMessage(message);
    }

    // Default empty action. Do nothing.
    public static @Nullable BiConsumer<AbstractMenu, InventoryClickEvent> ok() {
        return null;
    }
}
