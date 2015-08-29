package cr0s.warpdrive.data;

import java.util.LinkedList;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
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
			
			if (area.minX <= x && area.maxX >= x && area.minY <= y && area.maxY >= y && area.minZ <= z && area.maxZ >= z) {
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
			if ( area.minX <= (chunkPosX << 4 + 15) && area.maxX >= (chunkPosX << 4)
			  && area.minZ <= (chunkPosZ << 4 + 15) && area.maxZ >= (chunkPosZ << 4) ) {
				PacketHandler.sendCloakPacket(player, area, false);
			}
		}
		
		return false;
	}
	
	public boolean onPlayerEnteringDimension(EntityPlayer player) {
		if (WarpDriveConfig.LOGGING_CLOAKING) { WarpDrive.logger.info("onEntityJoinWorld " + player); }
		for (CloakedArea area : this.cloaks) {
			// skip other dimensions
			if (area.dimensionId != player.worldObj.provider.dimensionId) {
				continue;
			}
			
			// force refresh if player is outside the cloak
			if ( area.minX > player.posX || area.maxX < player.posX
			  || area.minY > player.posY || area.maxY < player.posY
			  || area.minZ > player.posZ || area.maxZ < player.posZ ) {
				PacketHandler.sendCloakPacket(player, area, false);
			}
		}
		
		return false;
	}
	
	public boolean isAreaExists(World worldObj, int x, int y, int z) {
		return (getCloakedArea(worldObj, x, y, z) != null);
	}
	
	public void updateCloakedArea(
			World worldObj,
			final int dimensionId, final int coreX, final int coreY, final int coreZ, final byte tier,
			final int minX, final int minY, final int minZ,
			final int maxX, final int maxY, final int maxZ) {
		CloakedArea newArea = new CloakedArea(worldObj, dimensionId, coreX, coreY, coreZ, tier, minX, minY, minZ, maxX, maxY, maxZ);
		
		// find existing one
		int index = -1;
		for (int i = 0; i < cloaks.size(); i++) {
			CloakedArea area = cloaks.get(i);
			if ( area.dimensionId == worldObj.provider.dimensionId
			  && area.coreX == coreX
			  && area.coreY == coreY
			  && area.coreZ == coreZ ) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			cloaks.set(index, newArea);
		} else {
			cloaks.add(newArea);
		}
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			newArea.clientCloak();
		}
		if (WarpDriveConfig.LOGGING_CLOAKING) { WarpDrive.logger.info("Cloak count is " + cloaks.size()); }
	}
	
	public void removeCloakedArea(final int dimensionId, final int coreX, final int coreY, final int coreZ) {
		int index = 0;
		for (int i = 0; i < cloaks.size(); i++) {
			CloakedArea area = cloaks.get(i);
			if ( area.dimensionId == dimensionId
			  && area.coreX == coreX
			  && area.coreY == coreY
			  && area.coreZ == coreZ ) {
				if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
					area.clientDecloak();
				} else {
					area.sendCloakPacketToPlayersEx(true); // send info about collapsing cloaking field
				}
				index = i;
				break;
			}
		}
		
		cloaks.remove(index);
	}
	
	public CloakedArea getCloakedArea(World worldObj, int x, int y, int z) {
		for (CloakedArea area : cloaks) {
			if (area.dimensionId == worldObj.provider.dimensionId && area.coreX == x && area.coreY == y && area.coreZ == z)
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
		if (block != Blocks.air && cloaks != null) {
			for (CloakedArea area : cloaks) {
				if (area.isBlockWithinArea(x, y, z)) {
					// WarpDrive.logger.info("CM block is inside");
					if (!area.isEntityWithinArea(Minecraft.getMinecraft().thePlayer)) {
						// WarpDrive.logger.info("CM player is outside");
						return Minecraft.getMinecraft().theWorld.setBlock(x, y, z, area.fogBlock, area.fogMetadata, flag);
					}
				}
			}
		}
		return Minecraft.getMinecraft().theWorld.setBlock(x, y, z, block, metadata, flag);
	}
	
	@SideOnly(Side.CLIENT)
	public static void onFillChunk(Chunk chunk) {
		if (cloaks == null) {
			// WarpDrive.logger.info("CM onFillChunk (" + chunk.xPosition + " " + chunk.zPosition + ") no cloaks");
			return;
		}
		
		int chunkXmin = chunk.xPosition * 16;
		int chunkXmax = chunk.xPosition * 16 + 15;
		int chunkZmin = chunk.zPosition * 16;
		int chunkZmax = chunk.zPosition * 16 + 15;
		// WarpDrive.logger.info("CM onFillChunk (" + chunk.xPosition + " " + chunk.zPosition + ") " + cloaks.size() + " cloak(s) from (" + chunkXmin + " " + chunkZmin + ") to (" + chunkXmax + " " + chunkZmax + ")");
		
		for (CloakedArea area : cloaks) {
			if ( area.minX <= chunkXmax && area.maxX >= chunkXmin
			  && area.minZ <= chunkZmax && area.maxZ >= chunkZmin ) {
				// WarpDrive.logger.info("CM chunk is inside");
				if (!area.isEntityWithinArea(Minecraft.getMinecraft().thePlayer)) {
					// WarpDrive.logger.info("CM player is outside");
					
					int areaXmin = Math.max(chunkXmin, area.minX) & 15;
					int areaXmax = Math.min(chunkXmax, area.maxX) & 15;
					int areaZmin = Math.max(chunkZmin, area.minZ) & 15;
					int areaZmax = Math.min(chunkZmax, area.maxZ) & 15;
					
					for (int x = areaXmin; x <= areaXmax; x++) {
						for (int z = areaZmin; z <= areaZmax; z++) {
							for (int y = area.maxY; y >= area.minY; y--) {
								if (chunk.getBlock(x, y, z) != Blocks.air) {
									chunk.func_150807_a(x, y, z, area.fogBlock, area.fogMetadata);
								}
								
							}
						}
					}
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void onClientChangingDimension() {
		cloaks.clear();
	}
}
