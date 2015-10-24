package cr0s.warpdrive.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.WarpDrive;

public class BlockLaserMedium extends BlockContainer {
	private IIcon[] iconBuffer;
	
	public BlockLaserMedium() {
		super(Material.rock);
		setHardness(0.5F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.machines.LaserMedium");
	}
	
	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[9];
		iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:laserMediumSide0");
		iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:laserMediumSide1");
		iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:laserMediumSide2");
		iconBuffer[3] = par1IconRegister.registerIcon("warpdrive:laserMediumSide3");
		iconBuffer[4] = par1IconRegister.registerIcon("warpdrive:laserMediumSide4");
		iconBuffer[5] = par1IconRegister.registerIcon("warpdrive:laserMediumSide5");
		iconBuffer[6] = par1IconRegister.registerIcon("warpdrive:laserMediumSide6");
		iconBuffer[7] = par1IconRegister.registerIcon("warpdrive:laserMediumSide7");
		iconBuffer[8] = par1IconRegister.registerIcon("warpdrive:laserMediumTopBottom");
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0 || side == 1) {
			return iconBuffer[8];
		}
		
		int index = metadata >= iconBuffer.length ? iconBuffer.length - 1 : metadata;

		return iconBuffer[index];
	}
	
	@Override
	public TileEntity createNewTileEntity(World var1, int i) {
		return new TileEntityLaserMedium();
	}
	
	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity != null && tileEntity instanceof TileEntityAbstractEnergy) {
				WarpDrive.addChatMessage(entityPlayer, ((TileEntityAbstractEnergy) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}
