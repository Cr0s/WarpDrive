package cr0s.warpdrive.data;

import java.util.LinkedList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.network.PacketHandler;

/**
 * Cloak manager stores cloaking devices covered areas
 *
 * @author Cr0s
 *
 */

public class CloakManager {
	
	private static LinkedList<CloakedArea> cloaks;
	
	public CloakManager() {
		this.cloaks = new LinkedList<CloakedArea>();
	}
	
	public boolean isCloaked(int dimensionID, int x, int y, int z) {
		for (CloakedArea area : this.cloaks) {
			if (area.dimensionId != dimensionID) {
				continue;
			}
			
			if (area.aabb.minX <= x && area.aabb.maxX >= x && area.aabb.minY <= y && area.aabb.maxY >= y && area.aabb.minZ <= z && area.aabb.maxZ >= z) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean onChunkLoaded(EntityPlayerMP player, int chunkPosX, int chunkPosZ) {
		for (CloakedArea area : this.cloaks) {
			// skip other dimensions
			if (area.dimensionId != player.worldObj.provider.dimensionId) {
				continue;
			}
			
			// force refresh if the chunk overlap the cloak
			if ( area.aabb.minX <= (chunkPosX << 4 + 15) && area.aabb.maxX >= (chunkPosX << 4)
			  && area.aabb.minZ <= (chunkPosZ << 4 + 15) && area.aabb.maxZ >= (chunkPosZ << 4) ) {
				PacketHandler.sendCloakPacket(player, area.aabb, area.tier, false);
			}
		}
		
		return false;
	}
	
	public boolean onEntityJoinWorld(EntityPlayerMP player) {
		for (CloakedArea area : this.cloaks) {
			// skip other dimensions
			if (area.dimensionId != player.worldObj.provider.dimensionId) {
				continue;
			}
			
			// force refresh if player is outside the cloak
			if ( area.aabb.minX > player.posX || area.aabb.maxX < player.posX
			  || area.aabb.minY > player.posY || area.aabb.maxY < player.posY
			  || area.aabb.minZ > player.posZ || area.aabb.maxZ < player.posZ ) {
				PacketHandler.sendCloakPacket(player, area.aabb, area.tier, false);
			}
		}
		
		return false;
	}
	
	public boolean isAreaExists(World worldObj, int x, int y, int z) {
		return (getCloakedArea(worldObj, x, y, z) != null);
	}
	
	public void addCloakedAreaWorld(World worldObj,
			final int minX, final int minY, final int minZ,
			final int maxX, final int maxY, final int maxZ,
			final int x, final int y, final int z, final byte tier) {
		cloaks.add(new CloakedArea(worldObj, x, y, z, AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ), tier));
	}
	
	public void removeCloakedArea(World worldObj, int x, int y, int z) {
		int index = 0;
		for (int i = 0; i < cloaks.size(); i++) {
			if ( cloaks.get(i).coreX == x
			  && cloaks.get(i).coreY == y
			  && cloaks.get(i).coreZ == z
			  && cloaks.get(i).dimensionId == worldObj.provider.dimensionId) {
				cloaks.get(i).sendCloakPacketToPlayersEx(true); // send info about collapsing cloaking field
				index = i;
				break;
			}
		}
		
		cloaks.remove(index);
	}
	
	public CloakedArea getCloakedArea(World worldObj, int x, int y, int z) {
		for (CloakedArea area : cloaks) {
			if (area.coreX == x && area.coreY == y && area.coreZ == z && area.dimensionId == worldObj.provider.dimensionId)
				return area;
		}
		
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public CloakedArea getCloakedArea(int x, int y, int z) {
		// client only 
		for (CloakedArea area : cloaks) {
			if (area.coreX == x && area.coreY == y && area.coreZ == z)
				return area;
		}
		
		return null;
	}
	
	public void updatePlayer(EntityPlayer player) {
		for (CloakedArea area : this.cloaks) {
			area.updatePlayer(player);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean onBlockChange(int x, int y, int z, Block block, int metadata, int flag) {
		if (block != Blocks.air) {
			for (CloakedArea area : cloaks) {
				if (area.isBlockWithinArea(x, y, z)) {
					// WarpDrive.logger.info("CM block is inside");
					if (!area.isEntityWithinArea(Minecraft.getMinecraft().thePlayer)) {
						// WarpDrive.logger.info("CM player is outside");
						if (area.tier == 1) {
							return Minecraft.getMinecraft().theWorld.setBlock(x, y, z, WarpDrive.blockGas, 5, flag);
						} else {
							return Minecraft.getMinecraft().theWorld.setBlock(x, y, z, Blocks.air, 0, flag);
						}
					}
				}
			}
		}
		return Minecraft.getMinecraft().theWorld.setBlock(x, y, z, block, metadata, flag);
	}
	
	@SideOnly(Side.CLIENT)
	public static void onFillChunk(Chunk chunk) {
		WarpDrive.logger.info("CM onFillChunk " + chunk.xPosition + " " + chunk.zPosition);
		int chunkXmin = chunk.xPosition << 4;
		int chunkXmax = chunk.xPosition << 4 + 15;
		int chunkZmin = chunk.zPosition << 4;
		int chunkZmax = chunk.zPosition << 4 + 15;
		
		for (CloakedArea area : cloaks) {
			if ( area.aabb.minX <= chunkXmax && area.aabb.maxX >= chunkXmin
			  && area.aabb.minZ <= chunkZmax && area.aabb.maxZ >= chunkZmin ) {
				WarpDrive.logger.info("CM chunk is inside");
				if (!area.isEntityWithinArea(Minecraft.getMinecraft().thePlayer)) {
					WarpDrive.logger.info("CM player is outside");
					
					int areaXmin = (int)Math.max(chunkXmin, area.aabb.minX) & 15;
					int areaXmax = (int)Math.min(chunkXmax, area.aabb.maxX) & 15;
					int areaZmin = (int)Math.max(chunkZmin, area.aabb.minZ) & 15;
					int areaZmax = (int)Math.min(chunkZmax, area.aabb.maxZ) & 15;
					
					Block block = Blocks.air;
					int metadata = 0;
					if (area.tier == 1) {
						block = WarpDrive.blockGas;
						metadata = 5;
					}
					
					for (int x = areaXmin; x <= areaXmax; x++) {
						for (int z = areaZmin; z <= areaZmax; z++) {
							for (int y = (int)area.aabb.maxY; y >= (int)area.aabb.minY; y--) {
								if (chunk.getBlock(x, y, z) != Blocks.air) {
									chunk.func_150807_a(x, y, z, block, metadata);
								}
								
							}
						}
					}
				}
			}
		}
	}
}
