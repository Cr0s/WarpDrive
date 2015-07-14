package cr0s.warpdrive;

import gravisuite.GraviSuite;
import ic2.api.item.IC2Items;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import advsolar.common.AdvancedSolarPanel;
import mffs.Content;
import mffs.ModularForceFieldSystem;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.Loader;
import cr0s.warpdrive.data.TransitionPlane;
import dan200.computercraft.ComputerCraft;

public class WarpDriveConfig
{
	private static Configuration config;
	// Blocks (no longer neccessary, TODO: delete once compiling without)
	/*
	public static int coreID;
	public static int controllerID;
	public static int radarID;
	public static int isolationID;
	public static int airID;
	public static int airgenID;
	public static int gasID;
	public static int laserID;
	public static int miningLaserID;
	public static int particleBoosterID;
	public static int liftID;
	public static int laserCamID;
	public static int camID;
	public static int monitorID;
	public static int iridiumBlockID;
	public static int shipScannerID;
	public static int cloakCoreID;
	public static int cloakCoilID;
	
	// Items
	public static int componentID;
	public static int helmetID;
	public static int chestID;
	public static int pantsID;
	public static int bootsID;
	public static int airCanisterID;
	public static int upgradeID;
	*/
	
	/*
	 * The variables which store whether or not individual mods are loaded
	 */
	public static boolean isForgeMultipartLoaded			= false;
	public static boolean isAppliedEnergisticsLoaded		= false;
	public static boolean isAdvSolPanelLoaded				= false;
	public static boolean isAtomicScienceLoaded				= false;
	public static boolean isAEExtraLoaded					= false;
	public static boolean isICBMLoaded						= false;
	public static boolean isMFFSLoaded						= false;
	public static boolean isGraviSuiteLoaded				= false;
	public static boolean isICLoaded						= false;
	public static boolean isCCLoaded						= false;
	public static boolean isUndergroundBiomesLoaded			= false;
	public static boolean isNetherOresLoaded				= false;
	public static boolean isThermalExpansionLoaded			= false;
	public static boolean isMetallurgyLoaded				= false;
	public static boolean isAdvancedRepulsionSystemsLoaded	= false;

	// ForgeMultipart (microblocks) support
	public static Method forgeMultipart_helper_createTileFromNBT = null;
	public static Method forgeMultipart_helper_sendDescPacket = null;
	public static Method forgeMultipart_tileMultipart_onChunkLoad = null;

	public static ItemStack IC2_Air;
	public static ItemStack IC2_Empty;
	public static Item IC2_RubberWood;
	public static Item IC2_Resin;
	public static Item IC2_fluidCell;
	public static Block CC_Computer, CC_peripheral, CC_Floppy, CCT_Turtle, CCT_Upgraded, CCT_Advanced;
	public static Block GT_Ores, GT_Granite, GT_Machine;
	public static Item ASP;
	public static int AS_Turbine = 0, AS_deuteriumCell = 0;
	public static int ICBM_Machine = 0, ICBM_Missile = 0, ICBM_Explosive = 0;
	public static Item GS_ultimateLappack;
	public static Block UB_igneousStone, UB_metamorphicStone, UB_metamorphicCobblestone, UB_sedimentaryStone, UB_igneousCobblestone;
	public static int NetherOres_count;
	public static int[] NetherOres_block;
	public static int[][] Metallurgy_overworldOresBlock;
	public static int[][] Metallurgy_netherOresBlock;
	public static int[][] Metallurgy_endOresBlock;
	public static ArrayList<Block> forceFieldBlocks;

	public static ArrayList<Item> spaceHelmets, jetpacks, minerLogs, minerLeaves;
	public static ArrayList<Block> minerOres, scannerIgnoreBlocks;
	private static Class<?> AEBlocks;
	private static Class<?> AEMaterials;
	private static Class<?> AEItems;
	public static ArrayList<Block> commonWorldGenOres;
	public static Item AEExtra_fluidDrive;
	public static Block AEExtra_certusQuartzTank;
	
	// Mod configuration (see loadWarpDriveConfig() for comments/definitions)
	// General
	public static int		G_SPACE_PROVIDER_ID = 14;
	public static int		G_SPACE_DIMENSION_ID = -2;
	public static int		G_HYPERSPACE_PROVIDER_ID = 15;
	public static int		G_HYPERSPACE_DIMENSION_ID = -3;
	public static int		G_SPACE_WORLDBORDER_BLOCKS = 100000;
	public static final int	LUA_SCRIPTS_NONE = 0;
	public static final int	LUA_SCRIPTS_TEMPLATES = 1;
	public static final int	LUA_SCRIPTS_ALL = 2;
	public static int		G_LUA_SCRIPTS = LUA_SCRIPTS_ALL;
	public static boolean	G_DEBUGMODE = false;
	public static String	G_SCHEMALOCATION = "warpDrive_schematics";
	public static int		G_BLOCKS_PER_TICK = 3500;
	
	public static boolean	G_ENABLE_IC2_RECIPES		= true;
	public static boolean	G_ENABLE_VANILLA_RECIPES	= false;
	public static boolean	G_ENABLE_TDK_RECIPES		= false;

	// Transition planes
	public static TransitionPlane[] G_TRANSITIONPLANES = null;

	// Warp Drive Core
    public static int		WC_MAX_ENERGY_VALUE = 100000000;
    public static int		WC_ENERGY_PER_BLOCK_MODE1 = 10;
    public static int		WC_ENERGY_PER_DISTANCE_MODE1 = 100;
    public static int		WC_ENERGY_PER_BLOCK_MODE2 = 1000;
    public static int		WC_ENERGY_PER_DISTANCE_MODE2 = 1000;
    public static int		WC_ENERGY_PER_ENTITY_TO_SPACE = 1000000;
    public static int		WC_MAX_JUMP_DISTANCE = 128;
    public static int		WC_MAX_SHIP_VOLUME_ON_SURFACE = 3000;
    public static int		WC_MIN_SHIP_VOLUME_FOR_HYPERSPACE = 1200;
    public static int		WC_MAX_SHIP_SIDE = 127;
    public static int		WC_COOLDOWN_INTERVAL_SECONDS = 30;
    public static int		WC_COLLISION_TOLERANCE_BLOCKS = 3;
    public static int		WC_WARMUP_SHORTJUMP_SECONDS = 10;
    public static int		WC_WARMUP_LONGJUMP_SECONDS = 30;
    public static int		WC_WARMUP_RANDOM_TICKS = 60;
    public static int		WC_CORES_REGISTRY_UPDATE_INTERVAL_SECONDS = 10;
    public static int		WC_ISOLATION_UPDATE_INTERVAL_SECONDS = 10;		

