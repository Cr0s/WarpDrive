package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.block.BlockAbstractContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockEnanReactorLaser extends BlockAbstractContainer {
	static IIcon[] iconBuffer = new IIcon[16];

	public BlockEnanReactorLaser() {
		setResistance(100.0F);
		setBlockName("warpdrive.energy.EnanReactorLaser");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityEnanReactorLaser();
	}

	private static boolean isActive(int side, int meta) {
		if (side == 3 && meta == 1) {
			return true;
		}

		if (side == 2 && meta == 2) {
			return true;
		}

		if (side == 4 && meta == 4) {
			return true;
		}

		if (side == 5 && meta == 3) {
			return true;
		}
		return false;
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		if (side == 0 || side == 1) {
			return iconBuffer[0];
		}

		if (isActive(side, meta)) {
			return iconBuffer[2];
		}

		return iconBuffer[1];
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorLaserTopBottom");
		iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorLaserSides");
		iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorLaserActive");
	}
}