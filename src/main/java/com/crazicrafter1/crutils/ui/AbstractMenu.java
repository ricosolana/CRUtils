package com.crazicrafter1.crutils.ui;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.Main;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractMenu {

    enum Status {
        OPEN,
        PUSH,
        POP,
        CLOSE_REQUESTED, // Result.close() used
        CLOSE_NO_POP, // Result.
        CLOSED,
    }

    // TODO this doesn't work as expected
    public static void closeAllMenus() {
        openMenus.forEach((uuid, menu) -> menu.closeInventory(false, true));
    }

    final static Map<UUID, AbstractMenu> openMenus = new HashMap<>();

    final Player player;
    final HashMap<Integer, Button> buttons;
    final Builder builder;

    // TODO do not use a 'BUTTON'
    final Button captureButton; // called prior to any other buttons; can cancel other buttons

    Inventory inventory;
    Status status;

    AbstractMenu(Player player,
                 HashMap<Integer, Button> buttons,
                 Builder builder,
                // TODO remove capture as BUTTON (make as independent)
                 @Nullable Button.Builder captureButton
    ) {
        Validate.notNull(player);

        this.player = player;
        this.buttons = buttons;
        this.builder = builder;
        this.captureButton = captureButton != null ? captureButton.get() : null;
    }

    protected AbstractMenu.Builder getBuilder() {
        return builder;
    }

    void openInventory(boolean sendOpenPacket) {
        placeButtons();

        // TODO should trigger push here
        //Main.notifier.debug("AbstractMenu openInv: " + status);

        openMenus.put(player.getUniqueId(), this);
        this.status = Status.OPEN;
    }

    final void placeButtons() {
        for (Map.Entry<Integer,Button> entry : buttons.entrySet()) {
            Function<Player, ItemStack> supplier = entry.getValue().getItemStackFunction;
            if (supplier != null) {
                ItemStack itemStack = supplier.apply(player);
                inventory.setItem(entry.getKey(), itemStack);
            }
        }
    }

    final void closeInventory() {
        closeInventory(true, false);
    }

    final void closeInventory(boolean sendClosePacket) {
        closeInventory(sendClosePacket, false);
    }

    // 'discrete' is for server stop, so onClose() functions can run,
    //  and do potentially important stuff like save data, if set...
    void closeInventory(boolean sendClosePacket, boolean discrete) {
        // POP_ROUTE generally means this was triggered, not directly by player
        Main.notifier.debug("AbstractMenu::closeInventory(), " + sendClosePacket + ", " + status.name());

        var popFunction = builder.navSelfPoppedFunction;

        if (status == Status.POP) {
            // The close was caused by a new menu opening
            if (popFunction != null) {
                Main.notifier.debug("route pop");
                invokeResult(null, popFunction.apply(player, false));
            }
        }
        // otherwise, no explicit popping was requested
        //  (player pressed 'esc', and there was no parent onClose requested)
        //  !!! BUT !!!
        //      we still need to trigger onNavPops for ALL submenus,
        //      we just won't reopen them to player...
        else if (status != Status.CLOSED) { // first iteration
            // TODO might change to
            var prevStatus = status;

            status = Status.CLOSED;

            /*
             * Underlying nms implementation
             *      CraftEventFactory.handleInventoryCloseEvent(this);
             *      this.b.sendPacket(new PacketPlayOutCloseWindow(this.bV.j));
             *      this.o();
             */
            if (sendClosePacket)
                player.closeInventory();

            if (prevStatus == Status.CLOSE_NO_POP || prevStatus == ) {
                // dont do anything
                Main.notifier.debug("abort");
                return;
            }

            // The close was directly caused by the player
            BiConsumer<AbstractMenu, InventoryClickEvent> res = null;

            if (popFunction != null) {
                Main.notifier.debug("force pop");

                res = popFunction.apply(player, true);
                if (!discrete) {
                    invokeResult(null, res);
                }
            }

            // in 99% of cases, a NONNULL res means 'Result.parent()'
            //  otherwise, we *should* error-out
            //  If no result, panic call all POPs
            if (res == null) {
                Main.notifier.debug("non-res");

                int max_depth = 10;

                // invoke parent pops,
                AbstractMenu.Builder parent = builder;
                while (true) {
                    parent = parent.parentMenuBuilder;
                    if (parent == null) {
                        // If we are the lsat menu left, we already popped
                        break;
                    }

                    Main.notifier.debug("parent " + parent);

                    var parentPopFunc = parent.navSelfPoppedFunction;
                    if (parentPopFunc != null) {
                        var resParent = parentPopFunc.apply(player, true);

                        Main.notifier.debug("parent onPopped");
                        // We do not really do anything with result,
                        //  if we are just popping up the chain...
                    }

                    // recurse protection
                    //  this shouldn't really happen, but safety...
                    if (max_depth-- <= 0) {
                        break;
                    }
                }
            }
        }
    }

    final @Nullable BiConsumer<AbstractMenu, InventoryClickEvent> invokeButtonAt(InventoryClickEvent event) {
        // Test capture button first
        BiConsumer<AbstractMenu, InventoryClickEvent> result = invokeButton(event, captureButton);
        if (result != null)
            return result;

        Button button = buttons.get(event.getSlot());
        return invokeButton(event, button);
    }

    private @Nullable BiConsumer<AbstractMenu, InventoryClickEvent> invokeButton(InventoryClickEvent event, @Nullable Button button) {
        if (button == null)
            return null;

        Button.Event e =
                new Button.Event(player,
                        Objects.requireNonNull(event.getCursor()).getType() != Material.AIR ?
                                event.getCursor() : null,
                        event.getCurrentItem(),
                        event.isShiftClick(),
                        event.getHotbarButton(), // -1 if not NUMBER_KEY
                        event.getClick(),
                        builder
                );

        Function<Button.Event, BiConsumer<AbstractMenu, InventoryClickEvent>> function = button.functionMap.get(event.getClick());
        if (function == null) function = button.functionMap.get(ClickType.UNKNOWN);

        return function != null ? function.apply(e) : Result.ok();
    }

    final void invokeResult(InventoryClickEvent event, @Nullable BiConsumer<AbstractMenu, InventoryClickEvent> result) {
        if (result != null)
            result.accept(this, event);
    }

    abstract void onInventoryClick(InventoryClickEvent event);

    final void onInventoryDrag(InventoryDragEvent event) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (event.getRawSlots().contains(slot)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    void onInventoryClose(InventoryCloseEvent event) {
        closeInventory(false);

        //Main.getInstance().no

        if (status != Status.POP) {
            openMenus.remove(player.getUniqueId());
        }
    }

    void button(int x, int y, Button button) {
        buttons.put(y*9 + x, button);
    }

    void delButton(int x, int y) {
        buttons.remove(y*9 + x);
    }

    public static abstract class Builder {
        // TODO how to implement animations?
        //  like moving stuff over ticks?
        //  this is where things would start to move towards dynamic
        //  graphics and flutter-like rendering...
        //  ---
        //  this is a simple minecraft menu plugin, ...
        //  going any further would start to break what I have set
        //  everything is currently based off events, not deferred updates...

        Function<Player, String> getTitleFunction;
        HashMap<Integer, Button.Builder> buttons = new HashMap<>();
        public Builder parentMenuBuilder;

        // Fired on 'self' when 'other' menu is pushed ('self' becomes parent)
        //Consumer<Player> navPushRouteFunction;
        // Fired on 'self' when I am pushed ('self' becomes child)
        //Consumer<Player> navSelfPushedFunction;
        // Fired on 'self' when other menu is popped
        //BiFunction<Player, Boolean, BiConsumer<AbstractMenu, InventoryClickEvent>> navPopRouteFunction;
        // Fired on 'self' when I am popped
        BiFunction<Player, Boolean, BiConsumer<AbstractMenu, InventoryClickEvent>> navSelfPoppedFunction;

        // TODO use @function instead of Button for capture
        Button.Builder captureButton;

        @Deprecated
        public Builder title(String staticTitle) {
            return this.title(p -> staticTitle);
        }

        public Builder title(Function<Player, String> getTitleFunction) {
            return this.title(getTitleFunction, ColorUtil.RENDER_ALL);
        }

        public Builder title(Function<Player, String> getTitleFunction, ColorUtil titleColorMode) {
            Validate.notNull(getTitleFunction);
            this.getTitleFunction = p -> titleColorMode.a(getTitleFunction.apply(p));
            return this;
        }

        /**
         * Execute a function on open
         *
         * @param openFunction the open function
         * @return this
         */
        //@Deprecated
        //public Builder onOpen(Consumer<Player> openFunction) {
        //    return this.onNavPush(openFunction);
        //}

        /**
         * Execute a function on close
         *  TODO Debating on whether to remove
         *      this function and reroute detection
         * @param closeFunction the runnable
         * @return this
         */
        @Deprecated
        public Builder onClose(BiFunction<Player, Boolean, BiConsumer<AbstractMenu, InventoryClickEvent>> closeFunction) {
            return onNavPop(closeFunction);
        }

        @Deprecated
        public Builder onClose(Function<Player, BiConsumer<AbstractMenu, InventoryClickEvent>> closeFunction) {
            return onNavPop(closeFunction);
        }

        // TODO
        //  flutter-like navigator control

        // Push: A child menu is created
        //public Builder onNavPush(Consumer<Player> pushFunction) {
        //    //return onClose((p, request) -> closeFunction.apply(p));
        //    Validate.notNull(pushFunction);
        //    this.navSelfPushedFunction = pushFunction;
        //    return this;
        //}

        // Pop: We nav to parent
        public Builder onNavPop(BiFunction<Player, Boolean, BiConsumer<AbstractMenu, InventoryClickEvent>> popFunction) {
            Validate.notNull(popFunction);
            this.navSelfPoppedFunction = popFunction;
            return this;
        }

        public Builder onNavPop(Function<Player, BiConsumer<AbstractMenu, InventoryClickEvent>> popFunction) {
            return onNavPop((p, request) -> popFunction.apply(p));
        }

        public Builder onNavPop(Supplier<BiConsumer<AbstractMenu, InventoryClickEvent>> supplier) {
            //Supplier<BiConsumer<AbstractMenu, InventoryClickEvent>> grabSupplier = Result::grab;;
            //Validate.isTrue(supplier != grabSupplier, "Hey dummy, this makes NO sense!");
            return onNavPop((p, req) -> supplier.get());
        }

        @Deprecated
        public Builder parentOnClose() {
            return popOnClose();
            //return onClose(p -> Result.parent());
        }

        //private void composite()

        public Builder popOnClose() {
            return onNavPop(Result::pop);
            //return onNavPop((p, req) -> Result.pop());
            //return onClose(p -> Result.parent());
            // TODO
            //  what are we trying to do here?
            //
            //  popping / pushing both refer to sub menu navigation and escaping
            //      but by default, sub menus do not exist
            //var inner_func = this.navSelfPoppedFunction;
            //if (inner_func != null) {
            //    navSelfPoppedFunction = (player, request) -> {
            //        var res = inner_func.apply(player, request);
            //        // If the initial onPop calls do nothing, then give it a try:
            //        return Objects.requireNonNullElseGet(res, Result::parent);
            //    };
            //}

            //return this;
        }

        /**
         * Set the parent of this menu
         *
         * @param builder the parent
         * @return this
         */
        @Deprecated
        /// fixme too tacky open-ended usage
        public final Builder parent(@Nonnull Builder builder) {
            Validate.notNull(builder);
            parentMenuBuilder = builder;
            return this;
        }

        /// fixme too tacky open-ended usage
        final Builder button(int slot, Button.Builder button) {
            return button(slot, button, null);
        }

        /// fixme too tacky open-ended usage
        final Builder button(int slot, @Nonnull Button.Builder button, @Nullable Button.Builder[] resOld) {
            Validate.notNull(button);
            Button.Builder b = buttons.putIfAbsent(slot, button);
            if (resOld != null) resOld[0] = b;
            return this;
        }

        /// fixme too tacky open-ended usage
        @Nonnull
        final Button.Builder getOrMakeButton(int slot, Function<Player, ItemStack> getItemStackFunction) {
            Button.Builder[] old = new Button.Builder[1];
            button(slot, new Button.Builder().icon(getItemStackFunction), old);

            if (old[0] == null)
                old[0] = buttons.get(slot);

            return old[0];
        }

        // Called when any element is clicked, regardless of button or not
        public Builder capture(Button.Builder button) {
            this.captureButton = button;

            return this;
        }

        /**
         * Constructs and opens the {@link AbstractMenu} to the player
         * @param player the player
         * @return the menu
         */
        public abstract AbstractMenu open(Player player);
    }
}
