package cr0s.WarpDrive.machines;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import cofh.api.energy.IEnergyHandler;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityPowerReactor extends WarpTE implements IPeripheral
{
	private int containedEnergy = 0;
	private final int maxEnergy;
	private final int tickTime;
	private final int maxLasers;
	private final int minGen;
	
	private Random randomGen = new Random();
	
	int tickCount = 0;
	
	private double[] instabilityValues = new double[4]; //no instability = 0, explosion = 100
	private int lasersReceived = 0;
	private int lastRate = 0;
	private int released = 0;
	
	private boolean active = false;
	private int releaseMode = 0;//0 = don't release, 1=manual release, 2=release all above amount, 3=release at rate
	private int releaseRate = 0;
	private int releaseAbove = 0;
	
	IEnergyHandler aboveConnection;
	IEnergyHandler belowConnection;
	
	private String[] methodArray = {
			"getActive",
			"setActive", //bool
			"energy", // returns energy,maxenergy
			"instability", // returns ins0,1,2,3
			"release",
			"releaseRate",
			"releaseAbove",
			"debugLaser"
	};
	private HashMap<Integer,IComputerAccess> connectedComputers = new HashMap<Integer,IComputerAccess>();
	
	{
		for(int i =0;i<4;i++)
			instabilityValues[i] = 0;
		
		maxEnergy = WarpDriveConfig.PR_MAX_ENERGY;
		tickTime  = WarpDriveConfig.PR_TICK_TIME;
		maxLasers = WarpDriveConfig.PR_MAX_LASERS;
		minGen = 20;
	}
	
	private void increaseInstability(ForgeDirection from)
	{
		if(canInterface(from))
			return;
		
		int side = from.ordinal() - 2;
		double amountToIncrease = Math.pow(randomGen.nextDouble() *(containedEnergy/4), 0.1) * 0.1;
		//WarpDrive.debugPrint("InsInc" + amountToIncrease);
		instabilityValues[side] += amountToIncrease * tickTime;
	}
	
	private void increaseInstability()
	{
		increaseInstability(ForgeDirection.NORTH);
		increaseInstability(ForgeDirection.SOUTH);
		increaseInstability(ForgeDirection.EAST);
		increaseInstability(ForgeDirection.WEST);
	}
	
	public void decreaseInstability(ForgeDirection from, int amount)
	{
		if(canInterface(from))
			return;
		
		if(amount <= 1)
			return;
		
		WarpDrive.debugPrint("validLaser" + amount);
		if(lasersReceived++ <= maxLasers)
		{
			double consumeRateIncrease = 1 + Math.pow(Math.E, lastRate/30000);
			double randomVariation = 0.4 + randomGen.nextDouble(); //Random num between 0.75 and 1.25
			double amountToRemove = Math.min(Math.pow(amount * randomVariation, (1.0/3)) * consumeRateIncrease,75);
			int side = from.ordinal() - 2;
			instabilityValues[side] = Math.max(0,instabilityValues[side]-amountToRemove);
		}
		else
		{
			increaseInstability(from);
			increaseInstability();
		}
	}
	
	public int generateEnergy()
	{
		int amountToGenerate = minGen + (int)Math.ceil(Math.pow(containedEnergy,0.6));
		amountToGenerate *= tickTime;
		
		double stabilityOffset = 0.5;
		for(int i=0;i<4;i++)
			stabilityOffset *= Math.max(0.01,instabilityValues[i]/100);
		
		//WarpDrive.debugPrint("INSOFF" + stabilityOffset);
		
		amountToGenerate *= (1-stabilityOffset);
		containedEnergy = Math.min(containedEnergy+amountToGenerate,maxEnergy);
		return (amountToGenerate / tickTime);
	}
	
	@Override
	public void updateEntity()
	{
		outputPower();
		released = 0;
		if(!active || ++tickCount % tickTime != 0)
			return;
		
		tickCount = 0;
		
		lasersReceived = 0;
		if(explode())
			return;
		increaseInstability();
		
		lastRate = generateEnergy();
		sendEvent("reactorPulse",new Object[] { lastRate });
	}
	
	public boolean explode()
	{
		boolean exp = false;
		for(int i=0;i<4;i++)
			exp = exp || (instabilityValues[i] >= 100);
		
		if(exp)
		{
			active = false;
			WarpDrive.debugPrint("EXPLODE!");
		}
		return exp;
	}
	
	public void updateNeighbours()
	{
		WarpDrive.debugPrint("CHANGES!");
		TileEntity te;
		IEnergyHandler ieh;
		te = worldObj.getBlockTileEntity(xCoord, yCoord+1, zCoord);
		
		boolean valid = false;
		if(te != null)
		{
			if(te instanceof IEnergyHandler)
			{
				ieh = (IEnergyHandler)te;
				if(ieh.canInterface(ForgeDirection.DOWN))
				{
					WarpDrive.debugPrint("a network");
					aboveConnection = ieh;
					valid = true;
				}
			}
		}
		if(!valid)
			aboveConnection = null;
		
		valid = false;
		te = worldObj.getBlockTileEntity(xCoord, yCoord-1, zCoord);
		if(te != null)
		{
			if(te instanceof IEnergyHandler)
			{
				ieh = (IEnergyHandler)te;
				if(ieh.canInterface(ForgeDirection.UP))
				{
					WarpDrive.debugPrint("a network");
					belowConnection = ieh;
					valid = true;
				}
			}
		}
		if(!valid)
			belowConnection = null;
		
		int[] xo = {0,0,-2,2};
		int[] zo = {2,-2,0,0};
		
		for(int i =0;i<4;i++)
		{
			te = worldObj.getBlockTileEntity(xCoord+xo[i], yCoord, zCoord+zo[i]);
			if(te instanceof TileEntityPowerLaser)
				((TileEntityPowerLaser)te).scanForReactor();
		}
	}
	
	private void outputPower(IEnergyHandler ieh,ForgeDirection dir)
	{
		//WarpDrive.debugPrint("outputting");
		int amountToDump = ieh.receiveEnergy(dir,getPotentialReleaseAmount(),true);
		int dumped = ieh.receiveEnergy(dir, amountToDump, false);
		released += dumped;
		containedEnergy -= dumped;
	}
	
	private void outputPower()
	{
		if(aboveConnection != null)
			outputPower(aboveConnection,ForgeDirection.DOWN);
		
		if(belowConnection != null)
			outputPower(belowConnection,ForgeDirection.UP);
	}
	
	//COMPUTER INTERFACES
	@Override
	public String getType()
	{
		return "warpdriveReactor";
	}
	
	@Override
	public String[] getMethodNames()
	{
		return methodArray;
	}
	
	@Override
	public void attach(IComputerAccess computer)
	{
		int id = computer.getID();
		connectedComputers.put(id, computer);
	}
	
	@Override
	public void detach(IComputerAccess computer)
	{
		int id = computer.getID();
		if(connectedComputers.containsKey(id))
			connectedComputers.remove(id);
	}
	
	@Override
	public boolean equals(IPeripheral other)
	{
		if(other == this)
			return true;
		return false;
	}
	
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,int methodID,Object[] arguments) throws Exception
	{
		if(methodID <0 || methodID >= methodArray.length)
			return null;
		
		String methodName = methodArray[methodID];
		
		if(methodName.equals("getActive"))
		{
			return new Object[] { active };
		}
		else if(methodName.equals("setActive"))
		{
			boolean activate = toBool(arguments[0]);
			if(active && !activate)
				sendEvent("reactorDeactivation",null);
			else if(!active && activate)
				sendEvent("reactorActivation",null);
			active = activate;
		}
		else if(methodName.equals("energy"))
		{
			return new Object[] { containedEnergy, maxEnergy };
		}
		else if(methodName.equals("instability"))
		{
			Object[] retVal = new Object[4];
			for(int i=0;i<4;i++)
				retVal[i] = instabilityValues[i];
			return retVal;
		}
		else if(methodName.equals("release"))
		{
			boolean doRelease = toBool(arguments[0]);
			releaseMode = doRelease ? 1 : 0;
			releaseAbove = 0;
			releaseRate  = 0;
			return new Object[] { doRelease };
		}
		else if(methodName.equals("releaseRate"))
		{
			int rate = -1;
			try
			{
				rate = toInt(arguments[0]);
			}
			catch(Exception e)
			{
				throw new Exception("Function expects an integer value");
			}
			
			if(rate <= 0)
				releaseMode = 0;
			else
				releaseMode = 3;
			
			releaseRate = (rate > 0) ? rate : 0;
			return new Object[] { (rate > 0) , rate };
		}
		else if(methodName.equals("releaseAbove"))
		{
			int above = -1;
			try
			{
				above = toInt(arguments[0]);
			}
			catch(Exception e)
			{
				throw new Exception("Function expects an integer value");
			}
			
			if(above <= 0)
				releaseMode = 0;
			else
				releaseMode = 2;
			
			releaseAbove = (above > 0) ? above : 0;
			return new Object[] { (above > 0) , above };
		}
		else if(methodName.equals("debugLaser"))
		{
			//WarpDrive.debugPrint("debugMethod");
			int side = toInt(arguments[0]);
			int amount = toInt(arguments[1]);
			
			ForgeDirection d;
			if(side == 0)
				d = ForgeDirection.NORTH;
			else if(side == 1)
				d = ForgeDirection.SOUTH;
			else if(side == 2)
				d = ForgeDirection.WEST;
			else if(side == 3)
				d = ForgeDirection.EAST;
			else
				d = ForgeDirection.UP;
			
			if(amount < containedEnergy)
			{
				containedEnergy -= amount;
				decreaseInstability(d,amount);
			}
		}
		
		return null;
	}
	
	private void sendEvent(String eventName,Object[] arguments)
	{
		Set<Integer> keys = connectedComputers.keySet();
		for(Integer key:keys)
		{
			IComputerAccess comp = connectedComputers.get(key);
			comp.queueEvent(eventName, arguments);
		}
	}
	
	//POWER INTERFACES
	@Override
	public boolean canInterface(ForgeDirection from)
	{
		if(from.equals(ForgeDirection.UP) || from.equals(ForgeDirection.DOWN))
			return true;
		return false;
	}
	
	private int getPotentialReleaseAmount()
	{
		if(releaseMode == 1)
			return containedEnergy;
		else if(releaseMode == 2)
			return Math.max(0, containedEnergy - releaseAbove);
		else if(releaseMode == 3)
		{
			int maxRelease = containedEnergy;
			if(lastRate > releaseRate && released < maxRelease)
				return Math.min(containedEnergy,(maxRelease-released));
			//return Math.min(releaseRate, containedEnergy);
		}
		return 0;
	}
	
	@Override
	public int receiveEnergy(ForgeDirection from, int amount, boolean sim)
	{
		return 0;
	}
	
	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		int toExtract;
		if(!canInterface(from))
			return 0;
		
		toExtract = Math.min(maxExtract,getPotentialReleaseAmount());
		if(!simulate)
			containedEnergy -= toExtract;
		
		return toExtract;
	}
	
	public int getEnergyStored()
	{
		return containedEnergy;
	}
	
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(canInterface(from))
			return getEnergyStored();
		return 0;
	}
	
	public int getMaxEnergyStored()
	{
		return maxEnergy;
	}
	
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(canInterface(from))
			return getMaxEnergyStored();
		return 0;
	}
	
	//NBT INTERFACES
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("energy", containedEnergy);
		nbt.setInteger("releaseMode",releaseMode);
		nbt.setInteger("releaseRate", releaseRate);
		nbt.setInteger("releaseAbove", releaseAbove);
		nbt.setDouble("i0", instabilityValues[0]);
		nbt.setDouble("i1", instabilityValues[1]);
		nbt.setDouble("i2", instabilityValues[2]);
		nbt.setDouble("i3", instabilityValues[3]);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		containedEnergy = nbt.getInteger("energy");
		releaseMode = nbt.getInteger("releaseMode");
		releaseRate = nbt.getInteger("releaseRate");
		releaseAbove = nbt.getInteger("releaseAbove");
		instabilityValues[0] = nbt.getDouble("i0");
		instabilityValues[1] = nbt.getDouble("i1");
		instabilityValues[2] = nbt.getDouble("i2");
		instabilityValues[3] = nbt.getDouble("i3");
	}
}