    // Warp Radar
    public static int		WR_MAX_ENERGY_VALUE = 100000000; // 100kk eU
    public static int		WR_MAX_ISOLATION_RANGE = 2;
    public static int		WR_MIN_ISOLATION_BLOCKS = 5;
    public static int		WR_MAX_ISOLATION_BLOCKS = 132;
    public static double	WR_MIN_ISOLATION_EFFECT = 0.12;
    public static double	WR_MAX_ISOLATION_EFFECT = 1.00;
    
	// Ship Scanner
	public static int		SS_MAX_ENERGY_VALUE = 500000000;
	public static int		SS_EU_PER_BLOCK_SCAN = 100; // eU per block of ship volume (including air)
	public static int		SS_EU_PER_BLOCK_DEPLOY = 5000;
	public static int		SS_MAX_DEPLOY_RADIUS_BLOCKS = 50;
	
    // Particle Booster
    public static int		PB_MAX_ENERGY_VALUE = 100000;
	
	// Laser Emitter
	public static int		LE_MAX_BOOSTERS_NUMBER = 10;
	public static int		LE_MAX_LASER_ENERGY = 4000000;
	public static int		LE_EMIT_DELAY_TICKS = 20 * 3;
	public static int		LE_EMIT_SCAN_DELAY_TICKS = 10;
	
	public static double	LE_COLLECT_ENERGY_MULTIPLIER = 0.60D;
	public static int		LE_BEAM_LENGTH_PER_ENERGY_DIVIDER = 5000;
	public static int		LE_ENTITY_HIT_SET_ON_FIRE_TIME = 100;
	public static int		LE_ENTITY_HIT_DAMAGE_PER_ENERGY_DIVIDER = 10000;
	public static int		LE_ENTITY_HIT_EXPLOSION_LASER_ENERGY = 1000000;
	public static int		LE_BLOCK_HIT_CONSUME_ENERGY = 70000;
	public static int		LE_BLOCK_HIT_CONSUME_ENERGY_PER_BLOCK_RESISTANCE = 1000;
	public static int		LE_BLOCK_HIT_CONSUME_ENERGY_PER_DISTANCE = 10;
    
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
	public static int		ML_MAX_BOOSTERS_NUMBER = 1;
	public static int		ML_WARMUP_DELAY_TICKS = 20;
	public static int		ML_SCAN_DELAY_TICKS = 20;
	public static int		ML_MINE_DELAY_TICKS = 3;
	public static int		ML_EU_PER_LAYER_SPACE = 25000;
	public static int		ML_EU_PER_LAYER_EARTH = 35000;
	public static int		ML_EU_PER_BLOCK_SPACE = 150; 
	public static int 		ML_EU_PER_BLOCK_EARTH = 300;
	public static double	ML_EU_MUL_ORESONLY = 5.0;	// lower value encourages to keep the land 'clean'
	public static double	ML_EU_MUL_SILKTOUCH = 2.5;
	public static double	ML_DEUTERIUM_MUL_SILKTOUCH = 1.0;
	public static double	ML_EU_MUL_FORTUNE = 2.5;
	public static int		ML_MAX_RADIUS = 5;
	
	// Tree farm
	public static int		TF_MAX_SIZE = 16;
	
	// Cloaking device core
	public static int		CD_MAX_CLOAKING_FIELD_SIDE = 100;
	public static int		CD_ENERGY_PER_BLOCK_TIER1 = 125;
	public static int		CD_ENERGY_PER_BLOCK_TIER2 = 500; 
	public static int		CD_FIELD_REFRESH_INTERVAL_SECONDS = 3;
	public static int		CD_COIL_CAPTURE_BLOCKS = 5;

	// Air generator
	public static int		AG_RF_PER_CANISTER = 20;
		
	// Laser Lift
	public static int		LL_MAX_ENERGY = 2400;
	public static int		LL_LIFT_ENERGY = 800;
	public static int		LL_TICK_RATE = 10;
	
	// Chunk Loader
	public static int		CL_MAX_ENERGY = 1000000;
	public static int		CL_MAX_DISTANCE = 2;
	public static int		CL_RF_PER_CHUNKTICK = 320;

	public static ItemStack getIC2Item(String id) {
		return IC2Items.getItem(id);
	}

	public static ItemStack getAEBlock(String id) {
		try {
			Object ret = AEBlocks.getField(id).get(null);
			if (ret instanceof ItemStack)
				return (ItemStack)ret;
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Call getAEBlock failed for " + id);
		}
		return null;
	}

	public static ItemStack getAEMaterial(String id) {
		try {
			Object ret = AEMaterials.getField(id).get(null);
			if (ret instanceof ItemStack)
				return (ItemStack)ret;
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Call getAEMaterial failed for " + id);
		}
		return null;
	}

	public static ItemStack getAEItem(String id) {
		try {
			Object ret = AEItems.getField(id).get(null);
			if (ret instanceof ItemStack)
				return (ItemStack)ret;
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Call getAEItem failed for " + id);
		}
		return null;
	}

	public static void preInit(Configuration configIn) {
		config = configIn;
	}

