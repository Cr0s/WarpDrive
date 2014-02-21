package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.Vector3;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet62LevelSound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.WorldCoord;
import appeng.api.IAEItemStack;
import appeng.api.Util;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.ITileCable;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;


public class TileEntityMiningLaser extends WarpChunkTE implements IPeripheral, IGridMachine, ITileCable
{
	Boolean powerStatus = false;
	private IGridInterface grid;

	private final int MAX_BOOSTERS_NUMBER = 1;

	private int digX,digZ = 8;
	private final int CUBE_SIDE = 8;
	
	private int dx, dz, dy;
	private boolean isMining = false;
	private boolean isQuarry = false;
	
	private double speedMul = 1;
	
	private boolean silkTouch = false;
	private int fortuneLevel = 0;
	
	private int miningDelay = 0;
	private int minLayer = 1;
	
	public void setNetworkReady( boolean isReady )
	{
		return ;
	}
	
	public boolean isMachineActive()
	{
		return true;
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
		"minLayer" //10
	};

	private int delayTicksScan = 0;
	private int delayTicksMine = 0;
	private int currentMode = 0; // 0 - scan next layer, 1 - collect valuables

	private int currentLayer;

	private ArrayList<Vector3> valuablesInLayer = new ArrayList<Vector3>();
	private int valuableIndex = 0;

	private int layerOffset = 1;

	private Vector3 minerVector;
	//private long uid = 0;

	TileEntityParticleBooster booster = null;

