package cr0s.warpdrive.block.energy;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
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
}
