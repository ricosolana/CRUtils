package com.crazicrafter1.crutils.ui;

import com.crazicrafter1.crutils.ColorUtil;
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
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractMenu {

    enum Status {
        OPEN,
        REROUTING,
        CLOSED,
    }

    public static void closeAllMenus() {
        openMenus.forEach((uuid, menu) -> menu.closeInventory());
    }

    final static HashMap<UUID, AbstractMenu> openMenus =
            new HashMap<>();

    final Player player;

    final Function<Player, String> getTitleFunction;
    final HashMap<Integer, Button> buttons;
    final Consumer<Player> openFunction;
    final BiFunction<Player, Boolean, BiConsumer<AbstractMenu, InventoryClickEvent>> closeFunction;
    final Builder builder;

    final Button captureButton; // called prior to any other buttons; can cancel other buttons

    Inventory inventory;
    Status status;

    AbstractMenu(Player player,
                 Function<Player, String> getTitleFunction,
                 HashMap<Integer, Button> buttons,
                 Consumer<Player> openFunction,
                 BiFunction<Player, Boolean, BiConsumer<AbstractMenu, InventoryClickEvent>> closeFunction,
                 Builder builder,
                 @Nullable Button.Builder captureButton
    ) {
        Validate.notNull(player, "Player cannot be null");
        Validate.notNull(getTitleFunction);

        this.player = player;

        this.getTitleFunction = getTitleFunction;
        this.buttons = buttons;
        this.openFunction = openFunction;
        this.closeFunction = closeFunction;
        this.builder = builder;
        this.captureButton = captureButton != null ? captureButton.get() : null;
    }

    void openInventory(boolean sendOpenPacket) {
        placeButtons();

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
        closeInventory(true);
    }

    void closeInventory(boolean sendClosePacket) {
        if (status == Status.REROUTING) {
            // The close was caused by a new menu opening
            if (closeFunction != null) //                      player did NOT request
                invokeResult(null, closeFunction.apply(player, false));
        }
        else if (status == Status.OPEN) { // first iteration
            status = Status.CLOSED;

            /*
             * Underlying nms implementation
             *      CraftEventFactory.handleInventoryCloseEvent(this);
             *      this.b.sendPacket(new PacketPlayOutCloseWindow(this.bV.j));
             *      this.o();
             */
            if (sendClosePacket)
                player.closeInventory();

            // The close was directly caused by the player
            if (closeFunction != null)
                //                                              player did request
                invokeResult(null, closeFunction.apply(player, true));
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

        return button.clickFunction.apply(e);
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

        if (status != Status.REROUTING) {
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
        Function<Player, String> getTitleFunction;
        HashMap<Integer, Button.Builder> buttons = new HashMap<>();
        public Builder parentMenuBuilder;
        Consumer<Player> openFunction;

        BiFunction<Player, Boolean, BiConsumer<AbstractMenu, InventoryClickEvent>> closeFunction;

        Button.Builder captureButton;

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
        public Builder onOpen(Consumer<Player> openFunction) {
            this.openFunction = openFunction;
            return this;
        }

        /**
         * Execute a function on close
         *  TODO Debating on whether to remove
         *      this function and reroute detection
         * @param closeFunction the runnable
         * @return this
         */
        public Builder onClose(BiFunction<Player, Boolean, BiConsumer<AbstractMenu, InventoryClickEvent>> closeFunction) {
            Validate.notNull(closeFunction);
            this.closeFunction = closeFunction;
            return this;
        }

        public Builder onClose(Function<Player, BiConsumer<AbstractMenu, InventoryClickEvent>> closeFunction) {
            Validate.notNull(closeFunction);
            this.closeFunction = (p, request) -> closeFunction.apply(p);
            return this;
        }

        public Builder parentOnClose() {
            onClose(p -> Result.parent());
            return this;
        }

        /**
         * Set the parent of this menu
         *
         * @param builder the parent
         * @return this
         */
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
