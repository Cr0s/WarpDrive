package shipmod;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraftforge.common.Configuration;

public class ShipModConfig
{
    private static int[] defaultForbiddenBlocks = new int[] {Block.dirt.blockID, Block.grass.blockID, Block.sand.blockID, Block.gravel.blockID, Block.blockClay.blockID, Block.ice.blockID, Block.waterMoving.blockID, Block.waterStill.blockID, Block.lavaMoving.blockID, Block.lavaStill.blockID, Block.snow.blockID, Block.waterlily.blockID, Block.netherrack.blockID, Block.slowSand.blockID, Block.tallGrass.blockID};
    private static int[] defaultOverwritableBlocks = new int[] {Block.tallGrass.blockID, Block.waterlily.blockID};
    public static final int CONTROL_TYPE_VANILLA = 0;
    public static final int CONTROL_TYPE_ARCHIMEDES = 1;
    private Configuration config;
    private Map<String, Object> materialStringMap;
    public boolean enableAirShips = true;
    public int shipEntitySyncRate;
    public int maxShipChunkBlocks;
    public float flyBalloonRatio = 0f;
    public boolean connectDiagonalBlocks1;
    public Set<Integer> forbiddenBlocks;
    public Set<Integer> overwritableBlocks;
    public int shipControlType;
    public boolean remountOnDecompilationFail;
    public float turnSpeed;
    public float speedLimit;
    public boolean enableRightClickDismount;
    public int itemCreateVehicleID;
    public int blockMarkShipID;
    public int blockFloaterID;
    public int blockBalloonID;
    public int blockGaugeID;

    public ShipModConfig(Configuration configuration)
    {
        this.config = configuration;
        this.forbiddenBlocks = new HashSet();
        this.overwritableBlocks = new HashSet();
    }

    public void loadAndSave()
    {
        this.config.load();
        this.shipEntitySyncRate = this.config.get("settings", "sync_rate", 20, "The amount of ticks between a server-client synchronization. Higher numbers reduce network traffic. Lower numbers increase multiplayer experience. 20 ticks = 1 second").getInt();
        this.shipControlType = this.config.get("control", "control_type", 1, "Set to 0 to use vanilla boat controls, set to 1 to use the new controls.").getInt();
        this.remountOnDecompilationFail = this.config.get("control", "remount_on_decomp_fail", false, "Set to \'true\' to automatically remount a ship if decompilation failed.").getBoolean(false);
        this.turnSpeed = (float)this.config.get("control", "turn_speed", 1.0D, "A multiplier of the ship\'s turn speed.").getDouble(1.0D);
        this.speedLimit = (float)this.config.get("control", "speed_limit", 30.0D, "The maximum velocity a ship can have, in meter per second. This does not affect acceleration.").getDouble(30.0D);
        this.speedLimit /= 20.0F;
        this.enableRightClickDismount = this.config.get("control", "enable_right_click_dismount", false, "Enable if right clicking on the ship should also dismount you.").getBoolean(false);
        this.maxShipChunkBlocks = this.config.get("mobile_chunk", "max_chunk_blocks", 2048, "The maximum amount of blocks that a mobile ship chunk may contain, limited to a maximum of 3200 blocks").getInt();
        this.maxShipChunkBlocks = Math.min(this.maxShipChunkBlocks, 3400);
        this.connectDiagonalBlocks1 = this.config.get("mobile_chunk", "connect_diagonal_blocks_1", false, "Blocks connected diagonally on one axis will also be added to the ship when this value is set to \'true\'.").getBoolean(false);
        int[] forbiddenblocks = this.config.get("mobile_chunk", "forbidden_blocks", defaultForbiddenBlocks, "A list of blocks that will not be added to a ship.").getIntList();
        int[] overwritableblocks = this.config.get("mobile_chunk", "overwritable_blocks", defaultOverwritableBlocks, "A list of blocks that may be overwritten when decompiling a ship.").getIntList();
        int[] arr$ = forbiddenblocks;
        int len$ = forbiddenblocks.length;
        int i$;
        int i;

        for (i$ = 0; i$ < len$; ++i$)
        {
            i = arr$[i$];
            this.forbiddenBlocks.add(Integer.valueOf(i));
        }

        arr$ = overwritableblocks;
        len$ = overwritableblocks.length;

        for (i$ = 0; i$ < len$; ++i$)
        {
            i = arr$[i$];
            this.overwritableBlocks.add(Integer.valueOf(i));
        }

        this.blockMarkShipID = this.config.getBlock("mark_ship", 3611).getInt();
        this.blockGaugeID = this.config.getBlock("gauge", 3614).getInt();
        this.config.save();
    }
}
