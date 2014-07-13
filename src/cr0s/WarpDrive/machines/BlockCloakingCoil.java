package cr0s.WarpDrive.machines;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.WarpDrive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockCloakingCoil extends Block
{
    private Icon[] iconBuffer;

    public BlockCloakingCoil(int id, int texture, Material material)
    {
        super(id, material);
        setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("warpdrive.machines.CloakCoil");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        iconBuffer = new Icon[2];
        iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:cloakCoilSide");
        iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:cloakCoilSideActive");
    }

    @Override
    public Icon getIcon(int side, int metadata)
    {
        return iconBuffer[metadata];
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

    @Override
    public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
    {
        TileEntity te = par1World.getBlockTileEntity(par2, par3, par4);

        if (te != null)
        {
            te.invalidate();
        }

        super.breakBlock(par1World, par2, par3, par4, par5, par6);
    }
}
