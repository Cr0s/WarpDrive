package cr0s.WarpDrive;

import java.util.ArrayList;
import java.util.Iterator;

import cr0s.WarpDrive.machines.TileEntityReactor;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class CamRegistry {
    private ArrayList<CamRegistryItem> registry;

    public CamRegistry() {
        registry = new ArrayList<CamRegistryItem>();
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

    public CamRegistryItem getCamByPosition(World worldObj, ChunkPosition position) {
		CamRegistryItem cam = null;
    	for (Iterator<CamRegistryItem> it = registry.iterator(); it.hasNext(); ) {
    		cam = it.next();
            if (cam.position.x == position.x && cam.position.y == position.y && cam.position.z == position.z && cam.dimensionId == worldObj.provider.dimensionId) {
                return cam;
            }
        }

        return null;
    }

    private boolean isCamAlive(World worldObj, CamRegistryItem cam) {
    	if (worldObj.provider.dimensionId != cam.dimensionId) {
    		WarpDrive.debugPrint("Inconsistent worldObj with camera " + worldObj.provider.dimensionId + " vs " + cam.dimensionId);
    		return false;
    	}
    	
        if ( (worldObj.getBlockId(cam.position.x, cam.position.y, cam.position.z) != WarpDriveConfig.camID)
          && (worldObj.getBlockId(cam.position.x, cam.position.y, cam.position.z) != WarpDriveConfig.laserCamID) ) {
        	WarpDrive.debugPrint("Reporting a 'dead' camera in dimension " + cam.dimensionId + " at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z);
            return false;
        }

        return true;
    }

    public void removeDeadCams(World worldObj) {
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
    	updateInRegistry(worldObj, new CamRegistryItem(worldObj, position, frequency, type));
    }
    	
    private void updateInRegistry(World worldObj, CamRegistryItem cam) {
        removeDeadCams(worldObj);

        if (isCamAlive(worldObj, cam)) {
            CamRegistryItem existingCam = getCamByPosition(worldObj, cam.position);
            if (existingCam == null) {
				WarpDrive.debugPrint("Adding 'live' camera at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z + " with frequency '" + cam.frequency + "'");
                registry.add(cam);
            } else if (existingCam.frequency != cam.frequency) {
				WarpDrive.debugPrint("Updating 'live' camera at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z + " to frequency '" + cam.frequency + "'");
                registry.add(cam);
            }
        } else {
			WarpDrive.debugPrint("Unable to update 'dead' camera at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z);
        }
    }

	public void printRegistry(World worldObj) {
		System.out.println("Cameras registry for dimension " + worldObj.provider.dimensionId + ":");
		removeDeadCams(worldObj);

		for (CamRegistryItem cam : registry) {
			System.out.println("- " + cam.frequency + " (" + cam.position.x + ", " + cam.position.y + ", " + cam.position.z + ")");
		}
	}
}
