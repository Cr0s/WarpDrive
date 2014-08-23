package cr0s.WarpDrive.machines;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import cr0s.WarpDrive.data.EnumUpgradeTypes;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;
import cr0s.WarpDrive.api.IUpgradable;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public class TileEntityChunkLoader extends WarpChunkTE implements IPeripheral, IUpgradable
{
	private boolean canLoad = false;
	private boolean shouldLoad = false;
	
	private boolean inited = false;
	private ChunkCoordIntPair myChunk;
	
	int negDX, posDX, negDZ, posDZ;
	int area = 1;
	
	private String[] methodArray = {
			"energy",
			"radius",
			"bounds",
			"active",
			"upgrades",
			"help"
	};
	
	{
		negDX = 0;
		negDZ = 0;
		posDX = 0;
		posDZ = 0;
	}
	
	@Override
	public int getMaxEnergyStored()
	{
		return WarpDriveConfig.CL_MAX_ENERGY;
	}
	
	@Override
	public boolean shouldChunkLoad()
	{
		return shouldLoad && canLoad;
	}
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		
		if(!inited)
		{
			inited = true;
			myChunk = worldObj.getChunkFromBlockCoords(xCoord, zCoord).getChunkCoordIntPair();
			changedDistance();
		}
		
		if(shouldLoad)
		{
			canLoad = consumeEnergy(area * WarpDriveConfig.CL_RF_PER_CHUNKTICK, false);
		}
		else
		{
			canLoad = consumeEnergy(area * WarpDriveConfig.CL_RF_PER_CHUNKTICK, true);
		}
	}
	
	private int clampDistance(int dis)
	{
		return clamp(dis,0,WarpDriveConfig.CL_MAX_DISTANCE);
	}
	
	private void changedDistance()
	{
		if(worldObj == null) {
			return;
		}
		if (myChunk == null) {
			Chunk aChunk = worldObj.getChunkFromBlockCoords(xCoord, zCoord);
			if (aChunk != null) {
				myChunk = aChunk.getChunkCoordIntPair();
			} else {
				return;
			}
		}
		negDX = -clampDistance(negDX);
		posDX =  clampDistance(posDX);
		negDZ = -clampDistance(negDZ);
		posDZ =  clampDistance(posDZ);
		minChunk = new ChunkCoordIntPair(myChunk.chunkXPos+negDX,myChunk.chunkZPos+negDZ);
		maxChunk = new ChunkCoordIntPair(myChunk.chunkXPos+posDX,myChunk.chunkZPos+posDZ);
		area = (posDX - negDX + 1) * (posDZ - negDZ + 1);
		refreshLoading(true);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		negDX  = nbt.getInteger("negDX");
		negDZ  = nbt.getInteger("negDZ");
		posDX  = nbt.getInteger("posDX");
		posDZ  = nbt.getInteger("posDZ");
		
		changedDistance();
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("negDX", negDX);
		nbt.setInteger("negDZ", negDZ);
		nbt.setInteger("posDX", posDX);
		nbt.setInteger("posDZ", posDZ);
	}

	@Override
	public String getType()
	{
		return "warpdriveChunkloader";
	}

	@Override
	public String[] getMethodNames()
	{
		return methodArray;
	}
	
	private String helpStr(Object[] args)
	{
		if(args.length == 1)
		{
			String m = args[0].toString().toLowerCase();
			if(m.equals("energy"))
				return WarpDrive.defEnergyStr;
			else if(m.equals("radius"))
				return "radius(int): sets the radius in chunks";
			else if(m.equals("bounds"))
				return "bounds(int,int,int,int): sets the bounds of chunks to load\nbounds(): returns the 4 bounds\nFormat is -X, +X, -Z, +Z";
			else if(m.equals("active"))
				return "active(): returns whether active or not\nactive(boolean): sets whether it should be active or not";
			else if(m.equals("upgrades"))
				return WarpDrive.defUpgradeStr;
		}
		return WarpDrive.defHelpStr;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		String meth = methodArray[method];
		
		if(meth.equals("energy"))
			return getEnergyObject();
		else if(meth.equals("radius"))
		{
			if(arguments.length == 1)
			{
				int dist = toInt(arguments[0]);
				negDX = dist;
				negDZ = dist;
				posDX = dist;
				posDZ = dist;
				changedDistance();
				return new Object[] { true };
			}
			return new Object[] { false };
		}
		else if(meth.equals("bounds"))
		{
			if(arguments.length == 4)
			{
				negDX = toInt(arguments[0]);
				posDX = toInt(arguments[1]);
				negDZ = toInt(arguments[2]);
				posDZ = toInt(arguments[3]);
				changedDistance();
			}
			return new Object[] { negDX, posDX, negDZ, posDZ };
		}
		else if(meth.equals("active"))
		{
			if(arguments.length == 1)
				shouldLoad = toBool(arguments[0]);
			return new Object[] { shouldChunkLoad() };
		}
		else if(meth.equals("upgrades"))
		{
			return getUpgrades();
		}
		else if(meth.equals("help"))
		{
			return new Object[] {helpStr(arguments) };
		}
		
		return null;
	}

	@Override
	public void attach(IComputerAccess computer)
	{	
	}

	@Override
	public void detach(IComputerAccess computer)
	{
	}

	@Override
	public boolean equals(IPeripheral other)
	{
		return false;
	}

	@Override
	public boolean takeUpgrade(EnumUpgradeTypes upgradeType, boolean simulate)
	{
		int max = 0;
		if(upgradeType == EnumUpgradeTypes.Energy)
			max = 2;
		else if(upgradeType == EnumUpgradeTypes.Power)
			max = 2;
		
		if(max == 0)
			return false;
		
		if(upgrades.containsKey(upgradeType))
			if(upgrades.get(upgradeType) >= max)
				return false;
		
		if(!simulate)
		{
			int c = 0;
			if(upgrades.containsKey(upgradeType))
				c = upgrades.get(upgradeType);
			upgrades.put(upgradeType, c+1);
		}
		return true;
	}
	
	@Override
	public Map<EnumUpgradeTypes, Integer> getInstalledUpgrades()
	{
		return upgrades;
	}

}
