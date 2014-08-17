package cr0s.WarpDrive;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

import cpw.mods.fml.common.Loader;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ic2.api.item.Items;

public class WarpDriveConfig
{
	private static Configuration config;
	public static int coreID, controllerID, radarID, isolationID, airID, airgenID, gasID, laserID, miningLaserID, particleBoosterID, liftID, laserCamID, camID, monitorID, iridiumBlockID, shipScannerID, cloakCoreID, cloakCoilID;
	public static int laserTreeFarmID, transporterID, transportBeaconID, reactorLaserFocusID, reactorMonitorID, powerReactorID, powerLaserID, powerStoreID, chunkLoaderID, componentID;
	public static int helmetID, chestID, pantsID, bootsID, airCanisterID;
	
	/*
	 * The variables which store whether or not individual mods are loaded
	 */
	public static boolean isForgeMultipartLoaded			= false;
	public static boolean isGregLoaded						= false;
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
	
	/*
	 * The variables that control which recipes should be loaded
	 */
	public static boolean recipesIC2			= true;
//
	public static Method forgeMultipart_helper_createTileFromNBT = null;
	public static Method forgeMultipart_helper_sendDescPacket = null;
	public static Method forgeMultipart_tileMultipart_onChunkLoad = null;

	public static int[] IC2_Air;
	public static int[] IC2_Empty;
	public static int IC2_RubberWood;
	public static ItemStack IC2_Resin;
	public static Item IC2_fluidCell;
	public static int CC_Computer = 0, CC_peripheral = 0, CC_Floppy = 0, CCT_Turtle = 0, CCT_Upgraded = 0, CCT_Advanced = 0;
	public static int GT_Ores = 0, GT_Granite = 0, GT_Machine = 0;
	public static int ASP = 0;
	public static int AS_Turbine = 0, AS_deuteriumCell = 0;
	public static int ICBM_Machine = 0, ICBM_Missile = 0, ICBM_Explosive = 0;
	public static int GS_ultimateLappack = 0;
	public static int UB_igneousStone = 0, UB_igneousCobblestone = 0, UB_metamorphicStone = 0, UB_metamorphicCobblestone = 0, UB_sedimentaryStone = 0;
	public static int NetherOres_count;
	public static int[] NetherOres_block;
	public static int[][] Metallurgy_overworldOresBlock;
	public static int[][] Metallurgy_netherOresBlock;
	public static int[][] Metallurgy_endOresBlock;
	public static ArrayList<Integer> forceFieldBlocks;

	public static Set<Integer> SpaceHelmets, Jetpacks, MinerOres, MinerLogs, MinerLeaves, scannerIgnoreBlocks;
	private static Class<?> AEBlocks;
	private static Class<?> AEMaterials;
	private static Class<?> AEItems;
	public static ArrayList<int[]> CommonWorldGenOres;
	public static Item AEExtra_fluidDrive;
	public static Block AEExtra_certusQuartzTank;
	
	public static boolean debugMode = false;

	// Mod config
	// General
	public static int G_SPACE_PROVIDER_ID = 14;
	public static int G_SPACE_DIMENSION_ID = 2;
	public static int G_HYPERSPACE_PROVIDER_ID = 15;
	public static int G_HYPERSPACE_DIMENSION_ID = 3;

	// Warp Core
    public static int WC_MAX_ENERGY_VALUE = 100000000;
    public static int WC_ENERGY_PER_BLOCK_MODE1 = 10; // eU
    public static int WC_ENERGY_PER_DISTANCE_MODE1 = 100; // eU
    public static int WC_ENERGY_PER_BLOCK_MODE2 = 1000; // eU
    public static int WC_ENERGY_PER_DISTANCE_MODE2 = 1000; // eU
    public static int WC_ENERGY_PER_ENTITY_TO_SPACE = 1000000; // eU
    public static int WC_MAX_JUMP_DISTANCE = 128;   // Maximum jump length value
    public static int WC_MAX_SHIP_VOLUME_ON_SURFACE = 3000;   // Maximum ship mass to jump on earth
    public static int WC_MIN_SHIP_VOLUME_FOR_HYPERSPACE = 1200; // Minimum ship volume value for hyperspace travel
    public static int WC_MAX_SHIP_SIDE = 127;
    public static int WC_COOLDOWN_INTERVAL_SECONDS = 4;	// FIXME update me
    public static int WC_COLLISION_TOLERANCE_BLOCKS = 3;
    public static int WC_WARMUP_SHORTJUMP_SECONDS = 10;
    public static int WC_WARMUP_LONGJUMP_SECONDS = 30;
    public static int WC_WARMUP_RANDOM_TICKS = 60;
    public static int WC_CORES_REGISTRY_UPDATE_INTERVAL_SECONDS = 10;
    public static int WC_ISOLATION_UPDATE_INTERVAL_SECONDS = 10;		

