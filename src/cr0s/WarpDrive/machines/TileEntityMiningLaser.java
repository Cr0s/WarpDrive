package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
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
import cr0s.WarpDrive.*;

public class TileEntityMiningLaser extends TileEntity implements IPeripheral, IGridMachine, ITileCable {
	Boolean powerStatus = false;
	private IGridInterface grid;

	private int dx, dz, dy;
	private boolean isMining() {
		return currentState != STATE_IDLE; 
	}
	private boolean isQuarry = false;
	private boolean useDeiterium = false;
	private boolean AENetworkReady = false;

	private String[] methodsArray = {
		"mine",		//0
		"stop",		//1
		"isMining",	//2
		"quarry",	//3
		"state",	//4
		"offset"	//5
	};

	private int delayTicksWarmup = 0;
	private int delayTicksScan = 0;
	private int delayTicksMine = 0;
	private final int STATE_IDLE = 0;
	private final int STATE_WARMUP = 1;
	private final int STATE_SCANNING = 2;
	private final int STATE_MINING = 3;
	private int currentState = 0; // 0 - scan next layer, 1 - collect valuables
	private boolean enoughPower = false;
	private int currentLayer;

	private ArrayList<Vector3> valuablesInLayer = new ArrayList<Vector3>();
	private int valuableIndex = 0;

	private int layerOffset = 1;

