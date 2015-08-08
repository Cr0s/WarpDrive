package cr0s.warpdrive.machines;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.IFluidBlock;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.network.PacketHandler;

public abstract class TileEntityAbstractMiner extends TileEntityAbstractLaser
{
	//FOR STORAGE
	private boolean silkTouch = false;
	private int fortuneLevel = 0;

	private TileEntityParticleBooster booster = null;
	private Vector3 minerVector;

	abstract boolean	canSilkTouch();
	abstract int		minFortune();
	abstract int		maxFortune();
	abstract double		laserBelow();

	abstract float		getColorR();
	abstract float		getColorG();
	abstract float		getColorB();

	public TileEntityAbstractMiner()
	{
		super();
		fixMinerVector();
	}

	private void fixMinerVector()
	{
		if(minerVector == null)
			minerVector = new Vector3(xCoord,yCoord-laserBelow(),zCoord);
		minerVector.x = xCoord;
		minerVector.y = yCoord - (laserBelow());
		minerVector.z = zCoord;
		minerVector.translate(0.5);
	}

	private List<ItemStack> getItemStackFromBlock(int i, int j, int k, Block block, int blockMeta)
	{
		if (block == null)
			return null;

		ArrayList<ItemStack> t = new ArrayList<ItemStack>();
		if (silkTouch(block))
		{
			if (block.canSilkHarvest(worldObj, null, i, j, k, blockMeta))
			{
				t.add(new ItemStack(block, 1, blockMeta));
				return t;
			}
		}
		t.add(new ItemStack(block.getItemDropped(blockMeta, new Random(), fortuneLevel), block.damageDropped(blockMeta), block.quantityDropped(blockMeta, fortuneLevel, new Random())));
		return t;
	}

	protected boolean isOnEarth()
	{
		return worldObj.provider.dimensionId == 0;
	}

	private IInventory findChest() {
		TileEntity result = null;

		for(int i = 0; i < 6; i++) {
			Vector3 sideOffset = adjacentSideOffsets[i];
			result = worldObj.getTileEntity(xCoord + sideOffset.intX(), yCoord + sideOffset.intY(), zCoord + sideOffset.intZ());
			if (result != null && !(result instanceof TileEntityAbstractMiner) && (result instanceof IInventory)) {
				return (IInventory) result;
			}
		}
		return null;
	}

	//GETTERSETTERS

	protected int fortune()
	{
		return fortuneLevel;
	}

	protected boolean silkTouch()
	{
		return silkTouch;
	}

	protected boolean silkTouch(Block block)
	{
		return silkTouch();
	}

	protected boolean silkTouch(boolean b)
	{
		silkTouch = canSilkTouch() && b;
		return silkTouch();
	}

	protected boolean silkTouch(Object o)
	{
		return silkTouch(toBool(o));
	}

	protected int fortune(int f)
	{
		try
		{
			fortuneLevel = clamp(f,minFortune(),maxFortune());
		}
		catch(NumberFormatException e)
		{
			fortuneLevel = minFortune();
		}
		return fortune();
	}

	protected TileEntityParticleBooster booster()
	{
		if(booster == null)
			findFirstBooster();
		return booster;
	}

	protected int energy() {
		TileEntityParticleBooster te = booster();
		if (te != null) {
			return te.getEnergyStored();
		}
		return 0;
	}

	//DATA RET

	protected int calculateLayerCost()
	{
		return isOnEarth() ? WarpDriveConfig.ML_EU_PER_LAYER_EARTH : WarpDriveConfig.ML_EU_PER_LAYER_SPACE;
	}

	protected int calculateBlockCost()
	{
		return calculateBlockCost(Blocks.air);
	}

	protected int calculateBlockCost(Block block)
	{
		int enPerBlock = isOnEarth() ? WarpDriveConfig.ML_EU_PER_BLOCK_EARTH : WarpDriveConfig.ML_EU_PER_BLOCK_SPACE;
		if (silkTouch(block))
			return (int) Math.round(enPerBlock * WarpDriveConfig.ML_EU_MUL_SILKTOUCH);
		return (int) Math.round(enPerBlock * (Math.pow(WarpDriveConfig.ML_EU_MUL_FORTUNE, fortune())));
	}

