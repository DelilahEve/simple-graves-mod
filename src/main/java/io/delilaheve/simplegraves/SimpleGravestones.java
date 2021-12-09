package io.delilaheve.simplegraves;

import io.delilaheve.simplegraves.config.SimpleConfig;
import io.delilaheve.simplegraves.registry.ModBlocks;
import io.delilaheve.simplegraves.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class SimpleGravestones implements ModInitializer {

    // Mod identifiers
    public static final String MOD_ID = "lilysgraves";
    public static final Identifier GRAVE = new Identifier(MOD_ID, "gravestone_block");

    // Config defaults
    public static final ArrayList<String> defaultReplaceBlocks = new ArrayList<>();
    public static final String defaultListMode = "whitelist";
    public static final boolean defaultReplaceAny = false;
    public static final int defaultSearchRadius = 7;
    public static final int defaultGraveSlots = 100;
    public static final int defaultFallbackHeight = 100;

    // Config keys
    public static final String replaceBlocksKey = "replace";
    public static final String listModeKey = "mode";
    public static final String replaceAnyKey = "replaceAny";
    public static final String searchRadiusKey = "searchRadius";
    public static final String graveSlotsKey = "graveSlots";
    public static final String fallbackHeightKey = "fallbackHeight";

    // Config list modes
    public enum ListModes {
        WHITELIST,
        BLACKLIST
    }
    public static final int minSearchRadius = 1;
    public static final int maxSearchRadius = 100;
    public static final int minGraveSlots = 1;
    public static final int maxGraveSlots = 100;
    public static final int minFallbackHeight = 1;
    public static final int maxFallbackHeight = 256;

    // Config accessor
    private static final SimpleConfig config = SimpleConfig.of("lilysgraves")
            .provider(SimpleGravestones::provider)
            .request();

    /**
     * Init mod
     */
    @Override
    public void onInitialize() {
        ModItems.registerItems();
        ModBlocks.registerBlocks();
    }

    /**
     * Config file default provider
     *
     * @param fileName file name to provide default text for
     *
     * @return default text for fileName
     */
    public static String provider(String fileName) {
        if (defaultReplaceBlocks.isEmpty()) {
            defaultReplaceBlocks.add("grass");
            defaultReplaceBlocks.add("snow");
            defaultReplaceBlocks.add("netherrack");
            defaultReplaceBlocks.add("stone");
            defaultReplaceBlocks.add("deepslate");
            defaultReplaceBlocks.add("nether_bricks");
            defaultReplaceBlocks.add("gravel");
            defaultReplaceBlocks.add("andesite");
            defaultReplaceBlocks.add("granite");
            defaultReplaceBlocks.add("diorite");
            defaultReplaceBlocks.add("sand");
            defaultReplaceBlocks.add("red_sand");
            defaultReplaceBlocks.add("sandstone");
            defaultReplaceBlocks.add("red_sandstone");
            defaultReplaceBlocks.add("mycelium");
            defaultReplaceBlocks.add("end_stone");
            defaultReplaceBlocks.add("podzol");
        }
        try {
            String defaultReplaceBlocksString = String.join(",", defaultReplaceBlocks);
            return "# A list of blocks by name that the gravestone can/can't replace\n" +
                    "# Accepted values: block identifiers (if prefix is excluded minecraft: will be assumed)\n" +
                    "# Default:" + defaultReplaceBlocksString + "\n" +
                    replaceBlocksKey + "=" + defaultReplaceBlocksString + "\n" +
                    "\n" +
                    "# Whether the above list should be treated as a whitelist or a blacklist\n" +
                    "# Accepted values: whitelist, blacklist\n" +
                    "# Default:" + defaultListMode + "\n" +
                    listModeKey + "=" + defaultListMode + "\n" +
                    "\n" +
                    "# Whether the replace list should be ignored and allow any block to be replaced\n" +
                    "# WARNING: This setting will cause ANY block to be replaced with discrimination\n" +
                    "# Accepted values: true, false\n" +
                    "# Default:" + defaultReplaceAny + "\n" +
                    replaceAnyKey + "=" + defaultReplaceAny + "\n" +
                    "\n" +
                    "# Radius to search for a valid grave placement\n" +
                    "# A larger radius can mean slower grave spawning\n" +
                    "# This only affects horizontal radius, vertical is full world height\n" +
                    "# Accepted values: positive integer between " + minSearchRadius + " and " + maxSearchRadius + "\n" +
                    "# Default:" + defaultSearchRadius + "\n" +
                    searchRadiusKey + "=" + defaultSearchRadius + "\n" +
                    "\n" +
                    "# Maximum slots to save in a grave\n" +
                    "# Accepted values: positive integer between " + minGraveSlots + " and " + maxGraveSlots + "\n" +
                    "# Default: " + defaultGraveSlots + "\n" +
                    graveSlotsKey + "=" + defaultGraveSlots + "\n" +
                    "\n" +
                    "# Default height to fallback to when an appropriate place to spawn the grave can't be found\n" +
                    "# WARNING: Setting this above 128 can cause graves to be placed above the nether roof\n" +
                    "# Accepted values: positive integer between " + minFallbackHeight + " and " + maxFallbackHeight +
                    "\n" +
                    "# Default: " + defaultFallbackHeight + "\n" +
                    fallbackHeightKey + "=" + defaultFallbackHeight;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Get a list of block names from config
     *
     * @return String[] of block names
     */
    public static String[] getReplaceBlocks() {
        String defaultReplaceBlocksString = String.join(",", defaultReplaceBlocks);
        return config.getOrDefault(replaceBlocksKey, defaultReplaceBlocksString)
            .split(",");
    }

    /**
     * Get block list mode from config
     *
     * @return ListMode as set or default if invalid/missing
     */
    public static ListModes getListMode() {
        String configValue = config.getOrDefault(listModeKey, defaultListMode);
        ListModes mode = ListModes.WHITELIST;
        try {
            mode = ListModes.valueOf(configValue);
        } catch (IllegalArgumentException ignored) { }
        return mode;
    }

    /**
     * Get replace any value from config
     *
     * @return true if replace any allowed
     */
    public static boolean getReplaceAny() {
        return config.getOrDefault(replaceAnyKey, defaultReplaceAny);
    }

    /**
     * Get search radius from config
     *
     * @return search radius integer
     */
    public static int getSearchRadius() {
        int configValue = config.getOrDefault(searchRadiusKey, defaultSearchRadius);
        if (configValue > maxSearchRadius || configValue < minSearchRadius) {
            configValue = defaultSearchRadius;
        }
        return configValue;
    }

    /**
     * Get grave slots from config
     *
     * @return grave slots integer
     */
    public static int getGraveSlots() {
        int configValue = config.getOrDefault(graveSlotsKey, defaultGraveSlots);
        if (configValue > maxGraveSlots || configValue < minGraveSlots) {
            configValue = defaultGraveSlots;
        }
        return configValue;
    }

    /**
     * Get fallback height from config
     *
     * @return fallback spawn height integer
     */
    public static int getFallbackHeight() {
        int configValue = config.getOrDefault(fallbackHeightKey, defaultFallbackHeight);
        if (configValue > maxFallbackHeight || configValue < minFallbackHeight) {
            configValue = defaultFallbackHeight;
        }
        return configValue;
    }

}
