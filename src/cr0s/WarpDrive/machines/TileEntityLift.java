package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;

import java.util.List;

import li.cil.oc.api.Network;
import li.cil.oc.api.detail.Builder;
import li.cil.oc.api.detail.Builder.NodeBuilder;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.ComponentConnector;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import cr0s.WarpDrive.*;
import cr0s.WarpDrive.data.Vector3;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = WarpDriveConfig.modid_OpenComputers),
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = WarpDriveConfig.modid_ComputerCraft)
})
public class TileEntityLift extends WarpEnergyTE implements IPeripheral, Environment {
    private static final int MODE_REDSTONE = -1;
    private static final int MODE_INACTIVE = 0;
    private static final int MODE_UP = 1;
    private static final int MODE_DOWN = 2;
    
    private int firstUncoveredY;
    private int mode = MODE_INACTIVE;
    private boolean isEnabled = false;
    private boolean computerEnabled = true;
    private int computerMode = MODE_REDSTONE;
    
    private String peripheralName = "warpdriveLaserLift";
    private String[] methodsArray = {
    	"energy",
    	"mode",
    	"active",
    	"help"
    };
    
    int tickCount = 0;
    
    protected Node node;
    protected boolean addedToNetwork = false;
    
    public TileEntityLift() {
    	if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
    		initOC();
    }
    
    @Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
    private void initOC() {
    	node = Network.newNode(this, Visibility.Network).withComponent(peripheralName).create();
    }
    
    @Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
    private void addToNetwork() {
		if (!addedToNetwork) {
			addedToNetwork = true;
			Network.joinOrCreateNetwork(this);
		}
    }
    
    @Override
    public void updateEntity() {
		if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
			addToNetwork();
    	if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		super.updateEntity();

		tickCount++;
        if (tickCount >= WarpDriveConfig.LL_TICK_RATE) {
        	tickCount = 0;
        	
            // Switching mode
            if (computerMode == MODE_DOWN || (computerMode == MODE_REDSTONE && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))) {
                mode = 2; // down
            } else {
                mode = 1; // up
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
                if (mode == 1) {
                	WarpDrive.instance.sendLaserPacket(worldObj, new Vector3(this).translate(0.5D), new Vector3(xCoord, firstUncoveredY, zCoord).translate(0.5D), 0f, 1f, 0f, 40, 0, 100);
                } else if (mode == 2) {
                	WarpDrive.instance.sendLaserPacket(worldObj, new Vector3(this).translate(0.5D), new Vector3(xCoord, firstUncoveredY, zCoord).translate(0.5D), 0f, 0f, 1f, 40, 0, 100);
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
                        WarpDrive.sendLaserPacket(worldObj, new Vector3(this).translate(0.5), new Vector3(xCoord, firstUncoveredY, zCoord).translate(0.5), 1F, 1F, 0F, 40, 0, 100);
                        worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
                        consumeEnergy(WarpDriveConfig.LL_LIFT_ENERGY, false);
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
						WarpDrive.sendLaserPacket(worldObj, new Vector3(this).translate(0.5), new Vector3(xCoord, firstUncoveredY + 0.5, zCoord).translate(0.5), 1F, 1F, 0F, 40, 0, 100);
						worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
						consumeEnergy(WarpDriveConfig.LL_LIFT_ENERGY, false);
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        computerMode = tag.getInteger("cm");
        computerEnabled = tag.getBoolean("en");
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	readFromNBT_OC(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setInteger("cm", computerMode);
        tag.setBoolean("en", computerEnabled);
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	writeToNBT_OC(tag);
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
    
    // ComputerCraft.
    
    @Override
    @Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
    public String getType() {
    	return peripheralName;
    }

    @Override
    @Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
    public String[] getMethodNames() {
    	return methodsArray;
    }

    @Override
    @Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
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
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public void attach(IComputerAccess computer) {

	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public void detach(IComputerAccess computer) {

	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public boolean equals(IPeripheral other) {
		return other.hashCode() == hashCode();
	}

	// OpenComputers.

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Node node() {
		return node;
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void onConnect(Node node) {}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void onDisconnect(Node node) {}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void onMessage(Message message) {}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void onChunkUnload() {
		super.onChunkUnload();
		if (node != null) node.remove();
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void invalidate() {
		super.invalidate();
		if (node != null) node.remove();
	}

	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void readFromNBT_OC(final NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (node != null && node.host() == this) {
			node.load(nbt.getCompoundTag("oc:node"));
		}
	}

	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void writeToNBT_OC(final NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if (node != null && node.host() == this) {
			final NBTTagCompound nodeNbt = new NBTTagCompound();
			node.save(nodeNbt);
			nbt.setTag("oc:node", nodeNbt);
		}
	}

	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] energy(Context context, Arguments args) {
		return getEnergyObject();
	}

	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] mode(Context context, Arguments args) {
			if (args.count() == 1) {
				if (args.checkString(0).equals("up")) {
					computerMode = MODE_UP;
				} else if(args.checkString(0).equals("down")) {
					computerMode = MODE_DOWN;
				} else {
					computerMode = MODE_REDSTONE;
				}
			}
			switch (computerMode) {
				case MODE_REDSTONE:
					return new Object[] { "redstone" };
				case MODE_UP:
					return new Object[] { "up" };
				case MODE_DOWN:
					return new Object[] { "down" };
			}
		return null;
	}

	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] active(Context context, Arguments args) throws Exception {
		if (args.count() == 0) {
			return new Object[] {computerEnabled ? isEnabled : false};
		} else if ((args.count() == 1) && (args.isBoolean(0))) {
			computerEnabled = args.checkBoolean(0);
			return new Object[] {computerEnabled ? isEnabled : false};
		} else {
			throw new Exception("\"active\" expects zero arguments or one boolean.");
		}
	}

	@Override
	public int hashCode() {
		return (((((super.hashCode() + worldObj.provider.dimensionId << 4) + xCoord) << 4) + yCoord) << 4) + zCoord;
	}
}