	private boolean isOnEarth = false;
	//int t = 20;
	@Override
	public void updateEntity()
	{
		if(minLayer > yCoord - 1)
			minLayer = yCoord - 1;
		if(currentLayer > yCoord - 1)
			currentLayer = yCoord - 1;
		if(speedMul == 0)
			speedMul = 1;
		speedMul = Math.max(WarpDriveConfig.i.ML_MIN_SPEED,Math.min(WarpDriveConfig.i.ML_MAX_SPEED,speedMul));
		if (isMining)
		{
			isOnEarth = (worldObj.provider.dimensionId == 0);
			if (minerVector != null)
			{
				minerVector.x = xCoord;
				minerVector.y = yCoord - 1;
				minerVector.z = zCoord;
				minerVector = minerVector.add(0.5);
			}

			if (currentMode == 0)
			{
				if (++delayTicksScan > (WarpDriveConfig.i.ML_SCAN_DELAY / speedMul))
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
						refreshLoading();
						isMining = false;
					}
				}
			}
			else
			{
				if (++delayTicksMine > ((WarpDriveConfig.i.ML_MINE_DELAY / speedMul) + miningDelay))
				{
					delayTicksMine = 0;
					int energyReq = calculateBlockCost();
					if (collectEnergyPacketFromBooster(energyReq,true) && valuableIndex < valuablesInLayer.size())
					{
						//System.out.println("[ML] Mining: " + (valuableIndex + 1) + "/" + valuablesInLayer.size());
						Vector3 valuable = valuablesInLayer.get(valuableIndex);
						// Mine valuable ore
						int blockID = worldObj.getBlockId(valuable.intX(), valuable.intY(), valuable.intZ());

						// Skip if block is too hard or its empty block
						if (!canDig(blockID))
						{
							WarpDrive.debugPrint("Cannot mine: " + blockID);
							valuableIndex++;
							return;
						}
						
						if((WarpDriveConfig.i.MinerOres.contains(blockID) || isQuarry) && isRoomForHarvest())
						{
							if(collectEnergyPacketFromBooster(energyReq,false))
							{
								sendLaserPacket(minerVector, new Vector3(valuable.intX(), valuable.intY(), valuable.intZ()).add(0.5), 1, 1, 0, 2 * WarpDriveConfig.i.ML_MINE_DELAY, 0, 50);
								worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
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


	private boolean canDig(int blockID)
	{
		if (Block.blocksList[blockID] != null)
			return ((blockID == WarpDriveConfig.i.GT_Granite || blockID == WarpDriveConfig.i.GT_Ores || blockID == WarpDriveConfig.i.iridiumID || Block.blocksList[blockID].blockResistance <= Block.obsidian.blockResistance) && blockID != WarpDriveConfig.i.MFFS_Field && blockID != Block.bedrock.blockID);
		else
			return (blockID != WarpDriveConfig.i.MFFS_Field && blockID != Block.bedrock.blockID);
	}

	private boolean harvestBlock(Vector3 valuable)
	{
		int blockID = worldObj.getBlockId(valuable.intX(), valuable.intY(), valuable.intZ());
		int blockMeta = worldObj.getBlockMetadata(valuable.intX(), valuable.intY(), valuable.intZ());
		if (blockID != Block.waterMoving.blockID && blockID != Block.waterStill.blockID && blockID != Block.lavaMoving.blockID && blockID != Block.lavaStill.blockID)
		{
			boolean didPlace = true;
			List<ItemStack> stacks = getItemStackFromBlock(valuable.intX(), valuable.intY(), valuable.intZ(), blockID, blockMeta);
			if (stacks != null)
			{
				for (ItemStack stack : stacks)
				{
					if (grid != null)
						didPlace = didPlace && putInGrid(stack) == stack.stackSize;
					else
						didPlace = didPlace && putInChest(findChest(), stack) == stack.stackSize;
				}
			}
			worldObj.playAuxSFXAtEntity(null, 2001, valuable.intX(), valuable.intY(), valuable.intZ(), blockID + (blockMeta << 12));
			worldObj.setBlockToAir(valuable.intX(), valuable.intY(), valuable.intZ());
			return didPlace;
		}
		else if (blockID == Block.waterMoving.blockID || blockID == Block.waterStill.blockID)
		// Evaporate water
			worldObj.playSoundEffect((double)((float)valuable.intX() + 0.5F), (double)((float)valuable.intY() + 0.5F), (double)((float)valuable.intZ() + 0.5F), "random.fizz", 0.5F, 2.6F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.8F);
		worldObj.setBlockToAir(valuable.intX(), valuable.intY(), valuable.intZ());
		return true;
	}

	private IInventory findChest()
	{
		TileEntity result = null;
		result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);

		if (result != null && result instanceof IInventory)
		{
			return (IInventory) result;
		}

		result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);

		if (result != null && result instanceof IInventory)
		{
			return (IInventory) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);

		if (result != null && result instanceof IInventory)
		{
			return (IInventory) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);

		if (result != null && result instanceof IInventory)
		{
			return (IInventory) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);

		if (result != null && result instanceof IInventory)
		{
			return (IInventory) result;
		}

		return null;
	}
	
	private int calculateLayerCost()
	{
		return isOnEarth ? WarpDriveConfig.i.ML_EU_PER_LAYER_EARTH : WarpDriveConfig.i.ML_EU_PER_LAYER_SPACE;
	}
	
	private int calculateBlockCost()
	{
		int enPerBlock = isOnEarth ? WarpDriveConfig.i.ML_EU_PER_BLOCK_EARTH : WarpDriveConfig.i.ML_EU_PER_BLOCK_SPACE;
		if(silkTouch)
			return (int) Math.round(enPerBlock * WarpDriveConfig.i.ML_EU_MUL_SILKTOUCH * speedMul);
		return (int) Math.round(enPerBlock * (Math.pow(WarpDriveConfig.i.ML_EU_MUL_FORTUNE, fortuneLevel)) * speedMul);
	}
	
	private boolean isRoomForHarvest()
	{
		if(grid != null)
			return true;
		
		IInventory inv = findChest();
		if(inv != null)
		{
			int size = inv.getSizeInventory();
			for(int i=0;i<size;i++)
				if(inv.getStackInSlot(i) == null)
					return true;
		}
		return false;
	}

	public List<ItemStack> getItemStackFromBlock(int i, int j, int k, int blockID, int blockMeta)
	{
		Block block = Block.blocksList[blockID];
		if (block == null)
			return null;
		if (silkTouch)
		{
			if (block.canSilkHarvest(worldObj, null, i, j, k, blockMeta))
			{
				ArrayList<ItemStack> t = new ArrayList<ItemStack>();
				t.add(new ItemStack(blockID, 1, blockMeta));
				return t;
			}
		}
		return block.getBlockDropped(worldObj, i, j, k, blockMeta, fortuneLevel);
	}

	public int putInGrid(ItemStack itemStackSource)
	{
		int transferred = 0;
		IMEInventoryHandler cellArray = grid.getCellArray();
		if (cellArray != null)
		{
			IAEItemStack ret = cellArray.addItems(Util.createItemStack(itemStackSource));
			if (ret != null)
				transferred = (int) ret.getStackSize();
		}
		return transferred;
	}

	public int putInChest(IInventory inventory, ItemStack itemStackSource)
	{
		if (inventory == null || itemStackSource == null)
		{
			return 0;
		}

		int transferred = 0;

		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			if (!inventory.isItemValidForSlot(i, itemStackSource))
			{
				continue;
			}

			ItemStack itemStack = inventory.getStackInSlot(i);

			if (itemStack == null || !itemStack.isItemEqual(itemStackSource))
			{
				continue;
			}

			int transfer = Math.min(itemStackSource.stackSize - transferred, itemStack.getMaxStackSize() - itemStack.stackSize);
			itemStack.stackSize += transfer;
			transferred += transfer;

			if (transferred == itemStackSource.stackSize)
			{
				return transferred;
			}
		}

		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			if (!inventory.isItemValidForSlot(i, itemStackSource))
			{
				continue;
			}

			ItemStack itemStack = inventory.getStackInSlot(i);

			if (itemStack != null)
			{
				continue;
			}

			int transfer = Math.min(itemStackSource.stackSize - transferred, itemStackSource.getMaxStackSize());
			ItemStack dest = copyWithSize(itemStackSource, transfer);
			inventory.setInventorySlotContents(i, dest);
			transferred += transfer;

			if (transferred == itemStackSource.stackSize)
			{
				return transferred;
			}
		}

		return transferred;
	}

	public ItemStack copyWithSize(ItemStack itemStack, int newSize)
	{
		ItemStack ret = itemStack.copy();
		ret.stackSize = newSize;
		return ret;
	}

	private void scanLayer()
	{
		//System.out.println("Scanning layer");
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

		//System.out.println("Layer: xmax: " + xmax + ", xmin: " + xmin);
		//System.out.println("Layer: zmax: " + zmax + ", zmin: " + zmin);

		minChunk = worldObj.getChunkFromBlockCoords(xmin,zmin).getChunkCoordIntPair();
		maxChunk = worldObj.getChunkFromBlockCoords(xmax,zmax).getChunkCoordIntPair();
		refreshLoading();
		
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
						if (WarpDriveConfig.i.MinerOres.contains(worldObj.getBlockId(x, currentLayer, z)))
							valuablesInLayer.add(new Vector3(x, currentLayer, z));
			}

		valuableIndex = 0;
		//System.out.println("[ML] Found " + valuablesInLayer.size() + " valuables");
	}

