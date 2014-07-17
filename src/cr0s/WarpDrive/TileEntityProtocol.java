package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;

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

    public String[] methodsArray =
    {
        "dim_getp", "dim_setp",                                        // 0, 1
        "dim_getn", "dim_setn",                                        // 2, 3
        "set_mode", "set_distance", "set_direction",                   // 4, 5, 6
        "get_attached_players", "summon", "summon_all",                // 7, 8, 9
        "get_x", "get_y", "get_z",                                     // 10, 11, 12
        "get_energy_level", "do_jump", "get_ship_size",                // 13, 14, 15
        "set_beacon_frequency", "get_dx", "get_dz",                    // 16, 17, 18
        "set_core_frequency", "is_in_space", "is_in_hyperspace",       // 19, 20, 21
        "set_target_jumpgate",                                         // 22
    };

    private int ticks = 0;
    private final int BLOCK_UPDATE_INTERVAL = 20 * 3; // 3 seconds

    private TileEntity core;

    @Override
    public void updateEntity()
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return;
        }

        if (++ticks >= BLOCK_UPDATE_INTERVAL)
        {
            findCoreBlock();

            if (core != null)
            {
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, this.mode, 1 + 2);  // Activated
            }
            else
            {
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);  // Inactive
            }

            ticks = 0;
        }
    }

    private void setJumpDistance(int distance)
    {
        System.out.println("Setting jump distance: " + distance);
        this.distance = distance;
    }

    private void setMode(int mode)
    {
        // System.out.println("Setting mode: " + mode);
        this.mode = mode;
    }

    private void setDirection(int dir)
    {
        if (dir == 1)
        {
            dir = -1;
        }
        else if (dir == 2)
        {
            dir = -2;
        }
        else if (dir == 255)
        {
            dir = 270;
        }

        System.out.println("Setting direction: " + dir);
        this.direction = dir;
    }

    private void doJump()
    {
        if (core != null && core instanceof TileEntityReactor)
        {
            ((TileEntityReactor)core).randomCooldownAddition = worldObj.rand.nextInt(60); // Adding random ticks to cooldown
        }

        setJumpFlag(true);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        setMode(tag.getInteger("mode"));
        setFront(tag.getInteger("front"));
        setRight(tag.getInteger("right"));
        setUp(tag.getInteger("up"));
        setBack(tag.getInteger("back"));
        setLeft(tag.getInteger("left"));
        setDown(tag.getInteger("down"));
        setDistance(tag.getInteger("distance"));
        playersString = tag.getString("players");
        updatePlayersList();
        setBeaconFrequency(tag.getString("bfreq"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
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
        tag.setString("bfreq", getBeaconFrequency());
    }

    public void attachPlayer(EntityPlayer ep)
    {
        for (int i = 0; i < players.size(); i++)
        {
            String nick = players.get(i);

            if (ep.username.equals(nick))
            {
                ep.addChatMessage("[WarpCtrlr] Detached.");
                players.remove(i);
                return;
            }
        }

        ep.attackEntityFrom(DamageSource.generic, 1);
        ep.addChatMessage("[WarpCtrlr] Successfully attached.");
        players.add(ep.username);
        updatePlayersString();
    }

    public void updatePlayersString()
    {
        String nick;
        this.playersString = "";

        for (int i = 0; i < players.size(); i++)
        {
            nick = players.get(i);
            this.playersString += nick + "|";
        }
    }

    public void updatePlayersList()
    {
        String[] playersArray = playersString.split("\\|");

        for (int i = 0; i < playersArray.length; i++)
        {
            String nick = playersArray[i];

            if (!nick.isEmpty())
            {
                players.add(nick);
            }
        }
    }

    public String getAttachedPlayersList()
    {
        String list = "";

        for (int i = 0; i < this.players.size(); i++)
        {
            String nick = this.players.get(i);
            list += nick + ((i == this.players.size() - 1) ? "" : ", ");
        }

        if (players.isEmpty())
        {
            list = "<nobody>";
        }

        return list;
    }

    /**
     * @return the jumpFlag
     */
    public boolean isJumpFlag()
    {
        return jumpFlag;
    }

    /**
     * @param jumpFlag the jumpFlag to set
     */
    public void setJumpFlag(boolean jumpFlag)
    {
        this.jumpFlag = jumpFlag;
    }

    /**
     * @return the front
     */
    public int getFront()
    {
        return front;
    }

    /**
     * @param front the front to set
     */
    public void setFront(int front)
    {
        this.front = front;
    }

    /**
     * @return the right
     */
    public int getRight()
    {
        return right;
    }

    /**
     * @param right the right to set
     */
    public void setRight(int right)
    {
        this.right = right;
    }

    /**
     * @return the up
     */
    public int getUp()
    {
        return up;
    }

    /**
     * @param up the up to set
     */
    public void setUp(int up)
    {
        this.up = up;
    }

    /**
     * @return the back
     */
    public int getBack()
    {
        return back;
    }

    /**
     * @param back the back to set
     */
    public void setBack(int back)
    {
        this.back = back;
    }

    /**
     * @return the left
     */
    public int getLeft()
    {
        return left;
    }

    /**
     * @param left the left to set
     */
    public void setLeft(int left)
    {
        this.left = left;
    }

    /**
     * @return the down
     */
    public int getDown()
    {
        return down;
    }

    /**
     * @param down the down to set
     */
    public void setDown(int down)
    {
        this.down = down;
    }

    public void setDistance(int distance)
    {
        this.distance = distance;
    }

    public int getDistance()
    {
        return this.distance;
    }

    /**
     * @return the mode
     */
    public int getMode()
    {
        return mode;
    }

    /**
     * @return the direction
     */
    public int getDirection()
    {
        return direction;
    }

    /**
     * @return the summonFlag
     */
    public boolean isSummonAllFlag()
    {
        return summonFlag;
    }

    /**
     * @param summonFlag the summonFlag to set
     */
    public void setSummonAllFlag(boolean summonFlag)
    {
        this.summonFlag = summonFlag;
    }

    @Override
    public String getType()
    {
        return "warpcore";
    }

    @Override
    public String[] getMethodNames()
    {
        return (methodsArray);
    }

    @Override
    public boolean canAttachToSide(int side)
    {
        return true;
    }

    @Override
    public void attach(IComputerAccess computer)
    {
    }

    @Override
    public void detach(IComputerAccess computer)
    {
    }

    /**
     * @return the toSummon
     */
    public String getToSummon()
    {
        return toSummon;
    }

    /**
     * @param toSummon the toSummon to set
     */
    public void setToSummon(String toSummon)
    {
        this.toSummon = toSummon;
    }

    /**
     * @return the beaconFrequency
     */
    public String getBeaconFrequency()
    {
        return beaconFrequency;
    }

    /**
     * @param beaconFrequency the beaconFrequency to set
     */
    public void setBeaconFrequency(String beaconFrequency)
    {
        //System.out.println("Setting beacon freqency: " + beaconFrequency);
        this.beaconFrequency = beaconFrequency;
    }

    public TileEntity findCoreBlock()
    {
        this.core = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);

        if (this.core != null && this.core instanceof TileEntityReactor)
        {
            return this.core;
        }

        this.core = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);

        if (this.core != null && this.core instanceof TileEntityReactor)
        {
            return this.core;
        }

        this.core = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);

        if (this.core != null && this.core instanceof TileEntityReactor)
        {
            return this.core;
        }

        this.core = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);

        if (this.core != null && this.core instanceof TileEntityReactor)
        {
            return this.core;
        }

        return null;
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
    {
        //System.out.println("[ProtoBlock] Method " + method + " " + methodsArray[method] + " called!");
        switch (method)
        {
            case 0: // dim_getp ()
                return new Integer[] { getFront(), getRight(), getUp() };
            case 1: // dim_setp (front, right, up)
                if ( arguments.length != 3 ||
					( ( (Double)arguments[0] ).intValue() < 0 ||
					( (Double)arguments[1] ).intValue() < 0 ||
					( (Double)arguments[2] ).intValue() < 0 ||
					( (Double)arguments[1] ).intValue() > WarpDriveConfig.i.WC_MAX_SHIP_SIDE ||
					( (Double)arguments[2] ).intValue() > WarpDriveConfig.i.WC_MAX_SHIP_SIDE ||
					( (Double)arguments[3] ).intValue() > WarpDriveConfig.i.WC_MAX_SHIP_SIDE ) )
                {
                    return new Integer[] { -1 };
                }

                System.out.println("Setting positive gabarits: f: " + ((Double)arguments[0]).intValue() + " r: " + ((Double)arguments[1]).intValue() + " u: " + ((Double)arguments[2]).intValue());
                setFront(((Double)arguments[0]).intValue());
                setRight(((Double)arguments[1]).intValue());
                setUp(((Double)arguments[2]).intValue());
                WarpDrive.instance.registry.removeDeadCores();
                break;

            case 2: // dim_getn ()
                return new Integer[] { getBack(), getLeft(), getDown() };
            case 3: // dim_setn (back, left, down)
                if ( arguments.length != 3 ||
					( ( (Double)arguments[0] ).intValue() < 0 ||
					( (Double)arguments[1] ).intValue() < 0 ||
					( (Double)arguments[2] ).intValue() < 0 ||
					( (Double)arguments[1] ).intValue() > WarpDriveConfig.i.WC_MAX_SHIP_SIDE ||
					( (Double)arguments[2] ).intValue() > WarpDriveConfig.i.WC_MAX_SHIP_SIDE ||
					( (Double)arguments[3] ).intValue() > WarpDriveConfig.i.WC_MAX_SHIP_SIDE ) )
                {
                    return new Integer[] { -1 };
                }

                System.out.println("Setting negative gabarits: b: " + ((Double)arguments[0]).intValue() + " l: " + ((Double)arguments[1]).intValue() + " d: " + ((Double)arguments[2]).intValue());
                setBack(((Double)arguments[0]).intValue());
                setLeft(((Double)arguments[1]).intValue());
                setDown(((Double)arguments[2]).intValue());
                WarpDrive.instance.registry.removeDeadCores();
                break;

            case 4: // set_mode (mode)
                if (arguments.length != 1)
                {
                    return new Integer[] { -1 };
                }

                setMode(((Double)arguments[0]).intValue());
                break;

            case 5: // set_distance (distance)
                if (arguments.length != 1)
                {
                    return new Integer[] { -1 };
                }

                setJumpDistance(((Double)arguments[0]).intValue());
                break;

            case 6: // set_direction (dir)
                if (arguments.length != 1)
                {
                    return new Integer[] { -1 };
                }

                setDirection(((Double)arguments[0]).intValue());
                break;

            case 7: // get_attached_players
                String list = "";

                for (int i = 0; i < this.players.size(); i++)
                {
                    String nick = this.players.get(i);
                    list += nick + ((i == this.players.size() - 1) ? "" : ",");
                }

                if (players.isEmpty())
                {
                    list = "";
                }

                return new Object[] { (String)list };

            case 8: // summon
                if (arguments.length != 1)
                {
                    return new Integer[] { -1 };
                }

                int playerID = ((Double)arguments[0]).intValue();

                if (playerID >= 0 && playerID < players.size())
                {
                    setToSummon(players.get(playerID));
                }

                break;

            case 9: // summon_all
                this.setSummonAllFlag(true);

            case 10: // get_x
                if (core == null)
                {
                    return null;
                }

                return new Object[] { (Integer)core.xCoord };

            case 11: // get_y
                if (core == null)
                {
                    return null;
                }

                return new Object[] { (Integer)core.yCoord };

            case 12: // get_z
                if (core == null)
                {
                    return null;
                }

                return new Object[] { (Integer)core.zCoord };

            case 13: // get_energy_value
                if (core != null)
                {
                    return new Object[] { (Integer)((TileEntityReactor)core).currentEnergyValue };
                }

                return null;

            case 14: // do_jump
                doJump();
                break;

            case 15: // get_ship_size
                if (core != null)
                {
                    ((TileEntityReactor)core).calculateSpatialShipParameters();
                    return new Object[] { (Integer)((TileEntityReactor)core).getRealShipVolume() };
                }

                break;

            case 16: // set_beacon_frequency
                if (arguments.length == 1)
                {
                    setBeaconFrequency((String)arguments[0]);
                }

                break;

            case 17: // get_dx
                if (core != null && core instanceof TileEntityReactor)
                {
                    return new Object[] { (Integer)(((TileEntityReactor)core).dx) };
                }

                break;

            case 18: // get_dz
                if (core != null && core instanceof TileEntityReactor)
                {
                    return new Object[] { (Integer)(((TileEntityReactor)core).dz) };
                }

                break;

            case 19: // set_core_frequency
                if (arguments.length == 1 && (core != null && core instanceof TileEntityReactor))
                {
                    ((TileEntityReactor)core).coreFrequency = ((String)arguments[0]).replace("/", "").replace(".", "").replace("\\", ".");
                }

                break;

            case 20: // is_in_space
                return new Boolean[] { worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID };
            case 21: // is_in_hyperspace
                return new Boolean[] { worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID };
            case 22: // set_target_jumpgate
                if (arguments.length == 1)
                {
                    setTargetJumpgateName((String)arguments[0]);
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
}
