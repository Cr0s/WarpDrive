package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.WarpDrive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockRadar extends BlockContainer
{
    private Icon[] iconBuffer;

    private final int ICON_SIDE_INACTIVE = 0;
    private final int ICON_BOTTOM = 1;
    private final int ICON_TOP = 2;
    private final int ICON_SIDE_ACTIVATED = 3;
    private final int ICON_SIDE_ACTIVATED_SCAN = 4;

    public BlockRadar(int id, int texture, Material material) {
        super(id, material);
        setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("warpdrive.machines.WarpRadar");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        iconBuffer = new Icon[16];
        iconBuffer[ICON_SIDE_INACTIVE] = par1IconRegister.registerIcon("warpdrive:radarSideInactive");
        iconBuffer[ICON_BOTTOM] = par1IconRegister.registerIcon("warpdrive:contBottom");
        iconBuffer[ICON_TOP] = par1IconRegister.registerIcon("warpdrive:contTop");
        iconBuffer[ICON_SIDE_ACTIVATED] = par1IconRegister.registerIcon("warpdrive:radarSideActive");
        iconBuffer[ICON_SIDE_ACTIVATED_SCAN] = par1IconRegister.registerIcon("warpdrive:radarSideActiveScan");
    }

    @Override
    public Icon getIcon(int side, int metadata) {
        if (side == 0) {
            return iconBuffer[ICON_BOTTOM];
        } else if (side == 1) {
            return iconBuffer[ICON_TOP];
        }

        if (metadata == 0) {// Inactive state
            return iconBuffer[ICON_SIDE_INACTIVE];
        } else if (metadata == 1) { // Attached state
            return iconBuffer[ICON_SIDE_ACTIVATED];
        } else if (metadata == 2) { // Scanning state
            return iconBuffer[ICON_SIDE_ACTIVATED_SCAN];
        }

        return iconBuffer[ICON_SIDE_INACTIVE];
    }

    @Override
    public TileEntity createNewTileEntity(World var1) {
        return new TileEntityRadar();
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random) {
        return 1;
    }

    /**
     * Returns the ID of the items to drop on destruction.
     */
    @Override
    public int idDropped(int par1, Random par2Random, int par3) {
        return this.blockID;
    }

    @Override
    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return false;
        }

        WarpEnergyTE te = (WarpEnergyTE)par1World.getBlockTileEntity(par2, par3, par4);
        if (te != null && (par5EntityPlayer.getHeldItem() == null)) {
        	par5EntityPlayer.addChatMessage(te.getStatus());
            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6) {
        TileEntity te = par1World.getBlockTileEntity(par2, par3, par4);
        if (te != null) {
            te.invalidate();
        }

        super.breakBlock(par1World, par2, par3, par4, par5, par6);
    }
}
