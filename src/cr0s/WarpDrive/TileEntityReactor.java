package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.ForgeDirection;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

/**
 * @author Cr0s
 */
public class TileEntityReactor extends TileEntity implements IEnergySink
{
    public boolean addedToEnergyNet = false;

    public Boolean ready;

    public Boolean invalidAssembly = false;

    public Boolean launchState = false;

    int ticks;

    public final int JUMP_UP = -1;
    public final int JUMP_DOWN = -2;
    int dx, dz;
    int direction;
    int distance;

    public int maxX, maxY, maxZ;
    public int minX, minY, minZ;

    public int shipFront, shipBack;
    public int shipLeft, shipRight;
    public int shipUp, shipDown;
    public int shipHeight, shipWidth, shipLength;
    int shipSize = 0;
    int shipVolume;
    int currentMode = 0;

    int currentEnergyValue = 0;
    int maxEnergyValue = 100000000;

    private final int ENERGY_PER_BLOCK_MODE1 = 10; // eU
    private final int ENERGY_PER_DISTANCE_MODE1 = 100; // eU
    private final int ENERGY_PER_BLOCK_MODE2 = 1000; // eU
    private final int ENERGY_PER_DISTANCE_MODE2 = 1000; // eU
    private final int ENERGY_PER_ENTITY_TO_SPACE = 1000000; // eU
    private final byte MODE_BASIC_JUMP = 1; // 0-128
    private final byte MODE_LONG_JUMP = 2;  // 0-12800
    private final byte MODE_BEACON_JUMP = 4;     // Jump ship by beacon
    private final byte MODE_HYPERSPACE = 5;      // Jump to Hyperspace
    private final byte MODE_TELEPORT = -1;
    private final byte MODE_GATE_JUMP = 6;       // Jump via jumpgate
    private final int MAX_JUMP_DISTANCE = 128;   // Maximum jump length value
    private final int MAX_SHIP_VOLUME_ON_SURFACE = 15000;   // Maximum ship mass to jump on earth (15k blocks)
    private final int MIN_SHIP_VOLUME_FOR_HYPERSPACE = 500; // Minimum ship volume value for

    public final int MAX_SHIP_SIDE = 100;
    int cooldownTime = 0;
    private final int COOLDOWN_INTERVAL_SECONDS = 4;
    public int randomCooldownAddition = 0;

    private final int CORES_REGISTRY_UPDATE_INTERVAL_SECONDS = 10;
    private int registryUpdateTicks = 0;
    public String coreFrequency = "default";

    public int isolationBlocksCount = 0;
    public int isolationUpdateTicks = 0;
    private final int ISOLATION_UPDATE_INTARVAL_SECONDS = 10;

    public String coreState = "";
    public TileEntityProtocol controller;

    private boolean soundPlayed = false;

    @Override
    public void updateEntity()
    {
        if (!FMLCommonHandler.instance().getEffectiveSide().isClient() && !addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }

        // Update warp core in cores registry
        if (++registryUpdateTicks > CORES_REGISTRY_UPDATE_INTERVAL_SECONDS * 20)
        {
            registryUpdateTicks = 0;
            WarpDrive.instance.registry.updateInRegistry(this);
        }

        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return;
        }

        if (++isolationUpdateTicks > ISOLATION_UPDATE_INTARVAL_SECONDS * 20)
        {
            isolationUpdateTicks = 0;
            updateIsolationState();
        }

        TileEntity c = findControllerBlock();

        if (c != null)
        {
            this.controller = (TileEntityProtocol)c;
            this.currentMode = controller.getMode();
            shipFront = controller.getFront();
            shipRight = controller.getRight();
            shipUp = controller.getUp();
            shipBack = controller.getBack();
            shipLeft = controller.getLeft();
            shipDown = controller.getDown();

            if (this.controller.isSummonAllFlag())
            {
                summonPlayers();
                controller.setSummonAllFlag(false);
            }
            else if (!this.controller.getToSummon().isEmpty())
            {
                summonSinglePlayer(this.controller.getToSummon());
                this.controller.setToSummon("");
            }
        }
        else
        {
            invalidAssembly = true;
            return;
        }

