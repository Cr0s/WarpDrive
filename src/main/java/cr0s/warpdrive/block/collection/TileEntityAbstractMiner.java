package cr0s.warpdrive.block.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;
import cr0s.warpdrive.block.TileEntityAbstractLaser;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.network.PacketHandler;

public abstract class TileEntityAbstractMiner extends TileEntityAbstractLaser
{
	//FOR STORAGE
	private boolean silkTouch = false;
	private int fortuneLevel = 0;

	private Vector3 laserOutput;

	abstract boolean	canSilkTouch();
	abstract int		minFortune();
	abstract int		maxFortune();
	ForgeDirection		laserOutputSide = ForgeDirection.UP;

	abstract float		getColorR();
	abstract float		getColorG();
	abstract float		getColorB();

	public TileEntityAbstractMiner() {
		super();
	}
	
	@Override
	public void validate() {
		super.validate();
		laserOutput = new Vector3(this).translate(0.5D).translate(laserOutputSide, 0.5D);
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

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			result = worldObj.getTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);
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

	protected int fortune(int level)
	{
		try
		{
			fortuneLevel = clamp(minFortune(), maxFortune(), level);
		}
		catch(NumberFormatException e)
		{
			fortuneLevel = minFortune();
		}
		return fortune();
	}

	//DATA RET

	protected int calculateLayerCost()
	{
		return isOnEarth() ? WarpDriveConfig.MINING_LASER_PLANET_ENERGY_PER_LAYER : WarpDriveConfig.MINING_LASER_SPACE_ENERGY_PER_LAYER;
	}

	protected int calculateBlockCost()
	{
		return calculateBlockCost(Blocks.air);
	}

	protected int calculateBlockCost(Block block)
	{
		int enPerBlock = isOnEarth() ? WarpDriveConfig.MINING_LASER_PLANET_ENERGY_PER_BLOCK : WarpDriveConfig.MINING_LASER_SPACE_ENERGY_PER_BLOCK;
		if (silkTouch(block))
			return (int) Math.round(enPerBlock * WarpDriveConfig.MINING_LASER_SILKTOUCH_ENERGY_FACTOR);
		return (int) Math.round(enPerBlock * (Math.pow(WarpDriveConfig.MINING_LASER_FORTUNE_ENERGY_FACTOR, fortune())));
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

	private boolean canDig(Block block, int x, int y, int z) {// not used, should be abstract
		return false;
	}

	//MINING FUNCTIONS

	protected void laserBlock(VectorI valuable)
	{
		float r = getColorR();
		float g = getColorG();
		float b = getColorB();
		PacketHandler.sendBeamPacket(worldObj, laserOutput, valuable.getBlockCenter(), r, g, b, 2 * WarpDriveConfig.MINING_LASER_MINE_DELAY_TICKS, 0, 50);
		//worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
	}

	private void mineBlock(VectorI valuable, Block block, int blockMeta)
	{
		laserBlock(valuable);
		worldObj.playAuxSFXAtEntity(null, 2001, valuable.x, valuable.y, valuable.z, (blockMeta << 12));
		worldObj.setBlockToAir(valuable.x, valuable.y, valuable.z);
	}

	protected boolean harvestBlock(VectorI valuable)
	{
		Block block = worldObj.getBlock(valuable.x, valuable.y, valuable.z);
		int blockMeta = worldObj.getBlockMetadata(valuable.x, valuable.y, valuable.z);
		if (!block.isAssociatedBlock(Blocks.water) && !block.isAssociatedBlock(Blocks.lava))
		{
			boolean didPlace = true;
			List<ItemStack> stacks = getItemStackFromBlock(valuable.x, valuable.y, valuable.z, block, blockMeta);
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
		else if (block.isAssociatedBlock(Blocks.water)) {
			// Evaporate water
			worldObj.playSoundEffect(valuable.x + 0.5D, valuable.y + 0.5D, valuable.z + 0.5D, "random.fizz", 0.5F, 2.6F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.8F);
		}
		worldObj.setBlockToAir(valuable.x, valuable.y, valuable.z);
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
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tag.setBoolean("silkTouch", silkTouch);
		tag.setInteger("fortuneLevel", fortuneLevel);
	}
}
