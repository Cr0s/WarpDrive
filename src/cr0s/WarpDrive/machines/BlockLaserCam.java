package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.CamRegistryItem;
import cr0s.WarpDrive.WarpDrive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockLaserCam extends BlockContainer {
    private Icon[] iconBuffer;

    private final int ICON_SIDE = 0;

    public BlockLaserCam(int id, int texture, Material material) {
        super(id, material);
        setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("Laser Emitter + Camera");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        iconBuffer = new Icon[1];
        // Solid textures
        iconBuffer[ICON_SIDE] = par1IconRegister.registerIcon("warpdrive:laserSideCam");
    }

    @Override
    public Icon getIcon(int side, int metadata) {
        return iconBuffer[ICON_SIDE];
    }

    @Override
    public TileEntity createNewTileEntity(World parWorld) {
        return new TileEntityLaser();
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random) {
        return 1;
    }

    /**
     * Returns the ID of the items to drop on destruction.
     */
    @Override
    public int idDropped(int par1, Random par2Random, int par3) {
        return this.blockID;
    }

    /**
     * Called upon block activation (right click on the block.)
     */
    public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return false;
        }

        // Get camera frequency
        TileEntity te = par1World.getBlockTileEntity(x, y, z);
        if (te != null && te instanceof TileEntityLaser && (par5EntityPlayer.getHeldItem() == null)) {
            int beamFrequency = ((TileEntityLaser)te).getBeamFrequency();
            int cameraFrequency = ((TileEntityLaser)te).getCameraFrequency();

            CamRegistryItem cam = WarpDrive.instance.cams.getCamByFrequency(par1World, cameraFrequency);
        	par5EntityPlayer.addChatMessage(getLocalizedName()
        			+ ": Beam frequency '" + beamFrequency + "' is " + ((beamFrequency < 0) ? "invalid!":"valid.") 
        			+ " Camera frequency '" + cameraFrequency + "' is " + ((cam == null) ? "invalid!":"valid for laser-camera at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z));
            return true;
        }

        return false;
    }
}