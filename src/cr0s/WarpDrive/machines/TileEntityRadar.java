package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.WarpDrive;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.ArrayList;

public class TileEntityRadar extends WarpTE implements IPeripheral
{

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
		return removeEnergy(needEnergy,true);
	}

	@Override
	public void updateEntity()
	{

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
					//WarpDrive.debugPrint("Scanning...");
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
					int radius = toInt(arguments[0]);
					if (radius <= 0 || radius > 10000)
					{
						scanRadius = 0;
						return new Boolean[] { false };
					}
					if (radius != 0 && isEnergyEnoughForScanRadiusW(radius))
					{
						// Consume energy
						removeEnergy(radius * radius,false);
						// Begin searching
						scanRadius = radius;
						cooldownTime = 0;
						worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 1 + 2);
					}
					else
					{
						results = null;
						WarpDrive.debugPrint("Radius: " + radius + " | Enough energy: " + isEnergyEnoughForScanRadiusW(radius));
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
				return new Integer[] { getEnergyStored() };
			case 5: // Pos
				return new Integer[] { xCoord, yCoord, zCoord };
		}

		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void detach(IComputerAccess computer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean equals(IPeripheral other) {
		// TODO Auto-generated method stub
		return false;
	}


}
