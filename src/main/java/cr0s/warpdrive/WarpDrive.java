package cr0s.warpdrive;

import java.util.List;

import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cr0s.warpdrive.block.BlockAirGenerator;
import cr0s.warpdrive.block.BlockChunkLoader;
import cr0s.warpdrive.block.BlockLaser;
import cr0s.warpdrive.block.BlockLaserMedium;
import cr0s.warpdrive.block.TileEntityAbstractChunkLoading;
import cr0s.warpdrive.block.TileEntityAirGenerator;
import cr0s.warpdrive.block.TileEntityChunkLoader;
import cr0s.warpdrive.block.TileEntityLaser;
import cr0s.warpdrive.block.TileEntityLaserMedium;
import cr0s.warpdrive.block.building.BlockShipScanner;
import cr0s.warpdrive.block.building.TileEntityShipScanner;
import cr0s.warpdrive.block.collection.BlockLaserTreeFarm;
import cr0s.warpdrive.block.collection.BlockMiningLaser;
import cr0s.warpdrive.block.collection.TileEntityLaserTreeFarm;
import cr0s.warpdrive.block.collection.TileEntityMiningLaser;
import cr0s.warpdrive.block.detection.BlockCamera;
import cr0s.warpdrive.block.detection.BlockCloakingCoil;
import cr0s.warpdrive.block.detection.BlockCloakingCore;
import cr0s.warpdrive.block.detection.BlockMonitor;
import cr0s.warpdrive.block.detection.BlockRadar;
import cr0s.warpdrive.block.detection.BlockWarpIsolation;
import cr0s.warpdrive.block.detection.TileEntityCamera;
import cr0s.warpdrive.block.detection.TileEntityCloakingCore;
import cr0s.warpdrive.block.detection.TileEntityMonitor;
import cr0s.warpdrive.block.detection.TileEntityRadar;
import cr0s.warpdrive.block.energy.BlockEnanReactorCore;
import cr0s.warpdrive.block.energy.BlockEnanReactorLaser;
import cr0s.warpdrive.block.energy.BlockEnergyBank;
import cr0s.warpdrive.block.energy.BlockIC2reactorLaserMonitor;
import cr0s.warpdrive.block.energy.TileEntityEnanReactorCore;
import cr0s.warpdrive.block.energy.TileEntityEnanReactorLaser;
import cr0s.warpdrive.block.energy.TileEntityEnergyBank;
import cr0s.warpdrive.block.energy.TileEntityIC2reactorLaserMonitor;
import cr0s.warpdrive.block.movement.BlockLift;
import cr0s.warpdrive.block.movement.BlockShipController;
import cr0s.warpdrive.block.movement.BlockShipCore;
import cr0s.warpdrive.block.movement.BlockTransporter;
import cr0s.warpdrive.block.movement.TileEntityLift;
import cr0s.warpdrive.block.movement.TileEntityShipController;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import cr0s.warpdrive.block.movement.TileEntityTransporter;
import cr0s.warpdrive.block.passive.BlockAir;
import cr0s.warpdrive.block.passive.BlockDecorative;
import cr0s.warpdrive.block.passive.BlockGas;
import cr0s.warpdrive.block.passive.BlockHighlyAdvancedMachine;
import cr0s.warpdrive.block.passive.BlockIridium;
import cr0s.warpdrive.block.passive.BlockTransportBeacon;
import cr0s.warpdrive.block.passive.ItemBlockDecorative;
import cr0s.warpdrive.block.weapon.BlockLaserCamera;
import cr0s.warpdrive.command.CommandDebug;
import cr0s.warpdrive.command.CommandGenerate;
import cr0s.warpdrive.command.CommandInvisible;
import cr0s.warpdrive.command.CommandJumpgates;
import cr0s.warpdrive.command.CommandSpace;
import cr0s.warpdrive.conf.WarpDriveConfig;
import cr0s.warpdrive.data.CamerasRegistry;
import cr0s.warpdrive.data.CloakManager;
import cr0s.warpdrive.data.JumpgatesRegistry;
import cr0s.warpdrive.data.ShipCoresRegistry;
import cr0s.warpdrive.item.ItemIC2reactorLaserFocus;
import cr0s.warpdrive.item.ItemAirCanisterFull;
import cr0s.warpdrive.item.ItemHelmet;
import cr0s.warpdrive.item.ItemComponent;
import cr0s.warpdrive.item.ItemUpgrade;
import cr0s.warpdrive.network.PacketHandler;
import cr0s.warpdrive.render.CameraOverlay;
import cr0s.warpdrive.world.BiomeSpace;
import cr0s.warpdrive.world.HyperSpaceProvider;
import cr0s.warpdrive.world.HyperSpaceWorldGenerator;
import cr0s.warpdrive.world.SpaceProvider;
import cr0s.warpdrive.world.SpaceWorldGenerator;

@Mod(modid = WarpDrive.MODID, name = "WarpDrive", version = WarpDrive.VERSION, dependencies = "after:IC2API;" + " after:CoFHCore;" + " after:ComputerCraft;"
		+ " after:OpenComputer;" + " after:CCTurtle;" + " after:gregtech_addon;" + " after:AppliedEnergistics;" + " after:AdvancedSolarPanel;"
		+ " after:AtomicScience;" + " after:ICBM|Explosion;" + " after:MFFS;" + " after:GraviSuite;" + " after:UndergroundBiomes;" + " after:NetherOres")
/**
 * @author Cr0s
 */
public class WarpDrive implements LoadingCallback {
	public static final String MODID = "WarpDrive";
	public static final String VERSION = "@version@";
	
	public static Block blockShipCore;
	public static Block blockShipController;
	public static Block blockRadar;
	public static Block blockWarpIsolation;
	public static Block blockAirGenerator;
	public static Block blockLaser;
	public static Block blockLaserCamera;
	public static Block blockCamera;
	public static Block blockMonitor;
	public static Block blockLaserMedium;
	public static Block blockMiningLaser;
	public static Block blockLaserTreeFarm;
	public static Block blockLift;
	public static Block blockShipScanner;
	public static Block blockCloakingCore;
	public static Block blockCloakingCoil;
	public static Block blockTransporter;
	public static Block blockIC2reactorLaserMonitor;
	public static Block blockEnanReactorCore;
	public static Block blockEnanReactorLaser;
	public static Block blockEnergyBank;
	public static Block blockAir;
	public static Block blockGas;
	public static Block blockIridium;
	public static Block blockHighlyAdvancedMachine;
	public static Block blockTransportBeacon;
	public static Block blockChunkLoader;
	public static BlockDecorative blockDecorative;

