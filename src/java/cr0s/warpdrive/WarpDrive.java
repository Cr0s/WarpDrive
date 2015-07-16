package cr0s.warpdrive;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.block.BlockAir;
import cr0s.warpdrive.block.BlockGas;
import cr0s.warpdrive.block.BlockIridium;
import cr0s.warpdrive.command.GenerateCommand;
import cr0s.warpdrive.command.InvisibleCommand;
import cr0s.warpdrive.command.JumpgateCommand;
import cr0s.warpdrive.command.SpaceTpCommand;
import cr0s.warpdrive.data.CamRegistry;
import cr0s.warpdrive.data.CloakManager;
import cr0s.warpdrive.data.JumpgatesRegistry;
import cr0s.warpdrive.data.WarpCoresRegistry;
import cr0s.warpdrive.item.ItemWarpAirCanister;
import cr0s.warpdrive.item.ItemWarpArmor;
import cr0s.warpdrive.item.ItemWarpComponent;
import cr0s.warpdrive.machines.BlockAirGenerator;
import cr0s.warpdrive.machines.BlockCamera;
import cr0s.warpdrive.machines.BlockCloakingCoil;
import cr0s.warpdrive.machines.BlockCloakingDeviceCore;
import cr0s.warpdrive.machines.BlockLaser;
import cr0s.warpdrive.machines.BlockLaserCam;
import cr0s.warpdrive.machines.BlockLift;
import cr0s.warpdrive.machines.BlockMiningLaser;
import cr0s.warpdrive.machines.BlockMonitor;
import cr0s.warpdrive.machines.BlockParticleBooster;
import cr0s.warpdrive.machines.BlockProtocol;
import cr0s.warpdrive.machines.BlockRadar;
import cr0s.warpdrive.machines.BlockReactor;
import cr0s.warpdrive.machines.BlockShipScanner;
import cr0s.warpdrive.machines.BlockWarpIsolation;
import cr0s.warpdrive.machines.TileEntityAirGenerator;
import cr0s.warpdrive.machines.TileEntityCamera;
import cr0s.warpdrive.machines.TileEntityCloakingDeviceCore;
import cr0s.warpdrive.machines.TileEntityLaser;
import cr0s.warpdrive.machines.TileEntityLift;
import cr0s.warpdrive.machines.TileEntityMiningLaser;
import cr0s.warpdrive.machines.TileEntityMonitor;
import cr0s.warpdrive.machines.TileEntityParticleBooster;
import cr0s.warpdrive.machines.TileEntityProtocol;
import cr0s.warpdrive.machines.TileEntityRadar;
import cr0s.warpdrive.machines.TileEntityReactor;
import cr0s.warpdrive.machines.TileEntityShipScanner;
import cr0s.warpdrive.machines.WarpChunkTE;
import cr0s.warpdrive.render.CameraOverlay;
import cr0s.warpdrive.world.BiomeSpace;
import cr0s.warpdrive.world.HyperSpaceProvider;
import cr0s.warpdrive.world.HyperSpaceWorldGenerator;
import cr0s.warpdrive.world.SpaceProvider;
import cr0s.warpdrive.world.SpaceWorldGenerator;
import dan200.computercraft.api.ComputerCraftAPI;

@Mod(modid = "warpdrive", name = "WarpDrive", version = "2.0.0",
	dependencies = "required-after:IC2;"
			+ " required-after:CoFHCore;"
			+ " after:ComputerCraft;" 
			+ " after:OpenComputer;" 
			+ " after:CCTurtle;" 
			+ " after:gregtech_addon;" 
			+ " required-after:AppliedEnergistics;" 
			+ " after:AdvancedSolarPanel;" 
			+ " after:AtomicScience;" 
			+ " after:ICBM|Explosion;"
			+ " after:MFFS;" 
			+ " after:GraviSuite;" 
			+ " after:UndergroundBiomes;" 
			+ " after:NetherOres")
/**
 * @author Cr0s
 */
