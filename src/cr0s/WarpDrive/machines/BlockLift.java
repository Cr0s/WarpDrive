package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockLift extends BlockContainer
{
    private Icon[] iconBuffer;

    public BlockLift(int id, int texture, Material material)
    {
        super(id, material);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        iconBuffer = new Icon[6];
        iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:liftSideOffline");
        iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:liftSideUp");
        iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:liftSideDown");
        iconBuffer[3] = par1IconRegister.registerIcon("warpdrive:liftUpInactive");
        iconBuffer[4] = par1IconRegister.registerIcon("warpdrive:liftUpOut");
        iconBuffer[5] = par1IconRegister.registerIcon("warpdrive:liftUpIn");
    }

    @Override
    public Icon getIcon(int side, int metadata)
    {
        if (side == 1)
        {
            return iconBuffer[3 + metadata];
        }
        else if (side == 0)
        {
            if (metadata == 0)
            {
                return iconBuffer[3];
            }
            else
            {
                return iconBuffer[6 - metadata];
            }
        }

        return iconBuffer[metadata];
    }

    @Override
    public TileEntity createNewTileEntity(World var1)
    {
        return new TileEntityLift();
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random)
    {
        return 1;
    }

    /**
     * Returns the ID of the items to drop on destruction.
     */
    @Override
    public int idDropped(int par1, Random par2Random, int par3)
    {
        return this.blockID;
    }

    /**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return false;
        }

        TileEntityLift booster = (TileEntityLift)par1World.getBlockTileEntity(par2, par3, par4);

        if (booster != null)
        {
            par5EntityPlayer.addChatMessage("[Laser Lift] Energy level: " + booster.getCurrentEnergyValue());
        }

        return true;
    }

    @Override
    public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
    {
        TileEntity te = par1World.getBlockTileEntity(par2, par3, par4);

        if (te != null)
        {
            te.invalidate();
        }
    }
}