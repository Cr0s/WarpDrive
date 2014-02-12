package cr0s.WarpDrive;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet56MapChunks;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkWatchEvent;

public class CloakChunkWatcher
{
	@ForgeSubscribe
	public void chunkLoaded(ChunkWatchEvent event)
	{
		EntityPlayerMP p = event.player;
		ChunkCoordIntPair chunk = event.chunk;
		ArrayList<CloakedArea> cloaks = WarpDrive.instance.cloaks.getCloaksForPoint(p.worldObj.provider.dimensionId, chunk.getCenterXPos(), 0, chunk.getCenterZPosition(), true);
		if (cloaks.size() == 0)
			return;
		for (CloakedArea area : cloaks)
			area.sendCloakPacketToPlayer(p, false);
	}
}
