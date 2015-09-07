package cr0s.warpdrive.block.passive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;

public class BlockAir extends Block {
	private final boolean TRANSPARENT_AIR = true;
	private final boolean AIR_DEBUG = false;
	private final int AIR_BLOCK_TICKS = 40;
	private IIcon[] iconBuffer;
	
	public BlockAir() {
		super(Material.fire);
		setHardness(0.0F);
		setBlockName("warpdrive.passive.Air");
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean isAir(IBlockAccess var1, int var2, int var3, int var4) {
		return true;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World var1, int var2, int var3, int var4) {
		return null;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess var1, int var2, int var3, int var4) {
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
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		if (AIR_DEBUG) {
			iconBuffer = new IIcon[16];
			iconBuffer[ 0] = par1IconRegister.registerIcon("warpdrive:passive/airBlock0");
			iconBuffer[ 1] = par1IconRegister.registerIcon("warpdrive:passive/airBlock1");
			iconBuffer[ 2] = par1IconRegister.registerIcon("warpdrive:passive/airBlock2");
			iconBuffer[ 3] = par1IconRegister.registerIcon("warpdrive:passive/airBlock3");
			iconBuffer[ 4] = par1IconRegister.registerIcon("warpdrive:passive/airBlock4");
			iconBuffer[ 5] = par1IconRegister.registerIcon("warpdrive:passive/airBlock5");
			iconBuffer[ 6] = par1IconRegister.registerIcon("warpdrive:passive/airBlock6");
			iconBuffer[ 7] = par1IconRegister.registerIcon("warpdrive:passive/airBlock7");
			iconBuffer[ 8] = par1IconRegister.registerIcon("warpdrive:passive/airBlock8");
			iconBuffer[ 9] = par1IconRegister.registerIcon("warpdrive:passive/airBlock9");
			iconBuffer[10] = par1IconRegister.registerIcon("warpdrive:passive/airBlock10");
			iconBuffer[11] = par1IconRegister.registerIcon("warpdrive:passive/airBlock11");
			iconBuffer[12] = par1IconRegister.registerIcon("warpdrive:passive/airBlock12");
			iconBuffer[13] = par1IconRegister.registerIcon("warpdrive:passive/airBlock13");
			iconBuffer[14] = par1IconRegister.registerIcon("warpdrive:passive/airBlock14");
			iconBuffer[15] = par1IconRegister.registerIcon("warpdrive:passive/airBlock15");
		} else {
			blockIcon = par1IconRegister.registerIcon("warpdrive:passive/airBlock");
		}
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
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
	public Item getItemDropped(int var1, Random var2, int var3) {
		return null;
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
			par1World.setBlock(x, y, z, Blocks.air, 0, 3); // replace our air block to vacuum block
		} else {
			// Try to spread the air
			spreadAirBlock(par1World, x, y, z, concentration);
		}
		par1World.scheduleBlockUpdate(x, y, z, this, 30 + 2 * concentration);
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		if (AIR_DEBUG) {
			return side == 0 || side == 1;
		}
		
		Block sideBlock = world.getBlock(x, y, z);
		if (sideBlock == this) {
			return false;
		}
		return world.isAirBlock(x, y, z);
	}
	
	private void spreadAirBlock(World world, int x, int y, int z, int concentration) {
		int air_count = 1;
		int empty_count = 0;
		int sum_concentration = concentration + 1;
		int max_concentration = concentration + 1;
		int min_concentration = concentration + 1;
		
		// Check air in adjacent blocks
		Block xp_block = world.getBlock(x + 1, y, z);
		boolean xp_isAir = world.isAirBlock(x + 1, y, z);
		int xp_concentration = (xp_block != this) ? -1 : world.getBlockMetadata(x + 1, y, z);
		if (xp_isAir) {
			air_count++;
			if (xp_concentration >= 0) {
				sum_concentration += xp_concentration + 1;
				max_concentration = Math.max(max_concentration, xp_concentration + 1);
				min_concentration = Math.min(min_concentration, xp_concentration + 1);
			} else {
				empty_count++;
			}
		}
		Block xn_block = world.getBlock(x - 1, y, z);
		boolean xn_isAir = world.isAirBlock(x - 1, y, z);
		int xn_concentration = (xn_block != this) ? -1 : world.getBlockMetadata(x - 1, y, z);
		if (xn_isAir) {
			air_count++;
			if (xn_concentration >= 0) {
				sum_concentration += xn_concentration + 1;
				max_concentration = Math.max(max_concentration, xn_concentration + 1);
				min_concentration = Math.min(min_concentration, xn_concentration + 1);
			} else {
				empty_count++;
			}
		}
		Block yp_block = world.getBlock(x, y + 1, z);
		boolean yp_isAir = world.isAirBlock(x, y + 1, z);
		int yp_concentration = (yp_block != this) ? -1 : world.getBlockMetadata(x, y + 1, z);
		if (yp_isAir) {
			air_count++;
			if (yp_concentration >= 0) {
				sum_concentration += yp_concentration + 1;
				max_concentration = Math.max(max_concentration, yp_concentration + 1);
				min_concentration = Math.min(min_concentration, yp_concentration + 1);
			} else {
				empty_count++;
			}
		}
		Block yn_block = world.getBlock(x, y - 1, z);
		boolean yn_isAir = world.isAirBlock(x, y - 1, z);
		int yn_concentration = (yn_block != this) ? -1 : world.getBlockMetadata(x, y - 1, z);
		if (yn_isAir) {
			air_count++;
			if (yn_concentration >= 0) {
				sum_concentration += yn_concentration + 1;
				max_concentration = Math.max(max_concentration, yn_concentration + 1);
				min_concentration = Math.min(min_concentration, yn_concentration + 1);
			} else {
				empty_count++;
			}
		}
		Block zp_block = world.getBlock(x, y, z + 1);
		boolean zp_isAir = world.isAirBlock(x, y, z + 1);
		int zp_concentration = (zp_block != this) ? -1 : world.getBlockMetadata(x, y, z + 1);
		if (zp_isAir) {
			air_count++;
			if (zp_concentration >= 0) {
				sum_concentration += zp_concentration + 1;
				max_concentration = Math.max(max_concentration, zp_concentration + 1);
				min_concentration = Math.min(min_concentration, zp_concentration + 1);
			} else {
				empty_count++;
			}
		}
		Block zn_block = world.getBlock(x, y, z - 1);
		boolean zn_isAir = world.isAirBlock(x, y, z - 1);
		int zn_concentration = (zn_block != this) ? -1 : world.getBlockMetadata(x, y, z - 1);
		if (zn_isAir) {
			air_count++;
			if (zn_concentration >= 0) {
				sum_concentration += zn_concentration + 1;
				max_concentration = Math.max(max_concentration, zn_concentration + 1);
				min_concentration = Math.min(min_concentration, zn_concentration + 1);
			} else {
				empty_count++;
			}
		}
		
		// air leaks means penalty plus some randomization for visual effects
		if (empty_count > 0) {
			if (concentration < 8) {
				sum_concentration -= empty_count;
			} else if (concentration < 4) {
				sum_concentration -= empty_count + (world.rand.nextBoolean() ? 0 : empty_count);
			} else {
				sum_concentration -= air_count;
			}
		}
		if (sum_concentration < 0) sum_concentration = 0;
		
		// compute new concentration, buffing closed space
		int mid_concentration;
		int new_concentration;
		boolean isGrowth = false || (max_concentration > 8 && (max_concentration - min_concentration < 9)) || (max_concentration > 5 && (max_concentration - min_concentration < 4)); 
		if (isGrowth) {
			mid_concentration = Math.round(sum_concentration / (float)air_count) - 1;
			new_concentration = sum_concentration - mid_concentration * (air_count - 1);
			new_concentration = Math.max(Math.max(concentration + 1, max_concentration - 1), new_concentration - 20) - 0;
		} else {
			mid_concentration = 0;
			new_concentration = 0;
			mid_concentration = (int) Math.floor(sum_concentration / (float)air_count);
			new_concentration = sum_concentration - mid_concentration * (air_count - 1);
			if (empty_count > 0) {
				new_concentration = Math.max(0, new_concentration - 5);
			}
		}
		
		// apply scale and clamp
		if (mid_concentration < 1) {
			mid_concentration = 0;
		} else if (mid_concentration > 14) {
			mid_concentration = 14;
		} else if (mid_concentration > 0) {
			mid_concentration--;
		}
		if (new_concentration < 1) {
			new_concentration = 0;
		} else if (new_concentration > max_concentration - 2) {
			new_concentration = Math.max(0, max_concentration - 2);
		} else {
			new_concentration--;
		}
		
		if (WarpDriveConfig.LOGGING_BREATHING && (new_concentration < 0 || mid_concentration < 0 || new_concentration > 14 || mid_concentration > 14)) {
			WarpDrive.logger.info("Invalid concentration at step B " + isGrowth + " " + concentration + " + "
					+ xp_concentration + " " + xn_concentration + " "
					+ yp_concentration + " " + yn_concentration + " "
					+ zp_concentration + " " + zn_concentration + " = " + sum_concentration + " total, " + empty_count + " empty / " + air_count
					+ " -> " + new_concentration + " + " + (air_count - 1) + " * " + mid_concentration);
		}
		
		// new_concentration = mid_concentration = 0;
		
		// protect air generator
		if (concentration != new_concentration) {
			if (concentration == 15) {
				if ( xp_block != WarpDrive.blockAirGenerator && xn_block != WarpDrive.blockAirGenerator
				  && yp_block != WarpDrive.blockAirGenerator && yn_block != WarpDrive.blockAirGenerator
				  && zp_block != WarpDrive.blockAirGenerator && zn_block != WarpDrive.blockAirGenerator) {
					if (WarpDriveConfig.LOGGING_BREATHING) {
						WarpDrive.logger.info("AirGenerator not found, removing air block at " + x + ", " + y + ", " + z);
					}
					world.setBlockMetadataWithNotify(x, y, z, 1, 2);
				} else {
					// keep the block as a source
				}
			} else {
				world.setBlockMetadataWithNotify(x, y, z, new_concentration, 2);
			}
		}
		
		// Check and setup air to adjacent blocks
		// (do not overwrite source block, do not decrease neighbors if we're growing)
		if (xp_isAir) {
			if (xp_block == this) {
				if (xp_concentration != mid_concentration && xp_concentration != 15 && (!isGrowth || xp_concentration < mid_concentration)) {
					world.setBlockMetadataWithNotify(x + 1, y, z, mid_concentration, 2);
				}
			} else {
				world.setBlock(x + 1, y, z, this, mid_concentration, 2);
			}
		}
		
		if (xn_isAir) {
			if (xn_block == this) {
				if (xn_concentration != mid_concentration && xn_concentration != 15 && (!isGrowth || xn_concentration < mid_concentration)) {
					world.setBlockMetadataWithNotify(x - 1, y, z, mid_concentration, 2);
				}
			} else {
				world.setBlock(x - 1, y, z, this, mid_concentration, 2);
			}
		}
		
		if (yp_isAir) {
			if (yp_block == this) {
				if (yp_concentration != mid_concentration && yp_concentration != 15 && (!isGrowth || yp_concentration < mid_concentration)) {
					world.setBlockMetadataWithNotify(x, y + 1, z, mid_concentration, 2);
				}
			} else {
				world.setBlock(x, y + 1, z, this, mid_concentration, 2);
			}
		}
		
		if (yn_isAir) {
			if (yn_block == this) {
				if (yn_concentration != mid_concentration && yn_concentration != 15 && (!isGrowth || yn_concentration < mid_concentration)) {
					world.setBlockMetadataWithNotify(x, y - 1, z, mid_concentration, 2);
				}
			} else {
				world.setBlock(x, y - 1, z, this, mid_concentration, 2);
			}
		}
		
		if (zp_isAir) {
			if (zp_block == this) {
				if (zp_concentration != mid_concentration && zp_concentration != 15 && (!isGrowth || zp_concentration < mid_concentration)) {
					world.setBlockMetadataWithNotify(x, y, z + 1, mid_concentration, 2);
				}
			} else {
				world.setBlock(x, y, z + 1, this, mid_concentration, 2);
			}
		}
		
		if (zn_isAir) {
			if (zn_block == this) {
				if (zn_concentration != mid_concentration && zn_concentration != 15 && (!isGrowth || zn_concentration < mid_concentration)) {
					world.setBlockMetadataWithNotify(x, y, z - 1, mid_concentration, 2);
				}
			} else {
				world.setBlock(x, y, z - 1, this, mid_concentration, 2);
			}
		}
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
			par1World.scheduleBlockUpdate(par2, par3, par4, this, this.tickRate(par1World));
		} else {
			par1World.setBlockToAir(par2, par3, par4);
		}
	}
}