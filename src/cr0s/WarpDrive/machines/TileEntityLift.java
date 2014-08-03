package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import cr0s.WarpDrive.*;

public class TileEntityLift extends WarpEnergyTE {
    private final int MAX_ENERGY_VALUE = 2048; // eU

    private int mode = 0; // 0 - inactive, 1 - up, 2 - down
    private int firstUncoveredY;

    private boolean isEnabled = false;

    int ticks = 0;

    @Override
    public void updateEntity() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		super.updateEntity();

        if (++ticks > 40)
        {
            ticks = 0;

            // Switching mode
            if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
            {
                mode = 2; // down
            }
            else
            {
                mode = 1; // up
            }

            isEnabled = (worldObj.isAirBlock(xCoord, yCoord + 1, zCoord) && worldObj.isAirBlock(xCoord, yCoord + 2, zCoord));

            if (getEnergyStored() != MAX_ENERGY_VALUE || !isEnabled) {
                mode = 0;
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);    // disabled
                return;
            }

            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, mode, 2); // current mode

            // Launch a beam: search non-air blocks under lift
            for (int ny = yCoord - 1; ny > 0; ny--) {
            	int blockId = worldObj.getBlockId(xCoord, ny, zCoord);
            	// 63 & 68 = signs
                if (blockId != 0 && blockId != 63 && blockId != 68 && !WarpDriveConfig.isAirBlock(worldObj, blockId, xCoord, ny, zCoord)) {
                    firstUncoveredY = ny;
                    break;
                }
            }

            if (yCoord - firstUncoveredY > 0) {
                if (mode == 1) {
                	WarpDrive.instance.sendLaserPacket(worldObj, new Vector3(this).translate(0.5D), new Vector3(xCoord, firstUncoveredY, zCoord).translate(0.5D), 0f, 1f, 0f, 40, 0, 100);
                } else if (mode == 2) {
                	WarpDrive.instance.sendLaserPacket(worldObj, new Vector3(this).translate(0.5D), new Vector3(xCoord, firstUncoveredY, zCoord).translate(0.5D), 0f, 0f, 1f, 40, 0, 100);
                }
            }

            liftEntity();
        }
    }

    public void liftEntity()
    {
        int xmax, zmax, x1, x2, z1, z2;
        int xmin, zmin;
        final int CUBE_SIDE = 2;
        x1 = xCoord + CUBE_SIDE / 2;
        x2 = xCoord - CUBE_SIDE / 2;

        if (x1 < x2)
        {
            xmin = x1;
            xmax = x2;
        }
        else
        {
            xmin = x2;
            xmax = x1;
        }

        z1 = zCoord + CUBE_SIDE / 2;
        z2 = zCoord - CUBE_SIDE / 2;

        if (z1 < z2)
        {
            zmin = z1;
            zmax = z2;
        }
        else
        {
            zmin = z2;
            zmax = z1;
        }

        // Lift up
        if (mode == 1)
        {
            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xmin + 0.3, firstUncoveredY, zmin + 0.3, xmax - 0.3, yCoord, zmax - 0.3);
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);

            if (list != null)   // up
            {
                for (Object o : list)
                {
                    if (o != null && o instanceof EntityLivingBase)
                    {
                        ((EntityLivingBase)o).setPositionAndUpdate(xCoord + 0.5f, yCoord + 1, zCoord + 0.5f);
                        WarpDrive.sendLaserPacket(worldObj, new Vector3(this).translate(0.5), new Vector3(xCoord, firstUncoveredY, zCoord).translate(0.5), 1F, 1F, 0F, 40, 0, 100);
                        worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
                        consumeAllEnergy();
                        return;
                    }
                }
            }
        }
        else if (mode == 2)     // down
        {
            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xmin + 0.3, yCoord, zmin + 0.3, xmax - 0.3, yCoord + 2, zmax - 0.3);
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);

            if (list != null)
            {
                for (Object o : list)
                {
                    if (o != null && o instanceof EntityLivingBase)
                    {
                        ((EntityLivingBase)o).setPositionAndUpdate(xCoord + 0.5f, firstUncoveredY + 1, zCoord + 0.5f);
                        WarpDrive.sendLaserPacket(worldObj, new Vector3(this).translate(0.5), new Vector3(xCoord, firstUncoveredY + 1, zCoord).translate(0.5), 1F, 1F, 0F, 40, 0, 100);
                        worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
                        consumeAllEnergy();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
    }

    // IEnergySink methods implementation
    @Override
    public int getMaxEnergyStored() {
    	return MAX_ENERGY_VALUE;
    }
    
    @Override
    public int getMaxSafeInput() {
        return Integer.MAX_VALUE;
    }
}
