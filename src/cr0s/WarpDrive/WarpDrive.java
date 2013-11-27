package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import java.util.List;

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
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = "WarpDrive", name = "WarpDrive", version = "1.1.5_ZLO", dependencies="required-after:IC2; required-after:ComputerCraft; after:CCTurtle; after:gregtech_addon; after:AppliedEnergistics; after:AdvancedSolarPanel; after:AtomicScience; after:ICBM|Explosion; after:MFFS; after:GraviSuite")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, channels = {"WarpDriveBeam", "WarpDriveFreq", "WarpDriveLaserT"}, packetHandler = PacketHandler.class)
/**
 * @author Cr0s
 */
public class WarpDrive implements LoadingCallback
{
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
    public static Block liftBlock;

    public static Block airBlock;
    public static Block gasBlock;

    public static Block iridiumBlock;

    public static BiomeGenBase spaceBiome;
    public World space;
    private int spaceProviderID;
    public int spaceDimID;
    public SpaceWorldGenerator spaceWorldGenerator;
    public HyperSpaceWorldGenerator hyperSpaceWorldGenerator;

    public World hyperSpace;
    private int hyperSpaceProviderID;
    public int hyperSpaceDimID;

    @Instance("WarpDrive")
    public static WarpDrive instance;
    @SidedProxy(clientSide = "cr0s.WarpDrive.ClientProxy", serverSide = "cr0s.WarpDrive.CommonProxy")
    public static CommonProxy proxy;

    public WarpCoresRegistry registry;
    public JumpGatesRegistry jumpGates;

    public CamRegistry cams;
    public boolean isOverlayEnabled = false;
    public int overlayType = 0;

    @EventHandler
    //@PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
        WarpDriveConfig.Init(new Configuration(event.getSuggestedConfigurationFile()));

