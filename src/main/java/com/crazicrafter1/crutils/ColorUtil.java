package com.crazicrafter1.crutils;

import org.bukkit.ChatColor;
import org.bukkit.Color;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.crazicrafter1.crutils.Util.clamp;

public enum ColorUtil {
    ;

    private static final char MARK_CHAR = '&';
    private static final char RENDER_CHAR = ChatColor.COLOR_CHAR;

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("#[0-9a-fA-F]{6}");

    //private static final Pattern LEGACY_MARK_PATTERN = Pattern.compile(MARK_CHAR + "[0-9a-fA-Fk-oK-OrR]");
    //private static final Pattern HEX_MARK_PATTERN = Pattern.compile("(?im)" + MARK_CHAR + HEX_COLOR_PATTERN);

    private static final Pattern GRADIENT_HEX_PATTERN = Pattern.compile("<" + HEX_COLOR_PATTERN + "[^>]*>" +
            "(.*?)" +
            "</" + HEX_COLOR_PATTERN + ">");
    private static final Pattern GRADIENT_NAME_PATTERN = Pattern.compile("<[0-9a-zA-Z_']+[^>]*>(.*?)</[0-9a-zA-Z_]+>");

    private static final Map<String, Color> COLOR_MAP = new HashMap<>();

    private static void putAllColors(Object... values) {
        for (int i=0; i<values.length; i+=2) {
            COLOR_MAP.put((String)values[i], Color.fromRGB((int)values[i+1]));
        }
    }
    static {
        putAllColors(
                "ABSOLUTE_ZERO", 0x0048BA,
                "ACID_GREEN", 0xB0BF1A,
                "AERO", 0x7CB9E8,
                "AERO_BLUE", 0xC0E8D5,
                "AFRICAN_VIOLET", 0xB284BE,
                "AIR_SUPERIORITY_BLUE", 0x72A0C1,
                "ALABASTER", 0xEDEAE0,
                "ALIZARIN", 0xDB2D43,
                "ALICE_BLUE", 0xF0F8FF,
                "ALLOY_ORANGE", 0xC46210,
                "ALMOND", 0xEFDECD,
                "AMARANTH", 0xE52B50,
                "AMARANTH_PINK", 0xF19CBB,
                "AMARANTH_PURPLE", 0xAB274F,
                "AMARANTH_RED", 0xD3212D,
                "AMAZON", 0x3B7A57,
                "AMBER", 0xFFBF00,
                "AMETHYST", 0x9966CC,
                "ANDROID_GREEN", 0x3DDC84,
                "ANTIQUE_BRASS", 0xCD9575,
                "ANTIQUE_BRONZE", 0x665D1E,
                "ANTIQUE_FUCHSIA", 0x915C83,
                "ANTIQUE_RUBY", 0x841B2D,
                "ANTIQUE_WHITE", 0xFAEBD7,
                "APPLE_GREEN", 0x8DB600,
                "APRICOT", 0xFBCEB1,
                "AQUA", 0x00FFFF,
                "AQUAMARINE", 0x7FFFD4,
                "ARCTIC_LIME", 0xD0FF14,
                "ARMY_GREEN", 0x4B5320,
                "ARTICHOKE", 0x8F9779,
                "ARYLIDE_YELLOW", 0xE9D66B,
                "ASH_GRAY", 0xB2BEB5,
                "ASPARAGUS", 0x87A96B,
                "ASTRONAUT", 0x27346F,
                "ATOMIC_TANGERINE", 0xFF9966,
                "AUBURN", 0xA52A2A,
                "AUREOLIN", 0xFDEE00,
                "AVOCADO", 0x568203,
                "AZURE", 0x007FFF,
                "BABY_BLUE", 0x89CFF0,
                "BABY_BLUE_EYES", 0xA1CAF1,
                "BABY_PINK", 0xF4C2C2,
                "BABY_POWDER", 0xFEFEFA,
                "BAKER-MILLER_PINK", 0xFF91AF,
                "BANANA_MANIA", 0xFAE7B5,
                "BARBIE_PINK", 0xDA1884,
                "BARN_RED", 0x7C0A02,
                "BATTLESHIP_GREY", 0x848482,
                "BEAU_BLUE", 0xBCD4E6,
                "BEAVER", 0x9F8170,
                "BEIGE", 0xF5F5DC,
                "B'DAZZLED_BLUE", 0x2E5894,
                "BIG_DIP_O’RUBY", 0x9C2542,
                "BISQUE", 0xFFE4C4,
                "BISTRE", 0x3D2B1F,
                "BISTRE_BROWN", 0x967117,
                "BITTER_LEMON", 0xCAE00D,
                "BITTER_LIME", 0xBFFF00,
                "BITTERSWEET", 0xFE6F5E,
                "BITTERSWEET_SHIMMER", 0xBF4F51,
                "BLACK", 0x000000,
                "BLACK_BEAN", 0x3D0C02,
                "BLACK_CHOCOLATE", 0x1B1811,
                "BLACK_COFFEE", 0x3B2F2F,
                "BLACK_CORAL", 0x54626F,
                "BLACK_OLIVE", 0x3B3C36,
                "BLACK_SHADOWS", 0xBFAFB2,
                "BLANCHED_ALMOND", 0xFFEBCD,
                "BLAST-OFF_BRONZE", 0xA57164,
                "BLEU_DE_FRANCE", 0x318CE7,
                "BLIZZARD_BLUE", 0xACE5EE,
                "BLOND", 0xFAF0BE,
                "BLOOD_RED", 0x660000,
                "BLUE", 0x0000FF,
                "BLUE_BELL", 0xA2A2D0,
                "BLUE-GRAY", 0x6699CC,
                "BLUE-GREEN", 0x0D98BA,
                "BLUE_JEANS", 0x5DADEC,
                "BLUE_SAPPHIRE", 0x126180,
                "BLUE-VIOLET", 0x8A2BE2,
                "BLUE_YONDER", 0x5072A7,
                "BLUETIFUL", 0x3C69E7,
                "BLUSH", 0xDE5D83,
                "BOLE", 0x79443B,
                "BONE", 0xE3DAC9,
                "BOTTLE_GREEN", 0x006A4E,
                "BRANDY", 0x87413F,
                "BRICK_RED", 0xCB4154,
                "BRIGHT_GREEN", 0x66FF00,
                "BRIGHT_LILAC", 0xD891EF,
                "BRIGHT_MAROON", 0xC32148,
                "BRIGHT_NAVY_BLUE", 0x1974D2,
                "BRILLIANT_ROSE", 0xFF55A3,
                "BRINK_PINK", 0xFB607F,
                "BRITISH_RACING_GREEN", 0x004225,
                "BRONZE", 0xCD7F32,
                "BROWN", 0x88540B,
                "BROWN_SUGAR", 0xAF6E4D,
                "BRUNSWICK_GREEN", 0x1B4D3E,
                "BUD_GREEN", 0x7BB661,
                "BUFF", 0xFFC680,
                "BURGUNDY", 0x800020,
                "BURLYWOOD", 0xDEB887,
                "BURNISHED_BROWN", 0xA17A74,
                "BURNT_ORANGE", 0xCC5500,
                "BURNT_SIENNA", 0xE97451,
                "BURNT_UMBER", 0x8A3324,
                "BYZANTINE", 0xBD33A4,
                "BYZANTIUM", 0x702963,
                "CADET", 0x536872,
                "CADET_BLUE", 0x5F9EA0,
                "CADET_GREY", 0x91A3B0,
                "CADMIUM_GREEN", 0x006B3C,
                "CADMIUM_ORANGE", 0xED872D,
                "CADMIUM_RED", 0xE30022,
                "CADMIUM_YELLOW", 0xFFF600,
                "CAFÉ_AU_LAIT", 0xA67B5B,
                "CAFÉ_NOIR", 0x4B3621,
                "CAMBRIDGE_BLUE", 0xA3C1AD,
                "CAMEL", 0xC19A6B,
                "CAMEO_PINK", 0xEFBBCC,
                "CANARY", 0xFFFF99,
                "CANARY_YELLOW", 0xFFEF00,
                "CANDY_APPLE_RED", 0xFF0800,
                "CANDY_PINK", 0xE4717A,
                "CAPRI", 0x00BFFF,
                "CAPUT_MORTUUM", 0x592720,
                "CARDINAL", 0xC41E3A,
                "CARIBBEAN_GREEN", 0x00CC99,
                "CARMINE", 0x960018,
                "CARNATION_PINK", 0xFFA6C9,
                "CARNELIAN", 0xB31B1B,
                "CAROLINA_BLUE", 0x56A0D3,
                "CARROT_ORANGE", 0xED9121,
                "CASTLETON_GREEN", 0x00563F,
                "CATAWBA", 0x703642,
                "CEDAR_CHEST", 0xC95A49,
                "CELADON", 0xACE1AF,
                "CELADON_BLUE", 0x007BA7,
                "CELADON_GREEN", 0x2F847C,
                "CELESTE", 0xB2FFFF,
                "CELTIC_BLUE", 0x246BCE,
                "CERISE", 0xDE3163,
                "CERULEAN", 0x007BA7,
                "CERULEAN_BLUE", 0x2A52BE,
                "CERULEAN_FROST", 0x6D9BC3,
                "CG_BLUE", 0x007AA5,
                "CG_RED", 0xE03C31,
                "CHAMPAGNE", 0xF7E7CE,
                "CHAMPAGNE_PINK", 0xF1DDCF,
                "CHARCOAL", 0x36454F,
                "CHARLESTON_GREEN", 0x232B2B,
                "CHARM_PINK", 0xE68FAC,
                "CHERRY_BLOSSOM_PINK", 0xFFB7C5,
                "CHESTNUT", 0x954535,
                "CHILI_RED", 0xE23D28,
                "CHINA_PINK", 0xDE6FA1,
                "CHINA_ROSE", 0xA8516E,
                "CHINESE_RED", 0xAA381E,
                "CHINESE_VIOLET", 0x856088,
                "CHINESE_YELLOW", 0xFFB200,
                "CHOCOLATE_COSMOS", 0x58111A,
                "CHROME_YELLOW", 0xFFA700,
                "CINEREOUS", 0x98817B,
                "CINNABAR", 0xE34234,
                "CINNAMON_SATIN", 0xCD607E,
                "CITRINE", 0xE4D00A,
                "CITRON", 0x9FA91F,
                "CLARET", 0x7F1734,
                "COBALT_BLUE", 0x0047AB,
                "COCOA_BROWN", 0xD2691E,
                "COFFEE", 0x6F4E37,
                "COLUMBIA_BLUE", 0xB9D9EB,
                "CONGO_PINK", 0xF88379,
                "COOL_GREY", 0x8C92AC,
                "COPPER", 0xB87333,
                "COPPER_PENNY", 0xAD6F69,
                "COPPER_RED", 0xCB6D51,
                "COPPER_ROSE", 0x996666,
                "COQUELICOT", 0xFF3800,
                "CORAL", 0xFF7F50,
                "CORAL_PINK", 0xF88379,
                "CORDOVAN", 0x893F45,
                "CORN", 0xFBEC5D,
                "CORNELL_RED", 0xB31B1B,
                "CORNFLOWER_BLUE", 0x6495ED,
                "CORNSILK", 0xFFF8DC,
                "COSMIC_COBALT", 0x2E2D88,
                "COSMIC_LATTE", 0xFFF8E7,
                "COYOTE_BROWN", 0x81613C,
                "COTTON_CANDY", 0xFFBCD9,
                "CREAM", 0xFFFDD0,
                "CRIMSON", 0xDC143C,
                "CRYSTAL", 0xA7D8DE,
                "CULTURED", 0xF5F5F5,
                "CYAN", 0x00FFFF,
                "CYBER_GRAPE", 0x58427C,
                "CYBER_YELLOW", 0xFFD300,
                "CYCLAMEN", 0xF56FA1,
                "DARK_BLUE-GRAY", 0x666699,
                "DARK_BROWN", 0x654321,
                "DARK_BYZANTIUM", 0x5D3954,
                "DARK_CORNFLOWER_BLUE", 0x26428B,
                "DARK_CYAN", 0x008B8B,
                "DARK_ELECTRIC_BLUE", 0x536878,
                "DARK_GOLDENROD", 0xB8860B,
                "DARK_GREEN", 0x013220,
                "DARK_JUNGLE_GREEN", 0x1A2421,
                "DARK_KHAKI", 0xBDB76B,
                "DARK_LAVA", 0x483C32,
                "DARK_LIVER", 0x534B4F,
                "DARK_MAGENTA", 0x8B008B,
                "DARK_MOSS_GREEN", 0x4A5D23,
                "DARK_OLIVE_GREEN", 0x556B2F,
                "DARK_ORANGE", 0xFF8C00,
                "DARK_ORCHID", 0x9932CC,
                "DARK_PASTEL_GREEN", 0x03C03C,
                "DARK_PURPLE", 0x301934,
                "DARK_RED", 0x8B0000,
                "DARK_SALMON", 0xE9967A,
                "DARK_SEA_GREEN", 0x8FBC8F,
                "DARK_SIENNA", 0x3C1414,
                "DARK_SKY_BLUE", 0x8CBED6,
                "DARK_SLATE_BLUE", 0x483D8B,
                "DARK_SLATE_GRAY", 0x2F4F4F,
                "DARK_SPRING_GREEN", 0x177245,
                "DARK_TURQUOISE", 0x00CED1,
                "DARK_VIOLET", 0x9400D3,
                "DARTMOUTH_GREEN", 0x00703C,
                "DAVY'S_GREY", 0x555555,
                "DEEP_CERISE", 0xDA3287,
                "DEEP_CHAMPAGNE", 0xFAD6A5,
                "DEEP_CHESTNUT", 0xB94E48,
                "DEEP_JUNGLE_GREEN", 0x004B49,
                "DEEP_PINK", 0xFF1493,
                "DEEP_SAFFRON", 0xFF9933,
                "DEEP_SKY_BLUE", 0x00BFFF,
                "DEEP_SPACE_SPARKLE", 0x4A646C,
                "DEEP_TAUPE", 0x7E5E60,
                "DENIM", 0x1560BD,
                "DENIM_BLUE", 0x2243B6,
                "DESERT", 0xC19A6B,
                "DESERT_SAND", 0xEDC9AF,
                "DIM_GRAY", 0x696969,
                "DODGER_BLUE", 0x1E90FF,
                "DOGWOOD_ROSE", 0xD71868,
                "DRAB", 0x967117,
                "DUKE_BLUE", 0x00009C,
                "DUTCH_WHITE", 0xEFDFBB,
                "EARTH_YELLOW", 0xE1A95F,
                "EBONY", 0x555D50,
                "ECRU", 0xC2B280,
                "EERIE_BLACK", 0x1B1B1B,
                "EGGPLANT", 0x614051,
                "EGGSHELL", 0xF0EAD6,
                "EGYPTIAN_BLUE", 0x1034A6,
                "EIGENGRAU", 0x16161D,
                "ELECTRIC_BLUE", 0x7DF9FF,
                "ELECTRIC_GREEN", 0x00FF00,
                "ELECTRIC_INDIGO", 0x6F00FF,
                "ELECTRIC_LIME", 0xCCFF00,
                "ELECTRIC_PURPLE", 0xBF00FF,
                "ELECTRIC_VIOLET", 0x8F00FF,
                "EMERALD", 0x50C878,
                "EMINENCE", 0x6C3082,
                "ENGLISH_GREEN", 0x1B4D3E,
                "ENGLISH_LAVENDER", 0xB48395,
                "ENGLISH_RED", 0xAB4B52,
                "ENGLISH_VERMILLION", 0xCC474B,
                "ENGLISH_VIOLET", 0x563C5C,
                "ERIN", 0x00FF40,
                "ETON_BLUE", 0x96C8A2,
                "FALLOW", 0xC19A6B,
                "FALU_RED", 0x801818,
                "FANDANGO", 0xB53389,
                "FANDANGO_PINK", 0xDE5285,
                "FASHION_FUCHSIA", 0xF400A1,
                "FAWN", 0xE5AA70,
                "FELDGRAU", 0x4D5D53,
                "FERN_GREEN", 0x4F7942,
                "FIELD_DRAB", 0x6C541E,
                "FIERY_ROSE", 0xFF5470,
                "FIREBRICK", 0xB22222,
                "FIRE_ENGINE_RED", 0xCE2029,
                "FIRE_OPAL", 0xE95C4B,
                "FLAME", 0xE25822,
                "FLAX", 0xEEDC82,
                "FLIRT", 0xA2006D,
                "FLORAL_WHITE", 0xFFFAF0,
                "FLUORESCENT_BLUE", 0x15F4EE,
                "FRENCH_BEIGE", 0xA67B5B,
                "FRENCH_BISTRE", 0x856D4D,
                "FRENCH_BLUE", 0x0072BB,
                "FRENCH_FUCHSIA", 0xFD3F92,
                "FRENCH_LILAC", 0x86608E,
                "FRENCH_LIME", 0x9EFD38,
                "FRENCH_MAUVE", 0xD473D4,
                "FRENCH_PINK", 0xFD6C9E,
                "FRENCH_RASPBERRY", 0xC72C48,
                "FRENCH_ROSE", 0xF64A8A,
                "FRENCH_SKY_BLUE", 0x77B5FE,
                "FRENCH_VIOLET", 0x8806CE,
                "FROSTBITE", 0xE936A7,
                "FUCHSIA", 0xFF00FF,
                "FUCHSIA_PURPLE", 0xCC397B,
                "FUCHSIA_ROSE", 0xC74375,
                "FULVOUS", 0xE48400,
                "FUZZY_WUZZY", 0x87421F,
                "NADESHIKO_PINK", 0xF6ADC6,
                "NAPLES_YELLOW", 0xFADA5E,
                "NAVAJO_WHITE", 0xFFDEAD,
                "NAVY_BLUE", 0x000080,
                "NEON_BLUE", 0x4666FF,
                "NEON_CARROT", 0xFFA343,
                "NEON_GREEN", 0x39FF14,
                "NEON_FUCHSIA", 0xFE4164,
                "NEW_YORK_PINK", 0xD7837F,
                "NICKEL", 0x727472,
                "NON-PHOTO_BLUE", 0xA4DDED,
                "NYANZA", 0xE9FFDB,
                "OCEAN_BLUE", 0x4F42B5,
                "OCEAN_GREEN", 0x48BF91,
                "OCHRE", 0xCC7722,
                "OLD_BURGUNDY", 0x43302E,
                "OLD_GOLD", 0xCFB53B,
                "OLD_LACE", 0xFDF5E6,
                "OLD_LAVENDER", 0x796878,
                "OLD_MAUVE", 0x673147,
                "OLD_ROSE", 0xC08081,
                "OLD_SILVER", 0x848482,
                "OLIVE", 0x808000,
                "OLIVE_DRAB", 0x6B8E23,
                "OLIVE_GREEN", 0xB5B35C,
                "OLIVINE", 0x9AB973,
                "ONYX", 0x353839,
                "OPAL", 0xA8C3BC,
                "OPERA_MAUVE", 0xB784A7,
                "ORANGE", 0xFF7F00,
                "ORANGE_PEEL", 0xFF9F00,
                "ORANGE-RED", 0xFF681F,
                "ORANGE_SODA", 0xFA5B3D,
                "ORANGE-YELLOW", 0xF5BD1F,
                "ORCHID", 0xDA70D6,
                "ORCHID_PINK", 0xF2BDCD,
                "OUTRAGEOUS_ORANGE", 0xFF6E4A,
                "OXBLOOD", 0x4A0000,
                "OXFORD_BLUE", 0x002147,
                "OU_CRIMSON_RED", 0x841617,
                "PACIFIC_BLUE", 0x1CA9C9,
                "PAKISTAN_GREEN", 0x006600,
                "PALATINATE_PURPLE", 0x682860,
                "PALE_AQUA", 0xBCD4E6,
                "PALE_CERULEAN", 0x9BC4E2,
                "PALE_DOGWOOD", 0xED7A9B,
                "PALE_PINK", 0xFADADD,
                "PALE_SILVER", 0xC9C0BB,
                "PALE_SPRING_BUD", 0xECEBBD,
                "PANSY_PURPLE", 0x78184A,
                "PAOLO_VERONESE_GREEN", 0x009B7D,
                "PAPAYA_WHIP", 0xFFEFD5,
                "PARADISE_PINK", 0xE63E62,
                "PARCHMENT", 0xF1E9D2,
                "PARIS_GREEN", 0x50C878,
                "PASTEL_PINK", 0xDEA5A4,
                "PATRIARCH", 0x800080,
                "PAYNE'S_GREY", 0x536878,
                "PEACH", 0xFFE5B4,
                "PEACH_PUFF", 0xFFDAB9,
                "PEAR", 0xD1E231,
                "PEARLY_PURPLE", 0xB768A2,
                "PERIWINKLE", 0xCCCCFF,
                "PERMANENT_GERANIUM_LAKE", 0xE12C2C,
                "PERSIAN_BLUE", 0x1C39BB,
                "PERSIAN_GREEN", 0x00A693,
                "PERSIAN_INDIGO", 0x32127A,
                "PERSIAN_ORANGE", 0xD99058,
                "PERSIAN_PINK", 0xF77FBE,
                "PERSIAN_PLUM", 0x701C1C,
                "PERSIAN_RED", 0xCC3333,
                "PERSIAN_ROSE", 0xFE28A2,
                "PERSIMMON", 0xEC5800,
                "PEWTER_BLUE", 0x8BA8B7,
                "PHLOX", 0xDF00FF,
                "PHTHALO_BLUE", 0x000F89,
                "PHTHALO_GREEN", 0x123524,
                "PICOTEE_BLUE", 0x2E2787,
                "PICTORIAL_CARMINE", 0xC30B4E,
                "PIGGY_PINK", 0xFDDDE6,
                "PINE_GREEN", 0x01796F,
                "PINE_TREE", 0x2A2F23,
                "PINK", 0xFFC0CB,
                "PINK_FLAMINGO", 0xFC74FD,
                "PINK_LACE", 0xFFDDF4,
                "PINK_LAVENDER", 0xD8B2D1,
                "PINK_SHERBET", 0xF78FA7,
                "PISTACHIO", 0x93C572,
                "PLATINUM", 0xE5E4E2,
                "PLUM", 0x8E4585,
                "PLUMP_PURPLE", 0x5946B2,
                "POLISHED_PINE", 0x5DA493,
                "POMP_AND_POWER", 0x86608E,
                "POPSTAR", 0xBE4F62,
                "PORTLAND_ORANGE", 0xFF5A36,
                "POWDER_BLUE", 0xB0E0E6,
                "PRINCETON_ORANGE", 0xF58025,
                "PROCESS_YELLOW", 0xFFEF00,
                "PRUNE", 0x701C1C,
                "PRUSSIAN_BLUE", 0x003153,
                "PSYCHEDELIC_PURPLE", 0xDF00FF,
                "PUCE", 0xCC8899,
                "PUMPKIN", 0xFF7518,
                "PURPLE", 0x6A0DAD,
                "PURPLE_MOUNTAIN_MAJESTY", 0x9678B6,
                "PURPLE_NAVY", 0x4E5180,
                "PURPLE_PIZZAZZ", 0xFE4EDA,
                "PURPLE_PLUM", 0x9C51B6,
                "PURPUREUS", 0x9A4EAE,
                "QUEEN_BLUE", 0x436B95,
                "QUEEN_PINK", 0xE8CCD7,
                "QUICK_SILVER", 0xA6A6A6,
                "QUINACRIDONE_MAGENTA", 0x8E3A59,
                "RADICAL_RED", 0xFF355E,
                "RAISIN_BLACK", 0x242124,
                "RAJAH", 0xFBAB60,
                "RASPBERRY", 0xE30B5D,
                "RASPBERRY_GLACE", 0x915F6D,
                "RASPBERRY_ROSE", 0xB3446C,
                "RAW_SIENNA", 0xD68A59,
                "RAW_UMBER", 0x826644,
                "RAZZLE_DAZZLE_ROSE", 0xFF33CC,
                "RAZZMATAZZ", 0xE3256B,
                "RAZZMIC_BERRY", 0x8D4E85,
                "REBECCA_PURPLE", 0x663399,
                "RED", 0xFF0000,
                "RED-ORANGE", 0xFF5349,
                "RED-PURPLE", 0xE40078,
                "RED_SALSA", 0xFD3A4A,
                "RED-VIOLET", 0xC71585,
                "REDWOOD", 0xA45A52,
                "RESOLUTION_BLUE", 0x002387,
                "RHYTHM", 0x777696,
                "RICH_BLACK", 0x004040,
                "RIFLE_GREEN", 0x444C38,
                "ROBIN_EGG_BLUE", 0x00CCCC,
                "ROCKET_METALLIC", 0x8A7F80,
                "ROJO_SPANISH_RED", 0xA91101,
                "ROMAN_SILVER", 0x838996,
                "ROSE", 0xFF007F,
                "ROSE_BONBON", 0xF9429E,
                "ROSE_DUST", 0x9E5E6F,
                "ROSE_EBONY", 0x674846,
                "ROSE_MADDER", 0xE32636,
                "ROSE_PINK", 0xFF66CC,
                "ROSE_POMPADOUR", 0xED7A9B,
                "ROSE_QUARTZ", 0xAA98A9,
                "ROSE_RED", 0xC21E56,
                "ROSE_TAUPE", 0x905D5D,
                "ROSE_VALE", 0xAB4E52,
                "ROSEWOOD", 0x65000B,
                "ROSSO_CORSA", 0xD40000,
                "ROSY_BROWN", 0xBC8F8F,
                "ROYAL_PURPLE", 0x7851A9,
                "ROYAL_YELLOW", 0xFADA5E,
                "RUBER", 0xCE4676,
                "RUBINE_RED", 0xD10056,
                "RUBY", 0xE0115F,
                "RUBY_RED", 0x9B111E,
                "RUFOUS", 0xA81C07,
                "RUSSET", 0x80461B,
                "RUSSIAN_GREEN", 0x679267,
                "RUSSIAN_VIOLET", 0x32174D,
                "RUST", 0xB7410E,
                "RUSTY_RED", 0xDA2C43,
                "SACRAMENTO_STATE_GREEN", 0x043927,
                "SADDLE_BROWN", 0x8B4513,
                "SAFETY_ORANGE", 0xFF7800,
                "SAFETY_YELLOW", 0xEED202,
                "SAFFRON", 0xF4C430,
                "SAGE", 0xBCB88A,
                "ST._PATRICK'S_BLUE", 0x23297A,
                "SALMON", 0xFA8072,
                "SALMON_PINK", 0xFF91A4,
                "SAND", 0xC2B280,
                "SAND_DUNE", 0x967117,
                "SANDY_BROWN", 0xF4A460,
                "SAP_GREEN", 0x507D2A,
                "SAPPHIRE", 0x0F52BA,
                "SAPPHIRE_BLUE", 0x0067A5,
                "SATIN_SHEEN_GOLD", 0xCBA135,
                "SCARLET", 0xFF2400,
                "SCHAUSS_PINK", 0xFF91AF,
                "SCHOOL_BUS_YELLOW", 0xFFD800,
                "SCREAMIN'_GREEN", 0x66FF66,
                "SEA_GREEN", 0x2E8B57,
                "SEANCE", 0x612086,
                "SEAL_BROWN", 0x59260B,
                "SEASHELL", 0xFFF5EE,
                "SELECTIVE_YELLOW", 0xFFBA00,
                "SEPIA", 0x704214,
                "SHADOW", 0x8A795D,
                "SHADOW_BLUE", 0x778BA5,
                "SHAMROCK_GREEN", 0x009E60,
                "SHEEN_GREEN", 0x8FD400,
                "SHIMMERING_BLUSH", 0xD98695,
                "SHINY_SHAMROCK", 0x5FA778,
                "SHOCKING_PINK", 0xFC0FC0,
                "SIENNA", 0x882D17,
                "SILVER", 0xC0C0C0,
                "SILVER_CHALICE", 0xACACAC,
                "SILVER_PINK", 0xC4AEAD,
                "SILVER_SAND", 0xBFC1C2,
                "SINOPIA", 0xCB410B,
                "SIZZLING_RED", 0xFF3855,
                "SIZZLING_SUNRISE", 0xFFDB00,
                "SKOBELOFF", 0x007474,
                "SKY_BLUE", 0x87CEEB,
                "SKY_MAGENTA", 0xCF71AF,
                "SLATE_BLUE", 0x6A5ACD,
                "SLATE_GRAY", 0x708090,
                "SLIMY_GREEN", 0x299617,
                "SMITTEN", 0xC84186,
                "SMOKY_BLACK", 0x100C08,
                "SNOW", 0xFFFAFA,
                "SOLID_PINK", 0x893843,
                "SONIC_SILVER", 0x757575,
                "SPACE_CADET", 0x1D2951,
                "SPANISH_BISTRE", 0x807532,
                "SPANISH_BLUE", 0x0070B8,
                "SPANISH_CARMINE", 0xD10047,
                "SPANISH_GRAY", 0x989898,
                "SPANISH_GREEN", 0x009150,
                "SPANISH_ORANGE", 0xE86100,
                "SPANISH_PINK", 0xF7BFBE,
                "SPANISH_RED", 0xE60026,
                "SPANISH_SKY_BLUE", 0x00FFFF,
                "SPANISH_VIOLET", 0x4C2882,
                "SPANISH_VIRIDIAN", 0x007F5C,
                "SPRING_BUD", 0xA7FC00,
                "SPRING_FROST", 0x87FF2A,
                "SPRING_GREEN", 0x00FF7F,
                "STAR_COMMAND_BLUE", 0x007BB8,
                "STEEL_BLUE", 0x4682B4,
                "STEEL_PINK", 0xCC33CC,
                "STEEL_TEAL", 0x5F8A8B,
                "STIL_DE_GRAIN_YELLOW", 0xFADA5E,
                "STRAW", 0xE4D96F,
                "STRAWBERRY", 0xFA5053,
                "STRAWBERRY_BLONDE", 0xFF9361,
                "SUGAR_PLUM", 0x914E75,
                "SUNGLOW", 0xFFCC33,
                "SUNRAY", 0xE3AB57,
                "SUNSET", 0xFAD6A5,
                "SUPER_PINK", 0xCF6BA9,
                "SWEET_BROWN", 0xA83731,
                "SYRACUSE_ORANGE", 0xD44500,
                "TAN", 0xD2B48C,
                "TANGERINE", 0xF28500,
                "TANGO_PINK", 0xE4717A,
                "TART_ORANGE", 0xFB4D46,
                "TAUPE", 0x483C32,
                "TAUPE_GRAY", 0x8B8589,
                "TEA_GREEN", 0xD0F0C0,
                "TEA_ROSE", 0xF4C2C2,
                "TEAL", 0x008080,
                "TEAL_BLUE", 0x367588,
                "TELEMAGENTA", 0xCF3476,
                "TERRA_COTTA", 0xE2725B,
                "THISTLE", 0xD8BFD8,
                "THULIAN_PINK", 0xDE6FA1,
                "TICKLE_ME_PINK", 0xFC89AC,
                "TIFFANY_BLUE", 0x0ABAB5,
                "TIMBERWOLF", 0xDBD7D2,
                "TITANIUM_YELLOW", 0xEEE600,
                "TOMATO", 0xFF6347,
                "TOURMALINE", 0x86A1A9,
                "TROPICAL_RAINFOREST", 0x00755E,
                "TRUE_BLUE", 0x2D68C4,
                "TRYPAN_BLUE", 0x1C05B3,
                "TUFTS_BLUE", 0x3E8EDE,
                "TUMBLEWEED", 0xDEAA88,
                "TURQUOISE", 0x40E0D0,
                "TURQUOISE_BLUE", 0x00FFEF,
                "TURQUOISE_GREEN", 0xA0D6B4,
                "TURTLE_GREEN", 0x8A9A5B,
                "TUSCAN", 0xFAD6A5,
                "TUSCAN_BROWN", 0x6F4E37,
                "TUSCAN_RED", 0x7C4848,
                "TUSCAN_TAN", 0xA67B5B,
                "TUSCANY", 0xC09999,
                "TWILIGHT_LAVENDER", 0x8A496B,
                "TYRIAN_PURPLE", 0x66023C,
                "UA_BLUE", 0x0033AA,
                "UA_RED", 0xD9004C,
                "ULTRAMARINE", 0x3F00FF,
                "ULTRAMARINE_BLUE", 0x4166F5,
                "ULTRA_PINK", 0xFF6FFF,
                "ULTRA_RED", 0xFC6C85,
                "UMBER", 0x635147,
                "UNBLEACHED_SILK", 0xFFDDCA,
                "UNITED_NATIONS_BLUE", 0x5B92E5,
                "UNIVERSITY_OF_PENNSYLVANIA_RED", 0xA50021,
                "UNMELLOW_YELLOW", 0xFFFF66,
                "UP_FOREST_GREEN", 0x014421,
                "UP_MAROON", 0x7B1113,
                "UPSDELL_RED", 0xAE2029,
                "URANIAN_BLUE", 0xAFDBF5,
                "USAFA_BLUE", 0x004F98,
                "VAN_DYKE_BROWN", 0x664228,
                "VANILLA", 0xF3E5AB,
                "VANILLA_ICE", 0xF38FA9,
                "VEGAS_GOLD", 0xC5B358,
                "VENETIAN_RED", 0xC80815,
                "VERDIGRIS", 0x43B3AE,
                "VERMILION", 0xE34234,
                "VERMILION", 0xD9381E,
                "VERONICA", 0xA020F0,
                "VIOLET", 0x8F00FF,
                "VIOLET-BLUE", 0x324AB2,
                "VIOLET-RED", 0xF75394,
                "VIRIDIAN", 0x40826D,
                "VIRIDIAN_GREEN", 0x009698,
                "VIVID_BURGUNDY", 0x9F1D35,
                "VIVID_SKY_BLUE", 0x00CCFF,
                "VIVID_TANGERINE", 0xFFA089,
                "VIVID_VIOLET", 0x9F00FF,
                "VOLT", 0xCEFF00,
                "WARM_BLACK", 0x004242,
                "WEEZY_BLUE", 0x189BCC,
                "WHEAT", 0xF5DEB3,
                "WHITE", 0xFFFFFF,
                "WILD_BLUE_YONDER", 0xA2ADD0,
                "WILD_ORCHID", 0xD470A2,
                "WILD_STRAWBERRY", 0xFF43A4,
                "WILD_WATERMELON", 0xFC6C85,
                "WINDSOR_TAN", 0xA75502,
                "WINE", 0x722F37,
                "WINE_DREGS", 0x673147,
                "WINTER_SKY", 0xFF007C,
                "WINTERGREEN_DREAM", 0x56887D,
                "WISTERIA", 0xC9A0DC,
                "WOOD_BROWN", 0xC19A6B,
                "XANADU", 0x738678,
                "XANTHIC", 0xEEED09,
                "XANTHOUS", 0xF1B42F,
                "YALE_BLUE", 0x00356B,
                "YELLOW", 0xFFFF00,
                "YELLOW-GREEN", 0x9ACD32,
                "YELLOW_ORANGE", 0xFFAE42,
                "YELLOW_SUNSHINE", 0xFFF700,
                "YINMN_BLUE", 0x2E5090,
                "ZAFFRE", 0x0014A8,
                "ZOMP", 0x39A78E
        );
    }

