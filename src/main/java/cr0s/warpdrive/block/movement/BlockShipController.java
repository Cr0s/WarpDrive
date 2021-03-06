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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;

public class BlockShipController extends BlockContainer {
	private IIcon[] iconBuffer;

	private final int ICON_INACTIVE_SIDE = 0, ICON_BOTTOM = 1, ICON_TOP = 2, ICON_SIDE_ACTIVATED = 3;

	public BlockShipController() {
		super(Material.rock);
		setHardness(0.5F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.movement.ShipController");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[10];
		// Solid textures
		iconBuffer[ICON_INACTIVE_SIDE] = par1IconRegister.registerIcon("warpdrive:movement/shipControllerSideInactive");
		iconBuffer[ICON_BOTTOM] = par1IconRegister.registerIcon("warpdrive:movement/shipControllerBottom");
		iconBuffer[ICON_TOP] = par1IconRegister.registerIcon("warpdrive:movement/shipControllerTop");
		// Animated textures
		iconBuffer[ICON_SIDE_ACTIVATED    ] = par1IconRegister.registerIcon("warpdrive:movement/shipControllerSideActive0");
		iconBuffer[ICON_SIDE_ACTIVATED + 1] = par1IconRegister.registerIcon("warpdrive:movement/shipControllerSideActive1");
		iconBuffer[ICON_SIDE_ACTIVATED + 2] = par1IconRegister.registerIcon("warpdrive:movement/shipControllerSideActive2");
		iconBuffer[ICON_SIDE_ACTIVATED + 3] = par1IconRegister.registerIcon("warpdrive:movement/shipControllerSideActive3");
		iconBuffer[ICON_SIDE_ACTIVATED + 4] = par1IconRegister.registerIcon("warpdrive:movement/shipControllerSideActive4");
		iconBuffer[ICON_SIDE_ACTIVATED + 5] = par1IconRegister.registerIcon("warpdrive:movement/shipControllerSideActive5");
		iconBuffer[ICON_SIDE_ACTIVATED + 6] = par1IconRegister.registerIcon("warpdrive:movement/shipControllerSideActive6");
	}

	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		} else if (side == 1) {
			return iconBuffer[ICON_TOP];
		}

		if (metadata == 0) { // Inactive state
			return iconBuffer[ICON_INACTIVE_SIDE];
		} else if (metadata > 0) { // Activated, in metadata stored mode number
			if (ICON_SIDE_ACTIVATED + metadata - 1 < iconBuffer.length) {
				return iconBuffer[ICON_SIDE_ACTIVATED + metadata];
			} else {
				return null;
			}
		}

		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int i) {
		return new TileEntityShipController();
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
			TileEntityShipController controller = (TileEntityShipController)par1World.getTileEntity(par2, par3, par4);
			if (controller != null) {
				controller.attachPlayer(par5EntityPlayer);
				WarpDrive.addChatMessage(par5EntityPlayer, controller.getBlockType().getLocalizedName() + " Attached players: " + controller.getAttachedPlayersList());
				return true;
			}
		}

		return false;
	}
}