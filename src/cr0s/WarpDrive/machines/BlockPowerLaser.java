package cr0s.WarpDrive.machines;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.WarpDrive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockPowerLaser extends BlockContainer {
	static Icon[] iconBuffer = new Icon[16];
	public BlockPowerLaser(int id) {
        super(id, Material.iron);
        setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("warpdrive.power.Laser");
		setResistance(100.0F);
    }

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityPowerLaser();
	}
	
	@Override
	public void onBlockAdded(World w,int x,int y, int z) {
		TileEntity te = w.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityPowerLaser) {
			((TileEntityPowerLaser)te).scanForReactor();
		}
	}
	 	
	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, int b) {
		TileEntity te = w.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityPowerLaser) {
	 		((TileEntityPowerLaser)te).updateNeighbours();
		}
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
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		if (side == 0 || side == 1) {
	 		return iconBuffer[0];
		}
		 
		if(isActive(side,meta)) {
	 		return iconBuffer[2];
		}
		 
		return iconBuffer[1];
	}

	@Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:powerLaserTopBottom");
        iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:powerLaserSides");
        iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:powerLaserActive");
	}
}