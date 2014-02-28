package cr0s.WarpDrive.machines;

import java.util.ArrayList;
import java.util.List;

import cr0s.WarpDrive.Vector3;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;

public class TileEntityTransporter extends WarpTE implements IEnergySink, IPeripheral
{
	private final double scanRange=2;
	
	private final int maxEnergy;
	private double energyBuffer=0;
	private boolean addedToEnergyNet = false;
	
	private double baseLockStrength=-1;
	private double lockStrengthMul = 1;
	private boolean isLocked=false;
	
	private Vector3 centreOnMe = new Vector3(0.5,1,0.5);
	private Vector3 source = new Vector3();
	private Vector3 dest = new Vector3();
	
	private String[] methodArray = {
			"source",
			"dest",
			"lock",
			"release",
			"lockStrength",
			"energize",
			"energy",
			"help" };
	
	public TileEntityTransporter()
	{
		super();
		maxEnergy = WarpDriveConfig.TR_MAX_ENERGY;
	}
	
	@Override
	public void updateEntity()
	{
		if(isLocked && lockStrengthMul > 0)
			lockStrengthMul-= 0.01;
	}
	
	@Override
	public String getType()
	{
		return "transporter";
	}

	@Override
	public String[] getMethodNames()
	{
		return methodArray;
	}
	
	private Object[] setVec3(boolean src,Object... arguments)
	{
		Vector3 vec = src ? source : dest;
		
		try
		{
			if(arguments.length >= 3)
			{
				vec.x = toDouble(arguments[0]);
				vec.y = toDouble(arguments[1]);
				vec.z = toDouble(arguments[2]);
			}
			else if(arguments.length == 1)
			{
				vec.x = xCoord + centreOnMe.x;
				vec.y = yCoord + centreOnMe.y;
				vec.z = zCoord + centreOnMe.z;
			}
		}
		catch(NumberFormatException e)
		{
			vec.x = xCoord + centreOnMe.x;
			vec.y = yCoord + centreOnMe.y;
			vec.z = zCoord + centreOnMe.z;
		}
		return new Object[] { vec.x, vec.y, vec.z };
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		String str = methodArray[method];
		if(str == "energy")
			return new Object[] {Math.max(energyBuffer,maxEnergy),maxEnergy};
		
		if(str == "source")
		{
			if(source == null)
				source = new Vector3(this).translate(centreOnMe);
			return setVec3(true,arguments);
		}
		
		if(str == "dest")
		{
			if(dest == null)
				dest = new Vector3(this).translate(centreOnMe);
			return setVec3(false,arguments);
		}
		
		if(str == "lock")
		{
			return new Object[] { lockStrength(source,dest,true) };
		}
		
		if(str == "release")
		{
			if(isLocked)
			{
				isLocked = false;
				return new Object[] { true };
			}
			else
			{
				return new Object[] { false };
			}
		}
		
		if(str == "lockStrength")
		{
			return new Object[] { lockStrength(source,dest,false) };
		}
		
		if(str == "energize")
		{
			return new Object[] { energize () };
		}
		return null;
	}
	
	private boolean energize()
	{
		double ls = lockStrength(source,dest,false);
		ArrayList<EntityLivingBase> entitiesToTransport = findEntities(source,ls);
		double energyReq = WarpDriveConfig.TR_EU_PER_METRE * source.distanceTo(dest);
		for(EntityLivingBase ent : entitiesToTransport)
		{
			if(energyBuffer >= energyReq)
			{
				energyBuffer -= energyReq;
				transportEnt(ent,dest);
			}
			else
				break;
		}
		return false;
	}
	
	private void transportEnt(EntityLivingBase ent, Vector3 dest)
	{
		if(WarpDriveConfig.TR_RELATIVE_COORDS)
			ent.setPositionAndUpdate(xCoord+dest.x, yCoord+dest.y, zCoord+dest.z);
		else
			ent.setPositionAndUpdate(dest.x, dest.y, dest.z);
	}
	
	private double lockStrength(Vector3 source,Vector3 dest,boolean lock)
	{
		if(isLocked)
		{
			return baseLockStrength * lockStrengthMul;
		}
		else if(lock)
		{
			baseLockStrength = 1; //REPLACE THIS!!!
			lockStrengthMul  = 1;
			isLocked = true;
			return baseLockStrength;
		}
		else
			return 0;
	}
	
	private AxisAlignedBB getAABB()
	{
		Vector3 tS = new Vector3(this);
		Vector3 bS = new Vector3(this);
		Vector3 scanPos = new Vector3(scanRange/2,1,scanRange/2);
		Vector3 scanNeg = new Vector3(-scanRange/2,-1,-scanRange/2);
		if(WarpDriveConfig.TR_RELATIVE_COORDS)
		{
			tS.translate(source).translate(scanPos);
			bS.translate(source).translate(scanNeg);
		}
		else
		{
			tS = source.clone().translate(scanPos);
			bS = source.clone().translate(scanNeg);
		}
		return AxisAlignedBB.getBoundingBox(bS.x,bS.y,bS.z,tS.x,tS.y,tS.z);
	}
	
	private ArrayList<EntityLivingBase> findEntities(Vector3 source, double lockStrength)
	{
		AxisAlignedBB bb = getAABB();
		ArrayList<EntityLivingBase> output = new ArrayList<EntityLivingBase>();
		WarpDrive.debugPrint("Transporter:" +bb.toString());
		List data = worldObj.getEntitiesWithinAABBExcludingEntity(null, bb);
		for(Object ent : data)
		{
			WarpDrive.debugPrint("Transporter:"+ent.toString() + " found");
			if(Math.random() < lockStrength) //If weak lock, don't transport
				if(ent instanceof EntityLivingBase)
					output.add((EntityLivingBase) ent);
		}
		return output;
	}

	@Override
	public boolean canAttachToSide(int side) { return true; }

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		if(direction.equals(ForgeDirection.UP))
			return false;
		return true;
	}

	@Override
	public double demandedEnergyUnits()
	{
		if(energyBuffer >= maxEnergy)
			return 0;
		return maxEnergy-energyBuffer;
	}

	@Override
	public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
	{
		if(energyBuffer < maxEnergy)
		{
			energyBuffer += amount;
			return 0;
		}
		return amount;
	}

	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}
	
	@Override
    public void onChunkUnload()
    {
        if (addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
    }

    @Override
    public void validate()
    {
        super.validate();
        if (!addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }
    }

    @Override
    public void invalidate()
    {
        if (addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }

        super.invalidate();
    }
	
}