    // Warp Radar
    public static int WR_MAX_ENERGY_VALUE = 100000000; // 100kk eU
    
    // Particle Booster
    public static int PB_MAX_ENERGY_VALUE = 100000;
    
    // Mining Laser
	public static int		ML_MAX_BOOSTERS_NUMBER = 1;
	public static int		ML_WARMUP_DELAY_TICKS = 20;
	public static int		ML_SCAN_DELAY_TICKS = 10;
	public static int		ML_MINE_DELAY_TICKS = 3;
	public static int		ML_EU_PER_LAYER_SPACE = 2000;
	public static int		ML_EU_PER_LAYER_EARTH = 10000;
	public static int		ML_EU_PER_BLOCK_SPACE = 500;
	public static int 		ML_EU_PER_BLOCK_EARTH = 2500;
	public static double	ML_EU_MUL_ORESONLY = 4.0;
	public static double	ML_EU_MUL_SILKTOUCH = 2.5;
	public static double	ML_EU_MUL_FORTUNE   = 2.5;
//	public static double	ML_MAX_SPEED   = 10;
//	public static double	ML_MIN_SPEED   = 0.1;
	public static int		ML_MAX_RADIUS  = 6;
	
	//Tree farm
	public static int		TF_MAX_SIZE=32;
	
	//Air generator
	public static int		AG_RF_PER_CANISTER = 80;
	
	//Transporter
	public static int		TR_MAX_ENERGY = 10000000;
	public static boolean	TR_RELATIVE_COORDS = false;
	public static double	TR_EU_PER_METRE = 100;
	public static double	TR_MAX_SCAN_RANGE = 4;
	public static double	TR_MAX_BOOST_MUL = 4;
	
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

	// POWER REACTOR
	public static int		PR_MAX_ENERGY = 100000000;
	public static int		PR_TICK_TIME  = 5;
	public static int		PR_MAX_LASERS = 3;
	
	// POWER STORE
	public static int		PS_MAX_ENERGY = 1000000;
	
	// REACTOR MONITOR
	public static int		RM_MAX_ENERGY = 1000000;
	public static double	RM_EU_PER_HEAT = 2;
	
	public static String schemaLocation = "/home/cros/mc_site/schematics/";
	
	// Cloaking device core
	public static int		CD_MAX_CLOAKING_FIELD_SIDE = 100;
	public static int		CD_ENERGY_PER_BLOCK_TIER1 = 1000;
	public static int		CD_ENERGY_PER_BLOCK_TIER2 = 5000; 
	public static int		CD_FIELD_REFRESH_INTERVAL_SECONDS = 10;
	public static int		CD_COIL_CAPTURE_BLOCKS = 5;
	
	// Laser Lift
	public static int		LL_MAX_ENERGY = 2400;
	public static int		LL_LIFT_ENERGY = 800;
	public static int		LL_TICK_RATE = 10;
	
	// Chunk Loader
	public static int		CL_MAX_ENERGY = 1000000;
	public static int		CL_MAX_DISTANCE = 2;
	public static int		CL_RF_PER_CHUNKTICK = 320;

