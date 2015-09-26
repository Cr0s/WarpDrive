package cr0s.warpdrive;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import org.apache.logging.log4j.Logger;

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
import cr0s.warpdrive.config.Recipes;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CamerasRegistry;
import cr0s.warpdrive.data.CloakManager;
import cr0s.warpdrive.data.JumpgatesRegistry;
import cr0s.warpdrive.data.StarMapRegistry;
import cr0s.warpdrive.event.LivingHandler;
import cr0s.warpdrive.event.WorldHandler;
import cr0s.warpdrive.item.ItemAirCanisterFull;
import cr0s.warpdrive.item.ItemComponent;
import cr0s.warpdrive.item.ItemHelmet;
import cr0s.warpdrive.item.ItemIC2reactorLaserFocus;
import cr0s.warpdrive.item.ItemUpgrade;
import cr0s.warpdrive.network.PacketHandler;
import cr0s.warpdrive.render.ClientCameraHandler;
import cr0s.warpdrive.render.RenderOverlayCamera;
import cr0s.warpdrive.world.BiomeSpace;
import cr0s.warpdrive.world.HyperSpaceWorldProvider;
import cr0s.warpdrive.world.HyperSpaceWorldGenerator;
import cr0s.warpdrive.world.SpaceWorldProvider;
import cr0s.warpdrive.world.SpaceWorldGenerator;

@Mod(modid = WarpDrive.MODID, name = "WarpDrive", version = WarpDrive.VERSION, dependencies = "after:IC2API;" + " after:CoFHCore;" + " after:ComputerCraft;"
		+ " after:OpenComputer;" + " after:CCTurtle;" + " after:gregtech_addon;" + " after:AppliedEnergistics;")
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
	public static CreativeTabs creativeTabWarpDrive = new CreativeTabWarpDrive("WarpDrive", "WarpDrive").setBackgroundImageName("warpdrive:creativeTab");
	
	@Instance(WarpDrive.MODID)
	public static WarpDrive instance;
	@SidedProxy(clientSide = "cr0s.warpdrive.client.ClientProxy", serverSide = "cr0s.warpdrive.CommonProxy")
	public static CommonProxy proxy;
	
	public static StarMapRegistry starMap;
	public static JumpgatesRegistry jumpgates;
	public static CloakManager cloaks;
	public static CamerasRegistry cameras;
	
	public static WarpDrivePeripheralHandler peripheralHandler = null;
	
	public static Logger logger;
	
	@EventHandler
	public void onFMLPreInitialization(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		
		WarpDriveConfig.onFMLpreInitialization(event.getModConfigurationDirectory().getAbsolutePath());
		
		if (FMLCommonHandler.instance().getSide().isClient()) {
			MinecraftForge.EVENT_BUS.register(new RenderOverlayCamera(Minecraft.getMinecraft()));
			
			FMLCommonHandler.instance().bus().register(new ClientCameraHandler());
		}
	}
	
	@EventHandler
	public void onFMLInitialization(FMLInitializationEvent event) {
		PacketHandler.init();
		
		WarpDriveConfig.onFMLInitialization();
		
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
		
		// IC2 REACTOR LASER MONITOR
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
		
		spaceBiome = (new BiomeSpace(WarpDriveConfig.G_SPACE_BIOME_ID)).setColor(0).setDisableRain().setBiomeName("Space");
		BiomeDictionary.registerBiomeType(spaceBiome, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.WASTELAND);
		DimensionManager.registerProviderType(WarpDriveConfig.G_SPACE_PROVIDER_ID, SpaceWorldProvider.class, true);
		DimensionManager.registerDimension(WarpDriveConfig.G_SPACE_DIMENSION_ID, WarpDriveConfig.G_SPACE_PROVIDER_ID);
		
		DimensionManager.registerProviderType(WarpDriveConfig.G_HYPERSPACE_PROVIDER_ID, HyperSpaceWorldProvider.class, true);
		DimensionManager.registerDimension(WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID, WarpDriveConfig.G_HYPERSPACE_PROVIDER_ID);
		
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			creativeTabWarpDrive.setBackgroundImageName("items.png");
		}
	}
	
	@EventHandler
	public void onFMLPostInitialization(FMLPostInitializationEvent event) {
		space = DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID);
		hyperSpace = DimensionManager.getWorld(WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID);
		
		WarpDriveConfig.onFMLPostInitialization();
		
		if (WarpDriveConfig.isIndustrialCraft2loaded && WarpDriveConfig.RECIPES_ENABLE_IC2) {
			Recipes.initIC2();
		}
		if (WarpDriveConfig.isIndustrialCraft2loaded && WarpDriveConfig.RECIPES_ENABLE_HARD_IC2) {
			Recipes.initHardIC2();
		}
		if (WarpDriveConfig.RECIPES_ENABLE_VANILLA) {
			Recipes.initVanilla();
		}
		
		// Registers
		starMap = new StarMapRegistry();
		jumpgates = new JumpgatesRegistry();
		cloaks = new CloakManager();
		cameras = new CamerasRegistry();
		
		// Event handlers
		WorldHandler worldHandler = new WorldHandler();
		MinecraftForge.EVENT_BUS.register(worldHandler);
		FMLCommonHandler.instance().bus().register(worldHandler);
		
		MinecraftForge.EVENT_BUS.register(new LivingHandler());
		
		if (WarpDriveConfig.isComputerCraftLoaded) {
			peripheralHandler = new WarpDrivePeripheralHandler();
			peripheralHandler.register();
		}
	}
	
	@EventHandler
	public void onFMLServerStarting(FMLServerStartingEvent event) {
		WarpDrive.logger.info("onFMLServerStarting");
		event.registerServerCommand(new CommandGenerate());
		event.registerServerCommand(new CommandSpace());
		event.registerServerCommand(new CommandInvisible());
		event.registerServerCommand(new CommandJumpgates());
		event.registerServerCommand(new CommandDebug());
	}
	
	public Ticket registerChunkLoadTE(TileEntityAbstractChunkLoading tileEntity, boolean refreshLoading) {
		World worldObj = tileEntity.getWorldObj();
		if (ForgeChunkManager.ticketCountAvailableFor(this, worldObj) > 0) {
			Ticket t = ForgeChunkManager.requestTicket(this, worldObj, Type.NORMAL);
			if (t != null) {
				tileEntity.giveTicket(t); // FIXME calling the caller is a bad idea
				if (refreshLoading)
					tileEntity.refreshLoading();
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

	public static void addChatMessage(final ICommandSender sender, final String message) {
		String[] lines = message.split("\n");
		for (String line : lines) {
			sender.addChatMessage(new ChatComponentText(line));
		}
	}
	
	@Mod.EventHandler
	public void onFMLMissingMappings(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping: event.get()) {
			if (mapping.type == GameRegistry.Type.ITEM) {
				if (mapping.name.equals("WarpDrive:airBlock")) {
					mapping.remap(Item.getItemFromBlock(blockAir));
				} else if (mapping.name.equals("WarpDrive:airCanisterFull")) {
					mapping.remap(itemAirCanisterFull);
				} else if (mapping.name.equals("WarpDrive:airgenBlock")) {
					mapping.remap(Item.getItemFromBlock(blockAirGenerator));
				} else if (mapping.name.equals("WarpDrive:blockHAMachine")) {
					mapping.remap(Item.getItemFromBlock(blockHighlyAdvancedMachine));
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
				} else if (mapping.name.equals("WarpDrive:blockHAMachine")) {
					mapping.remap(blockHighlyAdvancedMachine);
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
