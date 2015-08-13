package cr0s.warpdrive.data;

import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;

public class CamerasRegistry {
	private LinkedList<CameraRegistryItem> registry;

	public CamerasRegistry() {
		registry = new LinkedList<CameraRegistryItem>();
	}

	public CameraRegistryItem getCamByFrequency(World worldObj, int frequency) {
		CameraRegistryItem cam = null;
		for (Iterator<CameraRegistryItem> it = registry.iterator(); it.hasNext();) {
			cam = it.next();
			if (cam.frequency == frequency && cam.dimensionId == worldObj.provider.dimensionId) {
				if (isCamAlive(worldObj, cam)) {
					return cam;
				} else {
					WarpDrive.debugPrint("Removing 'dead' camera at " + cam.position.chunkPosX + ", " + cam.position.chunkPosY + ", " + cam.position.chunkPosZ
							+ " (while searching)");
					it.remove();
				}
			}
		}

		// not found => dump registry
		printRegistry(worldObj);
		return null;
	}

	private CameraRegistryItem getCamByPosition(World worldObj, ChunkPosition position) {
		CameraRegistryItem cam = null;
		for (Iterator<CameraRegistryItem> it = registry.iterator(); it.hasNext();) {
			cam = it.next();
			if (cam.position.chunkPosX == position.chunkPosX && cam.position.chunkPosY == position.chunkPosY && cam.position.chunkPosZ == position.chunkPosZ
					&& cam.dimensionId == worldObj.provider.dimensionId) {
				return cam;
			}
		}

		return null;
	}

	private static boolean isCamAlive(World worldObj, CameraRegistryItem cam) {
		if (worldObj.provider.dimensionId != cam.dimensionId) {
			WarpDrive.debugPrint("Inconsistent worldObj with camera " + worldObj.provider.dimensionId + " vs " + cam.dimensionId);
			return false;
		}

		if (!worldObj.getChunkFromBlockCoords(cam.position.chunkPosX, cam.position.chunkPosZ).isChunkLoaded) {
			WarpDrive.debugPrint("Reporting an 'unloaded' camera in dimension " + cam.dimensionId + " at " + cam.position.chunkPosX + ", "
					+ cam.position.chunkPosY + ", " + cam.position.chunkPosZ);
			return false;
		}
		Block block = worldObj.getBlock(cam.position.chunkPosX, cam.position.chunkPosY, cam.position.chunkPosZ);
		if ((block != WarpDrive.blockCamera) && (block != WarpDrive.blockLaserCamera)) {
			WarpDrive.debugPrint("Reporting a 'dead' camera in dimension " + cam.dimensionId + " at " + cam.position.chunkPosX + ", " + cam.position.chunkPosY
					+ ", " + cam.position.chunkPosZ);
			return false;
		}

		return true;
	}

	private void removeDeadCams(World worldObj) {
		// LocalProfiler.start("CamRegistry Removing dead cameras");

		CameraRegistryItem cam = null;
		for (Iterator<CameraRegistryItem> it = registry.iterator(); it.hasNext();) {
			cam = it.next();
			if (!isCamAlive(worldObj, cam)) {
				WarpDrive.debugPrint("Removing 'dead' camera in dimension " + cam.dimensionId + " at " + cam.position.chunkPosX + ", " + cam.position.chunkPosY
						+ ", " + cam.position.chunkPosZ);
				it.remove();
			}
		}

		// LocalProfiler.stop();
	}

	public void removeFromRegistry(World worldObj, ChunkPosition position) {
		CameraRegistryItem cam = getCamByPosition(worldObj, position);
		if (cam != null) {
			WarpDrive.debugPrint("Removing camera by request in dimension " + cam.dimensionId + " at " + cam.position.chunkPosX + ", " + cam.position.chunkPosY
					+ ", " + cam.position.chunkPosZ);
			registry.remove(cam);
		}
	}

	public void updateInRegistry(World worldObj, ChunkPosition position, int frequency, int type) {
		CameraRegistryItem cam = new CameraRegistryItem(worldObj, position, frequency, type);
		// WarpDrive.debugPrint("updateInRegistry " + cam.position.x + ", " +
		// cam.position.y + ", " + cam.position.z);
		removeDeadCams(worldObj);

		if (isCamAlive(worldObj, cam)) {
			CameraRegistryItem existingCam = getCamByPosition(worldObj, cam.position);
			if (existingCam == null) {
				WarpDrive.debugPrint("Adding 'live' camera at " + cam.position.chunkPosX + ", " + cam.position.chunkPosY + ", " + cam.position.chunkPosZ
						+ " with frequency '" + cam.frequency + "'");
				registry.add(cam);
			} else if (existingCam.frequency != cam.frequency) {
				WarpDrive.debugPrint("Updating 'live' camera at " + cam.position.chunkPosX + ", " + cam.position.chunkPosY + ", " + cam.position.chunkPosZ
						+ " from frequency '" + existingCam.frequency + "' to frequency '" + cam.frequency + "'");
				existingCam.frequency = cam.frequency;
			}
		} else {
			WarpDrive.debugPrint("Unable to update 'dead' camera at " + cam.position.chunkPosX + ", " + cam.position.chunkPosY + ", " + cam.position.chunkPosZ);
		}
	}

	public void printRegistry(World worldObj) {
		WarpDrive.logger.info("Cameras registry for dimension " + worldObj.provider.dimensionId + ":");

		for (CameraRegistryItem cam : registry) {
			WarpDrive.logger.info("- " + cam.frequency + " (" + cam.position.chunkPosX + ", " + cam.position.chunkPosY + ", " + cam.position.chunkPosZ + ")");
		}
	}
}