	public static ItemStack getIC2Item(String id) {
		return Items.getItem(id);
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
		G_SPACE_PROVIDER_ID = config.get("General", "space_provider_id", 14).getInt();
		G_SPACE_DIMENSION_ID = config.get("General", "space_dimension_id", -2).getInt();
		G_HYPERSPACE_PROVIDER_ID = config.get("General", "hyperspace_provider_id", 15).getInt();
		G_HYPERSPACE_DIMENSION_ID = config.get("General", "hyperspace_dimension_id", -3).getInt();
		
		// Warp Core
		WC_MAX_ENERGY_VALUE = config.get("WarpCore", "max_energy_value", 100000000).getInt();
		WC_ENERGY_PER_BLOCK_MODE1 = config.get("WarpCore", "energy_per_block_mode1", 10).getInt();
		WC_ENERGY_PER_DISTANCE_MODE1 = config.get("WarpCore", "energy_per_distance_mode1", 100).getInt();
	    WC_ENERGY_PER_DISTANCE_MODE2 = config.get("WarpCore", "energy_per_distance_mode2", 1000).getInt();
	    WC_ENERGY_PER_BLOCK_MODE2 = config.get("WarpCore", "energy_per_block_mode2", 1000).getInt();
	    WC_ENERGY_PER_ENTITY_TO_SPACE = config.get("WarpCore", "energy_per_entity_to_space", 1000000).getInt();
	    WC_MAX_JUMP_DISTANCE = config.get("WarpCore", "max_jump_distance", 128).getInt();
	    WC_MAX_SHIP_VOLUME_ON_SURFACE = config.get("WarpCore", "max_ship_volume_on_surface", 15000).getInt();   // Maximum ship mass to jump on earth (15k blocks)
	    WC_MIN_SHIP_VOLUME_FOR_HYPERSPACE = config.get("WarpCore", "min_ship_volume_for_hyperspace", 500).getInt(); // Minimum ship volume value for hyper space
	    WC_MAX_SHIP_SIDE = config.get("WarpCore", "max_ship_side", 100).getInt(); 
	    WC_COLLISION_TOLERANCE_BLOCKS = config.get("WarpCore", "collision_tolerance_blocks", 5).getInt();
	    
	    WC_COOLDOWN_INTERVAL_SECONDS = config.get("WarpCore", "cooldown_interval_seconds", 4).getInt();
	    WC_WARMUP_SHORTJUMP_SECONDS = config.get("WarpCore", "warmup_shortjump_seconds", 10).getInt();
	    WC_WARMUP_LONGJUMP_SECONDS = config.get("WarpCore", "warmup_longjump_seconds", 30).getInt();
	    
	    WC_CORES_REGISTRY_UPDATE_INTERVAL_SECONDS = config.get("WarpCore", "cores_registry_update_interval", 10).getInt(); 
	    WC_ISOLATION_UPDATE_INTERVAL_SECONDS = config.get("WarpCore", "isolation_update_interval", 10).getInt();		
	    
	    // Warp Radar
	    WR_MAX_ENERGY_VALUE = config.get("WarpRadar", "max_energy_value", 100000000).getInt();
	    
	    // Particle Booster
	    PB_MAX_ENERGY_VALUE = config.get("ParticleBooster", "max_energy_value", 100000).getInt();
		
		// Laser Emitter
		LE_MAX_BOOSTERS_NUMBER = config.get("LaserEmitter", "max_boosters_number", 10).getInt();
		LE_MAX_LASER_ENERGY = config.get("LaserEmitter", "max_laser_energy", 4000000).getInt();
		LE_EMIT_DELAY_TICKS = config.get("LaserEmitter", "emit_delay_ticks", 60).getInt();
		LE_EMIT_SCAN_DELAY_TICKS = config.get("LaserEmitter", "emit_scan_delay_ticks", 10).getInt();
		
		// Laser Emitter tweaks
		LE_COLLECT_ENERGY_MULTIPLIER = config.get("LaserEmitterTweaks", "collect_energy_multiplier", 0.6D).getDouble(0.6D);
		LE_BEAM_LENGTH_PER_ENERGY_DIVIDER = config.get("LaserEmitterTweaks", "beam_length_per_energy_divider", 5000).getInt();
		LE_ENTITY_HIT_SET_ON_FIRE_TIME = config.get("LaserEmitterTweaks", "entity_hit_set_on_fire_time", 100).getInt();
		LE_ENTITY_HIT_DAMAGE_PER_ENERGY_DIVIDER = config.get("LaserEmitterTweaks", "entity_hit_damage_per_energy_divider", 10000).getInt();
		LE_ENTITY_HIT_EXPLOSION_LASER_ENERGY = config.get("LaserEmitterTweaks", "entity_hit_explosion_laser_energy", 1000000).getInt();
		LE_BLOCK_HIT_CONSUME_ENERGY = config.get("LaserEmitterTweaks", "block_hit_consume_energy", 70000).getInt();
		LE_BLOCK_HIT_CONSUME_ENERGY_PER_BLOCK_RESISTANCE = config.get("LaserEmitterTweaks", "block_hit_consume_energy_per_block_resistance", 1000).getInt();
		LE_BLOCK_HIT_CONSUME_ENERGY_PER_DISTANCE = config.get("LaserEmitterTweaks", "block_hit_consume_energy_per_distance", 10).getInt();
		
		// Cloaking device core
		CD_MAX_CLOAKING_FIELD_SIDE = config.get("CloakingDevice", "max_cloaking_field_side", 100).getInt();
		CD_ENERGY_PER_BLOCK_TIER1 = config.get("CloakingDevice", "energy_per_block_tier1", 125).getInt();
		CD_ENERGY_PER_BLOCK_TIER2 = config.get("CloakingDevice", "energy_per_block_tier2", 500).getInt();	
		CD_FIELD_REFRESH_INTERVAL_SECONDS = config.get("CloakingDevice", "field_refresh_interval_seconds", 3).getInt();	
		CD_COIL_CAPTURE_BLOCKS = config.get("CloakingDevice", "coil_capture_blocks", 5).getInt();
		
		// Dark's modifications
		debugMode = config.get("Dark's stuff", "debug_mode", false).getBoolean(false);
		schemaLocation = config.get("Dark's stuff", "schematic_location", schemaLocation).getString();
		
	    // Mining Laser
		ML_MAX_BOOSTERS_NUMBER = config.get("MiningLaser", "max_boosters_number", 1).getInt();
		ML_WARMUP_DELAY_TICKS = config.get("MiningLaser", "warmup_delay_ticks", 20).getInt();
		ML_SCAN_DELAY_TICKS = config.get("MiningLaser", "scan_delay_ticks", 10).getInt();
		ML_MINE_DELAY_TICKS = config.get("MiningLaser", "mine_delay_ticks", 3).getInt();
		ML_EU_PER_LAYER_SPACE = config.get("MiningLaser", "eu_per_layer_space", 2000).getInt();
		ML_EU_PER_LAYER_EARTH = config.get("MiningLaser", "eu_per_layer_earth", 10000).getInt();	 
		ML_EU_PER_BLOCK_SPACE = config.get("MiningLaser", "eu_per_block_space", 500).getInt();
		ML_EU_PER_BLOCK_EARTH = config.get("MiningLaser", "eu_per_block_earth", 2500).getInt();	  
		ML_MAX_RADIUS = config.get("MiningLaser", "max_radius", 5).getInt();
		ML_EU_MUL_ORESONLY = config.get("MiningLaser", "oresonly_power_mul", 4.0).getDouble(4.0);
		ML_EU_MUL_SILKTOUCH = config.get("MiningLaser", "silktouch_power_mul", 2.5).getDouble(2.5);
		ML_EU_MUL_FORTUNE   = config.get("MiningLaser", "fortune_power_base", 2.5).getDouble(2.5);
//		ML_MAX_SPEED   = config.get("MiningLaser", "max_speed_mul", 10).getDouble(10);
//		ML_MIN_SPEED   = config.get("MiningLaser", "min_speed_mul", 0.1).getDouble(0.1);
		
		// Tree Farm
		TF_MAX_SIZE = config.get("TreeFarm", "max_treefarm_size", 16).getInt();
		
		// Transporter
		TR_MAX_ENERGY = config.get("Transporter", "max_energy", 1000000).getInt();	
		TR_RELATIVE_COORDS = config.get("Transporter", "relative_coords", true).getBoolean(true);
		TR_EU_PER_METRE = config.get("Transporter", "eu_per_ent_per_metre", 100).getDouble(100);
		TR_MAX_BOOST_MUL = config.get("Transporter", "max_boost", 4).getInt();

		// Reactor
		PR_MAX_ENERGY = config.get("Reactor", "max_energy", 100000000).getInt();
		PR_TICK_TIME  = config.get("Reactor", "ticks_per_update", 5).getInt();
		PR_MAX_LASERS = config.get("Reactor", "max_lasers", 7).getInt();
		
		// Store
		PS_MAX_ENERGY = config.get("Power Store", "max_energy", 10000000).getInt();
		
		// Air generator
		AG_RF_PER_CANISTER = config.get("Air Generator", "energy_per_canister", 20).getInt();
		
		// Reactor monitor
		RM_MAX_ENERGY = config.get("Reactor Monitor", "max_rm_energy", 1000000).getInt();
		RM_EU_PER_HEAT = config.get("Reactor Monitor", "eu_per_heat", 2).getDouble(2);
		
		// Recipes config
		recipesIC2 = config.get("Recipes", "ic2_recipes",true).getBoolean(true);
	}
	
