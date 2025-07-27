package com.crazicrafter1.crutils.ui;

import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Button {

    public static class Event {
        public Player player;
        public ItemStack heldItem;
        public ItemStack clickedItem;
        public boolean shift; // TODO remove
        public int numberKeySlot;
        public final ClickType clickType;
        public final AbstractMenu.Builder menuBuilder;

        public Event(Player player,
                     ItemStack heldItem,
                     ItemStack clickedItem,
                     boolean shift,
                     int numberKeySlot,
                     ClickType clickType,
                     AbstractMenu.Builder menuBuilder) {
            this.player = player;
            this.heldItem = heldItem;
            this.clickedItem = clickedItem;
            this.shift = shift;
            this.numberKeySlot = numberKeySlot;
            this.clickType = clickType;
            this.menuBuilder = menuBuilder;
        }
    }

    Function<Player, ItemStack> getItemStackFunction;
    final EnumMap<ClickType, Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>>> functionMap;

    Button(Function<Player, ItemStack> getItemStackFunction,
           EnumMap<ClickType, Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>>> functionMap) {
        // TODO warn on null Item function
        this.getItemStackFunction = getItemStackFunction;
        this.functionMap = functionMap;
    }

    public static class Builder {
        Function<Player, ItemStack> getItemStackFunction;
        final EnumMap<ClickType, Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>>> functionMap = new EnumMap<>(ClickType.class);

        /**
         * Set the icon for this button
         * @param getItemStackFunction
         * @return
         */
        public Builder icon(Function<Player, ItemStack> getItemStackFunction) {
            this.getItemStackFunction = getItemStackFunction;
            return this;
        }

        /**
         * Trigger a function on a specific click
         * @param clickType
         * @param func
         * @return
         */
        public Builder bind(ClickType clickType, Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> func) {
            functionMap.put(clickType, func);
            return this;
        }

        public Builder bind(Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> func, ClickType... clickTypes) {
            for (ClickType clickType : clickTypes) {
                bind(clickType, func);
            }
            return this;
        }

        /**
         * Function that is called everytime this button is interacted with
         * @param clickFunction the listener
         * @return this
         */
        public Builder click(Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> clickFunction) {
            return bind(ClickType.UNKNOWN, clickFunction);
        }

        public Builder lmb(Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> leftClickFunction) {
            bind(ClickType.LEFT, leftClickFunction);
            return bind(ClickType.SHIFT_LEFT, leftClickFunction);
        }

        public Builder mmb(Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> middleClickFunction) {
            return bind(ClickType.MIDDLE, middleClickFunction);
        }

        public Builder rmb(Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> rightClickFunction) {
            bind(ClickType.RIGHT, rightClickFunction);
            return bind(ClickType.SHIFT_RIGHT, rightClickFunction);
        }

        public Builder num(Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> numberKeyFunction) {
            return bind(ClickType.NUMBER_KEY, numberKeyFunction);
        }

        /**
         * Opens a menu on click.
         * @param menuToOpen menu to open
         * @param press ClickType
         * @return this
         */
        private Builder route(@Nonnull AbstractMenu.Builder menuToOpen,
                              ClickType press) {
            Validate.notNull(menuToOpen, "Supplied menu must not be null");

            return this.bind(press, (clickEvent) -> Result.open(menuToOpen));
        }

        /**
         * Opens a child menu on LMB. This becomes revisitable.
         * @param parentBuilder the parent menu
         * @param menuToOpen the menu to open
         * @return this
         */
        // TODO
        //  parent menu should be assigned when button added to menu
        //      AKA internal, not manually by end-user
        @Deprecated
        public Builder child(AbstractMenu.Builder parentBuilder, AbstractMenu.Builder menuToOpen) {
            menuToOpen.parent(parentBuilder);
            return route(menuToOpen, ClickType.LEFT);
        }

        public Button get() {
            return new Button(getItemStackFunction, new EnumMap<>(functionMap));
        }
    }
}
