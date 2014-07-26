package cr0s.WarpDrive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockAir extends Block
{
    private final boolean TRANSPARENT_AIR = true;
    private final boolean AIR_DEBUG = false;
    private final int AIR_BLOCK_TICKS = 20;
    private Icon[] iconBuffer;

    public BlockAir(int par1) {
        super(par1, Material.air);
        setHardness(0.0F);
        setUnlocalizedName("warpdrive.blocks.Air");
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isAirBlock(World var1, int var2, int var3, int var4) {
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World var1, int var2, int var3, int var4) {
        return null;
    }

    @Override
    public boolean isBlockReplaceable(World var1, int var2, int var3, int var4) {
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World var1, int var2, int var3, int var4) {
        return true;
    }

    @Override
    public boolean canCollideCheck(int var1, boolean var2) {
        return false;
    }

    @Override
    public int getRenderBlockPass() {
        return TRANSPARENT_AIR ? 1 : 0;
    }

    @Override
    public void registerIcons(IconRegister par1IconRegister) {
    	if (AIR_DEBUG) {
	        iconBuffer = new Icon[16];
	        iconBuffer[ 0] = par1IconRegister.registerIcon("warpdrive:airBlock0");
	        iconBuffer[ 1] = par1IconRegister.registerIcon("warpdrive:airBlock1");
	        iconBuffer[ 2] = par1IconRegister.registerIcon("warpdrive:airBlock2");
	        iconBuffer[ 3] = par1IconRegister.registerIcon("warpdrive:airBlock3");
	        iconBuffer[ 4] = par1IconRegister.registerIcon("warpdrive:airBlock4");
	        iconBuffer[ 5] = par1IconRegister.registerIcon("warpdrive:airBlock5");
	        iconBuffer[ 6] = par1IconRegister.registerIcon("warpdrive:airBlock6");
	        iconBuffer[ 7] = par1IconRegister.registerIcon("warpdrive:airBlock7");
	        iconBuffer[ 8] = par1IconRegister.registerIcon("warpdrive:airBlock8");
	        iconBuffer[ 9] = par1IconRegister.registerIcon("warpdrive:airBlock9");
	        iconBuffer[10] = par1IconRegister.registerIcon("warpdrive:airBlock10");
	        iconBuffer[11] = par1IconRegister.registerIcon("warpdrive:airBlock11");
	        iconBuffer[12] = par1IconRegister.registerIcon("warpdrive:airBlock12");
	        iconBuffer[13] = par1IconRegister.registerIcon("warpdrive:airBlock13");
	        iconBuffer[14] = par1IconRegister.registerIcon("warpdrive:airBlock14");
	        iconBuffer[15] = par1IconRegister.registerIcon("warpdrive:airBlock15");
    	} else {
    		blockIcon = par1IconRegister.registerIcon("warpdrive:airBlock");
    	}
    }

    @Override
    public Icon getIcon(int side, int metadata) {
    	if (AIR_DEBUG) {
            return iconBuffer[metadata];
    	} else {
    		return blockIcon;
    	}
    }

    @Override
    public int getMobilityFlag() {
        return 1;
    }

    @Override
    public int idDropped(int var1, Random var2, int var3) {
        return -1;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random) {
        return 0;
    }

    /**
     * How many world ticks before ticking
     */
    @Override
    public int tickRate(World par1World) {
        return AIR_BLOCK_TICKS;
    }

    /**
     * Ticks the block if it's been scheduled
     */
    @Override
    public void updateTick(World par1World, int x, int y, int z, Random par5Random) {
        int concentration = par1World.getBlockMetadata(x, y, z);
        boolean isInSpaceWorld = par1World.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID || par1World.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID;

        // Remove air block to vacuum block
        if (concentration <= 0 || !isInSpaceWorld) {
            par1World.setBlock(x, y, z, 0, 0, 3); // replace our air block to vacuum block
        } else {
            // Try to spread the air
            spreadAirBlock(par1World, x, y, z, concentration);
        }

        par1World.scheduleBlockUpdate(x, y, z, this.blockID, AIR_BLOCK_TICKS);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
    	if (AIR_DEBUG) {
    		return side == 0;
    	}
    	
    	int sideBlockID = world.getBlockId(x, y, z);
        if (sideBlockID == this.blockID) {
            return false;
        }
        return world.isAirBlock(x, y, z);
    }

    private void spreadAirBlock(World worldObj, int x, int y, int z, int concentration) {
        int air_count = 1;
        int empty_count = 0;
        int sum_concentration = concentration;

        // Count air in adjacent blocks
        int xp_blockId = worldObj.getBlockId(x + 1, y, z);
        boolean xp_isAir = WarpDriveConfig.isAirBlock(worldObj, xp_blockId, x + 1, y, z);;
        int xp_concentration = (xp_blockId != this.blockID) ? 0 : worldObj.getBlockMetadata(x + 1, y, z);
        if (xp_isAir) {
        	air_count++;
            if (xp_concentration > 0) {
            	sum_concentration += xp_concentration;
            } else {
            	empty_count++;
            }
        }
        int xn_blockId = worldObj.getBlockId(x - 1, y, z);
        boolean xn_isAir = WarpDriveConfig.isAirBlock(worldObj, xn_blockId, x - 1, y, z);
        int xn_concentration = (xn_blockId != this.blockID) ? 0 : worldObj.getBlockMetadata(x - 1, y, z);
        if (xn_isAir) {
        	air_count++;
            if (xn_concentration > 0) {
            	sum_concentration += xn_concentration;
	        } else {
	        	empty_count++;
	        }
        }
        int yp_blockId = worldObj.getBlockId(x, y + 1, z);
        boolean yp_isAir = WarpDriveConfig.isAirBlock(worldObj, yp_blockId, x, y + 1, z);
        int yp_concentration = (yp_blockId != this.blockID) ? 0 : worldObj.getBlockMetadata(x, y + 1, z);
        if (yp_isAir) {
        	air_count++;
            if (yp_concentration > 0) {
            	sum_concentration += yp_concentration;
	        } else {
	        	empty_count++;
	        }
        }
        int yn_blockId = worldObj.getBlockId(x, y - 1, z);
        boolean yn_isAir = WarpDriveConfig.isAirBlock(worldObj, yn_blockId, x, y - 1, z);
        int yn_concentration = (yn_blockId != this.blockID) ? 0 : worldObj.getBlockMetadata(x, y - 1, z);
        if (yn_isAir) {
        	air_count++;
            if (yn_concentration > 0) {
            	sum_concentration += yn_concentration;
	        } else {
	        	empty_count++;
	        }
        }
        int zp_blockId = worldObj.getBlockId(x, y, z + 1);
        boolean zp_isAir = WarpDriveConfig.isAirBlock(worldObj, zp_blockId, x, y, z + 1);
        int zp_concentration = (zp_blockId != this.blockID) ? 0 : worldObj.getBlockMetadata(x, y, z + 1);
        if (zp_isAir) {
        	air_count++;
            if (zp_concentration > 0) {
            	sum_concentration += zp_concentration;
	        } else {
	        	empty_count++;
	        }
        }
        int zn_blockId = worldObj.getBlockId(x, y, z - 1);
        boolean zn_isAir = WarpDriveConfig.isAirBlock(worldObj, zn_blockId, x, y, z - 1);
        int zn_concentration = (zn_blockId != this.blockID) ? 0 : worldObj.getBlockMetadata(x, y, z - 1);
        if (zn_isAir) {
        	air_count++;
            if (zn_concentration > 0) {
            	sum_concentration += zn_concentration;
	        } else {
	        	empty_count++;
	        }
        }

    	if (empty_count > 0) {
	        if (concentration < 8) {
	        	sum_concentration -= empty_count;
	        } else if (concentration < 4) {
	        	sum_concentration -= empty_count + (worldObj.rand.nextBoolean() ? 0 : empty_count);
	        } else {
        		sum_concentration -= 1;
        	}
        }
        if (sum_concentration < 0) sum_concentration = 0;
        int mid_concentration = (int) Math.floor(sum_concentration * 1.0F / air_count);
        int new_concentration = sum_concentration - mid_concentration * (air_count - 1);
        if (new_concentration > 14) {
        	new_concentration = 14;
        }
        if (concentration != new_concentration)
        {
        	if (concentration == 15) {
        		if ( (xp_blockId != WarpDriveConfig.airgenID) && (xn_blockId != WarpDriveConfig.airgenID)
        		  && (yp_blockId != WarpDriveConfig.airgenID) && (yn_blockId != WarpDriveConfig.airgenID)
        		  && (zp_blockId != WarpDriveConfig.airgenID) && (zn_blockId != WarpDriveConfig.airgenID) ) {
//        			WarpDrive.debugPrint("AirGenerator not found, removing air block at " + x + ", " + y + ", " + z);
            		worldObj.setBlockMetadataWithNotify(x, y, z, 1, 2);
        		} else {
        		// keep the block as a source
/*	        		WarpDrive.debugPrint("15 + "
        			  + xp_concentration + " " + xn_concentration + " "
        			  + yp_concentration + " " + yn_concentration + " "
        			  + zp_concentration + " " + zn_concentration + " = " + sum_concentration + " total, " + empty_count + " empty / " + air_count + " -> " + new_concentration);/**/
        		}
        	} else {
        		worldObj.setBlockMetadataWithNotify(x, y, z, new_concentration, 2);
        	}
        }
        
        // Check and setup air to adjacent blocks
        if (xp_isAir) {
        	if (xp_blockId == this.blockID) {
        		if (xp_concentration != 15) {
        			worldObj.setBlockMetadataWithNotify(x + 1, y, z, mid_concentration, 2);
        		}
        	} else {
        		worldObj.setBlock(x + 1, y, z, this.blockID, mid_concentration, 2);
        	}
        }

        if (xn_isAir) {
        	if (xn_blockId == this.blockID) {
        		if (xn_concentration != 15) {
        			worldObj.setBlockMetadataWithNotify(x - 1, y, z, mid_concentration, 2);
        		}
        	} else {
        		worldObj.setBlock(x - 1, y, z, this.blockID, mid_concentration, 2);
        	}
        }

        if (yp_isAir) {
        	if (yp_blockId == this.blockID) {
        		if (yp_concentration != 15) {
        			worldObj.setBlockMetadataWithNotify(x, y + 1, z, mid_concentration, 2);
        		}
        	} else {
        		worldObj.setBlock(x, y + 1, z, this.blockID, mid_concentration, 2);
        	}
        }

        if (yn_isAir) {
        	if (yn_blockId == this.blockID) {
        		if (yn_concentration != 15) {
        			worldObj.setBlockMetadataWithNotify(x, y - 1, z, mid_concentration, 2);
        		}
        	} else {
        		worldObj.setBlock(x, y - 1, z, this.blockID, mid_concentration, 2);
        	}
        }

        if (zp_isAir) {
        	if (zp_blockId == this.blockID) {
        		if (zp_concentration != 15) {
        			worldObj.setBlockMetadataWithNotify(x, y, z + 1, mid_concentration, 2);
        		}
        	} else {
        		worldObj.setBlock(x, y, z + 1, this.blockID, mid_concentration, 2);
        	}
        }

        if (zn_isAir) {
        	if (zn_blockId == this.blockID) {
        		if (zn_concentration != 15) {
        			worldObj.setBlockMetadataWithNotify(x, y, z - 1, mid_concentration, 2);
        		}
        	} else {
        		worldObj.setBlock(x, y, z - 1, this.blockID, mid_concentration, 2);
        	}
        }
    }

    // Used to prevent updates on chunk generation
    @Override
    public boolean func_82506_l() {
        return false;
    }

    /**
     * Returns if this block is collidable. Args: x, y, z
     */
    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
        if (par1World.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID || par1World.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID) {
            par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate(par1World));
        } else {
            par1World.setBlockToAir(par2, par3, par4);
        }
    }
}