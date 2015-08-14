package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.block.BlockAbstractContainer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockEnanReactorCore extends BlockAbstractContainer {
	IIcon[] iconBuffer = new IIcon[17];

	public BlockEnanReactorCore() {
		super();
		setBlockName("warpdrive.energy.EnanReactorCore");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityEnanReactorCore();
	}

	@Override
	public void breakBlock(World w, int x, int y, int z, Block oid, int om) {
		super.breakBlock(w, x, y, z, oid, om);

		int[] xo = { -2, 2, 0, 0 };
		int[] zo = { 0, 0, -2, 2 };
		for (int i = 0; i < 4; i++) {
			TileEntity te = w.getTileEntity(x + xo[i], y, z + zo[i]);
			if (te instanceof TileEntityEnanReactorLaser) {
				((TileEntityEnanReactorLaser) te).unlink();
			}
		}
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		if (side == 0 || side == 1) {
			return iconBuffer[16];
		}
		if (meta >= 0 && meta < 16) {
			return iconBuffer[meta];
		}
		return iconBuffer[0];
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer[16] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreTopBottom");
		iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide00");
		iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide01");
		iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide02");
		iconBuffer[3] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide03");
		iconBuffer[4] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide10");
		iconBuffer[5] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide11");
		iconBuffer[6] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide12");
		iconBuffer[7] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide13");
		iconBuffer[8] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide20");
		iconBuffer[9] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide21");
		iconBuffer[10] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide22");
		iconBuffer[11] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide23");
		iconBuffer[12] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide30");
		iconBuffer[13] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide31");
		iconBuffer[14] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide32");
		iconBuffer[15] = par1IconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide33");
	}
}