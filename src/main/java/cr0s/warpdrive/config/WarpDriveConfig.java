package cr0s.warpdrive.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.filler.FillerManager;
import cr0s.warpdrive.config.structures.StructureManager;
import cr0s.warpdrive.data.Planet;

public class WarpDriveConfig {
	private static File configDirectory;
	private static DocumentBuilder xmlDocumentBuilder;
	private static final String[] defaultXMLfilenames = {
		// fillers
		"filler-default.xml", "filler-ae2.xml", "filler-netherores.xml",
		// structures
		"structures-default.xml", "structures-netherores.xml",
	};

	/*
	 * The variables which store whether or not individual mods are loaded
	 */
	public static boolean isForgeMultipartLoaded = false;
	public static boolean isAdvancedSolarPanelLoaded = false;
	public static boolean isAppliedEnergistics2Loaded = false;
	public static boolean isAtomicScienceLoaded = false;
	public static boolean isICBMLoaded = false;
	public static boolean isMFFSLoaded = false;
	public static boolean isGraviSuiteLoaded = false;
	public static boolean isIndustrialCraft2loaded = false;
	public static boolean isComputerCraftLoaded = false;
	public static boolean isOpenComputersLoaded = false;
	public static boolean isNetherOresLoaded = false;
	public static boolean isThermalExpansionLoaded = false;
	public static boolean isAdvancedRepulsionSystemsLoaded = false;
	public static boolean isMagicalCropsLoaded = false;

	// ForgeMultipart (microblocks) support
	public static Method forgeMultipart_helper_createTileFromNBT = null;
	public static Method forgeMultipart_helper_sendDescPacket = null;
	public static Method forgeMultipart_tileMultipart_onChunkLoad = null;

	public static ItemStack IC2_air;
	public static ItemStack IC2_empty;
	public static ItemStack IC2_rubberWood;
	public static ItemStack IC2_Resin;
	public static Item IC2_fluidCell;
	public static Block CC_Computer, CC_peripheral, CCT_Turtle, CCT_Expanded, CCT_Advanced;

	public static ItemStack GT_Ores, GT_Granite, GT_Machine;
	public static ItemStack IC2_solarPanel;
	public static int AS_Turbine, AS_deuteriumCell;
	public static int ICBM_Machine, ICBM_Missile, ICBM_Explosive;
	public static Item GS_ultimateLappack;
	public static ArrayList<Block> forceFieldBlocks;

	public static ArrayList<Block> minerOres, minerLogs, minerLeaves, scannerIgnoreBlocks;
	public static ArrayList<Item> spaceHelmets, jetpacks;
	public static ArrayList<Block> commonWorldGenOres;

	// Mod configuration (see loadWarpDriveConfig() for comments/definitions)
	// General
	public static int G_SPACE_PROVIDER_ID = 14;
	public static int G_SPACE_DIMENSION_ID = -2;
	public static int G_HYPERSPACE_PROVIDER_ID = 15;
	public static int G_HYPERSPACE_DIMENSION_ID = -3;
	public static int G_SPACE_WORLDBORDER_BLOCKS = 100000;
	public static int G_ENTITY_JUMP_ID = 240;
	public static int G_ENTITY_SPHERE_GENERATOR_ID = 241;
	public static int G_ENTITY_STAR_CORE_ID = 242;
	public static int G_ENTITY_CAMERA_ID = 243;

	public static final int LUA_SCRIPTS_NONE = 0;
	public static final int LUA_SCRIPTS_TEMPLATES = 1;
	public static final int LUA_SCRIPTS_ALL = 2;
	public static int G_LUA_SCRIPTS = LUA_SCRIPTS_ALL;
	public static String G_SCHEMALOCATION = "warpDrive_schematics";
	public static int G_BLOCKS_PER_TICK = 3500;

	public static boolean RECIPES_ENABLE_IC2 = true;
	public static boolean RECIPES_ENABLE_HARD_IC2 = false;
	public static boolean RECIPES_ENABLE_VANILLA = false;
	public static boolean RECIPES_ENABLE_MIXED = false;

	// logging
	public static boolean LOGGING_JUMP = false;
	public static boolean LOGGING_ENERGY = false;
	public static boolean LOGGING_EFFECTS = false;
	public static boolean LOGGING_CLOAKING = false;
	public static boolean LOGGING_FREQUENCY = false;
	public static boolean LOGGING_TARGETTING = false;
	public static boolean LOGGING_WEAPON = false;
	public static boolean LOGGING_CAMERA = false;
	public static boolean LOGGING_BUILDING = false;
	public static boolean LOGGING_COLLECTION = false;
	public static boolean LOGGING_TRANSPORTER = false;
	public static boolean LOGGING_LUA = false;
	public static boolean LOGGING_RADAR = false;
	public static boolean LOGGING_BREATHING = false;
	public static boolean LOGGING_WORLDGEN = false;

	// Transition planes
	public static Planet[] PLANETS = null;

	// Warp Drive Core
	public static int SHIP_MAX_ENERGY_STORED = 100000000;
	public static int SHIP_NORMALJUMP_ENERGY_PER_BLOCK = 10;
	public static int SHIP_NORMALJUMP_ENERGY_PER_DISTANCE = 100;
	public static int SHIP_HYPERJUMP_ENERGY_PER_BLOCK = 1000;
	public static int SHIP_HYPERJUMP_ENERGY_PER_DISTANCE = 1000;
	public static int SHIP_TELEPORT_ENERGY_PER_ENTITY = 1000000;
	public static int SHIP_MAX_JUMP_DISTANCE = 128;
	public static int SHIP_VOLUME_MAX_ON_PLANET_SURFACE = 3000;
	public static int SHIP_VOLUME_MIN_FOR_HYPERSPACE = 1200;
	public static int SHIP_MAX_SIDE_SIZE = 127;
	public static int SHIP_COOLDOWN_INTERVAL_SECONDS = 30;
	public static int SHIP_COLLISION_TOLERANCE_BLOCKS = 3;
	public static int SHIP_SHORTJUMP_THRESHOLD_BLOCKS = 50;
	public static int SHIP_SHORTJUMP_WARMUP_SECONDS = 10;
	public static int SHIP_LONGJUMP_WARMUP_SECONDS = 30;
	public static int SHIP_WARMUP_RANDOM_TICKS = 60;
	public static int SHIP_CORE_REGISTRY_UPDATE_INTERVAL_SECONDS = 10;
	public static int SHIP_CONTROLLER_UPDATE_INTERVAL_SECONDS = 2;
	public static int SHIP_CORE_ISOLATION_UPDATE_INTERVAL_SECONDS = 10;
	public static String[] SHIP_VOLUME_UNLIMITED_PLAYERNAMES = { "notch", "someone" };
	public static boolean SHIP_WARMUP_SICKNESS = true;

	// Warp Radar
	public static int RADAR_MAX_ENERGY_STORED = 100000000; // 100kk eU
	public static int RADAR_MAX_ISOLATION_RANGE = 2;
	public static int RADAR_MIN_ISOLATION_BLOCKS = 5;
	public static int RADAR_MAX_ISOLATION_BLOCKS = 132;
	public static double RADAR_MIN_ISOLATION_EFFECT = 0.12;
	public static double RADAR_MAX_ISOLATION_EFFECT = 1.00;

	// Ship Scanner
	public static int SS_MAX_ENERGY_STORED = 500000000;
	public static int SS_EU_PER_BLOCK_SCAN = 100; // eU per block of ship volume
	// (including air)
	public static int SS_EU_PER_BLOCK_DEPLOY = 5000;
	public static int SS_MAX_DEPLOY_RADIUS_BLOCKS = 50;

	// Particle Booster
	public static int LASER_MEDIUM_MAX_ENERGY_STORED = 100000;

	// Laser Emitter
	public static int LASER_CANNON_MAX_MEDIUMS_COUNT = 10;
	public static int LASER_CANNON_MAX_LASER_ENERGY = 4000000;
	public static int LASER_CANNON_EMIT_FIRE_DELAY_TICKS = 20 * 3;
	public static int LASER_CANNON_EMIT_SCAN_DELAY_TICKS = 10;

