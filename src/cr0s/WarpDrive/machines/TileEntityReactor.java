package cr0s.WarpDrive.machines;

import java.util.List;

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
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.EntityJump;
import cr0s.WarpDrive.JumpGate;
import cr0s.WarpDrive.SpaceTeleporter;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;
/**
 * @author Cr0s
 */
public class TileEntityReactor extends WarpTE
{
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

    private final byte MODE_BASIC_JUMP = 1; // 0-128
    private final byte MODE_LONG_JUMP = 2;  // 0-12800
    private final byte MODE_BEACON_JUMP = 4;     // Jump ship by beacon
    private final byte MODE_HYPERSPACE = 5;      // Jump to Hyperspace
    private final byte MODE_TELEPORT = -1;
    private final byte MODE_GATE_JUMP = 6;       // Jump via jumpgate
    
    int cooldownTime = 0;
    public int randomCooldownAddition = 0;

    private int registryUpdateTicks = 0;
    public String coreFrequency = "default";

    public int isolationBlocksCount = 0;
    public int isolationUpdateTicks = 0;

    public String coreState = "";
    public TileEntityProtocol controller;
    
    public boolean shouldTele = false;

    private boolean soundPlayed = false;

    @Override
    public void updateEntity()
    {

        // Update warp core in cores registry
        if (++registryUpdateTicks > WarpDriveConfig.WC_CORES_REGISTRY_UPDATE_INTERVAL_SECONDS * 20)
        {
            registryUpdateTicks = 0;
            WarpDrive.instance.registry.updateInRegistry(this);
        }

        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return;
        }
        //WarpDrive.debugPrint("Serverside");

        if (++isolationUpdateTicks > WarpDriveConfig.WC_ISOLATION_UPDATE_INTARVAL_SECONDS * 20)
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
        	WarpDrive.debugPrint("No controller?");
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
                	if(controller != null && controller.isJumpFlag())
                	{
                		shouldTele = true;
                		teleportPlayersToSpace();
                		controller.setJumpFlag(false);
                	}
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

                coreState = "Energy level: " + getEnergyStored() + " RF";

                if (controller.isJumpFlag())
                {
                    // Set up activated animation
                    if (worldObj.getBlockMetadata(xCoord, yCoord, zCoord) == 0)
                    {
                        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2); // Set block state to "active"
                        makePlayersOnShipDrunk();
                    }

                    if (!soundPlayed)
                    {
                        worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:warp", 4F, 1F);
                        this.soundPlayed = true;
                    }

                    // Awaiting cooldown time
                    if (/*currentMode != MODE_BASIC_JUMP && */cooldownTime++ < ((WarpDriveConfig.WC_COOLDOWN_INTERVAL_SECONDS) * 20) + randomCooldownAddition)
                    {
                    	//System.out.println("[WC] Awaiting cooldown: " + cooldownTime + " < " + ( ((WarpDriveConfig.WC_COOLDOWN_INTERVAL_SECONDS) * 20) + randomCooldownAddition));
                        return;
                    }

                    cooldownTime = 0;
                    soundPlayed = false;

                    if (!prepareToJump())
                    {
                    	System.out.println("[WC] Prepare to jump returns false");
                        return;
                    }

                    if (WarpDrive.instance.registry.isWarpCoreIntersectsWithOthers(this))
                    {
                    	System.out.println("[WD] Intersect");
                        this.controller.setJumpFlag(false);
                        messageToAllPlayersOnShip("Warp field intersects with other ship's field. Cannot jump.");
                        return;
                    }

                    if (WarpDrive.instance.cloaks.isInCloak(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, false))
                    {
                    	System.out.println("[WD] Core inside cloaking field");
                        this.controller.setJumpFlag(false);
                        messageToAllPlayersOnShip("Wap-Core is inside cloaking field. Can't jump. Disable cloaking field to jump!");
                        return;                    	
                    }
                    
                    WarpDrive.debugPrint("[W-C] Jumping!");
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

