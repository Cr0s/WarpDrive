package cr0s.WarpDrive;

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

public class WarpDriveConfig
{
	public static WarpDriveConfig i;
	private static Configuration config;
	public static int coreID, controllerID, radarID, isolationID, airID, airgenID, gasID, laserID, miningLaserID, particleBoosterID, liftID, laserCamID, camID, monitorID, shipScannerID, cloakCoreID, cloakCoilID;
	public static int laserTreeFarmID, transporterID, transportBeaconID, powerReactorID, powerLaserID, componentID;
//
	/*
	 * The variables which store whether or not individual mods are loaded
	 */
	public static boolean isAELoaded			= false;
	public static boolean isASLoaded			= false;
	public static boolean isAEExtraLoaded		= false;
	public static boolean isICBMLoaded			= false;
	public static boolean isCCLoaded			= false;
//
	public static int CC_Computer = 0, CC_peripheral = 0, CCT_Turtle = 0, CCT_Upgraded = 0, CCT_Advanced = 0, ASP = 0, AS_Turbine = 0, ICBM_Machine = 0, ICBM_Missile = 0, MFFS_Field = 0;
	public static Set<Integer> SpaceHelmets, Jetpacks, MinerOres, MinerLogs, MinerLeaves, scannerIgnoreBlocks;
	private static Class<?> AEBlocks;
	private static Class<?> AEMaterials;
	private static Class<?> AEItems;
	public static ArrayList<int[]> CommonWorldGenOres;
	public static Item AEExtraFDI;
	
	public static boolean debugMode = false;

	// Mod config
	// Warp Core
    public static int WC_MAX_ENERGY_VALUE = 100000000;
    public static int WC_ENERGY_PER_BLOCK_MODE1 = 10; // eU
    public static int WC_ENERGY_PER_DISTANCE_MODE1 = 100; // eU
    public static int WC_ENERGY_PER_BLOCK_MODE2 = 1000; // eU
    public static int WC_ENERGY_PER_DISTANCE_MODE2 = 1000; // eU
    public static int WC_ENERGY_PER_ENTITY_TO_SPACE = 1000000; // eU
    public static int WC_MAX_JUMP_DISTANCE = 128;   // Maximum jump length value
    public static int WC_MAX_SHIP_VOLUME_ON_SURFACE = 15000;   // Maximum ship mass to jump on earth (15k blocks)
    public static int WC_MIN_SHIP_VOLUME_FOR_HYPERSPACE = 500; // Minimum ship volume value for
    public static int WC_MAX_SHIP_SIDE = 100;
    public static int WC_COOLDOWN_INTERVAL_SECONDS = 4;
    public static int WC_CORES_REGISTRY_UPDATE_INTERVAL_SECONDS = 10;
    public static int WC_ISOLATION_UPDATE_INTARVAL_SECONDS = 10;		

    // Warp Radar
    public static int WR_MAX_ENERGY_VALUE = 100000000; // 100kk eU
    
    // Particle Booster
    public static int PB_MAX_ENERGY_VALUE = 100000;
    
    // Mining Laser
	public static int		ML_MAX_BOOSTERS_NUMBER = 1;
	public static int		ML_SCAN_DELAY = 20 * 5;
	public static int		ML_MINE_DELAY = 10;
	public static int		ML_EU_PER_LAYER_SPACE = 100;
	public static int		ML_EU_PER_LAYER_EARTH = 2500;
	public static int		ML_EU_PER_BLOCK_SPACE = 10;
	public static int 		ML_EU_PER_BLOCK_EARTH = 50;
	public static double	ML_EU_MUL_SILKTOUCH = 2.5;
	public static double	ML_EU_MUL_FORTUNE   = 1.5;
	public static double	ML_MAX_SPEED   = 10;
	public static double	ML_MIN_SPEED   = 0.1;
	public static int		ML_MAX_SIZE    = 128;
	
	//Tree farm
	public static int TF_MAX_SIZE=32;
	
	//Transporter
	public static int     TR_MAX_ENERGY=10000000;
	public static boolean TR_RELATIVE_COORDS=false;
	public static double  TR_EU_PER_METRE=100;
	public static double  TR_MAX_SCAN_RANGE=4;
	public static double  TR_MAX_BOOST_MUL=4;
	
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
	
