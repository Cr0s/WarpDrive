package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import net.minecraftforge.common.ForgeDirection;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityLift extends TileEntity implements IEnergySink
{
    public boolean addedToEnergyNet = false;

    private final int MAX_ENERGY_VALUE = 2048; // eU
    private int currentEnergyValue = 0;

    private int mode = 0; // 0 - inactive, 1 - up, 2 - down
    private int firstUncoveredY;

    private boolean isEnabled = false;

    int ticks = 0;

    @Override
    public void updateEntity()
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return;
        }

        if (!addedToEnergyNet && !this.tileEntityInvalid)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }

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

            if (currentEnergyValue != MAX_ENERGY_VALUE || !isEnabled)
            {
                mode = 0;
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);    // disabled
                return;
            }

            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, mode, 2); // current mode

            // Launch a beam
            if (isEnabled)
            {
                // Search non-air blocks under lift
                for (int ny = yCoord - 1; ny > 0; ny--)
                {
                    if (!worldObj.isAirBlock(xCoord, ny, zCoord))
                    {
                        firstUncoveredY = ny;
                        break;
                    }
                }

                if (yCoord - firstUncoveredY > 0)
                    if (mode == 1)
                    {
                        sendLaserPacket(new Vector3(this).add(0.5), new Vector3(xCoord, firstUncoveredY, zCoord).add(0.5), 0f, 1f, 0f, 40, 0, 100);
                    }
                    else if (mode == 2)
                    {
                        sendLaserPacket(new Vector3(this).add(0.5), new Vector3(xCoord, firstUncoveredY, zCoord).add(0.5), 0f, 0f, 1f, 40, 0, 100);
                    }

                liftEntity();
            }
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
                        sendLaserPacket(new Vector3(this).add(0.5), new Vector3(xCoord, firstUncoveredY, zCoord).add(0.5), 1, 1, 0, 40, 0, 100);
                        worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
                        currentEnergyValue = 0;
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
                        sendLaserPacket(new Vector3(this).add(0.5), new Vector3(xCoord, firstUncoveredY + 1, zCoord).add(0.5), 1, 1, 0, 40, 0, 100);
                        worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
                        currentEnergyValue = 0;
                        return;
                    }
                }
            }
        }
    }

    public void sendLaserPacket(Vector3 source, Vector3 dest, float r, float g, float b, int age, int energy, int radius)
    {
        Side side = FMLCommonHandler.instance().getEffectiveSide();

        if (side == Side.SERVER)
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
            DataOutputStream outputStream = new DataOutputStream(bos);

            try
            {
                // Write source vector
                outputStream.writeDouble(source.x);
                outputStream.writeDouble(source.y);
                outputStream.writeDouble(source.z);
                // Write target vector
                outputStream.writeDouble(dest.x);
                outputStream.writeDouble(dest.y);
                outputStream.writeDouble(dest.z);
                // Write r, g, b of laser
                outputStream.writeFloat(r);
                outputStream.writeFloat(g);
                outputStream.writeFloat(b);
                // Write age
                outputStream.writeByte(age);
                // Write energy value
                outputStream.writeInt(energy);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "WarpDriveBeam";
            packet.data = bos.toByteArray();
            packet.length = bos.size();
            MinecraftServer.getServer().getConfigurationManager().sendToAllNear(source.intX(), source.intY(), source.intZ(), radius, worldObj.provider.dimensionId, packet);
            ByteArrayOutputStream bos2 = new ByteArrayOutputStream(8);
            DataOutputStream outputStream2 = new DataOutputStream(bos2);

            try
            {
                // Write source vector
                outputStream2.writeDouble(source.x);
                outputStream2.writeDouble(source.y);
                outputStream2.writeDouble(source.z);
                // Write target vector
                outputStream2.writeDouble(dest.x);
                outputStream2.writeDouble(dest.y);
                outputStream2.writeDouble(dest.z);
                // Write r, g, b of laser
                outputStream2.writeFloat(r);
                outputStream2.writeFloat(g);
                outputStream2.writeFloat(b);
                // Write age
                outputStream2.writeByte(age);
                // Write energy value
                outputStream2.writeInt(energy);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            Packet250CustomPayload packet2 = new Packet250CustomPayload();
            packet.channel = "WarpDriveBeam";
            packet.data = bos.toByteArray();
            packet.length = bos.size();
            MinecraftServer.getServer().getConfigurationManager().sendToAllNear(dest.intX(), dest.intY(), dest.intZ(), radius, worldObj.provider.dimensionId, packet);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        this.currentEnergyValue = tag.getInteger("energy");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setInteger("energy", this.getCurrentEnergyValue());
    }

    // IEnergySink methods implementation
    @Override
    public double demandedEnergyUnits()
    {
        return (MAX_ENERGY_VALUE - currentEnergyValue);
    }

    @Override
    public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
    {
        double leftover = 0;
        currentEnergyValue += Math.round(amount);

        if (getCurrentEnergyValue() > MAX_ENERGY_VALUE)
        {
            leftover = (getCurrentEnergyValue() - MAX_ENERGY_VALUE);
            currentEnergyValue = MAX_ENERGY_VALUE;
        }

        return leftover;
    }

    @Override
    public int getMaxSafeInput()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
    {
        return true;
    }

    /**
     * @return the currentEnergyValue
     */
    public int getCurrentEnergyValue()
    {
        return currentEnergyValue;
    }

    public int collectAllEnergy()
    {
        int energy = currentEnergyValue;
        currentEnergyValue = 0;
        return energy;
    }

    @Override
    public void onChunkUnload()
    {
        if (addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
    }

    @Override
    public void invalidate()
    {
        if (addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }

        super.invalidate();
    }
}
