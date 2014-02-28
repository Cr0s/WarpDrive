package cr0s.WarpDrive;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;

public class BlockIridium extends Block
{
    public BlockIridium(int par1)
    {
        super(par1, Material.rock);
    }

    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("warpdrive:iridiumSide");
    }

    @Override
    public int idDropped(int var1, Random var2, int var3)
    {
        return this.blockID;
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 1;
    }
}