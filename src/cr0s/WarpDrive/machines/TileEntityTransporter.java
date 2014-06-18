package cr0s.WarpDrive.machines;

import java.util.ArrayList;
import java.util.List;

import cr0s.WarpDrive.Vector3;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.DamageSource;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;

public class TileEntityTransporter extends WarpTE implements IPeripheral
{
	private double scanRange=2;
	
	private int scanDist = 4;
	private double powerBoost = 1;
	private double baseLockStrength=-1;
	private double lockStrengthMul = 1;
	private boolean isLocked=false;
	
	private Vector3 centreOnMe = new Vector3(0.5,1,0.5);
	private Vector3 sourceVec = new Vector3();
	private Vector3 destVec = new Vector3();
	
	private TeleporterDamage teleDam = new TeleporterDamage("teleporter");
	
	private String[] methodArray = {
			"source",
			"dest",
			"lock",
			"release",
			"lockStrength",
			"energize",
			"energy",
			"powerBoost",
			"help" };
	
	@Override
	public int getMaxEnergyStored()
	{
		return WarpDriveConfig.TR_MAX_ENERGY;
	}
	
	@Override
	public void updateEntity()
	{
		
		if(isLocked && lockStrengthMul > 0)
			lockStrengthMul*= 0.98;
	}
	
	@Override
	public String getType()
	{
		WarpDrive.debugPrint("GetType");
		return "transporter";
	}

	@Override
	public String[] getMethodNames()
	{
		return methodArray;
	}
	