public class WarpDrive implements LoadingCallback {
	public static Block warpCore;
	public static Block protocolBlock;
	public static Block radarBlock;
	public static Block isolationBlock;
	public static Block airgenBlock;
	public static Block laserBlock;
	public static Block laserCamBlock;
	public static Block cameraBlock;
	public static Block monitorBlock;
	public static Block boosterBlock;
	public static Block miningLaserBlock;
	public static Block liftBlock;
	public static Block scannerBlock;
	public static Block cloakBlock;
	public static Block cloakCoilBlock;
	
	public static Block airBlock;
	public static Block gasBlock;

	public static Block iridiumBlock;
	
	public static ItemWarpComponent componentItem;
	
	public static ArmorMaterial armorMaterial = EnumHelper.addArmorMaterial("WARP", 5, new int[]{1, 3, 2, 1}, 15);
	public static ItemWarpArmor helmetItem;
	public static ItemWarpAirCanister airCanisterItem;
	
	public static BiomeGenBase spaceBiome;
	public World space;
	public SpaceWorldGenerator spaceWorldGenerator;
	public HyperSpaceWorldGenerator hyperSpaceWorldGenerator;
	public World hyperSpace;
	
	// Client settings
    public static float normalFOV = 70.0F;
    public static float normalSensitivity = 1.0F;

	public static CreativeTabs warpdriveTab = new WarpDriveCreativeTab("Warpdrive", "Warpdrive").setBackgroundImageName("warpdrive:creativeTab");
	
	@Instance("WarpDrive")
	public static WarpDrive instance;
	@SidedProxy(clientSide = "cr0s.warpdrive.client.ClientProxy", serverSide = "cr0s.warpdrive.CommonProxy")
	public static CommonProxy proxy;

	public static WarpCoresRegistry warpCores;
	public static JumpgatesRegistry jumpgates;
	public static CloakManager cloaks;

	public static CamRegistry cams;
	public boolean isOverlayEnabled = false;
	public int overlayType = 0;
    public String debugMessage = "";
	
	public static WarpDrivePeripheralHandler peripheralHandler = null;
	
	public static String defHelpStr = "help(\"functionName\"): returns help for the function specified";
	public static String defEnergyStr = "getEnergyLevel(): returns currently contained energy, max contained energy";
	public static String defUpgradeStr = "upgrades(): returns a list of currently installed upgrades";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		WarpDriveConfig.preInit(new Configuration(event.getSuggestedConfigurationFile()));

