package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import cr0s.WarpDrive.*;
import cr0s.WarpDrive.data.Vector3;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public class TileEntityLift extends WarpEnergyTE implements IPeripheral {
    private static final int MODE_REDSTONE = -1;
    private static final int MODE_INACTIVE = 0;
    private static final int MODE_UP = 1;
    private static final int MODE_DOWN = 2;
    
    private int firstUncoveredY;
    private int mode = MODE_INACTIVE;
    private boolean isEnabled = false;
    private boolean computerEnabled = true;
    private int computerMode = MODE_REDSTONE;
    
    private String[] methodsArray = {
    	"energy",
    	"mode",
    	"active",
    	"help"
    };
    
    int tickCount = 0;
    
    @Override
    public void updateEntity() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		super.updateEntity();

		tickCount++;
        if (tickCount >= WarpDriveConfig.LL_TICK_RATE) {
        	tickCount = 0;
        	
            // Switching mode
            if (computerMode == MODE_DOWN || (computerMode == MODE_REDSTONE && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))) {
                mode = MODE_DOWN;
            } else {
                mode = MODE_UP;
            }
            
            isEnabled = computerEnabled
            		&& worldObj.isAirBlock(xCoord, yCoord + 1, zCoord)
            		&& worldObj.isAirBlock(xCoord, yCoord + 2, zCoord)
            		&& worldObj.isAirBlock(xCoord, yCoord - 1, zCoord);
            
            if (getEnergyStored() < WarpDriveConfig.LL_LIFT_ENERGY || !isEnabled) {
                mode = MODE_INACTIVE;
                if (getBlockMetadata() != 0) {
                	worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);    // disabled
                }
                return;
            }
            
            if (getBlockMetadata() != mode) {
            	worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, mode, 2); // current mode
            }
            
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
                if (mode == MODE_UP) {
                	PacketHandler.sendBeamPacket(worldObj, new Vector3(xCoord, firstUncoveredY, zCoord).translate(0.5D), new Vector3(this).translate(0.5D), 0f, 1f, 0f, 40, 0, 100);
                } else if (mode == MODE_DOWN) {
                	PacketHandler.sendBeamPacket(worldObj, new Vector3(this).translate(0.5D), new Vector3(xCoord, firstUncoveredY, zCoord).translate(0.5D), 0f, 0f, 1f, 40, 0, 100);
                }
            }
            
            liftEntity();
        }
    }

    public void liftEntity() {
        final double CUBE_RADIUS = 0.4;
        double xmax, zmax;
        double xmin, zmin;
        
        xmin = xCoord + 0.5 - CUBE_RADIUS;
        xmax = xCoord + 0.5 + CUBE_RADIUS;
        zmin = zCoord + 0.5 - CUBE_RADIUS;
        zmax = zCoord + 0.5 + CUBE_RADIUS;
        
        // Lift up
        if (mode == MODE_UP) {
            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xmin, firstUncoveredY, zmin, xmax, yCoord, zmax);
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);
            if (list != null) {
                for (Object o : list) {
                    if (o != null && o instanceof EntityLivingBase && consumeEnergy(WarpDriveConfig.LL_LIFT_ENERGY, true)) {
                        ((EntityLivingBase)o).setPositionAndUpdate(xCoord + 0.5f, yCoord + 1, zCoord + 0.5f);
                        PacketHandler.sendBeamPacket(worldObj, new Vector3(this).translate(0.5), new Vector3(xCoord, firstUncoveredY, zCoord).translate(0.5), 1F, 1F, 0F, 40, 0, 100);
                        worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
                        consumeEnergy(WarpDriveConfig.LL_LIFT_ENERGY, true);
                    }
                }
            }
        } else if (mode == MODE_DOWN) {
            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xmin, firstUncoveredY + 3, zmin, xmax, yCoord + 2, zmax);
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);
            if (list != null) {
                for (Object o : list) {
                    if (o != null && o instanceof EntityLivingBase && consumeEnergy(WarpDriveConfig.LL_LIFT_ENERGY, true)) {
						((EntityLivingBase)o).setPositionAndUpdate(xCoord + 0.5f, firstUncoveredY + 1, zCoord + 0.5f);
						PacketHandler.sendBeamPacket(worldObj, new Vector3(this).translate(0.5), new Vector3(xCoord, firstUncoveredY + 0.5, zCoord).translate(0.5), 1F, 1F, 0F, 40, 0, 100);
						worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
						consumeEnergy(WarpDriveConfig.LL_LIFT_ENERGY, true);
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

    @Override
    public int getMaxEnergyStored() {
    	return WarpDriveConfig.LL_MAX_ENERGY;
    }
    
    @Override
    public boolean canInputEnergy(ForgeDirection from) {
    	return true;
    }
    
    // IEnergySink methods implementation
    @Override
    public int getMaxSafeInput() {
        return Integer.MAX_VALUE;
    }


    @Override
    public String getType() {
    	return "warpdriveLaserLift";
    }

    @Override
    public String[] getMethodNames() {
    	return methodsArray;
    }

    public String helpStr(Object[] args) {
    	if (args.length == 1) {
    		String methodName = args[0].toString().toLowerCase();
    		if (methodName.equals("energy")) {
    			return WarpDrive.defEnergyStr;
    		} else if (methodName.equals("mode")) {
    			return "mode(\"up\" or \"down\" or \"redstone\"): sets the mode\nmode(): returns the current mode";
    		} else if (methodName.equals("active")) {
    			return "active(boolean): sets whether the laser is active\nactive(): returns whether the laser is active";
    		}
    	}
    	return WarpDrive.defHelpStr;
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] args) throws Exception {
    	String methodName = methodsArray[method];
    	if (methodName.equals("energy")) {
    		return getEnergyObject();
		} else if (methodName.equals("mode")) {
			if (args.length == 1) {
				if (args[0].toString().equals("up")) {
					computerMode = MODE_UP;
				} else if(args[0].toString().equals("down")) {
					computerMode = MODE_DOWN;
				} else {
					computerMode = MODE_REDSTONE;
				}
			}
			switch (computerMode) {
				case -1:
					return new Object[] { "redstone" };
				case 1:
					return new Object[] { "up" };
				case 2:
					return new Object[] { "down" };
			}
		} else if (methodName.equals("active")) {
			if (args.length == 1) {
				computerEnabled = toBool(args[0]);
    		}
    		return new Object[] { computerEnabled ? false : isEnabled };
    	} else if (methodName.equals("help")) {
    		return new Object[] { helpStr(args) };
    	}
    	return null;
    }
    
	@Override
	public void attach(IComputerAccess computer) {

	}

	@Override
	public void detach(IComputerAccess computer) {

	}
	
	@Override
	public int hashCode() {
		return (((((super.hashCode() + worldObj.provider.dimensionId << 4) + xCoord) << 4) + yCoord) << 4) + zCoord;
	}
	
	@Override
	public boolean equals(IPeripheral other) {
		return other.hashCode() == hashCode();
	}
}
