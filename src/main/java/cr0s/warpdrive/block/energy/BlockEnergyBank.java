package cr0s.warpdrive.block.energy;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;

public class BlockEnergyBank extends BlockAbstractContainer {
	private IIcon iconBuffer;

	public BlockEnergyBank() {
		setBlockName("warpdrive.energy.EnergyBank");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityEnergyBank();
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return iconBuffer;
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = par1IconRegister.registerIcon("warpdrive:energy/energyBank");
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return false;
		}

		TileEntityAbstractEnergy te = (TileEntityAbstractEnergy) par1World.getTileEntity(par2, par3, par4);
		if (te != null && (par5EntityPlayer.getHeldItem() == null)) {
			WarpDrive.addChatMessage(par5EntityPlayer, te.getStatus());
			return true;
		}

		return false;
	}
}