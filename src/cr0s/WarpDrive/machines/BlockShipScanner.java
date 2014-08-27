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

public class BlockShipScanner extends BlockContainer {
    private Icon[] iconBuffer;

    public BlockShipScanner(int id, int texture, Material material) {
        super(id, material);
        setHardness(0.5F);
        setStepSound(Block.soundMetalFootstep);
        setCreativeTab(WarpDrive.warpdriveTab);
        setUnlocalizedName("warpdrive.machines.Scanner");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        iconBuffer = new Icon[3];
        iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:shipScannerUp");
        iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:shipScannerSide");
        iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:contBottom");
    }

    @Override
    public Icon getIcon(int side, int metadata) {
        if (side == 1) { // UP
        	return iconBuffer[0];
        } else if (side == 0) { // DOWN
            return iconBuffer[2];
        }

        return iconBuffer[1];
    }

    @Override
    public TileEntity createNewTileEntity(World var1) {
        return new TileEntityShipScanner();
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

    /**
     * Called upon block activation (right click on the block.)
     */
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
}