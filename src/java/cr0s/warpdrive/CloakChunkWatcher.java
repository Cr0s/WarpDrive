package cr0s.warpdrive;

import java.util.ArrayList;

import cr0s.warpdrive.data.CloakedArea;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkWatchEvent;

public class CloakChunkWatcher {
	@ForgeSubscribe
	public void chunkLoaded(ChunkWatchEvent event) {
		ChunkCoordIntPair chunk = event.chunk;
		
		// Check chunk for locating in cloaked areas
		WarpDrive.cloaks.checkChunkLoaded(event.player, chunk.chunkXPos, chunk.chunkZPos);
		
		/*List<Chunk> list = new ArrayList<Chunk>();
		list.add(c);
		
		// Send obscured chunk
		System.out.println("[Cloak] Sending to player " + p.username + " obscured chunk at (" + chunk.chunkXPos + "; " + chunk.chunkZPos + ")");
		((EntityPlayerMP)p).playerNetServerHandler.sendPacketToPlayer(new Packet56MapChunks(list));*/
	}
}