	public static void load() {
		CommonWorldGenOres = new ArrayList<int[]>(30);
		CommonWorldGenOres.add(new int[] {Block.oreIron.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreGold.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreCoal.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreEmerald.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreLapis.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreRedstoneGlowing.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreRedstone.blockID, 0});
		
		forceFieldBlocks = new ArrayList<Integer>();

		SpaceHelmets = new HashSet<Integer>();
		Jetpacks = new HashSet<Integer>();
		MinerOres = new HashSet<Integer>();
		MinerLogs = new HashSet<Integer>();
		MinerLeaves = new HashSet<Integer>();
		scannerIgnoreBlocks = new HashSet<Integer>();
		config.load();
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
		laserTreeFarmID = config.getBlock("lasertreefarm", 519).getInt();
		transporterID = config.getBlock("transporter", 520).getInt();
		transportBeaconID = config.getBlock("transportBeacon", 521).getInt();
		reactorMonitorID = config.getBlock("reactorMonitor", 522).getInt();
		powerLaserID = config.getBlock("powerLaser", 523).getInt();
		powerReactorID = config.getBlock("powerReactor", 524).getInt();
		powerStoreID = config.getBlock("powerStore", 525).getInt();
		chunkLoaderID = config.getBlock("chunkLoader", 526).getInt();
		
		reactorLaserFocusID = config.getItem("reactorLaserFocus", 8700).getInt();
		componentID = config.getItem("component", 8701).getInt();
		airCanisterID = config.getItem("airCanisterFull", 8706).getInt();
		helmetID = config.getItem("helmet", 8702).getInt();
		chestID = config.getItem("chest", 8703).getInt();
		pantsID = config.getItem("pants", 8704).getInt();
		bootsID = config.getItem("boots", 8705).getInt();
		
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
		
		isGregLoaded = Loader.isModLoaded("gregtech_addon");
		if (isGregLoaded)
			loadGT();
		
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
//
		MinerOres.add(Block.oreNetherQuartz.blockID);
		MinerOres.add(Block.obsidian.blockID);
		MinerOres.add(Block.web.blockID);
		MinerOres.add(Block.fence.blockID);
		MinerOres.add(Block.torchWood.blockID);
		MinerOres.add(Block.glowStone.blockID);
		
		// Ignore WarpDrive blocks (which potentially will be duplicated by cheaters using ship scan/deploy)
		scannerIgnoreBlocks.add(coreID);
		scannerIgnoreBlocks.add(controllerID);
		scannerIgnoreBlocks.add(iridiumBlockID);
		
		if (isICLoaded) {
			scannerIgnoreBlocks.add(Items.getItem("mfsUnit").itemID);
			scannerIgnoreBlocks.add(Items.getItem("mfeUnit").itemID);
			scannerIgnoreBlocks.add(Items.getItem("cesuUnit").itemID);
			scannerIgnoreBlocks.add(Items.getItem("batBox").itemID);
		}
		if (isICBMLoaded) {
			scannerIgnoreBlocks.add(ICBM_Explosive);
		}
		if (isCCLoaded) {
			 scannerIgnoreBlocks.add(CC_Computer);
			 scannerIgnoreBlocks.add(CCT_Turtle);
			 scannerIgnoreBlocks.add(CCT_Upgraded);
			 scannerIgnoreBlocks.add(CCT_Advanced);
		}
		// Do not deploy ores and valuables
		for (int[] t : CommonWorldGenOres) {// each element of this set is pair [id, meta]
			scannerIgnoreBlocks.add(t[0]); // we adding ID only
		}
		
		loadWarpDriveConfig();
		config.save();
	}
	