	private boolean collectEnergyPacketFromBooster(int packet, boolean test)
	{
		if (booster == null)
			booster = findFirstBooster();
		if (booster != null)
			if (test)
				return packet <= booster.getCurrentEnergyValue();
			else
				return booster.consumeEnergy(packet);
		return false;
	}

	private TileEntityParticleBooster findFirstBooster()
	{
		TileEntity result;
		result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);

		if (result != null && result instanceof TileEntityParticleBooster)
		{
			dx = 1;
			dz = 0;
			dy = 0;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);

		if (result != null && result instanceof TileEntityParticleBooster)
		{
			dx = -1;
			dz = 0;
			dy = 0;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);

		if (result != null && result instanceof TileEntityParticleBooster)
		{
			dx = 0;
			dz = 1;
			dy = 0;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);

		if (result != null && result instanceof TileEntityParticleBooster)
		{
			dx = 0;
			dz = -1;
			dy = 0;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);

		if (result != null && result instanceof TileEntityParticleBooster)
		{
			dx = 0;
			dz = 0;
			dy = 1;
			return (TileEntityParticleBooster) result;
		}

		return null;
	}

	public void sendLaserPacket(Vector3 source, Vector3 dest, float r, float g, float b, int age, int energy, int radius)
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();

		if (side == Side.SERVER)
		{
			if (source == null || dest == null || worldObj == null)
			{
				return;
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
			DataOutputStream outputStream = new DataOutputStream(bos);

			try
			{
				// Write source vector
				outputStream.writeDouble(source.x);
				outputStream.writeDouble(source.y);
				outputStream.writeDouble(source.z);
				// Write target vector
				outputStream.writeDouble(dest.x);
				outputStream.writeDouble(dest.y);
				outputStream.writeDouble(dest.z);
				// Write r, g, b of laser
				outputStream.writeFloat(r);
				outputStream.writeFloat(g);
				outputStream.writeFloat(b);
				// Write age
				outputStream.writeByte(age);
				// Write energy value
				outputStream.writeInt(0);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = "WarpDriveBeam";
			packet.data = bos.toByteArray();
			packet.length = bos.size();
			MinecraftServer.getServer().getConfigurationManager().sendToAllNear(source.intX(), source.intY(), source.intZ(), radius, worldObj.provider.dimensionId, packet);
			MinecraftServer.getServer().getConfigurationManager().sendToAllNear(dest.intX(), dest.intY(), dest.intZ(), radius, worldObj.provider.dimensionId, packet);
		}
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
		
		silkTouch = tag.getBoolean("silkTouch");
		fortuneLevel = tag.getInteger("fortuneLevel");
		speedMul = tag.getDouble("speedMul");
		
		minerVector = new Vector3(xCoord, yCoord - 1, zCoord).add(0.5);
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
		
		tag.setBoolean("silkTouch", silkTouch);
		tag.setInteger("fortuneLevel", fortuneLevel);
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
	
	private int toInt(Object o)
	{
		return (int) Math.round(Double.parseDouble(o.toString()));
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		switch (method)
		{
			case 0: // Mine()
				if (isMining)
					return new Boolean[] { false };
				minerVector = new Vector3(xCoord, yCoord - 1, zCoord).add(0.5);
				currentLayer = yCoord - layerOffset;
				digX = CUBE_SIDE;
				digZ = CUBE_SIDE;
				try
				{
					if(arguments.length >= 2)
					{
						try
						{
							digX = Math.min(toInt(arguments[0]),WarpDriveConfig.i.ML_MAX_SIZE);
							digZ = Math.min(toInt(arguments[1]),WarpDriveConfig.i.ML_MAX_SIZE);
							
							isQuarry = false;
							delayTicksScan = 0;
							currentMode = 0;
							isMining = true;
						}
						catch(NumberFormatException e)
						{
							
						}
					}
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
				minerVector = new Vector3(xCoord, yCoord - 1, zCoord).add(0.5);
				currentLayer = yCoord - layerOffset;
				isMining = true;
				digX = CUBE_SIDE;
				digZ = CUBE_SIDE;
				try
				{
					if(arguments.length >= 2)
					{
						digX = Math.min(toInt(arguments[0]),WarpDriveConfig.i.ML_MAX_SIZE);
						digZ = Math.min(toInt(arguments[1]),WarpDriveConfig.i.ML_MAX_SIZE);
					}
				}
				catch(NumberFormatException e)
				{
					isMining = false;
					refreshLoading();
					return new Boolean[] { false };
				}
				return new Boolean[] { true };

			case 4: // State is: state, energy, currentLayer, valuablesMined, valuablesInLayer = getMinerState()
				int energy = 0;
				if (booster != null)
					energy = booster.getCurrentEnergyValue();
				String state = "not mining";
				int valuablesMined   = 0;
				int valuablesInLayer = 0;
				if (isMining)
				{
					valuablesInLayer = this.valuablesInLayer.size();
					valuablesMined = this.valuableIndex;
					state = "mining" + ((isQuarry) ? " (quarry mode)" : "");
					if (energy < 0)
						state = "out of energy";
				}
				return new Object[] {state, energy, currentLayer, valuablesMined, valuablesInLayer,
						digX, digZ, speedMul, fortuneLevel, silkTouch};

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
					silkTouch = arguments[0].toString() == "true" || arguments[0].toString() == "1";
				return new Boolean[] { silkTouch };
			case 7: // fortune(int)
				if (arguments.length == 1)
				{
					try
					{
						fortuneLevel = (int) Math.round(Math.max(0, Math.min(Double.parseDouble(arguments[0].toString()),5)));
					}
					catch (NumberFormatException e)
					{
						fortuneLevel = 0;
					}
				}
				return new Integer[] { fortuneLevel };
			case 8: // speedMul(double)
				if (arguments.length == 1)
				{
					try
					{
						Double arg = Double.parseDouble(arguments[0].toString());
						speedMul = Math.min(WarpDriveConfig.i.ML_MAX_SPEED,Math.max(arg,WarpDriveConfig.i.ML_MIN_SPEED));
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
	}

	@Override
	public void detach(IComputerAccess computer)
	{
	}
//AE
	@Override
	public float getPowerDrainPerTick()
	{
		return 1;
	}

	@Override
	public void validate()
	{
		super.validate();
		MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent(this, worldObj, getLocation()));
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		MinecraftForge.EVENT_BUS.post(new GridTileUnloadEvent(this, worldObj, getLocation()));
	}

	@Override
	public WorldCoord getLocation()
	{
		return new WorldCoord(xCoord, yCoord, zCoord);
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void setPowerStatus(boolean hasPower)
	{
		powerStatus = hasPower;
	}

	@Override
	public boolean isPowered()
	{
		return powerStatus;
	}

	@Override
	public IGridInterface getGrid()
	{
		return grid;
	}

	@Override
	public void setGrid(IGridInterface gi)
	{
		grid = gi;
	}

	@Override
	public World getWorld()
	{
		return worldObj;
	}

	@Override
	public boolean coveredConnections()
	{
		return true;
	}

	@Override
	public boolean shouldChunkLoad() {
		return isMining;
	}
}
