package cr0s.warpdrive.block.energy;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;

public class BlockIC2reactorLaserMonitor extends BlockContainer {
	public BlockIC2reactorLaserMonitor(int texture, Material material) {
		super(material);
		setHardness(0.5F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		this.setBlockName("warpdrive.energy.IC2ReactorLaserMonitor");
	}

	public BlockIC2reactorLaserMonitor(Material material) {
		this(0, material);
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		// Solid textures
		blockIcon = par1IconRegister.registerIcon("warpdrive:energy/IC2reactorLaserMonitor");
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int i) {
		return new TileEntityIC2reactorLaserMonitor();
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			TileEntity tileEntity = par1World.getTileEntity(x, y, z);
			if (tileEntity != null && tileEntity instanceof TileEntityIC2reactorLaserMonitor) {
				WarpDrive.addChatMessage(entityPlayer, ((TileEntityIC2reactorLaserMonitor) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}