		if (FMLCommonHandler.instance().getSide().isClient()) {
	        Minecraft mc = Minecraft.getMinecraft();

	        // System.out.println("[WarpDrive] Registering sounds event handler...");
			MinecraftForge.EVENT_BUS.register(new SoundHandler());

			normalFOV = mc.gameSettings.fovSetting;
		    normalSensitivity = mc.gameSettings.mouseSensitivity;
		    WarpDrive.print("[WarpDrive] FOV is " + normalFOV + " Sensitivity is " + normalSensitivity);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@EventHandler
	public void init(FMLInitializationEvent event) {
	}

	public static void print(String out) {
		System.out.println("WarpDrive " + (FMLCommonHandler.instance().getEffectiveSide().isClient() ? "Client ":"Server ") + ": " + out);
	}
	
	public static void debugPrint(String out) {
		if (WarpDriveConfig.G_DEBUGMODE) {
			print(out);
		}
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		WarpDriveConfig.load();

		// CORE CONTROLLER
		protocolBlock = new BlockProtocol(0, Material.rock);
		
		GameRegistry.registerBlock(protocolBlock, "protocolBlock");
		GameRegistry.registerTileEntity(TileEntityProtocol.class, "protocolBlock");
		
		// WARP CORE
		warpCore = new BlockReactor(0, Material.rock);
		
		GameRegistry.registerBlock(warpCore, "warpCore");
		GameRegistry.registerTileEntity(TileEntityReactor.class, "warpCore");		
		
		// WARP RADAR
		radarBlock = new BlockRadar(0, Material.rock);
		
		GameRegistry.registerBlock(radarBlock, "radarBlock");
		GameRegistry.registerTileEntity(TileEntityRadar.class, "radarBlock");
		
		// WARP ISOLATION
		isolationBlock = new BlockWarpIsolation(0, Material.rock);
		
		GameRegistry.registerBlock(isolationBlock, "isolationBlock");
		
		// AIR GENERATOR
		airgenBlock = new BlockAirGenerator(0,Material.rock);
		
		GameRegistry.registerBlock(airgenBlock, "airgenBlock");
		GameRegistry.registerTileEntity(TileEntityAirGenerator.class, "airgenBlock");
		
		
		// AIR Block
		airBlock = (new BlockAir());
		
		GameRegistry.registerBlock(airBlock, "airBlock");
		
		// GAS Block
		gasBlock = new BlockGas();
		
		GameRegistry.registerBlock(gasBlock, "gasBlock");
		
		// LASER EMITTER
		laserBlock = new BlockLaser(0, Material.rock);
			
		GameRegistry.registerBlock(laserBlock, "laserBlock");
		GameRegistry.registerTileEntity(TileEntityLaser.class, "laserBlock");
		
		// LASER EMITTER WITH CAMERA
		laserCamBlock = new BlockLaserCam(0, Material.rock);
			
		GameRegistry.registerBlock(laserCamBlock, "laserCamBlock");
		
		// CAMERA
		cameraBlock = new BlockCamera(0,Material.rock);
		
		GameRegistry.registerBlock(cameraBlock, "cameraBlock");
		GameRegistry.registerTileEntity(TileEntityCamera.class, "cameraBlock");
		
		// MONITOR
		monitorBlock = new BlockMonitor();
		
		GameRegistry.registerBlock(monitorBlock, "monitorBlock");
		GameRegistry.registerTileEntity(TileEntityMonitor.class, "monitorBlock");
		
		
		// MINING LASER
		miningLaserBlock = new BlockMiningLaser(0, Material.rock);
		
		GameRegistry.registerBlock(miningLaserBlock, "miningLaserBlock");
		GameRegistry.registerTileEntity(TileEntityMiningLaser.class, "miningLaserBlock");
		
		// PARTICLE BOOSTER
		boosterBlock = new BlockParticleBooster(0, Material.rock);
		
		GameRegistry.registerBlock(boosterBlock, "boosterBlock");
		GameRegistry.registerTileEntity(TileEntityParticleBooster.class, "boosterBlock");
		
		// LASER LIFT
		liftBlock = new BlockLift(0, Material.rock);
		
		GameRegistry.registerBlock(liftBlock, "liftBlocks");
		GameRegistry.registerTileEntity(TileEntityLift.class, "liftBlock");
		
		// IRIDIUM Blocks
		iridiumBlock = new BlockIridium();
		
		GameRegistry.registerBlock(iridiumBlock, "iridiumBlock");
		
        // SHIP SCANNER
        scannerBlock = new BlockShipScanner(0, Material.rock);
        
        GameRegistry.registerBlock(scannerBlock, "scannerBlocks");
        GameRegistry.registerTileEntity(TileEntityShipScanner.class, "scannerBlock");		

        // CLOAKING DEVICE CORE
        cloakBlock = new BlockCloakingDeviceCore(0, Material.rock);
        
        GameRegistry.registerBlock(cloakBlock, "cloakBlock");
        GameRegistry.registerTileEntity(TileEntityCloakingDeviceCore.class, "cloakBlock");        
        
        // CLOAKING DEVICE COIL
		cloakCoilBlock = new BlockCloakingCoil(0, Material.rock);
		
		GameRegistry.registerBlock(cloakCoilBlock, "cloakCoilBlock");    
		
		// COMPONENT ItemsS
		componentItem = new ItemWarpComponent();
		GameRegistry.registerItem(componentItem, "component");		
		
		helmetItem = new ItemWarpArmor(0);
		GameRegistry.registerItem(helmetItem, "helmet");
		
		airCanisterItem = new ItemWarpAirCanister();
		GameRegistry.registerItem(airCanisterItem, "airCanisterFull");
		
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
			warpdriveTab.setBackgroundImageName("Items.png");
			MinecraftForge.EVENT_BUS.register(new CameraOverlay(Minecraft.getMinecraft()));
		}
		
		if (WarpDriveConfig.isCCLoaded) {
			peripheralHandler = new WarpDrivePeripheralHandler();
			ComputerCraftAPI.registerPeripheralProvider(peripheralHandler);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		space = DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID);
		hyperSpace = DimensionManager.getWorld(WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID);
		
		WarpDriveConfig.postInit();
		
		if (WarpDriveConfig.isICLoaded && WarpDriveConfig.G_ENABLE_IC2_RECIPES) {
			initIC2Recipes();
		}
		if (WarpDriveConfig.isAppliedEnergisticsLoaded && WarpDriveConfig.isThermalExpansionLoaded && WarpDriveConfig.isAtomicScienceLoaded && WarpDriveConfig.G_ENABLE_TDK_RECIPES) {
			initAETERecipes();
		}
		if (WarpDriveConfig.G_ENABLE_VANILLA_RECIPES) {
			initVanillaRecipes();
		}
		
		warpCores = new WarpCoresRegistry();
		jumpgates = new JumpgatesRegistry();
		cams = new CamRegistry();
	}
	
