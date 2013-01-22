package cr0s.WarpDrive;

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
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

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
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event) {
        // Stub Method
    }
}