package cr0s.WarpDrive.machines;

import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.CamRegistryItem;
import cr0s.WarpDrive.ClientCameraUtils;
import cr0s.WarpDrive.EntityCamera;
import cr0s.WarpDrive.WarpDrive;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class BlockMonitor extends BlockContainer
{
    private Icon frontIcon;
    private Icon blockIcon;

    public BlockMonitor(int id)
    {
        super(id, Material.iron);
    }

    @SideOnly(Side.CLIENT)

    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    public Icon getIcon(int side, int meta)
    {
        meta &= 3;
        return side == 2 ? (meta == 0 ? this.frontIcon : this.blockIcon) : (side == 3 ? (meta == 2 ? this.frontIcon : this.blockIcon) : (side == 4 ? (meta == 3 ? this.frontIcon : this.blockIcon) : (side == 5 ? (meta == 1 ? this.frontIcon : this.blockIcon) : this.blockIcon)));
    }

    /**
     * When this method is called, your block should register all the icons it needs with the given IconRegister. This
     * is the only chance you get to register icons.
     */
    public void registerIcons(IconRegister reg)
    {
        this.frontIcon = reg.registerIcon("warpdrive:monitorFront");
        this.blockIcon = reg.registerIcon("warpdrive:monitorSide");
    }

    /**
     * Called when the block is placed in the world.
     */
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
    {
        int dir = Math.round(entityliving.rotationYaw / 90.0F) & 3;
        world.setBlockMetadataWithNotify(x, y, z, dir, 3);
    }

    /**
     * Called upon block activation (right click on the block.)
     */
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
    {
        if (!FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return true;
        }

        // Get camera frequency
        TileEntity te = world.getBlockTileEntity(x, y, z);

        if (te != null && te instanceof TileEntityMonitor)
        {
            int freq = ((TileEntityMonitor)te).getFrequency();
            WarpDrive.instance.cams.removeDeadCams();
            CamRegistryItem cam = WarpDrive.instance.cams.getCamByFreq(freq, world);

            if (cam == null || cam.worldObj == null || cam.worldObj != world || !WarpDrive.instance.cams.isCamAlive(cam))
            {
                entityplayer.addChatMessage("[Monitor: " + freq + "] Invalid frequency or camera is too far!");
                return false;
            }
            else
            {
                // Spawn camera entity
                EntityCamera e = new EntityCamera(world, cam.camPos, entityplayer);
                world.spawnEntityInWorld(e);
                e.setPositionAndUpdate(cam.camPos.x, cam.camPos.y, cam.camPos.z);
                //e.setPositionAndRotation(camPos.x, camPos.y, camPos.z, entityplayer.rotationYaw, entityplayer.rotationPitch);
                ClientCameraUtils.playerData = entityplayer;
                WarpDrive.instance.overlayType = cam.type;
                ClientCameraUtils.setupViewpoint(e);
            }
        }

        return false;
    }

    public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
    {
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityMonitor();
    }
}
