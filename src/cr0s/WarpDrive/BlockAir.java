package cr0s.WarpDrive;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockAir extends Block
{
    private final boolean TRANSPARENT_AIR = true;

    public BlockAir(int par1)
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
        return TRANSPARENT_AIR ? 1 : 0;
    }

    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("warpdrive:airBlock");
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

    /**
     * How many world ticks before ticking
     */
    @Override
    public int tickRate(World par1World)
    {
        return 20;
    }

    /**
     * Ticks the block if it's been scheduled
     */
    @Override
    public void updateTick(World par1World, int x, int y, int z, Random par5Random)
    {
        int concentration = par1World.getBlockMetadata(x, y, z);
        boolean isInSpaceWorld = par1World.provider.dimensionId == WarpDrive.instance.spaceDimID || par1World.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID;

        // Remove air block to vacuum block
        if (concentration <= 0 || !isInSpaceWorld)
        {
            //System.out.println("Killing air block");
            par1World.setBlock(x, y, z, 0, 0, 2); // replace our air block to vacuum block
        }
        else
        {
            //System.out.println("Conc: current " + concentration + " new: " + (concentration - 1) + " to spread: " + (concentration - 2));
            // Try to spread the air
            spreadAirBlock(par1World, x, y, z, concentration);
        }

        par1World.scheduleBlockUpdate(x, y, z, this.blockID, 20);
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

    private void spreadAirBlock(World worldObj, int x, int y, int z, int concentration)
    {
        if (concentration <= 0)
        {
            return;
        }

        int mid_concentration;
        int block_count = 1;
        //final int K = 128;
        mid_concentration = worldObj.getBlockMetadata(x, y, z);// * K;

        // Count air in adjacent blocks
        if (worldObj.isAirBlock(x + 1, y, z))
        {
            block_count++;
            mid_concentration += worldObj.getBlockMetadata(x + 1, y, z);// * K;
        }

        if (worldObj.isAirBlock(x - 1, y, z))
        {
            block_count++;
            mid_concentration += worldObj.getBlockMetadata(x - 1, y, z);// * K;
        }

        if (worldObj.isAirBlock(x, y + 1, z))
        {
            block_count++;
            mid_concentration += worldObj.getBlockMetadata(x, y + 1, z);// * K;
        }

        if (worldObj.isAirBlock(x, y - 1, z))
        {
            block_count++;
            mid_concentration += worldObj.getBlockMetadata(x, y - 1, z);// * K;
        }

        if (worldObj.isAirBlock(x, y, z + 1))
        {
            block_count++;
            mid_concentration += worldObj.getBlockMetadata(x, y, z + 1);// * K;
        }

        if (worldObj.isAirBlock(x, y, z - 1))
        {
            block_count++;
            mid_concentration += worldObj.getBlockMetadata(x, y, z - 1);// * K;
        }

        mid_concentration = (int) Math.floor(mid_concentration * 1.0f / block_count);
        setNewAirBlockWithConcentration(worldObj, x, y, z, mid_concentration);// / K);

        // Check and setup air to adjacent blocks
        if (worldObj.isAirBlock(x + 1, y, z) && (mid_concentration > worldObj.getBlockMetadata(x + 1, y, z)))// * K))
        {
           setNewAirBlockWithConcentration(worldObj, x + 1, y, z, mid_concentration);// / K);
        }

        if (worldObj.isAirBlock(x - 1, y, z) && (mid_concentration > worldObj.getBlockMetadata(x - 1, y, z)))// * K))
        {
           setNewAirBlockWithConcentration(worldObj, x - 1, y, z, mid_concentration);// / K);
        }

        if (worldObj.isAirBlock(x, y + 1, z) && (mid_concentration > worldObj.getBlockMetadata(x, y + 1, z)))// * K))
        {
           setNewAirBlockWithConcentration(worldObj, x, y + 1, z, mid_concentration);// / K);
        }

        if (worldObj.isAirBlock(x, y - 1, z) && (mid_concentration > worldObj.getBlockMetadata(x, y - 1, z)))// * K))
        {
           setNewAirBlockWithConcentration(worldObj, x, y - 1, z, mid_concentration);//  / K);
        }

        if (worldObj.isAirBlock(x, y, z + 1) && (mid_concentration > worldObj.getBlockMetadata(x, y, z + 1)))// * K))
        {
           setNewAirBlockWithConcentration(worldObj, x, y, z + 1, mid_concentration);// / K);
        }

        if (worldObj.isAirBlock(x, y, z - 1) && (mid_concentration > worldObj.getBlockMetadata(x, y, z - 1)))// * K))
        {
           setNewAirBlockWithConcentration(worldObj, x, y, z - 1, mid_concentration);// / K);
        }
    }

    private void setNewAirBlockWithConcentration(World worldObj, int x, int y, int z, int concentration)
    {
        worldObj.setBlock(x, y, z, this.blockID, concentration, 2);
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
        if (par1World.provider.dimensionId == WarpDrive.instance.spaceDimID || par1World.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID)
        {
            par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate(par1World));
        }
        else
        {
            par1World.setBlockToAir(par2, par3, par4);
        }
    }
}