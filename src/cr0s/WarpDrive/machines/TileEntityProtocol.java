package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import cr0s.WarpDrive.*;
import cr0s.WarpDrive.machines.TileEntityReactor.ReactorMode;

/**
 * Protocol block tile entity
 * @author Cr0s
 */
public class TileEntityProtocol extends WarpInterfacedTE {
    // Variables
    private int distance = 0;
    private int direction = 0;
    private ReactorMode mode = ReactorMode.IDLE;

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

    private int ticks = 0;
    private final int BLOCK_UPDATE_INTERVAL = 20 * 3; // 3 seconds

    private TileEntityReactor core = null;

    public TileEntityProtocol() {
    	peripheralName = "warpcore";
    	methodsArray = new String[] {
            "dim_getp", "dim_setp",
            "dim_getn", "dim_setn",
            "set_mode",
            "set_distance", "set_direction",
            "get_attached_players", "summon", "summon_all",
            "get_x", "get_y", "get_z",
            "get_energy_level", /* "get_energy_max",/**/
            "do_jump",
            "get_ship_size",
            "set_beacon_frequency",
            "get_dx", "get_dz",
            "set_core_frequency",
            "is_in_space", "is_in_hyperspace",
            "set_target_jumpgate",
            "isAttached", "get_energy_required"
        };
	}
    
