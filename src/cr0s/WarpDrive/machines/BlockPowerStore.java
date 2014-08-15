package cr0s.WarpDrive.machines;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cr0s.WarpDrive.WarpBlockContainer;

public class BlockPowerStore extends WarpBlockContainer {
	private Icon iconBuffer;
	
	public BlockPowerStore(int par1) {
		super(par1);
		setUnlocalizedName("warpdrive.power.Store");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityPowerStore();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		return iconBuffer;
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        iconBuffer = par1IconRegister.registerIcon("warpdrive:powerStore");
    }
}