	protected boolean isRoomForHarvest()
	{
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

	private boolean canDig(Block block, int x, int y, int z) {// not used
		// ignore air & fluids
		if (block == null || (worldObj.isAirBlock(x, y, z) || (block instanceof IFluidBlock))) {
			return false;
		}
		// check blacklist
		if (block.isAssociatedBlock(Blocks.bedrock)) {
			return false;
		}
		if (WarpDriveConfig.forceFieldBlocks.contains(block)) {
			//			isMining = false;
			return false;
		}
		// check whitelist
		// WarpDriveConfig.MinerOres.contains(blockID) then true ?
		else if (block.isAssociatedBlock(WarpDrive.iridiumBlock)) {
			return true;
		}
		// check default
		else if (block.getExplosionResistance(null) <= Blocks.obsidian.getExplosionResistance(null)) {
			return true;
		}
		return false;
	}

	//MINING FUNCTIONS

	protected void laserBlock(Vector3 valuable)
	{
		fixMinerVector();
		float r = getColorR();
		float g = getColorG();
		float b = getColorB();
		PacketHandler.sendBeamPacket(worldObj, minerVector, valuable.clone().translate(0.5D), r, g, b, 2 * WarpDriveConfig.ML_MINE_DELAY_TICKS, 0, 50);
		//worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
	}

	private void mineBlock(Vector3 valuable, Block block, int blockMeta)
	{
		laserBlock(valuable);
		worldObj.playAuxSFXAtEntity(null, 2001, valuable.intX(), valuable.intY(), valuable.intZ(), (blockMeta << 12));
		worldObj.setBlockToAir(valuable.intX(), valuable.intY(), valuable.intZ());
	}

	protected boolean harvestBlock(Vector3 valuable)
	{
		Block block = worldObj.getBlock(valuable.intX(), valuable.intY(), valuable.intZ());
		int blockMeta = worldObj.getBlockMetadata(valuable.intX(), valuable.intY(), valuable.intZ());
		if (!block.isAssociatedBlock(Blocks.water) && !block.isAssociatedBlock(Blocks.lava))
		{
			boolean didPlace = true;
			List<ItemStack> stacks = getItemStackFromBlock(valuable.intX(), valuable.intY(), valuable.intZ(), block, blockMeta);
			if (stacks != null)
			{
				for (ItemStack stack : stacks)
				{
					didPlace = didPlace && dumpToInv(stack) == stack.stackSize;
				}
			}
			mineBlock(valuable, block, blockMeta);
			return didPlace;
		}
		else if (block.isAssociatedBlock(Blocks.water))
			// Evaporate water
			worldObj.playSoundEffect(valuable.intX() + 0.5D, valuable.intY() + 0.5D, valuable.intZ() + 0.5D, "random.fizz", 0.5F, 2.6F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.8F);
		worldObj.setBlockToAir(valuable.intX(), valuable.intY(), valuable.intZ());
		return true;
	}

	protected int dumpToInv(ItemStack item)
	{
		return putInChest(findChest(), item);
	}

	private static int putInChest(IInventory inventory, ItemStack itemStackSource)
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

	protected boolean consumeEnergyFromBooster(int requiredEnergy, boolean simulate)
	{
		TileEntityParticleBooster te = booster();
		if (te != null) {
			return te.consumeEnergy(requiredEnergy, simulate);
		}
		return false;
	}

	private TileEntityParticleBooster findFirstBooster()
	{
		TileEntity result;
		int[] xPos = {1,-1,0,0,0,0};
		int[] yPos = {0,0,-1,1,0,0};
		int[] zPos = {0,0,0,0,-1,1};

		for(int i=0;i<6;i++)
		{
			result = worldObj.getTileEntity(xCoord + xPos[i], yCoord + yPos[i], zCoord + zPos[i]);

			if (result != null && result instanceof TileEntityParticleBooster)
			{
				booster = (TileEntityParticleBooster) result;
				return (TileEntityParticleBooster) result;
			}
		}
		booster = null;
		return null;
	}

	private static ItemStack copyWithSize(ItemStack itemStack, int newSize)
	{
		ItemStack ret = itemStack.copy();
		ret.stackSize = newSize;
		return ret;
	}

	// NBT DATA
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		silkTouch = tag.getBoolean("silkTouch");
		fortuneLevel = tag.getInteger("fortuneLevel");

		minerVector.x = xCoord;
		minerVector.y = yCoord - (laserBelow());
		minerVector.z = zCoord;
		minerVector = minerVector.translate(0.5);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tag.setBoolean("silkTouch", silkTouch);
		tag.setInteger("fortuneLevel", fortuneLevel);
	}
}