    @Override
    public void updateEntity() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }

        if (++ticks >= BLOCK_UPDATE_INTERVAL) {
            core = findCoreBlock();
            if (core != null) {
            	if (mode.getCode() != getBlockMetadata()) {
            		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, mode.getCode(), 1 + 2);  // Activated
            	}
            } else if (getBlockMetadata() != 0) {
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);  // Inactive
            }

            ticks = 0;
        }
    }

    private void setMode(int mode) {
    	ReactorMode[] modes = ReactorMode.values();
    	if (mode >= 0 && mode <= modes.length) {
    		this.mode = modes[mode];
            WarpDrive.debugPrint(this + " Mode set to " + this.mode + " (" + this.mode.getCode() + ")");
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
        // WarpDrive.print("" + this + " Direction set to " + this.direction);
    }

    private void doJump() {
        if (core != null) {
            // Adding random ticks to warmup
            core.randomWarmupAddition = worldObj.rand.nextInt(WarpDriveConfig.WC_WARMUP_RANDOM_TICKS);
        } else {
        	WarpDrive.debugPrint("" + this + " doJump without a core");
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
            String nick = players.get(i);

            if (entityPlayer.username.equals(nick)) {
            	entityPlayer.addChatMessage(getBlockType().getLocalizedName() + " Detached.");
                players.remove(i);
                return;
            }
        }

        entityPlayer.attackEntityFrom(DamageSource.generic, 1);
        players.add(entityPlayer.username);
        updatePlayersString();
        entityPlayer.addChatMessage(getBlockType().getLocalizedName() + " Successfully attached.");
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
    	WarpDrive.debugPrint("" + this + " setJumpFlag(" + jumpFlag + ")");
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
    public void setFront(int front) {
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
    public void setRight(int right) {
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
    public void setUp(int up) {
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
    public void setBack(int back) {
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
    public void setLeft(int left) {
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
    public void setDown(int down) {
        this.down = down;
    }

    public void setDistance(int distance) {
        this.distance = Math.max(1, Math.min(WarpDriveConfig.WC_MAX_JUMP_DISTANCE, distance));
    	WarpDrive.debugPrint(this + " Jump distance set to " + distance);
    }

    public int getDistance() {
        return this.distance;
    }

    /**
     * @return current reactor mode
     */
    public ReactorMode getMode() {
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

    private TileEntityReactor findCoreBlock() {
    	TileEntity te;

    	te = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);
        if (te != null && te instanceof TileEntityReactor) {
            return (TileEntityReactor)te;
        }

        te = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);
        if (te != null && te instanceof TileEntityReactor) {
            return (TileEntityReactor)te;
        }

        te = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);
        if (te != null && te instanceof TileEntityReactor) {
            return (TileEntityReactor)te;
        }

        te = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);
        if (te != null && te instanceof TileEntityReactor) {
            return (TileEntityReactor)te;
        }

        return null;
    }

	// ComputerCraft IPeripheral methods implementation
    @Override
	@Optional.Method(modid = "ComputerCraft")
    public void attach(IComputerAccess computer)  {
    	super.attach(computer);
		if (WarpDriveConfig.G_LUA_SCRIPTS != WarpDriveConfig.LUA_SCRIPTS_NONE) {
	        computer.mount("/warpcontroller", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/warpcontroller"));
	        computer.mount("/warpupdater", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/common/updater"));
			if (WarpDriveConfig.G_LUA_SCRIPTS == WarpDriveConfig.LUA_SCRIPTS_ALL) {
		        computer.mount("/startup", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/warpcontroller/startup"));
			}
		}
    }

    @Override
	@Optional.Method(modid = "ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
		int argInt0, argInt1, argInt2;
		String methodName = methodsArray[method];
		
		if (methodName.equals("dim_getp")) {
			return new Integer[] { getFront(), getRight(), getUp() };
			
		} else if (methodName.equals("dim_setp")) {// dim_setp (front, right, up)
			if (arguments.length != 3) {
				return new Integer[] { getFront(), getRight(), getUp() };
			}
			try {
				argInt0 = toInt(arguments[0]);
				argInt1 = toInt(arguments[1]);
				argInt2 = toInt(arguments[2]);
			} catch (Exception e) {
				return new Integer[] { getFront(), getRight(), getUp() };
			}
			if (argInt0 < 0 || argInt1 < 0 || argInt2 < 0) {
				return new Integer[] { getFront(), getRight(), getUp() };
			}
			
			System.out.println("Setting positive gabarits: f: " + argInt0 + " r: " + argInt1 + " u: " + argInt2);
			setFront(argInt0);
			setRight(argInt1);
			setUp(argInt2);
			
			return new Integer[] { getFront(), getRight(), getUp() };
			
		} else if (methodName.equals("dim_getn")) {
			return new Integer[] { getBack(), getLeft(), getDown() };
			
		} else if (methodName.equals("dim_setn")) {// dim_setn (back, left, down)
			if (arguments.length != 3) {
				return new Integer[] { -1 };
			}
			try {
				argInt0 = toInt(arguments[0]);
				argInt1 = toInt(arguments[1]);
				argInt2 = toInt(arguments[2]);
			} catch (Exception e) {
				return new Integer[] { getBack(), getLeft(), getDown() };
			}
			if (argInt0 < 0 || argInt1 < 0 || argInt2 < 0) {
				return new Integer[] { getBack(), getLeft(), getDown() };
			}
			
			System.out.println("Setting negative gabarits: b: " + argInt0 + " l: " + argInt1 + " d: " + argInt2);
			setBack(argInt0);
			setLeft(argInt1);
			setDown(argInt2);
			
			return new Integer[] { getBack(), getLeft(), getDown() };
			
		} else if (methodName.equals("set_mode")) {// set_mode (mode)
			if (arguments.length != 1) {
				return new Integer[] { -1 };
			}
			try {
				argInt0 = toInt(arguments[0]);
			} catch (Exception e) {
				return new Integer[] { -1 };
			}
			
			setMode(argInt0);
			
		} else if (methodName.equals("set_distance")) {// set_distance (distance)
			if (arguments.length != 1) {
				return new Integer[] { -1 };
			}
			try {
				argInt0 = toInt(arguments[0]);
			} catch (Exception e) {
				return new Integer[] { -1 };
			}
			
			setDistance(argInt0);
			
			return new Integer[] { getDistance() };
			
		} else if (methodName.equals("set_direction")) {// set_direction (dir)
			if (arguments.length != 1) {
				return new Integer[] { -1 };
			}
			try {
				argInt0 = toInt(arguments[0]);
			} catch (Exception e) {
				return new Integer[] { -1 };
			}
			
			setDirection(argInt0);
			
		} else if (methodName.equals("get_attached_players")) {// get_attached_players
			String list = "";
			
			for (int i = 0; i < this.players.size(); i++) {
				String nick = this.players.get(i);
				list += nick + ((i == this.players.size() - 1) ? "" : ",");
			}
			
			if (players.isEmpty()) {
				list = "";
			}
			
			return new Object[] { list };
			
		} else if (methodName.equals("summon")) {// summon
			if (arguments.length != 1) {
				return new Integer[] { -1 };
			}
			try {
				argInt0 = toInt(arguments[0]);
			} catch (Exception e) {
				return new Integer[] { -1 };
			}
			
			if (argInt0 >= 0 && argInt0 < players.size()) {
				setToSummon(players.get(argInt0));
			}
			
		} else if (methodName.equals("summon_all")) {// summon_all
			this.setSummonAllFlag(true);
			
		} else if (methodName.equals("get_x")) {// get_x
			if (core == null) {
				return null;
			}
			
			return new Object[] { core.xCoord };
			
		} else if (methodName.equals("get_y")) {// get_y
			if (core == null) {
				return null;
			}
			
			return new Object[] { core.yCoord };
			
		} else if (methodName.equals("get_z")) {// get_z
			if (core == null) {
				return null;
			}
			
			return new Object[] { core.zCoord };
			
		} else if (methodName.equals("get_energy_level")) {// get_energy_level
			if (core == null) {
				return null;
			}
			
			return new Object[] { core.getEnergyStored() };
			
		} else if (methodName.equals("get_energy_max")) {// get_energy_max
			if (core == null) {
				return null;
			}
			
			return new Object[] { core.getMaxEnergyStored() };
			
		} else if (methodName.equals("get_energy_required")) {// get_energy_required(distance)
			if (arguments.length != 1) {
				return new Integer[] { -1 };
			}
			try {
				argInt0 = toInt(arguments[0]);
			} catch (Exception e) {
				return new Integer[] { -1 };
			}
			if (core != null) {
				return new Object[] { (int) (core.calculateRequiredEnergy(getMode(), core.shipVolume, argInt0)) };
			}
			
		} else if (methodName.equals("do_jump")) {// do_jump
			doJump();
			
		} else if (methodName.equals("get_ship_size")) {// get_ship_size
			if (core != null) {
				StringBuilder reason = new StringBuilder();
				try {
					if (!core.validateShipSpatialParameters(reason)) {
						core.messageToAllPlayersOnShip(reason.toString());
						if (core.controller == null) {
							return null;
						}
					}
					return new Object[] { core.shipVolume };
				} catch (Exception e) {
					if (WarpDriveConfig.G_DEBUGMODE) {
						e.printStackTrace();
					}
					return null;
				}
			}
			
		} else if (methodName.equals("set_beacon_frequency")) {// set_beacon_frequency
			if (arguments.length != 1) {
				return new Integer[] { -1 };
			}
			
			setBeaconFrequency((String) arguments[0]);
			
		} else if (methodName.equals("get_dx")) {// get_dx
			if (core != null) {
				return new Object[] { core.dx };
			}
			
		} else if (methodName.equals("get_dz")) {// get_dz
			if (core != null) {
				return new Object[] { core.dz };
			}
			
		} else if (methodName.equals("set_core_frequency")) {// set_core_frequency
			if (arguments.length == 1 && (core != null)) {
				core.coreFrequency = ((String) arguments[0]).replace("/", "").replace(".", "").replace("\\", ".");
			}
			
		} else if (methodName.equals("is_in_space")) {// is_in_space
			return new Boolean[] { worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID };
			
		} else if (methodName.equals("is_in_hyperspace")) {// is_in_hyperspace
			return new Boolean[] { worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID };
			
		} else if (methodName.equals("set_target_jumpgate")) {// set_target_jumpgate
			if (arguments.length == 1) {
				setTargetJumpgateName((String) arguments[0]);
			}
			
		} else if (methodName.equals("isAttached")) {// isAttached
			if (core != null) {
				return new Object[] { (boolean) (core.controller != null) };
			}
		}
		
		return new Integer[] { 0 };
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
    public void setTargetJumpgateName(String targetJumpgateName)
    {
        this.targetJumpgateName = targetJumpgateName;
    }
	
	@Override
	public String toString() {
        return String.format("%s \'%s\' @ \'%s\' %d, %d, %d", new Object[] {
       		getClass().getSimpleName(),
       		core == null ? beaconFrequency : core.coreFrequency,
       		worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
       		Integer.valueOf(xCoord), Integer.valueOf(yCoord), Integer.valueOf(zCoord)});
	}
}