	public static double LASER_CANNON_BOOSTER_BEAM_ENERGY_EFFICIENCY = 0.60D;
	public static int LASER_CANNON_ENERGY_LOSS_PER_BLOCK = 5000;
	public static int LASER_CANNON_ENTITY_HIT_SET_ON_FIRE_SECONDS = 100;
	public static int LASER_CANNON_ENTITY_HIT_ENERGY_PER_DAMAGE = 10000;
	public static int LASER_CANNON_ENTITY_HIT_MAX_DAMAGE = 100;
	public static int LASER_CANNON_ENTITY_HIT_ENERGY_THRESHOLD_FOR_EXPLOSION = 1000000;
	public static float LASER_CANNON_ENTITY_HIT_EXPLOSION_BASE_STRENGTH = 4.0F;
	public static int LASER_CANNON_ENTITY_HIT_EXPLOSION_ENERGY_PER_STRENGTH = 125000;
	public static float LASER_CANNON_ENTITY_HIT_EXPLOSION_MAX_STRENGTH = 4.0F;

	public static int LASER_CANNON_BLOCK_HIT_ENERGY = 70000;
	public static int LASER_CANNON_BLOCK_HIT_ENERGY_PER_BLOCK_RESISTANCE = 1000;
	public static int LASER_CANNON_BLOCK_HIT_ENERGY_PER_DISTANCE = 10;
	public static double LASER_CANNON_BLOCK_HIT_EXPLOSION_RESISTANCE_THRESHOLD = 1200.0D; // obsidian is 2000 * 3 / 5 = 1200
	public static float LASER_CANNON_BLOCK_HIT_EXPLOSION_BASE_STRENGTH = 8.0F;
	public static int LASER_CANNON_BLOCK_HIT_EXPLOSION_ENERGY_PER_STRENGTH = 125000;
	public static float LASER_CANNON_BLOCK_HIT_EXPLOSION_MAX_STRENGTH = 100F;

	// Mining Laser
	// BuildCraft quarry values for reference
	// - harvesting one block is 60 MJ/block = 600 RF/block = ~145 EU/block
	// - maximum speed is 3.846 ticks per blocks
	// - overall consumption varies from 81.801 to 184.608 MJ/block (depending on speed) = up to 1846.08 RF/block = up to ~448 EU/block
	// - at radius 5, one layer takes ~465 ticks ((ML_MAX_RADIUS * 2 + 1) ^ 2 * 3.846)
	// - overall consumption is ((ML_MAX_RADIUS * 2 + 1) ^ 2) * 448 => ~ 54208 EU/layer
	// WarpDrive mining laser in comparison
	// - each mined layer is scanned twice
	// - default ore generation: 1 ore out of 25 blocks
	// - overall consumption in 'all, space' is ML_EU_PER_LAYER_SPACE / ((ML_MAX_RADIUS * 2 + 1) ^ 2) + ML_EU_PER_BLOCK_SPACE => ~ 356 EU/block
	// - overall consumption in 'all, space' is ML_EU_PER_LAYER_SPACE + ((ML_MAX_RADIUS * 2 + 1) ^ 2) * ML_EU_PER_BLOCK_SPACE => ~ 43150 EU/layer
	// - overall consumption in 'ores, space' is ML_EU_PER_LAYER_SPACE + ((ML_MAX_RADIUS * 2 + 1) ^ 2) * ML_EU_PER_BLOCK_SPACE * ML_EU_MUL_ORESONLY / 25 => ~ 28630 EU/layer
	// - at radius 5, one layer takes 403 ticks (2 * ML_SCAN_DELAY_TICKS + ML_MINE_DELAY_TICKS * (ML_MAX_RADIUS * 2 + 1) ^ 2)
	public static int MINING_LASER_MAX_MEDIUMS_COUNT = 1;
	public static int MINING_LASER_RADIUS_BLOCKS = 5;
	public static int MINING_LASER_WARMUP_DELAY_TICKS = 20;
	public static int MINING_LASER_SCAN_DELAY_TICKS = 20;
	public static int MINING_LASER_MINE_DELAY_TICKS = 3;
	public static int MINING_LASER_SPACE_ENERGY_PER_LAYER = 25000;
	public static int MINING_LASER_PLANET_ENERGY_PER_LAYER = 35000;
	public static int MINING_LASER_SPACE_ENERGY_PER_BLOCK = 150;
	public static int MINING_LASER_PLANET_ENERGY_PER_BLOCK = 300;
	public static double MINING_LASER_ORESONLY_ENERGY_FACTOR = 5.0; // lower value encourages to keep the land 'clean'
	public static double MINING_LASER_SILKTOUCH_ENERGY_FACTOR = 2.5;
	public static double MINING_LASER_SILKTOUCH_DEUTERIUM_L = 1.0;
	public static double MINING_LASER_FORTUNE_ENERGY_FACTOR = 2.5;

	// Tree farm
	public static int TREE_FARM_MIN_RADIUS = 5;
	public static int TREE_FARM_MAX_RADIUS = 16;

	// Cloaking
	public static int CLOAKING_MAX_ENERGY_STORED = 500000000;
	public static int CLOAKING_COIL_CAPTURE_BLOCKS = 5;
	public static int CLOAKING_MAX_FIELD_RADIUS = 100;
	public static int CLOAKING_TIER1_ENERGY_PER_BLOCK = 125;
	public static int CLOAKING_TIER2_ENERGY_PER_BLOCK = 500;
	public static int CLOAKING_FIELD_REFRESH_INTERVAL_SECONDS = 3;

	// Air generator
	public static int AIRGEN_ENERGY_PER_CANISTER = 20;
	public static int AIRGEN_ENERGY_PER_NEWAIRBLOCK = 12;
	public static int AIRGEN_ENERGY_PER_EXISTINGAIRBLOCK = 4;
	public static int AIRGEN_MAX_ENERGY_STORED = 4000;
	public static int AIRGEN_AIR_GENERATION_TICKS = 40;

	// IC2 Reactor monitor
	public static int IC2_REACTOR_MAX_ENERGY_STORED = 1000000;
	public static double IC2_REACTOR_ENERGY_PER_HEAT = 2;
	public static int IC2_REACTOR_COOLING_INTERVAL_TICKS = 10;

	// Transporter
	public static int TRANSPORTER_MAX_ENERGY = 1000000;
	public static boolean TRANSPORTER_USE_RELATIVE_COORDS = true;
	public static double TRANSPORTER_ENERGY_PER_BLOCK = 100.0;
	public static double TRANSPORTER_MAX_BOOST_MUL = 4.0;

	// Enantiomorphic Power reactor
	public static int ENAN_REACTOR_MAX_ENERGY_STORED = 100000000;
	public static int ENAN_REACTOR_UPDATE_INTERVAL_TICKS = 5;
	public static int ENAN_REACTOR_MAX_LASERS_PER_SECOND = 6;

	// Power store
	public static int ENERGY_BANK_MAX_ENERGY_STORED = 1000000;

	// Laser Lift
	public static int LIFT_MAX_ENERGY_STORED = 2400;
	public static int LIFT_ENERGY_PER_ENTITY = 800;
	public static int LIFT_UPDATE_INTERVAL_TICKS = 10;

	// Chunk Loader
	public static int CL_MAX_ENERGY = 1000000;
	public static int CL_MAX_DISTANCE = 2;
	public static int CL_RF_PER_CHUNKTICK = 320;

	public static Block getModBlock(final String mod, final String id) {
		try {
			return GameRegistry.findBlock(mod, id);
		} catch (Exception exception) {
			WarpDrive.logger.info("Failed to get mod block for " + mod + ":" + id);
			exception.printStackTrace();
		}
		return null;
	}

	public static ItemStack getModItemStack(final String mod, final String id, final int meta) {
		try {
			ItemStack item = new ItemStack((Item) Item.itemRegistry.getObject(mod + ":" + id));
			if (meta != -1) {
				item.setItemDamage(meta);
			}
			return item;
		} catch (Exception exception) {
			WarpDrive.logger.info("Failed to get mod item for " + mod + ":" + id + ":" + meta);
		}
		return null;
	}

	public static void preInit(final String stringConfigDirectory) {
		// create mod folder
		configDirectory = new File(stringConfigDirectory, WarpDrive.MODID);
		configDirectory.mkdir();
		if (!configDirectory.isDirectory()) {
			throw new RuntimeException("Unable to create config directory " + configDirectory);
		}

		// read configuration file
		loadWarpDriveConfig(new File(configDirectory, WarpDrive.MODID + ".cfg"));
	}

