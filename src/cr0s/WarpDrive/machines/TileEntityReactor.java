package cr0s.WarpDrive.machines;

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
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.*;

/**
 * @author Cr0s
 */
public class TileEntityReactor extends WarpEnergyTE
{
    public Boolean ready;

    public Boolean launchState = false;

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
    public int shipLength;
    public int shipVolume;
    private int currentMode = 0;

    private final byte MODE_TELEPORT = -1;
//    private final byte MODE_IDLE = 0;
    private final byte MODE_BASIC_JUMP = 1; // 0-128
    private final byte MODE_LONG_JUMP = 2;  // 0-12800
    private final byte MODE_BEACON_JUMP = 4;     // Jump ship by beacon
    private final byte MODE_HYPERSPACE = 5;      // Jump to Hyperspace
    private final byte MODE_GATE_JUMP = 6;       // Jump via jumpgate
    
    private int warmupTime = 0;
    private int cooldownTime = 0;
    public int randomWarmupAddition = 0;

    private int chestTeleportUpdateTicks = 0;
    private int registryUpdateTicks = 0;
    public String coreFrequency = "default";

    public int isolationBlocksCount = 0;
    public int isolationUpdateTicks = 0;

    public TileEntityProtocol controller;

    private boolean soundPlayed = false;

    @Override
    public void updateEntity() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }
        super.updateEntity();

        // Always cooldown
        if (cooldownTime > 0) {
        	cooldownTime--;
        	warmupTime = 0;
        }
        
        // Update state
        if (cooldownTime > 0) { // cooling down (2)
        	worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 1 + 2);
        } else if (controller == null) { // not connected (0)
        	worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);
        } else if (controller.isJumpFlag() || this.controller.isSummonAllFlag() || !this.controller.getToSummon().isEmpty()) { // active (1)
        	worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);
        } else { // inactive
        	worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);
        }
        
        // Update warp core in cores registry
        if (++registryUpdateTicks > WarpDriveConfig.WC_CORES_REGISTRY_UPDATE_INTERVAL_SECONDS * 20) {
            registryUpdateTicks = 0;
            WarpDrive.instance.warpCores.updateInRegistry(this);
//            WarpDrive.instance.registry.printRegistry();
            WarpDrive.debugPrint("" + this + " controller is " + controller + ", warmupTime " + warmupTime + ", currentMode " + currentMode + ", jumpFlag " + (controller == null ? "NA" : controller.isJumpFlag()) + ", cooldownTime " + cooldownTime); 

            TileEntity c = findControllerBlock();
            if (c == null) {
            	controller = null;
                warmupTime = 0;
                soundPlayed = false;
                return;
            }
            controller = (TileEntityProtocol)c;
        }

        if (++isolationUpdateTicks > WarpDriveConfig.WC_ISOLATION_UPDATE_INTERVAL_SECONDS * 20) {
            isolationUpdateTicks = 0;
            updateIsolationState();
        }
        
        if (controller == null) {
        	return;
        }

        currentMode = controller.getMode();

        StringBuilder reason = new StringBuilder();
        
        if ((controller.isJumpFlag() && (isolationUpdateTicks == 1)) || this.controller.isSummonAllFlag() || !this.controller.getToSummon().isEmpty()) {
	        if (!validateShipSpatialParameters(reason)) {
	        	if (controller.isJumpFlag()) {
	                controller.setJumpFlag(false);
	                messageToAllPlayersOnShip(reason.toString());
	        	}
	            warmupTime = 0;
	            soundPlayed = false;
	            return;
	        }

	        if (this.controller.isSummonAllFlag()) {
	            summonPlayers();
	            controller.setSummonAllFlag(false);
	        } else if (!this.controller.getToSummon().isEmpty()) {
	            summonSinglePlayer(this.controller.getToSummon());
	            this.controller.setToSummon("");
	        }
        }

        switch (currentMode) {
            case MODE_TELEPORT:
                if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
	                if (isChestSummonMode()) {
	                	chestTeleportUpdateTicks++;
	                    if (chestTeleportUpdateTicks >= 20) {
	                        summonPlayersByChestCode();
	                        chestTeleportUpdateTicks = 0;
	                    }
	                } else {
	                    teleportPlayersToSpace();
	                }
                } else {
                    chestTeleportUpdateTicks = 0;
                }
                break;

            case MODE_BASIC_JUMP:
            case MODE_LONG_JUMP:
            case MODE_BEACON_JUMP:
            case MODE_HYPERSPACE:
            case MODE_GATE_JUMP:
                if (controller.isJumpFlag()) {
                    // Compute warm-up time
                   	int targetCooldown = 0;
                   	switch (currentMode) {
	                    case MODE_BASIC_JUMP:
	                    case MODE_LONG_JUMP:
	                    	if (distance < 50) {
	                    		targetCooldown = (WarpDriveConfig.WC_WARMUP_SHORTJUMP_SECONDS) * 20 / 3;
	                    	} else {
	                    		targetCooldown = (WarpDriveConfig.WC_WARMUP_LONGJUMP_SECONDS) * 20;
	                    	}
	                    	break;
	                    	
	                    case MODE_BEACON_JUMP:
	                    case MODE_HYPERSPACE:
	                    case MODE_GATE_JUMP:
	                    default:
	                    	targetCooldown = (WarpDriveConfig.WC_WARMUP_LONGJUMP_SECONDS) * 20;
	                    	break;
                    }
                   	// Select best sound file and  adjust offset
                	int soundThreshold = 0;
                   	String soundFile = "";
                	if (targetCooldown < 10 * 20) {
                		soundThreshold = targetCooldown - 4 * 20;
                		soundFile = "warpdrive:warp_4s";
                	} else if (targetCooldown > 29 * 20) {
                		soundThreshold = targetCooldown - 30 * 20;
                		soundFile = "warpdrive:warp_30s";
                	} else {
                		soundThreshold = targetCooldown - 10 * 20;
                		soundFile = "warpdrive:warp_10s";	
                	}
                   	// Add random duration
                	soundThreshold += randomWarmupAddition;
                	
                	// Check cooldown time
                	if (cooldownTime > 0) {
                		if (cooldownTime % 20 == 0) {
                    		int seconds = cooldownTime / 20;
                    		if ((seconds < 5) || ((seconds < 30) && (seconds % 5 == 0)) || (seconds % 10 == 0)) {
                				messageToAllPlayersOnShip("Warp core is cooling down... " + cooldownTime / 20 + "s to go...");
                			}
                		}
                        return;
                	}

                    // Set up activated animation
                    if (warmupTime == 0) {
        				messageToAllPlayersOnShip("Running pre-jump checklist...");

                        // update ship parameters
                        if (!validateShipSpatialParameters(reason)) {
                            controller.setJumpFlag(false);
                            messageToAllPlayersOnShip(reason.toString());
                            return;
                        }
                        makePlayersOnShipDrunk(targetCooldown + WarpDriveConfig.WC_WARMUP_RANDOM_TICKS);
                    }

                    if (!soundPlayed && (soundThreshold > warmupTime)) {
                        worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, soundFile, 4F, 1F);
                        soundPlayed = true;
                    }

                    // Awaiting cool-down time
                    if (warmupTime < (targetCooldown + randomWarmupAddition)) {
                    	warmupTime++;
                        return;
                    }

                    warmupTime = 0;
                    soundPlayed = false;
                    
                    if (!validateShipSpatialParameters(reason)) {
                        controller.setJumpFlag(false);
                        messageToAllPlayersOnShip(reason.toString());
                        return;
                    }

                    if (WarpDrive.instance.warpCores.isWarpCoreIntersectsWithOthers(this)) {
                        controller.setJumpFlag(false);
                        messageToAllPlayersOnShip("Warp field intersects with other ship's field. Cannot jump.");
                        return;
                    }

                    if (WarpDrive.instance.cloaks.isInCloak(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, false)) {
                        controller.setJumpFlag(false);
                        messageToAllPlayersOnShip("Core is inside a cloaking field. Aborting. Disable cloaking field to jump!");
                        return;                    	
                    }

                    doJump();
                    cooldownTime = WarpDriveConfig.WC_COOLDOWN_INTERVAL_SECONDS * 20;
                    controller.setJumpFlag(false);
                } else {
                    warmupTime = 0;
                }
                break;
        }
    }

    public void messageToAllPlayersOnShip(String msg) {
        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

        System.out.println("" + (FMLCommonHandler.instance().getEffectiveSide().isClient() ? "Client":"Server") + this + " messageToAllPlayersOnShip: " + msg);
        for (Object o : list) {
            if (o == null || !(o instanceof EntityPlayer)) {
                continue;
            }

            ((EntityPlayer)o).addChatMessage("[WarpCore] " + msg);
        }
    }

    private void updateIsolationState()
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

    private void makePlayersOnShipDrunk(int tickDuration) {
        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

        for (Object o : list) {
            if (o == null || !(o instanceof EntityPlayer)) {
                continue;
            }

            // Set "drunk" effect
            ((EntityPlayer)o).addPotionEffect(new PotionEffect(Potion.confusion.id, tickDuration, 0, true));
        }
    }

    private void summonPlayers()
    {
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

    private void summonSinglePlayer(String nickname)
    {
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

    private void summonPlayer(EntityPlayerMP player, int x, int y, int z) {
        if (consumeEnergy(WarpDriveConfig.WC_ENERGY_PER_ENTITY_TO_SPACE, false)) {
            player.setPositionAndUpdate(x, y, z);

            if (player.dimension != worldObj.provider.dimensionId) {
                player.mcServer.getConfigurationManager().transferPlayerToDimension(player, this.worldObj.provider.dimensionId, new SpaceTeleporter(DimensionManager.getWorld(this.worldObj.provider.dimensionId), 0, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ)));
            }
        }
    }

    public boolean validateShipSpatialParameters(StringBuilder reason) {
    	if (controller == null) {
    		reason.append("TileEntityReactor.validateShipSpatialParameters: no controller detected!");
    		WarpDrive.debugPrint(reason.toString());
    		return false;
    	}
        direction = controller.getDirection();
        shipFront = controller.getFront();
        shipRight = controller.getRight();
        shipUp    = controller.getUp();
        shipBack  = controller.getBack();
        shipLeft  = controller.getLeft();
        shipDown  = controller.getDown();
        distance  = Math.min(WarpDriveConfig.WC_MAX_JUMP_DISTANCE, controller.getDistance());

        int x1 = 0, x2 = 0, z1 = 0, z2 = 0;

        if (Math.abs(dx) > 0) {
            if (dx == 1) {
                x1 = xCoord - shipBack;
                x2 = xCoord + shipFront;
                z1 = zCoord - shipLeft;
                z2 = zCoord + shipRight;
            } else {
                x1 = xCoord - shipFront;
                x2 = xCoord + shipBack;
                z1 = zCoord - shipRight;
                z2 = zCoord + shipLeft;
            }
        } else if (Math.abs(dz) > 0) {
            if (dz == 1) {
                z1 = zCoord - shipBack;
                z2 = zCoord + shipFront;
                x1 = xCoord - shipRight;
                x2 = xCoord + shipLeft;
            } else {
                z1 = zCoord - shipFront;
                z2 = zCoord + shipBack;
                x1 = xCoord - shipLeft;
                x2 = xCoord + shipRight;
            }
        }

        if (x1 < x2) {
            minX = x1;
            maxX = x2;
        } else {
            minX = x2;
            maxX = x1;
        }

        if (z1 < z2) {
            minZ = z1;
            maxZ = z2;
        } else {
            minZ = z2;
            maxZ = z1;
        }

        minY = yCoord - shipDown;
        maxY = yCoord + shipUp;
        shipLength = 0;

        switch (direction) {
            case 0:
            case 180:
            	shipLength = shipBack + shipFront;
                break;

            case 90:
            case 270:
            	shipLength = shipLeft + shipRight;
                break;

            case -1:
            case -2:
            	shipLength = shipDown + shipUp;
                break;
                
           default:
       	   		reason.append("Invalid jump direction " + direction);
        	   	WarpDrive.debugPrint(reason.toString());
				return false;
        }
        
        // Ship side is too big
        if ((shipBack + shipFront) > WarpDriveConfig.WC_MAX_SHIP_SIDE || (shipLeft + shipRight) > WarpDriveConfig.WC_MAX_SHIP_SIDE || (shipDown + shipUp) > WarpDriveConfig.WC_MAX_SHIP_SIDE) {
   	   		reason.append("Ship is too big (max is " + WarpDriveConfig.WC_MAX_SHIP_SIDE + " per side)");
    	   	WarpDrive.debugPrint(reason.toString());
            return false;
        }

        this.shipVolume = getRealShipVolume();

        if (shipVolume > WarpDriveConfig.WC_MAX_SHIP_VOLUME_ON_SURFACE && worldObj.provider.dimensionId == 0) {
   	   		reason.append("Ship is too big for the overworld (max is " + WarpDriveConfig.WC_MAX_SHIP_VOLUME_ON_SURFACE + " blocks)");
    	   	WarpDrive.debugPrint(reason.toString());
            return false;
        }

        return true;
    }

    private void doBeaconJump()
    {
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
        if (isBeaconFound) {
            // Consume energy
            if (consumeEnergy(calculateRequiredEnergy(shipVolume, distance), false)) {
	            System.out.println("" + this + " Moving ship to beacon (" + beaconX + "; " + yCoord + "; " + beaconZ + ")");
	            EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, dx, dz, this, false, 1, 0, true, beaconX, yCoord, beaconZ);
	            jump.maxX = maxX;
	            jump.minX = minX;
	            jump.maxZ = maxZ;
	            jump.minZ = minZ;
	            jump.maxY = maxY;
	            jump.minY = minY;
	            jump.shipLength = shipLength;
	            jump.on = true;
	            worldObj.spawnEntityInWorld(jump);
            } else {
            	messageToAllPlayersOnShip("Insufficient energy level");
            }
        } else {
            System.out.println("" + this + " Beacon '" + freq + "' is unknown.");
        }
    }

    private boolean isShipInJumpgate(JumpGate jg, StringBuilder reason)
    {
        AxisAlignedBB aabb = jg.getGateAABB();
        WarpDrive.debugPrint("[TEWarpCore] Jumpgate " + jg.name + " AABB is " + aabb);
        int countBlocksInside = 0;
        int countBlocksTotal = 0;

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
                	int blockID = worldObj.getBlockId(x, y, z);
                	
                	if (worldObj.isAirBlock(x, y, z) && (blockID != WarpDriveConfig.airID))
                    {
                		continue;
                    }
                    if (aabb.minX <= x && aabb.maxX >=  x && aabb.minY <=  y && aabb.maxY >=  y && aabb.minZ <=  z && aabb.maxZ >=  z)
                    {
                    	countBlocksInside++;
                    }
                    countBlocksTotal++;
                }
            }
        }

        float percent = 0F;
        if (shipVolume != 0)
		{
        	percent = Math.round((((countBlocksInside * 1.0F) / shipVolume) * 100.0F) * 10.0F) / 10.0F;
		}
        if (shipVolume != countBlocksTotal)
        {
        	System.out.println("" + this + " Ship volume has changed from " + shipVolume + " to " + countBlocksTotal + " blocks");
        }
        WarpDrive.debugPrint("Ship has " + countBlocksInside + " / " + shipVolume + " blocks (" + percent + "%) in jumpgate '" + jg.name + "'");
        // At least 80% of ship must be inside jumpgate
        if (percent > 80F)
        {
            return true;
        }
        else if (percent <= 0.001)
        {
        	reason.append("Ship is not inside a jumpgate. Jump rejected. Nearest jumpgate is " + jg.toNiceString());
        	return false;
        }
        else
        {
        	reason.append("Ship is only " + percent + "% inside a jumpgate. Sorry, we'll loose too much crew as is, jump rejected.");
        	return false;
        }
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
        // Search nearest jump-gate
        String gateName = controller.getTargetJumpgateName();
        JumpGate targetGate = WarpDrive.instance.jumpGates.findGateByName(gateName);

        if (targetGate == null)
        {
            messageToAllPlayersOnShip("Destination jumpgate '" + gateName + "' is unknown. Check jumpgate name.");
            this.controller.setJumpFlag(false);
            return;
        }

        // Now make jump to a beacon
        int gateX = targetGate.xCoord;
        int gateY = targetGate.yCoord;
        int gateZ = targetGate.zCoord;
        int destX = gateX;
        int destY = gateY;
        int destZ = gateZ;
        JumpGate nearestGate = WarpDrive.instance.jumpGates.findNearestGate(xCoord, yCoord, zCoord);

		StringBuilder reason = new StringBuilder();
        if (!isShipInJumpgate(nearestGate, reason))
        {
            messageToAllPlayersOnShip(reason.toString());
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
                // randomize destination coordinates around jumpgate
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
                messageToAllPlayersOnShip("Destination gate is blocked by obstacles. Aborting...");
                this.controller.setJumpFlag(false);
                return;
            }

            System.out.println("[GATE] Place found over " + (10 - numTries) + " tries.");
        }

        // Consume energy
        if (consumeEnergy(calculateRequiredEnergy(shipVolume, distance), false)) {
	        System.out.println("[TE-WC] Moving ship to a place around gate '" + targetGate.name + "' (" + destX + "; " + destY + "; " + destZ + ")");
	        EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, dx, dz, this, false, 1, 0, true, destX, destY, destZ);
	        jump.maxX = maxX;
	        jump.minX = minX;
	        jump.maxZ = maxZ;
	        jump.minZ = minZ;
	        jump.maxY = maxY;
	        jump.minY = minY;
	        jump.shipLength = shipLength;
	        jump.on = true;
	        worldObj.spawnEntityInWorld(jump);
        } else {
        	messageToAllPlayersOnShip("Insufficient energy level");
        }
    }

    private void doJump() {
    	int requiredEnergy = calculateRequiredEnergy(shipVolume, distance);

        if (!consumeEnergy(requiredEnergy, true)) {
            messageToAllPlayersOnShip("Insufficient energy to jump! Core is currently charged with " + getEnergyStored() + " EU while jump requires " + requiredEnergy + " EU");
            this.controller.setJumpFlag(false);
            return;
        }

        String shipInfo = "" + shipVolume + " blocks inside (" + minX + ", " + minY + ", " + minZ + ") to (" + maxX + ", " + maxY + ", " + maxZ + ")";
        if (currentMode == this.MODE_GATE_JUMP) {
            System.out.println("" + this + " Performing gate jump of " + shipInfo);
            doGateJump();
            return;
        } else if (currentMode == this.MODE_BEACON_JUMP) {
            System.out.println("" + this + " Performing beacon jump of " + shipInfo);
            doBeaconJump();
            return;
        } else if (currentMode == this.MODE_HYPERSPACE) {
            System.out.println("" + this + " Performing hyperspace jump of " + shipInfo);

        	// Check ship size for hyper-space jump
            if (shipVolume < WarpDriveConfig.WC_MIN_SHIP_VOLUME_FOR_HYPERSPACE) {
	            JumpGate nearestGate = null;
	            if (WarpDrive.instance.jumpGates == null) {
	            	System.out.println("" + this + " WarpDrive.instance.jumpGates is NULL!");
	            } else {
	            	nearestGate = WarpDrive.instance.jumpGates.findNearestGate(xCoord, yCoord, zCoord);
	            }

	            StringBuilder reason = new StringBuilder();
	            if (nearestGate == null || !isShipInJumpgate(nearestGate, reason)) {
                    this.messageToAllPlayersOnShip("Ship is too small (" + shipVolume + "/" + WarpDriveConfig.WC_MIN_SHIP_VOLUME_FOR_HYPERSPACE + "). Insufficient ship mass to open hyperspace portal. Use a jumpgate to reach or exit hyperspace.");
                    this.controller.setJumpFlag(false);
                    return;
                }
            }
        } else if (currentMode == this.MODE_BASIC_JUMP) {
            System.out.println("" + this + " Performing basic jump of " + shipInfo + " toward direction " + direction + " over " + distance + " blocks.");
        } else if (currentMode == this.MODE_LONG_JUMP) {
            System.out.println("" + this + " Performing long jump of " + shipInfo + " toward direction " + direction + " over " + distance + " blocks.");
        } else {
            System.out.println("" + this + " Performing some jump #" + currentMode + " of " + shipInfo);
        }
        
        if (currentMode == this.MODE_BASIC_JUMP || currentMode == this.MODE_LONG_JUMP || currentMode == MODE_HYPERSPACE) {
            if (!consumeEnergy(requiredEnergy, false)) {
            	messageToAllPlayersOnShip("Insufficient energy level");
            	return;
            }

            if (this.currentMode == this.MODE_BASIC_JUMP) {
                distance += shipLength;
            }

            if (currentMode == this.MODE_LONG_JUMP && (direction != -1) && (direction != -2)) {
                if (worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID) {
                    distance *= 100;
                }
            }

            WarpDrive.debugPrint("" + this + " Distance adjusted to " + distance + " blocks.");
            EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, dx, dz, this, (currentMode == MODE_HYPERSPACE), distance, direction, false, 0, 0, 0);
            jump.maxX = maxX;
            jump.minX = minX;
            jump.maxZ = maxZ;
            jump.minZ = minZ;
            jump.maxY = maxY;
            jump.minY = minY;
            jump.shipLength = shipLength;
            jump.on = true;
            worldObj.spawnEntityInWorld(jump);
        }
    }

    private void teleportPlayersToSpace()
    {
        if (worldObj.provider.dimensionId != WarpDriveConfig.G_SPACE_DIMENSION_ID)
        {
            AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(xCoord - 5, yCoord - 5, zCoord - 5, xCoord + 5, yCoord + 5, zCoord + 5);
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

            WorldServer spaceWorld = DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID);
            for (Object o : list) {
                if (!consumeEnergy(WarpDriveConfig.WC_ENERGY_PER_ENTITY_TO_SPACE, false)) {
                    return;
                }

                Entity entity = (Entity) o;
                int x = MathHelper.floor_double(entity.posX);
                int z = MathHelper.floor_double(entity.posZ);
                //int y = MathHelper.floor_double(entity.posY);
                final int WOOL_BLOCK_ID = 35;
                int newY;

                for (newY = 254; newY > 0; newY--) {
                    if (spaceWorld.getBlockId(x, newY, z) == WOOL_BLOCK_ID) {
                        break;
                    }
                }

                if (newY <= 0) {
                    newY = 254;
                }

                if (entity instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), WarpDriveConfig.G_SPACE_DIMENSION_ID, new SpaceTeleporter(DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID), 0, x, 256, z));

                    if (spaceWorld.isAirBlock(x, newY, z)) {
                    	spaceWorld.setBlock(x    , newY, z    , Block.stone.blockID, 0, 2);
                        spaceWorld.setBlock(x + 1, newY, z    , Block.stone.blockID, 0, 2);
                        spaceWorld.setBlock(x - 1, newY, z    , Block.stone.blockID, 0, 2);
                        spaceWorld.setBlock(x    , newY, z + 1, Block.stone.blockID, 0, 2);
                        spaceWorld.setBlock(x    , newY, z - 1, Block.stone.blockID, 0, 2);
                        spaceWorld.setBlock(x + 1, newY, z + 1, Block.stone.blockID, 0, 2);
                        spaceWorld.setBlock(x - 1, newY, z - 1, Block.stone.blockID, 0, 2);
                        spaceWorld.setBlock(x + 1, newY, z - 1, Block.stone.blockID, 0, 2);
                        spaceWorld.setBlock(x - 1, newY, z + 1, Block.stone.blockID, 0, 2);
                    }

                    ((EntityPlayerMP) entity).setPositionAndUpdate(x, newY + 2, z);
                }
            }
        }
    }

    private void summonPlayersByChestCode()
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
                System.out.println("" + this + " Summoning " + player.username);
                summonPlayer(player, xCoord, yCoord + 2, zCoord);
            }
        }
    }

    private boolean checkPlayerInventory(TileEntityChest chest, EntityPlayerMP player)  {
        Boolean result = false;
        final int MIN_KEY_LENGTH = 5;
        int keyLength = 0;

        for (int index = 0; index < chest.getSizeInventory(); index++) {
            ItemStack chestItem = chest.getStackInSlot(index);
            ItemStack playerItem = player.inventory.getStackInSlot(9 + index);

            if (chestItem == null) {
                continue;
            }

            if (playerItem == null || chestItem.itemID != playerItem.itemID || chestItem.getItemDamage() != playerItem.getItemDamage() || chestItem.stackSize != playerItem.stackSize) {
                return false;
            } else {
                result = true;
            }

            keyLength++;
        }

        if (keyLength < MIN_KEY_LENGTH) {
            System.out.println("[ChestCode] Key is too short: " + keyLength + " < " + MIN_KEY_LENGTH);
            return false;
        }

        return result;
    }

    private Boolean isChestSummonMode()
    {
        TileEntity te = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);

        if (te != null)
        {
            return (te instanceof TileEntityChest);
        }

        return false;
    }

    private boolean testBB(AxisAlignedBB axisalignedbb, int x, int y, int z)
    {
        return axisalignedbb.minX <= (double) x && axisalignedbb.maxX >= (double) x && axisalignedbb.minY <= (double) y && axisalignedbb.maxY >= (double) y && axisalignedbb.minZ <= (double) z && axisalignedbb.maxZ >= (double) z;
    }

    @Override
    public String getStatus() {
        return getBlockType().getLocalizedName() + " '" + coreFrequency + "' energy level is " + getEnergyStored() + " EU." + ((cooldownTime <= 0) ? "" : (" " + (cooldownTime / 20) + " s left of cooldown."));
    }

    private int calculateRequiredEnergy(int shipVolume, int jumpDistance)  {
        switch (currentMode) {
        	case MODE_TELEPORT:
        		return WarpDriveConfig.WC_ENERGY_PER_ENTITY_TO_SPACE;

            case MODE_BASIC_JUMP:
            	return (WarpDriveConfig.WC_ENERGY_PER_BLOCK_MODE1 * shipVolume) + (WarpDriveConfig.WC_ENERGY_PER_DISTANCE_MODE1 * jumpDistance);

            case MODE_LONG_JUMP:
            	return (WarpDriveConfig.WC_ENERGY_PER_BLOCK_MODE2 * shipVolume) + (WarpDriveConfig.WC_ENERGY_PER_DISTANCE_MODE2 * jumpDistance);

            case MODE_HYPERSPACE:
            	return WarpDriveConfig.WC_MAX_ENERGY_VALUE / 10; // 10% of maximum

            case MODE_BEACON_JUMP:
            	return WarpDriveConfig.WC_MAX_ENERGY_VALUE / 2;  // half of maximum

            case MODE_GATE_JUMP:
            	return 2 * shipVolume;
        }

        return WarpDriveConfig.WC_MAX_ENERGY_VALUE;
    }

    public int getRealShipVolume() {
        int shipVolume = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    int blockID = worldObj.getBlockId(x, y, z);
                    
                    if (WarpDriveConfig.isAirBlock(worldObj, blockID, x, y, z) && (blockID != WarpDriveConfig.airID)) {
                    	continue;
                    }
                    
                    shipVolume++;
                }
            }
        }

        return shipVolume;
    }

    private TileEntity findControllerBlock()
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
	
    public int getCooldown() {
    	return cooldownTime;
    }
    
    @Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.WC_MAX_ENERGY_VALUE;
	}

    @Override
    public double demandedEnergyUnits() {
        if (this.controller != null && controller.getMode() == 0) {
            return 0.0D;
        }

        return super.demandedEnergyUnits();
    }

    @Override
    public int getMaxSafeInput() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        coreFrequency = tag.getString("corefrequency");
        isolationBlocksCount = tag.getInteger("isolation");
        cooldownTime = tag.getInteger("cooldownTime");
        WarpDrive.instance.warpCores.updateInRegistry(this);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setString("corefrequency", coreFrequency);
        tag.setInteger("isolation", isolationBlocksCount);
        tag.setInteger("cooldownTime", cooldownTime);
    }

    @Override
    public void onChunkUnload() {
        WarpDrive.instance.warpCores.removeFromRegistry(this);
        super.onChunkUnload();
    }

    @Override
    public void validate() {
        super.validate();
        WarpDrive.instance.warpCores.updateInRegistry(this);
    }

    @Override
    public void invalidate() {
        WarpDrive.instance.warpCores.removeFromRegistry(this);
        super.invalidate();
    }
}
