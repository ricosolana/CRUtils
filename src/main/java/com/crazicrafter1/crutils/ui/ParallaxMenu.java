package com.crazicrafter1.crutils.ui;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.MathUtil;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ParallaxMenu extends SimpleMenu {

    //todo add a customizable area size for this menu

    private static final int ITEM_X = 1;
    private static final int ITEM_Y = 1;
    private static final int ITEM_W = 7;
    private static final int ITEM_H = 3;

    private static final int ITEM_X2 = ITEM_X + ITEM_W - 1;
    private static final int ITEM_Y2 = ITEM_Y + ITEM_H - 1;
    private static final int SIZE = ITEM_W * ITEM_H;

    private int page = 1;

    private final BiFunction<PBuilder, Player, List<Button>> orderedButtonsFunc;
    private final boolean async;

    private List<Button> orderedButtons = new ArrayList<>();
    //public List<Button> subset = new ArrayList<>();

    private ParallaxMenu(Player player,
                         Function<Player, String> getTitleFunction,
                         HashMap<Integer, Button> buttons,
                         Consumer<Player> openFunction,
                         BiFunction<Player, Boolean, Result> closeFunction,
                         Builder thisBuilder,
                         ItemStack background,
                         BiFunction<PBuilder, Player, List<Button>> orderedButtonsFunc,
                         boolean async
    ) {
        super(player, getTitleFunction, buttons, openFunction, closeFunction, thisBuilder, background, 6);
        //this.orderedButtons = orderedButtons;
        this.orderedButtonsFunc = orderedButtonsFunc;
        this.async = async;
    }

    /**
     * The ordered buttons function should be called ONLY on the initial construction of the menu,
     * because a page turn is simply a rearrangement of existing buttons
     * @param sendOpenPacket
     */
    @Override
    void openInventory(boolean sendOpenPacket) {
        if (async) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        List<Button> inner = orderedButtonsFunc.apply((PBuilder) builder, player);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (status == Status.OPEN) {
                                    orderedButtons = inner;
                                    rePage();
                                    placeButtons();
                                }
                            }
                        }.runTaskLater(com.crazicrafter1.crutils.Main.getInstance(), 0);

                    } catch (Exception e) {e.printStackTrace();}
                }
            }.runTaskAsynchronously(com.crazicrafter1.crutils.Main.getInstance());
        } else {
            this.orderedButtons = orderedButtonsFunc.apply((PBuilder) builder, player);
            rePage();
        }

        super.openInventory(sendOpenPacket);
    }

    private void rePage() {
        if (inventory != null)
            inventory.clear();

        if (page > 1) {
            // Previous page
            //
            button(0, 5, new Button.Builder()
                    .icon((p) -> ItemBuilder.copy(Material.ARROW).name("&aPrevious Page").lore("&ePage " + (page-1)).build())
                    .lmb((interact) -> {
                        prevPage();
                        return null;
                    }).get());

        } else
            delButton(0, 5);

        if (page < getMaxPages(orderedButtons.size())) {
            button(8, 5, new Button.Builder()
                    .icon((p) -> ItemBuilder.copy(Material.ARROW).name("&aNext Page").lore("&ePage " + (page+1)).build())
                    .lmb((interact) -> {
                        nextPage(orderedButtons.size());
                        return null;
                    }).get());
        } else
            delButton(8, 5);

        // now assign center block items

        final int size = orderedButtons.size();

        int startIndex = (page-1) * SIZE;
        final int endIndex = MathUtil.clamp(
                startIndex + MathUtil.clamp(size - startIndex, 0, SIZE),
                0, size-1);

        for (int y = ITEM_Y; y < ITEM_Y2 + 1; y++) {
            for (int x = ITEM_X; x < ITEM_X2 + 1; x++) {
                if (startIndex > endIndex) {
                    delButton(x, y);
                } else {
                    button(x, y, orderedButtons.get(startIndex++));
                }
            }
        }
    }

    private int getMaxPages(int size) {
        return 1 + (size - 1) / SIZE;
    }

    private void prevPage() {
        if (page > 1) {
            page--;
            rePage();
            placeButtons();
        }
    }

    private void nextPage(int size) {
        if (page < getMaxPages(size)) {
            page++;
            rePage();
            placeButtons();
        }
    }

    public static class PBuilder extends SBuilder {
        private BiFunction<PBuilder, Player, List<Button>> orderedButtonsFunc;
        private boolean async;

        public PBuilder() {
            super(6);
        }

        public PBuilder addAll(BiFunction<PBuilder, Player, List<Button>> orderedButtonsFunc) {
            Validate.notNull(orderedButtonsFunc);
            this.orderedButtonsFunc = orderedButtonsFunc;

            return this;
        }

        public PBuilder addAllAsync(BiFunction<PBuilder, Player, List<Button>> orderedButtonsFunc) {
            Validate.notNull(orderedButtonsFunc);
            this.orderedButtonsFunc = orderedButtonsFunc;
            async = true;

            return this;
        }

        // .searchButton(x, y)
        //public PBuilder search(int x, int y) {
        //    // on click, show a specific subset of the ordered buttons
        //    return childButton(x, y, p -> ItemBuilder.copyOf(Material.COMPASS).name("&8Search").build(), new TextMenu.TBuilder()
        //                    .onClose(p -> Result.PARENT())
        //                    .onComplete((p, s, menu) -> {
        //                        // open a new menu, of only sub queries
        //                        ((ParallaxMenu.PBuilder)menu.builder).subset.stream()
        //                                .filter(b -> {
        //                                    b.getItemStackFunction.apply(p)
        //                                })
        //                    }
        //                    )
        //            );
        //}


        @Override
        public PBuilder title(Function<Player, String> getTitleFunction) {
            return (PBuilder) super.title(getTitleFunction);
        }

        @Override
        public PBuilder title(Function<Player, String> getTitleFunction, ColorUtil titleColorMode) {
            return (PBuilder) super.title(getTitleFunction, titleColorMode);
        }

        @Override
        public PBuilder childButton(int x, int y, Function<Player, ItemStack> getItemStackFunction, Builder otherMenu) {
            Validate.isTrue(!(x >= ITEM_X && x <= ITEM_X2 && y >= ITEM_Y && y <= ITEM_Y2),
                    "x, y must not be within center block (" + x + ", " + y + ")");
            Validate.isTrue(!((x == 0 || x == 8) && y == 5), "button must not overlap page buttons");
            return (PBuilder) super.childButton(x, y, getItemStackFunction, otherMenu);
        }

        @Override
        public PBuilder button(int x, int y, Button.Builder button) {
            // inverse case to show the block, and not block
            Validate.isTrue(!(x >= ITEM_X && x <= ITEM_X2 && y >= ITEM_Y && y <= ITEM_Y2),
                    "x, y must not be within center block (" + x + ", " + y + ")");
            Validate.isTrue(!((x == 0 || x == 8) && y == 5), "button must not overlap page buttons");
            return (PBuilder) super.button(x, y, button);
        }

        @Override
        public PBuilder onOpen(Consumer<Player> openFunction) {
            return (PBuilder) super.onOpen(openFunction);
        }

        @Override
        public PBuilder onClose(BiFunction<Player, Boolean, Result> closeFunction) {
            return (PBuilder) super.onClose(closeFunction);
        }

        @Override
        public PBuilder onClose(Function<Player, Result> closeFunction) {
            return (PBuilder) super.onClose(closeFunction);
        }

        @Override
        public PBuilder background() {
            return (PBuilder) super.background();
        }

        @Override
        public PBuilder background(ItemStack itemStack) {
            return (PBuilder) super.background(itemStack);
        }

        @Override
        public PBuilder parentButton(int x, int y) {
            return (PBuilder) super.parentButton(x, y);
        }

        @Override
        public PBuilder parentButton(int x, int y, Function<Player, ItemStack> getItemStackFunction) {
            Validate.isTrue(!(x >= ITEM_X && x <= ITEM_X2 && y >= ITEM_Y && y <= ITEM_Y2),
                    "x, y must not be within center block (" + x + ", " + y + ")");
            return (PBuilder) super.parentButton(x, y, getItemStackFunction);
        }

        @Override
        public PBuilder bind(int x, int y, EnumPress press, Function<Player, ItemStack> getItemStackFunction, Builder menuToOpen) {
            return (PBuilder) super.bind(x, y, press, getItemStackFunction, menuToOpen);
        }

        @Override
        public ParallaxMenu open(Player player) {
            HashMap<Integer, Button> btns = new HashMap<>();
            buttons.forEach((i, b) -> btns.put(i, b.get()));

            ParallaxMenu menu = new ParallaxMenu(player,
                                                 getTitleFunction,
                                                 btns,
                                                 openFunction,
                                                 closeFunction,
                                                 this,
                                                 background,
                                                 orderedButtonsFunc,
                                                 async);

            menu.openInventory(true);

            return menu;
        }

    }

}