	public static void loadWarpDriveConfig() {
		// General
		G_SPACE_PROVIDER_ID = config.get("General", "space_provider_id", G_SPACE_PROVIDER_ID, "Space dimension provider ID").getInt();
		G_SPACE_DIMENSION_ID = config.get("General", "space_dimension_id", G_SPACE_DIMENSION_ID, "Space dimension world ID").getInt();
		G_HYPERSPACE_PROVIDER_ID = config.get("General", "hyperspace_provider_id", G_HYPERSPACE_PROVIDER_ID, "Hyperspace dimension provider ID").getInt();
		G_HYPERSPACE_DIMENSION_ID = config.get("General", "hyperspace_dimension_id", G_HYPERSPACE_DIMENSION_ID, "Hyperspace dimension world ID").getInt();
		G_SPACE_WORLDBORDER_BLOCKS = config.get("General", "space_worldborder_blocks", G_SPACE_WORLDBORDER_BLOCKS, "World border applied to hyperspace & space, set to 0 to disable it").getInt();
		G_LUA_SCRIPTS = config.get("General", "lua_scripts", G_LUA_SCRIPTS, "LUA scripts to load when connecting machines: 0 = none, 1 = templates in a subfolder, 2 = ready to roll (templates are still provided)").getInt();
		G_DEBUGMODE = config.get("General", "debug_mode", G_DEBUGMODE, "Detailled logs to help debug the mod, enable it before reporting a bug").getBoolean(false);
		G_SCHEMALOCATION = config.get("General", "schematic_location", G_SCHEMALOCATION, "Folder where to save ship schematics").getString();
		G_BLOCKS_PER_TICK = config.get("General", "blocks_per_tick", G_BLOCKS_PER_TICK, "Number of blocks to move per ticks, too high will cause lag spikes on ship jumping or deployment, too low may break the ship wirings").getInt();

		G_ENABLE_IC2_RECIPES = config.get("General", "enable_ic2_recipes", G_ENABLE_IC2_RECIPES, "Original recipes based on IndustrialCrat2 by Cr0s").getBoolean(true);
		G_ENABLE_VANILLA_RECIPES = config.get("General", "enable_vanilla_recipes", G_ENABLE_VANILLA_RECIPES, "Vanilla recipes by DarkholmeTenk").getBoolean(false);
		G_ENABLE_TDK_RECIPES = config.get("General", "enable_TDK_recipes", G_ENABLE_TDK_RECIPES, "Mixed recipes for TDK packs by Lem'ADEC (currently requires at least AppliedEnergistics, Extracells, AtomicScience, IndustrialCraft2, GraviSuite and ThermalExpansion").getBoolean(false);

		// TransitionPlane
		config.addCustomCategoryComment("TransitionPlane", "Transition planes defines which region in space allows to go to other dimensions, default is overworld with 100k radius.\n"
					+ "Each plane is square shaped and defined as a list of 7 integers (all measured in blocks, border is the radius from center)");
		String[] transitionNames = { "overworld" };
		transitionNames = config.get("TransitionPlane", "names", transitionNames, "this is the list of transition planes defined hereafter").getStringList();
		int[] defaultPlane = {0, 0, 0, 30000000, 30000000, 0, 0}; // 30000000 is Minecraft limit for SetBlock
		G_TRANSITIONPLANES = new TransitionPlane[transitionNames.length];
		int index = 0;
		for (String name : transitionNames) {
			int[] plane = config.get("TransitionPlane", name, defaultPlane, "dimensionId, dimensionCenterX, dimensionCenterZ, borderSizeX, borderSizeZ, SpaceCenterX, SpaceCenterZ").getIntList();
			if (plane.length != 7) {
				WarpDrive.print("Invalid transition plane definition '" + name + "' (exactly 7 integers are expected), using default instead");
				plane = defaultPlane.clone();
			}
			TransitionPlane newPlane = new TransitionPlane(plane[0], plane[1], plane[2], plane[3], plane[4], plane[5], plane[6]);
			WarpDrive.print("Adding '" + name + "' as " + newPlane.toString());
			G_TRANSITIONPLANES[index] = newPlane;
		}
		// FIXME: check transition planes aren't overlapping
		// FIXME: check transition planes have valid dimension id
		
		// Warp Core
		WC_MAX_ENERGY_VALUE = config.get("WarpCore", "max_energy_value", WC_MAX_ENERGY_VALUE, "Maximum energy storage").getInt();
		WC_ENERGY_PER_BLOCK_MODE1 = config.get("WarpCore", "energy_per_block_mode1", WC_ENERGY_PER_BLOCK_MODE1).getInt();
		WC_ENERGY_PER_DISTANCE_MODE1 = config.get("WarpCore", "energy_per_distance_mode1", WC_ENERGY_PER_DISTANCE_MODE1).getInt();
	    WC_ENERGY_PER_DISTANCE_MODE2 = config.get("WarpCore", "energy_per_distance_mode2", WC_ENERGY_PER_DISTANCE_MODE2).getInt();
	    WC_ENERGY_PER_BLOCK_MODE2 = config.get("WarpCore", "energy_per_block_mode2", WC_ENERGY_PER_BLOCK_MODE2).getInt();
	    WC_ENERGY_PER_ENTITY_TO_SPACE = config.get("WarpCore", "energy_per_entity_to_space", WC_ENERGY_PER_ENTITY_TO_SPACE).getInt();
	    WC_MAX_JUMP_DISTANCE = config.get("WarpCore", "max_jump_distance", WC_MAX_JUMP_DISTANCE, "Maximum jump lenght value in blocks").getInt();
	    WC_MAX_SHIP_VOLUME_ON_SURFACE = config.get("WarpCore", "max_ship_volume_on_surface", WC_MAX_SHIP_VOLUME_ON_SURFACE, "Maximum ship mass (in blocks) to jump on earth").getInt();
	    WC_MIN_SHIP_VOLUME_FOR_HYPERSPACE = config.get("WarpCore", "min_ship_volume_for_hyperspace", WC_MIN_SHIP_VOLUME_FOR_HYPERSPACE, "Minimum ship mass (in blocks) to enter or exit hyperspace without a jumpgate").getInt();
	    WC_MAX_SHIP_SIDE = config.get("WarpCore", "max_ship_side", WC_MAX_SHIP_SIDE, "Maximum ship size on each axis in blocks").getInt(); 
	    WC_COLLISION_TOLERANCE_BLOCKS = config.get("WarpCore", "collision_tolerance_blocks", WC_COLLISION_TOLERANCE_BLOCKS, "Tolerance in block in case of collision before causing damages...").getInt();
	    WC_COOLDOWN_INTERVAL_SECONDS = config.get("WarpCore", "cooldown_interval_seconds", WC_COOLDOWN_INTERVAL_SECONDS, "Cooldown seconds to wait after jumping").getInt();
	    WC_WARMUP_SHORTJUMP_SECONDS = config.get("WarpCore", "warmup_shortjump_seconds", WC_WARMUP_SHORTJUMP_SECONDS, "Short jump means less than 50 blocks").getInt();
	    WC_WARMUP_LONGJUMP_SECONDS = config.get("WarpCore", "warmup_longjump_seconds", WC_WARMUP_LONGJUMP_SECONDS, "Long jump means more than 50 blocks").getInt();
	    
	    WC_CORES_REGISTRY_UPDATE_INTERVAL_SECONDS = config.get("WarpCore", "cores_registry_update_interval", WC_CORES_REGISTRY_UPDATE_INTERVAL_SECONDS, "(measured in seconds)").getInt(); 
	    WC_ISOLATION_UPDATE_INTERVAL_SECONDS = config.get("WarpCore", "isolation_update_interval", WC_ISOLATION_UPDATE_INTERVAL_SECONDS, "(measured in seconds)").getInt();		
	    
	    // Warp Radar
	    WR_MAX_ENERGY_VALUE = config.get("WarpRadar", "max_energy_value", WR_MAX_ENERGY_VALUE).getInt();
	    WR_MAX_ISOLATION_RANGE = config.get("WarpRadar", "max_isolation_range", WR_MAX_ISOLATION_RANGE, "radius around core where isolation blocks count (2 to 8), higher is lagger").getInt();
	    WR_MAX_ISOLATION_RANGE = Math.min(8, Math.max(WR_MAX_ISOLATION_RANGE, 2));
	    WR_MIN_ISOLATION_BLOCKS = config.get("WarpRadar", "min_isolation_blocks", WR_MIN_ISOLATION_BLOCKS, "number of isolation blocks required to get some isolation (0 to 20)").getInt();
	    WR_MIN_ISOLATION_BLOCKS = Math.min(20, Math.max(WR_MIN_ISOLATION_BLOCKS, 0));
	    WR_MAX_ISOLATION_BLOCKS = config.get("WarpRadar", "max_isolation_blocks", WR_MAX_ISOLATION_BLOCKS, "number of isolation blocks required to reach maximum effect (5 to 100)").getInt();
	    WR_MAX_ISOLATION_BLOCKS = Math.min(100, Math.max(WR_MAX_ISOLATION_BLOCKS, 5));
	    WR_MIN_ISOLATION_EFFECT = config.get("WarpRadar", "min_isolation_effect", WR_MIN_ISOLATION_EFFECT, "isolation effect achieved with min number of isolation blocks (0.01 to 0.95)").getDouble(0.12D);
	    WR_MIN_ISOLATION_EFFECT = Math.min(0.95D, Math.max(WR_MIN_ISOLATION_EFFECT, 0.01D));
	    WR_MAX_ISOLATION_EFFECT = config.get("WarpRadar", "max_isolation_effect", WR_MAX_ISOLATION_EFFECT, "isolation effect achieved with max number of isolation blocks (0.01 to 1.00)").getDouble(1.00D);
	    WR_MAX_ISOLATION_EFFECT = Math.min(1.0D, Math.max(WR_MAX_ISOLATION_EFFECT, 0.01D));
	    
	    // Ship Scanner
		SS_MAX_ENERGY_VALUE = config.get("WarpCore", "max_energy_value", SS_MAX_ENERGY_VALUE, "Maximum energy storage").getInt();
	    SS_EU_PER_BLOCK_SCAN = config.get("ShipScanner", "energy_per_block_when_scanning", SS_EU_PER_BLOCK_SCAN, "Energy consummed per block when scanning a ship (use -1 to consume everything)").getInt();
	    if (SS_EU_PER_BLOCK_SCAN != -1) {
	    	SS_EU_PER_BLOCK_SCAN = Math.min(SS_MAX_ENERGY_VALUE, Math.max(SS_EU_PER_BLOCK_SCAN, 1));
	    }
	    SS_EU_PER_BLOCK_DEPLOY = config.get("ShipScanner", "energy_per_block_when_deploying", SS_EU_PER_BLOCK_DEPLOY, "Energy consummed per block when deploying a ship (use -1 to consume everything)").getInt();
	    if (SS_EU_PER_BLOCK_DEPLOY != -1) {
	    	SS_EU_PER_BLOCK_DEPLOY = Math.min(SS_MAX_ENERGY_VALUE, Math.max(SS_EU_PER_BLOCK_DEPLOY, 1));
	    }
	    SS_MAX_DEPLOY_RADIUS_BLOCKS = config.get("ShipScanner", "max_deploy_radius_blocks", SS_MAX_DEPLOY_RADIUS_BLOCKS, "Max distance from ship scanner to ship core, measured in blocks").getInt();

	    // Particle Booster
	    PB_MAX_ENERGY_VALUE = config.get("ParticleBooster", "max_energy_value", PB_MAX_ENERGY_VALUE).getInt();
		
		// Laser Emitter
		LE_MAX_BOOSTERS_NUMBER = config.get("LaserEmitter", "max_boosters_number", LE_MAX_BOOSTERS_NUMBER).getInt();
		LE_MAX_LASER_ENERGY = config.get("LaserEmitter", "max_laser_energy", LE_MAX_LASER_ENERGY).getInt();
		LE_EMIT_DELAY_TICKS = config.get("LaserEmitter", "emit_delay_ticks", LE_EMIT_DELAY_TICKS).getInt();
		LE_EMIT_SCAN_DELAY_TICKS = config.get("LaserEmitter", "emit_scan_delay_ticks", LE_EMIT_SCAN_DELAY_TICKS).getInt();
		
		// Laser Emitter tweaks
		LE_COLLECT_ENERGY_MULTIPLIER = config.get("LaserEmitterTweaks", "collect_energy_multiplier", LE_COLLECT_ENERGY_MULTIPLIER).getDouble(0.6D);
		LE_BEAM_LENGTH_PER_ENERGY_DIVIDER = config.get("LaserEmitterTweaks", "beam_length_per_energy_divider", LE_BEAM_LENGTH_PER_ENERGY_DIVIDER).getInt();
		LE_ENTITY_HIT_SET_ON_FIRE_TIME = config.get("LaserEmitterTweaks", "entity_hit_set_on_fire_time", LE_ENTITY_HIT_SET_ON_FIRE_TIME).getInt();
		LE_ENTITY_HIT_DAMAGE_PER_ENERGY_DIVIDER = config.get("LaserEmitterTweaks", "entity_hit_damage_per_energy_divider", LE_ENTITY_HIT_DAMAGE_PER_ENERGY_DIVIDER).getInt();
		LE_ENTITY_HIT_EXPLOSION_LASER_ENERGY = config.get("LaserEmitterTweaks", "entity_hit_explosion_laser_energy", LE_ENTITY_HIT_EXPLOSION_LASER_ENERGY).getInt();
		LE_BLOCK_HIT_CONSUME_ENERGY = config.get("LaserEmitterTweaks", "block_hit_consume_energy", LE_BLOCK_HIT_CONSUME_ENERGY).getInt();
		LE_BLOCK_HIT_CONSUME_ENERGY_PER_BLOCK_RESISTANCE = config.get("LaserEmitterTweaks", "block_hit_consume_energy_per_block_resistance", LE_BLOCK_HIT_CONSUME_ENERGY_PER_BLOCK_RESISTANCE).getInt();
		LE_BLOCK_HIT_CONSUME_ENERGY_PER_DISTANCE = config.get("LaserEmitterTweaks", "block_hit_consume_energy_per_distance", LE_BLOCK_HIT_CONSUME_ENERGY_PER_DISTANCE).getInt();
		
	    // Mining Laser
		ML_MAX_BOOSTERS_NUMBER = config.get("MiningLaser", "max_boosters_number", ML_MAX_BOOSTERS_NUMBER).getInt();
		ML_WARMUP_DELAY_TICKS = config.get("MiningLaser", "warmup_delay_ticks", ML_WARMUP_DELAY_TICKS).getInt();
		ML_SCAN_DELAY_TICKS = config.get("MiningLaser", "scan_delay_ticks", ML_SCAN_DELAY_TICKS).getInt();
		ML_MINE_DELAY_TICKS = config.get("MiningLaser", "mine_delay_ticks", ML_MINE_DELAY_TICKS).getInt();
		ML_EU_PER_LAYER_SPACE = config.get("MiningLaser", "eu_per_layer_space", ML_EU_PER_LAYER_SPACE).getInt();
		ML_EU_PER_LAYER_EARTH = config.get("MiningLaser", "eu_per_layer_earth", ML_EU_PER_LAYER_EARTH).getInt();	 
		ML_EU_PER_BLOCK_SPACE = config.get("MiningLaser", "eu_per_block_space", ML_EU_PER_BLOCK_SPACE).getInt();
		ML_EU_PER_BLOCK_EARTH = config.get("MiningLaser", "eu_per_block_earth", ML_EU_PER_BLOCK_EARTH).getInt();	  
		ML_EU_MUL_ORESONLY = config.get("MiningLaser", "oresonly_power_mul", ML_EU_MUL_ORESONLY).getDouble(4.0);
		ML_EU_MUL_SILKTOUCH = config.get("MiningLaser", "silktouch_power_mul", ML_EU_MUL_SILKTOUCH).getDouble(2.5);
		ML_DEUTERIUM_MUL_SILKTOUCH = config.get("MiningLaser", "silktouch_deuterium_mul", ML_DEUTERIUM_MUL_SILKTOUCH).getDouble(1.0);
		ML_EU_MUL_FORTUNE   = config.get("MiningLaser", "fortune_power_base", ML_EU_MUL_FORTUNE).getDouble(2.5);
		ML_MAX_RADIUS = config.get("MiningLaser", "max_radius", ML_MAX_RADIUS).getInt();
		
		// Tree Farm
		TF_MAX_SIZE = config.get("TreeFarm", "max_treefarm_size", TF_MAX_SIZE).getInt();
		
		// Cloaking device core
		CD_MAX_CLOAKING_FIELD_SIDE = config.get("CloakingDevice", "max_cloaking_field_side", CD_MAX_CLOAKING_FIELD_SIDE).getInt();
		CD_ENERGY_PER_BLOCK_TIER1 = config.get("CloakingDevice", "energy_per_block_tier1", CD_ENERGY_PER_BLOCK_TIER1).getInt();
		CD_ENERGY_PER_BLOCK_TIER2 = config.get("CloakingDevice", "energy_per_block_tier2", CD_ENERGY_PER_BLOCK_TIER2).getInt();	
		CD_FIELD_REFRESH_INTERVAL_SECONDS = config.get("CloakingDevice", "field_refresh_interval_seconds", CD_FIELD_REFRESH_INTERVAL_SECONDS).getInt();	
		CD_COIL_CAPTURE_BLOCKS = config.get("CloakingDevice", "coil_capture_blocks", CD_COIL_CAPTURE_BLOCKS, "Extra blocks covered after the outer coils").getInt();
		
		// Air generator
		AG_RF_PER_CANISTER = config.get("Air Generator", "energy_per_canister", AG_RF_PER_CANISTER).getInt();
		
		// Laser lift
		LL_MAX_ENERGY = config.get("LaserLift", "max_energy", LL_MAX_ENERGY).getInt();
		LL_LIFT_ENERGY = config.get("LaserLift", "lift_energy", LL_LIFT_ENERGY, "Energy consummed per entity moved").getInt();
		LL_TICK_RATE = config.get("LaserLift", "tick_rate", LL_TICK_RATE).getInt();
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
		minerLogs = new ArrayList<Item>();
		minerLeaves = new ArrayList<Item>();
		scannerIgnoreBlocks = new ArrayList<Block>();
		config.load();
		/* Unnneccessary, delete at some point
		coreID = config.getBlock("core", 500).getInt();
		controllerID = config.getBlock("controller", 501).getInt();
		radarID = config.getBlock("radar", 502).getInt();
		isolationID = config.getBlock("isolation", 503).getInt();
		airID = config.getBlock("air", 504).getInt();
		airgenID = config.getBlock("airgen", 505).getInt();
		gasID = config.getBlock("gas", 506).getInt();
		laserID = config.getBlock("laser", 507).getInt();
		miningLaserID = config.getBlock("mininglaser", 508).getInt();
		particleBoosterID = config.getBlock("particlebooster", 509).getInt();
		liftID = config.getBlock("lift", 510).getInt();
		laserCamID = config.getBlock("lasercam", 512).getInt();
		camID = config.getBlock("camera", 513).getInt();
		monitorID = config.getBlock("monitor", 514).getInt();
		iridiumBlockID = config.getBlock("iridium", 515).getInt();
		shipScannerID = config.getBlock("shipscanner", 516).getInt();
		cloakCoreID = config.getBlock("cloakcore", 517).getInt();
		cloakCoilID = config.getBlock("cloakcoil", 518).getInt();
		
		componentID = config.getItem("component", 8701).getInt();
		helmetID = config.getItem("helmet", 8702).getInt();
		chestID = config.getItem("chest", 8703).getInt();
		pantsID = config.getItem("pants", 8704).getInt();
		bootsID = config.getItem("boots", 8705).getInt();
		airCanisterID = config.getItem("airCanisterFull", 8706).getInt();
		upgradeID = config.getItem("upgrade", 8707).getInt();
		*/
		
		isForgeMultipartLoaded = Loader.isModLoaded("ForgeMultipart");
		if (isForgeMultipartLoaded) {
			loadForgeMultipart();
		}
		
		isICLoaded = Loader.isModLoaded("IC2");
		if (isICLoaded)
			loadIC2();
		
		isCCLoaded = Loader.isModLoaded("ComputerCraft");
		if (isCCLoaded)
			loadCC();
		
		isAppliedEnergisticsLoaded = Loader.isModLoaded("AppliedEnergistics");
		if (isAppliedEnergisticsLoaded)
			loadAppliedEnergistics();
		
		isAEExtraLoaded = Loader.isModLoaded("extracells");
		if (isAEExtraLoaded)
			loadAEExtra();	
		
		isAdvSolPanelLoaded = Loader.isModLoaded("AdvancedSolarPanel");
		if (isAdvSolPanelLoaded)
			loadASP();
		
		isAtomicScienceLoaded = Loader.isModLoaded("ResonantInduction|Atomic");
		if (isAtomicScienceLoaded)
			loadAtomicScience();
		
		isICBMLoaded = Loader.isModLoaded("ICBM|Explosion");
		if (isICBMLoaded)
			loadICBM();
		
		isMFFSLoaded = Loader.isModLoaded("MFFS");
		if (isMFFSLoaded)
			loadMFFS();
		
		isGraviSuiteLoaded = Loader.isModLoaded("GraviSuite");
		if (isGraviSuiteLoaded)
			loadGraviSuite();
		
		isUndergroundBiomesLoaded = Loader.isModLoaded("UndergroundBiomes");
		if (isUndergroundBiomesLoaded)
			loadUndergroundBiomes();

		isNetherOresLoaded = Loader.isModLoaded("NetherOres");
		if (isNetherOresLoaded)
			loadNetherOres();
		
		isThermalExpansionLoaded = Loader.isModLoaded("ThermalExpansion");
		if (isThermalExpansionLoaded)
			loadThermalExpansion();
		
		isMetallurgyLoaded = Loader.isModLoaded("Metallurgy3Core");
		if (isMetallurgyLoaded) {
			loadMetallurgy();
		}

		isAdvancedRepulsionSystemsLoaded = Loader.isModLoaded("AdvancedRepulsionSystems");
		if (isAdvancedRepulsionSystemsLoaded) {
			loadAdvancedRepulsionSystems();
		}

		minerOres.add(WarpDrive.iridiumBlock);
		minerOres.add(Blocks.coal_ore);
		minerOres.add(Blocks.quartz_ore);
		minerOres.add(Blocks.obsidian);
		minerOres.add(Blocks.web);
		minerOres.add(Blocks.fence);
		minerOres.add(Blocks.torch);
		minerOres.add(Blocks.glowstone);
		minerOres.add(Blocks.redstone_block);
		
		// Ignore WarpDrive blocks (which potentially will be duplicated by cheaters using ship scan/deploy)
		scannerIgnoreBlocks.add(WarpDrive.warpCore);
		scannerIgnoreBlocks.add(WarpDrive.protocolBlock);
		scannerIgnoreBlocks.add(WarpDrive.iridiumBlock);
		
		if (isICLoaded) {
			scannerIgnoreBlocks.add(Block.getBlockFromItem(IC2Items.getItem("mfsUnit").getItem()));
			scannerIgnoreBlocks.add(Block.getBlockFromItem(IC2Items.getItem("mfeUnit").getItem()));
			scannerIgnoreBlocks.add(Block.getBlockFromItem(IC2Items.getItem("cesuUnit").getItem()));
			scannerIgnoreBlocks.add(Block.getBlockFromItem(IC2Items.getItem("batBox").getItem()));
		}
		if (isICBMLoaded) {
			//TODO: What is this for?
			//scannerIgnoreBlocks.add(ICBM_Explosive);
		}
		if (isCCLoaded) {
			//TODO: Fix
			
			 //scannerIgnoreBlocks.add(CC_Computer);
			 //scannerIgnoreBlocks.add(CCT_Turtle);
			 //scannerIgnoreBlocks.add(CCT_Upgraded);
			 //scannerIgnoreBlocks.add(CCT_Advanced);
		}
		// Do not deploy ores and valuables
		for (Block t : commonWorldGenOres) {// each element of this set is pair [id, meta]
			scannerIgnoreBlocks.add(t); // we adding ID only
		}
		
		loadWarpDriveConfig();
		config.save();
	}
	