        switch (currentMode)
        {
            case MODE_TELEPORT:
                if (isChestSummonMode())
                {
                    if (ticks++ < 20)
                    {
                        return;
                    }

                    if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
                    {
                        summonPlayersByChestCode();
                        ticks = 0;
                    }
                }
                else
                {
                    teleportPlayersToSpace();
                }

                break;

            case MODE_BASIC_JUMP:
            case MODE_LONG_JUMP:
            case MODE_BEACON_JUMP:
            case MODE_HYPERSPACE:
            case MODE_GATE_JUMP:
                if (controller == null)
                {
                    return;
                }

                coreState = "Energy level: " + currentEnergyValue + " Eu";

                if (controller.isJumpFlag())
                {
                    // Set up activated animation
                    if (worldObj.getBlockMetadata(xCoord, yCoord, zCoord) == 0)
                    {
                        // TODO: check for "warpcore turns into dirt" bug
                        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2); // Set block state to "active"
                        makePlayersOnShipDrunk();
                    }

                    if (!soundPlayed)
                    {
                        worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:warp", 4F, 1F);
                        this.soundPlayed = true;
                    }

                    // Awaiting cooldown time
                    if (/*currentMode != MODE_BASIC_JUMP && */cooldownTime++ < ((COOLDOWN_INTERVAL_SECONDS) * 20) + randomCooldownAddition)
                    {
                        return;
                    }

                    cooldownTime = 0;
                    soundPlayed = false;

                    if (!prepareToJump())
                    {
                        return;
                    }

                    if (WarpDrive.instance.registry.isWarpCoreIntersectsWithOthers(this))
                    {
                        this.controller.setJumpFlag(false);
                        messageToAllPlayersOnShip("Warp field intersects with other ship's field. Cannot jump.");
                        return;
                    }

                    System.out.println("[W-C] Jumping!");
                    doJump();
                    controller.setJumpFlag(false);
                }
                else
                {
                    worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2); // Deactivate block animation
                }

                break;
        }
    }

    public void messageToAllPlayersOnShip(String msg)
    {
        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

        for (Object o : list)
        {
            if (o == null || !(o instanceof EntityPlayer))
            {
                continue;
            }

            System.out.println(msg);
            ((EntityPlayer)o).addChatMessage("[WarpCore] " + msg);
        }
    }

    public void updateIsolationState()
    {
        // Search block in cube around core with side 10
        int xmax, ymax, zmax, x1, x2, z1, z2;
        int xmin, ymin, zmin;
        final int ISOLATION_CUBE_SIDE = 6;
        x1 = xCoord + ((ISOLATION_CUBE_SIDE / 2) - 1);
        x2 = xCoord - ((ISOLATION_CUBE_SIDE / 2) - 1);

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

        z1 = zCoord + ((ISOLATION_CUBE_SIDE / 2) - 1);
        z2 = zCoord - ((ISOLATION_CUBE_SIDE / 2) - 1);

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

        ymax = yCoord + ((ISOLATION_CUBE_SIDE / 2) - 1);
        ymin = yCoord - ((ISOLATION_CUBE_SIDE / 2) - 1);
        this.isolationBlocksCount = 0;

        // Search for warp isolation blocks
        for (int y = ymin; y <= ymax; y++)
        {
            for (int x = xmin; x <= xmax; x++)
            {
                for (int z = zmin; z <= zmax; z++)
                {
                    if (worldObj.getBlockId(x, y, z) == WarpDriveConfig.i.isolationID)
                    {
                        this.isolationBlocksCount++;
                    }
                }
            }
        }
    }

    public void makePlayersOnShipDrunk()
    {
        if (!prepareToJump())
        {
            return;
        }

        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

        for (Object o : list)
        {
            if (o == null || !(o instanceof EntityPlayer))
            {
                continue;
            }

            // Set "drunk" effect
            ((EntityPlayer)o).addPotionEffect(new PotionEffect(Potion.confusion.id, 180, 0, true));
        }
    }

    public void summonPlayers()
    {
        calculateSpatialShipParameters();
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);

        for (int i = 0; i < controller.players.size(); i++)
        {
            String nick = controller.players.get(i);
            EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(nick);

            if (player != null && !testBB(aabb, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ)))
            {
                summonPlayer(player, xCoord + dx, yCoord, zCoord + dz);
            }
        }
    }

    public void summonSinglePlayer(String nickname)
    {
        calculateSpatialShipParameters();
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);

        for (int i = 0; i < controller.players.size(); i++)
        {
            String nick = controller.players.get(i);
            EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(nick);

            if (player != null && nick.equals(nickname) && !testBB(aabb, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ)))
            {
                summonPlayer(player, xCoord + dx, yCoord, zCoord + dz);
                return;
            }
        }
    }

    public void summonPlayer(EntityPlayerMP player, int x, int y, int z)
    {
        if (this.currentEnergyValue - this.ENERGY_PER_ENTITY_TO_SPACE >= 0)
        {
            player.setPositionAndUpdate(x, y, z);

            if (player.dimension != worldObj.provider.dimensionId)
            {
                player.mcServer.getConfigurationManager().transferPlayerToDimension(player, this.worldObj.provider.dimensionId, new SpaceTeleporter(DimensionManager.getWorld(this.worldObj.provider.dimensionId), 0, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ)));
            }

            this.currentEnergyValue -= this.ENERGY_PER_ENTITY_TO_SPACE;
        }
    }

    public boolean prepareToJump()
    {
        this.direction = controller.getDirection();
        this.shipFront = controller.getFront();
        this.shipRight = controller.getRight();
        this.shipUp    = controller.getUp();
        this.shipBack  = controller.getBack();
        this.shipLeft  = controller.getLeft();
        this.shipDown  = controller.getDown();
        this.distance  = Math.min(this.MAX_JUMP_DISTANCE, controller.getDistance());
        return calculateSpatialShipParameters();
    }

    public boolean calculateSpatialShipParameters()
    {
        int x1 = 0, x2 = 0, z1 = 0, z2 = 0;

        if (Math.abs(dx) > 0)
        {
            if (dx == 1)
            {
                x1 = xCoord - shipBack;
                x2 = xCoord + shipFront;
                z1 = zCoord - shipLeft;
                z2 = zCoord + shipRight;
            }
            else
            {
                x1 = xCoord - shipFront;
                x2 = xCoord + shipBack;
                z1 = zCoord - shipRight;
                z2 = zCoord + shipLeft;
            }
        }
        else if (Math.abs(dz) > 0)
        {
            if (dz == 1)
            {
                z1 = zCoord - shipBack;
                z2 = zCoord + shipFront;
                x1 = xCoord - shipRight;
                x2 = xCoord + shipLeft;
            }
            else
            {
                z1 = zCoord - shipFront;
                z2 = zCoord + shipBack;
                x1 = xCoord - shipLeft;
                x2 = xCoord + shipRight;
            }
        }

        if (x1 < x2)
        {
            minX = x1;
            maxX = x2;
        }
        else
        {
            minX = x2;
            maxX = x1;
        }

        if (z1 < z2)
        {
            minZ = z1;
            maxZ = z2;
        }
        else
        {
            minZ = z2;
            maxZ = z1;
        }

        minY = yCoord - shipDown;
        maxY = yCoord + shipUp;
        this.shipSize = 0;

        switch (this.direction)
        {
            case 0:
            case 180:
                this.shipSize = this.shipBack + this.shipFront;
                break;

            case 90:
            case 270:
                this.shipSize = this.shipLeft + shipRight;
                break;

            case -1:
            case -2:
                this.shipSize = this.shipDown + this.shipUp;
                break;
        }

        // Ship side is too big
        if (shipLength > MAX_SHIP_SIDE || shipWidth > MAX_SHIP_SIDE || shipHeight > MAX_SHIP_SIDE)
        {
            this.controller.setJumpFlag(false);
            return false;
        }

        this.shipVolume = getRealShipVolume();

        if (shipVolume > MAX_SHIP_VOLUME_ON_SURFACE && worldObj.provider.dimensionId == 0)
        {
            this.controller.setJumpFlag(false);
            return false;
        }

        return true;
    }

    private void doBeaconJump()
    {
        if (currentEnergyValue - calculateRequiredEnergy(shipVolume, distance) < 0)
        {
            System.out.println("[WP-TE] Insufficient energy to jump");
            this.controller.setJumpFlag(false);
            return;
        }

        // Search beacon coordinates
        String freq = controller.getBeaconFrequency();
        int beaconX = 0, beaconZ = 0;
        boolean isBeaconFound = false;
        EntityPlayerMP player;

        for (int i = 0; i < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); i++)
        {
            player = (EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(i);

            // Skip players from other dimensions
            if (player.dimension != worldObj.provider.dimensionId)
            {
                continue;
            }

            TileEntity te = worldObj.getBlockTileEntity(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY) - 1, MathHelper.floor_double(player.posZ));

            if (te != null && (te instanceof TileEntityProtocol))
            {
                if (((TileEntityProtocol)te).getBeaconFrequency().equals(freq))
                {
                    beaconX = te.xCoord;
                    beaconZ = te.zCoord;
                    isBeaconFound = true;
                    break;
                }
            }
        }

        // Now make jump to a beacon
        if (isBeaconFound)
        {
            // Consume all energy
            currentEnergyValue -= calculateRequiredEnergy(shipVolume, distance);
            System.out.println("[TE-WC] Moving ship to a beacon (" + beaconX + "; " + yCoord + "; " + beaconZ + ")");
            EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, 1, 0, dx, dz, this);
            jump.maxX = maxX;
            jump.minX = minX;
            jump.maxZ = maxZ;
            jump.minZ = minZ;
            jump.maxY = maxY;
            jump.minY = minY;
            jump.shipFront = shipFront;
            jump.shipBack = shipBack;
            jump.shipLeft = shipLeft;
            jump.shipRight = shipRight;
            jump.shipUp = shipUp;
            jump.shipDown = shipDown;
            jump.shipLength = this.shipSize;
            jump.xCoord = xCoord;
            jump.yCoord = yCoord;
            jump.zCoord = zCoord;
            jump.isCoordJump = true; // is jump to a beacon
            jump.destX = beaconX;
            jump.destY = yCoord;
            jump.destZ = beaconZ;
            jump.on = true;
            worldObj.spawnEntityInWorld(jump);
            coreState = "";
        }
        else
        {
            System.out.println("[TE-WC] Beacon not found.");
        }
    }

    private boolean isShipInJumpgate(JumpGate jg)
    {
        AxisAlignedBB aabb = jg.getGateAABB();
        System.out.println("Gate AABB: " + aabb);
        int numBlocks = 0;

        if (aabb.isVecInside(worldObj.getWorldVec3Pool().getVecFromPool(maxX - minX, maxY - minY, maxZ - minZ)))
        {
            return true;
        }

        for (int x = minX; x <= maxX; x++)
        {
            for (int z = minZ; z <= maxZ; z++)
            {
                for (int y = minY; y <= maxY; y++)
                {
                    if (!worldObj.isAirBlock(x, y, z))
                    {
                        if (aabb.minX <= x && aabb.maxX >=  x && aabb.minY <=  y && aabb.maxY >=  y && aabb.minZ <=  z && aabb.maxZ >=  z)
                        {
                            numBlocks++;
                        }
                    }
                }
            }
        }

        if (numBlocks == 0)
        {
            System.out.println("[GATE] Is 0 blocks inside gate.");
            return false;
        }

        System.out.println("[GATE] Ship volume: " + shipVolume + ", blocks in gate: " + numBlocks + ". Percentage: " + ((shipVolume / numBlocks) * 100));

        // At least 80% of ship must be inside jumpgate
        if (shipVolume / numBlocks > 0.8F)
        {
            return true;
        }

        return false;
    }

    private boolean isFreePlaceForShip(int destX, int destY, int destZ)
    {
        int newX, newY, newZ;

        if (destY + shipUp > 255 || destY - shipDown < 5)
        {
            return false;
        }

        int moveX = destX - xCoord;
        int moveY = destY - yCoord;
        int moveZ = destZ - zCoord;

        for (int x = minX; x <= maxX; x++)
        {
            for (int z = minZ; z <= maxZ; z++)
            {
                for (int y = minY; y <= maxY; y++)
                {
                    if (!worldObj.isAirBlock(x, y, z))
                    {
                        newX = moveX + x;
                        newY = moveY + y;
                        newZ = moveZ + z;

                        if (!worldObj.isAirBlock(newX, newY, newZ))
                        {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private void doGateJump()
    {
        if (currentEnergyValue - calculateRequiredEnergy(shipVolume, distance) < 0)
        {
            System.out.println("[WP-TE] Insufficient energy to jump");
            this.controller.setJumpFlag(false);
            return;
        }

        // Search beacon coordinates
        String gateName = controller.getTargetJumpgateName();
        JumpGate jg = WarpDrive.instance.jumpGates.findGateByName(gateName);
        int gateX, gateY, gateZ;
        int destX = 0, destY = 0, destZ = 0;
        boolean isGateFound = (jg != null);

        // Now make jump to a beacon
        if (isGateFound)
        {
            gateX = jg.xCoord;
            gateY = jg.yCoord;
            gateZ = jg.zCoord;
            destX = gateX;
            destY = gateY;
            destZ = gateZ;
            JumpGate nearestGate = WarpDrive.instance.jumpGates.findNearestGate(xCoord, yCoord, zCoord);

            if (!isShipInJumpgate(nearestGate))
            {
                messageToAllPlayersOnShip("[GATE] Ship is not inside the jumpgate. Jump rejected. Nearest jumpgate: " + nearestGate.toNiceString());
                this.controller.setJumpFlag(false);
                return;
            }

            // If gate is blocked by obstacle
            if (!isFreePlaceForShip(gateX, gateY, gateZ))
            {
                // Randomize destination coordinates and check for collision with obstacles around jumpgate
                // Try to find good place for ship
                int numTries = 10; // num tries to check for collision
                boolean placeFound = false;

                for (; numTries > 0; numTries--)
                {
                    // randomize dest. coordinates around jumpgate
                    destX = gateX + ((worldObj.rand.nextBoolean()) ? -1 : 1) * (20 + worldObj.rand.nextInt(100));
                    destZ = gateZ + ((worldObj.rand.nextBoolean()) ? -1 : 1) * (20 + worldObj.rand.nextInt(100));
                    destY = gateY + ((worldObj.rand.nextBoolean()) ? -1 : 1) * (20 + worldObj.rand.nextInt(50));

                    // check for collision
                    if (isFreePlaceForShip(destX, destY, destZ))
                    {
                        placeFound = true;
                        break;
                    }
                }

                if (!placeFound)
                {
                    messageToAllPlayersOnShip("[GATE] Destination gate is blocked by obstacles. Cannot jump.");
                    this.controller.setJumpFlag(false);
                    return;
                }

                System.out.println("[GATE] Place found over " + (10 - numTries) + " tries.");
            }

            // Consume energy
            currentEnergyValue -= calculateRequiredEnergy(shipVolume, distance);
            System.out.println("[TE-WC] Moving ship to a place around gate '" + jg.name + "' (" + destX + "; " + destY + "; " + destZ + ")");
            EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, 1, 0, dx, dz, this);
            jump.maxX = maxX;
            jump.minX = minX;
            jump.maxZ = maxZ;
            jump.minZ = minZ;
            jump.maxY = maxY;
            jump.minY = minY;
            jump.shipFront = shipFront;
            jump.shipBack = shipBack;
            jump.shipLeft = shipLeft;
            jump.shipRight = shipRight;
            jump.shipUp = shipUp;
            jump.shipDown = shipDown;
            jump.shipLength = this.shipSize;
            jump.xCoord = xCoord;
            jump.yCoord = yCoord;
            jump.zCoord = zCoord;
            jump.isCoordJump = true;
            jump.destX = destX;
            jump.destY = destY;
            jump.destZ = destZ;
            jump.on = true;
            worldObj.spawnEntityInWorld(jump);
            coreState = "";
        }
        else
        {
            messageToAllPlayersOnShip("[GATE] Destination jumpgate is not found. Check jumpgate name.");
            this.controller.setJumpFlag(false);
        }
    }

    public void doJump()
    {
        if (currentMode == this.MODE_GATE_JUMP)
        {
            if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
                return;
            }

            System.out.println("[TE-WC] Performing gate jump...");
            doGateJump();
            return;
        }

        if (currentMode == this.MODE_BEACON_JUMP)
        {
            System.out.println("[TE-WC] Performing beacon jump...");
            doBeaconJump();
            return;
        }

        // Check ship size for hyperspace jump
        if (currentMode == this.MODE_HYPERSPACE)
        {
            if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
                return;
            }

            JumpGate t = WarpDrive.instance.jumpGates.findNearestGate(xCoord, yCoord, zCoord);

            if (t != null && !isShipInJumpgate(t))
            {
                if (shipVolume < MIN_SHIP_VOLUME_FOR_HYPERSPACE)
                {
                    this.messageToAllPlayersOnShip("Ship is too small (" + shipVolume + "/" + MIN_SHIP_VOLUME_FOR_HYPERSPACE + "). Insufficient ship mass to open hyperspace portal.");
                    this.controller.setJumpFlag(false);
                    return;
                }
            }
        }

        if (currentMode == this.MODE_BASIC_JUMP || currentMode == this.MODE_LONG_JUMP || currentMode == MODE_HYPERSPACE)
        {
            System.out.println("[WP-TE] Energy: " + currentEnergyValue + " eU");
            System.out.println("[WP-TE] Need to jump: " + calculateRequiredEnergy(shipVolume, distance) + " eU");

            if (this.currentEnergyValue - calculateRequiredEnergy(shipVolume, distance) < 0)
            {
                System.out.println("[WP-TE] Insufficient energy to jump");
                this.controller.setJumpFlag(false);
                return;
            }

            this.currentEnergyValue -= calculateRequiredEnergy(shipVolume, distance);
            System.out.println((new StringBuilder()).append("Jump params: X ").append(minX).append(" -> ").append(maxX).append(" blocks").toString());
            System.out.println((new StringBuilder()).append("Jump params: Y ").append(minY).append(" -> ").append(maxY).append(" blocks").toString());
            System.out.println((new StringBuilder()).append("Jump params: Z ").append(minZ).append(" -> ").append(maxZ).append(" blocks").toString());

            //System.out.println("[WC-TE] Distance: " + distance + "; shipSize: " + shipSize);
            if (this.currentMode == this.MODE_BASIC_JUMP)
            {
                distance += shipSize;
            }

            if (currentMode == this.MODE_LONG_JUMP && (direction != -1 && direction != -2))
            {
                if (worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID)
                {
                    distance *= 100;
                }
            }

            System.out.println((new StringBuilder()).append("[JUMP] Totally moving ").append((new StringBuilder()).append(shipVolume).append(" blocks to length ").append(distance).append(" blocks, direction: ").append(direction).toString()).toString());
            EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, distance, direction, dx, dz, this);
            jump.maxX = maxX;
            jump.minX = minX;
            jump.maxZ = maxZ;
            jump.minZ = minZ;
            jump.maxY = maxY;
            jump.minY = minY;
            jump.shipFront = shipFront;
            jump.shipBack = shipBack;
            jump.shipLeft = shipLeft;
            jump.shipRight = shipRight;
            jump.shipUp = shipUp;
            jump.shipDown = shipDown;
            jump.shipLength = this.shipSize;
            jump.isCoordJump = false;

            if (currentMode == MODE_HYPERSPACE)
            {
                jump.toHyperSpace = (worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID);
                jump.fromHyperSpace = (worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID);
                System.out.println("[JUMP] From HS: " + jump.fromHyperSpace + " | To HS: " + jump.fromHyperSpace);
            }

            jump.xCoord = xCoord;
            jump.yCoord = yCoord;
            jump.zCoord = zCoord;
            jump.mode = currentMode;
            jump.on = true;
            worldObj.spawnEntityInWorld(jump);
            coreState = "";
        }
    }

    public void teleportPlayersToSpace()
    {
        if (currentMode == MODE_TELEPORT && worldObj.provider.dimensionId != WarpDrive.instance.spaceDimID)
        {
            if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
            {
                AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(xCoord - 5, yCoord - 5, zCoord - 5, xCoord + 5, yCoord + 5, zCoord + 5);
                List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

                for (Object o : list)
                {
                    if (currentEnergyValue - ENERGY_PER_ENTITY_TO_SPACE < 0)
                    {
                        return;
                    }

                    currentEnergyValue -= ENERGY_PER_ENTITY_TO_SPACE;
                    Entity entity = (Entity) o;
                    int x = MathHelper.floor_double(entity.posX);
                    int z = MathHelper.floor_double(entity.posZ);
                    //int y = MathHelper.floor_double(entity.posY);
                    final int WOOL_BLOCK_ID = 35;
                    int newY;

                    for (newY = 254; newY > 0; newY--)
                    {
                        if (DimensionManager.getWorld(WarpDrive.instance.spaceDimID).getBlockId(x, newY, z) == WOOL_BLOCK_ID)
                        {
                            break;
                        }
                    }

                    if (newY <= 0)
                    {
                        newY = 254;
                    }

                    if (entity instanceof EntityPlayerMP)
                    {
                        ((EntityPlayerMP) entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), WarpDrive.instance.spaceDimID, new SpaceTeleporter(DimensionManager.getWorld(WarpDrive.instance.spaceDimID), 0, x, 256, z));
                        WorldServer space = DimensionManager.getWorld(WarpDrive.instance.spaceDimID);

                        if (space.isAirBlock(x, newY, z))
                        {
                            space.setBlock(x, newY, z, Block.stone.blockID, 0, 2);
                            space.setBlock(x + 1, newY, z, Block.stone.blockID, 0, 2);
                            space.setBlock(x - 1, newY, z, Block.stone.blockID, 0, 2);
                            space.setBlock(x, newY, z + 1, Block.stone.blockID, 0, 2);
                            space.setBlock(x, newY, z - 1, Block.stone.blockID, 0, 2);
                            space.setBlock(x + 1, newY, z + 1, Block.stone.blockID, 0, 2);
                            space.setBlock(x - 1, newY, z - 1, Block.stone.blockID, 0, 2);
                            space.setBlock(x + 1, newY, z - 1, Block.stone.blockID, 0, 2);
                            space.setBlock(x - 1, newY, z + 1, Block.stone.blockID, 0, 2);
                        }

                        ((EntityPlayerMP) entity).setPositionAndUpdate(x, newY + 2, z);
                    }
                }
            }
        }
    }

    public void summonPlayersByChestCode()
    {
        if (worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord) == null)
        {
            return;
        }

        TileEntityChest chest = (TileEntityChest)worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
        EntityPlayerMP player;

        for (int i = 0; i < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); i++)
        {
            player = (EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(i);

            if (checkPlayerInventory(chest, player))
            {
                System.out.println("[P] Summoning " + player.username);
                summonPlayer(player, xCoord, yCoord + 2, zCoord);
            }
        }
    }

    public boolean checkPlayerInventory(TileEntityChest chest, EntityPlayerMP player)
    {
        Boolean result = false;
        final int MIN_KEY_LENGTH = 5;
        int keyLength = 0;

        for (int index = 0; index < chest.getSizeInventory(); index++)
        {
            ItemStack chestItem = chest.getStackInSlot(index);
            ItemStack playerItem = player.inventory.getStackInSlot(9 + index);

            if (chestItem == null || playerItem == null)
            {
                continue;
            }

            if (chestItem.itemID != playerItem.itemID || chestItem.getItemDamage() != playerItem.getItemDamage() || chestItem.stackSize != playerItem.stackSize)
            {
                return false;
            }
            else
            {
                result = true;
            }

            keyLength++;
        }

        if (keyLength < MIN_KEY_LENGTH)
        {
            System.out.println("[ChestCode] Key is too short: " + keyLength + " < " + MIN_KEY_LENGTH);
            return false;
        }

        return result;
    }

    public Boolean isChestSummonMode()
    {
        TileEntity te = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);

        if (te != null)
        {
            return (te instanceof TileEntityChest);
        }

        return false;
    }

    public boolean testBB(AxisAlignedBB axisalignedbb, int x, int y, int z)
    {
        return axisalignedbb.minX <= (double) x && axisalignedbb.maxX >= (double) x && axisalignedbb.minY <= (double) y && axisalignedbb.maxY >= (double) y && axisalignedbb.minZ <= (double) z && axisalignedbb.maxZ >= (double) z;
    }

    public String getCoreState()
    {
        return "[WarpCore] " + this.coreState;
    }

    public int calculateRequiredEnergy(int shipVolume, int jumpDistance)
    {
        int energyValue = 0;

        switch (currentMode)
        {
            case MODE_BASIC_JUMP:
                energyValue = (ENERGY_PER_BLOCK_MODE1 * shipVolume) + (ENERGY_PER_DISTANCE_MODE1 * jumpDistance);
                break;

            case MODE_LONG_JUMP:
                energyValue = (ENERGY_PER_BLOCK_MODE2 * shipVolume) + (ENERGY_PER_DISTANCE_MODE2 * jumpDistance);
                break;

            case MODE_HYPERSPACE:
                energyValue = this.maxEnergyValue / 10; // 10% of maximum
                break;

            case MODE_BEACON_JUMP:
                energyValue = this.maxEnergyValue / 2;  // half of maximum
                break;

            case MODE_GATE_JUMP:
                energyValue = 2 * shipVolume;
        }

        return energyValue;
    }

    public int getRealShipVolume()
    {
        int shipVol = 0;

        for (int x = minX; x <= maxX; x++)
        {
            for (int z = minZ; z <= maxZ; z++)
            {
                for (int y = minY; y <= maxY; y++)
                {
                    int blockID = worldObj.getBlockId(x, y, z);

                    if (blockID != 0)
                    {
                        shipVol++;
                    }
                }
            }
        }

        return shipVol;
    }

    public TileEntity findControllerBlock()
    {
        TileEntity result;
        result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);

        if (result != null && result instanceof TileEntityProtocol)
        {
            dx = 1;
            dz = 0;
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);

        if (result != null && result instanceof TileEntityProtocol)
        {
            dx = -1;
            dz = 0;
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);

        if (result != null && result instanceof TileEntityProtocol)
        {
            dx = 0;
            dz = 1;
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);

        if (result != null && result instanceof TileEntityProtocol)
        {
            dx = 0;
            dz = -1;
            return result;
        }

        return null;
    }

    @Override
    public double demandedEnergyUnits()
    {
        if (this.controller != null && controller.getMode() == 0)
        {
            return 0;
        }

        return (maxEnergyValue - currentEnergyValue);
    }

    @Override
    public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
    {
        double leftover = 0;
        currentEnergyValue += Math.round(amount);

        if (currentEnergyValue > maxEnergyValue)
        {
            leftover = (currentEnergyValue - maxEnergyValue);
            currentEnergyValue = maxEnergyValue;
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

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        currentEnergyValue = tag.getInteger("energy");
        coreFrequency = tag.getString("corefrequency");
        isolationBlocksCount = tag.getInteger("isolation");
        WarpDrive.instance.registry.updateInRegistry(this);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setInteger("energy", currentEnergyValue);
        tag.setString("corefrequency", coreFrequency);
        tag.setInteger("isolation", this.isolationBlocksCount);
    }

    @Override
    public void onChunkUnload()
    {
        if (addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }

        WarpDrive.instance.registry.removeFromRegistry(this);
    }

    @Override
    public void validate()
    {
        super.validate();
        WarpDrive.instance.registry.updateInRegistry(this);
        if (!addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
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
