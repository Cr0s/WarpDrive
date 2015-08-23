package cr0s.warpdrive.data;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import cr0s.warpdrive.LocalProfiler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import cr0s.warpdrive.block.movement.TileEntityShipCore.ShipCoreMode;
import cr0s.warpdrive.conf.WarpDriveConfig;
import cr0s.warpdrive.data.StarMapEntry.StarMapEntryType;

/**
 * Registry of all known ships, jumpgates, etc. in the world
 * 
 * @author LemADEC
 */
public class StarMapRegistry {
	private LinkedList<StarMapEntry> registry;
	
	public StarMapRegistry() {
		registry = new LinkedList<StarMapEntry>();
	}
	
	public int searchInRegistry(StarMapEntry entryKey) {
		int res = -1;
		
		for (int i = 0; i < registry.size(); i++) {
			StarMapEntry entry = registry.get(i);
			
			if (entry.dimensionId == entryKey.dimensionId && entry.x == entryKey.x && entry.y == entryKey.y && entry.z == entryKey.z) {
				return i;
			}
		}
		
		return res;
	}
	
	public boolean isInRegistry(StarMapEntry entryKey) {
		return (searchInRegistry(entryKey) != -1);
	}
	
	public void updateInRegistry(StarMapEntry entryKey) {
		int idx = searchInRegistry(entryKey);
		
		// update
		if (idx != -1) {
			registry.set(idx, entryKey);
		} else {
			registry.add(entryKey);
			printRegistry();
		}
	}
	
	public void removeFromRegistry(StarMapEntry entryKey) {
		int idx = searchInRegistry(entryKey);
		
		if (idx != -1) {
			registry.remove(idx);
			printRegistry();
		}
	}
	
	public ArrayList<StarMapEntry> radarScan(TileEntity tileEntity, final int radius) {
		ArrayList<StarMapEntry> res = new ArrayList<StarMapEntry>(registry.size());
		cleanup();
		
		// printRegistry();
		int radius2 = radius * radius;
		for (StarMapEntry entry : registry) {
			double dX = entry.x - tileEntity.xCoord;
			double dY = entry.y - tileEntity.yCoord;
			double dZ = entry.z - tileEntity.zCoord;
			double distance2 = dX * dX + dY * dY + dZ * dZ;
			
			if ( distance2 <= radius2
			  && (entry.isolationRate == 0.0D || tileEntity.getWorldObj().rand.nextDouble() >= entry.isolationRate)
			  && (entry.getSpaceCoordinates() != null)) {
				res.add(entry);
			}
		}
		
		return res;
	}
	
	public void printRegistry() {
		WarpDrive.logger.info("Starmap registry (" + registry.size() + " entries):");
		
		for (StarMapEntry entry : registry) {
			WarpDrive.logger.info("- " + entry.type.toString() + " '" + entry.name + "' @ "
					+ entry.dimensionId + ": " + entry.x + ", " + entry.y + ", " + entry.z
					+ " with " + entry.isolationRate + " isolation rate");
		}
	}
	
	public boolean isWarpCoreIntersectsWithOthers(TileEntityShipCore core) {
		StringBuilder reason = new StringBuilder();
		AxisAlignedBB aabb1, aabb2;
		cleanup();
		
		core.validateShipSpatialParameters(reason);
		aabb1 = AxisAlignedBB.getBoundingBox(core.minX, core.minY, core.minZ, core.maxX, core.maxY, core.maxZ);
		
		for (StarMapEntry entry : registry) {
			// Skip cores in other worlds
			if (entry.dimensionId != core.getWorldObj().provider.dimensionId) {
				continue;
			}
			// only check cores
			if (entry.type != StarMapEntryType.SHIP) {
				continue;
			}
			
			// Skip self
			if (entry.x == core.xCoord && entry.y == core.yCoord && entry.z == core.zCoord) {
				continue;
			}
			
			// Skip offline warp cores
			TileEntityShipCore shipCore = (TileEntityShipCore) core.getWorldObj().getTileEntity(entry.x, entry.y, entry.z);
			if (shipCore.controller == null || shipCore.controller.getMode() == ShipCoreMode.IDLE || !shipCore.validateShipSpatialParameters(reason)) {
				continue;
			}
			
			// Search for nearest warp cores
			double d3 = entry.x - core.xCoord;
			double d4 = entry.y - core.yCoord;
			double d5 = entry.z - core.zCoord;
			double distance2 = d3 * d3 + d4 * d4 + d5 * d5;
			
			if (distance2 <= ((2 * WarpDriveConfig.SHIP_MAX_SIDE_SIZE) - 1) * ((2 * WarpDriveConfig.SHIP_MAX_SIDE_SIZE) - 1)) {
				// Compare warp-fields for intersection
				aabb2 = AxisAlignedBB.getBoundingBox(entry.minX, entry.minY, entry.minZ, entry.maxX, entry.maxY, entry.maxZ);
				if (aabb1.intersectsWith(aabb2)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	// do not call during tileEntity construction (readFromNBT and validate)
	private void cleanup() {
		LocalProfiler.start("StarMapRegistry cleanup");
		
		StarMapEntry entry;
		boolean isValid; 
		for (int i = registry.size() - 1; i >= 0; i--) {
			entry = registry.get(i);
			isValid = false;
			if (entry != null) {
				WorldServer world = DimensionManager.getWorld(entry.dimensionId);
				// skip unloaded worlds
				if (world == null) {
					continue;
				}
				
				boolean isLoaded = false;
				if (world.getChunkProvider() instanceof ChunkProviderServer) {
					ChunkProviderServer chunkProviderServer = (ChunkProviderServer) world.getChunkProvider();
					try {
						isLoaded = chunkProviderServer.loadedChunkHashMap.containsItem(ChunkCoordIntPair.chunkXZ2Int(entry.x >> 4, entry.z >> 4));
					} catch (NoSuchFieldError exception) {
						isLoaded = chunkProviderServer.chunkExists(entry.x >> 4, entry.z >> 4);
					}
				} else {
					isLoaded = world.getChunkProvider().chunkExists(entry.x >> 4, entry.z >> 4);
				}
				// skip unloaded chunks
				if (!isLoaded) {
					continue;
				}
				
				// get block and tile entity
				Block block = world.getBlock(entry.x, entry.y, entry.z);
				
				TileEntity tileEntity = world.getTileEntity(entry.x, entry.y, entry.z);
				isValid = true;
				switch (entry.type) {
				case UNDEFINED: break;
				case SHIP:
					isValid = block == WarpDrive.blockShipCore && tileEntity != null && !tileEntity.isInvalid();
					break;
				case JUMPGATE: break;
				case PLANET: break;
				case STAR: break;
				case STRUCTURE: break;
				case WARPECHO: break;
				}
			}
			
			if (!isValid) {
				if (WarpDriveConfig.LOGGING_JUMP) {
					if (entry == null) {
						WarpDrive.logger.info("Cleaning up starmap object ~null~");
					} else {
						WarpDrive.logger.info("Cleaning up starmap object " + entry.type + " at "
								+ entry.dimensionId + " " + entry.x + " " + entry.y + " " + entry.z);
					}
				}
				registry.remove(i);
			}
		}
		
		LocalProfiler.stop();
	}
}
