package com.crazicrafter1.crutils.ui;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class Button {

    public static class Event {
        public Player player;
        public ItemStack heldItem;
        public ItemStack clickedItem;
        public boolean shift;
        public int numberKeySlot;
        public final AbstractMenu.Builder menuBuilder;

        public Event(Player player,
                     ItemStack heldItem,
                     ItemStack clickedItem,
                     boolean shift,
                     int numberKeySlot,
                     AbstractMenu.Builder menuBuilder) {
            this.player = player;
            this.heldItem = heldItem;
            this.clickedItem = clickedItem;
            this.shift = shift;
            this.numberKeySlot = numberKeySlot;
            this.menuBuilder = menuBuilder;
        }
    }

    Function<Player, ItemStack> getItemStackFunction;
    final Function<Event, Result> leftClickFunction;
    final Function<Event, Result> middleClickFunction;
    final Function<Event, Result> rightClickFunction;
    final Function<Event, Result> numberKeyFunction;

    Button(Function<Player, ItemStack> getItemStackFunction,
           Function<Event, Result> leftClickFunction,
           Function<Event, Result> middleClickFunction,
           Function<Event, Result> rightClickFunction,
           Function<Event, Result> numberKeyFunction) {
        this.getItemStackFunction = getItemStackFunction;
        this.leftClickFunction = leftClickFunction;
        this.middleClickFunction = middleClickFunction;
        this.rightClickFunction = rightClickFunction;
        this.numberKeyFunction = numberKeyFunction;
    }

    public static class Builder {
        Function<Player, ItemStack> getItemStackFunction;
        Function<Event, Result> leftClickFunction;
        Function<Event, Result> middleClickFunction;
        Function<Event, Result> rightClickFunction;
        Function<Event, Result> numberKeyFunction;

        public Builder icon(Function<Player, ItemStack> getItemStackFunction) {
            this.getItemStackFunction = getItemStackFunction;
            return this;
        }

        public Builder lmb(Function<Event, Result> leftClickFunction) {
            this.leftClickFunction = leftClickFunction;
            return this;
        }

        public Builder mmb(Function<Event, Result> middleClickFunction) {
            this.middleClickFunction = middleClickFunction;
            return this;
        }

        public Builder rmb(Function<Event, Result> rightClickFunction) {
            this.rightClickFunction = rightClickFunction;
            return this;
        }

        public Builder num(Function<Event, Result> numberKeyFunction) {
            this.numberKeyFunction = numberKeyFunction;
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

            return this.append(press, (clickEvent) -> Result.OPEN(menuToOpen));
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
                             Function<Event, Result> rightClickListener) {

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
        public Builder append(EnumPress press, Function<Event, Result> func) {
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
            return new Button(getItemStackFunction,
                              leftClickFunction,
                              middleClickFunction,
                              rightClickFunction,
                              numberKeyFunction);
        }
    }
}