    private static final char[] BUFFER = new char[2048 * 10];

    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *                  renderers                  *
     *                                             *
     * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Format an input string completely, including legacy markers, hex markers, hex gradients and name gradients
     * @param s input string
     * @return the formatted string
     */
    @Nullable
    @CheckReturnValue
    public static String renderAll(@Nullable String s) {
        if (s == null) return null;
        s = applyGradients(s);
        return render(s);
    }

    @Nullable
    @CheckReturnValue
    public static String render(@Nullable final String s) {
        if (s == null) return null;

        /*
        StringBuilder builder = new StringBuilder(s);

        Matcher matcher = HEX_MARK_PATTERN.matcher(builder);
        while (matcher.find()) {
            // &#123456
            // §x§1§2§3§4§5§6
            String group = matcher.group();
            StringBuilder r = new StringBuilder(RENDER_CHAR + "x");
            for (int i=2; i < 8; i++) {
                r.append(RENDER_CHAR).append(group.charAt(i));
            }
            builder.replace(matcher.start(), matcher.end(), r.toString());
            matcher = HEX_MARK_PATTERN.matcher(builder);
        }

        matcher = LEGACY_MARK_PATTERN.matcher(builder);
        while (matcher.find()) {
            // &c
            // §c
            builder.setCharAt(matcher.start(), RENDER_CHAR);
            matcher = LEGACY_MARK_PATTERN.matcher(builder);
        }

        return builder.toString();
         */

        int size = render(s.toCharArray(), BUFFER, 0);
        return new String(BUFFER, 0, size);
    }

