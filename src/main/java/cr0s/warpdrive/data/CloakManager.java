package cr0s.warpdrive.data;

import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import cr0s.warpdrive.network.PacketHandler;

/**
 * Cloak manager stores cloaking devices covered areas
 *
 * @author Cr0s
 *
 */

public class CloakManager {

	private LinkedList<CloakedArea> cloaks;

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
			if (area.dimensionId != player.worldObj.provider.dimensionId) {
				continue;
			}

			if (area.aabb.minX <= (chunkPosX << 4 + 15) && area.aabb.maxX >= (chunkPosX << 4) && area.aabb.minZ <= (chunkPosZ << 4 + 15)
					&& area.aabb.maxZ >= (chunkPosZ << 4)) {
				PacketHandler.sendCloakPacket(player, area.aabb, area.tier, false);
			}
		}

		return false;
	}

	public boolean isAreaExists(World worldObj, int x, int y, int z) {
		return (getCloakedArea(worldObj, x, y, z) != null);
	}

	public void addCloakedAreaWorld(World worldObj, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int x, int y, int z, byte tier) {
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
		for (CloakedArea area : this.cloaks) {
			if (area.coreX == x && area.coreY == y && area.coreZ == z && area.dimensionId == worldObj.provider.dimensionId)
				return area;
		}

		return null;
	}

	public void updatePlayer(EntityPlayer player) {
		for (CloakedArea area : this.cloaks) {
			area.updatePlayer(player);
		}
	}
}