	private Object[] setVec3(boolean src,Object... arguments)
	{
		Vector3 vec = src ? sourceVec : destVec;
		
		if(vec == null)
		{
			if(src)
				sourceVec = new Vector3(0,0,0);
			else
				destVec = new Vector3(0,0,0);
			vec = src ? sourceVec : destVec;
		}
		
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
				if(WarpDriveConfig.TR_RELATIVE_COORDS)
				{
					vec.x = centreOnMe.x;
					vec.y = centreOnMe.y;
					vec.z = centreOnMe.z;
				}
				else
				{
					vec.x = xCoord + centreOnMe.x;
					vec.y = yCoord + centreOnMe.y;
					vec.z = zCoord + centreOnMe.z;
				}
			}
		}
		catch(NumberFormatException e)
		{
			return setVec3(src,"this");
		}
		return new Object[] { vec.x, vec.y, vec.z };
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		String str = methodArray[method];
		if(str == "energy")
			return new Object[] {getEnergyStored(),getMaxEnergyStored()};
		
		if(str == "source")
		{
			return setVec3(true,arguments);
		}
		
		if(str == "dest")
		{
			return setVec3(false,arguments);
		}
		
		if(str == "lock")
		{
			return new Object[] { lockStrength(sourceVec,destVec,true) };
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
			return new Object[] { lockStrength(sourceVec,destVec,false) };
		}
		
		if(str == "energize")
		{
			return new Object[] { energize () };
		}
		
		if(str == "powerBoost")
		{
			try
			{
				if(arguments.length >= 1)
					powerBoost = clamp(toInt(arguments[0]),1,WarpDriveConfig.TR_MAX_BOOST_MUL);
			}
			catch(NumberFormatException e)
			{
				powerBoost = 1;
			}
			return new Object[] { powerBoost };
		}
		
		return null;
	}
	
	private boolean energize()
	{
		double ls = lockStrength(sourceVec,destVec,false);
		ArrayList<EntityLivingBase> entitiesToTransport = findEntities(sourceVec,ls);
		int energyReq = (int) Math.ceil(WarpDriveConfig.TR_EU_PER_METRE * sourceVec.distanceTo(destVec));
		for(EntityLivingBase ent : entitiesToTransport)
		{
			if(removeEnergy(energyReq,false))
			{
				inflictNegativeEffect(ent,ls);
				transportEnt(ent,destVec);
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
	
	private void inflictNegativeEffect(EntityLivingBase ent,double lockStrength)
	{
		double value = Math.random() + lockStrength;
		
		WarpDrive.debugPrint("TELEPORT INFLICTION: " + value);
		if(value < 0.1)
			ent.attackEntityFrom(teleDam, 1000);
		
		if(value < 0.2)
			ent.attackEntityFrom(teleDam, 10);
		
		if(value < 0.5)
			ent.attackEntityFrom(teleDam, 1);
	}
	
	private double beaconScan(int xV, int yV, int zV)
	{
		double beacon = 0;
		for(int x=xV-scanDist;x<=xV+scanDist;x++)
		{
			for(int y=yV-scanDist;y<=yV+scanDist;y++)
			{
				if(y < 0 || y > 254)
					continue;
				
				for(int z=xV-scanDist;z<=xV+scanDist;z++)
				{
					if(worldObj.getBlockId(x, y, z) != WarpDriveConfig.transportBeaconID)
						continue;
					double dist = Math.abs(x - xV) + Math.abs(y - yV) + Math.abs(z - zV);
					
					if(worldObj.getBlockMetadata(x, y, z) == 0)
						beacon += 1/dist;
					else
						beacon -= 1/dist;
				}
			}
		}
		return beacon;
	}
	
	private double calculatePower(Vector3 d)
	{
		Vector3 myCoords;
		if(WarpDriveConfig.TR_RELATIVE_COORDS)
			myCoords = centreOnMe;
		else
			myCoords = new Vector3(this).translate(centreOnMe);
		return calculatePower(myCoords,d);
	}
	
	private double calculatePower(Vector3 s, Vector3 d)
	{
		double dist = s.distanceTo(d);
		return clamp(Math.pow(Math.E, -dist / 100) * (1/dist),0,1);
	}
	
	private double min(double... ds)
	{
		double curMin = Double.MAX_VALUE;
		for(double d: ds)
			curMin = Math.min(curMin, d);
		return curMin;
	}
	
	private double lockStrength(Vector3 source,Vector3 dest,boolean lock)
	{
		if(isLocked)
		{
			return Math.max(1, baseLockStrength * lockStrengthMul * Math.pow(2, powerBoost-1));
		}
		else if(lock && source != null && dest != null)
		{
			double basePower = min(calculatePower(source),calculatePower(dest),calculatePower(source,dest));
			baseLockStrength = basePower;
			lockStrengthMul  = 1;
			isLocked = true;
			return Math.max(1,baseLockStrength * powerBoost);
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
			tS.translate(sourceVec).translate(scanPos);
			bS.translate(sourceVec).translate(scanNeg);
		}
		else
		{
			tS = sourceVec.clone().translate(scanPos);
			bS = sourceVec.clone().translate(scanNeg);
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
			if(lockStrength >= 1 || Math.random() < lockStrength) //If weak lock, don't transport (lazy java shouldn't do math.random() if strong lock)
				if(ent instanceof EntityLivingBase)
					output.add((EntityLivingBase) ent);
		}
		return output;
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}
    
    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
    	super.writeToNBT(tag);
    	tag.setDouble("powerBoost", powerBoost);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
    	super.readFromNBT(tag);
    	powerBoost   = tag.getDouble("powerBoost");
    }
    
    class TeleporterDamage extends DamageSource
    {

		protected TeleporterDamage(String par1Str)
		{
			super(par1Str);
		}
		
		@Override
		public ChatMessageComponent getDeathMessage(EntityLivingBase e)
		{
			String mess = "";
			if(e instanceof EntityPlayer || e instanceof EntityPlayerMP)
				mess = ((EntityPlayer) e).username + " was killed by a teleporter malfunction";
			else
				mess = e.getEntityName() + " was killed by a teleporter malfunction";
			
			WarpDrive.debugPrint(mess);
			return ChatMessageComponent.createFromText(mess);
		}
    	
    }

	@Override
	public boolean equals(IPeripheral other)
	{
		return false;
	}
}
