package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import net.minecraftforge.common.ForgeDirection;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityRadar extends TileEntity implements IPeripheral, IEnergySink
{
	public boolean addedToEnergyNet = false;

	private final int MAX_ENERGY_VALUE = 100 * (1000 * 1000); // 100 000 000 Eu
	private int currentEnergyValue = 0;

	private String[] methodsArray =
	{
		"scanRay",		// 0
		"scanRadius",		// 1
		"getResultsCount",	// 2
		"getResult",		// 3
		"getEnergyLevel",	// 4
		"pos"			// 5
	};

	private ArrayList<TileEntityReactor> results;

	private int scanRadius = 0;
	private int cooldownTime = 0;

	private boolean isEnergyEnoughForScanRadiusW(int radius)
	{
		int needEnergy = (radius * radius);
		return ((getCurrentEnergyValue() - needEnergy) > 0);
	}

	@Override
	public void updateEntity()
	{
		if (!addedToEnergyNet && !this.tileEntityInvalid)
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			addedToEnergyNet = true;
		}

		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			return;
		}

		try
		{
			if (worldObj.getBlockMetadata(xCoord, yCoord, zCoord) == 2)
			{
				if (cooldownTime++ > (20 * ((scanRadius / 1000) + 1)))
				{
					//System.out.println("Scanning...");
					WarpDrive.instance.registry.removeDeadCores();
					results = WarpDrive.instance.registry.searchWarpCoresInRadius(xCoord, yCoord, zCoord, scanRadius);
					worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);
					cooldownTime = 0;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		this.currentEnergyValue = tag.getInteger("energy");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tag.setInteger("energy", this.getCurrentEnergyValue());
	}

	// IPeripheral methods implementation
	@Override
	public String getType()
	{
		return "radar";
	}

	@Override
	public String[] getMethodNames()
	{
		return methodsArray;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		switch (method)
		{
			case 0: // scanRay (toX, toY, toZ)
				return new Object[] { -1 };
			case 1: // scanRadius (radius)
				if (arguments.length == 1)
				{
					int radius = ((Double)arguments[0]).intValue();
					if (radius <= 0 || radius > 10000)
					{
						scanRadius = 0;
						return new Boolean[] { false };
					}
					if (radius != 0 && isEnergyEnoughForScanRadiusW(radius))
					{
						// Consume energy
						this.currentEnergyValue -= radius * radius;
						// Begin searching
						scanRadius = radius;
						cooldownTime = 0;
						worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 1 + 2);
					}
					else
					{
						results = null;
						System.out.println("Radius: " + radius + " | Enough energy: " + isEnergyEnoughForScanRadiusW(radius));
						return new Boolean[] { false };
					}
				}
				else
					return new Boolean[] { false };
				return new Boolean[] { true };

			case 2: // getResultsCount
				if (results != null)
					return new Integer[] { results.size() };
				return new Integer[] { 0 };
			case 3: // getResult
				if (arguments.length == 1 && (results != null))
				{
					int index = ((Double)arguments[0]).intValue();
					if (index > -1 && index < results.size())
					{
						TileEntityReactor res = results.get(index);
						if (res != null)
						{
							int yAddition = (res.worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID) ? 256 : (res.worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID) ? 512 : 0;
							return new Object[] { (String)res.coreFrequency, (Integer)res.xCoord, (Integer)res.yCoord + yAddition, (Integer)res.zCoord };
						}
					}
				}
				return new Object[] { (String)"FAIL", 0, 0, 0 };
			case 4: // getEnergyLevel
				return new Integer[] { getCurrentEnergyValue() };
			case 5: // Pos
				return new Integer[] { xCoord, yCoord, zCoord };
		}

		return null;
	}

	@Override
	public boolean canAttachToSide(int side)
	{
		return true;
	}

	@Override
	public void attach(IComputerAccess computer)
	{
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);
	}

	// IEnergySink methods implementation
	@Override
	public double demandedEnergyUnits()
	{
		return (MAX_ENERGY_VALUE - currentEnergyValue);
	}

	@Override
	public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
	{
		double leftover = 0;
		currentEnergyValue += Math.round(amount);

		if (getCurrentEnergyValue() > MAX_ENERGY_VALUE)
		{
			leftover = (getCurrentEnergyValue() - MAX_ENERGY_VALUE);
			currentEnergyValue = MAX_ENERGY_VALUE;
		}

		return leftover;
	}

	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return true;
	}

	/**
	 * @return the currentEnergyValue
	 */
	public int getCurrentEnergyValue()
	{
		return currentEnergyValue;
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