	public static void loadWarpDriveConfig(File file) {
		Configuration config = new Configuration(file);
		config.load();

		// General
		G_SPACE_PROVIDER_ID = clamp(Integer.MIN_VALUE, Integer.MAX_VALUE,
				config.get("general", "space_provider_id", G_SPACE_PROVIDER_ID, "Space dimension provider ID").getInt());
		G_SPACE_DIMENSION_ID = clamp(Integer.MIN_VALUE, Integer.MAX_VALUE,
				config.get("general", "space_dimension_id", G_SPACE_DIMENSION_ID, "Space dimension world ID").getInt());
		G_HYPERSPACE_PROVIDER_ID = clamp(Integer.MIN_VALUE, Integer.MAX_VALUE,
				config.get("general", "hyperspace_provider_id", G_HYPERSPACE_PROVIDER_ID, "Hyperspace dimension provider ID").getInt());
		G_HYPERSPACE_DIMENSION_ID = clamp(Integer.MIN_VALUE, Integer.MAX_VALUE,
				config.get("general", "hyperspace_dimension_id", G_HYPERSPACE_DIMENSION_ID, "Hyperspace dimension world ID").getInt());
		G_SPACE_WORLDBORDER_BLOCKS = clamp(0, 3000000,
				config.get("general", "space_worldborder_blocks", G_SPACE_WORLDBORDER_BLOCKS,
						"World border applied to hyperspace & space, set to 0 to disable it").getInt());

		G_ENTITY_JUMP_ID = clamp(Integer.MIN_VALUE, Integer.MAX_VALUE,
				config.get("general", "entity_jump_id", G_ENTITY_JUMP_ID, "Entity jump ID").getInt());
		G_ENTITY_SPHERE_GENERATOR_ID = clamp(Integer.MIN_VALUE, Integer.MAX_VALUE,
				config.get("general", "entity_sphere_generator_id", G_ENTITY_SPHERE_GENERATOR_ID, "Entity sphere generator ID").getInt());
		G_ENTITY_STAR_CORE_ID = clamp(Integer.MIN_VALUE, Integer.MAX_VALUE,
				config.get("general", "entity_star_core_id", G_ENTITY_STAR_CORE_ID, "Entity star core ID").getInt());
		G_ENTITY_CAMERA_ID = clamp(Integer.MIN_VALUE, Integer.MAX_VALUE,
				config.get("general", "entity_camera_id", G_ENTITY_CAMERA_ID, "Entity camera ID").getInt());

		G_LUA_SCRIPTS = clamp(0, 2,
				config.get("general", "lua_scripts", G_LUA_SCRIPTS,
						"LUA scripts to load when connecting machines: 0 = none, 1 = templates in a subfolder, 2 = ready to roll (templates are still provided)").getInt());
		G_SCHEMALOCATION = config.get("general", "schematic_location", G_SCHEMALOCATION, "Folder where to save ship schematics").getString();
		G_BLOCKS_PER_TICK = clamp(100, 100000,
				config.get("general", "blocks_per_tick", G_BLOCKS_PER_TICK,
						"Number of blocks to move per ticks, too high will cause lag spikes on ship jumping or deployment, too low may break the ship wirings").getInt());

		// Recipes
		RECIPES_ENABLE_VANILLA = config.get("recipes", "enable_vanilla", RECIPES_ENABLE_VANILLA, "Vanilla recipes by DarkholmeTenk").getBoolean(false);
		RECIPES_ENABLE_IC2 = config.get("recipes", "enable_ic2", RECIPES_ENABLE_IC2, "Original recipes based on IndustrialCrat2 by Cr0s").getBoolean(true);
		RECIPES_ENABLE_HARD_IC2 = config.get("recipes", "enable_hard_ic2", RECIPES_ENABLE_HARD_IC2, "Harder recipes based on IC2 by YuRaNnNzZZ").getBoolean(false);
		RECIPES_ENABLE_MIXED = config.get("recipes", "enable_mixed", RECIPES_ENABLE_MIXED,
				"Mixed recipes for Lem'ADEC's packs (currently requires at least AppliedEnergistics, Extracells, AtomicScience, IndustrialCraft2, GraviSuite and ThermalExpansion").getBoolean(false);

		// Logging
		LOGGING_JUMP = config.get("logging", "enable_jump_debugLogs", LOGGING_JUMP, "Detailled jump logs to help debug the mod, enable it before reporting a bug").getBoolean(false);
		LOGGING_ENERGY = config.get("logging", "enable_energy_debugLogs", LOGGING_ENERGY, "Detailled energy logs to help debug the mod, enable it before reporting a bug").getBoolean(false);
		if (WarpDrive.VERSION.contains("-dev")) {// disabled in production, for obvious reasons :)
			LOGGING_EFFECTS = config.get("logging", "enable_effects_logs", LOGGING_EFFECTS, "Detailled effects logs to help debug the mod, will spam your console!").getBoolean(false);
			LOGGING_CLOAKING = config.get("logging", "enable_cloaking_logs", LOGGING_CLOAKING, "Detailled cloaking logs to help debug the mod, will spam your console!").getBoolean(false);
			LOGGING_FREQUENCY = config.get("logging", "enable_frequency_logs", LOGGING_FREQUENCY, "Detailled frequency logs to help debug the mod, will spam your console!").getBoolean(false);
			LOGGING_TARGETTING = config.get("logging", "enable_targetting_logs", LOGGING_TARGETTING, "Detailled targetting logs to help debug the mod, will spam your console!").getBoolean(false);
		} else {
			LOGGING_EFFECTS = false;
			LOGGING_CLOAKING = false;
			LOGGING_FREQUENCY = false;
			LOGGING_TARGETTING = false;
		}
		LOGGING_WEAPON = config.get("logging", "enable_weapon_logs", LOGGING_WEAPON, "Detailled weapon logs to help debug the mod, enable it before reporting a bug").getBoolean(false);
		LOGGING_CAMERA = config.get("logging", "enable_camera_logs", LOGGING_CAMERA, "Detailled camera logs to help debug the mod, enable it before reporting a bug").getBoolean(false);
		LOGGING_BUILDING = config.get("logging", "enable_building_logs", LOGGING_BUILDING, "Detailled building logs to help debug the mod, enable it before reporting a bug").getBoolean(false);
		LOGGING_COLLECTION = config.get("logging", "enable_collection_logs", LOGGING_COLLECTION, "Detailled collection logs to help debug the mod, enable it before reporting a bug").getBoolean(false);
		LOGGING_TRANSPORTER = config.get("logging", "enable_transporter_logs", LOGGING_TRANSPORTER, "Detailled transporter logs to help debug the mod, enable it before reporting a bug").getBoolean(false);
		LOGGING_LUA = config.get("logging", "enable_LUA_logs", LOGGING_LUA, "Detailled LUA logs to help debug the mod, enable it before reporting a bug").getBoolean(false);
		LOGGING_RADAR = config.get("logging", "enable_radar_logs", LOGGING_RADAR, "Detailled radar logs to help debug the mod, enable it before reporting a bug").getBoolean(false);
		LOGGING_BREATHING = config.get("logging", "enable_breathing_logs", LOGGING_BREATHING, "Detailled breathing logs to help debug the mod, enable it before reporting a bug").getBoolean(false);
		LOGGING_WORLDGEN = config.get("logging", "enable_worldgen_logs", LOGGING_WORLDGEN, "Detailled world generation logs to help debug the mod, enable it before reporting a bug").getBoolean(false);

		// Planets
		config.addCustomCategoryComment("planets",
				"Planets are other dimensions connected through the Space dimension. Default is overworld with 100k radius.\n"
						+ "Each planet orbit is square shaped and defined as a list of 7 integers (all measured in blocks).");

		ConfigCategory cat = config.getCategory("planets");
		String[] planetsName = cat.getValues().keySet().toArray(new String[0]);
		if (planetsName.length == 0) {
			planetsName = new String[] { "overworld" };
		}

		int[] defaultPlanet = { 0, 0, 0, 100000, 100000, 0, 0 }; // 30000000 is Minecraft limit for SetBlock
		PLANETS = new Planet[planetsName.length];
		int index = 0;
		for (String name : planetsName) {
			int[] planetInts = config.get("planets", name, defaultPlanet,
					"dimensionId, dimensionCenterX, dimensionCenterZ, radiusX, radiusZ, spaceCenterX, spaceCenterZ").getIntList();
			if (planetInts.length != 7) {
				WarpDrive.logger.warn("Invalid planet definition '" + name + "' (exactly 7 integers are expected), using default instead");
				planetInts = defaultPlanet.clone();
			}
			Planet planet = new Planet(planetInts[0], planetInts[1], planetInts[2], planetInts[3], planetInts[4], planetInts[5], planetInts[6]);
			WarpDrive.logger.info("Adding '" + name + "' as " + planet.toString());
			PLANETS[index] = planet;
			index++;
		}
		// FIXME: check planets aren't overlapping
		// We're not checking invalid dimension id, so they can be pre-allocated (see MystCraft)

		// Ship
		SHIP_MAX_ENERGY_STORED = clamp(0, Integer.MAX_VALUE,
				config.get("ship", "max_energy_stored", SHIP_MAX_ENERGY_STORED, "Maximum energy storage").getInt());
		SHIP_NORMALJUMP_ENERGY_PER_BLOCK = clamp(0, Integer.MAX_VALUE,
				config.get("ship", "normaljump_energy_per_block", SHIP_NORMALJUMP_ENERGY_PER_BLOCK).getInt());
		SHIP_NORMALJUMP_ENERGY_PER_DISTANCE = clamp(0, Integer.MAX_VALUE,
				config.get("ship", "normaljump_energy_per_distance", SHIP_NORMALJUMP_ENERGY_PER_DISTANCE).getInt());
		SHIP_HYPERJUMP_ENERGY_PER_DISTANCE = clamp(0, Integer.MAX_VALUE,
				config.get("ship", "hyperjump_energy_per_distance", SHIP_HYPERJUMP_ENERGY_PER_DISTANCE).getInt());
		SHIP_HYPERJUMP_ENERGY_PER_BLOCK = clamp(0, Integer.MAX_VALUE,
				config.get("ship", "hyperjump_energy_per_block", SHIP_HYPERJUMP_ENERGY_PER_BLOCK).getInt());
		SHIP_TELEPORT_ENERGY_PER_ENTITY = clamp(0, Integer.MAX_VALUE,
				config.get("ship", "teleport_energy_per_entity", SHIP_TELEPORT_ENERGY_PER_ENTITY).getInt());

		SHIP_MAX_JUMP_DISTANCE = clamp(0, 30000000,
				config.get("ship", "max_jump_distance", SHIP_MAX_JUMP_DISTANCE, "Maximum jump lenght value in blocks").getInt());

		SHIP_VOLUME_MAX_ON_PLANET_SURFACE = clamp(0, 10000000,
				config.get("ship", "volume_max_on_planet_surface", SHIP_VOLUME_MAX_ON_PLANET_SURFACE,
						"Maximum ship mass (in blocks) to jump on earth").getInt());
		SHIP_VOLUME_MIN_FOR_HYPERSPACE = clamp(0, 10000000,
				config.get("ship", "volume_min_for_hyperspace", SHIP_VOLUME_MIN_FOR_HYPERSPACE,
						"Minimum ship mass (in blocks) to enter or exit hyperspace without a jumpgate").getInt());
		SHIP_VOLUME_UNLIMITED_PLAYERNAMES = config.get("ship", "volume_unlimited_playernames", SHIP_VOLUME_UNLIMITED_PLAYERNAMES,
				"List of player names which have unlimited block counts to their ship").getStringList();

		SHIP_MAX_SIDE_SIZE = clamp(0, 30000000,
				config.get("ship", "max_side_size", SHIP_MAX_SIDE_SIZE, "Maximum ship size on each axis in blocks").getInt());
		SHIP_COLLISION_TOLERANCE_BLOCKS = clamp(0, 30000000,
				config.get("ship", "collision_tolerance_blocks", SHIP_COLLISION_TOLERANCE_BLOCKS, "Tolerance in block in case of collision before causing damages...").getInt());
		SHIP_COOLDOWN_INTERVAL_SECONDS = clamp(0, 3600,
				config.get("ship", "cooldown_interval_seconds", SHIP_COOLDOWN_INTERVAL_SECONDS, "Cooldown seconds to wait after jumping").getInt());

		SHIP_SHORTJUMP_THRESHOLD_BLOCKS = clamp(0, 30000000,
				config.get("ship", "shortjump_threhold_blocs", SHIP_SHORTJUMP_THRESHOLD_BLOCKS, "Short jump definition").getInt());
		SHIP_SHORTJUMP_WARMUP_SECONDS = clamp(0, 3600,
				config.get("ship", "shortjump_warmup_seconds", SHIP_SHORTJUMP_WARMUP_SECONDS, "(measured in seconds)").getInt());
		SHIP_LONGJUMP_WARMUP_SECONDS = clamp(0, 3600,
				config.get("ship", "longjump_warmup_seconds", SHIP_LONGJUMP_WARMUP_SECONDS, "(measured in seconds)").getInt());
		SHIP_WARMUP_RANDOM_TICKS = clamp(10, 200,
				config.get("ship", "warmp_random_ticks", SHIP_WARMUP_RANDOM_TICKS).getInt());
		SHIP_WARMUP_SICKNESS = config.get("ship", "warmup_sickness", true, "Enable warp sickness during warmup").getBoolean(true);

		SHIP_CORE_REGISTRY_UPDATE_INTERVAL_SECONDS = clamp(0, 300,
				config.get("ship", "core_registry_update_interval", SHIP_CORE_REGISTRY_UPDATE_INTERVAL_SECONDS, "(measured in seconds)").getInt());
		SHIP_CORE_ISOLATION_UPDATE_INTERVAL_SECONDS = clamp(0, 300,
				config.get("ship", "core_isolation_update_interval", SHIP_CORE_ISOLATION_UPDATE_INTERVAL_SECONDS, "(measured in seconds)").getInt());
		SHIP_CONTROLLER_UPDATE_INTERVAL_SECONDS = clamp(0, 300,
				config.get("ship", "controller_update_interval", SHIP_CONTROLLER_UPDATE_INTERVAL_SECONDS, "(measured in seconds)").getInt());

		// Radar
		RADAR_MAX_ENERGY_STORED = clamp(0, Integer.MAX_VALUE,
				config.get("radar", "max_energy_stored", RADAR_MAX_ENERGY_STORED).getInt());
		RADAR_MAX_ISOLATION_RANGE = clamp(2, 8,
				config.get("radar", "max_isolation_range", RADAR_MAX_ISOLATION_RANGE, "radius around core where isolation blocks count (2 to 8), higher is lagger").getInt());

		RADAR_MIN_ISOLATION_BLOCKS = clamp(0, 20,
				config.get("radar", "min_isolation_blocks", RADAR_MIN_ISOLATION_BLOCKS, "number of isolation blocks required to get some isolation (0 to 20)").getInt());
		RADAR_MAX_ISOLATION_BLOCKS = clamp(5, 100,
				config.get("radar", "max_isolation_blocks", RADAR_MAX_ISOLATION_BLOCKS, "number of isolation blocks required to reach maximum effect (5 to 100)").getInt());

		RADAR_MIN_ISOLATION_EFFECT = clamp(0.01D, 0.95D,
				config.get("radar", "min_isolation_effect", RADAR_MIN_ISOLATION_EFFECT, "isolation effect achieved with min number of isolation blocks (0.01 to 0.95)").getDouble(0.12D));
		RADAR_MAX_ISOLATION_EFFECT = clamp(0.01D, 1.0D,
				config.get("radar", "max_isolation_effect", RADAR_MAX_ISOLATION_EFFECT, "isolation effect achieved with max number of isolation blocks (0.01 to 1.00)").getDouble(1.00D));

		// Ship Scanner
		SS_MAX_ENERGY_STORED = clamp(1, Integer.MAX_VALUE,
				config.get("ship_scanner", "max_energy_stored", SS_MAX_ENERGY_STORED, "Maximum energy storage").getInt());

		SS_EU_PER_BLOCK_SCAN = config.get("ship_scanner", "energy_per_block_when_scanning", SS_EU_PER_BLOCK_SCAN,
				"Energy consummed per block when scanning a ship (use -1 to consume everything)").getInt();
		if (SS_EU_PER_BLOCK_SCAN != -1) {
			SS_EU_PER_BLOCK_SCAN = clamp(1, SS_MAX_ENERGY_STORED, SS_EU_PER_BLOCK_SCAN);
		}

		SS_EU_PER_BLOCK_DEPLOY = config.get("ship_scanner", "energy_per_block_when_deploying", SS_EU_PER_BLOCK_DEPLOY,
				"Energy consummed per block when deploying a ship (use -1 to consume everything)").getInt();
		if (SS_EU_PER_BLOCK_DEPLOY != -1) {
			SS_EU_PER_BLOCK_DEPLOY = clamp(1, SS_MAX_ENERGY_STORED, SS_EU_PER_BLOCK_DEPLOY);
		}

		SS_MAX_DEPLOY_RADIUS_BLOCKS = clamp(5, 150,
				config.get("ship_scanner", "max_deploy_radius_blocks", SS_MAX_DEPLOY_RADIUS_BLOCKS, "Max distance from ship scanner to ship core, measured in blocks (5-150)").getInt());

		// Laser medium
		LASER_MEDIUM_MAX_ENERGY_STORED = clamp(1, Integer.MAX_VALUE,
				config.get("laser_medium", "max_energy_stored", LASER_MEDIUM_MAX_ENERGY_STORED).getInt());

		// Laser cannon
		LASER_CANNON_MAX_MEDIUMS_COUNT = clamp(1, 64,
				config.get("laser_cannon", "max_mediums_count", LASER_CANNON_MAX_MEDIUMS_COUNT).getInt());
		LASER_CANNON_MAX_LASER_ENERGY = clamp(1, Integer.MAX_VALUE,
				config.get("laser_cannon", "max_laser_energy", LASER_CANNON_MAX_LASER_ENERGY, "Maximum energy in beam after accounting for boosters beams").getInt());
		LASER_CANNON_EMIT_FIRE_DELAY_TICKS = clamp(1, 100,
				config.get("laser_cannon", "emit_fire_delay_ticks", LASER_CANNON_EMIT_FIRE_DELAY_TICKS, "Delay while booster beams are accepted, before actually shooting").getInt());
		LASER_CANNON_EMIT_SCAN_DELAY_TICKS = clamp(1, 100,
				config.get("laser_cannon", "emit_scan_delay_ticks", LASER_CANNON_EMIT_SCAN_DELAY_TICKS, "Delay while booster beams are accepted, before actually scanning").getInt());

		LASER_CANNON_BOOSTER_BEAM_ENERGY_EFFICIENCY = clamp(0.01D, 10.0D,
				config.get("laser_cannon", "booster_beam_energy_efficiency", LASER_CANNON_BOOSTER_BEAM_ENERGY_EFFICIENCY).getDouble(0.6D));
		LASER_CANNON_ENERGY_LOSS_PER_BLOCK = clamp(1, LASER_CANNON_MAX_LASER_ENERGY / 10,
				config.get("laser_cannon", "energy_loss_per_block", LASER_CANNON_ENERGY_LOSS_PER_BLOCK).getInt());

		LASER_CANNON_ENTITY_HIT_SET_ON_FIRE_SECONDS = clamp(0, 300,
				config.get("laser_cannon", "entity_hit_set_on_fire_seconds", LASER_CANNON_ENTITY_HIT_SET_ON_FIRE_SECONDS).getInt());
		LASER_CANNON_ENTITY_HIT_ENERGY_PER_DAMAGE = clamp(0, LASER_CANNON_MAX_LASER_ENERGY,
				config.get("laser_cannon", "entity_hit_energy_per_damage", LASER_CANNON_ENTITY_HIT_ENERGY_PER_DAMAGE).getInt());
		LASER_CANNON_ENTITY_HIT_MAX_DAMAGE = clamp(0, Integer.MAX_VALUE,
				config.get("laser_cannon", "entity_hit_max_damage", LASER_CANNON_ENTITY_HIT_MAX_DAMAGE).getInt());

		LASER_CANNON_ENTITY_HIT_ENERGY_THRESHOLD_FOR_EXPLOSION = clamp(0, Integer.MAX_VALUE,
				config.get("laser_cannon", "entity_hit_energy_threshold_for_explosion", LASER_CANNON_ENTITY_HIT_ENERGY_THRESHOLD_FOR_EXPLOSION, "Minimum energy to cause explosion effect").getInt());
		LASER_CANNON_ENTITY_HIT_EXPLOSION_BASE_STRENGTH = (float) clamp(0.0D, 100.0D,
				config.get("laser_cannon", "entity_hit_explosion_base_strength", LASER_CANNON_ENTITY_HIT_EXPLOSION_BASE_STRENGTH).getDouble());
		LASER_CANNON_ENTITY_HIT_EXPLOSION_ENERGY_PER_STRENGTH = clamp(1, Integer.MAX_VALUE,
				config.get("laser_cannon", "entity_hit_explosion_energy_per_strength", LASER_CANNON_ENTITY_HIT_EXPLOSION_ENERGY_PER_STRENGTH, "Energy per added explosion strength").getInt());
		LASER_CANNON_ENTITY_HIT_EXPLOSION_MAX_STRENGTH = (float) clamp(0.0D, 1000.0D,
				config.get("laser_cannon", "entity_hit_explosion_max_strength", LASER_CANNON_ENTITY_HIT_EXPLOSION_MAX_STRENGTH, "Maximum explosion strength, set to 0 to disable explosion completly").getDouble());

		LASER_CANNON_BLOCK_HIT_ENERGY = clamp(0, Integer.MAX_VALUE,
				config.get("laser_cannon", "block_hit_energy", LASER_CANNON_BLOCK_HIT_ENERGY, "Base energy consummed from hitting a block").getInt());
		LASER_CANNON_BLOCK_HIT_ENERGY_PER_BLOCK_RESISTANCE = clamp(0, Integer.MAX_VALUE,
				config.get("laser_cannon", "block_hit_energy_per_block_resistance", LASER_CANNON_BLOCK_HIT_ENERGY_PER_BLOCK_RESISTANCE, "Energy consummed per explosive resistance points").getInt());
		LASER_CANNON_BLOCK_HIT_ENERGY_PER_DISTANCE = clamp(0, Integer.MAX_VALUE,
				config.get("laser_cannon", "block_hit_energy_per_distance", LASER_CANNON_BLOCK_HIT_ENERGY_PER_DISTANCE, "Energy consummed per distance travelled").getInt());

		LASER_CANNON_BLOCK_HIT_EXPLOSION_RESISTANCE_THRESHOLD = clamp(0.0D, 1000000.0D,
				config.get("laser_cannon", "block_hit_explosion_resistance_threshold", LASER_CANNON_BLOCK_HIT_EXPLOSION_RESISTANCE_THRESHOLD, "Block explosion resistance threshold to cause an explosion").getDouble());
		LASER_CANNON_BLOCK_HIT_EXPLOSION_BASE_STRENGTH = (float) clamp(0.0D, 1000.0D,
				config.get("laser_cannon", "block_hit_explosion_base_strength", LASER_CANNON_BLOCK_HIT_EXPLOSION_BASE_STRENGTH, "Explosion base strength, 4 is Vanilla TNT").getDouble());
		LASER_CANNON_BLOCK_HIT_EXPLOSION_ENERGY_PER_STRENGTH = clamp(1, Integer.MAX_VALUE,
				config.get("laser_cannon", "block_hit_explosion_energy_per_strength", LASER_CANNON_BLOCK_HIT_EXPLOSION_ENERGY_PER_STRENGTH, "Energy per added explosion strength").getInt());
		LASER_CANNON_BLOCK_HIT_EXPLOSION_MAX_STRENGTH = (float) clamp(0.0D, 1000.0D,
				config.get("laser_cannon", "block_hit_explosion_max_strength", LASER_CANNON_BLOCK_HIT_EXPLOSION_MAX_STRENGTH, "Maximum explosion strength, set to 0 to disable explosion completly").getDouble());

		// Mining Laser
		MINING_LASER_MAX_MEDIUMS_COUNT = clamp(1, 64,
				config.get("mining_laser", "max_mediums_count", MINING_LASER_MAX_MEDIUMS_COUNT).getInt());
		MINING_LASER_RADIUS_BLOCKS = clamp(1, 64,
				config.get("mining_laser", "radius_blocks", MINING_LASER_RADIUS_BLOCKS).getInt());

		MINING_LASER_WARMUP_DELAY_TICKS = clamp(1, 300,
				config.get("mining_laser", "warmup_delay_ticks", MINING_LASER_WARMUP_DELAY_TICKS).getInt());
		MINING_LASER_SCAN_DELAY_TICKS = clamp(1, 300,
				config.get("mining_laser", "scan_delay_ticks", MINING_LASER_SCAN_DELAY_TICKS).getInt());
		MINING_LASER_MINE_DELAY_TICKS = clamp(1, 300,
				config.get("mining_laser", "mine_delay_ticks", MINING_LASER_MINE_DELAY_TICKS).getInt());

		MINING_LASER_PLANET_ENERGY_PER_LAYER = clamp(1, Integer.MAX_VALUE,
				config.get("mining_laser", "planet_energy_per_layer", MINING_LASER_PLANET_ENERGY_PER_LAYER).getInt());
		MINING_LASER_PLANET_ENERGY_PER_BLOCK = clamp(1, Integer.MAX_VALUE,
				config.get("mining_laser", "planet_energy_per_block", MINING_LASER_PLANET_ENERGY_PER_BLOCK).getInt());
		MINING_LASER_SPACE_ENERGY_PER_LAYER = clamp(1, Integer.MAX_VALUE,
				config.get("mining_laser", "space_energy_per_layer", MINING_LASER_SPACE_ENERGY_PER_LAYER).getInt());
		MINING_LASER_SPACE_ENERGY_PER_BLOCK = clamp(1, Integer.MAX_VALUE,
				config.get("mining_laser", "space_energy_per_block", MINING_LASER_SPACE_ENERGY_PER_BLOCK).getInt());

		MINING_LASER_ORESONLY_ENERGY_FACTOR = clamp(0.01D, 1000.0D,
				config.get("mining_laser", "oresonly_energy_factor", MINING_LASER_ORESONLY_ENERGY_FACTOR).getDouble(4.0D));
		MINING_LASER_SILKTOUCH_ENERGY_FACTOR = clamp(0.01D, 1000.0D,
				config.get("mining_laser", "silktouch_energy_factor", MINING_LASER_SILKTOUCH_ENERGY_FACTOR).getDouble(2.5D));
		MINING_LASER_SILKTOUCH_DEUTERIUM_L = clamp(0.001D, 10.0D,
				config.get("mining_laser", "silktouch_deuterium_l", MINING_LASER_SILKTOUCH_DEUTERIUM_L).getDouble(1.0D));
		MINING_LASER_FORTUNE_ENERGY_FACTOR = clamp(0.01D, 1000.0D,
				config.get("mining_laser", "fortune_energy_factor", MINING_LASER_FORTUNE_ENERGY_FACTOR).getDouble(2.5D));

		// Tree Farm
		TREE_FARM_MIN_RADIUS = clamp(1, 30,
				config.get("tree_farm", "min_radius", TREE_FARM_MIN_RADIUS, "Minimum radius on X and Z axis, measured in blocks").getInt());
		TREE_FARM_MAX_RADIUS = clamp(TREE_FARM_MIN_RADIUS, 30,
				config.get("tree_farm", "max_radius", TREE_FARM_MAX_RADIUS, "Maximum radius on X and Z axis, measured in blocks").getInt());

		// Cloaking
		CLOAKING_MAX_ENERGY_STORED = clamp(1, Integer.MAX_VALUE,
				config.get("cloaking", "max_energy_stored", CLOAKING_MAX_ENERGY_STORED, "Maximum energy storage").getInt());
		CLOAKING_COIL_CAPTURE_BLOCKS = clamp(0, 30,
				config.get("cloaking", "coil_capture_blocks", CLOAKING_COIL_CAPTURE_BLOCKS, "Extra blocks covered after the outer coils").getInt());
		CLOAKING_MAX_FIELD_RADIUS = clamp(CLOAKING_COIL_CAPTURE_BLOCKS + 3, 128,
				config.get("cloaking", "max_field_radius", CLOAKING_MAX_FIELD_RADIUS).getInt());
		CLOAKING_TIER1_ENERGY_PER_BLOCK = clamp(0, Integer.MAX_VALUE,
				config.get("cloaking", "tier1_energy_per_block", CLOAKING_TIER1_ENERGY_PER_BLOCK).getInt());
		CLOAKING_TIER2_ENERGY_PER_BLOCK = clamp(CLOAKING_TIER1_ENERGY_PER_BLOCK, Integer.MAX_VALUE,
				config.get("cloaking", "tier2_energy_per_block", CLOAKING_TIER2_ENERGY_PER_BLOCK).getInt());
		CLOAKING_FIELD_REFRESH_INTERVAL_SECONDS = clamp(1, 30,
				config.get("cloaking", "field_refresh_interval_seconds", CLOAKING_FIELD_REFRESH_INTERVAL_SECONDS).getInt());

		// Air generator
		AIRGEN_MAX_ENERGY_STORED = clamp(1, Integer.MAX_VALUE,
				config.get("air_generator", "max_energy_stored", AIRGEN_MAX_ENERGY_STORED).getInt());
		AIRGEN_ENERGY_PER_CANISTER = clamp(1, AIRGEN_MAX_ENERGY_STORED,
				config.get("air_generator", "energy_per_canister", AIRGEN_ENERGY_PER_CANISTER).getInt());
		AIRGEN_ENERGY_PER_NEWAIRBLOCK = clamp(1, AIRGEN_MAX_ENERGY_STORED,
				config.get("air_generator", "energy_per_new_air_block", AIRGEN_ENERGY_PER_NEWAIRBLOCK).getInt());
		AIRGEN_ENERGY_PER_EXISTINGAIRBLOCK = clamp(1, AIRGEN_MAX_ENERGY_STORED,
				config.get("air_generator", "eu_per_existing_air_block", AIRGEN_ENERGY_PER_EXISTINGAIRBLOCK).getInt());
		AIRGEN_AIR_GENERATION_TICKS = clamp(1, 300,
				config.get("air_generator", "air_generation_ticks", AIRGEN_AIR_GENERATION_TICKS).getInt());

		// Reactor monitor
		IC2_REACTOR_MAX_ENERGY_STORED = clamp(1, Integer.MAX_VALUE,
				config.get("ic2_reactor_laser", "max_energy_stored", IC2_REACTOR_MAX_ENERGY_STORED).getInt());
		IC2_REACTOR_ENERGY_PER_HEAT = clamp(2.0D, 100000.0D,
				config.get("ic2_reactor_laser", "energy_per_heat", IC2_REACTOR_ENERGY_PER_HEAT).getDouble(2));
		IC2_REACTOR_COOLING_INTERVAL_TICKS = clamp(0, 1200,
				config.get("ic2_reactor_laser", "cooling_interval_ticks", IC2_REACTOR_COOLING_INTERVAL_TICKS).getInt());

		// Transporter
		TRANSPORTER_MAX_ENERGY = clamp(1, Integer.MAX_VALUE,
				config.get("transporter", "max_energy", TRANSPORTER_MAX_ENERGY).getInt());
		TRANSPORTER_USE_RELATIVE_COORDS = config.get("transporter", "use_relative_coords", TRANSPORTER_USE_RELATIVE_COORDS).getBoolean(true);
		TRANSPORTER_ENERGY_PER_BLOCK = clamp(1.0D, TRANSPORTER_MAX_ENERGY / 10.0D,
				config.get("transporter", "energy_per_block", TRANSPORTER_ENERGY_PER_BLOCK).getDouble(100.0D));
		TRANSPORTER_MAX_BOOST_MUL = clamp(1.0D, 1000.0D,
				config.get("transporter", "max_boost", TRANSPORTER_MAX_BOOST_MUL).getDouble(4.0));

		// Enantiomorphic reactor
		ENAN_REACTOR_MAX_ENERGY_STORED = clamp(1, Integer.MAX_VALUE,
				config.get("enantiomorphic_reactor", "max_energy_stored", ENAN_REACTOR_MAX_ENERGY_STORED).getInt());
		ENAN_REACTOR_UPDATE_INTERVAL_TICKS = clamp(1, 300,
				config.get("enantiomorphic_reactor", "update_interval_ticks", ENAN_REACTOR_UPDATE_INTERVAL_TICKS).getInt());
		ENAN_REACTOR_MAX_LASERS_PER_SECOND = clamp(4, 80,
				config.get("enantiomorphic_reactor", "max_lasers", ENAN_REACTOR_MAX_LASERS_PER_SECOND, "Maximum number of stabiliation laser shots per seconds before loosing effiency").getInt());

		// Energy bank
		ENERGY_BANK_MAX_ENERGY_STORED = config.get("energy_bank", "max_energy_stored", ENERGY_BANK_MAX_ENERGY_STORED).getInt();

		// Lift
		LIFT_MAX_ENERGY_STORED = clamp(1, Integer.MAX_VALUE,
				config.get("lift", "max_energy_stored", LIFT_MAX_ENERGY_STORED).getInt());
		LIFT_ENERGY_PER_ENTITY = clamp(1, Integer.MAX_VALUE,
				config.get("lift", "energy_per_entity", LIFT_ENERGY_PER_ENTITY, "Energy consummed per entity moved").getInt());
		LIFT_UPDATE_INTERVAL_TICKS = clamp(1, 60,
				config.get("lift", "update_interval_ticks", LIFT_UPDATE_INTERVAL_TICKS).getInt());

		config.save();
	}

