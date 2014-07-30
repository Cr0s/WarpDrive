package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cr0s.WarpDrive.block.BlockAir;
import cr0s.WarpDrive.block.BlockGas;
import cr0s.WarpDrive.item.ItemWarpComponent;
import cr0s.WarpDrive.machines.*;

import java.util.ArrayList;
import java.util.List;

import dan200.computercraft.api.ComputerCraftAPI;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Mod(modid = "WarpDrive", name = "WarpDrive", version = "1.3.0.0", dependencies = "required-after:ComputerCraft; after:CCTurtle; required-after:AppliedEnergistics; after:AtomicScience; after:ICBM|Explosion; after:MFFS")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, channels = {
		"WarpDriveBeam", 
		"WarpDriveFreq", 
		"WarpDriveLaserT",
		"WarpDriveCloaks" }, packetHandler = PacketHandler.class)
/**
 * @author Cr0s
 */
public class WarpDrive implements LoadingCallback {
	// World limits
	public final static int WORLD_LIMIT_BLOCKS = 100000;

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
	public static Block laserTreeFarmBlock;
	public static Block liftBlock;
	public static Block scannerBlock;
	public static Block cloakBlock;
	public static Block cloakCoilBlock;
	public static Block transporterBlock;
	public static Block powerReactorBlock;
	public static Block powerLaserBlock;
	
	public static Block airBlock;
	public static Block gasBlock;
	
	public static Block transportBeaconBlock;
	
	public static ItemWarpComponent componentItem;

	public static BiomeGenBase spaceBiome;
	public World space;
	private int spaceProviderID;
	public int spaceDimID;
	public SpaceWorldGenerator spaceWorldGenerator;
	public HyperSpaceWorldGenerator hyperSpaceWorldGenerator;

	public World hyperSpace;
	private int hyperSpaceProviderID;
	public int hyperSpaceDimID;

	public static CreativeTabs warpdriveTab = new WarpDriveCreativeTab("Warpdrive","Warpdrive").setBackgroundImageName("warpdrive:creativeTab");
	
	@Instance("WarpDrive")
	public static WarpDrive instance;
	@SidedProxy(clientSide = "cr0s.WarpDrive.client.ClientProxy", serverSide = "cr0s.WarpDrive.CommonProxy")
	public static CommonProxy proxy;

	public WarpCoresRegistry registry;
	public JumpGatesRegistry jumpGates;
	
	public CloakManager cloaks;

	public CamRegistry cams;
	public boolean isOverlayEnabled = false;
	public int overlayType = 0;
	
	public static WarpDrivePeripheralHandler peripheralHandler = new WarpDrivePeripheralHandler();
	
	public static String defHelpStr = "help(\"functionName\"): returns help for the function specified";
	public static String defEnergyStr = "energy(): returns currently contained energy, max contained energy";
	
	private ArrayList<Ticket> warpTickets = new ArrayList<Ticket>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		WarpDriveConfig.Init(new Configuration(event.getSuggestedConfigurationFile()));

