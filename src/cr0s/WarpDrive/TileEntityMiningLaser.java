package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
import appeng.api.WorldCoord;
import appeng.api.IAEItemStack;
import appeng.api.Util;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.ITileCable;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;


public class TileEntityMiningLaser extends TileEntity implements IPeripheral, IGridMachine, ITileCable
{
	Boolean powerStatus = false;
	private IGridInterface grid;

	private final int MAX_BOOSTERS_NUMBER = 1;

	private int dx, dz, dy;
	private boolean isMining = false;
	private boolean isQuarry = false;
	private boolean useDeiterium = false;
	private boolean AENetworkReady = false;

	private String[] methodsArray =
	{
		"mine",		//0
		"stop",		//1
		"isMining",	//2
		"quarry",	//3
		"state",	//4
		"offset"	//5
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
				if (++delayTicksScan > WarpDriveConfig.i.ML_SCAN_DELAY)
				{
					delayTicksScan = 0;
					valuablesInLayer.clear();
					valuableIndex = 0;
					if (!collectEnergyPacketFromBooster(isOnEarth ? WarpDriveConfig.i.ML_EU_PER_LAYER_EARTH : WarpDriveConfig.i.ML_EU_PER_LAYER_SPACE, true))
						return;
					while (currentLayer > 0)
					{
						scanLayer();
						if (valuablesInLayer.size() > 0)
						{
							if (!collectEnergyPacketFromBooster(isOnEarth ? WarpDriveConfig.i.ML_EU_PER_LAYER_EARTH : WarpDriveConfig.i.ML_EU_PER_LAYER_SPACE, false))
								return;
							sendLaserPacket(minerVector, new Vector3(xCoord, currentLayer, zCoord).add(0.5), 0, 0, 1, 20, 0, 50);
							worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
							int blockID = worldObj.getBlockId(xCoord, currentLayer, zCoord);
							if (blockID != 0)
								if (canDig(blockID))
									harvestBlock(new Vector3(xCoord, currentLayer, zCoord));
							currentMode = 1;
							return;
						}
						else
							--currentLayer;
					}
					if (currentLayer <= 0)
						isMining = false;
				}
			}
			else
			{
				if (++delayTicksMine > WarpDriveConfig.i.ML_MINE_DELAY)
				{
					delayTicksMine = 0;

					if (valuableIndex < valuablesInLayer.size())
					{
						//System.out.println("[ML] Mining: " + (valuableIndex + 1) + "/" + valuablesInLayer.size());
						Vector3 valuable = valuablesInLayer.get(valuableIndex++);
						// Mine valuable ore
						int blockID = worldObj.getBlockId(valuable.intX(), valuable.intY(), valuable.intZ());

						// Skip if block is too hard or its empty block
						if (!canDig(blockID))
							return;

						sendLaserPacket(minerVector, new Vector3(valuable.intX(), valuable.intY(), valuable.intZ()).add(0.5), 1, 1, 0, 2 * WarpDriveConfig.i.ML_MINE_DELAY, 0, 50);
						worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
						harvestBlock(valuable);
					}
					else
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

	private void harvestBlock(Vector3 valuable)
	{
		int blockID = worldObj.getBlockId(valuable.intX(), valuable.intY(), valuable.intZ());
		int blockMeta = worldObj.getBlockMetadata(valuable.intX(), valuable.intY(), valuable.intZ());
		if (blockID != Block.waterMoving.blockID && blockID != Block.waterStill.blockID && blockID != Block.lavaMoving.blockID && blockID != Block.lavaStill.blockID)
		{
			List<ItemStack> stacks = getItemStackFromBlock(valuable.intX(), valuable.intY(), valuable.intZ(), blockID, blockMeta);
			if (stacks != null)
				for (ItemStack stack : stacks)
				{
					if (grid != null && AENetworkReady)
						putInGrid(stack);
					else
						putInChest(findChest(), stack);
				}
			worldObj.playAuxSFXAtEntity(null, 2001, valuable.intX(), valuable.intY(), valuable.intZ(), blockID + (blockMeta << 12));
		}
		else if (blockID == Block.waterMoving.blockID || blockID == Block.waterStill.blockID)
		// Evaporate water
			worldObj.playSoundEffect((double)((float)valuable.intX() + 0.5F), (double)((float)valuable.intY() + 0.5F), (double)((float)valuable.intZ() + 0.5F), "random.fizz", 0.5F, 2.6F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.8F);
		worldObj.setBlockToAir(valuable.intX(), valuable.intY(), valuable.intZ());
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

	public List<ItemStack> getItemStackFromBlock(int i, int j, int k, int blockID, int blockMeta)
	{
		Block block = Block.blocksList[blockID];
		if (block == null)
			return null;
		if (useDeiterium && grid != null && AENetworkReady)
		{
			IMEInventoryHandler cellArray = grid.getCellArray();
			if (cellArray != null)
			{
				int consume = isQuarry?15:1000;
				IAEItemStack entryToAEIS = Util.createItemStack(new ItemStack(WarpDriveConfig.i.AEExtraFDI, consume, FluidRegistry.getFluidID("deuterium")));
				long contained = cellArray.countOfItemType(entryToAEIS);
				if (block.canSilkHarvest(worldObj, null, i, j, k, blockMeta) && contained >= consume)
				{
					cellArray.extractItems(entryToAEIS);
					ArrayList<ItemStack> t = new ArrayList<ItemStack>();
					t.add(new ItemStack(blockID, 1, blockMeta));
					return t;
				}
			}
		}
		return block.getBlockDropped(worldObj, i, j, k, blockMeta, 0);
	}

	public int putInGrid(ItemStack itemStackSource)
	{
		int transferred = itemStackSource.stackSize;
		IMEInventoryHandler cellArray = grid.getCellArray();
		if (cellArray != null)
		{
			IAEItemStack ret = cellArray.addItems(Util.createItemStack(itemStackSource));
			if (ret != null)
				transferred -= ret.getStackSize();
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
		final int CUBE_SIDE = 8;
		x1 = xCoord + CUBE_SIDE / 2;
		x2 = xCoord - CUBE_SIDE / 2;

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

		z1 = zCoord + CUBE_SIDE / 2;
		z2 = zCoord - CUBE_SIDE / 2;

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
		useDeiterium = tag.getBoolean("useDeiterium");
		minerVector = new Vector3(xCoord, yCoord - 1, zCoord).add(0.5);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tag.setBoolean("isMining", isMining);
		tag.setBoolean("isQuarry", isQuarry);
		tag.setInteger("currentLayer", currentLayer);
		tag.setBoolean("useDeiterium", useDeiterium);
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
				isQuarry = false;
				delayTicksScan = 0;
				currentMode = 0;
				minerVector = new Vector3(xCoord, yCoord - 1, zCoord).add(0.5);
				currentLayer = yCoord - layerOffset;
				isMining = true;
				useDeiterium = (arguments.length == 1 && FluidRegistry.isFluidRegistered("deuterium"));
				return new Boolean[] { true };

			case 1: // stop()
				isMining = false;
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
				useDeiterium = (arguments.length == 1 && FluidRegistry.isFluidRegistered("deuterium"));
				return new Boolean[] { true };

			case 4: // State is: state, energy, currentLayer, valuablesMined, valuablesInLayer = getMinerState()
				int energy = 0;
				if (booster != null)
					energy = booster.getCurrentEnergyValue();
				String state = "not mining";
				Integer valuablesInLayer, valuablesMined;
				if (isMining)
				{
					valuablesInLayer = this.valuablesInLayer.size();
					valuablesMined = this.valuableIndex;
					state = "mining" + ((isQuarry) ? " (quarry mode)" : "");
					if (energy < 0)
						state = "out of energy";
					return new Object[] {state, energy, currentLayer, valuablesMined, valuablesInLayer};
				}
				return new Object[] {state, energy, currentLayer, 0, 0};

			case 5: // Offset
				if (arguments.length == 1)
				{
					int t = ((Double)arguments[0]).intValue();
					if (t < 0)
						t = 0;
					layerOffset = t + 1;
				}
				return new Integer[] { layerOffset-1 };
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

	public void setNetworkReady( boolean isReady )
	{
		AENetworkReady = isReady;
	}

	public boolean isMachineActive()
	{
		return true;
	}
}
