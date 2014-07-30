package cr0s.WarpDrive.machines;

import cr0s.WarpDrive.Vector3;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityMiningLaser extends TileEntityAbstractMiner implements IPeripheral
{
	protected final int laserBelow = 0;
	
	private int digX,digZ = 8;
	private final int CUBE_SIDE = 8;
	private boolean isMining = false;
	private boolean isQuarry = false;
	
	private double speedMul = 1;
	
	private int miningDelay = 0;
	private int minLayer = 1;
	
	@Override
	protected int calculateLayerCost()
	{
		return isOnEarth() ? WarpDriveConfig.ML_EU_PER_LAYER_EARTH : WarpDriveConfig.ML_EU_PER_LAYER_SPACE;
	}
	
	@Override
	protected int calculateBlockCost()
	{
		int enPerBlock = isOnEarth() ? WarpDriveConfig.ML_EU_PER_BLOCK_EARTH : WarpDriveConfig.ML_EU_PER_BLOCK_SPACE;
		if(silkTouch())
			return (int) Math.round(enPerBlock * WarpDriveConfig.ML_EU_MUL_SILKTOUCH  * speedMul);
		return (int) Math.round(enPerBlock * (Math.pow(WarpDriveConfig.ML_EU_MUL_FORTUNE, fortune()))  * speedMul);
	}
	
	private String[] methodsArray =
	{
		"mine",		//0
		"stop",		//1
		"isMining",	//2
		"quarry",	//3
		"state",	//4
		"offset",	//5
		"silktouch", //6
		"fortune", //7
		"speedMul", //8
		"layer", //9
		"minLayer", //10
		"energy"
	};

	private int delayTicksScan = 0;
	private int delayTicksMine = 0;
	private int currentMode = 0; // 0 - scan next layer, 1 - collect valuables

	private int currentLayer;

	private ArrayList<Vector3> valuablesInLayer = new ArrayList<Vector3>();
	private int valuableIndex = 0;

	private int layerOffset = 1;
	//private long uid = 0;
	//int t = 20;
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		
		if(minLayer > yCoord - 1)
			minLayer = yCoord - 1;
		if(currentLayer > yCoord - 1)
			currentLayer = yCoord - 1;
		if(speedMul == 0)
			speedMul = 1;
		speedMul = clamp(speedMul,WarpDriveConfig.ML_MIN_SPEED,WarpDriveConfig.ML_MAX_SPEED);
		if (isMining)
		{
			if (currentMode == 0)
			{
				if (++delayTicksScan > (WarpDriveConfig.ML_SCAN_DELAY / speedMul))
				{
					delayTicksScan = 0;
					valuablesInLayer.clear();
					valuableIndex = 0;
					if (!collectEnergyPacketFromBooster(calculateLayerCost(), true))
						return;
					while (currentLayer > (minLayer - 1))
					{
						scanLayer();
						if (valuablesInLayer.size() > 0)
						{
							if(collectEnergyPacketFromBooster(calculateLayerCost(),false))
							{
								currentMode = 1;
								return;
							}
						}
						else
							--currentLayer;
					}
					if (currentLayer < minLayer)
					{
						isMining = false;
						refreshLoading();
					}
				}
			}
			else
			{
				if (++delayTicksMine > ((WarpDriveConfig.ML_MINE_DELAY / speedMul) + miningDelay))
				{
					delayTicksMine = 0;
					int energyReq = calculateBlockCost();
					if (collectEnergyPacketFromBooster(energyReq,true) && valuableIndex < valuablesInLayer.size())
					{
						//WarpDrive.debugPrint("[ML] Mining: " + (valuableIndex + 1) + "/" + valuablesInLayer.size());
						Vector3 valuable = valuablesInLayer.get(valuableIndex);
						// Mine valuable ore
						int blockID = worldObj.getBlockId(valuable.intX(), valuable.intY(), valuable.intZ());
						if (!canDig(blockID))
						{
							WarpDrive.debugPrint("Cannot mine: " + blockID);
							valuableIndex++;
							return;
						}
						
						if((WarpDriveConfig.MinerOres.contains(blockID) || isQuarry) && isRoomForHarvest())
						{
							if(collectEnergyPacketFromBooster(energyReq,false))
							{
								harvestBlock(valuable);
								valuableIndex++;
								miningDelay = 0;
								return;
							}
						}
						else if(isRoomForHarvest())
						{
							miningDelay = 0;
							valuableIndex++;
							return;
						}
						else
						{
							miningDelay= Math.min(miningDelay+1, 20);
							return;
						}
					}
					else if(valuableIndex >= valuablesInLayer.size())
					{
						currentMode = 0;
						--currentLayer;
					}
				}
			}
		}
	}

	private void scanLayer()
	{
		//WarpDrive.debugPrint("Scanning layer");
		valuablesInLayer.clear();
		int xmax, zmax, x1, x2, z1, z2;
		int xmin, zmin;
		x1 = xCoord + digX / 2;
		x2 = xCoord - digX / 2;

		if (x1 < x2)
		{
			xmin = x1;
			xmax = x2;
		}
		else
		{
			xmin = x2;
			xmax = x1;
		}

		z1 = zCoord + digZ / 2;
		z2 = zCoord - digZ / 2;

		if (z1 < z2)
		{
			zmin = z1;
			zmax = z2;
		}
		else
		{
			zmin = z2;
			zmax = z1;
		}
		defineMiningArea(xmin,zmin,xmax,zmax);
		
		// Search for valuable blocks
		for (int x = xmin; x <= xmax; x++)
			for (int z = zmin; z <= zmax; z++)
			{
				int blockID = worldObj.getBlockId(x, currentLayer, z);
				if (canDig(blockID))
					if (isQuarry)   // Quarry collects all blocks
					{
						if (!worldObj.isAirBlock(x, currentLayer, z) && blockID != Block.lavaMoving.blockID && blockID != Block.lavaStill.blockID)
							valuablesInLayer.add(new Vector3(x, currentLayer, z));
					}
					else   // Not-quarry collect only valuables blocks
						if (WarpDriveConfig.MinerOres.contains(worldObj.getBlockId(x, currentLayer, z)))
							valuablesInLayer.add(new Vector3(x, currentLayer, z));
			}

		valuableIndex = 0;
		//WarpDrive.debugPrint("[ML] Found " + valuablesInLayer.size() + " valuables");
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		isMining = tag.getBoolean("isMining");
		isQuarry = tag.getBoolean("isQuarry");
		currentLayer = tag.getInteger("currentLayer");
		minLayer= tag.getInteger("minLayer");
		
		digX = tag.getInteger("digX");
		digZ = tag.getInteger("digZ");
		
		speedMul = tag.getDouble("speedMul");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tag.setBoolean("isMining", isMining);
		tag.setBoolean("isQuarry", isQuarry);
		tag.setInteger("currentLayer", currentLayer);
		tag.setInteger("minLayer", minLayer);
		
		tag.setInteger("digX", digX);
		tag.setInteger("digZ", digZ);
		
		tag.setDouble("speedMul", speedMul);
	}
