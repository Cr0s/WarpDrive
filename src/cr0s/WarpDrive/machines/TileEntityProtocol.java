package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import cr0s.WarpDrive.*;

/**
 * Protocol block tile entity
 * @author Cr0s
 */
public class TileEntityProtocol extends TileEntity implements IPeripheral
{
    // Variables
    private int distance = 0;
    private int direction = 0;
    private int mode = 0;

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

    public String[] methodsArray = {
        "dim_getp", "dim_setp",										// 0, 1
        "dim_getn", "dim_setn",										// 2, 3
        "set_mode", "set_distance", "set_direction",				// 4, 5, 6
        "get_attached_players", "summon", "summon_all",				// 7, 8, 9
        "get_x", "get_y", "get_z",									// 10, 11, 12
        "get_energy_level", "do_jump", "get_ship_size",				// 13, 14, 15
        "set_beacon_frequency", "get_dx", "get_dz",					// 16, 17, 18
        "set_core_frequency", "is_in_space", "is_in_hyperspace",	// 19, 20, 21
        "set_target_jumpgate",										// 22
        "isAttached"												// 23
    };

    private int ticks = 0;
    private final int BLOCK_UPDATE_INTERVAL = 20 * 3; // 3 seconds

    private TileEntityReactor core = null;

    @Override
    public void updateEntity() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }

        if (++ticks >= BLOCK_UPDATE_INTERVAL) {
            core = findCoreBlock();
            if (core != null) {
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, mode, 1 + 2);  // Activated
            } else {
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);  // Inactive
            }

            ticks = 0;
        }
    }

    private void setJumpDistance(int distance) {
        System.out.println("Setting jump distance: " + distance);
        this.distance = distance;
    }

    private void setMode(int mode) {
        // System.out.println("Setting mode: " + mode);
        this.mode = mode;
    }

    private void setDirection(int dir) {
        if (dir == 1) {
            dir = -1;
        } else if (dir == 2) {
            dir = -2;
        } else if (dir == 255) {
            dir = 270;
        }

        //WarpDrive.debugPrint("" + this + " Setting direction: " + dir);
        this.direction = dir;
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
        tag.setInteger("mode", this.mode);
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
        this.distance = distance;
    }

    public int getDistance()
    {
        return this.distance;
    }

    /**
     * @return the mode
     */
    public int getMode() {
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
    public String getType() {
        return "warpcore";
    }

    @Override
    public String[] getMethodNames() {
        return methodsArray;
    }

    @Override
    public void attach(IComputerAccess computer)  {
        computer.mount("/warpcontroller", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/warpcontroller"));
        computer.mount("/startup", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/warpcontroller/startup"));
    }

    @Override
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
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
    	int argInt0, argInt1, argInt2;
        //WarpDrive.debugPrint("[ProtoBlock] Method " + method + " " + methodsArray[method] + " called!");
        switch (method) {
            case 0: // dim_getp ()
                return new Integer[] { getFront(), getRight(), getUp() };
                
            case 1: // dim_setp (front, right, up)
                if (arguments.length != 3) {
                    return new Integer[] { -1 };
                }
                try {
                	argInt0 = ((Double)arguments[0]).intValue();
                	argInt1 = ((Double)arguments[1]).intValue();
                	argInt2 = ((Double)arguments[2]).intValue();
                } catch(Exception e) {
                	return new Integer[] { -1 };
                }
                if (argInt0 < 0 || argInt1 < 0 || argInt2 < 0) {
                	return new Integer[] { -1 };
                }

                System.out.println("Setting positive gabarits: f: " + argInt0 + " r: " + argInt1 + " u: " + argInt2);
                setFront(((Double)arguments[0]).intValue());
                setRight(((Double)arguments[1]).intValue());
                setUp(((Double)arguments[2]).intValue());
                WarpDrive.instance.warpCores.removeDeadCores();
                break;

            case 2: // dim_getn ()
                return new Integer[] { getBack(), getLeft(), getDown() };
                
            case 3: // dim_setn (back, left, down)
                if (arguments.length != 3) {
                    return new Integer[] { -1 };
                }
                try {
                	argInt0 = ((Double)arguments[0]).intValue();
                	argInt1 = ((Double)arguments[1]).intValue();
                	argInt2 = ((Double)arguments[2]).intValue();
                } catch(Exception e) {
                	return new Integer[] { -1 };
                }
                if (argInt0 < 0 || argInt1 < 0 || argInt2 < 0) {
                	return new Integer[] { -1 };
                }
                
                System.out.println("Setting negative gabarits: b: " + argInt0 + " l: " + argInt1 + " d: " + argInt2);
                setBack(argInt0);
                setLeft(argInt1);
                setDown(argInt2);
                WarpDrive.instance.warpCores.removeDeadCores();
                break;

            case 4: // set_mode (mode)
                if (arguments.length != 1) {
                    return new Integer[] { -1 };
                }
                try {
                	argInt0 = ((Double)arguments[0]).intValue();
                } catch(Exception e) {
                	return new Integer[] { -1 };
                }

                setMode(argInt0);
                break;

            case 5: // set_distance (distance)
                if (arguments.length != 1) {
                    return new Integer[] { -1 };
                }
                try {
                	argInt0 = ((Double)arguments[0]).intValue();
                } catch(Exception e) {
                	return new Integer[] { -1 };
                }

                setJumpDistance(argInt0);
                break;

            case 6: // set_direction (dir)
                if (arguments.length != 1) {
                    return new Integer[] { -1 };
                }
                try {
                	argInt0 = ((Double)arguments[0]).intValue();
                } catch(Exception e) {
                	return new Integer[] { -1 };
                }

                setDirection(argInt0);
                break;

            case 7: // get_attached_players
                String list = "";

                for (int i = 0; i < this.players.size(); i++) {
                    String nick = this.players.get(i);
                    list += nick + ((i == this.players.size() - 1) ? "" : ",");
                }

                if (players.isEmpty()) {
                    list = "";
                }

                return new Object[] { (String)list };

            case 8: // summon
                if (arguments.length != 1) {
                    return new Integer[] { -1 };
                }
                try {
                	argInt0 = ((Double)arguments[0]).intValue();
                } catch(Exception e) {
                	return new Integer[] { -1 };
                }

                if (argInt0 >= 0 && argInt0 < players.size()) {
                    setToSummon(players.get(argInt0));
                }
                break;

            case 9: // summon_all
                this.setSummonAllFlag(true);
                break;

            case 10: // get_x
                if (core == null) {
                    return null;
                }

                return new Object[] { (Integer)core.xCoord };

            case 11: // get_y
                if (core == null) {
                    return null;
                }

                return new Object[] { (Integer)core.yCoord };

            case 12: // get_z
                if (core == null) {
                    return null;
                }

                return new Object[] { (Integer)core.zCoord };

            case 13: // get_energy_value
                if (core == null) {
                	return null;
                }

                return new Object[] { (Integer)(core.getEnergyStored() ) };

            case 14: // do_jump
                doJump();
                break;

            case 15: // get_ship_size
                if (core != null) {
                	StringBuilder reason = new StringBuilder();
                	try {
	                    if (!core.validateShipSpatialParameters(reason)) {
	                    	core.messageToAllPlayersOnShip(reason.toString());
	                    	return null;
	                    }
	                    return new Object[] { (Integer)core.getRealShipVolume() };
                	} catch(Exception e) {
                		if (WarpDriveConfig.debugMode) {
                			e.printStackTrace();
                		}
                		return null; 
                	}
                }
                break;

            case 16: // set_beacon_frequency
                if (arguments.length != 1) {
                    return new Integer[] { -1 };
                }

                setBeaconFrequency((String)arguments[0]);
                break;

            case 17: // get_dx
                if (core != null && core instanceof TileEntityReactor) {
                    return new Object[] { (Integer)core.dx };
                }
                break;

            case 18: // get_dz
                if (core != null && core instanceof TileEntityReactor) {
                    return new Object[] { (Integer)core.dz };
                }
                break;

            case 19: // set_core_frequency
                if (arguments.length == 1 && (core != null)) {
                    core.coreFrequency = ((String)arguments[0]).replace("/", "").replace(".", "").replace("\\", ".");
                }
                break;

            case 20: // is_in_space
                return new Boolean[] { worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID };
                
            case 21: // is_in_hyperspace
                return new Boolean[] { worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID };
                
            case 22: // set_target_jumpgate
                if (arguments.length == 1) {
                    setTargetJumpgateName((String)arguments[0]);
                }
                break;
                
            case 23: // isAttached
                if (core != null) {
                	return new Object[] { (boolean)(core.controller != null) };
                }
                break;
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
	public boolean equals(IPeripheral other) {
		// TODO Auto-generated method stub
		return false;
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
