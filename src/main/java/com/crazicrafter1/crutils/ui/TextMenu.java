package com.crazicrafter1.crutils.ui;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.TriFunction;
import com.crazicrafter1.crutils.Util;
import net.wesjd.anvilgui.version.VersionMatcher;
import net.wesjd.anvilgui.version.VersionWrapper;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class TextMenu extends AbstractMenu {

    public static final int SLOT_LEFT = 0;
    public static final int SLOT_RIGHT = 1;
    public static final int SLOT_OUTPUT = 2;

    private static final VersionWrapper WRAPPER = new VersionMatcher().match();

    private int containerId;

    private TextMenu(Player player,
                     Function<Player, String> getTitleFunction,
                     HashMap<Integer, Button> buttons,
                     Consumer<Player> openFunction,
                     BiFunction<Player, Boolean, BiConsumer<AbstractMenu, InventoryClickEvent>> closeFunction,
                     Builder builder,
                     @Nullable Button.Builder captureButton
    ) {
        super(player, getTitleFunction, buttons, openFunction, closeFunction, builder, captureButton);
    }

    @Override
    void openInventory(boolean sendOpenPacket) {
        if (openFunction != null)
            openFunction.accept(player);

        WRAPPER.handleInventoryCloseEvent(player);
        WRAPPER.setActiveContainerDefault(player);

        final Object titleChatComponent = WRAPPER.literalChatComponent(getTitleFunction.apply(player));

        VersionWrapper.AnvilContainerWrapper container = WRAPPER.newContainerAnvil(player, titleChatComponent);

        containerId = WRAPPER.getNextContainerId(player, container);

        WRAPPER.sendPacketOpenWindow(player, containerId, titleChatComponent);
        WRAPPER.setActiveContainer(player, container);
        WRAPPER.setActiveContainerId(container, containerId);
        WRAPPER.addActiveContainerSlotListener(container, player);

        inventory = container.getBukkitInventory();

        super.openInventory(sendOpenPacket);
    }

    @Override
    void closeInventory(boolean sendClosePacket) {
        if (status != Status.OPEN)
            return;

        if (sendClosePacket) {
            WRAPPER.handleInventoryCloseEvent(player);
            WRAPPER.setActiveContainerDefault(player);
            WRAPPER.sendPacketCloseWindow(player, containerId);
        }

        super.closeInventory(false);
    }

    @Override
    void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        final ItemStack clicked = inventory.getItem(SLOT_OUTPUT);
        if (clicked == null || clicked.getType() == Material.AIR) return;

        invokeResult(event, invokeButtonAt(event));
    }

    public static class TBuilder extends Builder {

        @Override
        public TBuilder title(@Nonnull Function<Player, String> getTitleFunction) {
            return (TBuilder) super.title(getTitleFunction);
        }

        @Override
        public TBuilder title(Function<Player, String> getTitleFunction, ColorUtil titleColorMode) {
            return (TBuilder) super.title(getTitleFunction, titleColorMode);
        }

        @Override
        public TBuilder onOpen(@Nonnull Consumer<Player> openFunction) {
            return (TBuilder) super.onOpen(openFunction);
        }

        @Override
        public TBuilder onClose(@Nonnull BiFunction<Player, Boolean, BiConsumer<AbstractMenu, InventoryClickEvent>> closeFunction) {
            return (TBuilder) super.onClose(closeFunction);
        }

        @Override
        public TBuilder onClose(@Nonnull Function<Player, BiConsumer<AbstractMenu, InventoryClickEvent>> closeFunction) {
            return (TBuilder) super.onClose(closeFunction);
        }

        public TBuilder onComplete(@Nonnull TriFunction<Player, String, TBuilder, BiConsumer<AbstractMenu, InventoryClickEvent>> completeFunction) {
            return (TBuilder) this.button(SLOT_OUTPUT, new Button.Builder()
                    .lmb((interact) -> completeFunction.apply(interact.player,
                            Util.def(ItemBuilder.mut(interact.clickedItem).getName(), ""), (TBuilder) interact.menuBuilder))
            );
        }

        @Override
        public TBuilder parentOnClose() {
            return (TBuilder) super.parentOnClose();
        }

        /**
         * Set left item text. Defaults to ColorMode.STRIP
         *
         * @param itemTextFunction The supplier text of the item
         * @return The {@link TBuilder} instance
         * @throws IllegalArgumentException if the text is null
         */
        public TBuilder leftRaw(@Nonnull Function<Player, String> itemTextFunction) {
            return this.leftRaw(itemTextFunction, ColorUtil.INVERT_RENDERED, null, ColorUtil.RENDER_ALL);
        }

        public TBuilder leftRaw(@Nonnull Function<Player, String> itemTextFunction,
                                @Nullable Function<Player, String> itemLoreFunction) {
            return this.leftRaw(itemTextFunction, ColorUtil.INVERT_RENDERED, itemLoreFunction, ColorUtil.RENDER_ALL);
        }

        public TBuilder leftRaw(@Nonnull Function<Player, String> itemTextFunction,
                                @Nullable Function<Player, String> itemLoreFunction, @Nonnull ColorUtil loreColorMode) {
            return this.leftRaw(itemTextFunction, ColorUtil.INVERT_RENDERED, itemLoreFunction, loreColorMode);
        }

        public TBuilder leftRaw(@Nonnull Function<Player, String> itemTextFunction, ColorUtil nameColorMode) {
            return this.leftRaw(itemTextFunction, nameColorMode, null, ColorUtil.RENDER_MARKERS);
        }

        public TBuilder leftRaw(@Nonnull Function<Player, String> itemNameFunction, @Nonnull ColorUtil nameColorMode,
                                @Nullable Function<Player, String> itemLoreFunction) {
            return leftRaw(itemNameFunction, nameColorMode, itemLoreFunction, ColorUtil.RENDER_MARKERS);
        }

        public TBuilder leftRaw(@Nonnull Function<Player, String> itemNameFunction, @Nonnull ColorUtil nameColorMode,
                                @Nullable Function<Player, String> itemLoreFunction, @Nonnull ColorUtil loreColorMode) {
            Validate.notNull(itemNameFunction, "itemNameFunction must not be null");
            Validate.notNull(nameColorMode, "nameColorMode must not be null");
            Validate.isTrue(nameColorMode == ColorUtil.STRIP_RENDERED || nameColorMode == ColorUtil.INVERT_RENDERED, "nameColorMode must be STRIP_RENDERED or INVERT_RENDERED");
            Validate.notNull(loreColorMode, "loreColorMode must not be null");

            return (TBuilder) this.button(SLOT_LEFT, new Button.Builder()
                    .icon((p) -> ItemBuilder.copy(Material.IRON_SWORD)
                            .name(Objects.requireNonNull(itemNameFunction.apply(p), "itemNameFunction must not return null"), nameColorMode)
                            .lore(itemLoreFunction != null ? Objects.requireNonNull(itemLoreFunction.apply(p), "itemLoreFunction must not return null") : null, loreColorMode).build()));
        }



        public TBuilder right(@Nonnull Function<Player, String> itemNameFunction) {
            return this.right(itemNameFunction, ColorUtil.RENDER_ALL, null, ColorUtil.RENDER_ALL);
        }

        public TBuilder right(@Nonnull Function<Player, String> itemNameFunction,
                              @Nullable Function<Player, String> itemLoreFunction) {
            return this.right(itemNameFunction, ColorUtil.RENDER_ALL, itemLoreFunction, ColorUtil.RENDER_ALL);
        }

        public TBuilder right(@Nonnull Function<Player, String> itemNameFunction,
                              @Nullable Function<Player, String> itemLoreFunction, @Nonnull ColorUtil loreColorMode) {
            return this.right(itemNameFunction, ColorUtil.RENDER_ALL, itemLoreFunction, loreColorMode);
        }

        public TBuilder right(@Nonnull Function<Player, String> itemNameFunction, @Nonnull ColorUtil nameColorMode) {
            return this.right(itemNameFunction, nameColorMode, null, ColorUtil.RENDER_ALL);
        }

        public TBuilder right(@Nonnull Function<Player, String> itemNameFunction, @Nonnull ColorUtil nameColorMode,
                              @Nullable Function<Player, String> itemLoreFunction) {
            return this.right(itemNameFunction, nameColorMode, itemLoreFunction, ColorUtil.RENDER_ALL);
        }

        public TBuilder right(@Nonnull Function<Player, String> itemNameFunction, @Nonnull ColorUtil nameColorMode,
                              @Nullable Function<Player, String> itemLoreFunction, @Nonnull ColorUtil loreColorMode) {
            Validate.notNull(itemNameFunction, "itemNameFunction must not be null");
            Validate.notNull(nameColorMode, "nameColorMode must not be null");
            Validate.notNull(loreColorMode, "loreColorMode must not be null");
            return (TBuilder) super.button(SLOT_RIGHT, new Button.Builder()
                    .icon((p) -> ItemBuilder.copy(Material.IRON_SWORD)
                            .name(Objects.requireNonNull(itemNameFunction.apply(p), "Name function returns null"), nameColorMode)
                            .lore(itemLoreFunction != null ? Objects.requireNonNull(itemLoreFunction.apply(p), "Lore function returns null") : null, loreColorMode).build()));
        }

        @Override
        public TBuilder capture(Button.Builder button) {
            return (TBuilder) super.capture(button);
        }

        @Override
        public TextMenu open(@Nonnull Player player) {
            HashMap<Integer, Button> btns = new HashMap<>();
            buttons.forEach((i, b) -> btns.put(i, b.get()));

            // validate slots were assigned
            Validate.isTrue(buttons.containsKey(SLOT_LEFT), "Must assign left item to text menu");

            TextMenu textMenu = new TextMenu(player,
                                             getTitleFunction,
                                             btns,
                                             openFunction,
                                             closeFunction,
                                             this,
                                             captureButton
                    );

            textMenu.openInventory(true);

            return textMenu;
        }
    }

}