	public static void postInit() {
		loadOreDict();
	}
	
	private static void loadOreDict() {
		String[] oreNames = OreDictionary.getOreNames();
		for(String oreName: oreNames) {
			String lowerOreName = oreName.toLowerCase();
			if (oreName.substring(0,3).equals("ore")) {
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for(ItemStack i: item) {
					minerOres.add(Block.getBlockFromItem(i.getItem()));
					WarpDrive.debugPrint("Added ore: "+ i.getItem().getUnlocalizedName());
				}
			}
			if (lowerOreName.contains("log")) {
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for(ItemStack i: item) {
					minerLogs.add(i.getItem());
					WarpDrive.debugPrint("Added log: "+i.getDisplayName());
				}
			}
			if (lowerOreName.contains("leave") || lowerOreName.contains("leaf")) {
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for(ItemStack i: item) {
					minerLeaves.add(i.getItem());
					WarpDrive.debugPrint("Added leaf: "+i.getDisplayName());
				}
			}
		}
	}

	private static void loadForgeMultipart() {
		try {
			Class forgeMultipart_helper = Class.forName("codechicken.multipart.MultipartHelper");
			forgeMultipart_helper_createTileFromNBT =  forgeMultipart_helper.getDeclaredMethod("createTileFromNBT", World.class, NBTTagCompound.class);
			forgeMultipart_helper_sendDescPacket = forgeMultipart_helper.getDeclaredMethod("sendDescPacket", World.class, TileEntity.class);
			Class forgeMultipart_tileMultipart = Class.forName("codechicken.multipart.TileMultipart");
			forgeMultipart_tileMultipart_onChunkLoad = forgeMultipart_tileMultipart.getDeclaredMethod("onChunkLoad");
		} catch (Exception e) {
			isForgeMultipartLoaded = false;
			WarpDrive.debugPrint("WarpDriveConfig Error loading ForgeMultipart classes");
			e.printStackTrace();
		}
	}
	
