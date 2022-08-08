package com.crazicrafter1.crutils.ui;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleMenu extends AbstractMenu {
    private final ItemStack background;
    private final int columns;

    SimpleMenu(Player player,
               Function<Player, String> getTitleFunction,
               HashMap<Integer, Button> buttons,
               Consumer<Player> openFunction,
               BiFunction<Player, Boolean, Result> closeFunction,
               Builder thisBuilder,
               ItemStack background,
               int columns) {
        super(player, getTitleFunction, buttons, openFunction, closeFunction, thisBuilder);
        this.background = background;
        this.columns = columns;
    }

    @Override
    void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        invokeResult(event, invokeButtonAt(event));
    }

    void button(int x, int y, Button button) {
        buttons.put(y*9 + x, button);
    }

    void delButton(int x, int y) {
        buttons.remove(y*9 + x);
    }

    @Override
    void openInventory(boolean sendOpenPacket) {
        if (openFunction != null) {
            openFunction.accept(player);
        }

        if (sendOpenPacket) {
            this.inventory = Bukkit.createInventory(null, columns * 9, getTitleFunction.apply(player));
            player.openInventory(inventory);
        }

        if (background != null) {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, background);
            }
        }

        super.openInventory(sendOpenPacket);
    }

    public static class SBuilder extends Builder {
        final static ItemStack PREV_1 = ItemBuilder.copyOf(Material.ARROW).name("&cBack").build();
        private static final ItemStack BACKGROUND_1 = ItemBuilder.fromModernMaterial("BLACK_STAINED_GLASS_PANE").name(" ").build();

        ItemStack background;
        private final int columns;

        public SBuilder(int columns) {
            Validate.isTrue(columns >= 1, "columns must be greater or equal to 1 (" + columns + ")");
            Validate.isTrue(columns <= 6, "columns must be less or equal to 6 (" + columns + ")");
            this.columns = columns;
        }

        @Override
        public SBuilder title(Function<Player, String> getTitleFunction) {
            return (SBuilder) super.title(getTitleFunction);
        }

        @Override
        public SBuilder title(Function<Player, String> getTitleFunction, ColorUtil titleColorMode) {
            return (SBuilder) super.title(getTitleFunction, titleColorMode);
        }

        @Override
        public SBuilder onOpen(Consumer<Player> openFunction) {
            return (SBuilder) super.onOpen(openFunction);
        }

        @Override
        public SBuilder onClose(BiFunction<Player, Boolean, Result> closeFunction) {
            return (SBuilder) super.onClose(closeFunction);
        }

        @Override
        public SBuilder onClose(Function<Player, Result> closeFunction) {
            return (SBuilder) super.onClose(closeFunction);
        }

        /**
         * Bind a sub menu with LMB as the default button
         *
         * @param x horizontal position
         * @param y vertical position
         * @param getItemStackFunction button icon
         * @param builder the menu to eventually open
         * @return this
         */
        public SBuilder childButton(int x, int y,
                                    Function<Player, ItemStack> getItemStackFunction, Builder builder) {
            return this.childButton(x, y, getItemStackFunction, builder, true);
        }

        public SBuilder childButton(int x, int y,
                                    Function<Player, ItemStack> getItemStackFunction, Builder menuToOpen,
                                    Function<Button.Event, Result> rightClickListener) {

            return this.childButton(x, y, getItemStackFunction, menuToOpen, rightClickListener, true);
        }

        public SBuilder childButton(int x, int y,
                                    Function<Player, ItemStack> getItemStackFunction, Builder builder, boolean addCondition) {
            if (addCondition) {
                builder.parent(this);

                return this.bind(x, y, EnumPress.LMB, getItemStackFunction, builder);
            }
            return this;
        }

        public SBuilder childButton(int x, int y,
                                    Function<Player, ItemStack> getItemStackFunction, Builder menuToOpen,
                                    Function<Button.Event, Result> rightClickListener, boolean addCondition) {
            if (addCondition) {
                menuToOpen.parent(this);

                return this.button(x, y, new Button.Builder()
                        .icon(getItemStackFunction)
                        .bind(menuToOpen, EnumPress.LMB)
                        .rmb(rightClickListener));
            }
            return this;
        }

        public SBuilder button(int x, int y, Button.Builder button) {
            return (SBuilder) super.button(slotOf(x, y), button);
        }

        public SBuilder background() {
            return this.background(BACKGROUND_1);
        }

        public SBuilder background(ItemStack itemStack) {
            Validate.notNull(itemStack);
            Validate.isTrue(itemStack.getType() != Material.AIR, "itemstack must not be air");
            this.background = itemStack;
            return this;
        }

        public SBuilder parentButton(int x, int y) {
            return this.parentButton(x, y, (p) -> PREV_1);
        }

        public SBuilder parentButton(int x, int y,
                                     Function<Player, ItemStack> getItemStackFunction) {
            Validate.isTrue(x >= 0, "x must be greater or equal to 0 (" + x + ")");
            Validate.isTrue(x <= 8, "x must be less or equal to 8 (" + x + ")");
            Validate.isTrue(y >= 0, "y must be greater or equal to 0 (" + y + ")");
            Validate.isTrue(y < columns, "y must be less than columns " + columns + " (" + y + ")");

            return this.button(x, y, new Button.Builder()
                    .icon(getItemStackFunction)
                    .lmb((clickEvent) -> Result.PARENT()));
        }

        /**
         * Attach a menu to a button on {@link EnumPress} being invoked
         * @param x horizontal position
         * @param y vertical position
         * @param press bind to which event
         * @param getItemStackFunction button icon lambda
         * @param menuToOpen the menu to open on press
         * @return this
         */
        public SBuilder bind(int x, int y,
                             EnumPress press,
                             Function<Player, ItemStack> getItemStackFunction, Builder menuToOpen) {
            menuToOpen.parent(this);

            this.getOrMakeButton(x, y, getItemStackFunction)
                    .bind(menuToOpen, press);
            return this;
        }

        final Button.Builder getOrMakeButton(int x, int y, Function<Player, ItemStack> getItemStackFunction) {
            return super.getOrMakeButton(slotOf(x, y), getItemStackFunction);
        }

        // Get pitch
        // Also performs auxiliary check
        protected int slotOf(int x, int y) {
            // assert things here
            Validate.isTrue(x >= 0, "x must be x>=0 (" + x + ")");
            Validate.isTrue(x <= 8, "x must be x<=8 (" + x + ")");
            Validate.isTrue(y >= 0, "y must be y>=0 (" + y + ")");
            Validate.isTrue(y < columns, "y must be y<col col:" + columns + " (" + y + ")");

            return y*9 + x;
        }

        public SimpleMenu open(Player player) {
            HashMap<Integer, Button> btns = new HashMap<>();
            buttons.forEach((i, b) -> btns.put(i, b.get()));

            SimpleMenu menu = new SimpleMenu(player,
                                             getTitleFunction,
                                             btns,
                                             openFunction,
                                             closeFunction,
                                             this,
                                             background,
                                             columns);

            menu.openInventory(true);

            return menu;
        }
    }
}
