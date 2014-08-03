package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.CamRegistryItem;
import cr0s.WarpDrive.ClientCameraUtils;
import cr0s.WarpDrive.EntityCamera;
import cr0s.WarpDrive.WarpDrive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockMonitor extends BlockContainer {
    private Icon iconFront;
    private Icon iconBlock;

    public BlockMonitor(int id) {
        super(id, Material.iron);
        setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("warpdrive.machines.Monitor");
    }

    @Override
	@SideOnly(Side.CLIENT)

    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    public Icon getIcon(int side, int parMetadata) {
        int meta = parMetadata & 3;
        return side == 2 ? (meta == 0 ? this.iconFront : this.iconBlock) : (side == 3 ? (meta == 2 ? this.iconFront : this.iconBlock) : (side == 4 ? (meta == 3 ? this.iconFront : this.iconBlock) : (side == 5 ? (meta == 1 ? this.iconFront : this.iconBlock) : this.iconBlock)));
    }

    /**
     * When this method is called, your block should register all the icons it needs with the given IconRegister. This
     * is the only chance you get to register icons.
     */
    @Override
	public void registerIcons(IconRegister reg) {
        this.iconFront = reg.registerIcon("warpdrive:monitorFront");
        this.iconBlock = reg.registerIcon("warpdrive:monitorSide");
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack) {
        int dir = Math.round(entityliving.rotationYaw / 90.0F) & 3;
        world.setBlockMetadataWithNotify(x, y, z, dir, 3);
    }

    /**
     * Called upon block activation (right click on the block.)
     */
    @Override
	public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
    	// Monitor is only reacting client side
    	if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return false;
        }

        // Get camera frequency
        TileEntity te = par1World.getBlockTileEntity(x, y, z);

        if (te != null && te instanceof TileEntityMonitor && (par5EntityPlayer.getHeldItem() == null)) {
            int frequency = ((TileEntityMonitor)te).getFrequency();
            WarpDrive.instance.cams.removeDeadCams(par1World);
            CamRegistryItem cam = WarpDrive.instance.cams.getCamByFrequency(par1World, frequency);
            if (cam == null) {
            	par5EntityPlayer.addChatMessage(getLocalizedName() + " Frequency '" + frequency + "' is invalid or camera is too far!");
                return false;
            } else {
            	par5EntityPlayer.addChatMessage(getLocalizedName() + " Frequency '" + frequency + "' is valid. Viewing camera at " + cam.position.x + ", " + cam.position.y + ", " + cam.position.z);
                // Spawn camera entity
                EntityCamera e = new EntityCamera(par1World, cam.position, par5EntityPlayer);
                par1World.spawnEntityInWorld(e);
                e.setPositionAndUpdate(cam.position.x + 0.5D, cam.position.y + 0.5D, cam.position.z + 0.5D);
                //e.setPositionAndRotation(camPos.x, camPos.y, camPos.z, entityplayer.rotationYaw, entityplayer.rotationPitch);
                WarpDrive.instance.overlayType = cam.type;
                ClientCameraUtils.setupViewpoint(par5EntityPlayer, e, x, y, z, blockID, cam.position.x, cam.position.y, cam.position.z, par1World.getBlockId(cam.position.x, cam.position.y, cam.position.z));
            }
        }

        return false;
    }

    @Override
	public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6) {
    }

    @Override
    public TileEntity createNewTileEntity(World world) {
        return new TileEntityMonitor();
    }
}
