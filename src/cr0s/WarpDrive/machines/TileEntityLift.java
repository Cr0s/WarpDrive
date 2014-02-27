package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.Vector3;
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

public class TileEntityLift extends TileEntityAbstractLaser implements IEnergySink
{
    public boolean addedToEnergyNet = false;

    private final int MAX_ENERGY_VALUE = 2048; // eU
    private int currentEnergyValue = 0;

    private int mode = 0; // 0 - inactive, 1 - up, 2 - down
    private int firstUncoveredY;
    private Vector3 firstUncoveredYVec;
    private Vector3 myVector;

    private boolean isEnabled = false;

    int ticks = 0;
    
    public TileEntityLift()
    {
    	super();
    }

    private void sendLaser(float r,float g,float b, int age, int energy, int rad)
    {
    	sendLaserPacket(myVector,firstUncoveredYVec,r,g,b,age,energy,rad);
    }
    
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
        if(ticks % 8 == 0)
        	if(isEnabled)
        		liftEntity();

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
                        myVector = new Vector3(this).translate(0.5);
                        firstUncoveredYVec = new Vector3(xCoord,ny,zCoord).translate(0.5);
                        break;
                    }
                }

                if (yCoord - firstUncoveredY > 0)
                    if (mode == 1)
                    {
                    	sendLaser(0f,1f,0f,40,0,100);
                        //sendLaserPacket(new Vector3(this).translate(0.5), new Vector3(xCoord, firstUncoveredY, zCoord).translate(0.5), 0f, 1f, 0f, 40, 0, 100);
                    }
                    else if (mode == 2)
                    {
                    	sendLaser(0f,0f,1f,40,0,100);
                        sendLaserPacket(new Vector3(this).translate(0.5), new Vector3(xCoord, firstUncoveredY, zCoord).translate(0.5), 0f, 0f, 1f, 40, 0, 100);
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
        double errorMargin = 0.1;
        // Lift up
        if (mode == 1)
        {
            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xmin + errorMargin, firstUncoveredY, zmin + errorMargin, xmax - errorMargin, yCoord, zmax - errorMargin);
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);

            if (list != null)   // up
            {
                for (Object o : list)
                {
                    if (o != null && o instanceof EntityLivingBase)
                    {
                        ((EntityLivingBase)o).setPositionAndUpdate(xCoord + 0.5f, yCoord + 1, zCoord + 0.5f);
                        sendLaser(1f,1f,0f,40,0,100);
                        //sendLaserPacket(new Vector3(this).add(0.5), new Vector3(xCoord, firstUncoveredY, zCoord).add(0.5), 1, 1, 0, 40, 0, 100);
                        worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
                        currentEnergyValue = 0;
                        return;
                    }
                }
            }
        }
        else if (mode == 2)     // down
        {
            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xmin + errorMargin, yCoord, zmin + errorMargin, xmax - errorMargin, yCoord + 2, zmax - errorMargin);
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);

            if (list != null)
            {
                for (Object o : list)
                {
                    if (o != null && o instanceof EntityLivingBase)
                    {
                        ((EntityLivingBase)o).setPositionAndUpdate(xCoord + 0.5f, firstUncoveredY + 1, zCoord + 0.5f);
                        //sendLaserPacket(new Vector3(this).translate(0.5), new Vector3(xCoord, firstUncoveredY + 1, zCoord).translate(0.5), 1, 1, 0, 40, 0, 100);
                        sendLaser(1f,1f,0f,40,0,100);
                        worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
                        currentEnergyValue = 0;
                        return;
                    }
                }
            }
        }
    }
    
    @Override
    public boolean shouldChunkLoad()
    {
    	return false;
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
