package cr0s.warpdrive.data;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.WarpDriveConfig;

public class JumpBlock {
    public int blockID;
    public int blockMeta;
    public TileEntity blockTileEntity;
    public NBTTagCompound blockNBT;
    public int x;
    public int y;
    public int z;
    
    public JumpBlock() {
    }
    
    public JumpBlock(int i, int j, int k, int l, int i1) {
        blockID = i;
        blockMeta = j;
        blockTileEntity = null;
        x = k;
        y = l;
        z = i1;
    }
    
    public JumpBlock(int i, int j, TileEntity tileentity, int k, int l, int i1) {
        blockID = i;
        blockMeta = j;
        blockTileEntity = tileentity;
        x = k;
        y = l;
        z = i1;
    }
    
	public boolean deploy(World targetWorld, int offsetX, int offsetY, int offsetZ) {
		try {
			int newX = x + offsetX;
			int newY = y + offsetY;
			int newZ = z + offsetZ;
			mySetBlock(targetWorld, newX, newY, newZ, blockID, blockMeta, 2);
			
			// Re-schedule air blocks update
			if (blockID == WarpDriveConfig.airID) {
				targetWorld.markBlockForUpdate(newX, newY, newZ);
				targetWorld.scheduleBlockUpdate(newX, newY, newZ, blockID, 40 + targetWorld.rand.nextInt(20));
			}
			
			NBTTagCompound oldnbt = new NBTTagCompound();
			if (blockTileEntity != null) {
				blockTileEntity.writeToNBT(oldnbt);
				oldnbt.setInteger("x", newX);
				oldnbt.setInteger("y", newY);
				oldnbt.setInteger("z", newZ);
				
				if (oldnbt.hasKey("mainX") && oldnbt.hasKey("mainY") && oldnbt.hasKey("mainZ"))	{ // Mekanism 6.0.4.44
					WarpDrive.debugPrint("[JUMP] moveBlockSimple: TileEntity from Mekanism detected");
					oldnbt.setInteger("mainX", oldnbt.getInteger("mainX") + offsetX);
					oldnbt.setInteger("mainY", oldnbt.getInteger("mainY") + offsetY);
					oldnbt.setInteger("mainZ", oldnbt.getInteger("mainZ") + offsetZ);
				}
				
				TileEntity newTileEntity = null;
				boolean	isForgeMultipart = false;
				if (oldnbt.hasKey("id") && oldnbt.getString("id") == "savedMultipart" && WarpDriveConfig.isForgeMultipartLoaded) {
					isForgeMultipart = true;
					newTileEntity = (TileEntity) WarpDriveConfig.forgeMultipart_helper_createTileFromNBT.invoke(null, targetWorld, oldnbt);
					
				} else if (blockID == WarpDriveConfig.CC_Computer || blockID == WarpDriveConfig.CC_peripheral || blockID == WarpDriveConfig.CCT_Turtle || blockID == WarpDriveConfig.CCT_Upgraded || blockID == WarpDriveConfig.CCT_Advanced) {
					newTileEntity = TileEntity.createAndLoadEntity(oldnbt);
					newTileEntity.invalidate();
					
				} else if (blockID == WarpDriveConfig.AS_Turbine) {
					if (oldnbt.hasKey("zhuYao")) {
						NBTTagCompound nbt1 = oldnbt.getCompoundTag("zhuYao");
						nbt1.setDouble("x", newX);
						nbt1.setDouble("y", newY);
						nbt1.setDouble("z", newZ);
						oldnbt.setTag("zhuYao", nbt1);
					}
					newTileEntity = TileEntity.createAndLoadEntity(oldnbt);
				}
				
				if (newTileEntity == null) {
					newTileEntity = TileEntity.createAndLoadEntity(oldnbt);
				}
				
				if (newTileEntity != null) {
					newTileEntity.worldObj = targetWorld;
					newTileEntity.validate();
				
					targetWorld.setBlockTileEntity(newX, newY, newZ, newTileEntity);
					if (isForgeMultipart) {
						WarpDriveConfig.forgeMultipart_tileMultipart_onChunkLoad.invoke(newTileEntity);
						WarpDriveConfig.forgeMultipart_helper_sendDescPacket.invoke(null, targetWorld, newTileEntity);
					}
				} else {
					WarpDrive.print(" moveBlockSimple failed to create new tile entity at " + x + ", " + y + ", " + z + " blockId " + blockID + ":" + blockMeta);
					WarpDrive.print("NBT data was " + ((oldnbt == null) ? "null" : oldnbt.toString()));
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			String coordinates = "";
			try {
				coordinates = " at " + x + ", " + y + ", " + z + " blockId " + blockID + ":" + blockMeta;
			} catch (Exception dropMe) {
				coordinates = " (unknown coordinates)";
			}
			WarpDrive.print("moveBlockSimple exception at " + coordinates);
			return false;
		}
		
		return true;
	}	
	
	// This code is a straight copy from Vanilla to remove lighting computations
	public static boolean mySetBlock(World w, int x, int y, int z, int blockId, int blockMeta, int par6) {
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
			if (y < 0) {
				return false;
			}  else if (y >= 256) {
				return false;
			} else {
				w.markBlockForUpdate(x, y, z);
				Chunk chunk = w.getChunkFromChunkCoords(x >> 4, z >> 4);
				return myChunkSBIDWMT(chunk, x & 15, y, z & 15, blockId, blockMeta);
			}
		} else {
			return false;
		}
	}
	
	// This code is a straight copy from Vanilla to remove lighting computations
	public static boolean myChunkSBIDWMT(Chunk c, int x, int y, int z, int blockId, int blockMeta) {
		int j1 = z << 4 | x;
		
		if (y >= c.precipitationHeightMap[j1] - 1) {
			c.precipitationHeightMap[j1] = -999;
		}
		
		//int k1 = c.heightMap[j1];
		int l1 = c.getBlockID(x, y, z);
		int i2 = c.getBlockMetadata(x, y, z);

		if (l1 == blockId && i2 == blockMeta) {
			return false;
		} else {
			ExtendedBlockStorage[] storageArrays = c.getBlockStorageArray();
			ExtendedBlockStorage extendedblockstorage = storageArrays[y >> 4];
			
			if (extendedblockstorage == null) {
				if (blockId == 0) {
					return false;
				}

				extendedblockstorage = storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !c.worldObj.provider.hasNoSky);
			}
			
			int j2 = c.xPosition * 16 + x;
			int k2 = c.zPosition * 16 + z;
			extendedblockstorage.setExtBlockID(x, y & 15, z, blockId);
			
			if (l1 != 0) {
				if (!c.worldObj.isRemote) {
					Block.blocksList[l1].breakBlock(c.worldObj, j2, y, k2, l1, i2);
				} else if (Block.blocksList[l1] != null && Block.blocksList[l1].hasTileEntity(i2)) {
					TileEntity te = c.worldObj.getBlockTileEntity(j2, y, k2);

					if (te != null && te.shouldRefresh(l1, blockId, i2, blockMeta, c.worldObj, j2, y, k2)) {
						c.worldObj.removeBlockTileEntity(j2, y, k2);
					}
				}
			}
			
			if (extendedblockstorage.getExtBlockID(x, y & 15, z) != blockId) {
				return false;
			} else {
				extendedblockstorage.setExtBlockMetadata(x, y & 15, z, blockMeta);
				// Removed light recalculations
				/*
				if (flag) {
					c.generateSkylightMap();
				} else {
					if (c.getBlockLightOpacity(par1, par2, par3) > 0) {
						if (par2 >= k1) {
							c.relightBlock(par1, par2 + 1, par3);
						}
					} else if (par2 == k1 - 1) {
						c.relightBlock(par1, par2, par3);
					}

					c.propagateSkylightOcclusion(par1, par3);
				}
				/**/
				TileEntity tileentity;
				
				if (blockId != 0) {
					if (Block.blocksList[blockId] != null && Block.blocksList[blockId].hasTileEntity(blockMeta)) {
						tileentity = c.getChunkBlockTileEntity(x, y, z);
						
						if (tileentity == null) {
							tileentity = Block.blocksList[blockId].createTileEntity(c.worldObj, blockMeta);
							c.worldObj.setBlockTileEntity(j2, y, k2, tileentity);
						}
						
						if (tileentity != null) {
							tileentity.updateContainingBlockInfo();
							tileentity.blockMetadata = blockMeta;
						}
					}
				}
				
				c.isModified = true;
				return true;
			}
		}
	}
}
