package com.crazicrafter1.crutils.ui;

import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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
    final Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> clickFunction;

    Button(Function<Player, ItemStack> getItemStackFunction,
           Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> clickFunction) {
        this.getItemStackFunction = getItemStackFunction;
        this.clickFunction = clickFunction;
    }

    public static class Builder {
        Function<Player, ItemStack> getItemStackFunction;

        Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> clickFunction = e -> Result.ok();

        public Builder icon(Function<Player, ItemStack> getItemStackFunction) {
            this.getItemStackFunction = getItemStackFunction;
            return this;
        }

        /**
         * Function that is called everytime this button is interacted with
         * @param clickFunction the listener
         * @return this
         */
        public Builder click(Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> clickFunction) {
            this.clickFunction = clickFunction;
            return this;
        }

        public Builder lmb(Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> leftClickFunction) {
            // TODO this is causing deep recursion
            this.clickFunction = e -> e.clickType.isLeftClick() ? leftClickFunction.apply(e) : clickFunction.apply(e);
            return this;
        }

        public Builder mmb(Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> middleClickFunction) {
            this.clickFunction = e -> e.clickType == ClickType.MIDDLE ? middleClickFunction.apply(e) : clickFunction.apply(e);
            return this;
        }

        public Builder rmb(Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> rightClickFunction) {
            this.clickFunction = e -> e.clickType.isRightClick() ? rightClickFunction.apply(e) : clickFunction.apply(e);
            return this;
        }

        public Builder num(Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> numberKeyFunction) {
            this.clickFunction = e -> e.clickType == ClickType.NUMBER_KEY ? numberKeyFunction.apply(e) : clickFunction.apply(e);
            return this;
        }

        /**
         * Attach an {@link AbstractMenu.Builder} to this button to open on
         * the specified {@link EnumPress}
         * @param menuToOpen the menu to open
         * @param press which press
         * @return this
         */
        public Builder bind(AbstractMenu.Builder menuToOpen,
                            EnumPress press) {
            Validate.notNull(menuToOpen, "Supplied menu must not be null");

            return this.append(press, (clickEvent) -> Result.open(menuToOpen));
        }

        /**
         * Bind a menu to LMB, and assign parent menu
         * @param parentBuilder the parent menu
         * @param menuToOpen the menu to open
         * @return this
         */
        public Builder child(AbstractMenu.Builder parentBuilder, AbstractMenu.Builder menuToOpen) {
            Validate.notNull(parentBuilder);

            menuToOpen.parent(parentBuilder);
            return bind(menuToOpen, EnumPress.LMB);
        }

        public Builder child(AbstractMenu.Builder parentBuilder,
                             AbstractMenu.Builder menuToOpen,
                             Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> rightClickListener) {

            Validate.notNull(parentBuilder);

            menuToOpen.parent(parentBuilder);
            append(EnumPress.RMB, rightClickListener);
            return bind(menuToOpen, EnumPress.LMB);
        }

        /**
         * Combine button presses to listeners
         * @param press
         * @param func
         * @return
         */
        public Builder append(EnumPress press, Function<Event, BiConsumer<AbstractMenu, InventoryClickEvent>> func) {
            if (press != null)
                switch (press) {
                    case LMB: return lmb(func);
                    case MMB: return mmb(func);
                    case RMB: return rmb(func);
                    case NUM: return num(func);
                }

            throw new NullPointerException("Supplied EnumPress must not be null");
        }

        public Button get() {
            return new Button(getItemStackFunction, clickFunction);
        }
    }
}