	// REACTOR MONITOR
	public static int		RM_MAX_ENERGY = 1000000;
	public static double	RM_EU_PER_HEAT = 2;
	
	public static String schemaLocation = "/home/cros/mc_site/schematics/";
	
	// Cloaking device core
	public static int CD_MAX_CLOAKING_FIELD_SIDE = 100;
	public static int CD_ENERGY_PER_BLOCK_TIER1 = 1000;
	public static int CD_ENERGY_PER_BLOCK_TIER2 = 5000; 
	public static int CD_FIELD_REFRESH_INTERVAL_SECONDS = 10;
	public static int CD_COIL_CAPTURE_BLOCKS = 5;

	public static ItemStack getAEBlock(String id)
	{
		try
		{
			Object ret = AEBlocks.getField(id).get(null);
			if (ret instanceof ItemStack)
				return (ItemStack)ret;
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Call getAEBlock failed for " + id);
		}
		return null;
	}

	public static ItemStack getAEMaterial(String id)
	{
		try
		{
			Object ret = AEMaterials.getField(id).get(null);
			if (ret instanceof ItemStack)
				return (ItemStack)ret;
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Call getAEMaterial failed for " + id);
		}
		return null;
	}

	public static ItemStack getAEItem(String id)
	{
		try
		{
			Object ret = AEItems.getField(id).get(null);
			if (ret instanceof ItemStack)
				return (ItemStack)ret;
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Call getAEItem failed for " + id);
		}
		return null;
	}

	public static void Init(Configuration configIn)
	{
		config = configIn;
	}

	public static void loadWarpDriveConfig()
	{		
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
	    
	    WC_COOLDOWN_INTERVAL_SECONDS = config.get("WarpCore", "cooldown_interval_seconds", 4).getInt(); 
	    WC_CORES_REGISTRY_UPDATE_INTERVAL_SECONDS = config.get("WarpCore", "cores_registry_update_interval", 10).getInt(); 
	    WC_ISOLATION_UPDATE_INTARVAL_SECONDS = config.get("WarpCore", "isolation_update_interval", 10).getInt();		
	    
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
		ML_SCAN_DELAY = 20 * config.get("MiningLaser", "scan_delay_seconds", 5).getInt();
		ML_MINE_DELAY = config.get("MiningLaser", "mine_delay_ticks", 10).getInt();
		ML_EU_PER_LAYER_SPACE = config.get("MiningLaser", "eu_per_layer_space", 100).getInt();
		ML_EU_PER_LAYER_EARTH = config.get("MiningLaser", "eu_per_layer_earth", 2500).getInt();	 
		ML_EU_PER_BLOCK_SPACE = config.get("MiningLaser", "eu_per_block_space", 10).getInt();
		ML_EU_PER_BLOCK_EARTH = config.get("MiningLaser", "eu_per_block_earth", 50).getInt();	  
		ML_MAX_SIZE = config.get("MiningLaser", "max_size", 128).getInt();
		ML_EU_MUL_SILKTOUCH = config.get("MiningLaser", "silktouch_power_mul", 2.5).getDouble(2.5);
		ML_EU_MUL_FORTUNE   = config.get("MiningLaser", "fortune_power_base", 1.5).getDouble(1.5);
		ML_MAX_SPEED   = config.get("MiningLaser", "max_speed_mul", 10).getDouble(10);
		ML_MIN_SPEED   = config.get("MiningLaser", "min_speed_mul", 0.1).getDouble(0.1);
		
		// Tree Farm
		TF_MAX_SIZE = config.get("TreeFarm", "max_treefarm_size", 16).getInt();
		
		// Transporter
		TR_MAX_ENERGY = config.get("Transporter", "max_energy", 1000000).getInt();	
		TR_RELATIVE_COORDS = config.get("Transporter", "relative_coords", true).getBoolean(true);
		TR_EU_PER_METRE = config.get("Transporter", "eu_per_ent_per_metre", 100).getDouble(100);
		TR_MAX_BOOST_MUL = config.get("Transporter", "max_boost", 4).getInt();
		
	}
	
