package cr0s.warpdrive.block.movement;

import java.util.ArrayList;
import java.util.Arrays;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractInterfaced;
import cr0s.warpdrive.block.movement.TileEntityShipCore.ShipCoreMode;
import cr0s.warpdrive.config.WarpDriveConfig;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

/**
 * Protocol block tile entity
 * @author Cr0s
 */
public class TileEntityShipController extends TileEntityAbstractInterfaced {
	// Variables
	private int distance = 0;
	private int direction = 0;
	private ShipCoreMode mode = ShipCoreMode.IDLE;
	
	private boolean jumpFlag = false;
	private boolean summonFlag = false;
	private String toSummon = "";
	
	private String targetJumpgateName = "";
	
	// Gabarits
	private int front, right, up;
	private int back, left, down;
	
	// Player attaching
	public ArrayList<String> players = new ArrayList();
	public String playersString = "";
	
	private String beaconFrequency = "";
	
	boolean ready = false;                // Ready to operate (valid assembly)
	
	private final int updateInterval_ticks = 20 * WarpDriveConfig.SHIP_CONTROLLER_UPDATE_INTERVAL_SECONDS;
	private int updateTicks = updateInterval_ticks;
	private int bootTicks = 20;
	
	private TileEntityShipCore core = null;
	
	public TileEntityShipController() {
		super();
		
		peripheralName = "warpdriveShipController";
		addMethods(new String[] {
				"dim_positive",
				"dim_negative",
				"mode",
				"distance",
				"direction",
				"energy",
				"getAttachedPlayers",
				"summon",
				"summonAll",
				"jump",
				"getShipSize",
				"beaconFrequency",
				"getOrientation",
				"coreFrequency",
				"isInSpace",
				"isInHyperspace",
				"targetJumpgate",
				"isAttached",
				"getEnergyRequired"
		});
		CC_scripts = Arrays.asList("startup");
	}
    
    @Override
    public void updateEntity() {
		super.updateEntity();
		
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }

