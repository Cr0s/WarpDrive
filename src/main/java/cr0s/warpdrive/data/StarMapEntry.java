package cr0s.warpdrive.data;

import java.util.UUID;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import cr0s.warpdrive.conf.WarpDriveConfig;

public class StarMapEntry {
	public StarMapEntryType type = StarMapEntryType.UNDEFINED;
	public UUID uuid = null;
	public int dimensionId = -666;
	public int x, y, z = 0;
	public int maxX, maxY, maxZ;
	public int minX, minY, minZ;
	public int volume;
	public double isolationRate = 0.0D;
	public String name = "default";
	
	public enum StarMapEntryType {
		UNDEFINED(0),
		SHIP(1),		// a ship core
		JUMPGATE(2),	// a jump gate
		PLANET(3),		// a planet (a transition plane allowing to move to another dimension)
		STAR(4),		// a star
		STRUCTURE(5),	// a structure from WorldGeneration (moon, asteroid field, etc.)
		WARPECHO(6);	// remains of a warp
		
		private final int code;
		
		StarMapEntryType(int code) {
			this.code = code;
		}
		
		public int getType() {
			return code;
		}
	}
	
	public StarMapEntry(
			final int type, final UUID uuid,
			final int dimensionId, final int x, final int y, final int z,
			final int maxX, final int maxY, final int maxZ,
			final int minX, final int minY, final int minZ,
			final int volume, final double isolationRate,
			final String name) {
		this.type = StarMapEntryType.SHIP;
		this.uuid = uuid;
		this.dimensionId = dimensionId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.volume = volume;
		this.isolationRate = isolationRate;
		this.name = name;
	}
	
	public StarMapEntry(TileEntityShipCore core) {
		this(
				0, core.uuid,
				core.getWorldObj().provider.dimensionId, core.xCoord, core.yCoord, core.zCoord,
				core.maxX, core.maxY, core.maxZ,
				core.minX, core.minY, core.minZ,
				core.shipVolume, core.isolationRate,
				core.shipName);
	}
	
	public WorldServer getWorldServerIfLoaded() {
		WorldServer world = DimensionManager.getWorld(dimensionId);
		// skip unloaded worlds
		if (world == null) {
			return null;
		}
		
		boolean isLoaded = false;
		if (world.getChunkProvider() instanceof ChunkProviderServer) {
			ChunkProviderServer chunkProviderServer = (ChunkProviderServer) world.getChunkProvider();
			try {
				isLoaded = chunkProviderServer.loadedChunkHashMap.containsItem(ChunkCoordIntPair.chunkXZ2Int(x >> 4, z >> 4));
			} catch (NoSuchFieldError exception) {
				isLoaded = chunkProviderServer.chunkExists(x >> 4, z >> 4);
			}
		} else {
			isLoaded = world.getChunkProvider().chunkExists(x >> 4, z >> 4);
		}
		// skip unloaded chunks
		if (!isLoaded) {
			return null;
		}
		return world;
	}
	
	public boolean isLoaded() {
		return getWorldServerIfLoaded() != null;
	}
	
	public VectorI getSpaceCoordinates() {
		if (dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID) {
			return new VectorI(x, y + 256, z);
		}
		if (dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID) {
			return new VectorI(x, y + 512, z);
		}
		for (Planet transitionPlane : WarpDriveConfig.PLANETS) {
			if (transitionPlane.dimensionId == dimensionId) {
				if ( (Math.abs(x - transitionPlane.dimensionCenterX) <= transitionPlane.borderSizeX)
				  && (Math.abs(z - transitionPlane.dimensionCenterZ) <= transitionPlane.borderSizeZ)) {
					return new VectorI(
							x - transitionPlane.dimensionCenterX + transitionPlane.spaceCenterX,
							y,
							z - transitionPlane.dimensionCenterZ + transitionPlane.spaceCenterZ);
				}
			}
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return dimensionId << 24 + (x >> 10) << 12 + y << 10 + (z >> 10);
	}
}