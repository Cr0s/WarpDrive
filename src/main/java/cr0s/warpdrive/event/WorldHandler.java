package cr0s.warpdrive.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;

/**
*
* @author LemADEC
*/
public class WorldHandler {
	
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
	
	// Server side
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event){
		if (event.entity instanceof EntityPlayer) {
			// WarpDrive.logger.info("onEntityJoinWorld " + event.entity);
			if (!event.world.isRemote) {
				WarpDrive.cloaks.onPlayerEnteringDimension((EntityPlayer)event.entity);
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
		// WarpDrive.logger.info("onPlayerChangedDimension " + event.player.getCommandSenderName() + " " + event.fromDim + " -> " + event.toDim);
		WarpDrive.cloaks.onPlayerEnteringDimension(event.player);
	}
	
	// Client side
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientConnectedToServer(ClientConnectedToServerEvent event) {
		// WarpDrive.logger.info("onClientConnectedToServer connectionType " + event.connectionType + " isLocal " + event.isLocal);
		WarpDrive.cloaks.onClientChangingDimension();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onWorldUnload(WorldEvent.Unload event) {
		// WarpDrive.logger.info("onWorldUnload world " + event.world);
		WarpDrive.cloaks.onClientChangingDimension();
	}
}