		if (FMLCommonHandler.instance().getSide().isClient())
		{
			debugPrint("[WarpDrive] Registering sounds event handler...");
			MinecraftForge.EVENT_BUS.register(new SoundHandler());
		} 
	}
	
	public static void debugPrint(String out)
	{
		if(WarpDriveConfig.debugMode == false)
			return;
		if(WarpDriveConfig.debugMode)
			System.out.println(out);
	}

	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		WarpDriveConfig.Init2();
		// CORE CONTROLLER
		protocolBlock = new BlockProtocol(WarpDriveConfig.controllerID,0, Material.rock);
		GameRegistry.registerBlock(protocolBlock, "protocolBlock");
		GameRegistry.registerTileEntity(TileEntityProtocol.class,"protocolBlock");
		
		// WARP CORE
		warpCore = new BlockReactor(WarpDriveConfig.coreID, 0, Material.rock);
		GameRegistry.registerBlock(warpCore, "warpCore");
		GameRegistry.registerTileEntity(TileEntityReactor.class, "warpCore");		
		
		// WARP RADAR
		radarBlock = new BlockRadar(WarpDriveConfig.radarID, 0, Material.rock);
		GameRegistry.registerBlock(radarBlock, "radarBlock");
		GameRegistry.registerTileEntity(TileEntityRadar.class, "radarBlock");
		
		// WARP ISOLATION
		isolationBlock = new BlockWarpIsolation( WarpDriveConfig.isolationID, 0, Material.rock);
		GameRegistry.registerBlock(isolationBlock, "isolationBlock");
		
		// AIR GENERATOR
		airgenBlock = new BlockAirGenerator(WarpDriveConfig.airgenID, 0,Material.rock);
		GameRegistry.registerBlock(airgenBlock, "airgenBlock");
		GameRegistry.registerTileEntity(TileEntityAirGenerator.class,"airgenBlock");
		
		// AIR BLOCK
		airBlock = (new BlockAir(WarpDriveConfig.airID));
		GameRegistry.registerBlock(airBlock, "airBlock");
		
		// GAS BLOCK
		gasBlock = (new BlockGas(WarpDriveConfig.gasID));
		GameRegistry.registerBlock(gasBlock, "gasBlock");
		
		// LASER EMITTER
		laserBlock = new BlockLaser(WarpDriveConfig.laserID, 0,Material.rock);
		GameRegistry.registerBlock(laserBlock, "laserBlock");
		GameRegistry.registerTileEntity(TileEntityLaser.class, "laserBlock");
		
		// LASER EMITTER WITH CAMERA
		laserCamBlock = new BlockLaserCam(WarpDriveConfig.laserCamID, 0, Material.rock);
		GameRegistry.registerBlock(laserCamBlock, "laserCamBlock");
		
		// CAMERA
		cameraBlock = new BlockCamera(WarpDriveConfig.camID, 0,Material.rock);
		GameRegistry.registerBlock(cameraBlock, "cameraBlock");
		GameRegistry.registerTileEntity(TileEntityCamera.class, "cameraBlock");
		
		// MONITOR
		monitorBlock = new BlockMonitor(WarpDriveConfig.monitorID);
		GameRegistry.registerBlock(monitorBlock, "monitorBlock");
		GameRegistry.registerTileEntity(TileEntityMonitor.class, "monitorBlock");
		
		// MINING LASER
		miningLaserBlock = new BlockMiningLaser(WarpDriveConfig.miningLaserID, 0, Material.rock);
		GameRegistry.registerBlock(miningLaserBlock, "miningLaserBlock");
		GameRegistry.registerTileEntity(TileEntityMiningLaser.class, "miningLaserBlock");
		
		// LASER TREE FARM
		laserTreeFarmBlock = new BlockLaserTreeFarm(WarpDriveConfig.laserTreeFarmID,0,Material.rock);
		GameRegistry.registerBlock(laserTreeFarmBlock, "laserTreeFarmBlock");
		GameRegistry.registerTileEntity(TileEntityLaserTreeFarm.class,"laserTreeFarmBlock");
		
		// PARTICLE BOOSTER
		boosterBlock = new BlockParticleBooster(WarpDriveConfig.particleBoosterID, 0, Material.rock);
		GameRegistry.registerBlock(boosterBlock, "boosterBlock");
		GameRegistry.registerTileEntity(TileEntityParticleBooster.class,"boosterBlock");
		
		// LASER LIFT
		liftBlock = new BlockLift(WarpDriveConfig.liftID, 0, Material.rock);
		GameRegistry.registerBlock(liftBlock, "liftBlock");
		GameRegistry.registerTileEntity(TileEntityLift.class, "liftBlock");
		
        // SHIP SCANNER
        scannerBlock = new BlockShipScanner(WarpDriveConfig.shipScannerID, 0, Material.rock);
        GameRegistry.registerBlock(scannerBlock, "scannerBlock");
        GameRegistry.registerTileEntity(TileEntityShipScanner.class, "scannerBlock");		

        // CLOAKING DEVICE CORE
        cloakBlock = new BlockCloakingDeviceCore(WarpDriveConfig.cloakCoreID, 0, Material.rock);
        GameRegistry.registerBlock(cloakBlock, "cloakBlock");
        GameRegistry.registerTileEntity(TileEntityCloakingDeviceCore.class, "cloakBlock");        
        
        // CLOAKING DEVICE COIL
		cloakCoilBlock = new BlockCloakingCoil(WarpDriveConfig.cloakCoilID, 0, Material.rock);
		GameRegistry.registerBlock(cloakCoilBlock, "cloakCoilBlock");    
		
		// TRANSPORTER
		transporterBlock = new BlockTransporter(WarpDriveConfig.transporterID,Material.rock);
		GameRegistry.registerBlock(transporterBlock, "transporter");
		GameRegistry.registerTileEntity(TileEntityTransporter.class,"transporter");
		
		// POWER STUFF
		powerReactorBlock = new BlockPowerReactor(WarpDriveConfig.powerReactorID);
		GameRegistry.registerBlock(powerReactorBlock,"powerReactor");
		GameRegistry.registerTileEntity(TileEntityPowerReactor.class, "powerReactor");
		powerLaserBlock   = new BlockPowerLaser(WarpDriveConfig.powerLaserID);
		GameRegistry.registerBlock(powerLaserBlock,"powerLaser");
		GameRegistry.registerTileEntity(TileEntityPowerLaser.class,"powerLaser");
		
		// TRANSPORT BEACON
		/*transportBeaconBlock = new BlockTransportBeacon(WarpDriveConfig.transportBeaconID)
			.setHardness(0.5F)
			.setStepSound(Block.soundMetalFootstep)
			.setCreativeTab(CreativeTabs.tabRedstone)
			.setUnlocalizedName("transporterBeacon");
		
		LanguageRegistry.addName(transportBeaconBlock, "Test");
		GameRegistry.registerBlock(transportBeaconBlock, "transportBeacon");*/
		
		componentItem = new ItemWarpComponent(WarpDriveConfig.componentID);
		GameRegistry.registerItem(componentItem, "component");
        
		proxy.registerEntities();
		ForgeChunkManager.setForcedChunkLoadingCallback(instance, instance);
		spaceWorldGenerator = new SpaceWorldGenerator();
		GameRegistry.registerWorldGenerator(spaceWorldGenerator);
		hyperSpaceWorldGenerator = new HyperSpaceWorldGenerator();
		GameRegistry.registerWorldGenerator(hyperSpaceWorldGenerator);
		registerSpaceDimension();
		registerHyperSpaceDimension();
		MinecraftForge.EVENT_BUS.register(new SpaceEventHandler());

		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			warpdriveTab.setBackgroundImageName("items.png");
			MinecraftForge.EVENT_BUS.register(new CameraOverlay(Minecraft.getMinecraft()));
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		space = DimensionManager.getWorld(spaceDimID);
		hyperSpace = DimensionManager.getWorld(hyperSpaceDimID);
		
		//Init recipes
		initRecipes();
		
		registry = new WarpCoresRegistry();
		
		ComputerCraftAPI.registerPeripheralProvider(peripheralHandler);

		jumpGates = new JumpGatesRegistry();
		cams = new CamRegistry();
	}
	
	private void initRecipes()
	{
		componentItem.registerRecipes();
		//WarpCore
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(warpCore), false, "ipi","ici","idi",
				'i', Item.ingotIron,
				'p', componentItem.getIS(6),
				'c', componentItem.getIS(2),
				'd', Item.diamond));
		//Controller
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(protocolBlock), false, "ici","idi","iii",
				'i', Item.ingotIron,
				'c', componentItem.getIS(5),
				'd', Item.diamond));
		
		//Radar
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(radarBlock), false, "ggg","pdc","iii",
				'i', Item.ingotIron,
				'c', componentItem.getIS(5),
				'p', componentItem.getIS(6),
				'g', Block.glass,
				'd', Item.diamond));
		
		//Isolation Block
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(isolationBlock), false, "igi","geg","igi",
				'i', Item.ingotIron,
				'g', Block.glass,
				'e', Item.enderPearl));
		
		//Air generator
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(airgenBlock), false, "ibi","i i","ipi",
				'i', Item.ingotIron,
				'b', Block.fenceIron,
				'p', componentItem.getIS(6)));
		
		//Laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(laserBlock), false, "ili","iri","ici",
				'i', Item.ingotIron,
				'r', Item.redstone,
				'c', componentItem.getIS(5),
				'l', componentItem.getIS(3),
				'p', componentItem.getIS(6)));
		
		//Mining laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(miningLaserBlock), false, "ici","iti","ili",
				'i', Item.ingotIron,
				'r', Item.redstone,
				't', componentItem.getIS(1),
				'c', componentItem.getIS(5),
				'l', componentItem.getIS(3)));
		
		//Tree farm laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(laserTreeFarmBlock), false, "ili","sts","ici",
				'i', Item.ingotIron,
				's', "treeSapling",
				't', componentItem.getIS(1),
				'c', componentItem.getIS(5),
				'l', componentItem.getIS(3)));
		
		//Laser Lift
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(liftBlock), false, "ipi","rtr","ili",
				'i', Item.ingotIron,
				'r', Item.redstone,
				't', componentItem.getIS(1),
				'l', componentItem.getIS(3),
				'p', componentItem.getIS(6)));
		
		//Transporter
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(transporterBlock), false, "iii","ptc","iii",
				'i', Item.ingotIron,
				't', componentItem.getIS(1),
				'c', componentItem.getIS(5),
				'p', componentItem.getIS(6)));
		
		//Particle Booster
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(boosterBlock), false, "ipi","rgr","iii",
				'i', Item.ingotIron,
				'r', Item.redstone,
				'g', Block.glass,
				'p', componentItem.getIS(6)));
		
		//Camera
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cameraBlock), false, "ngn","i i","ici",
				'i', Item.ingotIron,
				'n', Item.goldNugget,
				'g', Block.glass,
				'c', componentItem.getIS(5)));
		
		//LaserCamera
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(laserCamBlock),cameraBlock,laserBlock));
		
		//Monitor
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(monitorBlock), false, "ggg","iti","ici",
				'i', Item.ingotIron,
				't', Block.torchWood,
				'g', Block.glass,
				'c', componentItem.getIS(5)));
		
		//Cloaking device
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cloakBlock), false, "ipi","lrl","ici",
				'i', Item.ingotIron,
				'r', Item.redstone,
				'l', componentItem.getIS(3),
				'c', componentItem.getIS(5),
				'p', componentItem.getIS(6)));
		
		//Cloaking coil
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cloakCoilBlock), false, "ini","rdr","ini",
				'i', Item.ingotIron,
				'd', Item.diamond,
				'r', Item.redstone,
				'n', Item.goldNugget));
		
		//Power Laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(powerLaserBlock), false, "iii","ilg","ici",
				'i', Item.ingotIron,
				'g', Block.glass,
				'c', componentItem.getIS(5),
				'l', componentItem.getIS(3)));
		
		//Power Reactor
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(powerReactorBlock), false, "ipi","gog","ici",
				'i', Item.ingotIron,
				'g', Block.glass,
				'o', componentItem.getIS(4),
				'c', componentItem.getIS(5),
				'p', componentItem.getIS(6)));
	}
	
	/*private void initIC2Recipes()
	{
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
			'd', Block.blockDiamond);
		
		GameRegistry.addRecipe(new ItemStack(airgenBlock), "lcl", "lml", "lll",
			'l', Block.leaves,
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
			'g', Block.glass);
		
		GameRegistry.addRecipe(new ItemStack(monitorBlock), "gcg", "gmg", "ggg", 
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"),
			'g', Block.glass);
		
		GameRegistry.addRecipe(new ItemStack(scannerBlock), "sgs", "mma", "amm",
			'm', WarpDriveConfig.getIC2Item("advancedMachine"),
			'a', WarpDriveConfig.getIC2Item("advancedAlloy"),
			's', WarpDriveConfig.getIC2Item("advancedCircuit"),
			'g', Block.glass);	
	
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(laserTreeFarmBlock),false,new Object[] {
			"cwc", "wmw", "cwc",
			'c', WarpDriveConfig.getIC2Item("electronicCircuit"),
			'w', "logWood",
			'm', miningLaserBlock }));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(transporterBlock), false, new Object[] {
			"ece", "imi", "iei",
			'e', Item.enderPearl,
			'c', WarpDriveConfig.getIC2Item("electronicCircuit"),
			'i', WarpDriveConfig.getIC2Item("plateiron"),
			'm', WarpDriveConfig.getIC2Item("machine") }));
		
		GameRegistry.addRecipe(new ItemStack(cloakBlock), "imi", "mcm", "imi", 
			'i', iridiumBlock,
			'c', cloakCoilBlock,
			'm', WarpDriveConfig.getIC2Item("advancedMachine"));
		
		GameRegistry.addRecipe(new ItemStack(cloakCoilBlock), "iai", "aca", "iai", 
			'i', WarpDriveConfig.getIC2Item("iridiumPlate"),
			'c', WarpDriveConfig.getIC2Item("advancedCircuit"),
			'a', WarpDriveConfig.getIC2Item("advancedAlloy"));
	}*/

	private void registerSpaceDimension() {
		spaceBiome = (new BiomeSpace(23))
			.setColor(0)
			.setDisableRain()
			.setBiomeName("Space");
		this.spaceProviderID = 14;
		DimensionManager.registerProviderType(this.spaceProviderID, SpaceProvider.class, true);
		this.spaceDimID = DimensionManager.getNextFreeDimId();
		DimensionManager.registerDimension(this.spaceDimID, this.spaceProviderID);
	}

	private void registerHyperSpaceDimension() {
		this.hyperSpaceProviderID = 15;
		DimensionManager.registerProviderType(this.hyperSpaceProviderID, HyperSpaceProvider.class, true);
		this.hyperSpaceDimID = DimensionManager.getNextFreeDimId();
		DimensionManager.registerDimension(this.hyperSpaceDimID, this.hyperSpaceProviderID);
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
	
	private ArrayList<Ticket> worldTickets(World worldObj)
	{
		ArrayList<Ticket> ticks = new ArrayList<Ticket>();
		for(Ticket t: warpTickets)
			if(t.world.equals(worldObj))
				ticks.add(t);
		return ticks;
	}
	
	public Ticket registerChunkLoadTE(WarpChunkTE te,boolean refreshLoading)
	{
		World worldObj = te.worldObj;
		ArrayList<Ticket> worldTicks = worldTickets(worldObj);
		boolean isWorldTicketed = worldTicks.size() != 0;
		if(isWorldTicketed)
		{
			if(ForgeChunkManager.ticketCountAvailableFor(this, worldObj) > 0)
			{
				Ticket t = ForgeChunkManager.requestTicket(this, worldObj, Type.NORMAL);
				if(t != null)
				{
					te.giveTicket(t);
					if(refreshLoading)
						te.refreshLoading();
					return t;
				}
				else
					WarpDrive.debugPrint("Ticket not granted");
			}
			else
				WarpDrive.debugPrint("No tickets left!");
		}
		else
		{
			Ticket t = ForgeChunkManager.requestTicket(this, worldObj, Type.NORMAL);
			if(t != null)
			{
				te.giveTicket(t);
				if(refreshLoading)
					te.refreshLoading();
				return t;
			}
			else
			{
				WarpDrive.debugPrint("Ticket not granted");
			}
		}
		return null;
	}
	
	public Ticket registerChunkLoadTE(WarpChunkTE te)
	{
		return registerChunkLoadTE(te,true);
	}
	
	public Ticket getTicket(WarpChunkTE te)
	{
		return registerChunkLoadTE(te,false);
	}
	
	public void removeTicket(Ticket t)
	{
		for(Ticket ticket:warpTickets)
			if(t.equals(ticket))
				warpTickets.remove(ticket);
	}

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world)
	{
		for (Ticket ticket : tickets)
			ForgeChunkManager.releaseTicket(ticket);
	}
}