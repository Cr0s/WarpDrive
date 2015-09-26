package cr0s.warpdrive.block.detection;

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
import cr0s.warpdrive.data.CameraRegistryItem;

public class BlockCamera extends BlockContainer {
	private IIcon[] iconBuffer;
	
	private final int ICON_SIDE = 0;
	
	public BlockCamera() {
		super(Material.rock);
		setHardness(0.5F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		this.setBlockName("warpdrive.detection.Camera");
	}
	
	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[1];
		// Solid textures
		iconBuffer[ICON_SIDE] = par1IconRegister.registerIcon("warpdrive:detection/cameraSide");
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		return iconBuffer[ICON_SIDE];
	}
	
	@Override
	public TileEntity createNewTileEntity(World parWorld, int i) {
		return new TileEntityCamera();
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
	
	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3) {
		return Item.getItemFromBlock(this);
	}
	
	@Override
	public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return false;
		}
		
		// Get camera frequency
		TileEntity te = par1World.getTileEntity(x, y, z);
		if (te != null && te instanceof TileEntityCamera && (par5EntityPlayer.getHeldItem() == null)) {
			int frequency = ((TileEntityCamera)te).getVideoChannel();
			
			CameraRegistryItem cam = WarpDrive.instance.cameras.getCameraByFrequency(par1World, frequency);
			if (cam == null) {
				WarpDrive.instance.cameras.printRegistry(par1World);
				WarpDrive.addChatMessage(par5EntityPlayer, getLocalizedName() + " Frequency '" + frequency + "' is invalid!");
			} else {
				WarpDrive.addChatMessage(par5EntityPlayer, getLocalizedName() + " Frequency '" + frequency + "' is valid for camera at " + cam.position.chunkPosX + ", " + cam.position.chunkPosY + ", " + cam.position.chunkPosZ);
			}
			return true;
		}
		
		return false;
	}
}