	public static void Init2()
	{
		CommonWorldGenOres = new ArrayList<int[]>();
		CommonWorldGenOres.add(new int[] {Block.oreIron.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreGold.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreCoal.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreEmerald.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreLapis.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreRedstoneGlowing.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreRedstone.blockID, 0});
//
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
		shipScannerID = config.getBlock("shipscanner", 516).getInt();
		cloakCoreID = config.getBlock("cloakcore", 517).getInt();
		cloakCoilID = config.getBlock("cloakcoil", 518).getInt();
		laserTreeFarmID = config.getBlock("lasertreefarm", 519).getInt();
		transporterID = config.getBlock("transporter", 520).getInt();
		transportBeaconID = config.getBlock("transportBeacon", 521).getInt();
		powerLaserID = config.getBlock("powerLaser", 522).getInt();
		powerReactorID = config.getBlock("powerReactor",523).getInt();
		
		componentID = config.getItem("component", 21140).getInt();
		
		isCCLoaded = Loader.isModLoaded("ComputerCraft");
		if (isCCLoaded)
			loadCC();
		
		isAELoaded = Loader.isModLoaded("AppliedEnergistics");
		if (isAELoaded)
			loadAE();
		
		isAEExtraLoaded = Loader.isModLoaded("extracells");
		if (isAEExtraLoaded)
			loadAEExtra();
		
		isASLoaded = Loader.isModLoaded("AtomicScience");
		if (isASLoaded)
			loadAS();
		
		isICBMLoaded = Loader.isModLoaded("ICBM|Explosion");
		if (isICBMLoaded)
			loadICBM();
//
		MinerOres.add(Block.oreNetherQuartz.blockID);
		MinerOres.add(Block.obsidian.blockID);
		MinerOres.add(Block.web.blockID);
		MinerOres.add(Block.fence.blockID);
		//MinerOres.add(Block.torchWood.blockID);
		LoadOreDict();
		// Ignore WarpDrive blocks (which potentially will be duplicated by cheaters using ship scan/deploy)
		scannerIgnoreBlocks.add(coreID);
		scannerIgnoreBlocks.add(controllerID);

		// Do not scan ores and valuables
		for (int[] t : CommonWorldGenOres) // each element of this set is pair [id, meta]
			scannerIgnoreBlocks.add(t[0]); // we adding ID only
		
		loadWarpDriveConfig();
		config.save();
	}
	
	private static void LoadOreDict()
	{
		String[] oreNames = OreDictionary.getOreNames();
		for(String oreName: oreNames)
		{
			String lowerOreName = oreName.toLowerCase();
			if(oreName.substring(0,3).equals("ore"))
			{
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for(ItemStack i: item)
				{
					MinerOres.add(i.itemID);
					WarpDrive.debugPrint("WD: Added ore ID: "+i.itemID);
				}
			}
			if(lowerOreName.contains("log"))
			{
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for(ItemStack i: item)
				{
					MinerLogs.add(i.itemID);
					WarpDrive.debugPrint("WD: Added log ID: "+i.itemID);
				}
			}
			if(lowerOreName.contains("leave") || lowerOreName.contains("leaf"))
			{
				ArrayList<ItemStack> item = OreDictionary.getOres(oreName);
				for(ItemStack i: item)
				{
					MinerLeaves.add(i.itemID);
					WarpDrive.debugPrint("WD: Added leaf ID: "+i.itemID);
				}
			}
		}
	}

	private static void loadCC()
	{
		try
		{
			Class<?> z = Class.forName("dan200.ComputerCraft");
			CC_Computer = z.getField("computerBlockID").getInt(null);
			CC_peripheral = z.getField("peripheralBlockID").getInt(null);
			z = Class.forName("dan200.CCTurtle");
			CCT_Turtle = z.getField("turtleBlockID").getInt(null);
			CCT_Upgraded = z.getField("turtleUpgradedBlockID").getInt(null);
			CCT_Advanced = z.getField("turtleAdvancedBlockID").getInt(null);
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading CC classes AWWW SHEEEEET NIGGA");
			e.printStackTrace();
		}
	}