    @Nullable
    @CheckReturnValue
    public static String render_ThreadSafe(@Nullable String s) {
        if (s == null) return null;
        char[] res = new char[(int)(s.length()*1.667f) + 1];
        int offset = render(s.toCharArray(), res, 0);
        return new String(res, 0, offset);
    }

    @CheckReturnValue
    public static int render(@Nonnull char[] in, @Nonnull char[] out, int outOffset) {
        int end = outOffset;

        final int length = in.length;

        for (int i=0; i < length; i++) {
            // determine whether HEX char

            // & # 0 8 4 c f b H
            // 0 1 2 3 4 5 6 7 8
            char c0 = in[i];
            if (c0 == MARK_CHAR && i+1 < length) {
                char c1 = in[i + 1];
                if (c1 == '#' && i+7 < length) {
                    // analyze the characters ahead of time
                    // ... later
                    // Assuming that the characters do not need to
                    // be checked against during a '&#......' match,
                    // because 99.99% of the time the characters are intended
                    // to be converted into hex characters
                    // just asking who would pass &#zztyui into this
                    // function? such an extraneous value
                    // this allows for performance in the long run
                    // with the use of failsafe assumption


                    // Then scan assuming this branch will be taken
                    out[end++] = RENDER_CHAR;
                    out[end++] = 'x';
                    i += 2;
                    for (int w = 0; w < 6; w++) {
                        out[end++] = RENDER_CHAR;
                        out[end++] = in[i];
                        i++;
                    }
                    i--;
                    //out[end++] = in[i];
                    continue;
                }
                else if ((c1 >= '0' && c1 <= '9') || (c1 >= 'a' && c1 <= 'f') || (c1 >= 'k' && c1 <= 'o') || c1 == 'r') {
                    out[end++] = RENDER_CHAR;
                    out[end++] = c1;
                    i++;
                    continue;
                }
            }

            out[end++] = c0;
        }

        return end;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *                invert string                *
     *                                             *
     * * * * * * * * * * * * * * * * * * * * * * * */

    @Nullable
    @CheckReturnValue
    public static String invert(@Nullable String s) {
        if (s == null) return null;
        int offset = invert(s.toCharArray(), BUFFER, 0);
        return new String(BUFFER, 0, offset);
    }

    @Nullable
    @CheckReturnValue
    public static List<String> invert(@Nullable List<String> list) {
        if (list == null) return null;

        List<String> ret = new ArrayList<>();
        for (String lore : list) {
            ret.add(invert(lore));
        }

        return ret;
    }

    @Nullable
    @CheckReturnValue
    public static String invert_ThreadSafe(@Nullable String s) {
        if (s == null) return null;
        char[] res = new char[(int)(s.length()*1.667f) + 1];
        int offset = invert(s.toCharArray(), res, 0);
        return new String(res, 0, offset);
    }

    @CheckReturnValue
    public static int invert(@Nonnull char[] in, @Nonnull char[] out, int outOffset) {
        final int length = in.length;

        for (int i=0; i < length; i++) {
            char c0 = in[i];
            if (c0 == RENDER_CHAR && i+1 < length) {
                char c1 = in[i+1];

                if (c1 == 'x' && i+13 < length) {
                    // HEX
                    out[outOffset++] = MARK_CHAR;
                    out[outOffset++] = '#';
                    i++;
                    for (int w = 0; w < 6; w++) {
                        out[outOffset++] = in[i+=2];
                    }

                    continue;
                } else if ((c1 >= '0' && c1 <= '9')
                        || (c1 >= 'a' && c1 <= 'f')
                        || (c1 >= 'k' && c1 <= 'o')
                        || c1 == 'r') {
                    // LEGACY
                    out[outOffset++] = MARK_CHAR;

                    continue;
                }
            }

            out[outOffset++] = c0;
        }

        return outOffset;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *                 strip string                *
     *                                             *
     * * * * * * * * * * * * * * * * * * * * * * * */

    @Nullable
    @CheckReturnValue
    public static String strip(@Nullable String s) {
        return strip(s, false);
    }

    @Nullable
    @CheckReturnValue
    public static String strip(@Nullable String s, boolean removeMarkersOnly) {
        if (s == null) return null;
        int offset = strip(s.toCharArray(), BUFFER, 0, removeMarkersOnly);
        return new String(BUFFER, 0, offset);
    }

    @Nullable
    @CheckReturnValue
    public static String strip_ThreadSafe(@Nullable String s, boolean removeMarkersOnly) {
        if (s == null) return null;
        char[] res = new char[(int)(s.length()*1.667f) + 1];
        int offset = strip(s.toCharArray(), res, 0, removeMarkersOnly);
        return new String(res, 0, offset);
    }

    @CheckReturnValue
    public static int strip(@Nonnull char[] in, @Nonnull char[] out, int outOffset, boolean removeMarkersOnly) {
        final int length = in.length;

        for (int i=0; i < length; i++) {
            char c0 = in[i];
            if (((c0 == RENDER_CHAR && !removeMarkersOnly)
                    || (c0 == MARK_CHAR && removeMarkersOnly))
                    && i+1 < length) {
                char c1 = in[i+1];

                if (c1 == 'x' && i+13 < length) {
                    // HEX
                    i+=13;

                    continue;
                } else if ((c1 >= '0' && c1 <= '9')
                        || (c1 >= 'a' && c1 <= 'f')
                        || (c1 >= 'k' && c1 <= 'o')
                        || c1 == 'r') {
                    // LEGACY
                    i++;
                    continue;
                }
            }

            out[outOffset++] = c0;
        }
        return outOffset;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *                   gradient                  *
     *                                             *
     * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Translate gradients embedded as both hex and name markdown of an input string
     * @param in the input string
     * @return the hex marked string
     */
    @Nonnull
    @CheckReturnValue
    public static String applyGradients(@Nonnull final String in) {
        return ColorUtil.applyNameGradients(ColorUtil.applyHexGradients(in));
    }

    @Nonnull
    @CheckReturnValue
    public static String applyHexGradients(@Nonnull final String in) {

        StringBuilder builder = new StringBuilder(in.length()*15).append("&7").append(in);

        // begin and end gradient can be extracted sing string indexOf and lastIndexOf < and > chars
        Matcher matcher = GRADIENT_HEX_PATTERN.matcher(builder);
        while (matcher.find()) {
            // REPLACE

            String group = builder.substring(matcher.start(), matcher.end());

            // If version less than 1.16
            // then instead keep optional embedded legacy codes
            String text = group.substring(9, group.length() - 10);
            if (Version.AT_LEAST_v1_16.a()) {
                text = strip(text, true);
                Color start = toColor(group.substring(2, 8));
                Color end = toColor(group.substring(group.length() - 7, group.length() - 1));

                //noinspection ConstantConditions
                builder.replace(matcher.start(), matcher.end(),
                        applyEdgeColors(text, start, end) + "&7");
            } else {
                builder.replace(matcher.start(), matcher.end(), text + "&7");
            }
            matcher = GRADIENT_HEX_PATTERN.matcher(builder);
        }

        return builder.toString();
    }

    @Nonnull
    @CheckReturnValue
    public static String applyNameGradients(@Nonnull final String in) {

        StringBuilder builder = new StringBuilder(in.length()*15).append("&7").append(in);

        // begin and end gradient can be extracted sing string indexOf and lastIndexOf < and > chars
        Matcher matcher = GRADIENT_NAME_PATTERN.matcher(builder);
        while (matcher.find()) {
            // REPLACE
            String group = builder.substring(matcher.start(), matcher.end());

            if (Version.AT_LEAST_v1_16.a()) {
                String text = strip(group.substring(group.indexOf(">") + 1, group.lastIndexOf("<")), true);
                Color start = COLOR_MAP.get(group.substring(1, group.indexOf(">")));
                Color end = COLOR_MAP.get(group.substring(group.lastIndexOf("<") + 2, group.length() - 1));

                if (start != null && end != null) {
                    builder.replace(matcher.start(), matcher.end(),
                            applyEdgeColors(text, start, end) + "&7");
                    matcher = GRADIENT_NAME_PATTERN.matcher(builder);
                }
            } else {
                String text = group.substring(group.indexOf(">") + 1, group.lastIndexOf("<"));

                builder.replace(matcher.start(), matcher.end(), text + "&7");
            }
        }

        return builder.toString();
    }

    /**
     * Linearly apply an array of colors comprised of gradients across an input string, resulting in a hex marked string
     * @param in the input string
     * @param colors the gradients
     * @return the hex marked string
     */
    @Nonnull
    @CheckReturnValue
    public static String applyEdgeColors(@Nonnull final String in, @Nonnull final Color... colors) {
        return applyAllColors(in, generateColors(in.length(), colors));
    }

    /**
     * Linearly apply a {@link List<Color>} across each character of an input string, returning a hex marked string
     * @param in input string
     * @param colors colors to linearly
     * @return the hex marked string
     */
    @Nonnull
    @CheckReturnValue
    private static String applyAllColors(@Nonnull final String in, @Nonnull final List<Color> colors) {
        // apply a foreach merge of colors onto 'in'
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < in.length(); i++) {
            builder.append(toHexMark(colors.get(i))).append(in.charAt(i));
        }

        return builder.toString();
    }

    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *               color generator               *
     *                                             *
     * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Construct a gradient comprised of {@link Color colors} many color edges
     * @param totalColors blends to generate
     * @param edgeColors edge colors
     * @return {@link ArrayList<Color>} colors
     * @throws IllegalArgumentException if edgeColors is less than 2
     */
    @Nonnull
    @CheckReturnValue
    private static ArrayList<Color> generateColors(@Nonnull int totalColors, @Nonnull Color... edgeColors) {
        int gradientCount = edgeColors.length - 1;

        if (gradientCount < 1)
            throw new IllegalArgumentException("Requires at least two colors to form a gradient");

        float avgChange = (float) totalColors / (float) gradientCount;
        float currentShift = 0;
        float lastShift;

        ArrayList<Color> colors = new ArrayList<>();

        for (int gradientIndex = 0; gradientIndex < gradientCount; gradientIndex++) {
            lastShift = currentShift;
            currentShift += avgChange;

            int samples = Math.round(currentShift) - Math.round(lastShift);

            colors.addAll(generateColors(edgeColors[gradientIndex], edgeColors[gradientIndex+1],
                    samples));
        }

        return colors;
    }

    /**
     * Construct a single linear gradient from start to end
     * @param startEdge {@link Color} start color
     * @param endEdge {@link Color} end color
     * @param totalColors the total colors to generate
     * @return {@link ArrayList<Color>} colors
     */
    @Nonnull
    @CheckReturnValue
    private static ArrayList<Color> generateColors(Color startEdge, Color endEdge, int totalColors) {
        int r1 = startEdge.getRed();
        int g1 = startEdge.getGreen();
        int b1 = startEdge.getBlue();

        int div = totalColors > 1 ? totalColors - 1 : 1;

        final int rc = (endEdge.getRed() - r1 + 1) / div;
        final int gc = (endEdge.getGreen() - g1 + 1) / div;
        final int bc = (endEdge.getBlue() - b1 + 1) / div;

        ArrayList<Color> colors = new ArrayList<>();

        for (int i = 0; i < totalColors; i++) {
            colors.add(toColor(r1, g1, b1));

            r1 += rc;
            g1 += gc;
            b1 += bc;
        }

        return colors;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *                   utility                   *
     *                                             *
     * * * * * * * * * * * * * * * * * * * * * * * */

    private static String toHexMark(Color color) {
        return "&#" + String.format("%06x", color.asRGB());
    }

    private static Color toColor(int r, int g, int b) {
        return Color.fromRGB(clamp(r, 0, 255), clamp(g, 0, 255), clamp(b, 0, 255));
    }

    private static Color toColor(String hex) {
        return Color.fromRGB(Integer.parseInt(hex, 16));
    }

    private static String toHex(Color color) {
        return Integer.toHexString(color.asRGB());
    }
}
