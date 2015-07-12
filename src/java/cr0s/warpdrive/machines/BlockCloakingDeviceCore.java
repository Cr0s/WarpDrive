package cr0s.warpdrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockCloakingDeviceCore extends BlockContainer {
    private Icon[] iconBuffer;

    public BlockCloakingDeviceCore(int id, int texture, Material material) {
        super(id, material);
        setHardness(0.5F);
        setStepSound(Block.soundMetalFootstep);
        setCreativeTab(WarpDrive.warpdriveTab);
        setUnlocalizedName("warpdrive.machines.CloakingDeviceCore");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        iconBuffer = new Icon[2];
        iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:cloakingCoreInactive");
        iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:cloakingCoreActive");
    }

    @Override
    public Icon getIcon(int side, int metadata) {
    	if (metadata < iconBuffer.length) {
    		return iconBuffer[metadata];
    	} else {
    		return null;
    	}
    }

    @Override
    public TileEntity createNewTileEntity(World var1) {
        return new TileEntityCloakingDeviceCore();
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

        TileEntityCloakingDeviceCore te = (TileEntityCloakingDeviceCore)par1World.getBlockTileEntity(par2, par3, par4);
        if (te != null && (par5EntityPlayer.getHeldItem() == null)) {
        	par5EntityPlayer.addChatMessage(te.getStatus() + "\n"
//        			+ " Valid? " + te.isValid + " Cloaking? " + te.isCloaking + " Enabled? " + te.isEnabled + "\n"
        			+ ((!te.isValid) ? "Invalid assembly!" :
        				((!te.isEnabled) ? "Cloak is disabled" :
        					((te.isCloaking) ? "A tier " + te.tier + " cloak is currently covering " + te.volume + " blocks!" : "Cloak needs more power!"))));
            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6) {
        TileEntity te = par1World.getBlockTileEntity(par2, par3, par4);

        if (te != null && te instanceof TileEntityCloakingDeviceCore) {
        	((TileEntityCloakingDeviceCore)te).isEnabled = false;
       		((TileEntityCloakingDeviceCore)te).disableCloakingField();
        }

        super.breakBlock(par1World, par2, par3, par4, par5, par6);
    }
}