	public static Item itemIC2reactorLaserFocus;
	public static ItemComponent itemComponent;
	public static ItemUpgrade itemUpgrade;

	public static ArmorMaterial armorMaterial = EnumHelper.addArmorMaterial("WARP", 5, new int[] { 1, 3, 2, 1 }, 15);
	public static ItemHelmet itemHelmet;
	public static ItemAirCanisterFull itemAirCanisterFull;

	public static BiomeGenBase spaceBiome;
	public SpaceWorldGenerator spaceWorldGenerator;
	public HyperSpaceWorldGenerator hyperSpaceWorldGenerator;
	public World space;
	public World hyperSpace;

	// Client settings
	public static float normalFOV = 70.0F;
	public static float normalSensitivity = 1.0F;

	public static CreativeTabs creativeTabWarpDrive = new CreativeTabWarpDrive("Warpdrive", "Warpdrive").setBackgroundImageName("warpdrive:creativeTab");

	@Instance(WarpDrive.MODID)
	public static WarpDrive instance;
	@SidedProxy(clientSide = "cr0s.warpdrive.client.ClientProxy", serverSide = "cr0s.warpdrive.CommonProxy")
	public static CommonProxy proxy;

	public static ShipCoresRegistry shipCores;
	public static JumpgatesRegistry jumpgates;
	public static CloakManager cloaks;

	public static CamerasRegistry cameras;
	public boolean isOverlayEnabled = false;
	public int overlayType = 0;
	public static int zoomIndex = 0;
	public String debugMessage = "";

	public static WarpDrivePeripheralHandler peripheralHandler = null;

	public static String defHelpStr = "help(\"functionName\"): returns help for the function specified";
	public static String defEnergyStr = "getEnergyLevel(): returns currently contained energy, max contained energy";
	public static String defUpgradeStr = "upgrades(): returns a list of currently installed upgrades";

	public static Logger logger;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		WarpDriveConfig.preInit(new Configuration(event.getSuggestedConfigurationFile()));
		
		logger = event.getModLog();
		
		// TODO: clarify best approach
		// option 1: we register values when opening a monitor => balance issue with cascading monitors
		// option 2: we record values at boot, and stick to them => starting bad, remains bad + changing config won't work until client gets restarted
		if (FMLCommonHandler.instance().getSide().isClient()) {
			Minecraft mc = Minecraft.getMinecraft();
		
			normalFOV = mc.gameSettings.fovSetting;
			normalSensitivity = mc.gameSettings.mouseSensitivity;
			logger.info("FOV is " + normalFOV + " Sensitivity is " + normalSensitivity);
		}
		
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		PacketHandler.init();
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		WarpDriveConfig.load();

		// CORE CONTROLLER
		blockShipController = new BlockShipController(0, Material.rock);

		GameRegistry.registerBlock(blockShipController, "blockShipController");
		GameRegistry.registerTileEntity(TileEntityShipController.class, MODID + ":blockShipController");

		// WARP CORE
		blockShipCore = new BlockShipCore(0, Material.rock);

		GameRegistry.registerBlock(blockShipCore, "blockShipCore");
		GameRegistry.registerTileEntity(TileEntityShipCore.class, MODID + ":blockShipCore");

		// WARP RADAR
		blockRadar = new BlockRadar(0, Material.rock);

		GameRegistry.registerBlock(blockRadar, "blockRadar");
		GameRegistry.registerTileEntity(TileEntityRadar.class, MODID + ":blockRadar");

		// WARP ISOLATION
		blockWarpIsolation = new BlockWarpIsolation(0, Material.rock);

		GameRegistry.registerBlock(blockWarpIsolation, "blockWarpIsolation");

		// AIR GENERATOR
		blockAirGenerator = new BlockAirGenerator(0, Material.rock);

		GameRegistry.registerBlock(blockAirGenerator, "blockAirGenerator");
		GameRegistry.registerTileEntity(TileEntityAirGenerator.class, MODID + ":blockAirGenerator");

		// AIR BLOCK
		blockAir = new BlockAir();

		GameRegistry.registerBlock(blockAir, "blockAir");

		// GAS BLOCK
		blockGas = new BlockGas();

		GameRegistry.registerBlock(blockGas, "blockGas");

		// LASER EMITTER
		blockLaser = new BlockLaser(0, Material.rock);

		GameRegistry.registerBlock(blockLaser, "blockLaser");
		GameRegistry.registerTileEntity(TileEntityLaser.class, MODID + ":blockLaser");

		// LASER EMITTER WITH CAMERA
		blockLaserCamera = new BlockLaserCamera(0, Material.rock);

		GameRegistry.registerBlock(blockLaserCamera, "blockLaserCamera");

		// CAMERA
		blockCamera = new BlockCamera(0, Material.rock);

		GameRegistry.registerBlock(blockCamera, "blockCamera");
		GameRegistry.registerTileEntity(TileEntityCamera.class, MODID + ":blockCamera");

		// MONITOR
		blockMonitor = new BlockMonitor();

		GameRegistry.registerBlock(blockMonitor, "blockMonitor");
		GameRegistry.registerTileEntity(TileEntityMonitor.class, MODID + ":blockMonitor");

		// MINING LASER
		blockMiningLaser = new BlockMiningLaser(0, Material.rock);

		GameRegistry.registerBlock(blockMiningLaser, "blockMiningLaser");
		GameRegistry.registerTileEntity(TileEntityMiningLaser.class, MODID + ":blockMiningLaser");

		// LASER TREE FARM
		blockLaserTreeFarm = new BlockLaserTreeFarm(0, Material.rock);

