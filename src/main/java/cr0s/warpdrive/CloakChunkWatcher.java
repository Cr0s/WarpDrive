package cr0s.warpdrive;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;

public class CloakChunkWatcher {
	
	//TODO: register as event receiver
	public void onChunkLoaded(ChunkWatchEvent event) {
		ChunkCoordIntPair chunk = event.chunk;
		
		// Check chunk for locating in cloaked areas
		WarpDrive.logger.info("onChunkLoaded " + chunk.chunkXPos + " " + chunk.chunkZPos);
		WarpDrive.cloaks.onChunkLoaded(event.player, chunk.chunkXPos, chunk.chunkZPos);
		
		/*
		List<Chunk> list = new ArrayList<Chunk>();
		list.add(c);
		
		// Send obscured chunk
		System.out.println("[Cloak] Sending to player " + p.username + " obscured chunk at (" + chunk.chunkXPos + "; " + chunk.chunkZPos + ")");
		((EntityPlayerMP)p).playerNetServerHandler.sendPacketToPlayer(new Packet56MapChunks(list));
		*/
	}
	
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event){
		if (!event.world.isRemote) {
			if (event.entity instanceof EntityPlayerMP) {
				WarpDrive.cloaks.onEntityJoinWorld((EntityPlayerMP)event.entity);
			}
		}
	}
}
