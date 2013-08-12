package cr0s.WarpDrive;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class CamRegistryItem {
	public int freq;
	public ChunkPosition camPos;
	public World worldObj;
	
	public int type = 0; // 0 - basic cam, 1 - laser cam
	public CamRegistryItem(int freq, ChunkPosition pos, World worldObj) {
		this.freq = freq;
		this.camPos = pos;
		this.worldObj = worldObj;
	}
	
	public CamRegistryItem setType(int type) {
		this.type = type;
		return this;
	}
}