		GameRegistry.registerBlock(blockLaserTreeFarm, "blockLaserTreeFarm");
		GameRegistry.registerTileEntity(TileEntityLaserTreeFarm.class, MODID + ":blockLaserTreeFarm");

		// LASER MEDIUM
		blockLaserMedium = new BlockLaserMedium(0, Material.rock);

		GameRegistry.registerBlock(blockLaserMedium, "blockLaserMedium");
		GameRegistry.registerTileEntity(TileEntityLaserMedium.class, MODID + ":blockLaserMedium");

		// LIFT
		blockLift = new BlockLift(0, Material.rock);

		GameRegistry.registerBlock(blockLift, "blockLift");
		GameRegistry.registerTileEntity(TileEntityLift.class, MODID + ":blockLift");

		// IRIDIUM BLOCK
		blockIridium = new BlockIridium();

		GameRegistry.registerBlock(blockIridium, "blockIridium");

		// HIGHLY ADVANCED MACHINE BLOCK
		if (WarpDriveConfig.isIndustrialCraft2loaded) {
			blockHighlyAdvancedMachine = new BlockHighlyAdvancedMachine();

			GameRegistry.registerBlock(blockHighlyAdvancedMachine, "blockHighlyAdvancedMachine");
		}

		// SHIP SCANNER
		blockShipScanner = new BlockShipScanner(0, Material.rock);

		GameRegistry.registerBlock(blockShipScanner, "blockShipScanner");
		GameRegistry.registerTileEntity(TileEntityShipScanner.class, MODID + ":blockShipScanner");

		// CLOAKING DEVICE CORE
		blockCloakingCore = new BlockCloakingCore(0, Material.rock);

		GameRegistry.registerBlock(blockCloakingCore, "blockCloakingCore");
		GameRegistry.registerTileEntity(TileEntityCloakingCore.class, MODID + ":blockCloakingCore");

		// CLOAKING DEVICE COIL
		blockCloakingCoil = new BlockCloakingCoil(0, Material.rock);

		GameRegistry.registerBlock(blockCloakingCoil, "blockCloakingCoil");

		// TRANSPORTER
		blockTransporter = new BlockTransporter(Material.rock);

		GameRegistry.registerBlock(blockTransporter, "blockTransporter");
		GameRegistry.registerTileEntity(TileEntityTransporter.class, MODID + ":blockTransporter");

		// REACTOR MONITOR
		if (WarpDriveConfig.isIndustrialCraft2loaded) {
			blockIC2reactorLaserMonitor = new BlockIC2reactorLaserMonitor(Material.rock);
			
			GameRegistry.registerBlock(blockIC2reactorLaserMonitor, "blockIC2reactorLaserMonitor");
			GameRegistry.registerTileEntity(TileEntityIC2reactorLaserMonitor.class, MODID + ":blockIC2reactorLaserMonitor");
		}

		// TRANSPORT BEACON
		blockTransportBeacon = new BlockTransportBeacon();

		GameRegistry.registerBlock(blockTransportBeacon, "blockTransportBeacon");

		// POWER REACTOR, LASER, STORE
		blockEnanReactorCore = new BlockEnanReactorCore();
		GameRegistry.registerBlock(blockEnanReactorCore, "blockEnanReactorCore");
		GameRegistry.registerTileEntity(TileEntityEnanReactorCore.class, MODID + ":blockEnanReactorCore");

		blockEnanReactorLaser = new BlockEnanReactorLaser();
		GameRegistry.registerBlock(blockEnanReactorLaser, "blockEnanReactorLaser");
		GameRegistry.registerTileEntity(TileEntityEnanReactorLaser.class, MODID + ":blockEnanReactorLaser");

		blockEnergyBank = new BlockEnergyBank();
		GameRegistry.registerBlock(blockEnergyBank, "blockEnergyBank");
		GameRegistry.registerTileEntity(TileEntityEnergyBank.class, MODID + ":blockEnergyBank");

		// CHUNK LOADER
		blockChunkLoader = new BlockChunkLoader();
		GameRegistry.registerBlock(blockChunkLoader, "blockChunkLoader");
		GameRegistry.registerTileEntity(TileEntityChunkLoader.class, MODID + ":blockChunkLoader");

		// DECORATIVE
		blockDecorative = new BlockDecorative();
		GameRegistry.registerBlock(blockDecorative, ItemBlockDecorative.class, "blockDecorative");

		// REACTOR LASER FOCUS
		if (WarpDriveConfig.isIndustrialCraft2loaded) {
			itemIC2reactorLaserFocus = new ItemIC2reactorLaserFocus();
			GameRegistry.registerItem(itemIC2reactorLaserFocus, "itemIC2reactorLaserFocus");
		}

		// COMPONENT ITEMS
		itemComponent = new ItemComponent();
		GameRegistry.registerItem(itemComponent, "itemComponent");

		itemHelmet = new ItemHelmet(armorMaterial, 0);
		GameRegistry.registerItem(itemHelmet, "itemHelmet");

		itemAirCanisterFull = new ItemAirCanisterFull();
		GameRegistry.registerItem(itemAirCanisterFull, "itemAirCanisterFull");

		itemUpgrade = new ItemUpgrade();
		GameRegistry.registerItem(itemUpgrade, "itemUpgrade");
		
		
		proxy.registerEntities();
		ForgeChunkManager.setForcedChunkLoadingCallback(instance, instance);

		spaceWorldGenerator = new SpaceWorldGenerator();
		GameRegistry.registerWorldGenerator(spaceWorldGenerator, 0);
		hyperSpaceWorldGenerator = new HyperSpaceWorldGenerator();
		GameRegistry.registerWorldGenerator(hyperSpaceWorldGenerator, 0);

		registerSpaceDimension();
		registerHyperSpaceDimension();