            WarpDrive.debugPrint(msg);
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
                    if (worldObj.getBlockId(x, y, z) == WarpDriveConfig.isolationID)
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
    	if (this.removeEnergy(WarpDriveConfig.WC_ENERGY_PER_ENTITY_TO_SPACE, true))
        {
            player.setPositionAndUpdate(x, y, z);

            if (player.dimension != worldObj.provider.dimensionId)
            {
                player.mcServer.getConfigurationManager().transferPlayerToDimension(player, this.worldObj.provider.dimensionId, new SpaceTeleporter(DimensionManager.getWorld(this.worldObj.provider.dimensionId), 0, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ)));
            }
            removeEnergy(WarpDriveConfig.WC_ENERGY_PER_ENTITY_TO_SPACE,false);
        }
    }

    public boolean prepareToJump()
    {
    	if (controller == null)
    		return false;
    	
        this.direction = controller.getDirection();
        this.shipFront = controller.getFront();
        this.shipRight = controller.getRight();
        this.shipUp    = controller.getUp();
        this.shipBack  = controller.getBack();
        this.shipLeft  = controller.getLeft();
        this.shipDown  = controller.getDown();
        this.distance  = Math.min(WarpDriveConfig.WC_MAX_JUMP_DISTANCE, controller.getDistance());
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
                
            default:
            	this.controller.setJumpFlag(false);
				return false;                
        }
        
        // Ship side is too big
        if (shipLength > WarpDriveConfig.WC_MAX_SHIP_SIDE || shipWidth > WarpDriveConfig.WC_MAX_SHIP_SIDE || shipHeight > WarpDriveConfig.WC_MAX_SHIP_SIDE)
        {
            this.controller.setJumpFlag(false);
            return false;
        }

        this.shipVolume = getRealShipVolume();

        if (shipVolume > WarpDriveConfig.WC_MAX_SHIP_VOLUME_ON_SURFACE && worldObj.provider.dimensionId == 0)
        {
            this.controller.setJumpFlag(false);
            return false;
        }

        return true;
    }

    private void doBeaconJump()
    {
    	if(!removeEnergy(calculateRequiredEnergy(shipVolume, distance),true))
        {
            WarpDrive.debugPrint("[WP-TE] Insufficient energy to jump");
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
        	removeEnergy(calculateRequiredEnergy(shipVolume, distance),false);
            WarpDrive.debugPrint("[TE-WC] Moving ship to a beacon (" + beaconX + "; " + yCoord + "; " + beaconZ + ")");
            EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, 1, 0, dx, dz, this);
            jump.setMinMaxes(minX,maxX,minY,maxY,minZ,maxZ);
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
            jump.setIsCoordJump(true);
            jump.setDest(beaconX, yCoord, beaconZ);
            jump.on = true;
            worldObj.spawnEntityInWorld(jump);
            coreState = "";
        }
        else
        {
            WarpDrive.debugPrint("[TE-WC] Beacon not found.");
        }
    }

    private boolean isShipInJumpgate(JumpGate jg)
    {
        AxisAlignedBB aabb = jg.getGateAABB();
        WarpDrive.debugPrint("Gate AABB: " + aabb);
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
            WarpDrive.debugPrint("[GATE] Is 0 blocks inside gate.");
            return false;
        }

        WarpDrive.debugPrint("[GATE] Ship volume: " + shipVolume + ", blocks in gate: " + numBlocks + ". Percentage: " + ((shipVolume / numBlocks) * 100));

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
    	if(!removeEnergy(calculateRequiredEnergy(shipVolume, distance),true))
        {
            WarpDrive.debugPrint("[WP-TE] Insufficient energy to jump");
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

                WarpDrive.debugPrint("[GATE] Place found over " + (10 - numTries) + " tries.");
            }

            // Consume energy
            removeEnergy(calculateRequiredEnergy(shipVolume, distance),false);
            WarpDrive.debugPrint("[TE-WC] Moving ship to a place around gate '" + jg.name + "' (" + destX + "; " + destY + "; " + destZ + ")");
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
            jump.setIsCoordJump(true);
            jump.setDest(destX,destY,destZ);
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
    	if (currentMode == this.MODE_TELEPORT)
    	{
    		WarpDrive.debugPrint("SETTING TELE TO TRU");
    		shouldTele = true;
    	}
    	
        if (currentMode == this.MODE_GATE_JUMP)
        {
            if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
                return;
            }

            WarpDrive.debugPrint("[TE-WC] Performing gate jump...");
            doGateJump();
            return;
        }

        if (currentMode == this.MODE_BEACON_JUMP)
        {
            WarpDrive.debugPrint("[TE-WC] Performing beacon jump...");
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

            
            if (WarpDrive.instance.jumpGates == null) 
            	System.out.println("[JumpGates] WarpDrive.instance.jumpGates is NULL!");
            
            if (WarpDrive.instance.jumpGates != null && t != null && !isShipInJumpgate(t))
            {
                if (shipVolume < WarpDriveConfig.WC_MIN_SHIP_VOLUME_FOR_HYPERSPACE)
                {
                    this.messageToAllPlayersOnShip("Ship is too small (" + shipVolume + "/" + WarpDriveConfig.WC_MIN_SHIP_VOLUME_FOR_HYPERSPACE + "). Insufficient ship mass to open hyperspace portal.");
                    this.controller.setJumpFlag(false);
                    return;
                }
            }
        }

        if (currentMode == this.MODE_BASIC_JUMP || currentMode == this.MODE_LONG_JUMP || currentMode == MODE_HYPERSPACE)
        {
            WarpDrive.debugPrint("[WP-TE] Energy: " + getEnergyStored() + " RF");
            WarpDrive.debugPrint("[WP-TE] Need to jump: " + calculateRequiredEnergy(shipVolume, distance) + " RF");

            if(!removeEnergy(calculateRequiredEnergy(shipVolume, distance),true))
            {
                WarpDrive.debugPrint("[WP-TE] Insufficient energy to jump");
                messageToAllPlayersOnShip("Insufficient energy to jump!");
                this.controller.setJumpFlag(false);
                return;
            }
            removeEnergy(calculateRequiredEnergy(shipVolume, distance),false);
            WarpDrive.debugPrint((new StringBuilder()).append("Jump params: X ").append(minX).append(" -> ").append(maxX).append(" blocks").toString());
            WarpDrive.debugPrint((new StringBuilder()).append("Jump params: Y ").append(minY).append(" -> ").append(maxY).append(" blocks").toString());
            WarpDrive.debugPrint((new StringBuilder()).append("Jump params: Z ").append(minZ).append(" -> ").append(maxZ).append(" blocks").toString());

            //WarpDrive.debugPrint("[WC-TE] Distance: " + distance + "; shipSize: " + shipSize);
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

            WarpDrive.debugPrint((new StringBuilder()).append("[JUMP] Totally moving ").append((new StringBuilder()).append(shipVolume).append(" blocks to length ").append(distance).append(" blocks, direction: ").append(direction).toString()).toString());
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
            jump.setIsCoordJump(false);

            if (currentMode == MODE_HYPERSPACE)
            {
                jump.toHyperSpace = (worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID);
                jump.fromHyperSpace = (worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID);
                WarpDrive.debugPrint("[JUMP] From HS: " + jump.fromHyperSpace + " | To HS: " + jump.fromHyperSpace);
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
    	int destDimensionID = -1;
        if (currentMode == MODE_TELEPORT && worldObj.provider.dimensionId != WarpDrive.instance.spaceDimID && worldObj.provider.dimensionId != WarpDrive.instance.hyperSpaceDimID)
        {
        	destDimensionID = WarpDrive.instance.spaceDimID;
        }
        else if(currentMode == MODE_TELEPORT && worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID)
        {
        	destDimensionID = 0;
        }
        else
        {
        	return ;
        }
        
    	if (shouldTele)
        {
    		WarpDrive.debugPrint("Trying to teleport to Dimension" + destDimensionID);
    		AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(xCoord - 5, yCoord - 5, zCoord - 5, xCoord + 5, yCoord + 5, zCoord + 5);
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
    		for (Object o : list)
            {
    			if(!removeEnergy(WarpDriveConfig.WC_ENERGY_PER_ENTITY_TO_SPACE,false))
                {
                    return;
                }
                Entity entity = (Entity) o;
                int x = MathHelper.floor_double(entity.posX);
                int z = MathHelper.floor_double(entity.posZ);
                //int y = MathHelper.floor_double(entity.posY);
                final int WOOL_BLOCK_ID = 35;
                int newY;

                for (newY = 254; newY > 0; newY--)
                {
                	if(destDimensionID == 0)
                	{
                		if(!DimensionManager.getWorld(0).isAirBlock(x,newY,z))
                			break;
                	}
                	
                	if (DimensionManager.getWorld(destDimensionID).getBlockId(x, newY, z) == WOOL_BLOCK_ID)
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
                    ((EntityPlayerMP) entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), destDimensionID, new SpaceTeleporter(DimensionManager.getWorld(destDimensionID), 0, x, 256, z));
                    WorldServer space = DimensionManager.getWorld(0);

                    ((EntityPlayerMP) entity).setPositionAndUpdate(x, newY + 2, z);
                }
            }
    		shouldTele = false;
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
                WarpDrive.debugPrint("[P] Summoning " + player.username);
                summonPlayer(player, xCoord, yCoord + 2, zCoord);
            }
        }
    }

    public boolean checkPlayerInventory(TileEntityChest chest, EntityPlayerMP player)
    {
        final int MIN_KEY_LENGTH = 5;
        int keyLength = 0;

        for (int index = 0; index < chest.getSizeInventory(); index++)
        {
            ItemStack chestItem = chest.getStackInSlot(index);
            ItemStack playerItem = player.inventory.getStackInSlot(9 + index);

            if (chestItem == null)
            {
                continue;
            }

            if (chestItem.itemID != playerItem.itemID || chestItem.getItemDamage() != playerItem.getItemDamage() || chestItem.stackSize != playerItem.stackSize)
            {
                return false;
            }

            keyLength++;
        }

        if (keyLength < MIN_KEY_LENGTH)
        {
            WarpDrive.debugPrint("[ChestCode] Key is too short: " + keyLength + " < " + MIN_KEY_LENGTH);
            return false;
        }

        return true;
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
                energyValue = (WarpDriveConfig.WC_ENERGY_PER_BLOCK_MODE1 * shipVolume) + (WarpDriveConfig.WC_ENERGY_PER_DISTANCE_MODE1 * jumpDistance);
                break;

            case MODE_LONG_JUMP:
                energyValue = (WarpDriveConfig.WC_ENERGY_PER_BLOCK_MODE2 * shipVolume) + (WarpDriveConfig.WC_ENERGY_PER_DISTANCE_MODE2 * jumpDistance);
                break;

            case MODE_HYPERSPACE:
                energyValue = WarpDriveConfig.WC_MAX_ENERGY_VALUE / 10; // 10% of maximum
                break;

            case MODE_BEACON_JUMP:
                energyValue = WarpDriveConfig.WC_MAX_ENERGY_VALUE / 2;  // half of maximum
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
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        coreFrequency = tag.getString("corefrequency");
        isolationBlocksCount = tag.getInteger("isolation");
        WarpDrive.instance.registry.updateInRegistry(this);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setString("corefrequency", coreFrequency);
        tag.setInteger("isolation", this.isolationBlocksCount);
    }
    
    @Override
    public int getMaxEnergyStored()
    {
    	return WarpDriveConfig.WC_MAX_ENERGY_VALUE;
    }

    @Override
    public void onChunkUnload()
    {
    	super.onChunkUnload();
        WarpDrive.instance.registry.removeFromRegistry(this);
    }

    @Override
    public void validate()
    {
        super.validate();
        WarpDrive.instance.registry.updateInRegistry(this);
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
    }
}
