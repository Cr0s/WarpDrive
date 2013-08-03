package cr0s.WarpDrive;

import cr0s.WarpDrive.WarpDrive;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGas extends Block
{   
    private Icon[] gasIcons;
    
    public BlockGas(int par1)
    {
        super(par1, Material.air);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean isAirBlock(World var1, int var2, int var3, int var4)
    {
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World var1, int var2, int var3, int var4)
    {
        return null;
    }

    @Override
    public boolean isBlockReplaceable(World var1, int var2, int var3, int var4)
    {
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World var1, int var2, int var3, int var4)
    {
        return true;
    }

    @Override
    public boolean canCollideCheck(int var1, boolean var2)
    {
        return false;
    }

    @Override
    public int getRenderBlockPass()
    {
        return 1; // transparency enabled
    }

    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        gasIcons = new Icon[12];
        
        gasIcons[0] = par1IconRegister.registerIcon("warpdrive:gasBlockBlue");
        gasIcons[1] = par1IconRegister.registerIcon("warpdrive:gasBlockRed"); 
        gasIcons[2] = par1IconRegister.registerIcon("warpdrive:gasBlockGreen");
        gasIcons[3] = par1IconRegister.registerIcon("warpdrive:gasBlockYellow"); 
        gasIcons[4] = par1IconRegister.registerIcon("warpdrive:gasBlockDark"); 
        gasIcons[5] = par1IconRegister.registerIcon("warpdrive:gasBlockDarkness"); 
        gasIcons[6] = par1IconRegister.registerIcon("warpdrive:gasBlockWhite"); 
        gasIcons[7] = par1IconRegister.registerIcon("warpdrive:gasBlockMilk"); 
        gasIcons[8] = par1IconRegister.registerIcon("warpdrive:gasBlockOrange"); 
        gasIcons[9] = par1IconRegister.registerIcon("warpdrive:gasBlockSyren"); 
        gasIcons[10] = par1IconRegister.registerIcon("warpdrive:gasBlockGray"); 
        gasIcons[11] = par1IconRegister.registerIcon("warpdrive:gasBlockViolet");
    }
      
    @Override
    public Icon getIcon(int side, int metadata)
    {
        return gasIcons[metadata];
    }     
    
    @Override
    public int getMobilityFlag()
    {
        return 1;
    }

    @Override
    public int idDropped(int var1, Random var2, int var3)
    {
        return -1;
    }


    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        if (par1IBlockAccess.getBlockId(par2, par3, par4) == this.blockID)
        {
            return false;
        }
        else
        {
            final int i = par1IBlockAccess.getBlockId(par2, par3, par4);
            boolean var6 = false;

            if (Block.blocksList[i] != null)
            {
                var6 = !Block.blocksList[i].isOpaqueCube();
            }

            final boolean var7 = i == 0;

            if ((var6 || var7) && par5 == 3 && !var6)
            {
                return true;
            }
            else if ((var6 || var7) && par5 == 4 && !var6)
            {
                return true;
            }
            else if ((var6 || var7) && par5 == 5 && !var6)
            {
                return true;
            }
            else if ((var6 || var7) && par5 == 2 && !var6)
            {
                return true;
            }
            else if ((var6 || var7) && par5 == 0 && !var6)
            {
                return true;
            }
            else if ((var6 || var7) && par5 == 1 && !var6)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }   
    
    @Override
    public boolean func_82506_l()
    {
        return false;
    }

    /**
     * Returns if this block is collidable. Args: x, y, z
     */
    @Override
    public boolean isCollidable()
    {
        return false;
    }

    @Override
    public void onBlockAdded(World par1World, int par2, int par3, int par4)
    {
        // Gas blocks allow only in space
        if (par1World.provider.dimensionId != WarpDrive.instance.spaceDimID)
        {
            par1World.setBlockToAir(par2, par3, par4);
        }
    }
}