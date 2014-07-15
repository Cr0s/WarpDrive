package cr0s.WarpDrive.machines;

import cr0s.WarpDrive.Vector3;
import cr0s.WarpDrive.WarpDrive;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityPowerLaser extends TileEntityAbstractLaser implements IPeripheral
{
	Vector3 myVec;
	Vector3 reactorVec;
	ForgeDirection side = ForgeDirection.UNKNOWN;
	TileEntityParticleBooster booster;
	TileEntityPowerReactor reactor;
	
	boolean useLaser = false;
	boolean doOnce = false;
	
	String[] methodArray = {
			"energy",
			"hasReactor",
			"sendLaser"
	};
	
	@Override
	public boolean shouldChunkLoad()
	{
		return false;
	}
	
	public TileEntityPowerReactor scanForReactor()
	{
		reactor = null;
		TileEntity te;
		//I AM ON THE NORTH SIDE
		side = ForgeDirection.UNKNOWN;
		te = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 2);
		if(te instanceof TileEntityPowerReactor && isAir(worldObj,xCoord,yCoord,zCoord+1))
		{
			side = ForgeDirection.NORTH;
			reactor = (TileEntityPowerReactor) te;
		}
		
		//I AM ON THE SOUTH SIDE
		te = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 2);
		if(te instanceof TileEntityPowerReactor && isAir(worldObj,xCoord,yCoord,zCoord-1))
		{
			side = ForgeDirection.SOUTH;
			reactor = (TileEntityPowerReactor) te;
		}
		
		//I AM ON THE WEST SIDE
		te = worldObj.getBlockTileEntity(xCoord + 2, yCoord, zCoord);
		if(te instanceof TileEntityPowerReactor && isAir(worldObj,xCoord + 1,yCoord,zCoord))
		{
			side = ForgeDirection.WEST;
			reactor = (TileEntityPowerReactor) te;
		}
		
		//I AM ON THE EAST SIDE
		te = worldObj.getBlockTileEntity(xCoord - 2, yCoord, zCoord);
		if(te instanceof TileEntityPowerReactor && isAir(worldObj,xCoord - 1,yCoord,zCoord))
		{
			side = ForgeDirection.EAST;
			reactor = (TileEntityPowerReactor) te;
		}
		
		setMetadata();
		
		if(reactor != null)
			reactorVec = new Vector3(reactor).translate(0.5);
		return reactor;
	}
	
	private void setMetadata()
	{
		int meta = 0;
		if(side != ForgeDirection.UNKNOWN)
			meta = side.ordinal() - 1;
		WarpDrive.debugPrint("META:" + meta);
		if(worldObj.getBlockMetadata(xCoord, yCoord, zCoord)!= meta)
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta, 3);
	}
	
	public TileEntityParticleBooster scanForBooster()
	{
		booster = null;
		TileEntity te;
		te = worldObj.getBlockTileEntity(xCoord, yCoord+1, zCoord);
		if(te != null && te instanceof TileEntityParticleBooster)
			booster = (TileEntityParticleBooster)te;
		
		te = worldObj.getBlockTileEntity(xCoord, yCoord-1, zCoord);
		if(te != null && te instanceof TileEntityParticleBooster)
			booster = (TileEntityParticleBooster)te;
		
		return booster;
	}
	
	@Override
	public void updateEntity()
	{
		if(doOnce == false)
		{
			scanForReactor();
			scanForBooster();
			myVec = new Vector3(this).translate(0.5);
			doOnce = true;
		}
		
		if(useLaser == true)
		{
			WarpDrive.debugPrint("LAS:" + myVec.toString() + "TO" + reactorVec.toString());
			sendLaserPacket(myVec,reactorVec,0.1f,0.2f,1.0f,25,50,100);
			useLaser = false;
		}
	}
	
	public void unlink()
	{
		side = ForgeDirection.UNKNOWN;
		setMetadata();
	}
	
	public void updateNeighbours()
	{
		scanForBooster();
		scanForReactor();
	}
	
	private void laserReactor(int amount)
	{
		scanForBooster();
		scanForReactor();
		if(booster == null)
			return;
		if(reactor == null)
			return;
		WarpDrive.debugPrint("TTS:" + amount);
		if(booster.removeEnergy(amount, false))
		{
			WarpDrive.debugPrint("L:" + side.toString() +":" + amount);
			useLaser = true;
			reactor.decreaseInstability(side, amount);
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
	}

	@Override
	public String getType()
	{
		return "warpdriveReactorLaser";
	}

	@Override
	public String[] getMethodNames() 
	{
		return methodArray;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,int methodID, Object[] arguments) throws Exception
	{
		String method = methodArray[methodID];
		if(method.equals("energy"))
		{
			scanForBooster();
			if(booster == null)
				return new Object[] { 0,0 };
			else
				return new Object[] { booster.getEnergyStored(), booster.getMaxEnergyStored() };
		}
		else if(method.equals("hasReactor"))
		{
			return new Object[] { scanForReactor()==null };
		}
		else if(method.equals("sendLaser"))
		{
			if(arguments.length >= 1)
				laserReactor(toInt(arguments[0]));
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
		return other == this;
	}

}