	private static void loadIC2()
	{
		ASP = IC2Items.getItem("solarPanel").getItem();
		spaceHelmets.add(IC2Items.getItem("hazmatHelmet").getItem());
		spaceHelmets.add(IC2Items.getItem("quantumHelmet").getItem());
		jetpacks.add(IC2Items.getItem("jetpack").getItem());
		jetpacks.add(IC2Items.getItem("electricJetpack").getItem());
		IC2_Air = IC2Items.getItem("airCell");
		IC2_Empty = IC2Items.getItem("cell");
		ItemStack rubberWood = IC2Items.getItem("rubberWood");
		IC2_Resin = IC2Items.getItem("resin").getItem();
		if(rubberWood != null) {
			IC2_RubberWood = rubberWood.getItem();
		}
		ItemStack ore = IC2Items.getItem("uraniumOre");
		if (ore != null) commonWorldGenOres.add(Block.getBlockFromItem(ore.getItem()));
		ore = IC2Items.getItem("copperOre");
		if (ore != null) commonWorldGenOres.add(Block.getBlockFromItem(ore.getItem()));
		ore = IC2Items.getItem("tinOre");
		if (ore != null) commonWorldGenOres.add(Block.getBlockFromItem(ore.getItem()));
		ore = IC2Items.getItem("leadOre");
		if (ore != null) commonWorldGenOres.add(Block.getBlockFromItem(ore.getItem()));

		minerOres.add(Block.getBlockFromItem(IC2Items.getItem("rubberWood").getItem()));
		IC2_fluidCell = IC2Items.getItem("FluidCell").getItem();
	}

