package cr0s.warpdrive.data;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.util.AxisAlignedBB;
import cr0s.warpdrive.LocalProfiler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import cr0s.warpdrive.block.movement.TileEntityShipCore.ShipCoreMode;
import cr0s.warpdrive.conf.WarpDriveConfig;

/**
 * Registry of active Warp Cores in world
 * 
 * @author Cr0s
 */
public class ShipCoresRegistry {
	private LinkedList<TileEntityShipCore> registry;

	public ShipCoresRegistry() {
		registry = new LinkedList<TileEntityShipCore>();
	}

	public int searchCoreInRegistry(TileEntityShipCore core) {
		int res = -1;

		for (int i = 0; i < registry.size(); i++) {
			TileEntityShipCore c = registry.get(i);

			if (c.xCoord == core.xCoord && c.yCoord == core.yCoord && c.zCoord == core.zCoord) {
				return i;
			}
		}

		return res;
	}

	public boolean isCoreInRegistry(TileEntityShipCore core) {
		return (searchCoreInRegistry(core) != -1);
	}

	public void updateInRegistry(TileEntityShipCore core) {
		int idx = searchCoreInRegistry(core);

		// update
		if (idx != -1) {
			registry.set(idx, core);
		} else {
			registry.add(core);
		}
	}

	public void removeFromRegistry(TileEntityShipCore core) {
		int idx = searchCoreInRegistry(core);

		if (idx != -1) {
			registry.remove(idx);
		}
	}

	public ArrayList<TileEntityShipCore> searchWarpCoresInRadius(int x, int y, int z, int radius) {
		ArrayList<TileEntityShipCore> res = new ArrayList<TileEntityShipCore>(registry.size());
		removeDeadCores();

		// printRegistry();
		int radius2 = radius * radius;
		for (TileEntityShipCore core : registry) {
			double dX = core.xCoord - x;
			double dY = core.yCoord - y;
			double dZ = core.zCoord - z;
			double distance2 = dX * dX + dY * dY + dZ * dZ;

			if (distance2 <= radius2 && !core.isHidden()) {
				res.add(core);
			}
		}

		return res;
	}

	public void printRegistry() {
		WarpDrive.logger.info("WarpCores registry:");
		removeDeadCores();

		for (TileEntityShipCore core : registry) {
			WarpDrive.logger.info("- Frequency '" + core.coreFrequency + "' @ '" + core.getWorldObj().provider.getDimensionName() + "' " + core.xCoord + ", "
					+ core.yCoord + ", " + core.zCoord + " with " + core.isolationBlocksCount + " isolation blocks");
		}
	}

	public boolean isWarpCoreIntersectsWithOthers(TileEntityShipCore core) {
		StringBuilder reason = new StringBuilder();
		AxisAlignedBB aabb1, aabb2;
		removeDeadCores();

		core.validateShipSpatialParameters(reason);
		aabb1 = AxisAlignedBB.getBoundingBox(core.minX, core.minY, core.minZ, core.maxX, core.maxY, core.maxZ);

		for (TileEntityShipCore c : registry) {
			// Skip cores in other worlds
			if (c.getWorldObj() != core.getWorldObj()) {
				continue;
			}

			// Skip self
			if (c.xCoord == core.xCoord && c.yCoord == core.yCoord && c.zCoord == core.zCoord) {
				continue;
			}

			// Skip offline warp cores
			if (c.controller == null || c.controller.getMode() == ShipCoreMode.IDLE || !c.validateShipSpatialParameters(reason)) {
				continue;
			}

			// Search for nearest warp cores
			double d3 = c.xCoord - core.xCoord;
			double d4 = c.yCoord - core.yCoord;
			double d5 = c.zCoord - core.zCoord;
			double distance2 = d3 * d3 + d4 * d4 + d5 * d5;

			if (distance2 <= ((2 * WarpDriveConfig.WC_MAX_SHIP_SIDE) - 1) * ((2 * WarpDriveConfig.WC_MAX_SHIP_SIDE) - 1)) {
				// Compare warp-fields for intersection
				aabb2 = AxisAlignedBB.getBoundingBox(c.minX, c.minY, c.minZ, c.maxX, c.maxY, c.maxZ);
				if (aabb1.intersectsWith(aabb2)) {
					return true;
				}
			}
		}

		return false;
	}

	private void removeDeadCores() {
		LocalProfiler.start("ShipCoresRegistry Removing dead cores");

		TileEntityShipCore c;
		for (int i = registry.size() - 1; i >= 0; i--) {
			c = registry.get(i);
			if (c == null || c.getWorldObj() == null || c.getWorldObj().getBlock(c.xCoord, c.yCoord, c.zCoord) != WarpDrive.blockShipCore
					|| c.getWorldObj().getTileEntity(c.xCoord, c.yCoord, c.zCoord) != c
					|| c.getWorldObj().getTileEntity(c.xCoord, c.yCoord, c.zCoord).isInvalid()) {
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info("Removing 'dead' core at "
							+ ((c != null) ? c.xCoord : "?") + ", "
							+ ((c != null) ? c.yCoord : "?") + ", "
							+ ((c != null) ? c.zCoord : "?"));
				}
				registry.remove(i);
			}
		}

		LocalProfiler.stop();
	}

	// TODO: fix it to normal work in client
	/*
	 * public boolean isEntityInsideAnyWarpField(Entity e) { AxisAlignedBB
	 * aabb1, aabb2;
	 * 
	 * double x = e.posX; double y = e.posY; double z = e.posZ;
	 * 
	 * for (TileEntityReactor c : registry) { // Skip offline or disassembled
	 * warp cores if (c.controller == null || !c.prepareToJump()) {
	 * System.out.println("Skipping " + c); if (c.controller == null) {
	 * System.out.println("Controller is null!"); continue; }
	 * 
	 * if (c.controller.getMode() == 0) { System.out.println("Mode is zero!");
	 * continue; }
	 * 
	 * if (!c.prepareToJump()) {
	 * System.out.println("prepareToJump() returns false!"); continue; }
	 * continue; }
	 * 
	 * if (c.minX <= x && c.maxX >= x && c.minY <= y && c.maxY >= y && c.minZ <=
	 * z && c.maxZ >= z) { return true; } }
	 * 
	 * return false; }
	 */
}
