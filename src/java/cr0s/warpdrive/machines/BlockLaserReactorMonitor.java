package cr0s.WarpDrive.machines;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.WarpDrive;

public class BlockLaserReactorMonitor extends BlockContainer {
    public BlockLaserReactorMonitor(int id, int texture, Material material) {
        super(id, material);
        setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("warpdrive.machines.LaserReactorMonitor");
    }
    
    public BlockLaserReactorMonitor(int id, Material material) {
        this(id, 0, material);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        // Solid textures
        blockIcon = par1IconRegister.registerIcon("warpdrive:reactorMonitor");
    }

    @Override
    public TileEntity createNewTileEntity(World var1) {
        return new TileEntityLaserReactorMonitor();
    }
}
