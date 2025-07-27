package com.crazicrafter1.crutils.ui;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Main;
import org.apache.commons.lang3.Validate;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiConsumer;

// TODO use blank enum and functional interfaces instead
public enum Result {
    ;

    // Push new nav menu
    //public static BiConsumer<AbstractMenu, InventoryClickEvent> push(AbstractMenu.Builder builder) {
    //    return (menu, e) -> {
    //        // TODO set mode to PUSH
    //        menu.status = AbstractMenu.Status.PUSH_ROUTE;
    //        builder.open(menu.player);
    //    };
    //}

    // Allow the player to equip the item onto their cursor
    public static BiConsumer<AbstractMenu, InventoryClickEvent> grab() {
        return (menu, e) -> {
            Validate.notNull(e, "Do not return '::grab' inside ::onNavPop()!");
            e.setCancelled(false);
        };
    }

    // Open a menu by MenuBuilder
    public static BiConsumer<AbstractMenu, InventoryClickEvent> open(AbstractMenu.Builder builder) {
        return (menu, e) -> {
            // TODO wha..?
            //menu.status = AbstractMenu.Status.PUSH;
            builder.open(menu.player);
        };
    }

    public static BiConsumer<AbstractMenu, InventoryClickEvent> abort() {
        return (menu, e) -> {
            // panic out
            //  which means just DO NOT call sub closes
            menu.status = AbstractMenu.Status.CLOSE_NO_POP;
            menu.closeInventory(true);
        };
    }

    // Close the currently opened menu
    public static BiConsumer<AbstractMenu, InventoryClickEvent> close() {
        return (menu, e) -> {
            // graceful
            menu.status = AbstractMenu.Status.CLOSE_REQUESTED;
            menu.closeInventory(true);
        };
    }

    @Deprecated
    public static BiConsumer<AbstractMenu, InventoryClickEvent> parent() {
        return pop();
    }

    // Open the parent menu of the current menu
    public static BiConsumer<AbstractMenu, InventoryClickEvent> pop() {
        return (menu, e) -> new BukkitRunnable() {
            @Override
            public void run() {
                Main.notifier.debug("Result trigger: parent");
                menu.status = AbstractMenu.Status.POP;
                menu.builder.parentMenuBuilder.open(menu.player);
                Main.notifier.debug("Result post p");
            }
        }.runTaskLater(Main.getInstance(), 0);
    }

    // Regenerate elements in the current menu
    public static BiConsumer<AbstractMenu, InventoryClickEvent> refresh() {
        return (menu, e) -> new BukkitRunnable() {
            @Override
            public void run() {
                menu.inventory.clear();
                menu.openInventory(false);
            }
        }.runTaskLater(Main.getInstance(), 0);
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