        if (FMLCommonHandler.instance().getSide().isClient())
        {
            System.out.println("[WarpDrive] Registering sounds event handler...");
            MinecraftForge.EVENT_BUS.register(new SoundHandler());
        }
    }

    @Init
    public void load(FMLInitializationEvent event)
    {
	WarpDriveConfig.i.Init2();
        // WARP CORE
        this.warpCore = new BlockReactor(WarpDriveConfig.i.coreID, 0, Material.rock)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Warp Core");
        LanguageRegistry.addName(warpCore, "Warp Core");
        GameRegistry.registerBlock(warpCore, "warpCore");
        GameRegistry.registerTileEntity(TileEntityReactor.class, "warpCore");
        // CORE CONTROLLER
        this.protocolBlock = new BlockProtocol(WarpDriveConfig.i.controllerID, 0, Material.rock)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Warp Controller");
        LanguageRegistry.addName(protocolBlock, "Warp Controller");
        GameRegistry.registerBlock(protocolBlock, "protocolBlock");
        GameRegistry.registerTileEntity(TileEntityProtocol.class, "protocolBlock");
        // WARP RADAR
        this.radarBlock = new BlockRadar(WarpDriveConfig.i.radarID, 0, Material.rock)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("W-Radar");
        LanguageRegistry.addName(radarBlock, "W-Radar");
        GameRegistry.registerBlock(radarBlock, "radarBlock");
        GameRegistry.registerTileEntity(TileEntityRadar.class, "radarBlock");
        // WARP ISOLATION
        this.isolationBlock = new BlockWarpIsolation(WarpDriveConfig.i.isolationID, 0, Material.rock)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Warp-Field Isolation Block");
        LanguageRegistry.addName(isolationBlock, "Warp-Field Isolation Block");
        GameRegistry.registerBlock(isolationBlock, "isolationBlock");
        // AIR GENERATOR
        this.airgenBlock = new BlockAirGenerator(WarpDriveConfig.i.airgenID, 0, Material.rock)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Air Generator");
        LanguageRegistry.addName(airgenBlock, "Air Generator");
        GameRegistry.registerBlock(airgenBlock, "airgenBlock");
        GameRegistry.registerTileEntity(TileEntityAirGenerator.class, "airgenBlock");
        // AIR BLOCK
        this.airBlock = (new BlockAir(WarpDriveConfig.i.airID)).setHardness(0.0F).setUnlocalizedName("Air block");
        LanguageRegistry.addName(airBlock, "Air block");
        GameRegistry.registerBlock(airBlock, "airBlock");
        // GAS BLOCK
        this.gasBlock = (new BlockGas(WarpDriveConfig.i.gasID)).setHardness(0.0F).setUnlocalizedName("Gas block");
        LanguageRegistry.addName(gasBlock, "Gas block");
        GameRegistry.registerBlock(gasBlock, "gasBlock");
        // LASER EMITTER
        this.laserBlock = new BlockLaser(WarpDriveConfig.i.laserID, 0, Material.rock)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Laser Emitter");
        LanguageRegistry.addName(laserBlock, "Laser Emitter");
        GameRegistry.registerBlock(laserBlock, "laserBlock");
        GameRegistry.registerTileEntity(TileEntityLaser.class, "laserBlock");
        // LASER EMITTER WITH CAMERA
        this.laserCamBlock = new BlockLaserCam(WarpDriveConfig.i.laserCamID, 0, Material.rock)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Laser Emitter + Camera");
        LanguageRegistry.addName(laserCamBlock, "Laser Emitter + Camera");
        GameRegistry.registerBlock(laserCamBlock, "laserCamBlock");
        // CAMERA
        this.cameraBlock = new BlockCamera(WarpDriveConfig.i.camID, 0, Material.rock)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Camera block");
        LanguageRegistry.addName(cameraBlock, "Camera");
        GameRegistry.registerBlock(cameraBlock, "cameraBlock");
        GameRegistry.registerTileEntity(TileEntityCamera.class, "cameraBlock");
        // MONITOR
        this.monitorBlock = new BlockMonitor(WarpDriveConfig.i.monitorID)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Monitor");
        LanguageRegistry.addName(monitorBlock, "Monitor");
        GameRegistry.registerBlock(monitorBlock, "monitorBlock");
        GameRegistry.registerTileEntity(TileEntityMonitor.class, "monitorBlock");
        // MINING LASER
        this.miningLaserBlock = new BlockMiningLaser(WarpDriveConfig.i.miningLaserID, 0, Material.rock)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Mining Laser");
        LanguageRegistry.addName(miningLaserBlock, "Mining Laser");
        GameRegistry.registerBlock(miningLaserBlock, "miningLaserBlock");
        GameRegistry.registerTileEntity(TileEntityMiningLaser.class, "miningLaserBlock");
        // PARTICLE BOOSTER
        this.boosterBlock = new BlockParticleBooster(WarpDriveConfig.i.particleBoosterID, 0, Material.rock)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Particle Booster");
        LanguageRegistry.addName(boosterBlock, "Particle Booster");
        GameRegistry.registerBlock(boosterBlock, "boosterBlock");
        GameRegistry.registerTileEntity(TileEntityParticleBooster.class, "boosterBlock");
        // RAY LIFT
        this.liftBlock = new BlockLift(WarpDriveConfig.i.liftID, 0, Material.rock)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Laser lift");
        LanguageRegistry.addName(liftBlock, "Laser lift");
        GameRegistry.registerBlock(liftBlock, "liftBlock");
        GameRegistry.registerTileEntity(TileEntityLift.class, "liftBlock");
        // IRIDIUM BLOCK
        this.iridiumBlock = new BlockIridium(WarpDriveConfig.i.iridiumID)
        .setHardness(0.8F).setResistance(150 * 4).setStepSound(Block.soundMetalFootstep)
        .setCreativeTab(CreativeTabs.tabRedstone).setUnlocalizedName("Block of Iridium");
        LanguageRegistry.addName(iridiumBlock, "Block of Iridium");
        GameRegistry.registerBlock(iridiumBlock, "iridiumBlock");
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
            MinecraftForge.EVENT_BUS.register(new CameraOverlay(Minecraft.getMinecraft()));
        }
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event)
    {
        space = DimensionManager.getWorld(spaceDimID);
        hyperSpace = DimensionManager.getWorld(hyperSpaceDimID);
        GameRegistry.addRecipe(new ItemStack(warpCore), "ici", "cmc", "ici",
                               'i', WarpDriveConfig.i.getIC2Item("iridiumPlate"), 'm', WarpDriveConfig.i.getIC2Item("advancedMachine"), 'c', WarpDriveConfig.i.getIC2Item("advancedCircuit"));
        GameRegistry.addRecipe(new ItemStack(protocolBlock), "iic", "imi", "cii",
                               'i', WarpDriveConfig.i.getIC2Item("iridiumPlate"), 'm', WarpDriveConfig.i.getIC2Item("advancedMachine"), 'c', WarpDriveConfig.i.getIC2Item("advancedCircuit"));
        GameRegistry.addRecipe(new ItemStack(radarBlock), "ifi", "imi", "imi",
                               'i', WarpDriveConfig.i.getIC2Item("iridiumPlate"), 'm', WarpDriveConfig.i.getIC2Item("advancedMachine"), 'f', WarpDriveConfig.i.getIC2Item("frequencyTransmitter"));
        GameRegistry.addRecipe(new ItemStack(isolationBlock), "iii", "idi", "iii",
                               'i', WarpDriveConfig.i.getIC2Item("iridiumPlate"), 'm', WarpDriveConfig.i.getIC2Item("advancedMachine"), 'd', Block.blockDiamond);
        GameRegistry.addRecipe(new ItemStack(airgenBlock), "lcl", "lml", "lll",
                               'l', Block.leaves, 'm', WarpDriveConfig.i.getIC2Item("advancedMachine"), 'c', WarpDriveConfig.i.getIC2Item("advancedCircuit"));
        GameRegistry.addRecipe(new ItemStack(laserBlock), "sss", "ama", "aaa",
                               'm', WarpDriveConfig.i.getIC2Item("advancedMachine"), 'a', WarpDriveConfig.i.Compot, 's', WarpDriveConfig.i.getIC2Item("advancedCircuit"));
        GameRegistry.addRecipe(new ItemStack(miningLaserBlock), "aaa", "ama", "ccc",
                               'c', WarpDriveConfig.i.getIC2Item("advancedCircuit"), 'a', WarpDriveConfig.i.Compot, 'm', WarpDriveConfig.i.getIC2Item("miner"));
        GameRegistry.addRecipe(new ItemStack(boosterBlock), "afc", "ama", "cfa",
                               'c', WarpDriveConfig.i.getIC2Item("advancedCircuit"), 'a', WarpDriveConfig.i.Compot, 'f', WarpDriveConfig.i.getIC2Item("glassFiberCableItem"), 'm', WarpDriveConfig.i.getIC2Item("mfeUnit"));
        GameRegistry.addRecipe(new ItemStack(liftBlock), "aca", "ama", "a#a",
                               'c', WarpDriveConfig.i.getIC2Item("advancedCircuit"), 'a', WarpDriveConfig.i.Compot, 'm', WarpDriveConfig.i.getIC2Item("magnetizer"));
        /*
                GameRegistry.addRecipe(new ItemStack(Item.enderPearl), "uuu", "uuu", "uuu",
                        'u', WarpDriveConfig.i.getIC2Item("uraniumDrop"));
        */
        GameRegistry.addRecipe(new ItemStack(iridiumBlock), "iii", "iii", "iii",
                               'i', WarpDriveConfig.i.getIC2Item("iridiumPlate"));
        GameRegistry.addShapelessRecipe(new ItemStack(WarpDriveConfig.i.getIC2Item("iridiumPlate").getItem(), 9), new ItemStack(iridiumBlock));
        GameRegistry.addRecipe(new ItemStack(laserCamBlock), "imi", "cec", "#k#",
                               'i', WarpDriveConfig.i.getIC2Item("iridiumPlate"), 'm', WarpDriveConfig.i.getIC2Item("advancedMachine"), 'c', WarpDriveConfig.i.getIC2Item("advancedCircuit"), 'e', laserBlock, 'k', cameraBlock);
        GameRegistry.addRecipe(new ItemStack(cameraBlock), "cgc", "gmg", "cgc",
                               'm', WarpDriveConfig.i.getIC2Item("advancedMachine"), 'c', WarpDriveConfig.i.getIC2Item("advancedCircuit"), 'g', Block.glass);
        GameRegistry.addRecipe(new ItemStack(monitorBlock), "gcg", "gmg", "ggg",
                               'm', WarpDriveConfig.i.getIC2Item("advancedMachine"), 'c', WarpDriveConfig.i.getIC2Item("advancedCircuit"), 'g', Block.glass);
        registry = new WarpCoresRegistry();

        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            jumpGates = new JumpGatesRegistry();
        }
        else
        {
            cams = new CamRegistry();
        }
    }

    private void registerSpaceDimension()
    {
        spaceBiome = (new BiomeSpace(23)).setColor(0).setDisableRain().setBiomeName("Space");
        this.spaceProviderID = 14;
        DimensionManager.registerProviderType(this.spaceProviderID, SpaceProvider.class, true);
        this.spaceDimID = DimensionManager.getNextFreeDimId();
        DimensionManager.registerDimension(this.spaceDimID, this.spaceProviderID);
    }

    private void registerHyperSpaceDimension()
    {
        this.hyperSpaceProviderID = 15;
        DimensionManager.registerProviderType(this.hyperSpaceProviderID, HyperSpaceProvider.class, true);
        this.hyperSpaceDimID = DimensionManager.getNextFreeDimId();
        DimensionManager.registerDimension(this.hyperSpaceDimID, this.hyperSpaceProviderID);
    }

    @ServerStarting
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new GenerateCommand());
        event.registerServerCommand(new SpaceTpCommand());
        event.registerServerCommand(new InvisibleCommand());
    }

    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world)
    {
        for (Ticket ticket : tickets)
        {
            ForgeChunkManager.releaseTicket(ticket);
        }
    }
}