package cr0s.WarpDrive.machines;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.ImmutableSet;

import cr0s.WarpDrive.WarpDrive;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public abstract class WarpChunkTE extends TileEntity
{
	private ArrayList<Ticket> ticketList = new ArrayList<Ticket>();
	
	public abstract boolean shouldChunkLoad();
	protected ChunkCoordIntPair minChunk = null;
	protected ChunkCoordIntPair maxChunk = null;
	
	boolean areChunksLoaded = false;
	
	public void refreshLoading(boolean force)
	{
		boolean load = shouldChunkLoad();
		if(ticketList.size() != 0)
		{
			if(load && (!areChunksLoaded || force))
			{
				int ticketSize = ticketList.get(0).getMaxChunkListDepth();
				ArrayList<ChunkCoordIntPair> chunkList = getChunksToLoad();
				int numTicketsRequired = (int) Math.ceil((double) chunkList.size() / ticketSize);
				if(ticketList.size() != numTicketsRequired)
				{
					for(int i=ticketList.size();i<numTicketsRequired;i++)
						WarpDrive.instance.getTicket(this);
				}
				
				int tickNum = 0;
				int chunkInTick = 0;
				
				Ticket t = ticketList.get(0);
				for(ChunkCoordIntPair chunk:chunkList)
				{
					if(chunkInTick >= ticketSize)
					{
						chunkInTick = 0;
						tickNum++;
						t = ticketList.get(tickNum);
					}
					
					WarpDrive.debugPrint("Attempting to force chunk" + chunk);
					ForgeChunkManager.forceChunk(t, chunk);
					chunkInTick++;
				}
				areChunksLoaded = true;
			}
			else if(!load)
			{
				for(Ticket ticket:ticketList)
				{
					ImmutableSet<ChunkCoordIntPair> chunks = ticket.getChunkList();
					for(ChunkCoordIntPair chunk:chunks)
						ForgeChunkManager.unforceChunk(ticket, chunk);
					
					ForgeChunkManager.releaseTicket(ticket);
					WarpDrive.instance.removeTicket(ticket);
				}
				ticketList.clear();
				areChunksLoaded = false;
			}
		}
		else if(load)
		{
			WarpDrive.instance.registerChunkLoadTE(this);
		}
	}
	
	public void refreshLoading()
	{
		refreshLoading(false);
	}
	
	public void giveTicket(Ticket t)
	{
		ticketList.add(t);
	}
	
	private int dX(int dir)
	{
		if(dir == 1)
			return 1;
		else if(dir == 3)
			return -1;
		return 0;
	}
	
	private int dZ(int dir)
	{
		if(dir == 0)
			return 1;
		else if(dir == 2)
			return -1;
		return 0;
	}
	
	public ArrayList<ChunkCoordIntPair> getChunksFromCentre(ChunkCoordIntPair chunkA,ChunkCoordIntPair chunkB)
	{
		int minX = Math.min(chunkA.chunkXPos, chunkB.chunkXPos);
		int maxX = Math.max(chunkA.chunkXPos, chunkB.chunkXPos);
		int minZ = Math.min(chunkA.chunkZPos, chunkB.chunkZPos);
		int maxZ = Math.max(chunkA.chunkZPos, chunkB.chunkZPos);
		WarpDrive.debugPrint("From " + minX + "," + minZ + " to " + maxX + "," + maxZ);
		
		int deltaX = 2 * ((maxX - minX + 1) / 2) + 1;
		int deltaZ = 2 * ((maxZ - minZ + 1) / 2) + 1;
		WarpDrive.debugPrint("Allocating Block: " + deltaX + "," + deltaZ);
		
		maxX = minX + deltaX - 1;
		maxZ = minZ + deltaZ - 1;
		WarpDrive.debugPrint("From " + minX + "," + minZ + " to " + maxX + "," + maxZ);
		
		int maxEnts = (deltaX) * (deltaZ);
		ArrayList<ChunkCoordIntPair> chunkList = new ArrayList<ChunkCoordIntPair>(maxEnts);
		
		int dir = 1;
		int x = minX;
		int z = maxZ;
		for(int i=0;i<maxEnts;i++)
		{
			chunkList.add(new ChunkCoordIntPair(x,z));
			int dX = dX(dir);
			int dZ = dZ(dir);
			if(x+dX > maxX || x+dX < minX || z+dZ > maxZ || z+dZ < minZ)
			{
				dir++;
				if(dir >= 4)
					dir = 0;
				dX = dX(dir);
				dZ = dZ(dir);
				
				if(dX == 1)
					minX++;
				if(dX == -1)
					maxX--;
				if(dZ == 1)
					minZ++;
				if(dZ == -1)
					maxZ--;
				
			}
			x += dX;
			z += dZ;
		}
		
		return chunkList;
	}
	
	public ArrayList<ChunkCoordIntPair> getChunksToLoad()
	{
		if(minChunk == null || maxChunk == null)
		{
			ArrayList<ChunkCoordIntPair> chunkList = new ArrayList<ChunkCoordIntPair>(1);
			chunkList.add(this.worldObj.getChunkFromBlockCoords(xCoord, zCoord).getChunkCoordIntPair());
			return chunkList;
		}
		return getChunksFromCentre(minChunk,maxChunk);
	}
}