	public static int clamp(final int min, final int max, final int value) {
		return Math.min(max, Math.max(value, min));
	}

	public static double clamp(final double min, final double max, final double value) {
		return Math.min(max, Math.max(value, min));
	}

	public static void load() {
		commonWorldGenOres = new ArrayList<Block>();
		commonWorldGenOres.add(Blocks.iron_ore);
		commonWorldGenOres.add(Blocks.gold_ore);
		commonWorldGenOres.add(Blocks.coal_ore);
		commonWorldGenOres.add(Blocks.emerald_ore);
		commonWorldGenOres.add(Blocks.lapis_ore);
		commonWorldGenOres.add(Blocks.redstone_ore);

		forceFieldBlocks = new ArrayList<Block>();

		spaceHelmets = new ArrayList<Item>();
		jetpacks = new ArrayList<Item>();
		minerOres = new ArrayList<Block>();
		minerLogs = new ArrayList<Block>();
		minerLeaves = new ArrayList<Block>();
		scannerIgnoreBlocks = new ArrayList<Block>();

		isForgeMultipartLoaded = Loader.isModLoaded("ForgeMultipart");
		if (isForgeMultipartLoaded) {
			loadForgeMultipart();
		}

		isIndustrialCraft2loaded = Loader.isModLoaded("IC2");
		if (isIndustrialCraft2loaded) {
			loadIC2();
		}

		isComputerCraftLoaded = Loader.isModLoaded("ComputerCraft");
		if (isComputerCraftLoaded) {
			loadCC();
		}

		isAdvancedSolarPanelLoaded = Loader.isModLoaded("AdvancedSolarPanel");
		if (isAdvancedSolarPanelLoaded) {
			loadASP();
		}

		isAtomicScienceLoaded = Loader.isModLoaded("ResonantInduction|Atomic");
		if (isAtomicScienceLoaded) {
			loadAtomicScience();
		}

		isMFFSLoaded = Loader.isModLoaded("MFFS");
		if (isMFFSLoaded) {
			loadMFFS();
		}

		isGraviSuiteLoaded = Loader.isModLoaded("GraviSuite");
		if (isGraviSuiteLoaded) {
			loadGraviSuite();
		}

		isNetherOresLoaded = Loader.isModLoaded("NetherOres");

		isThermalExpansionLoaded = Loader.isModLoaded("ThermalExpansion");
		if (isThermalExpansionLoaded) {
			loadThermalExpansion();
		}

		isAdvancedRepulsionSystemsLoaded = Loader.isModLoaded("AdvancedRepulsionSystems");
		if (isAdvancedRepulsionSystemsLoaded) {
			loadAdvancedRepulsionSystems();
		}

		isMagicalCropsLoaded = Loader.isModLoaded("MagicalCrops");
		isAppliedEnergistics2Loaded = Loader.isModLoaded("appliedenergistics2");
		isOpenComputersLoaded = Loader.isModLoaded("OpenComputers");

		//
		minerOres.add(WarpDrive.blockIridium);
		minerOres.add(Blocks.coal_ore);
		minerOres.add(Blocks.quartz_ore);
		minerOres.add(Blocks.obsidian);
		minerOres.add(Blocks.web);
		minerOres.add(Blocks.fence);
		minerOres.add(Blocks.torch);
		minerOres.add(Blocks.glowstone);
		minerOres.add(Blocks.redstone_block);

		// Ignore WarpDrive blocks (which potentially will be duplicated by
		// cheaters using ship scan/deploy)
		scannerIgnoreBlocks.add(WarpDrive.blockShipCore);
		scannerIgnoreBlocks.add(WarpDrive.blockShipController);
		scannerIgnoreBlocks.add(WarpDrive.blockIridium);

		if (isIndustrialCraft2loaded) {
			// Metadata: 0 Batbox, 1 MFE, 2 MFSU, 3 LV transformer, 4 MV transformer, 5 HV transformer, 6 EV transformer, 7 CESU
			scannerIgnoreBlocks.add(Block.getBlockFromName("IC2:blockElectric"));

			// Metadata: 0 Batbox, 1 CESU, 2 MFE, 3 MFSU
			scannerIgnoreBlocks.add(Block.getBlockFromName("IC2:blockChargepad"));
		}
		if (isICBMLoaded) {
			scannerIgnoreBlocks.add(Block.getBlockFromName("ICBM:explosive"));
		}
		if (isComputerCraftLoaded) {
			scannerIgnoreBlocks.add(CC_Computer);
			scannerIgnoreBlocks.add(CCT_Turtle);
			scannerIgnoreBlocks.add(CCT_Expanded);
			scannerIgnoreBlocks.add(CCT_Advanced);
		}
		// Do not deploy ores and valuables
		for (Block t : commonWorldGenOres) {
			scannerIgnoreBlocks.add(t);
		}
	}

