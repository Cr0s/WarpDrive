package shipmod.blockitem;

import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
import net.minecraft.world.World;
import shipmod.ShipMod;
import shipmod.chunk.ChunkBuilder;
import shipmod.entity.EntityShip;

public class BlockMarkShip extends BlockDirectional
{
    private Icon frontIcon;
	final int FUEL_CAN_ID = 30232;
	final int EMPTY_FUEL_CAN_ID = 30231;
	
	final int SECONDS_OF_FLYING_PER_CAN = 300; // without ship blocks
	final int TICKS_OF_TIME_PER_BLOCK_DECREASE = 10; // half a second per block
	
    public BlockMarkShip(int id)
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
        super.registerIcons(reg);
        this.frontIcon = reg.registerIcon("shipmod:shuttleControllerFront");
        this.blockIcon = reg.registerIcon("shipmod:shuttleControllerSide");
    }

    /**
     * Called when the block is placed in the world.
     */
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
    {
        int dir = Math.round(entityliving.rotationYaw / 90.0F) & 3;
        world.setBlockMetadataWithNotify(x, y, z, dir, 3);
    }
    
    private TileEntityChest searchChest(World worldObj, int xCoord, int yCoord, int zCoord) {
    	TileEntity result = null;
        result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);
        if (result != null && result instanceof TileEntityChest) {
            return (TileEntityChest) result;
        }

        result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);
        if (result != null && result instanceof TileEntityChest) {
            return (TileEntityChest) result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);
        if (result != null && result instanceof TileEntityChest) {
            return (TileEntityChest) result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);
        if (result != null && result instanceof TileEntityChest) {
            return (TileEntityChest) result;
        }
        
        result = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
        if (result != null && result instanceof TileEntityChest) {
            return (TileEntityChest) result;
        }

        return null;    	
    }
    
    private int consumeFuel(World worldObj, int xCoord, int yCoord, int zCoord) {
    	int res = 0;
    	
    	TileEntityChest chest = searchChest(worldObj, xCoord, yCoord, zCoord);
    	if (chest != null) {
    		for (int i = 0; i < chest.getSizeInventory(); i++) {
    			ItemStack stack = chest.getStackInSlot(i);
    			
    			if (stack != null && stack.itemID == FUEL_CAN_ID) {
    				res += (20 * SECONDS_OF_FLYING_PER_CAN) * stack.stackSize;
    				chest.setInventorySlotContents(i, new ItemStack(EMPTY_FUEL_CAN_ID, 1, 0));
    			}
    		}
    	}
    	
    	return res;
    }

    /**
     * Called upon block activation (right click on the block.)
     */
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return true;
        }
        else
        {
        	world.isRemote = true; // lock world from changes
            ChunkBuilder builder = new ChunkBuilder(world, x, y, z);
            builder.doFilling();
            
            if (builder.getResult() == 0)
            {
            	if (builder.getBlockCount() < 10) {
            		((EntityPlayerMP)entityplayer).addChatMessage("[ShipMod] Ship is too small!");
            		world.isRemote = false;
            		return false;            		
            	}
            	
            	int fuelLevel = consumeFuel(world, x, y, z);
            	fuelLevel -= (builder.getBlockCount() * TICKS_OF_TIME_PER_BLOCK_DECREASE);
            	
            	// By half of second per ship block
            	if (fuelLevel <= 0) {
            		((EntityPlayerMP)entityplayer).addChatMessage("[ShipMod] Not enough fuel!");
            		world.isRemote = false;
            		return false;
            	} else {
            		int flysec = fuelLevel / 20;
            		((EntityPlayerMP)entityplayer).addChatMessage("[ShipMod] Loaded fuel for " + ((flysec <= 120) ? flysec + " seconds" : (flysec / 60) + " minutes") + " of flying");
            	}
            	
                EntityShip entity = builder.getEntity(world);
                world.isRemote = false;
                if (entity != null)
                {
                	entity.health = builder.getBlockCount();
                	entity.fuelLevel = fuelLevel;
                    world.spawnEntityInWorld(entity);
                    entityplayer.mountEntity(entity);
                    return true;
                }
            }

            if (builder.getResult() == 1)
            {
                ((EntityPlayerMP)entityplayer).addChatMessage("[ShipMod] Cannot create vehicle with more than " + ShipMod.instance.modConfig.maxShipChunkBlocks + " blocks");
            }
            else if (builder.getResult() == 2)
            {
                ((EntityPlayerMP)entityplayer).addChatMessage("[ShipMod] Cannot create vehicle with no vehicle marker");
            }
            else if (builder.getResult() == 3)
            {
                ((EntityPlayerMP)entityplayer).addChatMessage("[ShipMod] An error occured while compiling ship. Please report the appeared exception in the console.");
            } else if (builder.getResult() == 4) {
            	((EntityPlayerMP)entityplayer).addChatMessage("[ShipMod] ACTIVE WARP-CORE DETECTED. CANNOT CREATE VEHICLE.");           	
            }

            world.isRemote = false;
            return false;
        }
    }
}