	private static void loadCC()
	{
		try
		{
			
			CC_Computer = ComputerCraft.Blocks.computer;
			CC_peripheral = ComputerCraft.Blocks.peripheral;
			//CC_Floppy = ComputerCraft.Blocks.floppy; //TODO: Does not exist anymore. Check if replacement relevant
			CCT_Turtle = ComputerCraft.Blocks.turtle;
			CCT_Upgraded = ComputerCraft.Blocks.turtleExpanded;
			CCT_Advanced = ComputerCraft.Blocks.turtleAdvanced;
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading ComputerCraft classes");
			e.printStackTrace();
		}
	}
	
	private static void loadAppliedEnergistics()
	{
		try
		{
			AEBlocks = Class.forName("appeng.api.Blocks");
			AEMaterials = Class.forName("appeng.api.Materials");
			AEItems = Class.forName("appeng.api.Items");
			minerOres.add(Block.getBlockFromItem(((ItemStack)AEBlocks.getField("blkQuartzOre").get(null)).getItem()));
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading AppliedEnergistics classes");
			e.printStackTrace();
			isAppliedEnergisticsLoaded = false;
		}
	}

	private static void loadAEExtra()
	{
		try
		{
			Class<?> z = Class.forName("extracells.ItemEnum");
			Object z1 = z.getEnumConstants()[6];
			AEExtra_fluidDrive = (Item)z1.getClass().getDeclaredMethod("getItemInstance").invoke(z1);
			z = Class.forName("extracells.BlockEnum");
			z1 = z.getEnumConstants()[9];
			AEExtra_certusQuartzTank = (Block)z1.getClass().getDeclaredMethod("getBlockInstance").invoke(z1);
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading AEExtra classes");
			e.printStackTrace();
			isAEExtraLoaded = false;
		}
	}	
	
