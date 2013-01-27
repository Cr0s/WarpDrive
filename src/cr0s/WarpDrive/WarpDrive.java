package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;

@Mod(modid = "WarpDrive", name = "WarpDrive", version = "0.0.1")
@NetworkMod(clientSideRequired = false, serverSideRequired = true)
/**
 * @author Cr0s
 */
public class WarpDrive {

    public final static int WARP_CORE_BLOCKID = 500;
    public final static Block warpCore = new BlockReactor(WARP_CORE_BLOCKID, 0, Material.ground)
            .setHardness(0.5F).setStepSound(Block.soundMetalFootstep)
            .setBlockName("warpCore").setCreativeTab(CreativeTabs.tabRedstone);
    /**
     *
     */
    public static BiomeGenBase spaceBiome;
    public World space;
    private int spaceProviderID;
    public int spaceDimID;
    public SpaceWorldGenerator spaceWorldGenerator;
    @Instance("WarpDrive")
    public static WarpDrive instance;
    @SidedProxy(clientSide = "cr0s.WarpDrive.client.ClientProxy", serverSide = "cr0s.WarpDrive.CommonProxy")
    public static CommonProxy proxy;

    @PreInit
    public void preInit(FMLPreInitializationEvent event) {
        // Stub Method
    }

    @Init
    public void load(FMLInitializationEvent event) {
        LanguageRegistry.addName(warpCore, "Warp-drive Reactor Core");
        GameRegistry.registerBlock(warpCore, "warpCore");
        GameRegistry.registerTileEntity(TileEntityReactor.class, "warpCore");

        proxy.registerRenderers();
        proxy.registerJumpEntity();

        //if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
        spaceWorldGenerator = new SpaceWorldGenerator();
        GameRegistry.registerWorldGenerator(spaceWorldGenerator);
        
        registerSpaceDimension();
        //}
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event) {
        space = DimensionManager.getWorld(spaceDimID);
    }

    //@SideOnly(Side.SERVER)
    private void registerSpaceDimension() {
        spaceBiome = (new BiomeSpace(23)).setColor(9286496).setDisableRain().setBiomeName("Space");
        this.spaceProviderID = 14;

        DimensionManager.registerProviderType(this.spaceProviderID, SpaceProvider.class, true);
        this.spaceDimID = DimensionManager.getNextFreeDimId();
        DimensionManager.registerDimension(this.spaceDimID, this.spaceProviderID);
    }
}