	private static void loadAE()
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
			WarpDrive.debugPrint("WarpDriveConfig Error loading AE classes");
			e.printStackTrace();
			isAELoaded = false;
		}
	}

	private static void loadAEExtra()
	{
		try
		{
			Class<?> z = Class.forName("extracells.ItemEnum");
			Object z1 = z.getEnumConstants()[6];
			AEExtraFDI = (Item)z1.getClass().getDeclaredMethod("getItemInstance").invoke(z1);
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading AEExtra classes");
			e.printStackTrace();
			isAEExtraLoaded = false;
		}
	}	

	private static void loadAS()
	{
		try
		{
			Class<?> z = Class.forName("atomicscience.AtomicScience");
			CommonWorldGenOres.add(new int[] {((Block)z.getField("bHeOre").get(null)).blockID, 0});
			AS_Turbine = ((Block)z.getField("bWoLun").get(null)).blockID;
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading AS classes");
			isASLoaded = false;
		}
	}

	private static void loadICBM()
	{
		try
		{
			Class<?> z = Class.forName("icbm.core.ICBMCore");
			CommonWorldGenOres.add(new int[] {((Block)z.getField("blockSulfurOre").get(null)).blockID, 0});
			z = Class.forName("icbm.explosion.ICBMExplosion");
			ICBM_Machine = ((Block)z.getField("blockMachine").get(null)).blockID;
			ICBM_Missile = ((Item)z.getField("itemMissile").get(null)).itemID;
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading ICBM classes");
			e.printStackTrace();
			isICBMLoaded = false;
		}
	}

	private static void loadMFFS()
	{
		try
		{
			Class<?> z = Class.forName("mffs.ModularForceFieldSystem");
			MFFS_Field = ((Block)z.getField("blockForceField").get(null)).blockID;
		}
		catch (Exception e)
		{
			WarpDrive.debugPrint("WarpDriveConfig Error loading MFFS classes");
			e.printStackTrace();
			isICBMLoaded = false;
		}
	}

	public static int[] getDefaultSurfaceBlock(Random random, boolean corrupted, boolean isMoon)
	{
		if (isMoon)
		{
			if (random.nextInt(666) == 1)
				return new int[] {Block.netherrack.blockID, 0};
			else if (random.nextInt(1000) == 1)
				return new int[] {Block.whiteStone.blockID, 0};
		}
		else
		{
			if (random.nextInt(50) == 1)
				return new int[] {Block.netherrack.blockID, 0};
			else if (random.nextInt(150) == 1)
				return new int[] {Block.whiteStone.blockID, 0};
		}
		if (corrupted && random.nextBoolean())
			return new int[] {Block.cobblestone.blockID, 0};
		return new int[] {Block.stone.blockID, 0};
	}

	public static int[] getRandomSurfaceBlock(Random random, int blockID, int blockMeta, boolean bedrock)
	{
		if (bedrock && random.nextInt(1000) == 1)
			return new int[] {Block.bedrock.blockID, 0};
		else if (blockID == Block.whiteStone.blockID)
			return getRandomEndBlock(random, blockID, blockMeta);
		else if (blockID == Block.netherrack.blockID)
			return getRandomNetherBlock(random, blockID, blockMeta);
		return getRandomOverworldBlock(random, blockID, blockMeta);
	}

	public static int[] getRandomOverworldBlock(Random random, int blockID, int blockMeta)
	{
		if (random.nextInt(25) == 5)
			return CommonWorldGenOres.get(random.nextInt(CommonWorldGenOres.size()));
		else if (isAELoaded && random.nextInt(750) == 1)
			return new int[] {getAEBlock("blkQuartzOre").itemID, getAEBlock("blkQuartzOre").getItemDamage()};
		else if (random.nextInt(250) == 1)
			return new int[] {Block.oreDiamond.blockID, 0};
		return new int[] {blockID, blockMeta};
	}

	public static int[] getRandomNetherBlock(Random random, int blockID, int blockMeta)
	{
		if (random.nextInt(25) == 1)
			return new int[] {Block.oreNetherQuartz.blockID, 0};
		else if (random.nextInt(100) == 13)
			return CommonWorldGenOres.get(random.nextInt(CommonWorldGenOres.size()));
		return new int[] {blockID, blockMeta};
	}

	public static int[] getRandomEndBlock(Random random, int blockID, int blockMeta)
	{
		if (random.nextInt(200) == 13)
			return CommonWorldGenOres.get(random.nextInt(CommonWorldGenOres.size()));
		return new int[] {blockID, blockMeta};
	}
}