	@Override
	public void updateEntity() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }
		if (currentState == STATE_IDLE) {
			delayTicksWarmup = 0;
			delayTicksScan = 0;
			delayTicksMine = 0;
			updateMetadata(BlockMiningLaser.ICON_IDLE);
			return;
		}
		
		boolean isOnEarth = (worldObj.provider.dimensionId == 0);
		
		Vector3 minerVector = new Vector3(xCoord + 0.5D, yCoord, zCoord + 0.5D);

		if (currentState == STATE_WARMUP) { // warming up
			delayTicksWarmup++;
			updateMetadata(BlockMiningLaser.ICON_SCANNINGLOWPOWER);
			if (delayTicksWarmup >= WarpDriveConfig.ML_WARMUP_DELAY_TICKS) {
				delayTicksScan = 0;
				currentState = STATE_SCANNING;
				updateMetadata(BlockMiningLaser.ICON_SCANNINGLOWPOWER);
				return;
			}
		} else if (currentState == STATE_SCANNING) { // scanning
			delayTicksScan++;
			if (delayTicksScan == 1) {
				// check power level
				enoughPower = consumeEnergyPacketFromBooster(isOnEarth ? WarpDriveConfig.ML_EU_PER_LAYER_EARTH : WarpDriveConfig.ML_EU_PER_LAYER_SPACE, true); 
				if (!enoughPower) {
					updateMetadata(BlockMiningLaser.ICON_SCANNINGLOWPOWER);
					delayTicksScan = 0;
					return;
				} else {
					updateMetadata(BlockMiningLaser.ICON_SCANNINGPOWERED);
				}
				// show current layer
				int age = Math.max(40, 5 * WarpDriveConfig.ML_SCAN_DELAY_TICKS);
				double xmax = xCoord + WarpDriveConfig.ML_MAX_RADIUS + 1.0D;
				double xmin = xCoord - WarpDriveConfig.ML_MAX_RADIUS + 0.0D;
				double zmax = zCoord + WarpDriveConfig.ML_MAX_RADIUS + 1.0D;
				double zmin = zCoord - WarpDriveConfig.ML_MAX_RADIUS + 0.0D;
				double y = currentLayer + 1.0D;
				WarpDrive.sendLaserPacket(worldObj, new Vector3(xmin, y, zmin), new Vector3(xmax, y, zmin), 0.3F, 0.0F, 1.0F, age, 0, 50);
				WarpDrive.sendLaserPacket(worldObj, new Vector3(xmax, y, zmin), new Vector3(xmax, y, zmax), 0.3F, 0.0F, 1.0F, age, 0, 50);
				WarpDrive.sendLaserPacket(worldObj, new Vector3(xmax, y, zmax), new Vector3(xmin, y, zmax), 0.3F, 0.0F, 1.0F, age, 0, 50);
				WarpDrive.sendLaserPacket(worldObj, new Vector3(xmin, y, zmax), new Vector3(xmin, y, zmin), 0.3F, 0.0F, 1.0F, age, 0, 50);
			} else if (delayTicksScan >= WarpDriveConfig.ML_SCAN_DELAY_TICKS) {
				delayTicksScan = 0;
				if (currentLayer <= 0) {
					stop();
					return;
				}
				// consume power
				enoughPower = consumeEnergyPacketFromBooster(isOnEarth ? WarpDriveConfig.ML_EU_PER_LAYER_EARTH : WarpDriveConfig.ML_EU_PER_LAYER_SPACE, false); 
				if (!enoughPower) {
					updateMetadata(BlockMiningLaser.ICON_SCANNINGLOWPOWER);
					return;
				} else {
					updateMetadata(BlockMiningLaser.ICON_SCANNINGPOWERED);
				}
				// scan
				scanLayer();
				if (valuablesInLayer.size() > 0) {
					int r = (int)Math.ceil(WarpDriveConfig.ML_MAX_RADIUS / 2.0D);
					int offset = (yCoord - currentLayer) % (2 * r);
					int age = Math.max(20, Math.round(2.5F * WarpDriveConfig.ML_SCAN_DELAY_TICKS));
					double y = currentLayer + 1.0D;
					WarpDrive.sendLaserPacket(worldObj, minerVector, new Vector3(xCoord - r + offset, y, zCoord + r         ).translate(0.3D), 0.0F, 0.0F, 1.0F, age, 0, 50);
					WarpDrive.sendLaserPacket(worldObj, minerVector, new Vector3(xCoord + r         , y, zCoord + r - offset).translate(0.3D), 0.0F, 0.0F, 1.0F, age, 0, 50);
					WarpDrive.sendLaserPacket(worldObj, minerVector, new Vector3(xCoord + r - offset, y, zCoord - r         ).translate(0.3D), 0.0F, 0.0F, 1.0F, age, 0, 50);
					WarpDrive.sendLaserPacket(worldObj, minerVector, new Vector3(xCoord - r         , y, zCoord - r + offset).translate(0.3D), 0.0F, 0.0F, 1.0F, age, 0, 50);
					worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
					delayTicksMine = 0;
					currentState = STATE_MINING;
					updateMetadata(BlockMiningLaser.ICON_MININGPOWERED);
					return;
				} else {
					worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
					currentLayer--;
				}
			}
		} else if (currentState == STATE_MINING) { // mining
			delayTicksMine++;
			if (delayTicksMine >= WarpDriveConfig.ML_MINE_DELAY_TICKS) {
				delayTicksMine = 0;

				if (valuableIndex >= valuablesInLayer.size()) {
					delayTicksScan = 0;
					currentState = STATE_SCANNING;
					updateMetadata(BlockMiningLaser.ICON_SCANNINGPOWERED);
					// rescan same layer
					scanLayer();
					if (valuablesInLayer.size() <= 0) {
						currentLayer--;
					}
					return;
				}
				
				// consume power
				enoughPower = consumeEnergyPacketFromBooster(isOnEarth ? WarpDriveConfig.ML_EU_PER_BLOCK_EARTH : WarpDriveConfig.ML_EU_PER_BLOCK_SPACE, false); 
				if (!enoughPower) {
					updateMetadata(BlockMiningLaser.ICON_MININGLOWPOWER);
					return;
				} else {
					updateMetadata(BlockMiningLaser.ICON_MININGPOWERED);
				}
				
				//System.out.println("[ML] Mining: " + (valuableIndex + 1) + "/" + valuablesInLayer.size());
				Vector3 valuable = valuablesInLayer.get(valuableIndex);
				valuableIndex++;
				// Mine valuable ore
				int blockID = worldObj.getBlockId(valuable.intX(), valuable.intY(), valuable.intZ());
				// Skip if block is too hard or its empty block (check again in case it changed)
				if (!canDig(blockID, valuable.intX(), valuable.intY(), valuable.intZ())) {
					delayTicksMine = Math.round(WarpDriveConfig.ML_MINE_DELAY_TICKS * 0.8F);
				}
				int age = Math.max(10, Math.round((4 + worldObj.rand.nextFloat()) * WarpDriveConfig.ML_MINE_DELAY_TICKS));
				WarpDrive.sendLaserPacket(worldObj, minerVector, new Vector3(valuable.intX(), valuable.intY(), valuable.intZ()).translate(0.5D), 1.0F, 1.0F, 0.0F, age, 0, 50);
				worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
				harvestBlock(valuable);
			}
		}
	}

	private void updateMetadata(int metadata) {
		int blockId = worldObj.getBlockId(xCoord, yCoord, zCoord);
		if (blockId == WarpDriveConfig.miningLaserID) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 2);
		}
	}

	private void stop() {
		// WarpDrive.debugPrint("" + this + " Stop requested");
		currentState = STATE_IDLE;
		updateMetadata(BlockMiningLaser.ICON_IDLE);
	}
	
	private boolean canDig(int blockID, int x, int y, int z) {
		// ignore air
		if (WarpDriveConfig.isAirBlock(worldObj, blockID, x, y, z)) {
			return false;
		}
		// check blacklist
		if (blockID == Block.bedrock.blockID) {
			return false;
		}
		if (WarpDriveConfig.forceFieldBlocks.contains(blockID)) {
			stop();
			return false;
		}
		// check whitelist
		if (WarpDriveConfig.MinerOres.contains(blockID)) {
			return true;
		}
		// check default
		if ( (Block.blocksList[blockID] != null) && (Block.blocksList[blockID].blockResistance <= Block.obsidian.blockResistance) ) {
			return true;
		}
		// WarpDrive.debugPrint("" + this + " Rejecting " + blockID + " at (" + x + ", " + y + ", " + z + ")");
		return false;
	}

	private void harvestBlock(Vector3 valuable) {
		int blockID = worldObj.getBlockId(valuable.intX(), valuable.intY(), valuable.intZ());
		int blockMeta = worldObj.getBlockMetadata(valuable.intX(), valuable.intY(), valuable.intZ());
		if (Block.blocksList[blockID] != null && (Block.blocksList[blockID] instanceof BlockFluid)) {
			// Evaporate fluid
			worldObj.playSoundEffect(valuable.intX() + 0.5D, valuable.intY() + 0.5D, valuable.intZ() + 0.5D, "random.fizz", 0.5F, 2.6F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.8F);
		} else {
			List<ItemStack> stacks = getItemStackFromBlock(valuable.intX(), valuable.intY(), valuable.intZ(), blockID, blockMeta);
			if (stacks != null) {
				boolean overflow = false;
				for (ItemStack stack : stacks) {
					if (grid != null && AENetworkReady) {
						putInGrid(stack);
					} else {
						if (!putInChest(findChest(), stack)) {
							WarpDrive.debugPrint("" + this + " Overflow detected");
							overflow = true;
						}
					}
				}
				if (overflow) {
					stop();
				}
			}
			// standard harvest block effect
			worldObj.playAuxSFXAtEntity(null, 2001, valuable.intX(), valuable.intY(), valuable.intZ(), blockID + (blockMeta << 12));
		}
		worldObj.setBlockToAir(valuable.intX(), valuable.intY(), valuable.intZ());
	}

	private IInventory findChest() {
		TileEntity result = null;
		result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);
		if (result != null && result instanceof IInventory) {
			return (IInventory) result;
		}

		result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);
		if (result != null && result instanceof IInventory) {
			return (IInventory) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);
		if (result != null && result instanceof IInventory) {
			return (IInventory) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);
		if (result != null && result instanceof IInventory) {
			return (IInventory) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
		if (result != null && result instanceof IInventory) {
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
				IAEItemStack entryToAEIS = Util.createItemStack(new ItemStack(WarpDriveConfig.AEExtraFDI, consume, FluidRegistry.getFluidID("deuterium")));
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

	public boolean putInChest(IInventory inventory, ItemStack itemStackSource) {
		if (itemStackSource == null) {
			stop();
			return false;
		}

		int qtyLeft = itemStackSource.stackSize;
		int transfer;

		if (inventory != null) {
			// fill existing stacks first
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				if (!inventory.isItemValidForSlot(i, itemStackSource)) {
					continue;
				}
	
				ItemStack itemStack = inventory.getStackInSlot(i);
				if (itemStack == null || !itemStack.isItemEqual(itemStackSource)) {
					continue;
				}
	
				transfer = Math.min(qtyLeft, itemStack.getMaxStackSize() - itemStack.stackSize);
				itemStack.stackSize += transfer;
				qtyLeft -= transfer;
				if (qtyLeft <= 0) {
					return true;
				}
			}
	
			// put remaining in empty slot
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				if (!inventory.isItemValidForSlot(i, itemStackSource)) {
					continue;
				}
	
				ItemStack itemStack = inventory.getStackInSlot(i);
				if (itemStack != null) {
					continue;
				}
	
				transfer = Math.min(qtyLeft, itemStackSource.getMaxStackSize());
				ItemStack dest = copyWithSize(itemStackSource, transfer);
				inventory.setInventorySlotContents(i, dest);
				qtyLeft -= transfer;
	
				if (qtyLeft <= 0) {
					return true;
				}
			}
		}

		while (qtyLeft > 0) {
			transfer = Math.min(qtyLeft, itemStackSource.getMaxStackSize());
			ItemStack dropItemStack = copyWithSize(itemStackSource, transfer);
			EntityItem itemEnt = new EntityItem(worldObj, xCoord + 0.5D, yCoord + 1.0D, zCoord + 0.5D, dropItemStack);
			worldObj.spawnEntityInWorld(itemEnt);
			qtyLeft -= transfer;
		}
		
		return false;
	}

	public static ItemStack copyWithSize(ItemStack itemStack, int newSize)
	{
		ItemStack ret = itemStack.copy();
		ret.stackSize = newSize;
		return ret;
	}

	private void scanLayer() {
		//System.out.println("Scanning layer");
		valuablesInLayer.clear();
		valuableIndex = 0;
		int radius, x, z, blockID;
		int xmax, zmax;
		int xmin, zmin;

		// Search for valuable blocks
		x = xCoord;
		z = zCoord;
		blockID = worldObj.getBlockId(x, currentLayer, z);
		if (canDig(blockID, x, currentLayer, z)) {
			if (isQuarry || WarpDriveConfig.MinerOres.contains(blockID)) {// Quarry collects all blocks or only collect valuables blocks
				valuablesInLayer.add(new Vector3(x, currentLayer, z));
			}
		}
		for (radius = 1; radius <= WarpDriveConfig.ML_MAX_RADIUS; radius++) {
			xmax = xCoord + radius;
			xmin = xCoord - radius;
			zmax = zCoord + radius;
			zmin = zCoord - radius;
			x = xCoord;
			z = zmin;
			for (; x <= xmax; x++) {
				blockID = worldObj.getBlockId(x, currentLayer, z);
				if (canDig(blockID, x, currentLayer, z)) {
					if (isQuarry || WarpDriveConfig.MinerOres.contains(blockID)) {// Quarry collects all blocks or only collect valuables blocks
						valuablesInLayer.add(new Vector3(x, currentLayer, z));
					}
				}
			}
			x = xmax;
			z++;
			for (; z <= zmax; z++) {
				blockID = worldObj.getBlockId(x, currentLayer, z);
				if (canDig(blockID, x, currentLayer, z)) {
					if (isQuarry || WarpDriveConfig.MinerOres.contains(blockID)) {// Quarry collects all blocks or only collect valuables blocks
						valuablesInLayer.add(new Vector3(x, currentLayer, z));
					}
				}
			}
			x--;
			z = zmax;
			for (; x >= xmin; x--) {
				blockID = worldObj.getBlockId(x, currentLayer, z);
				if (canDig(blockID, x, currentLayer, z)) {
					if (isQuarry || WarpDriveConfig.MinerOres.contains(blockID)) {// Quarry collects all blocks or only collect valuables blocks
						valuablesInLayer.add(new Vector3(x, currentLayer, z));
					}
				}
			}
			x = xmin;
			z--;
			for (; z > zmin; z--) {
				blockID = worldObj.getBlockId(x, currentLayer, z);
				if (canDig(blockID, x, currentLayer, z)) {
					if (isQuarry || WarpDriveConfig.MinerOres.contains(blockID)) {// Quarry collects all blocks or only collect valuables blocks
						valuablesInLayer.add(new Vector3(x, currentLayer, z));
					}
				}
			}
			x = xmin;
			z = zmin;
			for (; x < xCoord; x++) {
				blockID = worldObj.getBlockId(x, currentLayer, z);
				if (canDig(blockID, x, currentLayer, z)) {
					if (isQuarry || WarpDriveConfig.MinerOres.contains(blockID)) {// Quarry collects all blocks or only collect valuables blocks
						valuablesInLayer.add(new Vector3(x, currentLayer, z));
					}
				}
			}
		}

		//System.out.println("" + this + " Found " + valuablesInLayer.size() + " valuables");
	}

	private int getEnergyLevel() {
		TileEntityParticleBooster booster = findFirstBooster();
		if (booster != null) {
			return booster.getEnergyStored();
		} else {
			return 0;
		}
	}

	private boolean consumeEnergyPacketFromBooster(int amount, boolean simulate) {
		TileEntityParticleBooster booster = findFirstBooster();
		if (booster != null) {
			return booster.consumeEnergy(amount, simulate);
		} else {
			return false;
		}
	}

	private TileEntityParticleBooster findFirstBooster() {
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

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		currentState = tag.getInteger("currentState");
		isQuarry = tag.getBoolean("isQuarry");
		currentLayer = tag.getInteger("currentLayer");
		useDeiterium = tag.getBoolean("useDeiterium");
		if (currentState == STATE_MINING) {
			scanLayer();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("currentState", currentState);
		tag.setBoolean("isQuarry", isQuarry);
		tag.setInteger("currentLayer", currentLayer);
		tag.setBoolean("useDeiterium", useDeiterium);
	}
	
	// ComputerCraft
	// IPeripheral methods implementation
	@Override
	public String getType() {
		return "mininglaser";
	}

	@Override
	public String[] getMethodNames() {
		return methodsArray;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
		switch (method) {
			case 0: // Mine()
				if (isMining()) {
					return new Boolean[] { false };
				}
				
				isQuarry = false;
				delayTicksWarmup = 0;
				currentState = STATE_WARMUP;
				currentLayer = yCoord - layerOffset - 1;
				useDeiterium = (arguments.length == 1 && FluidRegistry.isFluidRegistered("deuterium"));
				return new Boolean[] { true };

			case 1: // stop()
				stop();
				break;

			case 2: // isMining()
				return new Boolean[] { isMining() };
				
			case 3: // Quarry()
				if (isMining()) {
					return new Boolean[] { false };
				}

				isQuarry = true;
				delayTicksScan = 0;
				currentState = STATE_WARMUP;
				currentLayer = yCoord - layerOffset - 1;
				useDeiterium = (arguments.length == 1 && FluidRegistry.isFluidRegistered("deuterium"));
				return new Boolean[] { true };

			case 4: // State is: state, energy, currentLayer, valuablesMined, valuablesInLayer = getMinerState()
				int energy = getEnergyLevel();
				String status = getStatus();
				Integer retValuablesInLayer, retValuablesMined;
				if (isMining()) {
					retValuablesInLayer = valuablesInLayer.size();
					retValuablesMined = valuableIndex;
					
					return new Object[] {status, energy, currentLayer, retValuablesMined, retValuablesInLayer};
				}
				return new Object[] {status, energy, currentLayer, 0, 0};

			case 5: // Offset
				if (arguments.length == 1) {
	                try {
	                	layerOffset = Math.min(256,  Math.abs(((Double)arguments[0]).intValue()));
	                } catch(Exception e) {
	                	return new Integer[] { layerOffset };
	                }
				}
				return new Integer[] { layerOffset };
		}
		return null;
	}
	
	public String getStatus() {
		int energy = 0;
		energy = getEnergyLevel();
		String state = "IDLE (not mining)";
		if (currentState == STATE_IDLE) {
			state = "IDLE (not mining)";
		} else if (currentState == STATE_WARMUP) {
			state = "Warming up...";
		} else if (currentState == STATE_SCANNING) {
			if (isQuarry) {
				state = "Scanning all";						
			} else {
				state = "Scanning ores";
			}
		} else if (currentState == STATE_MINING) {
			if (isQuarry) {
				state = "Mining all";						
			} else {
				state = "Mining ores";
			}
			if (useDeiterium) {
				state = state + " using Deuterium";
			}
		}
		if (energy <= 0) {
			state = state + " - Out of energy";
		} else if (((currentState == STATE_SCANNING) || (currentState == STATE_MINING)) && !enoughPower) {
			state = state + " - Not enough power";
		}
		return state;
	}

	@Override
	public void attach(IComputerAccess computer) {
        computer.mount("/mininglaser", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/mininglaser"));
        computer.mount("/mine", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/mininglaser/mine"));
        computer.mount("/stop", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/mininglaser/stop"));
	}

	@Override
	public void detach(IComputerAccess computer) {
	}

	@Override
	public boolean equals(IPeripheral other) {
		return other == this;
	}

	// Applied Energistics	@Override

	@Override
	public float getPowerDrainPerTick() {
		return 1;
	}

	@Override
	public void validate() {
		super.validate();
		MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent(this, worldObj, getLocation()));
	}

	@Override
	public void invalidate() {
		super.invalidate();
		MinecraftForge.EVENT_BUS.post(new GridTileUnloadEvent(this, worldObj, getLocation()));
	}

	@Override
	public WorldCoord getLocation() {
		return new WorldCoord(xCoord, yCoord, zCoord);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void setPowerStatus(boolean hasPower) {
		powerStatus = hasPower;
	}

	@Override
	public boolean isPowered() {
		return powerStatus;
	}

	@Override
	public IGridInterface getGrid() {
		return grid;
	}

	@Override
	public void setGrid(IGridInterface parGrid) {
		grid = parGrid;
	}

	@Override
	public World getWorld() {
		return worldObj;
	}

	@Override
	public boolean coveredConnections() {
		return true;
	}

	@Override
	public void setNetworkReady( boolean isReady ) {
		AENetworkReady = isReady;
	}

	@Override
	public boolean isMachineActive() {
		return isMining();
	}

	@Override
	public String toString() {
        return String.format("%s @ \'%s\' %d, %d, %d", new Object[] {
                getClass().getSimpleName(),
                worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
                Integer.valueOf(xCoord), Integer.valueOf(yCoord), Integer.valueOf(zCoord)});
	}
}
