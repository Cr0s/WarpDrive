package cr0s.warpdrive.block.movement;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.WarpDrive;

public class BlockShipCore extends BlockContainer {
	private IIcon[] iconBuffer;

	private final int ICON_SIDE_INACTIVE = 0, ICON_BOTTOM = 1, ICON_TOP = 2, ICON_SIDE_ACTIVATED = 3, ICON_SIDE_HEATED = 4;

	public BlockShipCore(int texture, Material material) {
		super(material);
		setHardness(0.5F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.warpdriveTab);
		setBlockName("warpdrive.movement.ShipCore");
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[5];
		iconBuffer[ICON_SIDE_INACTIVE] = par1IconRegister.registerIcon("warpdrive:movement/shipCoreSideInactive");
		iconBuffer[ICON_BOTTOM] = par1IconRegister.registerIcon("warpdrive:movement/shipCoreBottom");
		iconBuffer[ICON_TOP] = par1IconRegister.registerIcon("warpdrive:movement/shipCoreTop");
		iconBuffer[ICON_SIDE_ACTIVATED] = par1IconRegister.registerIcon("warpdrive:movement/shipCoreSideActive");
		iconBuffer[ICON_SIDE_HEATED] = par1IconRegister.registerIcon("warpdrive:movement/shipCoreSideHeated");
	}

	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		} else if (side == 1) {
			return iconBuffer[ICON_TOP];
		}

		if (metadata == 0) { // Inactive state
			return iconBuffer[ICON_SIDE_INACTIVE];
		} else if (metadata == 1) { // Activated state
			return iconBuffer[ICON_SIDE_ACTIVATED];
		} else if (metadata == 2) { // Heated state
			return iconBuffer[ICON_SIDE_HEATED];
		}

		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int i) {
		return new TileEntityShipCore();
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}

	/**
	 * Returns the ID of the items to drop on destruction.
	 */
	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3) {
		return Item.getItemFromBlock(this);
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return false;
		}

		if (par5EntityPlayer.getHeldItem() == null) {
			TileEntity te = par1World.getTileEntity(par2, par3, par4);
			if (te != null && te instanceof TileEntityShipCore) {
				WarpDrive.addChatMessage(par5EntityPlayer, ((TileEntityShipCore)te).getStatus());
				return true;
			}
		}

		return false;
	}

	@Override
	public void breakBlock(World par1World, int par2, int par3, int par4, Block par5, int par6) {
		TileEntity te = par1World.getTileEntity(par2, par3, par4);
		if (te != null && te instanceof TileEntityShipCore) {
			WarpDrive.warpCores.removeFromRegistry((TileEntityShipCore)te);
		}

		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}
}