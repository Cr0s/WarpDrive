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
	Icon[] iconBuffer = new Icon[17];
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
		super.onBlockAdded(w, x, y, z);
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
		super.breakBlock(w, x, y, z, oid, om);
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
	public Icon getIcon(int side, int meta)
	{
		if(side == 0 || side == 1)
			return iconBuffer[16];
		if(meta >= 0 && meta < 16)
			return iconBuffer[meta];
		return iconBuffer[0];
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        iconBuffer[16] = par1IconRegister.registerIcon("warpdrive:reactorTB");
        iconBuffer[0]  = par1IconRegister.registerIcon("warpdrive:reactorSide00");
        iconBuffer[1]  = par1IconRegister.registerIcon("warpdrive:reactorSide01");
        iconBuffer[2]  = par1IconRegister.registerIcon("warpdrive:reactorSide02");
        iconBuffer[3]  = par1IconRegister.registerIcon("warpdrive:reactorSide03");
        iconBuffer[4]  = par1IconRegister.registerIcon("warpdrive:reactorSide10");
        iconBuffer[5]  = par1IconRegister.registerIcon("warpdrive:reactorSide11");
        iconBuffer[6]  = par1IconRegister.registerIcon("warpdrive:reactorSide12");
        iconBuffer[7]  = par1IconRegister.registerIcon("warpdrive:reactorSide13");
        iconBuffer[8]  = par1IconRegister.registerIcon("warpdrive:reactorSide20");
        iconBuffer[9]  = par1IconRegister.registerIcon("warpdrive:reactorSide21");
        iconBuffer[10] = par1IconRegister.registerIcon("warpdrive:reactorSide22");
        iconBuffer[11] = par1IconRegister.registerIcon("warpdrive:reactorSide23");
        iconBuffer[12] = par1IconRegister.registerIcon("warpdrive:reactorSide30");
        iconBuffer[13] = par1IconRegister.registerIcon("warpdrive:reactorSide31");
        iconBuffer[14] = par1IconRegister.registerIcon("warpdrive:reactorSide32");
        iconBuffer[15] = par1IconRegister.registerIcon("warpdrive:reactorSide33");
    }

}