	public static void postInit() {
		LoadOreDict();
	}
	
	private static void LoadOreDict() {
		String[] oreNames = OreDictionary.getOreNames();
		for(String oreName: oreNames) {
			String lowerOreName = oreName.toLowerCase();
			if (oreName.substring(0,3).equals("ore")) {
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for(ItemStack i: item) {
					MinerOres.add(i.itemID);
					WarpDrive.debugPrint("WD: Added ore ID: "+i.itemID);
				}
			}
			if (lowerOreName.contains("log")) {
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for(ItemStack i: item) {
					MinerLogs.add(i.itemID);
					WarpDrive.debugPrint("WD: Added log ID: "+i.itemID);
				}
			}
			if (lowerOreName.contains("leave") || lowerOreName.contains("leaf")) {
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for(ItemStack i: item) {
					MinerLeaves.add(i.itemID);
					WarpDrive.debugPrint("WD: Added leaf ID: "+i.itemID);
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
		ASP = Items.getItem("solarPanel").itemID;
		SpaceHelmets.add(Items.getItem("hazmatHelmet").itemID);
		SpaceHelmets.add(Items.getItem("quantumHelmet").itemID);
		Jetpacks.add(Items.getItem("jetpack").itemID);
		Jetpacks.add(Items.getItem("electricJetpack").itemID);
		IC2_Air = new int[] {Items.getItem("airCell").itemID, Items.getItem("airCell").getItemDamage()};
		IC2_Empty = new int[] {Items.getItem("cell").itemID, Items.getItem("cell").getItemDamage()};
		ItemStack rubberWood = Items.getItem("rubberWood");
		IC2_Resin = Items.getItem("resin");
		if(rubberWood != null) {
			IC2_RubberWood = rubberWood.itemID;
		}
		ItemStack ore = Items.getItem("uraniumOre");
		if (ore != null) CommonWorldGenOres.add(new int[] {ore.itemID, ore.getItemDamage()});
		ore = Items.getItem("copperOre");
		if (ore != null) CommonWorldGenOres.add(new int[] {ore.itemID, ore.getItemDamage()});
		ore = Items.getItem("tinOre");
		if (ore != null) CommonWorldGenOres.add(new int[] {ore.itemID, ore.getItemDamage()});
		ore = Items.getItem("leadOre");
		if (ore != null) CommonWorldGenOres.add(new int[] {ore.itemID, ore.getItemDamage()});

		MinerOres.add(Items.getItem("rubberWood").itemID);
		IC2_fluidCell = Items.getItem("FluidCell").getItem();
	}

	private static void loadCC()
	{
		try
		{
			Class<?> z = Class.forName("dan200.computercraft.ComputerCraft");
			CC_Computer = z.getField("computerBlockID").getInt(null);
			CC_peripheral = z.getField("peripheralBlockID").getInt(null);
			CC_Floppy = z.getField("diskItemID").getInt(null);
			CCT_Turtle = z.getField("turtleBlockID").getInt(null);
			CCT_Upgraded = z.getField("turtleUpgradedBlockID").getInt(null);
			CCT_Advanced = z.getField("turtleAdvancedBlockID").getInt(null);
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading ComputerCraft classes");
			e.printStackTrace();
		}
	}

	private static void loadGT()
	{
		try
		{
			Class<?> z = Class.forName("gregtechmod.GT_Mod");
			int[] t = (int[])z.getField("sBlockIDs").get(null);
			GT_Machine = t[1];
			GT_Ores = t[2]; // meta 1-15 = ores
			GT_Granite = t[5]; // 0 - black, 1 - black cobble, 8 - red, 9 - red cobble
			MinerOres.add(GT_Ores);
			//MinerOres.add(GT_Granite);
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading GT classes");
			e.printStackTrace();
			isGregLoaded = false;
		}
	}

	private static void loadAppliedEnergistics()
	{
		try
		{
			AEBlocks = Class.forName("appeng.api.Blocks");
			AEMaterials = Class.forName("appeng.api.Materials");
			AEItems = Class.forName("appeng.api.Items");
			MinerOres.add(((ItemStack)AEBlocks.getField("blkQuartzOre").get(null)).itemID);
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
			Class<?> z = Class.forName("advsolar.common.AdvancedSolarPanel");
			ASP = z.getField("idAdv").getInt(null);
			SpaceHelmets.add(((Item)z.getField("advancedSolarHelmet").get(null)).itemID);
			SpaceHelmets.add(((Item)z.getField("hybridSolarHelmet").get(null)).itemID);
			SpaceHelmets.add(((Item)z.getField("ultimateSolarHelmet").get(null)).itemID);
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading ASP classes");
			e.printStackTrace();
			isAdvSolPanelLoaded = false;
		}
	}

	private static void loadAtomicScience() {
		try {
			Class<?> z = Class.forName("resonantinduction.atomic.Atomic");
			CommonWorldGenOres.add(new int[] {((Block)z.getField("blockUraniumOre").get(null)).blockID, 0});
			AS_Turbine = ((Block)z.getField("blockElectricTurbine").get(null)).blockID;
			AS_deuteriumCell = ((Item)z.getField("itemDeuteriumCell").get(null)).itemID;
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Error loading AS classes");
			isAtomicScienceLoaded = false;
		}
	}

	private static void loadICBM() {
		try {
			Class<?> z = Class.forName("icbm.core.ICBMCore");
			CommonWorldGenOres.add(new int[] {((Block)z.getField("blockSulfurOre").get(null)).blockID, 0});
			z = Class.forName("icbm.explosion.ICBMExplosion");
			ICBM_Machine = ((Block)z.getField("blockMachine").get(null)).blockID;
			ICBM_Missile = ((Item)z.getField("itemMissile").get(null)).itemID;
			ICBM_Explosive = ((Block)z.getField("blockExplosive").get(null)).blockID;
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Error loading ICBM classes");
			e.printStackTrace();
			isICBMLoaded = false;
		}
	}

	private static void loadMFFS() {
		try {
			Class<?> z = Class.forName("mffs.ModularForceFieldSystem");
			int blockId = ((Block)z.getField("blockForceField").get(null)).blockID;
			forceFieldBlocks.add(blockId);
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Error loading MFFS classes");
			e.printStackTrace();
			isICBMLoaded = false;
		}
	}

	private static void loadGraviSuite() {
		try {
			Class<?> z = Class.forName("gravisuite.GraviSuite");
			if (z.getField("ultimateSolarHelmet").get(null) != null)
				SpaceHelmets.add(((Item)z.getField("ultimateSolarHelmet").get(null)).itemID);
			Jetpacks.add(z.getField("advJetpackID").getInt(null) + 256);
			Jetpacks.add(z.getField("graviChestPlateID").getInt(null) + 256);
			GS_ultimateLappack = z.getField("ultimateLappackID").getInt(null) + 256;
		} catch (Exception e) {
			WarpDrive.debugPrint("WarpDriveConfig Error loading GS classes");
			e.printStackTrace();
			isGraviSuiteLoaded = false;
		}
	}

	private static void loadUndergroundBiomes() {
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
			System.out.println("WarpDriveConfig found " + NetherOres_count + " NetherOres");
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Error loading NetherOres classes");
			e.printStackTrace();
			isNetherOresLoaded = false;
		}
	}
	
	private static void loadThermalExpansion() {
		try {
//			TEEnergyCell = Class.forName("thermalexpansion.block.energycell.BlockEnergyCell");
//			TEFluids = Class.forName("thermalexpansion.fluid.TEFluids");
		} catch (Exception e) {
			System.out.println("WarpDriveConfig Error loading ThermalExpansion classes");
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
			System.out.println("WarpDriveConfig Error loading Metallurgy classes");
			e.printStackTrace();
			isMetallurgyLoaded = false;
		}
	}
	
	private static void loadAdvancedRepulsionSystems() {
		try {
			Class<?> z = Class.forName("mods.immibis.ars.ARSMod");
			int fieldBlockId = ((Block)z.getField("MFFSFieldblock").get(null)).blockID;
			forceFieldBlocks.add(fieldBlockId);
		} catch (Exception e) {
			System.out.println("WarpDriveConfig Error loading AdvancedRepulsionSystems classes");
			e.printStackTrace();
			isAdvancedRepulsionSystemsLoaded = false;
		}
	}
	
	public static int[] getDefaultSurfaceBlock(Random random, boolean corrupted, boolean isMoon) {
		if (isMoon) {
			if (isGregLoaded && (random.nextInt(100) == 1)) {
				if (random.nextBoolean()) {
					return new int[] {GT_Granite, (corrupted && random.nextBoolean())?1:0};
				} else {
					return new int[] {GT_Granite, (corrupted && random.nextBoolean())?9:8};
				}
			} else if (random.nextInt(5) == 1) {
				return new int[] {Block.netherrack.blockID, 0};
			} else if (random.nextInt(15) == 1) {
				return new int[] {Block.whiteStone.blockID, 0};
			}
		} else {
			if (isGregLoaded && (random.nextInt(25) == 1)) {
				if (random.nextBoolean()) {
					return new int[] {GT_Granite, (corrupted && random.nextBoolean())?1:0};
				} else {
					return new int[] {GT_Granite, (corrupted && random.nextBoolean())?9:8};
				}
			} else if (random.nextInt(6) == 1) {
				return new int[] {Block.netherrack.blockID, 0};
			} else if (random.nextInt(50) == 1) {
				return new int[] {Block.whiteStone.blockID, 0};
			}
		}
		if (corrupted && random.nextBoolean()) {
			if (isUndergroundBiomesLoaded) {
				int rnd = random.nextInt(8 + 8 + 2);
				if (rnd < 8) {
					return new int[] {UB_igneousCobblestone, rnd};
				} else if (rnd < (8 + 8)) {
					return new int[] {UB_metamorphicCobblestone, rnd - 8};
				}
			}
			return new int[] {Block.cobblestone.blockID, 0};
		}
		if (isUndergroundBiomesLoaded) {
			int rnd = random.nextInt(8 + 8 + 8 + 3);
			if (rnd < 8) {
				return new int[] {UB_igneousStone, rnd};
			} else if (rnd < (8 + 8)) {
				return new int[] {UB_metamorphicStone, rnd - 8};
			}
			else if (rnd < (8 + 8 + 8)) {
				return new int[] {UB_sedimentaryStone, rnd - 8 - 8};
			}
		}
		return new int[] {Block.stone.blockID, 0};
	}

	public static int[] getRandomSurfaceBlock(Random random, int blockID, int blockMeta, boolean bedrock) {
		if (bedrock && (random.nextInt(1000) == 1)) {
			return new int[] {Block.bedrock.blockID, 0};
		} else if (blockID == GT_Granite) {
			if ((blockMeta == 0) || (blockMeta == 1)) {
				int[] t;
				t = getRandomOverworldBlock(random, blockID, blockMeta);
				if (t[0] == blockID)
					t = getRandomOverworldBlock(random, blockID, blockMeta);
				if (t[0] == blockID)
					t = getRandomEndBlock(random, blockID, blockMeta);
				return t;
			} else if ((blockMeta == 8) || (blockMeta == 9)) {
				int[] t;
				t = getRandomOverworldBlock(random, blockID, blockMeta);
				if (t[0] == blockID)
					t = getRandomEndBlock(random, blockID, blockMeta);
				if (t[0] == blockID)
					t = getRandomOverworldBlock(random, blockID, blockMeta);
				return t;
			}
		} else if (blockID == Block.whiteStone.blockID) {
			return getRandomEndBlock(random, blockID, blockMeta);
		} else if (blockID == Block.netherrack.blockID) {
			return getRandomNetherBlock(random, blockID, blockMeta);
		}
		return getRandomOverworldBlock(random, blockID, blockMeta);
	}

	public static int[] getRandomOverworldBlock(Random random, int blockID, int blockMeta)
	{
		if (random.nextInt(25) == 5) {
			return CommonWorldGenOres.get(random.nextInt(CommonWorldGenOres.size()));
		} else if (isMetallurgyLoaded && (random.nextInt(25) == 1)) {
			return Metallurgy_overworldOresBlock[random.nextInt(Metallurgy_overworldOresBlock.length)];
		} else if (isAppliedEnergisticsLoaded && random.nextInt(750) == 1) {
			return new int[] {getAEBlock("blkQuartzOre").itemID, getAEBlock("blkQuartzOre").getItemDamage()};
		} else if (random.nextInt(250) == 1) {
			return new int[] {Block.oreDiamond.blockID, 0};
		} else if (!isNetherOresLoaded && (random.nextInt(10000) == 42)) {
			return new int[] {iridiumBlockID, 0};
		} else if (isGregLoaded) {
			if (random.nextInt(50) == 1)
				return new int[] {GT_Ores, 5}; //Bauxite S /* Stone/Iron/Diamod pick | +S = Silktouch recommended */
			else if (random.nextInt(50) == 1)
				return new int[] {GT_Ores, 1}; //Galena S
			else if (random.nextInt(100) == 1)
				return new int[] {GT_Ores, 8}; //Sphalerite S+S
			else if (random.nextInt(250) == 1)
				return new int[] {GT_Ores, 13}; //Tetrahedrite I
			else if (random.nextInt(250) == 1)
				return new int[] {GT_Ores, 14}; //Cassiterite I
			else if (random.nextInt(250) == 1)
				return new int[] {GT_Ores, 15}; //Nickel I
			else if (random.nextInt(500) == 1)
				return new int[] {GT_Ores, 3}; //Ruby I+S
			else if (random.nextInt(500) == 1)
				return new int[] {GT_Ores, 4}; //Sapphire I+S
			else if (random.nextInt(2000) == 1)
				return new int[] {GT_Ores, 2}; //Iridium D+S
		}
		return new int[] {blockID, blockMeta};
	}

	public static int[] getRandomNetherBlock(Random random, int blockID, int blockMeta) {
		if (isICLoaded && (!isNetherOresLoaded) && (random.nextInt(10000) == 42)) {
			return new int[] {iridiumBlockID, 0};
		} else if (isNetherOresLoaded && (random.nextInt(25) == 1)) {
			int rnd = random.nextInt(NetherOres_count);
			return new int[] {NetherOres_block[rnd / 16], rnd % 16};
		} else if (isMetallurgyLoaded && (random.nextInt(25) == 1)) {
			return Metallurgy_netherOresBlock[random.nextInt(Metallurgy_netherOresBlock.length)];
		} else if (random.nextInt(25) == 1) {
			return new int[] {Block.oreNetherQuartz.blockID, 0};
		} else if (isGregLoaded)  {
			if (random.nextInt(100) == 1)
				return new int[] {GT_Ores, 6}; //Pyrite S+S
			else if (random.nextInt(100) == 1)
				return new int[] {GT_Ores, 8}; //Sphalerite S+S
			else if (random.nextInt(500) == 1)
				return new int[] {GT_Ores, 7}; //Cinnabar I+S
		} else if ((!isNetherOresLoaded) && (random.nextInt(100) == 13))
			return CommonWorldGenOres.get(random.nextInt(CommonWorldGenOres.size()));
		return new int[] {blockID, blockMeta};
	}

	public static int[] getRandomEndBlock(Random random, int blockID, int blockMeta)
	{
		if (isICLoaded && random.nextInt(10000) == 42) {
			return new int[] { iridiumBlockID, 0 };
		} else if (isGregLoaded) {
			if (random.nextInt(250) == 1)
				return new int[] {GT_Ores, 9}; //Tungstate I
			else if (random.nextInt(500) == 1)
				return new int[] {GT_Ores, 12}; //Sodalite I+S
			else if (random.nextInt(500) == 1)
				return new int[] {GT_Ores, 10}; //Cooperite=Sheldonite D
			else if (random.nextInt(1000) == 1)
				return new int[] {GT_Ores, 11}; //Olivine D+S
		} else if (isMetallurgyLoaded && (random.nextInt(25) == 1)) {
			return Metallurgy_endOresBlock[random.nextInt(Metallurgy_endOresBlock.length)];
		} else if (random.nextInt(200) == 13) {
			return CommonWorldGenOres.get(random.nextInt(CommonWorldGenOres.size()));
		}
		return new int[] {blockID, blockMeta};
	}

	public static boolean isAirBlock(World worldObj, int id, int x, int y, int z) {
		return id == 0 || Block.blocksList[id] == null || Block.blocksList[id].isAirBlock(worldObj, x, y, z);
    }
}
