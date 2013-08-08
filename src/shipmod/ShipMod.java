package shipmod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import ic2.api.item.Items;

import java.util.Collections;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.command.CommandBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import shipmod.blockitem.BlockMarkShip;
import shipmod.blockitem.ItemCreateShip;
import shipmod.command.CommandHelp;
import shipmod.command.CommandAlignShip;
import shipmod.command.CommandDismountShip;
import shipmod.command.CommandKillShip;
import shipmod.command.CommandReloadMetaRotations;
import shipmod.command.CommandShipInfo;
import shipmod.command.CommandTpShip;
import shipmod.entity.EntityShip;

@Mod(
    modid = "ShipMod",
    name = "ShipMod",
    version = "1.6.2 v0.0.1"
)
@NetworkMod(
    clientSideRequired = true,
    serverSideRequired = true,
    packetHandler = PacketHandler.class,
    connectionHandler = ConnectionHandler.class,
    channels = {"shipControl", "shipInteract", "reqShipSigns", "shipSigns"}
)
public class ShipMod
{
    @Instance("ShipMod")
    public static ShipMod instance;
    @SidedProxy(
        clientSide = "shipmod.ClientProxy",
        serverSide = "shipmod.CommonProxy"
    )
    public static CommonProxy proxy;
    public static Logger modLogger;
    public static ItemCreateShip itemCreateVehicle;
    public static BlockMarkShip blockMarkShip;
    public static Material materialFloater;
    public ShipModConfig modConfig;
    public MetaRotations metaRotations = new MetaRotations();
    private ForgeHookContainer hookContainer = new ForgeHookContainer();

    @EventHandler
    public void preInitMod(FMLPreInitializationEvent event)
    {
        modLogger = event.getModLog();
        this.modConfig = new ShipModConfig(new Configuration(event.getSuggestedConfigurationFile()));
        this.modConfig.loadAndSave();
        this.metaRotations.setConfigDirectory(event.getModConfigurationDirectory());
    }

    @EventHandler
    public void initMod(FMLInitializationEvent event)
    {
        blockMarkShip = (BlockMarkShip)(new BlockMarkShip(this.modConfig.blockMarkShipID)).setUnlocalizedName("markShip").func_111022_d("markShip").setCreativeTab(CreativeTabs.tabTransport);
        blockMarkShip.setStepSound(Block.soundMetalFootstep).setHardness(2.0F).setResistance(15.0F);
        GameRegistry.registerBlock(blockMarkShip, "markShip");
        
        GameRegistry.addRecipe(new ItemStack(blockMarkShip), "ici", "cec", "ici",
        'i', Items.getItem("iridiumPlate"), 'e', Item.enderPearl, 'c', Items.getItem("advancedCircuit"));
        
        EntityRegistry.registerModEntity(EntityShip.class, "shipmod", 1, this, 64, this.modConfig.shipEntitySyncRate, true);
        
        proxy.registerTickHandlers();
        proxy.registerLocalization();
        proxy.registerRenderers();
        MinecraftForge.EVENT_BUS.register(this.hookContainer);
    }

    @EventHandler
    public void postInitMod(FMLPostInitializationEvent event)
    {
        this.metaRotations.readMetaRotationFiles();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        this.registerASCommand(event, new CommandHelp());
        this.registerASCommand(event, new CommandReloadMetaRotations());
        this.registerASCommand(event, new CommandDismountShip());
        this.registerASCommand(event, new CommandShipInfo());
        this.registerASCommand(event, new CommandKillShip());
        this.registerASCommand(event, new CommandAlignShip());
        this.registerASCommand(event, new CommandTpShip());
        Collections.sort(CommandHelp.asCommands);
    }

    private void registerASCommand(FMLServerStartingEvent event, CommandBase commandbase)
    {
        event.registerServerCommand(commandbase);
        CommandHelp.asCommands.add(commandbase);
    }
}
