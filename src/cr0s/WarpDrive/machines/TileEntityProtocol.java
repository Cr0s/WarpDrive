package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.ArrayList;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
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
@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = WarpDriveConfig.modid_OpenComputers),
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = WarpDriveConfig.modid_ComputerCraft)
})
public class TileEntityProtocol extends TileEntity implements IPeripheral, Environment
{
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

    private String peripheralName = "warpcore";
    public String[] methodsArray = {
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

    protected Node node;
    protected boolean addedToNetwork = false;

    private int ticks = 0;
    private final int BLOCK_UPDATE_INTERVAL = 20 * 3; // 3 seconds

    private TileEntityReactor core = null;

	public TileEntityProtocol() {
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
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	readFromNBT_OC(tag);
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
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	writeToNBT_OC(tag);
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
     * @return the front
     */
    public int getFront() {
        return front;
    }

    /**
     * @param front the front to set
     */
    public void setFront(int front) {
        this.front = front;
    }

    /**
     * @return the right
     */
    public int getRight() {
        return right;
    }

    /**
     * @param right the right to set
     */
    public void setRight(int right) {
        this.right = right;
    }

    /**
     * @return the up
     */
    public int getUp() {
        return up;
    }

    /**
     * @param up the up to set
     */
    public void setUp(int up) {
        this.up = up;
    }

    /**
     * @return the back
     */
    public int getBack() {
        return back;
    }

    /**
     * @param back the back to set
     */
    public void setBack(int back) {
        this.back = back;
    }

    /**
     * @return the left
     */
    public int getLeft() {
        return left;
    }

    /**
     * @param left the left to set
     */
    public void setLeft(int left) {
        this.left = left;
    }

    /**
     * @return the down
     */
    public int getDown() {
        return down;
    }

    /**
     * @param down the down to set
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
     * @return the mode
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
     * @param summonFlag the summonFlag to set
     */
    public void setSummonAllFlag(boolean summonFlag) {
        this.summonFlag = summonFlag;
    }

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
    public void attach(IComputerAccess computer)  {
		if (WarpDriveConfig.G_LUA_SCRIPTS != WarpDriveConfig.LUA_SCRIPTS_NONE) {
	        computer.mount("/warpcontroller", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/warpcontroller"));
	        computer.mount("/warpupdater", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/common/updater"));
			if (WarpDriveConfig.G_LUA_SCRIPTS == WarpDriveConfig.LUA_SCRIPTS_ALL) {
		        computer.mount("/startup", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/warpcontroller/startup"));
			}
		}
    }

    @Override
    @Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
    public void detach(IComputerAccess computer) {
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

    @Override
    @Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
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
				argInt0 = ((Double) arguments[0]).intValue();
				argInt1 = ((Double) arguments[1]).intValue();
				argInt2 = ((Double) arguments[2]).intValue();
			} catch (Exception e) {
				return new Integer[] { getFront(), getRight(), getUp() };
			}
			if (argInt0 < 0 || argInt1 < 0 || argInt2 < 0) {
				return new Integer[] { getFront(), getRight(), getUp() };
			}
			
			System.out.println("Setting positive gabarits: f: " + argInt0 + " r: " + argInt1 + " u: " + argInt2);
			setFront(((Double) arguments[0]).intValue());
			setRight(((Double) arguments[1]).intValue());
			setUp(((Double) arguments[2]).intValue());
			
			return new Integer[] { getFront(), getRight(), getUp() };
			
		} else if (methodName.equals("dim_getn")) {
			return new Integer[] { getBack(), getLeft(), getDown() };
			
		} else if (methodName.equals("dim_setn")) {// dim_setn (back, left, down)
			if (arguments.length != 3) {
				return new Integer[] { -1 };
			}
			try {
				argInt0 = ((Double) arguments[0]).intValue();
				argInt1 = ((Double) arguments[1]).intValue();
				argInt2 = ((Double) arguments[2]).intValue();
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
				argInt0 = ((Double) arguments[0]).intValue();
			} catch (Exception e) {
				return new Integer[] { -1 };
			}
			
			setMode(argInt0);
			
		} else if (methodName.equals("set_distance")) {// set_distance (distance)
			if (arguments.length != 1) {
				return new Integer[] { -1 };
			}
			try {
				argInt0 = ((Double) arguments[0]).intValue();
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
				argInt0 = ((Double) arguments[0]).intValue();
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
				argInt0 = ((Double) arguments[0]).intValue();
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
				argInt0 = ((Double) arguments[0]).intValue();
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
						return null;
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
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public boolean equals(IPeripheral other) {
		return other == this;
	}
	
	@Override
	public String toString() {
        return String.format("%s \'%s\' @ \'%s\' %d, %d, %d", new Object[] {
       		getClass().getSimpleName(),
       		core == null ? beaconFrequency : core.coreFrequency,
       		worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
       		Integer.valueOf(xCoord), Integer.valueOf(yCoord), Integer.valueOf(zCoord)});
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
	public Object[] dim_getp(Context context, Arguments args) {
		return new Integer[] { getFront(), getRight(), getUp() };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] dim_setp(Context context, Arguments args) {
		int argInt0, argInt1, argInt2;
		if ((args.count() != 3) || !args.isInteger(0) || !args.isInteger(1) || !args.isInteger(2)) {
			return new Integer[] { getFront(), getRight(), getUp() };
		}
		argInt0 = args.checkInteger(0);
		argInt1 = args.checkInteger(1);
		argInt2 = args.checkInteger(2);
		if (argInt0 < 0 || argInt1 < 0 || argInt2 < 0) {
			return new Integer[] { getFront(), getRight(), getUp() };
		}
		System.out.println("Setting positive gabarits: f: " + argInt0 + " r: " + argInt1 + " u: " + argInt2);
		setFront(args.checkInteger(0));
		setRight(args.checkInteger(1));
		setUp(args.checkInteger(2));
		return new Integer[] { getFront(), getRight(), getUp() };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] dim_getn(Context context, Arguments args) {
		return new Integer[] { getBack(), getLeft(), getDown() };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] dim_setn(Context context, Arguments args) {
		int argInt0, argInt1, argInt2;
		if ((args.count() != 3) || !args.isInteger(0) || !args.isInteger(1) || !args.isInteger(2)) {
			return new Integer[] { -1 };
		}
		argInt0 = args.checkInteger(0);
		argInt1 = args.checkInteger(1);
		argInt2 = args.checkInteger(2);
		if (argInt0 < 0 || argInt1 < 0 || argInt2 < 0) {
			return new Integer[] { getBack(), getLeft(), getDown() };
		}
		System.out.println("Setting negative gabarits: b: " + argInt0 + " l: " + argInt1 + " d: " + argInt2);
		setBack(argInt0);
		setLeft(argInt1);
		setDown(argInt2);
		return new Integer[] { getBack(), getLeft(), getDown() };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] set_mode(Context context, Arguments args) {
		if ((args.count() != 1) || !args.isInteger(0)) {
			return new Integer[] { -1 };
		}
		setMode(args.checkInteger(0));
		return new Integer[] { 0 };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] set_distance(Context context, Arguments args) {
		if ((args.count() != 1) || !args.isInteger(0)) {
			return new Integer[] { -1 };
		}
		setDistance(args.checkInteger(0));
		return new Integer[] { getDistance() };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] set_direction(Context context, Arguments args) {
		if ((args.count() != 1) || !args.isInteger(0)) {
			return new Integer[] { -1 };
		}
		setDirection(args.checkInteger(0));
		return new Integer[] { 0 };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] get_attached_players(Context context, Arguments args) {
		String list = "";
		for (int i = 0; i < this.players.size(); i++) {
			String nick = this.players.get(i);
			list += nick + ((i == this.players.size() - 1) ? "" : ",");
		}
		if (players.isEmpty()) {
			list = "";
		}
		return new Object[] { list };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] summon(Context context, Arguments args) {
		int argInt0;
		if ((args.count() != 1) || !args.isInteger(0)) {
			return new Integer[] { -1 };
		}
		argInt0 = args.checkInteger(0);
		if (argInt0 >= 0 && argInt0 < players.size()) {
			setToSummon(players.get(argInt0));
		}
		return new Integer[] { 0 };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] summon_all(Context context, Arguments args) {
		this.setSummonAllFlag(true);
		return new Integer[] { 0 };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] get_x(Context context, Arguments args) {
		if (core == null) {
			return null;
		}
		return new Object[] { core.xCoord };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] get_y(Context context, Arguments args) {
		if (core == null) {
			return null;
		}
		return new Object[] { core.yCoord };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] get_z(Context context, Arguments args) {
		if (core == null) {
			return null;
		}
		return new Object[] { core.zCoord };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] get_energy_level(Context context, Arguments args) {
		if (core == null) {
			return null;
		}
		return new Object[] { core.getEnergyStored() };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] do_jump(Context context, Arguments args) {
		doJump();
		return new Integer[] { 0 };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] get_ship_size(Context context, Arguments args) {
		if (core != null) {
			StringBuilder reason = new StringBuilder();
			try {
				if (!core.validateShipSpatialParameters(reason)) {
					core.messageToAllPlayersOnShip(reason.toString());
					return null;
				}
				return new Object[] { core.shipVolume };
			} catch (Exception e) {
				if (WarpDriveConfig.G_DEBUGMODE) {
					e.printStackTrace();
				}
				return null;
			}
		}
		return new Integer[] { 0 };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] set_beacon_frequency(Context context, Arguments args) {
		if ((args.count() != 1) || !args.isString(0)) {
			return new Integer[] { -1 };
		}
		setBeaconFrequency(args.checkString(0));
		return new Integer[] { 0 };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] get_dx(Context context, Arguments args) {
		if (core != null) {
			return new Object[] { core.dx };
		}
		return new Integer[] { 0 };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] get_dz(Context context, Arguments args) {
		if (core != null) {
			return new Object[] { core.dz };
		}
		return new Integer[] { 0 };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] set_core_frequency(Context context, Arguments args) {
		if (args.count() == 1 && (core != null)) {
			core.coreFrequency = (args.checkString(0).replace("/", "").replace(".", "").replace("\\", "."));
		}
		return new Integer[] { 0 };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] is_in_space(Context context, Arguments args) {
		return new Boolean[] { worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] is_in_hyperspace(Context context, Arguments args) {
		return new Boolean[] { worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] set_target_jumpgate(Context context, Arguments args) {
		if (args.count() == 1) {
			setTargetJumpgateName(args.checkString(0));
		}
		return new Integer[] { 0 };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] isAttached(Context context, Arguments args) {
		if (core != null) {
			return new Object[] { (boolean) (core.controller != null) };
		}
		return new Integer[] { 0 };
	}

	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] get_energy_required(Context context, Arguments args) {
		if ((args.count() != 1) || !args.isInteger(0)) {
			return new Integer[] { -1 };
		}
		if (core != null) {
			return new Object[] { (int) (core.calculateRequiredEnergy(getMode(), core.shipVolume, args.checkInteger(0))) };
		}
		return new Integer[] { 0 };
	}
}