		MinecraftForge.EVENT_BUS.register(new SpaceEventHandler());

		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			creativeTabWarpDrive.setBackgroundImageName("items.png");
			MinecraftForge.EVENT_BUS.register(new CameraOverlay(Minecraft.getMinecraft()));
		}

		if (WarpDriveConfig.isComputerCraftLoaded) {
			peripheralHandler = new WarpDrivePeripheralHandler();
			peripheralHandler.register();
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		space = DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID);
		hyperSpace = DimensionManager.getWorld(WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID);

		WarpDriveConfig.postInit();

		if (WarpDriveConfig.isIndustrialCraft2loaded && WarpDriveConfig.G_ENABLE_IC2_RECIPES) {
			initIC2Recipes();
		}
		if (WarpDriveConfig.isIndustrialCraft2loaded && WarpDriveConfig.G_ENABLE_HARD_IC2_RECIPES) {
			initHardIC2Recipes();
		}
		if (WarpDriveConfig.G_ENABLE_VANILLA_RECIPES) {
			initVanillaRecipes();
		}

		shipCores = new ShipCoresRegistry();
		jumpgates = new JumpgatesRegistry();
		cameras = new CamerasRegistry();
	}

	private static void initVanillaRecipes() {
		itemComponent.registerRecipes();
		blockDecorative.initRecipes();
		itemUpgrade.initRecipes();

		// WarpCore
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockShipCore), false, "ipi", "ici", "idi",
				'i', Items.iron_ingot,
				'p', itemComponent.getItemStack(6),
				'c', itemComponent.getItemStack(2),
				'd', Items.diamond));

		// Controller
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockShipController), false, "ici", "idi", "iii",
				'i', Items.iron_ingot,
				'c', itemComponent.getItemStack(5),
				'd', Items.diamond));

		// Radar
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockRadar), false, "ggg", "pdc", "iii",
				'i', Items.iron_ingot,
				'c', itemComponent.getItemStack(5),
				'p', itemComponent.getItemStack(6),
				'g', Blocks.glass,
				'd', Items.diamond));

		// Isolation Block
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWarpIsolation), false, "igi", "geg", "igi",
				'i', Items.iron_ingot,
				'g', Blocks.glass,
				'e', Items.ender_pearl));

		// Air generator
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockAirGenerator), false, "ibi", "i i", "ipi",
				'i', Items.iron_ingot,
				'b', Blocks.iron_bars,
				'p', itemComponent.getItemStack(6)));

		// Laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockLaser), false, "ili", "iri", "ici",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				'c', itemComponent.getItemStack(5),
				'l', itemComponent.getItemStack(3),
				'p', itemComponent.getItemStack(6)));

		// Mining laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMiningLaser), false, "ici", "iti", "ili",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				't', itemComponent.getItemStack(1),
				'c', itemComponent.getItemStack(5),
				'l', itemComponent.getItemStack(3)));

		// Tree farm laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockLaserTreeFarm), false, "ili", "sts", "ici",
				'i', Items.iron_ingot,
				's', "treeSapling",
				't', itemComponent.getItemStack(1), 
				'c', itemComponent.getItemStack(5),
				'l', itemComponent.getItemStack(3)));

		// Laser Lift
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockLift), false, "ipi", "rtr", "ili",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				't', itemComponent.getItemStack(1),
				'l', itemComponent.getItemStack(3),
				'p', itemComponent.getItemStack(6)));

		// Transporter
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTransporter), false, "iii", "ptc", "iii",
				'i', Items.iron_ingot,
				't', itemComponent.getItemStack(1),
				'c', itemComponent.getItemStack(5),
				'p', itemComponent.getItemStack(6)));

		// Particle Booster
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockLaserMedium), false, "ipi", "rgr", "iii", 
				'i', Items.iron_ingot,
				'r', Items.redstone,
				'g', Blocks.glass,
				'p', itemComponent.getItemStack(6)));

		// Camera
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCamera), false, "ngn", "i i", "ici",
				'i', Items.iron_ingot,
				'n', Items.gold_nugget,
				'g', Blocks.glass,
				'c', itemComponent.getItemStack(5)));

		// LaserCamera
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(blockLaserCamera), blockCamera, blockLaser));

		// Monitor
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMonitor), false, "ggg", "iti", "ici",
				'i', Items.iron_ingot,
				't', Blocks.torch,
				'g', Blocks.glass,
				'c', itemComponent.getItemStack(5)));

		// Cloaking device
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCloakingCore), false, "ipi", "lrl", "ici",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				'l', itemComponent.getItemStack(3),
				'c', itemComponent.getItemStack(5),
				'p', itemComponent.getItemStack(6)));

		// Cloaking coil
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCloakingCoil), false, "ini", "rdr", "ini",
				'i', Items.iron_ingot,
				'd', Items.diamond,
				'r', Items.redstone,
				'n', Items.gold_nugget));

		// Power Laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockEnanReactorLaser), false, "iii", "ilg", "ici",
				'i', Items.iron_ingot,
				'g', Blocks.glass,
				'c', itemComponent.getItemStack(5),
				'l', itemComponent.getItemStack(3)));

		// Power Reactor
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockEnanReactorCore), false, "ipi", "gog", "ici",
				'i', Items.iron_ingot,
				'g', Blocks.glass,
				'o', itemComponent.getItemStack(4),
				'c', itemComponent.getItemStack(5),
				'p', itemComponent.getItemStack(6)));

		// Power Store
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockEnergyBank), false, "ipi", "isi", "ici",
				'i', Items.iron_ingot,
				's', itemComponent.getItemStack(7),
				'c', itemComponent.getItemStack(5),
				'p', itemComponent.getItemStack(6)));

		// Transport Beacon
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTransportBeacon), false, " e ", "ldl", " s ",
				'e', Items.ender_pearl,
				'l', "dyeBlue",
				'd', Items.diamond,
				's', Items.stick));

		// Chunk Loader
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockChunkLoader), false, "ipi", "ici", "ifi",
				'i', Items.iron_ingot,
				'p', itemComponent .getItemStack(6),
				'c', itemComponent.getItemStack(0),
				'f', itemComponent.getItemStack(5)));

		// Helmet
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemHelmet), false, "iii", "iwi", "gcg",
				'i', Items.iron_ingot,
				'w', Blocks.wool,
				'g', Blocks.glass,
				'c', itemComponent.getItemStack(8)));
	}

	private static void initIC2Recipes() {
		ItemStack advancedAlloy = WarpDriveConfig.getModItemStack("IC2", "itemPartAlloy", -1).copy();
		ItemStack iridiumAlloy = WarpDriveConfig.getModItemStack("IC2", "itemPartIridium", -1).copy();
		ItemStack advancedMachine = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 12).copy();
		ItemStack miner = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 7).copy();
		ItemStack magnetizer = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 9).copy();
		ItemStack fiberGlassCable = WarpDriveConfig.getModItemStack("IC2", "itemCable", 9).copy();
		ItemStack circuit = WarpDriveConfig.getModItemStack("IC2", "itemPartCircuit", -1).copy();
		ItemStack advancedCircuit = WarpDriveConfig.getModItemStack("IC2", "itemPartCircuitAdv", -1).copy();
		ItemStack ironPlate = WarpDriveConfig.getModItemStack("IC2", "itemPlates", 4).copy();
		ItemStack mfe = WarpDriveConfig.getModItemStack("IC2", "blockElectric", 1).copy();
		
		GameRegistry.addRecipe(new ItemStack(blockShipCore), "ici", "cmc", "ici",
				'i', iridiumAlloy,
				'm', advancedMachine,
				'c', advancedCircuit);

		GameRegistry.addRecipe(new ItemStack(blockShipController), "iic", "imi", "cii",
				'i', iridiumAlloy,
				'm', advancedMachine,
				'c', advancedCircuit);

		GameRegistry.addRecipe(new ItemStack(blockRadar), "ifi", "imi", "imi",
				'i', iridiumAlloy,
				'm', advancedMachine,
				'f', WarpDriveConfig.getModItemStack("IC2", "itemFreq", -1));

		GameRegistry.addRecipe(new ItemStack(blockWarpIsolation), "iii", "idi", "iii",
				'i', WarpDriveConfig.getModItemStack("IC2", "itemPartIridium", -1),
				'm', advancedMachine,
				'd', Blocks.diamond_block);

		GameRegistry.addRecipe(new ItemStack(blockAirGenerator), "lcl", "lml", "lll",
				'l', Blocks.leaves,
				'm', advancedMachine,
				'c', advancedCircuit);

		GameRegistry.addRecipe(new ItemStack(blockLaser), "sss", "ama", "aaa",
				'm', advancedMachine,
				'a', advancedAlloy,
				's', advancedCircuit);

		GameRegistry.addRecipe(new ItemStack(blockMiningLaser), "aaa", "ama", "ccc",
				'c', advancedCircuit,
				'a', advancedAlloy,
				'm', miner);

		GameRegistry.addRecipe(new ItemStack(blockLaserMedium), "afc", "ama", "cfa",
				'c', advancedCircuit,
				'a', advancedAlloy,
				'f', fiberGlassCable,
				'm', mfe);

		GameRegistry.addRecipe(new ItemStack(blockLift), "aca", "ama", "a#a",
				'c', advancedCircuit,
				'a', WarpDriveConfig.getModItemStack("IC2", "itemPartAlloy", -1),
				'm', magnetizer);

		GameRegistry.addRecipe(new ItemStack(blockIridium), "iii", "iii", "iii",
				'i', iridiumAlloy);

		GameRegistry.addShapelessRecipe(new ItemStack(iridiumAlloy.getItem(), 9), new ItemStack(blockIridium));

		GameRegistry.addRecipe(new ItemStack(blockLaserCamera), "imi", "cec", "#k#",
				'i', iridiumAlloy,
				'm', advancedMachine,
				'c', advancedCircuit,
				'e', blockLaser,
				'k', blockCamera);

		GameRegistry.addRecipe(new ItemStack(blockCamera), "cgc", "gmg", "cgc",
				'm', advancedMachine,
				'c', advancedCircuit,
				'g', Blocks.glass);

		GameRegistry.addRecipe(new ItemStack(blockMonitor), "gcg", "gmg", "ggg",
				'm', advancedMachine,
				'c', advancedCircuit,
				'g', Blocks.glass);

		GameRegistry.addRecipe(new ItemStack(blockShipScanner), "sgs", "mma", "amm",
				'm', advancedMachine,
				'a', advancedAlloy,
				's', advancedCircuit,
				'g', Blocks.glass);

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockLaserTreeFarm), false, new Object[] { "cwc", "wmw", "cwc",
				'c', circuit,
				'w', "logWood",
				'm', blockMiningLaser }));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTransporter), false, new Object[] { "ece", "imi", "iei",
				'e', Items.ender_pearl,
				'c', circuit,
				'i', ironPlate,
				'm', advancedMachine }));

		if (WarpDriveConfig.isIndustrialCraft2loaded) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemIC2reactorLaserFocus), false, new Object[] { " p ", "pdp", " p ",
					'p', ironPlate,
					'd', "gemDiamond" }));

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockIC2reactorLaserMonitor), false, new Object[] { "pdp", "dmd", "pdp",
					'p', ironPlate,
					'd', "gemDiamond",
					'm', mfe }));
		}

		GameRegistry.addRecipe(new ItemStack(blockCloakingCore), "imi", "mcm", "imi",
				'i', blockIridium,
				'c', blockCloakingCoil,
				'm', advancedMachine);

		GameRegistry.addRecipe(new ItemStack(blockCloakingCoil), "iai", "aca", "iai",
				'i', iridiumAlloy,
				'c', advancedCircuit,
				'a', advancedAlloy);
	}

	private static void initHardIC2Recipes() {
		ItemStack advancedAlloy = WarpDriveConfig.getModItemStack("IC2", "itemPartAlloy", -1).copy();
		ItemStack iridiumAlloy = WarpDriveConfig.getModItemStack("IC2", "itemPartIridium", -1).copy();
		ItemStack advancedMachine = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 12).copy();
		ItemStack magnetizer = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 9).copy();
		ItemStack fiberGlassCable = WarpDriveConfig.getModItemStack("IC2", "itemCable", 9).copy();
		ItemStack mfe = WarpDriveConfig.getModItemStack("IC2", "blockElectric", 1).copy();
		ItemStack mfsu = WarpDriveConfig.getModItemStack("IC2", "blockElectric", 2).copy();
		ItemStack energiumDust = WarpDriveConfig.getModItemStack("IC2", "itemDust2", 2).copy();
		ItemStack crystalmemory = WarpDriveConfig.getModItemStack("IC2", "itemcrystalmemory", -1).copy();
		ItemStack itemHAMachine = new ItemStack(blockHighlyAdvancedMachine).copy();
		
		GameRegistry.addRecipe(new ItemStack(blockShipCore),"uau", "tmt", "uau",
				'a', advancedAlloy,
				't', WarpDriveConfig.getModItemStack("IC2", "blockMachine2", 0), // Teleporter
				'm', itemHAMachine,
				'u', mfsu);
		
		if (WarpDriveConfig.isOpenComputersLoaded) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockShipController), false, new Object[] { "aha", "cmc", "apa", // With OC Adapter
					'a', advancedAlloy,
					'm', itemHAMachine,
					'c', "circuitAdvanced",
					'h', crystalmemory,
					'p', WarpDriveConfig.getModItemStack("OpenComputers", "adapter", -1)}));
		} else if (WarpDriveConfig.isComputerCraftLoaded) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockShipController), false, new Object[] { "aha", "cmc", "apa", // With CC Modem
					'a', advancedAlloy,
					'm', itemHAMachine,
					'c', "circuitAdvanced",
					'h', crystalmemory,
					'p', WarpDriveConfig.getModItemStack("ComputerCraft", "CC-Cable", 1)}));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockShipController), false, new Object[] { "aha", "cmc", "aca",
				'a', advancedAlloy,
				'm', itemHAMachine,
				'c', "circuitAdvanced",
				'h', crystalmemory}));
		}
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockRadar), false, new Object[] { "afa", "cmc", "aca",
				'a', advancedAlloy,
				'm', itemHAMachine,
				'c', "circuitAdvanced",
				'f', WarpDriveConfig.getModItemStack("IC2", "itemFreq", -1)}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWarpIsolation), false, new Object[] { "sls", "lml", "sls",
				's', "plateDenseSteel",
				'l', "plateDenseLead",
				'm', itemHAMachine}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockAirGenerator), false, new Object[] { "lel", "vmv", "lcl",
				'l', Blocks.leaves, 
				'm', WarpDriveConfig.getModItemStack("IC2", "blockMachine", 0),
				'c', "circuitBasic",
				'e', WarpDriveConfig.getModItemStack("IC2", "blockMachine", 5), // Compressor
				'v', WarpDriveConfig.getModItemStack("IC2", "reactorVent", -1)}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockLaser), false, new Object[] { "aca", "cmc", "ala",
				'm', advancedMachine,
				'a', advancedAlloy,
				'c', "circuitAdvanced",
				'l', WarpDriveConfig.getModItemStack("IC2", "itemToolMiningLaser", -1)}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMiningLaser), false, new Object[] { "pcp", "pap", "plp",
				'c', "circuitAdvanced",
				'p', advancedAlloy,
				'a', WarpDriveConfig.getModItemStack("IC2", "blockMachine2", 11), // Advanced Miner
				'l', blockLaser}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockLaserMedium), false, new Object[] { "efe", "aca", "ama",
				'c', "circuitAdvanced",
				'a', advancedAlloy,
				'f', fiberGlassCable,
				'e', energiumDust,
				'm', mfe}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockLift), false, new Object[] { "aca", "ama", "aea",
				'c', "circuitAdvanced",
				'a', advancedAlloy,
				'm', magnetizer,
				'e', energiumDust}));

		GameRegistry.addRecipe(new ItemStack(blockIridium), "iii", "iii", "iii",
				'i', iridiumAlloy);

		GameRegistry.addShapelessRecipe(new ItemStack(iridiumAlloy.getItem(), 9), new ItemStack(blockIridium));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockLaserCamera), false, new Object[] { "ala", "sss", "aca",
				'a', advancedAlloy,
				's', "circuitAdvanced",
				'l', blockLaser,
				'c', blockCamera}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCamera), false, new Object[] { "aed", "cma", "aga",
				'a', advancedAlloy,
				'e', WarpDriveConfig.getModItemStack("IC2", "itemRecipePart", 1), // Electric Motor
				'd', "gemDiamond",
				'c', crystalmemory,
				'm', advancedMachine,
				'g', WarpDriveConfig.getModItemStack("IC2", "itemCable", 2)}));


		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMonitor), false, new Object[] { "ala", "aca", "aga",
				'a', advancedAlloy,
				'l', Blocks.redstone_lamp,
				'c', "circuitAdvanced",
				'g', "paneGlassColorless" }));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockShipScanner), false, new Object[] { "ici", "isi", "mcm",
				'm', mfsu,
				'i', iridiumAlloy,
				'c', "circuitAdvanced",
				's', WarpDriveConfig.getModItemStack("IC2", "blockMachine2", 7) })); // Scanner

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockLaserTreeFarm), false, new Object[] { "awa", "cmc", "asa",
				'a', advancedAlloy,
				'c', "circuitAdvanced",
				'w', "logWood",
				'm', blockMiningLaser,
				's', WarpDriveConfig.getModItemStack("IC2", "itemToolChainsaw", -1) }));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTransporter), false, new Object[] { "aea", "ctc", "ama",
				'a', advancedAlloy,
				'e', Items.ender_pearl,
				'c', "circuitAdvanced",
				'm', advancedMachine,
				't', WarpDriveConfig.getModItemStack("IC2", "blockMachine2", 0) })); // Teleporter

		// IC2 is loaded for this recipe set
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemIC2reactorLaserFocus), false, new Object[] { "a a", " d ", "a a",
				'a', advancedAlloy,
				'd', "gemDiamond" }));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockIC2reactorLaserMonitor), false, new Object[] { "pdp", "dmd", "pdp",
				'p', advancedAlloy,
				'd', "gemDiamond",
				'm', mfe }));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCloakingCore), false, new Object[] { "ici", "cmc", "igi",
				'i', blockIridium,
				'c', blockCloakingCoil,
				'm', blockHighlyAdvancedMachine,
				'g', "circuitAdvanced" }));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCloakingCoil), false, new Object[] { "iai", "ccc", "iai",
				'i', iridiumAlloy,
				'c', WarpDriveConfig.getModItemStack("IC2", "itemRecipePart", 0), // Coil
				'a', advancedAlloy })); 

		GameRegistry.addRecipe(new ItemStack(blockHighlyAdvancedMachine), "iii", "imi", "iii",
				'i', iridiumAlloy,
				'm', advancedMachine);
	}

	private static void registerSpaceDimension() {
		spaceBiome = (new BiomeSpace(24)).setColor(0).setDisableRain().setBiomeName("Space");
		DimensionManager.registerProviderType(WarpDriveConfig.G_SPACE_PROVIDER_ID, SpaceProvider.class, true);
		DimensionManager.registerDimension(WarpDriveConfig.G_SPACE_DIMENSION_ID, WarpDriveConfig.G_SPACE_PROVIDER_ID);
	}

	private static void registerHyperSpaceDimension() {
		DimensionManager.registerProviderType(WarpDriveConfig.G_HYPERSPACE_PROVIDER_ID, HyperSpaceProvider.class, true);
		DimensionManager.registerDimension(WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID, WarpDriveConfig.G_HYPERSPACE_PROVIDER_ID);
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		cloaks = new CloakManager();
		MinecraftForge.EVENT_BUS.register(new CloakChunkWatcher());

		event.registerServerCommand(new CommandGenerate());
		event.registerServerCommand(new CommandSpace());
		event.registerServerCommand(new CommandInvisible());
		event.registerServerCommand(new CommandJumpgates());
		event.registerServerCommand(new CommandDebug());
	}

	public Ticket registerChunkLoadTE(TileEntityAbstractChunkLoading te, boolean refreshLoading) {
		World worldObj = te.getWorldObj();
		if (ForgeChunkManager.ticketCountAvailableFor(this, worldObj) > 0) {
			Ticket t = ForgeChunkManager.requestTicket(this, worldObj, Type.NORMAL);
			if (t != null) {
				te.giveTicket(t); // FIXME calling the caller is a bad idea
				if (refreshLoading)
					te.refreshLoading();
				return t;
			} else {
				WarpDrive.logger.error("Ticket not granted");
			}
		} else {
			WarpDrive.logger.error("No tickets left!");
		}
		return null;
	}

	public Ticket registerChunkLoadTE(TileEntityAbstractChunkLoading te) {
		return registerChunkLoadTE(te, true);
	}

	public Ticket getTicket(TileEntityAbstractChunkLoading te) {
		return registerChunkLoadTE(te, false);
	}

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {
		for (Ticket ticket : tickets) {
			NBTTagCompound data = ticket.getModData();
			if (data != null) {
				int w = data.getInteger("ticketWorldObj");
				int x = data.getInteger("ticketX");
				int y = data.getInteger("ticketY");
				int z = data.getInteger("ticketZ");
				if (w != 0 || x != 0 || y != 0 || z != 0) {
					WorldServer worldServer = DimensionManager.getWorld(w);
					if (worldServer != null) {
						TileEntity tileEntity = worldServer.getTileEntity(x, y, z);
						if (tileEntity != null && tileEntity instanceof TileEntityAbstractChunkLoading) {
							if (((TileEntityAbstractChunkLoading) tileEntity).shouldChunkLoad()) {
								WarpDrive.logger.info("ChunkLoadingTicket is loading " + tileEntity);
								((TileEntityAbstractChunkLoading) tileEntity).giveTicket(ticket);
								((TileEntityAbstractChunkLoading) tileEntity).refreshLoading(true);
								return;
							}
						}
					}
				}
			}

			ForgeChunkManager.releaseTicket(ticket);
		}
	}
	
	public static void addChatMessage(final EntityPlayer player, final String message) {
		String[] lines = message.split("\n");
		for (String line : lines) {
			player.addChatMessage(new ChatComponentText(line));
		}
	}
	@Mod.EventHandler
	public void remap(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping: event.get()) {
			if (mapping.type == GameRegistry.Type.ITEM) {
				if (mapping.name.equals("WarpDrive:airBlock")) {
					mapping.remap(Item.getItemFromBlock(blockAir));
				} else if (mapping.name.equals("WarpDrive:airCanisterFull")) {
					mapping.remap(itemAirCanisterFull);
				} else if (mapping.name.equals("WarpDrive:airgenBlock")) {
					mapping.remap(Item.getItemFromBlock(blockAirGenerator));
				} else if (mapping.name.equals("WarpDrive:boosterBlock")) {
					mapping.remap(Item.getItemFromBlock(blockLaserMedium));
				} else if (mapping.name.equals("WarpDrive:cameraBlock")) {
					mapping.remap(Item.getItemFromBlock(blockCamera));
				} else if (mapping.name.equals("WarpDrive:chunkLoader")) {
					mapping.remap(Item.getItemFromBlock(blockChunkLoader));
				} else if (mapping.name.equals("WarpDrive:cloakBlock")) {
					mapping.remap(Item.getItemFromBlock(blockCloakingCore));
				} else if (mapping.name.equals("WarpDrive:cloakCoilBlock")) {
					mapping.remap(Item.getItemFromBlock(blockCloakingCoil));
				} else if (mapping.name.equals("WarpDrive:component")) {
					mapping.remap(itemComponent);
				} else if (mapping.name.equals("WarpDrive:decorative")) {
					mapping.remap(Item.getItemFromBlock(blockDecorative));
				} else if (mapping.name.equals("WarpDrive:gasBlock")) {
					mapping.remap(Item.getItemFromBlock(blockGas));
				} else if (mapping.name.equals("WarpDrive:helmet")) {
					mapping.remap(itemHelmet);
				} else if (mapping.name.equals("WarpDrive:iridiumBlock")) {
					mapping.remap(Item.getItemFromBlock(blockIridium));
				} else if (mapping.name.equals("WarpDrive:isolationBlock")) {
					mapping.remap(Item.getItemFromBlock(blockWarpIsolation));
				} else if (mapping.name.equals("WarpDrive:laserBlock")) {
					mapping.remap(Item.getItemFromBlock(blockLaser));
				} else if (mapping.name.equals("WarpDrive:laserCamBlock")) {
					mapping.remap(Item.getItemFromBlock(blockLaserCamera));
				} else if (mapping.name.equals("WarpDrive:laserTreeFarmBlock")) {
					mapping.remap(Item.getItemFromBlock(blockLaserTreeFarm));
				} else if (mapping.name.equals("WarpDrive:liftBlock")) {
					mapping.remap(Item.getItemFromBlock(blockLift));
				} else if (mapping.name.equals("WarpDrive:miningLaserBlock")) {
					mapping.remap(Item.getItemFromBlock(blockMiningLaser));
				} else if (mapping.name.equals("WarpDrive:monitorBlock")) {
					mapping.remap(Item.getItemFromBlock(blockMonitor));
				} else if (mapping.name.equals("WarpDrive:powerLaser")) {
					mapping.remap(Item.getItemFromBlock(blockEnanReactorLaser));
				} else if (mapping.name.equals("WarpDrive:powerReactor")) {
					mapping.remap(Item.getItemFromBlock(blockEnanReactorCore));
				} else if (mapping.name.equals("WarpDrive:powerStore")) {
					mapping.remap(Item.getItemFromBlock(blockEnergyBank));
				} else if (mapping.name.equals("WarpDrive:protocolBlock")) {
					mapping.remap(Item.getItemFromBlock(blockShipController));
				} else if (mapping.name.equals("WarpDrive:radarBlock")) {
					mapping.remap(Item.getItemFromBlock(blockRadar));
				} else if (mapping.name.equals("WarpDrive:reactorLaserFocus")) {
					mapping.remap(itemIC2reactorLaserFocus);
				} else if (mapping.name.equals("WarpDrive:reactorMonitor")) {
					mapping.remap(Item.getItemFromBlock(blockIC2reactorLaserMonitor));
				} else if (mapping.name.equals("WarpDrive:scannerBlock")) {
					mapping.remap(Item.getItemFromBlock(blockShipScanner));
				} else if (mapping.name.equals("WarpDrive:transportBeacon")) {
					mapping.remap(Item.getItemFromBlock(blockTransportBeacon));
				} else if (mapping.name.equals("WarpDrive:transporter")) {
					mapping.remap(Item.getItemFromBlock(blockTransporter));
				} else if (mapping.name.equals("WarpDrive:upgrade")) {
					mapping.remap(itemUpgrade);
				} else if (mapping.name.equals("WarpDrive:warpCore")) {
					mapping.remap(Item.getItemFromBlock(blockShipCore));
				}
			} else if (mapping.type == GameRegistry.Type.BLOCK) {
				if (mapping.name.equals("WarpDrive:airBlock")) {
					mapping.remap(blockAir);
				} else if (mapping.name.equals("WarpDrive:airgenBlock")) {
					mapping.remap(blockAirGenerator);
				} else if (mapping.name.equals("WarpDrive:boosterBlock")) {
					mapping.remap(blockLaserMedium);
				} else if (mapping.name.equals("WarpDrive:cameraBlock")) {
					mapping.remap(blockCamera);
				} else if (mapping.name.equals("WarpDrive:chunkLoader")) {
					mapping.remap(blockChunkLoader);
				} else if (mapping.name.equals("WarpDrive:cloakBlock")) {
					mapping.remap(blockCloakingCore);
				} else if (mapping.name.equals("WarpDrive:cloakCoilBlock")) {
					mapping.remap(blockCloakingCoil);
				} else if (mapping.name.equals("WarpDrive:decorative")) {
					mapping.remap(blockDecorative);
				} else if (mapping.name.equals("WarpDrive:gasBlock")) {
					mapping.remap(blockGas);
				} else if (mapping.name.equals("WarpDrive:iridiumBlock")) {
					mapping.remap(blockIridium);
				} else if (mapping.name.equals("WarpDrive:isolationBlock")) {
					mapping.remap(blockWarpIsolation);
				} else if (mapping.name.equals("WarpDrive:laserBlock")) {
					mapping.remap(blockLaser);
				} else if (mapping.name.equals("WarpDrive:laserCamBlock")) {
					mapping.remap(blockLaserCamera);
				} else if (mapping.name.equals("WarpDrive:laserTreeFarmBlock")) {
					mapping.remap(blockLaserTreeFarm);
				} else if (mapping.name.equals("WarpDrive:liftBlock")) {
					mapping.remap(blockLift);
				} else if (mapping.name.equals("WarpDrive:miningLaserBlock")) {
					mapping.remap(blockMiningLaser);
				} else if (mapping.name.equals("WarpDrive:monitorBlock")) {
					mapping.remap(blockMonitor);
				} else if (mapping.name.equals("WarpDrive:powerLaser")) {
					mapping.remap(blockEnanReactorLaser);
				} else if (mapping.name.equals("WarpDrive:powerReactor")) {
					mapping.remap(blockEnanReactorCore);
				} else if (mapping.name.equals("WarpDrive:powerStore")) {
					mapping.remap(blockEnergyBank);
				} else if (mapping.name.equals("WarpDrive:protocolBlock")) {
					mapping.remap(blockShipController);
				} else if (mapping.name.equals("WarpDrive:radarBlock")) {
					mapping.remap(blockRadar);
				} else if (mapping.name.equals("WarpDrive:reactorMonitor")) {
					mapping.remap(blockIC2reactorLaserMonitor);
				} else if (mapping.name.equals("WarpDrive:scannerBlock")) {
					mapping.remap(blockShipScanner);
				} else if (mapping.name.equals("WarpDrive:transportBeacon")) {
					mapping.remap(blockTransportBeacon);
				} else if (mapping.name.equals("WarpDrive:transporter")) {
					mapping.remap(blockTransporter);
				} else if (mapping.name.equals("WarpDrive:warpCore")) {
					mapping.remap(blockShipCore);
				}
			}
		}
	}
}