        // accelerate update ticks during boot
        if (bootTicks > 0) {
            bootTicks--;
            if (core == null) {
            	updateTicks = 1;
            }
        }
        updateTicks--;
        if (updateTicks <= 0) {
            updateTicks = updateInterval_ticks;
            
            core = findCoreBlock();
            if (core != null) {
            	if (mode.getCode() != getBlockMetadata()) {
            		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, mode.getCode(), 1 + 2);  // Activated
            	}
            } else if (getBlockMetadata() != 0) {
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);  // Inactive
            }
        }
    }

    private void setMode(int mode) {
    	ShipCoreMode[] modes = ShipCoreMode.values();
    	if (mode >= 0 && mode <= modes.length) {
    		this.mode = modes[mode];
    		if (WarpDriveConfig.LOGGING_JUMP) {
    			WarpDrive.logger.info(this + " Mode set to " + this.mode + " (" + this.mode.getCode() + ")");
    		}
    	}
    }

    private void setDirection(int dir) {
        if (dir == 1) {
        	this.direction = -1;
        } else if (dir == 2) {
        	this.direction = -2;
        } else if (dir == 255) {
        	this.direction = 270;
        } else {
        	this.direction = dir;
        }
        if (WarpDriveConfig.LOGGING_JUMP) {
        	WarpDrive.logger.info(this + " Direction set to " + direction);
        }
    }

    private void doJump() {
        if (core != null) {
            // Adding random ticks to warmup
            core.randomWarmupAddition = worldObj.rand.nextInt(WarpDriveConfig.SHIP_WARMUP_RANDOM_TICKS);
        } else {
        	WarpDrive.logger.error(this + " doJump without a core");
        }

        setJumpFlag(true);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        setMode(tag.getInteger("mode"));
        setFront(tag.getInteger("front"));
        setRight(tag.getInteger("right"));
        setUp(tag.getInteger("up"));
        setBack(tag.getInteger("back"));
        setLeft(tag.getInteger("left"));
        setDown(tag.getInteger("down"));
        setDistance(tag.getInteger("distance"));
        setDirection(tag.getInteger("direction"));
        playersString = tag.getString("players");
        updatePlayersList();
        setBeaconFrequency(tag.getString("bfreq"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        updatePlayersString();
        tag.setString("players", playersString);
        tag.setInteger("mode", this.mode.getCode());
        tag.setInteger("front", this.front);
        tag.setInteger("right", this.right);
        tag.setInteger("up", this.up);
        tag.setInteger("back", this.back);
        tag.setInteger("left", this.left);
        tag.setInteger("down", this.down);
        tag.setInteger("distance", this.distance);
        tag.setInteger("direction", this.direction);
        tag.setString("bfreq", getBeaconFrequency());
        // FIXME: shouldn't we save boolean jumpFlag, boolean summonFlag, String toSummon, String targetJumpgateName?
    }

    public void attachPlayer(EntityPlayer entityPlayer) {
        for (int i = 0; i < players.size(); i++) {
            String name = players.get(i);

            if (entityPlayer.getDisplayName().equals(name)) {
            	WarpDrive.addChatMessage(entityPlayer, getBlockType().getLocalizedName() + " Detached.");
                players.remove(i);
                return;
            }
        }

        entityPlayer.attackEntityFrom(DamageSource.generic, 1);
        players.add(entityPlayer.getDisplayName());
        updatePlayersString();
        WarpDrive.addChatMessage(entityPlayer, getBlockType().getLocalizedName() + " Successfully attached.");
    }

    public void updatePlayersString() {
        String nick;
        this.playersString = "";

        for (int i = 0; i < players.size(); i++) {
            nick = players.get(i);
            this.playersString += nick + "|";
        }
    }

    public void updatePlayersList() {
        String[] playersArray = playersString.split("\\|");

        for (int i = 0; i < playersArray.length; i++) {
            String nick = playersArray[i];

            if (!nick.isEmpty()) {
                players.add(nick);
            }
        }
    }

    public String getAttachedPlayersList() {
        StringBuilder list = new StringBuilder("");

        for (int i = 0; i < this.players.size(); i++) {
            String nick = this.players.get(i);
            list.append(nick + ((i == this.players.size() - 1) ? "" : ", "));
        }

        if (players.isEmpty()) {
            list = new StringBuilder("<nobody>");
        }

        return list.toString();
    }

    /**
     * @return the jumpFlag
     */
    public boolean isJumpFlag() {
        return jumpFlag;
    }

    /**
     * @param jumpFlag the jumpFlag to set
     */
    public void setJumpFlag(boolean jumpFlag) {
    	if (WarpDriveConfig.LOGGING_JUMP) {
    		WarpDrive.logger.info(this + " setJumpFlag(" + jumpFlag + ")");
    	}
        this.jumpFlag = jumpFlag;
    }

    /**
     * @return front size
     */
    public int getFront() {
        return front;
    }

    /**
     * @param front new front size
     * @return front size
     */
    private void setFront(int front) {
        this.front = front;
    }

    /**
     * @return right size
     */
    public int getRight() {
        return right;
    }

    /**
     * @param right new right size
     * @return right size
     */
    private void setRight(int right) {
        this.right = right;
    }

    /**
     * @return up size
     */
    public int getUp() {
        return up;
    }

    /**
     * @param up new up size
     * @return up size
     */
    private void setUp(int up) {
        this.up = up;
    }

    /**
     * @return back size
     */
    public int getBack() {
        return back;
    }

    /**
     * @param back new back size
     * @return back size
     */
    private void setBack(int back) {
        this.back = back;
    }

    /**
     * @return left size
     */
    public int getLeft() {
        return left;
    }

    /**
     * @param left new left size
     * @return left size
     */
    private void setLeft(int left) {
        this.left = left;
    }

    /**
     * @return down size
     */
    public int getDown() {
        return down;
    }

    /**
     * @param down new down size
     * @return down size
     */
    private void setDown(int down) {
        this.down = down;
    }

    private void setDistance(int distance) {
        this.distance = Math.max(1, Math.min(WarpDriveConfig.SHIP_MAX_JUMP_DISTANCE, distance));
        if (WarpDriveConfig.LOGGING_JUMP) {
        	WarpDrive.logger.info(this + " Jump distance set to " + distance);
        }
    }

    public int getDistance() {
        return this.distance;
    }

    /**
     * @return current reactor mode
     */
    public ShipCoreMode getMode() {
        return mode;
    }

    /**
     * @return the direction
     */
    public int getDirection() {
        return direction;
    }

    /**
     * @return the summonFlag
     */
    public boolean isSummonAllFlag() {
        return summonFlag;
    }

    /**
     * @param summonFlag to set
     */
    public void setSummonAllFlag(boolean summonFlag) {
        this.summonFlag = summonFlag;
    }

    /**
     * @return the toSummon
     */
    public String getToSummon() {
        return toSummon;
    }

    /**
     * @param toSummon the toSummon to set
     */
    public void setToSummon(String toSummon) {
        this.toSummon = toSummon;
    }

    /**
     * @return the beaconFrequency
     */
    public String getBeaconFrequency() {
        return beaconFrequency;
    }

    /**
     * @param beaconFrequency the beaconFrequency to set
     */
    public void setBeaconFrequency(String beaconFrequency) {
        //WarpDrive.debugPrint("Setting beacon frequency: " + beaconFrequency);
        this.beaconFrequency = beaconFrequency;
    }

    private TileEntityShipCore findCoreBlock() {
    	TileEntity tileEntity;

    	tileEntity = worldObj.getTileEntity(xCoord + 1, yCoord, zCoord);
        if (tileEntity != null && tileEntity instanceof TileEntityShipCore) {
            return (TileEntityShipCore)tileEntity;
        }

        tileEntity = worldObj.getTileEntity(xCoord - 1, yCoord, zCoord);
        if (tileEntity != null && tileEntity instanceof TileEntityShipCore) {
            return (TileEntityShipCore)tileEntity;
        }

        tileEntity = worldObj.getTileEntity(xCoord, yCoord, zCoord + 1);
        if (tileEntity != null && tileEntity instanceof TileEntityShipCore) {
            return (TileEntityShipCore)tileEntity;
        }

        tileEntity = worldObj.getTileEntity(xCoord, yCoord, zCoord - 1);
        if (tileEntity != null && tileEntity instanceof TileEntityShipCore) {
            return (TileEntityShipCore)tileEntity;
        }

        return null;
    }

	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] dim_positive(Context context, Arguments arguments) {
		return dim_positive(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] dim_negative(Context context, Arguments arguments) {
		return dim_negative(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] mode(Context context, Arguments arguments) {
		return mode(argumentsOCtoCC(arguments));
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] distance(Context context, Arguments arguments) {
		return distance(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] direction(Context context, Arguments arguments) {
		return direction(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getAttachedPlayers(Context context, Arguments arguments) {
		return getAttachedPlayers(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] summon(Context context, Arguments arguments) {
		return summon(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] summon_all(Context context, Arguments arguments) {
		setSummonAllFlag(true);
		return null;
	}
	
	@Override
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] position(Context context, Arguments arguments) {
		if (core == null) {
			return null;
		}
		
		return new Object[] { core.xCoord, core.yCoord, core.zCoord };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] energy(Context context, Arguments arguments) {
		if (core == null) {
			return null;
		}
		
		return core.energy();
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getEnergyRequired(Context context, Arguments arguments) {
		if (core == null) {
			return null;
		}
		
		return getEnergyRequired(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] jump(Context context, Arguments arguments) {
		doJump();
		return null;
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getShipSize(Context context, Arguments arguments) {
		return getShipSize(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] beaconFrequency(Context context, Arguments arguments) {
		return beaconFrequency(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getOrientation(Context context, Arguments arguments) {
		if (core != null) {
			return new Object[] { core.dx, 0, core.dz };
		}
		return null;
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] coreFrequency(Context context, Arguments arguments) {
		return shipName(argumentsOCtoCC(arguments));
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] isInSpace(Context context, Arguments arguments) {
		return new Boolean[] { worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID };
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] isInHyperspace(Context context, Arguments arguments) {
		return new Boolean[] { worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] targetJumpgate(Context context, Arguments arguments) {
		return targetJumpgate(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] isAttached(Context context, Arguments arguments) {
		if (core != null) {
			return new Object[] { (boolean) (core.controller != null) };
		}
		return null;
	}
	
	private Object[] dim_positive(Object[] arguments) {
		try {
			if (arguments.length == 3) {
				int argInt0, argInt1, argInt2;
				argInt0 = toInt(arguments[0]);
				argInt1 = toInt(arguments[1]);
				argInt2 = toInt(arguments[2]);
				if (argInt0 < 0 || argInt1 < 0 || argInt2 < 0) {
					return new Integer[] { getFront(), getRight(), getUp() };
				}
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info(this + " Positive dimensions set to front " + argInt0 + ", right " + argInt1 + ", up " + argInt2);
				}
				setFront(argInt0);
				setRight(argInt1);
				setUp(argInt2);
			}
		} catch (Exception e) {
			return new Integer[] { getFront(), getRight(), getUp() };
		}
		
		return new Integer[] { getFront(), getRight(), getUp() };
	}
	
	private Object[] dim_negative(Object[] arguments) {
		try {
			if (arguments.length == 3) {
				int argInt0, argInt1, argInt2;
				argInt0 = toInt(arguments[0]);
				argInt1 = toInt(arguments[1]);
				argInt2 = toInt(arguments[2]);
				if (argInt0 < 0 || argInt1 < 0 || argInt2 < 0) {
					return new Integer[] { getBack(), getLeft(), getDown() };
				}
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info(this + " Negative dimensions set to back " + argInt0 + ", left " + argInt1 + ", down " + argInt2);
				}
				setBack(argInt0);
				setLeft(argInt1);
				setDown(argInt2);
			}
		} catch (Exception e) {
			return new Integer[] { getBack(), getLeft(), getDown() };
		}
		
		return new Integer[] { getBack(), getLeft(), getDown() };
	}
	
	private Object[] mode(Object[] arguments) {
		try {
			if (arguments.length == 1) {
				setMode(toInt(arguments[0]));
			}
		} catch (Exception e) {
			return new Integer[] { mode.getCode() };
		}
		
		return new Integer[] { mode.getCode() };
	}
	
	private Object[] distance(Object[] arguments) {
		try {
			if (arguments.length == 1) {
				setDistance(toInt(arguments[0]));
			}
		} catch (Exception e) {
			return new Integer[] { getDistance() };
		}
		
		return new Integer[] { getDistance() };
	}
	
	private Object[] direction(Object[] arguments) {
		try {
			if (arguments.length == 1) {
				setDirection(toInt(arguments[0]));
			}
		} catch (Exception e) {
			return new Integer[] { getDirection() };
		}
		
		return new Integer[] { getDirection() };
	}
	
	private Object[] getAttachedPlayers(Object[] arguments) {
		String list = "";
		
		if (!players.isEmpty()) {
			for (int i = 0; i < players.size(); i++) {
				String nick = players.get(i);
				list += nick + ((i == players.size() - 1) ? "" : ",");
			}
		}
		
		return new Object[] { list, players };
	}
	
	private Object[] summon(Object[] arguments) {
		int playerIndex = -1;
		if (arguments.length != 1) {
			return new Object[] { false };
		}
		try {
			playerIndex = toInt(arguments[0]);
		} catch (Exception e) {
			return new Object[] { false };
		}
		
		if (playerIndex >= 0 && playerIndex < players.size()) {
			setToSummon(players.get(playerIndex));
			return new Object[] { true };
		}
		return new Object[] { false };
	}
	
	private Object[] getEnergyRequired(Object[] arguments) {
		try {
			if (arguments.length == 1 && core != null) {
				return new Object[] { (int) (core.calculateRequiredEnergy(getMode(), core.shipMass, toInt(arguments[0]))) };
			}
		} catch (Exception e) {
			return new Integer[] { -1 };
		}
		return new Integer[] { -1 };
	}
	
	private Object[] getShipSize(Object[] arguments) {
		if (core == null) {
			return null;
		}
		StringBuilder reason = new StringBuilder();
		try {
			if (!core.validateShipSpatialParameters(reason)) {
				core.messageToAllPlayersOnShip(reason.toString());
				if (core.controller == null) {
					return null;
				}
			}
			return new Object[] { core.shipMass };
		} catch (Exception exception) {
			if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {// disabled by default to avoid console spam as ship size is checked quite frequently
				exception.printStackTrace();
			}
			return null;
		}
	}
	
	private Object[] beaconFrequency(Object[] arguments) {
		if (arguments.length == 1) {
			setBeaconFrequency((String) arguments[0]);
		}
		return new Object[] { beaconFrequency };
	}
	
	private Object[] shipName(Object[] arguments) { 
		if (core == null) {
			return null;
		}
		if (arguments.length == 1) {
			core.shipName = ((String) arguments[0]).replace("/", "").replace(".", "").replace("\\", ".");
		}
		return new Object[] { core.shipName };
	}
	
	private Object[] targetJumpgate(Object[] arguments) { 
		if (arguments.length == 1) {
			setTargetJumpgateName((String) arguments[0]);
		}
		return new Object[] { targetJumpgateName };
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = getMethodName(method);
		
		if (methodName.equals("dim_positive")) {// dim_positive (front, right, up)
			return dim_positive(arguments);
			
		} else if (methodName.equals("dim_negative")) {// dim_negative (back, left, down)
			return dim_negative(arguments);
			
		} else if (methodName.equals("mode")) {// mode (mode)
			return mode(arguments);
			
		} else if (methodName.equals("distance")) {// distance (distance)
			return distance(arguments);
			
		} else if (methodName.equals("direction")) {// direction (direction)
			return direction(arguments);
			
		} else if (methodName.equals("getAttachedPlayers")) {
			return getAttachedPlayers(arguments);
			
		} else if (methodName.equals("summon")) {
			return summon(arguments);
			
		} else if (methodName.equals("summon_all")) {
			setSummonAllFlag(true);
			
		} else if (methodName.equals("position")) {
			if (core == null) {
				return null;
			}
			
			return new Object[] { core.xCoord, core.yCoord, core.zCoord };
			
		} else if (methodName.equals("energy")) {
			if (core == null) {
				return null;
			}
			
			return core.energy();
		
		} else if (methodName.equals("getEnergyRequired")) {// getEnergyRequired(distance)
			return getEnergyRequired(arguments);
			
		} else if (methodName.equals("jump")) {
			doJump();
			
		} else if (methodName.equals("getShipSize")) {
			return getShipSize(arguments);
			
		} else if (methodName.equals("beaconFrequency")) {
			return beaconFrequency(arguments);
			
		} else if (methodName.equals("getOrientation")) {
			if (core != null) {
				return new Object[] { core.dx, 0, core.dz };
			}
			return null;
			
		} else if (methodName.equals("coreFrequency")) {
			return shipName(arguments);
			
		} else if (methodName.equals("isInSpace")) {
			return new Boolean[] { worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID };
			
		} else if (methodName.equals("isInHyperspace")) {
			return new Boolean[] { worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID };
			
		} else if (methodName.equals("targetJumpgate")) {
			return targetJumpgate(arguments);
			
		} else if (methodName.equals("isAttached")) {// isAttached
			if (core != null) {
				return new Object[] { (boolean) (core.controller != null) };
			}
		}
		
		return super.callMethod(computer, context, method, arguments);
	}

    /**
     * @return the targetJumpgateName
     */
    public String getTargetJumpgateName()
    {
        return targetJumpgateName;
    }

    /**
     * @param targetJumpgateName the targetJumpgateName to set
     */
    public void setTargetJumpgateName(String parTargetJumpgateName) {
        targetJumpgateName = parTargetJumpgateName;
    }
	
	@Override
	public String toString() {
        return String.format("%s \'%s\' @ \'%s\' %d, %d, %d", new Object[] {
       		getClass().getSimpleName(),
       		core == null ? beaconFrequency : core.shipName,
       		worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
       		Integer.valueOf(xCoord), Integer.valueOf(yCoord), Integer.valueOf(zCoord)});
	}
}