	private static void initVanillaRecipes() {
		componentItem.registerRecipes();
		
		//WarpCore
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(warpCore), false, "ipi", "ici", "idi",
				'i', Items.iron_ingot,
				'p', componentItem.getIS(6),
				'c', componentItem.getIS(2),
				'd', Items.diamond));
		
		//Controller
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(protocolBlock), false, "ici", "idi", "iii",
				'i', Items.iron_ingot,
				'c', componentItem.getIS(5),
				'd', Items.diamond));
		
		//Radar
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(radarBlock), false, "ggg", "pdc", "iii",
				'i', Items.iron_ingot,
				'c', componentItem.getIS(5),
				'p', componentItem.getIS(6),
				'g', Blocks.glass,
				'd', Items.diamond));
		
		//Isolation Blocks
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(isolationBlock), false, "igi", "geg", "igi",
				'i', Items.iron_ingot,
				'g', Blocks.glass,
				'e', Items.ender_pearl));
		
		//Air generator
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(airgenBlock), false, "ibi", "i i", "ipi",
				'i', Items.iron_ingot,
				'b', Blocks.iron_bars,
				'p', componentItem.getIS(6)));
		
		//Laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(laserBlock), false, "ili", "iri", "ici",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				'c', componentItem.getIS(5),
				'l', componentItem.getIS(3),
				'p', componentItem.getIS(6)));
		
		//Mining laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(miningLaserBlock), false, "ici", "iti", "ili",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				't', componentItem.getIS(1),
				'c', componentItem.getIS(5),
				'l', componentItem.getIS(3)));
		
		//Laser Lift
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(liftBlock), false, "ipi", "rtr", "ili",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				't', componentItem.getIS(1),
				'l', componentItem.getIS(3),
				'p', componentItem.getIS(6)));
		
		//Particle Booster
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(boosterBlock), false, "ipi", "rgr", "iii",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				'g', Blocks.glass,
				'p', componentItem.getIS(6)));
		
		//Camera
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cameraBlock), false, "ngn", "i i", "ici",
				'i', Items.iron_ingot,
				'n', Items.gold_nugget,
				'g', Blocks.glass,
				'c', componentItem.getIS(5)));
		
		//LaserCamera
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(laserCamBlock), cameraBlock, laserBlock));
		
		//Monitor
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(monitorBlock), false, "ggg", "iti", "ici",
				'i', Items.iron_ingot,
				't', Blocks.torch,
				'g', Blocks.glass,
				'c', componentItem.getIS(5)));
		
		//Cloaking device
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cloakBlock), false, "ipi", "lrl", "ici",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				'l', componentItem.getIS(3),
				'c', componentItem.getIS(5),
				'p', componentItem.getIS(6)));
		
		//Cloaking coil
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cloakCoilBlock), false, "ini", "rdr", "ini",
				'i', Items.iron_ingot,
				'd', Items.diamond,
				'r', Items.redstone,
				'n', Items.gold_nugget));
		
		//Helmet
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(helmetItem), false, "iii", "iwi", "gcg",
				'i', Items.iron_ingot,
				'w', Blocks.wool,
				'g', Blocks.glass,
				'c', componentItem.getIS(8)));
	}
	
	private static void initAETERecipes() {
		ItemStack redstoneEnergycell = GameRegistry.findItemStack("ThermalExpansion", "cellReinforced", 1);
		ItemStack bucketEnder = GameRegistry.findItemStack("ThermalExpansion", "bucketEnder", 1);
		ItemStack fluixCrystal = WarpDriveConfig.getAEMaterial("matFluxCrystal");
		ItemStack vibrantQuartzGlass = WarpDriveConfig.getAEBlock("blkQuartzLamp");
		vibrantQuartzGlass.setItemDamage(4);
		ItemStack antimatter = GameRegistry.findItemStack("ResonantInduction|Atomic", "antimatter", 1);
		antimatter.setItemDamage(0);
		ItemStack floppy = GameRegistry.findItemStack("ComputerCraft", "disk", 1);

		// top = advancedCircuit, redstoneEnergycell, advancedCircuit
		// middle = fluix crystal, advancedMachine, fluix crystal
		// bottom = advancedCircuit, bucket Resonant ender, advancedCircuit
		GameRegistry.addRecipe(new ItemStack(warpCore), "crc", "fmf", "cec",
				'm', WarpDriveConfig.getIC2Item("advancedMachine"),
				'c', WarpDriveConfig.getIC2Item("advancedCircuit"),
				'r', redstoneEnergycell, 
				'e', bucketEnder,
				'f', fluixCrystal);
		
		// top = advancedCircuit, floppy, advancedCircuit
		// middle = advancedCircuit, advancedMachine, advancedCircuit
		// bottom = advancedCircuit, fluix crystal, advancedCircuit
		GameRegistry.addRecipe(new ItemStack(protocolBlock), "coc", "cmc", "cfc",
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"),
			'o', floppy,
			'f', fluixCrystal);
	}
	
	private static void initIC2Recipes() {
		GameRegistry.addRecipe(new ItemStack(warpCore), "ici", "cmc", "ici",
			'i', WarpDriveConfig.getIC2Item("iridiumPlate"),
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"));
		
		GameRegistry.addRecipe(new ItemStack(protocolBlock), "iic", "imi", "cii",
			'i', WarpDriveConfig.getIC2Item("iridiumPlate"),
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"));
		
		GameRegistry.addRecipe(new ItemStack(radarBlock), "ifi", "imi", "imi",
			'i', WarpDriveConfig.getIC2Item("iridiumPlate"),
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'f', WarpDriveConfig.getIC2Item("frequencyTransmitter"));
		
		GameRegistry.addRecipe(new ItemStack(isolationBlock), "iii", "idi", "iii",
			'i', WarpDriveConfig.getIC2Item("iridiumPlate"),
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'd', Blocks.diamond_block);
		
		GameRegistry.addRecipe(new ItemStack(airgenBlock), "lcl", "lml", "lll",
			'l', Blocks.leaves,
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"));
		
		GameRegistry.addRecipe(new ItemStack(laserBlock), "sss", "ama", "aaa",
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'a', WarpDriveConfig.getIC2Item("advancedAlloy"),
			's', WarpDriveConfig.getIC2Item("advancedCircuit"));
		
		GameRegistry.addRecipe(new ItemStack(miningLaserBlock), "aaa", "ama", "ccc",
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"),
			'a', WarpDriveConfig.getIC2Item("advancedAlloy"),
			'm', WarpDriveConfig.getIC2Item("miner"));
		
		GameRegistry.addRecipe(new ItemStack(boosterBlock), "afc", "ama", "cfa",
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"),
			'a', WarpDriveConfig.getIC2Item("advancedAlloy"),
			'f', WarpDriveConfig.getIC2Item("glassFiberCableItem"),
			'm', WarpDriveConfig.getIC2Item("mfeUnit"));
		
		GameRegistry.addRecipe(new ItemStack(liftBlock), "aca", "ama", "a#a",
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"),
			'a', WarpDriveConfig.getIC2Item("advancedAlloy"),
			'm', WarpDriveConfig.getIC2Item("magnetizer"));

		GameRegistry.addRecipe(new ItemStack(iridiumBlock), "iii", "iii", "iii",
			'i', WarpDriveConfig.getIC2Item("iridiumPlate"));
		
		GameRegistry.addShapelessRecipe(
			new ItemStack(WarpDriveConfig.getIC2Item("iridiumPlate").getItem(), 9),
			new ItemStack(iridiumBlock));
		
		GameRegistry.addRecipe(new ItemStack(laserCamBlock), "imi", "cec", "#k#",
			'i', WarpDriveConfig.getIC2Item("iridiumPlate"),
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"), 
			'e', laserBlock,
			'k', cameraBlock);
		
		GameRegistry.addRecipe(new ItemStack(cameraBlock), "cgc", "gmg", "cgc",
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"),
			'g', Blocks.glass);
		
		GameRegistry.addRecipe(new ItemStack(monitorBlock), "gcg", "gmg", "ggg", 
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"),
			'g', Blocks.glass);
		
		GameRegistry.addRecipe(new ItemStack(scannerBlock), "sgs", "mma", "amm",
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'a', WarpDriveConfig.getIC2Item("advancedAlloy"),
			's', WarpDriveConfig.getIC2Item("advancedCircuit"),
			'g', Blocks.glass);	
		
		GameRegistry.addRecipe(new ItemStack(cloakBlock), "imi", "mcm", "imi", 
			'i', iridiumBlock,
			'c', cloakCoilBlock,
			'm', WarpDriveConfig.getIC2Item("advancedMachine"));
		
		GameRegistry.addRecipe(new ItemStack(cloakCoilBlock), "iai", "aca", "iai", 
			'i', WarpDriveConfig.getIC2Item("iridiumPlate"),
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"),
			'a', WarpDriveConfig.getIC2Item("advancedAlloy"));
	}

	private static void registerSpaceDimension() {
		spaceBiome = (new BiomeSpace(24))
			.setColor(0)
			.setDisableRain()
			.setBiomeName("Space");
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
		
		event.registerServerCommand(new GenerateCommand());
		event.registerServerCommand(new SpaceTpCommand());
		event.registerServerCommand(new InvisibleCommand());
		event.registerServerCommand(new JumpgateCommand());
	}
	
	public Ticket registerChunkLoadTE(WarpChunkTE te, boolean refreshLoading) {
		World worldObj = te.getWorldObj();
		if (ForgeChunkManager.ticketCountAvailableFor(this, worldObj) > 0)
		{
			Ticket t = ForgeChunkManager.requestTicket(this, worldObj, Type.NORMAL);
			if (t != null) {
				te.giveTicket(t);	// FIXME calling the caller is a bad idea
				if(refreshLoading)
					te.refreshLoading();
				return t;
			}
			else
			{
				WarpDrive.debugPrint("Ticket not granted");
			}
		}
		else
		{
			WarpDrive.debugPrint("No tickets left!");
		}
		return null;
	}
	
	public Ticket registerChunkLoadTE(WarpChunkTE te)
	{
		return registerChunkLoadTE(te, true);
	}
	
	public Ticket getTicket(WarpChunkTE te)
	{
		return registerChunkLoadTE(te, false);
	}
	
	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world)
	{
		for (Ticket ticket : tickets)
		{
			NBTTagCompound data = ticket.getModData();
			if(data != null)
			{
				int w = data.getInteger("ticketWorldObj");
				int x = data.getInteger("ticketX");
				int y = data.getInteger("ticketY");
				int z = data.getInteger("ticketZ");
				if(w != 0 || x != 0 || y != 0 || z != 0)
				{
					WorldServer ws = DimensionManager.getWorld(w);
					if(ws != null)
					{
						TileEntity te = ws.getTileEntity(x, y, z);
						if(te != null && te instanceof WarpChunkTE)
						{
							if(((WarpChunkTE)te).shouldChunkLoad())
							{
								WarpDrive.debugPrint("[TicketCallback] Regiving Ticket!");
								((WarpChunkTE)te).giveTicket(ticket);
								((WarpChunkTE)te).refreshLoading(true);
								return;
							}
						}
					}
				}
			}

			ForgeChunkManager.releaseTicket(ticket);
		}
	}
}
