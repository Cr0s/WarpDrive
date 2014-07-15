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

public class BlockPowerReactor extends BlockContainer
{
	Icon[] iconBuffer = new Icon[2];
	public BlockPowerReactor(int id)
    {
        super(id, Material.iron);
        setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("warpdrive.power.Reactor");
    }

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityPowerReactor();
	}
	
	@Override
	public void onBlockAdded(World w,int x, int y,int z)
	{
		TileEntity te = w.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityPowerReactor)
			((TileEntityPowerReactor)te).updateNeighbours();
	}
	
	@Override
	public void onNeighborBlockChange(World w,int x,int y,int z,int b)
	{
		TileEntity te = w.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityPowerReactor)
			((TileEntityPowerReactor)te).updateNeighbours();
	}
	
	@Override
	public void breakBlock(World w,int x,int y,int z, int oid,int om)
	{
		int[] xo = {-2,2,0,0};
		int[] zo = {0,0,-2,2};
		for(int i=0;i<4;i++)
		{
			TileEntity te = w.getBlockTileEntity(x+xo[i], y, z+zo[i]);
			if(te instanceof TileEntityPowerLaser)
				((TileEntityPowerLaser)te).unlink();
		}
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:powerReactorTopBottom");
        iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:powerReactorSides");
    }

}