	private static void loadASP()
	{
		try
		{
			
			spaceHelmets.add(AdvancedSolarPanel.advancedSolarHelmet);
			spaceHelmets.add(AdvancedSolarPanel.hybridSolarHelmet);
			spaceHelmets.add(AdvancedSolarPanel.ultimateSolarHelmet);
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading ASP classes");
			e.printStackTrace();
			isAdvSolPanelLoaded = false;
		}
	}

	private static void loadAtomicScience() {
		//No 1.7.10 version
		/*
		try {
			Class<?> z = Class.forName("resonantinduction.atomic.Atomic");
			commonWorldGenOres.add(new int[] {((Block)z.getField("blockUraniumOre").get(null)).blockID, 0});
			AS_Turbine = ((Block)z.getField("blockElectricTurbine").get(null)).blockID;
			AS_deuteriumCell = ((Item)z.getField("itemDeuteriumCell").get(null)).itemID;
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Error loading AS classes");
			isAtomicScienceLoaded = false;
		}*/
	}

	private static void loadICBM() {
		//No 1.7 yet, second project
		/*
		try {
			Class<?> z = Class.forName("icbm.core.ICBMCore");
			commonWorldGenOres.add(new int[] {((Block)z.getField("blockSulfurOre").get(null)).blockID, 0});
			z = Class.forName("icbm.explosion.ICBMExplosion");
			ICBM_Machine = ((Block)z.getField("blockMachine").get(null)).blockID;
			ICBM_Missile = ((Item)z.getField("itemMissile").get(null)).itemID;
			ICBM_Explosive = ((Block)z.getField("blockExplosive").get(null)).blockID;
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Error loading ICBM classes");
			e.printStackTrace();
			isICBMLoaded = false;
		}*/
	}

	private static void loadMFFS() {
		try {
			forceFieldBlocks.add(Content.forceField());
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Error loading MFFS classes");
			e.printStackTrace();
			isICBMLoaded = false;
		}
	}

	private static void loadGraviSuite() {
		try {
			spaceHelmets.add(GraviSuite.ultimateSolarHelmet);
			jetpacks.add(GraviSuite.advJetpack);
			jetpacks.add(GraviSuite.graviChestPlate);
			GS_ultimateLappack = GraviSuite.ultimateLappack;
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Error loading GS classes");
			e.printStackTrace();
			isGraviSuiteLoaded = false;
		}
	}

	private static void loadUndergroundBiomes() {
		//TODO: Re-add if desired. My patience has run out for mod compatibility.
		/*
		try {
			Class<?> z = Class.forName("exterminatorJeff.undergroundBiomes.common.UndergroundBiomes");
			UB_igneousStone           = ((Block)z.getField("igneousStone").get(null)).blockID;
			UB_igneousCobblestone     = ((Block)z.getField("igneousCobblestone").get(null)).blockID;
			UB_metamorphicStone       = ((Block)z.getField("metamorphicStone").get(null)).blockID;
			UB_metamorphicCobblestone = ((Block)z.getField("metamorphicCobblestone").get(null)).blockID;
			UB_sedimentaryStone       = ((Block)z.getField("sedimentaryStone").get(null)).blockID;
			WarpDrive.debugPrint("WarpDriveConfig found UndergroundBiomes blocks " + UB_igneousStone + ", " + UB_igneousCobblestone + ", " + UB_metamorphicStone + ", " + UB_metamorphicCobblestone + ", " + UB_sedimentaryStone);
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Error loading UndergroundBiomes classes");
			e.printStackTrace();
			isUndergroundBiomesLoaded = false;
		}
		*/
	}