	public static void postInit() {
		// read XML files
		File[] files = configDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file_notUsed, String name) {
				return name.endsWith(".xml");
			}
		});
		if (files.length == 0) {
			for(String defaultXMLfilename : defaultXMLfilenames) {
				unpackResourceToFolder(defaultXMLfilename, "config", configDirectory);
			}
		}

		FillerManager.loadOres(configDirectory);
		StructureManager.loadStructures(configDirectory);

		LoadOreDict();

		FillerManager.finishLoading();
	}

	private static void LoadOreDict() {
		String[] oreNames = OreDictionary.getOreNames();
		for (String oreName : oreNames) {
			String lowerOreName = oreName.toLowerCase();
			if (oreName.substring(0, 3).equals("ore")) {
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for (ItemStack i : item) {
					minerOres.add(Block.getBlockFromItem(i.getItem()));
					WarpDrive.logger.info("Added ore ID: " + i);
				}
			}
			if (lowerOreName.contains("log")) {
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for (ItemStack i : item) {
					minerLogs.add(Block.getBlockFromItem(i.getItem()));
					WarpDrive.logger.info("Added log ID: " + i);
				}
			}
			if (lowerOreName.contains("leave") || lowerOreName.contains("leaf")) {
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for (ItemStack i : item) {
					minerLeaves.add(Block.getBlockFromItem(i.getItem()));
					WarpDrive.logger.info("Added leaf ID: " + i);
				}
			}
		}
	}

	private static void loadForgeMultipart() {
		try {
			Class forgeMultipart_helper = Class.forName("codechicken.multipart.MultipartHelper");
			forgeMultipart_helper_createTileFromNBT = forgeMultipart_helper.getDeclaredMethod("createTileFromNBT", World.class, NBTTagCompound.class);
			forgeMultipart_helper_sendDescPacket = forgeMultipart_helper.getDeclaredMethod("sendDescPacket", World.class, TileEntity.class);
			Class forgeMultipart_tileMultipart = Class.forName("codechicken.multipart.TileMultipart");
			forgeMultipart_tileMultipart_onChunkLoad = forgeMultipart_tileMultipart.getDeclaredMethod("onChunkLoad");
		} catch (Exception exception) {
			isForgeMultipartLoaded = false;
			WarpDrive.logger.error("Error loading ForgeMultipart classes");
			exception.printStackTrace();
		}
	}

	private static void loadIC2() {
		try {
			IC2_solarPanel = getModItemStack("IC2", "blockGenerator", 3);

			spaceHelmets.add(getModItemStack("IC2", "itemArmorHazmatHelmet", -1).getItem());
			spaceHelmets.add(getModItemStack("IC2", "itemSolarHelmet", -1).getItem());
			spaceHelmets.add(getModItemStack("IC2", "itemArmorNanoHelmet", -1).getItem());
			spaceHelmets.add(getModItemStack("IC2", "itemArmorQuantumHelmet", -1).getItem());

			jetpacks.add(getModItemStack("IC2", "itemArmorJetpack", -1).getItem());
			jetpacks.add(getModItemStack("IC2", "itemArmorJetpackElectric", -1).getItem());

			IC2_empty = getModItemStack("IC2", "itemCellEmpty", -1);
			IC2_air = getModItemStack("IC2", "itemCellEmpty", 5);

			ItemStack rubberWood = getModItemStack("IC2", "blockRubWood", -1);
			IC2_Resin = getModItemStack("IC2", "itemHarz", -1);
			if (rubberWood != null) {
				IC2_rubberWood = rubberWood;
				minerOres.add(Block.getBlockFromItem(getModItemStack("IC2", "rubberWood", -1).getItem()));
			}
			ItemStack ore = getModItemStack("IC2", "blockOreUran", -1);
			if (ore != null) {
				commonWorldGenOres.add(Block.getBlockFromItem(ore.getItem()));
			}
			ore = getModItemStack("IC2", "blockOreCopper", -1);
			if (ore != null) {
				commonWorldGenOres.add(Block.getBlockFromItem(ore.getItem()));
			}
			ore = getModItemStack("IC2", "blockOreTin", -1);
			if (ore != null) {
				commonWorldGenOres.add(Block.getBlockFromItem(ore.getItem()));
			}
			ore = getModItemStack("IC2", "blockOreLead", -1);
			if (ore != null) {
				commonWorldGenOres.add(Block.getBlockFromItem(ore.getItem()));
			}

			IC2_fluidCell = getModItemStack("IC2", "itemFluidCell", -1).getItem();
		} catch (Exception exception) {
			WarpDrive.logger.error("Error loading IndustrialCraft2 classes");
			exception.printStackTrace();
		}
	}

	private static void loadCC() {
		try {
			CC_Computer = getModBlock("ComputerCraft", "CC-Computer");
			CC_peripheral = getModBlock("ComputerCraft", "CC-Peripheral");
			CCT_Turtle = getModBlock("ComputerCraft", "CC-Turtle");
			CCT_Expanded = getModBlock("ComputerCraft", "CC-TurtleExpanded");
			CCT_Advanced = getModBlock("ComputerCraft", "CC-TurtleAdvanced");
		} catch (Exception exception) {
			WarpDrive.logger.error("Error loading ComputerCraft classes");
			exception.printStackTrace();
		}
	}

	private static void loadASP() {
		try {
			spaceHelmets.add((Item) Item.itemRegistry.getObject("AdvancedSolarPanel:advanced_solar_helmet"));
			spaceHelmets.add((Item) Item.itemRegistry.getObject("AdvancedSolarPanel:hybrid_solar_helmet"));
			spaceHelmets.add((Item) Item.itemRegistry.getObject("AdvancedSolarPanel:ultimate_solar_helmet"));
		} catch (Exception exception) {
			WarpDrive.logger.error("Error loading ASP classes");
			exception.printStackTrace();
			isAdvancedSolarPanelLoaded = false;
		}
	}

	private static void loadAtomicScience() {
		try {
			/* TODO: Does not exist for 1.7
			Class<?> z = Class.forName("resonantinduction.atomic.Atomic");
			commonWorldGenOres.add(((Block) z.getField("blockUraniumOre").get(null)), 0 });
			AS_Turbine = ((Block) z.getField("blockElectricTurbine").get(null));
			AS_deuteriumCell = ((Item) z.getField("itemDeuteriumCell").get(null));
			 */
			isAtomicScienceLoaded = false;
		} catch (Exception exception) {
			WarpDrive.logger.error("Error loading AS classes");
			exception.printStackTrace();
			isAtomicScienceLoaded = false;
		}
	}

	private static void loadICBM() {
		try {
			/* TODO: Does not exist yet for 1.7
			Class<?> z = Class.forName("icbm.core.ICBMCore");
			commonWorldGenOres.add(((Block) z.getField("blockSulfurOre").get(null)), 0 });
			z = Class.forName("icbm.explosion.ICBMExplosion");
			ICBM_Machine = ((Block) z.getField("blockMachine").get(null));
			ICBM_Missile = ((Item) z.getField("itemMissile").get(null));
			ICBM_Explosive = ((Block) z.getField("blockExplosive").get(null));
			 */
			isICBMLoaded = false;
		} catch (Exception exception) {
			WarpDrive.logger.error("Error loading ICBM classes");
			exception.printStackTrace();
			isICBMLoaded = false;
		}
	}

	private static void loadMFFS() {
		try {
			forceFieldBlocks.add(Block.getBlockFromName("MFFS:FIXME_field"));	// FIXME
			isMFFSLoaded = false;
		} catch (Exception exception) {
			WarpDrive.logger.error("Error loading MFFS classes");
			exception.printStackTrace();
			isMFFSLoaded = false;
		}
	}

	private static void loadGraviSuite() {
		try {
			spaceHelmets.add((Item) Item.itemRegistry.getObject("GraviSuite.ultimateSolarHelmet")); // FIXME
			jetpacks.add((Item) Item.itemRegistry.getObject("GraviSuite.advJetpack")); // FIXME
			jetpacks.add((Item) Item.itemRegistry.getObject("GraviSuite.graviChestPlate")); // FIXME
			GS_ultimateLappack = (Item) Item.itemRegistry.getObject("GraviSuite.ultimateLappack"); // FIXME
		} catch (Exception exception) {
			WarpDrive.logger.error("Error loading GS classes");
			exception.printStackTrace();
			isGraviSuiteLoaded = false;
		}
	}

	private static void loadThermalExpansion() {
		try {
			// TEEnergyCell =
			// Class.forName("thermalexpansion.block.energycell.BlockEnergyCell");
			// TEFluids = Class.forName("thermalexpansion.fluid.TEFluids");
		} catch (Exception exception) {
			WarpDrive.logger.error("Error loading ThermalExpansion classes");
			exception.printStackTrace();
			isThermalExpansionLoaded = false;
		}
	}

	private static void loadAdvancedRepulsionSystems() {
		try {

			forceFieldBlocks.add(Block.getBlockFromName("AdvancedRepulsionSystems:field"));
		} catch (Exception exception) {
			WarpDrive.logger.error("Error loading AdvancedRepulsionSystems classes");
			exception.printStackTrace();
			isAdvancedRepulsionSystemsLoaded = false;
		}
	}

	public static DocumentBuilder getXmlDocumentBuilder() {
		if (xmlDocumentBuilder == null) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(false);
			dbf.setValidating(true);
			try {
				xmlDocumentBuilder = dbf.newDocumentBuilder();
			} catch (ParserConfigurationException exception) {
				exception.printStackTrace();
			}
		}

		return xmlDocumentBuilder;
	}

	/*
	 * Copy a default configuration file from the mod's resources to the specified configuration folder
	 */
	public static void unpackResourceToFolder(final String filename, final String sourceResourcePath, File targetFolder) {
		// targetFolder is already created by caller

		String resourceName = sourceResourcePath + "/" + filename;

		File destination = new File(targetFolder, filename);

		try {
			InputStream inputStream = WarpDrive.class.getClassLoader().getResourceAsStream(resourceName);
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destination));

			byte[] byteBuffer = new byte[Math.max(8192, inputStream.available())];
			int bytesRead;
			while ((bytesRead = inputStream.read(byteBuffer)) >= 0) {
				outputStream.write(byteBuffer, 0, bytesRead);
			}

			inputStream.close();
			outputStream.close();
		} catch (Exception exception) {
			WarpDrive.logger.error("Failed to unpack resource \'" + resourceName + "\' into " + destination);
			exception.printStackTrace();
		}
	}
}
