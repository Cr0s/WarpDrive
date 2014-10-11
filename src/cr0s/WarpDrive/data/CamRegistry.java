package cr0s.WarpDrive.data;

import java.util.Iterator;
import java.util.LinkedList;

import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class CamRegistry {
    private LinkedList<CamRegistryItem> registry;

    public CamRegistry() {
        registry = new LinkedList<CamRegistryItem>();
    }

    public CamRegistryItem getCamByFrequency(World worldObj, int frequency) {
		CamRegistryItem cam = null;
    	for (Iterator<CamRegistryItem> it = registry.iterator(); it.hasNext(); ) {
    		cam = it.next();
            if (cam.frequency == frequency && cam.dimensionId == worldObj.provider.dimensionId) {
            	if (isCamAlive(worldObj, cam)) {
            		return cam;
            	} else {
    				WarpDrive.debugPrint("Removing 'dead' camera at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z + " (while searching)");
            		it.remove();
            	}
            }
        }
    	
    	// not found => dump registry
   		printRegistry(worldObj);
        return null;
    }

    private CamRegistryItem getCamByPosition(World worldObj, ChunkPosition position) {
		CamRegistryItem cam = null;
    	for (Iterator<CamRegistryItem> it = registry.iterator(); it.hasNext(); ) {
    		cam = it.next();
            if (cam.position.x == position.x && cam.position.y == position.y && cam.position.z == position.z && cam.dimensionId == worldObj.provider.dimensionId) {
                return cam;
            }
        }

        return null;
    }

    private static boolean isCamAlive(World worldObj, CamRegistryItem cam) {
    	if (worldObj.provider.dimensionId != cam.dimensionId) {
    		WarpDrive.debugPrint("Inconsistent worldObj with camera " + worldObj.provider.dimensionId + " vs " + cam.dimensionId);
    		return false;
    	}
    	
    	if (!worldObj.getChunkFromBlockCoords(cam.position.x, cam.position.z).isChunkLoaded) {
        	WarpDrive.debugPrint("Reporting an 'unloaded' camera in dimension " + cam.dimensionId + " at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z);
        	return false;
    	}
    	int blockId = worldObj.getBlockId(cam.position.x, cam.position.y, cam.position.z);
        if ( (blockId != WarpDriveConfig.camID)
          && (blockId != WarpDriveConfig.laserCamID) ) {
        	WarpDrive.debugPrint("Reporting a 'dead' camera in dimension " + cam.dimensionId + " at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z);
            return false;
        }

        return true;
    }

    private void removeDeadCams(World worldObj) {
//		LocalProfiler.start("CamRegistry Removing dead cameras");

		CamRegistryItem cam = null;
    	for (Iterator<CamRegistryItem> it = registry.iterator(); it.hasNext(); ) {
    		cam = it.next();
			if (!isCamAlive(worldObj, cam)) {
				WarpDrive.debugPrint("Removing 'dead' camera in dimension " + cam.dimensionId + " at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z);
				it.remove();
			}
		}
		
//		LocalProfiler.stop();
    }

    public void removeFromRegistry(World worldObj, ChunkPosition position) {
    	CamRegistryItem cam = getCamByPosition(worldObj, position);
    	if (cam != null) {
			WarpDrive.debugPrint("Removing camera by request in dimension " + cam.dimensionId + " at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z);
    		registry.remove(cam);
    	}
    }
    
    public void updateInRegistry(World worldObj, ChunkPosition position, int frequency, int type) {
    	CamRegistryItem cam = new CamRegistryItem(worldObj, position, frequency, type);
		// WarpDrive.debugPrint("updateInRegistry " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z);
        removeDeadCams(worldObj);

        if (isCamAlive(worldObj, cam)) {
            CamRegistryItem existingCam = getCamByPosition(worldObj, cam.position);
            if (existingCam == null) {
				WarpDrive.debugPrint("Adding 'live' camera at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z + " with frequency '" + cam.frequency + "'");
                registry.add(cam);
            } else if (existingCam.frequency != cam.frequency) {
				WarpDrive.debugPrint("Updating 'live' camera at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z + " from frequency '" + existingCam.frequency + "' to frequency '" + cam.frequency + "'");
                existingCam.frequency = cam.frequency;
            }
        } else {
			WarpDrive.debugPrint("Unable to update 'dead' camera at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z);
        }
    }

	public void printRegistry(World worldObj) {
		WarpDrive.print("Cameras registry for dimension " + worldObj.provider.dimensionId + ":");

		for (CamRegistryItem cam : registry) {
			WarpDrive.print("- " + cam.frequency + " (" + cam.position.x + ", " + cam.position.y + ", " + cam.position.z + ")");
		}
	}
}