//CC
	// IPeripheral methods implementation
	@Override
	public String getType()
	{
		return "mininglaser";
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
			case 0: // Mine()
				if (isMining)
					return new Boolean[] { false };
				currentLayer = yCoord - layerOffset;
				digX = CUBE_SIDE;
				digZ = CUBE_SIDE;
				try
				{
					if(arguments.length >= 2)
					{
						digX = Math.min(toInt(arguments[0]),WarpDriveConfig.ML_MAX_SIZE);
						digZ = Math.min(toInt(arguments[1]),WarpDriveConfig.ML_MAX_SIZE);
					}
					else
					{
						digX = CUBE_SIDE;
						digZ = CUBE_SIDE;
					}
					isQuarry = false;
					delayTicksScan = 0;
					currentMode = 0;
					isMining = true;
					defineMiningArea(digX,digZ);
				}
				catch(NumberFormatException e)
				{
					isMining = false;
					refreshLoading();
					return new Boolean[] { false };
				}
				return new Boolean[] { true };

			case 1: // stop()
				isMining = false;
				refreshLoading();
				break;

			case 2: // isMining()
				return new Boolean[] { isMining };
			case 3: // Quarry()
				if (isMining)
					return new Boolean[] { false };

				isQuarry = true;
				delayTicksScan = 0;
				currentMode = 0;
				currentLayer = yCoord - layerOffset;
				isMining = true;
				digX = CUBE_SIDE;
				digZ = CUBE_SIDE;
				try
				{
					if(arguments.length >= 2)
					{
						digX = Math.min(toInt(arguments[0]),WarpDriveConfig.ML_MAX_SIZE);
						digZ = Math.min(toInt(arguments[1]),WarpDriveConfig.ML_MAX_SIZE);
					}
					defineMiningArea(digX,digZ);
				}
				catch(NumberFormatException e)
				{
					isMining = false;
					refreshLoading();
					return new Boolean[] { false };
				}
				return new Boolean[] { true };

			case 4: // State is: state, energy, currentLayer, valuablesMined, valuablesInLayer = getMinerState()
				String state = "not mining";
				int valuablesMined   = 0;
				int valuablesInLayer = 0;
				if (isMining)
				{
					valuablesInLayer = this.valuablesInLayer.size();
					valuablesMined = this.valuableIndex;
					state = "mining" + ((isQuarry) ? " (quarry mode)" : "");
					if (energy() < 0)
						state = "out of energy";
				}
				return new Object[] {state, getEnergyObject(), currentLayer, valuablesMined, valuablesInLayer,
						digX, digZ, speedMul, fortune(), silkTouch()};

			case 5: // Offset
				if (arguments.length == 1)
				{
					int t = ((Double)arguments[0]).intValue();
					if (t < 0)
						t = 0;
					layerOffset = t + 1;
				}
				return new Integer[] { layerOffset-1 };
			case 6: // silktouch(1/boolean)
				if (arguments.length == 1)
					silkTouch(toBool(arguments[0]));
				return new Boolean[] { silkTouch() };
			case 7: // fortune(int)
				if (arguments.length == 1)
					fortune(toInt(arguments[0]));
				return new Integer[] { fortune() };
			case 8: // speedMul(double)
				if (arguments.length == 1)
				{
					try
					{
						Double arg = Double.parseDouble(arguments[0].toString());
						speedMul = Math.min(WarpDriveConfig.ML_MAX_SPEED,Math.max(arg,WarpDriveConfig.ML_MIN_SPEED));
					}
					catch(NumberFormatException e)
					{
						speedMul = 1;
					}
				}
				return new Double[] { speedMul };
			case 9: //layer
			{
				try
				{
					if(arguments.length >= 1)
					{
						currentLayer = Math.min(yCoord-1, Math.max(1, toInt(arguments[0])));
						if(isMining)
							currentMode = 0;
					}
				}
				catch(NumberFormatException e)
				{
					return new String[] { "NaN" };
				}
				return new Integer[] { currentLayer };
			}
			case 10: //setMinLayer
			{
				try
				{
					if(arguments.length >= 1)
						minLayer = Math.min(yCoord-1, Math.max(1, toInt(arguments[0])));
				}
				catch(NumberFormatException e)
				{
					return new String[] { "NaN" };
				}
				return new Integer[] { currentLayer };
			}
			case 11:
			{
				return getEnergyObject();
			}
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
	public boolean shouldChunkLoad() {
		return isMining;
	}
	
	//ABSTRACT LASER IMPLEMENTATION
	@Override
	protected boolean canSilkTouch()
	{
		return true;
	}
	
	@Override
	protected int minFortune()
	{
		return 0;
	}

	@Override
	protected int maxFortune()
	{
		return 5;
	}

	@Override
	protected double laserBelow()
	{
		return 0.5;
	}

	@Override
	protected float getColorR()
	{
		return 0f;
	}

	@Override
	protected float getColorG()
	{
		return 0f;
	}

	@Override
	protected float getColorB()
	{
		return 1f;
	}

	@Override
	public boolean equals(IPeripheral other) {
		// TODO Auto-generated method stub
		return false;
	}
}