	private static void loadNetherOres() {
		try {
			NetherOres_count = 21;	// FIXME: extract it properly
/*			Class<?> z = Class.forName("powercrystals.netherores.ores.Ores");
			NO_netherOresCount = z.getField("values").get(null).length;
			WarpDrive.debugPrint("WarpDriveConfig found NetherOres count " + NO_netherOresCount);
			
			z = Class.forName("powercrystals.netherores.NetherOresCore");
			for (int i = 0; i < (NO_netherOresCount + 15) / 16; i++)
			{
				NO_netherOresBlock[i] = ((Block[])z.getDeclaredField("blockNetherOres").get(null))[i].blockID;
				WarpDrive.debugPrint("WarpDriveConfig found NetherOres blockId " + NO_netherOresBlock[i]);
			}*/
			NetherOres_block = new int[(NetherOres_count + 15) / 16];
			NetherOres_block[0] = 1440;
			NetherOres_block[1] = 1442;
			WarpDrive.debugPrint("WarpDriveConfig found " + NetherOres_count + " NetherOres");
		}
		catch (Exception e)
		{
			WarpDrive.print("WarpDriveConfig Error loading NetherOres classes");
			e.printStackTrace();
			isNetherOresLoaded = false;
		}
	}
	
	private static void loadThermalExpansion() {
		try {
//			TEEnergyCell = Class.forName("thermalexpansion.block.energycell.BlockEnergyCell");
//			TEFluids = Class.forName("thermalexpansion.fluid.TEFluids");
		} catch (Exception e) {
			WarpDrive.print("WarpDriveConfig Error loading ThermalExpansion classes");
			e.printStackTrace();
			isThermalExpansionLoaded = false;
		}
	}
	
	private static void loadMetallurgy() {
		try {
			Metallurgy_overworldOresBlock = new int[][] { { 905, 7}, { 905, 8}, { 905, 9}, { 905, 10}, { 905, 11}, { 905, 12}, { 906, 0}, { 906, 1}, { 906, 2}, { 906, 4}, { 906, 5}, { 906, 6}, { 906, 7}, { 906, 8}, { 906, 11}, { 906, 13}, { 906, 14} };   
			Metallurgy_netherOresBlock = new int[][] { { 903, 0}, { 903, 1}, { 903, 2}, { 903, 3}, { 903, 4}, { 903, 5}, { 903, 6}, { 903, 7}, { 903, 8}, { 903, 9} };
			Metallurgy_endOresBlock = new int[][] { { 900, 5}, { 900, 6} };   
		} catch (Exception e) {
			WarpDrive.print("WarpDriveConfig Error loading Metallurgy classes");
			e.printStackTrace();
			isMetallurgyLoaded = false;
		}
	}
	
	private static void loadAdvancedRepulsionSystems() {
		//TODO: Cant find block object
		
		try {
			//forceFieldBlocks.add(BlockForceField);
		} catch (Exception e) {
			WarpDrive.print("WarpDriveConfig Error loading AdvancedRepulsionSystems classes");
			e.printStackTrace();
			isAdvancedRepulsionSystemsLoaded = false;
		}
	}
	
	public static Block getDefaultSurfaceBlock(Random random, boolean corrupted, boolean isMoon) {
		if (isMoon) {
			if (random.nextInt(5) == 1) {
				return Blocks.netherrack;
			} else if (random.nextInt(15) == 1) {
				return Blocks.end_stone;
			}
		} else {
			if (random.nextInt(6) == 1) {
				return Blocks.netherrack;
			} else if (random.nextInt(50) == 1) {
				return Blocks.end_stone;
			}
		}
		if (corrupted && random.nextBoolean()) {
			if (isUndergroundBiomesLoaded) {
				int rnd = random.nextInt(8 + 8 + 2);
				if (rnd < 8) {
					return UB_igneousCobblestone;
				} else if (rnd < (8 + 8)) {
					return UB_metamorphicCobblestone;
				}
			}
			return Blocks.cobblestone;
		}
		if (isUndergroundBiomesLoaded) {
			int rnd = random.nextInt(8 + 8 + 8 + 3);
			if (rnd < 8) {
				return UB_igneousCobblestone;
			} else if (rnd < (8 + 8)) {
				return UB_metamorphicStone;
			}
			else if (rnd < (8 + 8 + 8)) {
				return UB_sedimentaryStone;
			}
		}
		return Blocks.stone;
	}

	public static Block getRandomSurfaceBlock(Random random, Block block, int blockMeta, boolean bedrock) {
		if (bedrock && (random.nextInt(1000) == 1)) {
			return Blocks.bedrock;
		} else if (Blocks.end_stone.isAssociatedBlock(block)) {
			return getRandomEndBlock(random, block, blockMeta);
		} else if (Blocks.netherrack.isAssociatedBlock(block)) {
			return getRandomNetherBlock(random, block, blockMeta);
		}
		return getRandomOverworldBlock(random, block, blockMeta);
	}

	public static Block getRandomOverworldBlock(Random random, Block block, int blockMeta)
	{
		if (random.nextInt(25) == 5) {
			return commonWorldGenOres.get(random.nextInt(commonWorldGenOres.size()));
		} else if (isAppliedEnergisticsLoaded && random.nextInt(750) == 1) {
			return Block.getBlockFromItem(getAEBlock("blkQuartzOre").getItem());
		} else if (random.nextInt(250) == 1) {
			return Blocks.diamond_ore;
		}
		return block;
	}

	public static Block getRandomNetherBlock(Random random, Block block, int blockMeta) {
		if (isICLoaded && (!isNetherOresLoaded) && (random.nextInt(10000) == 42)) {
			return WarpDrive.iridiumBlock;
		} else if (random.nextInt(25) == 1) {
			return Blocks.quartz_ore;
		} else if ((!isNetherOresLoaded) && (random.nextInt(100) == 13))
			return commonWorldGenOres.get(random.nextInt(commonWorldGenOres.size()));
		return block;
	}

	public static Block getRandomEndBlock(Random random, Block block, int blockMeta)
	{
		if (isICLoaded && random.nextInt(10000) == 42) {
			return WarpDrive.iridiumBlock;
		} else if (random.nextInt(200) == 13) {
			return commonWorldGenOres.get(random.nextInt(commonWorldGenOres.size()));
		}
		return block;
	}
	
}
