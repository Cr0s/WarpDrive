package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IPeripheral;
import dan200.turtle.api.ITurtleAccess;
import dan200.turtle.api.TurtleSide;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.DimensionManager;

public class EntityJump extends Entity {

    private int moveX;
    private int moveZ;
    public int xCoord;
    public int yCoord;
    public int zCoord;
    public int distance;
    public int dir;
    public int shipLeft;
    public int shipRight;
    public int shipFront;
    public int shipBack;
    public int shipDown;
    public int shipUp;
    public int shipLength;
    public int maxX;
    public int maxZ;
    public int maxY;
    public int minX;
    public int minZ;
    public int minY;
    public int dx;
    public int dz;
    public World targetWorld;
    // Collision point coordinates
    public int blowX, blowY, blowZ;
    boolean needToExplode = false;
    public boolean on = false;
    public boolean bedrockOnShip = false;
    public JumpBlock ship[];
    public TileEntityReactor reactor;

    boolean isJumping = false;
    int currentIndexInShip = 0;
    
    private final int BLOCKS_PER_TICK = 3000;
    
    private List entityOnShip;
    
    AxisAlignedBB axisalignedbb;
    
    private boolean fromSpace, toSpace;

    int destX, destZ;
    boolean isCoordJump; 
    
    long msCounter = 0;
    
    public EntityJump(World world) {
        super(world);

        targetWorld = worldObj;
    }

    public EntityJump(World world, int x, int y, int z, int _dist, int _direction, int _dx, int _dz, TileEntityReactor parReactor) {
        super(world);

        this.xCoord = x;
        this.posX = (double) x;

        this.yCoord = y;
        this.posY = (double) y;

        this.zCoord = z;
        this.posZ = (double) z;

        this.distance = _dist;
        this.dir = _direction;

        shipLeft = shipRight = shipFront = shipBack = shipDown = shipUp = shipLength = 0;
        this.dx = _dx;
        this.dz = _dz;
        maxX = maxZ = maxY = minX = minZ = minY = 0;

        targetWorld = worldObj;

        System.out.println("[JE] Entity created");
        
        this.reactor = parReactor;
        
        this.isJumping = false;
    }

    public void killEntity(String reason) {
        if (!on) { return; }
        on = false;
        
        System.out.println("[K] Killing jump entity...");
        
        if (!reason.isEmpty()) {
            System.out.println("[JUMP] Killed: " + reason);
        }

        unlockWorlds();
        
        try {
            if (!this.fromSpace && !this.toSpace)  { worldObj.removeEntity(this); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            return;
        if (!on || worldObj.getBlockId(xCoord, yCoord, zCoord) != WarpDrive.WARP_CORE_BLOCKID) {
            unlockWorlds();
            worldObj.removeEntity(this);
            return; 
        }

        if (minY < 0 || maxY > 256) {
            killEntity("Y-coord error!");
            return;
        }

        // Skip tick, awaiting chunk generation
        if ((targetWorld == worldObj) && !checkForChunksGeneratedIn(targetWorld)) {
            return;
        }

        if (!isJumping) {
            this.toSpace   = (dir == -1 && (maxY + distance > 255) && worldObj.provider.dimensionId != WarpDrive.instance.spaceDimID);
            this.fromSpace = (dir == -2 && (minY - distance < 0) && worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID);               

            System.out.println("[JE] Preparing to jump...");
            axisalignedbb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

            prepareToJump();

            isJumping = true;
        } else {
            if (currentIndexInShip >= ship.length-1) {
                isJumping = false;
                finishJump();
            } else { 
                //moveEntities(axisalignedbb, distance, dir, true);
                moveShip();
            }
        }
    }
    
    public void lockWorlds() {
        System.out.println("Locking worlds...");
        targetWorld.isRemote = true;
        
        // When warping between dimensions is need to lock both worlds
        if (targetWorld.provider.dimensionId != worldObj.provider.dimensionId) {
            worldObj.isRemote = true; 
        }
    }
    
    public void unlockWorlds() {
        System.out.println("Unlocking worlds..");
        targetWorld.isRemote = false;
        
        if (targetWorld.provider.dimensionId != worldObj.provider.dimensionId) {
            worldObj.isRemote = false;
        }        
    }
    
    
    /**
     * Setting/removing crap blocks under players to prevent them to fall
     * @param removeBlocks
     */
    public void setBlocksUnderPlayers(boolean removeBlocks) {
        List list = this.entityOnShip;

        if (list != null) {
            for (Object obj : list) {
                if (!(obj instanceof MovingEntity)) {
                    continue;
                }
                
                MovingEntity me = (MovingEntity)obj;
                Entity entity = me.entity;
                
                if (entity instanceof EntityPlayer) {
                    if (!removeBlocks) {
                        mySetBlock(worldObj, (int)me.oldX, (int)me.oldY - 2, (int)me.oldZ, Block.dirt.blockID, 0, 1 + 2);
                    } else
                    {
                        if (worldObj.getBlockId((int)me.oldX, (int)me.oldY - 2, (int)me.oldZ) == Block.dirt.blockID) {
                            mySetBlock(worldObj, (int)me.oldX, (int)me.oldY - 2, (int)me.oldZ, 0, 0, 1 + 2);
                        }
                    }
                }
            }
        }        
    }
    
    /**
     * Check to chunk existence in destination point
     * If chunks not loaded or does not exists, they will
     * @param world
     * @return
     */
    public boolean checkForChunksGeneratedIn(World w) {
        // TODO: ходить не по координатам, а по координатам чанков, так быстрее.
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                final int newX = getNewXCoord(x, 0, z, this.distance, this.dir);
                final int newZ = getNewZCoord(x, 0, z, this.distance, this.dir);

                int chunkX = newX >> 4;
                int chunkZ = newZ >> 4;

                if (!w.getChunkProvider().chunkExists(chunkX, chunkZ)) {
                    messageToAllPlayersOnShip("Generating chunks...");
                    w.getBlockId(newX, 128, newZ);

                    return false;
                }
            }
        }
        return true;
    }
    
    public void messageToAllPlayersOnShip(String msg) {
        List list = this.entityOnShip;

        if (list != null) {
            for (Object obj : list) {
                if (!(obj instanceof MovingEntity)) {
                    continue;
                }

                MovingEntity me = (MovingEntity)obj;
                Entity entity = me.entity;

                if (entity instanceof EntityPlayer) {
                    ((EntityPlayer)entity).sendChatToPlayer("[WarpCore] " + msg);
                }
            }
        }
    }
    
    public void prepareToJump() {
        LocalProfiler.start("EntityJump.prepareToJump");
        boolean betweenWorlds;

        betweenWorlds = fromSpace || toSpace;

        if (toSpace) {
            targetWorld = DimensionManager.getWorld(WarpDrive.instance.spaceDimID);
        } else if (fromSpace) {
            targetWorld = DimensionManager.getWorld(0);
        } else {
            targetWorld = this.worldObj;
        }

        lockWorlds();

        saveEntities(axisalignedbb);
        System.out.println("[JE] Saved " + entityOnShip.size() + " entities from ship");

        if (!isCoordJump) {
            if (dir != -2 && dir != -1) {
                messageToAllPlayersOnShip("Jumping in direction " + dir + " degrees to distance " + distance + " blocks ");
            } else if (dir == -1) {
                messageToAllPlayersOnShip("Jumping UP to distance " + distance + " blocks ");
            } else if (dir == -2) {
                messageToAllPlayersOnShip("Jumping DOWN to distance " + distance + " blocks ");
            }
        } else
        {
            messageToAllPlayersOnShip("Jumping to beacon at (" + destX + "; " + yCoord + "; " + destZ + ")!");
        }
        
        if (!betweenWorlds && !isCoordJump) {
            // Do not check in long jumps
            if (this.distance < 256) {
                distance = getPossibleJumpDistance();
            }
        } else {
            distance = 1;
        }
        
        if (distance <= this.shipLength && !betweenWorlds && !isCoordJump) {
            killEntity("Not enough space for jump.");
            messageToAllPlayersOnShip("Not enough space for jump!");
            LocalProfiler.stop();
            return;
        }

        bedrockOnShip = false;
        int shipSize = getRealShipSize(); // sets bedrockOnShip

        if (bedrockOnShip) {
            killEntity("Bedrock is on the ship. Aborting.");
            messageToAllPlayersOnShip("Bedrock is on the ship. Aborting.");
            LocalProfiler.stop();
            return;
        }

        saveShip(shipSize);
        setBlocksUnderPlayers(false);
        
        this.currentIndexInShip = 0;   
        
        msCounter = System.currentTimeMillis();
        LocalProfiler.stop();
    }

    /**
     * Finish jump: move entities, unlock worlds and delete self
     */
    public void finishJump() {
        moveEntities(axisalignedbb, distance, dir, false);
        setBlocksUnderPlayers(true);

        removeShip();

        System.out.println("[JE] Finished. Jump took " + ((System.currentTimeMillis() - msCounter) / 1000F) + " seconds");

        // Прыжок окончен
        killEntity("");
    }

    /**
     * Removing ship from world
     * 
     */
    public void removeShip() {
        for (JumpBlock jb : ship) {
            if (jb.blockTileEntity != null) {
                worldObj.removeBlockTileEntity(jb.x, jb.y, jb.z);
            }

            //System.out.println("[EJ] Removing block: " + jb.x + " " + jb.y + " " + jb.z + " " + jb.blockID);
            worldObj.setBlockToAir(jb.x, jb.y, jb.z);
        }
    }

    /**
     * Saving ship to memory
     *
     * @param shipSize
     */
    public void saveShip(int shipSize) {
        LocalProfiler.start("EntityJump.saveShip");
        ship = new JumpBlock[shipSize];
        int index = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (ship == null) {
                        killEntity("ship is null!");
                        LocalProfiler.stop();
                        return;
                    }

                    int blockID = worldObj.getBlockId(x, y, z);
                    int blockMeta = worldObj.getBlockMetadata(x, y, z);
                    TileEntity tileentity = worldObj.getBlockTileEntity(x, y, z);

                    // Skip air blocks
                    if (blockID == 0) {
                        continue;
                    }

                    if (tileentity != null) {
                        ship[index] = new JumpBlock(blockID, blockMeta, tileentity, x, y, z);
                    } else {
                        ship[index] = new JumpBlock(blockID, blockMeta, x, y, z);
                    }

                    index++;
                }
            }
        }

        System.out.println((new StringBuilder()).append("[JUMP] Ship saved: ").append((new StringBuilder()).append(ship.length).append(" blocks")).toString());
        LocalProfiler.stop();
    }

    /**
     *Ship moving
     */
    public void moveShip() {
        LocalProfiler.start("EntityJump.moveShip");
        int blocksToMove = Math.min(BLOCKS_PER_TICK, ship.length - currentIndexInShip);

        System.out.println("[JE] Moving ship part: " + currentIndexInShip + "/" + ship.length + " [btm: " + blocksToMove + "]");
        
        for (int index = 0; index < blocksToMove; index++) {
            moveBlockSimple(currentIndexInShip, distance, dir, toSpace, fromSpace);
            currentIndexInShip++;
        }
        LocalProfiler.stop();
    }

    /**
     * Checking jump possibility
     *
     * @return possible jump distance or -1
     */
    public int getPossibleJumpDistance() {
        System.out.println("[JUMP] Calculating possible jump distance...");
        int testDistance = this.distance;
        boolean canJump;

        int blowPoints = 0;

        while (true) {
            // Is place enough in destination point?
            canJump = checkMovement(testDistance);

            if (canJump) {
                break;
            }

            blowPoints++;
            testDistance--;
        }
        
        // Make a explosion in collisoon point
        if (blowPoints > 5 && (this.dir != -1 && this.dir != -2)) {
            messageToAllPlayersOnShip(" [COLLISION] at (" + blowX + "; " + blowY + "; " + blowZ + ")");
            worldObj.createExplosion((Entity) null, blowX, blowY, blowZ, Math.min(4F * 30, 4F * (distance / 2)), true);
        }

        return testDistance;
    }

    /*
     * Получить реальное количество блоков, из которых состоит корабль 
     */
    public int getRealShipSize() {
        LocalProfiler.start("EntityJump.getRealShipSize");
        int shipSize = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    int blockID = worldObj.getBlockId(x, y, z);

                    // Пропускаем пустые блоки воздуха
                    if (blockID != 0) {
                        shipSize++;

                        if (blockID == Block.bedrock.blockID) {
                            bedrockOnShip = true;
                            LocalProfiler.stop();
                            return shipSize;
                        }
                    }
                }
            }
        }

        LocalProfiler.stop();
        return shipSize;
    }

    public void saveEntities(AxisAlignedBB axisalignedbb) {
        this.entityOnShip = new ArrayList();
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
        
        for (Object o : list) {
            if (o == null || !(o instanceof Entity) || (o instanceof EntityJump)) {
                continue;
            }
            
            Entity entity = (Entity)o;

            MovingEntity movingentity = new MovingEntity(entity, entity.posX, entity.posY, entity.posZ);
                        
            // Добавим в список Entity
            entityOnShip.add(movingentity);
        }
    }
    
    /**
     * Перемещение сущностей вместе с кораблем
     * @param axisalignedbb область корабля
     * @param distance расстояние перемещения
     * @param direction направление перемещения
     * @param restorePositions восстановление старых позиций для предотвращения выпадения, либо перемещение на новую
     * @return 
     */
    public boolean moveEntities(AxisAlignedBB axisalignedbb, int distance, int direction, boolean restorePositions) {
        List list = this.entityOnShip;

        if (list != null) {
            for (Object obj : list) {
                if (!(obj instanceof MovingEntity)) {
                    continue;
                }
                
                MovingEntity me = (MovingEntity)obj;
                
                Entity entity = me.entity;
                
                if (me == null) { continue; }
                
                // TODO: пересчитывать всё в вещественных координатах
                int oldEntityX = (int)me.oldX;
                int oldEntityY = (int)me.oldY;
                int oldEntityZ = (int)me.oldZ;

                int newEntityX, newEntityY, newEntityZ;

                newEntityX = oldEntityX;
                newEntityY = oldEntityY;
                newEntityZ = oldEntityZ;                
                
                if (!restorePositions && !toSpace) 
                {
                    if (!fromSpace) 
                    {
                        newEntityX = getNewXCoord(oldEntityX, oldEntityY, oldEntityZ, distance, direction);
                        newEntityY = getNewYCoord(oldEntityX, oldEntityY, oldEntityZ, distance, direction);
                        newEntityZ = getNewZCoord(oldEntityX, oldEntityY, oldEntityZ, distance, direction);    
                    } else {
                        newEntityX = oldEntityX;
                        newEntityY = 255 - this.shipDown - this.shipUp + oldEntityY - this.yCoord -1;
                        newEntityZ = oldEntityZ;                    
                    }
                }
                
                //System.out.println("Entity moving: old (" + oldEntityX + " " + oldEntityY + " " + oldEntityZ + ") -> new (" + newEntityX + " " + newEntityY + " " + newEntityZ);

                if (!(entity instanceof EntityPlayerMP)) {
                    entity.moveEntity(newEntityX, newEntityY, newEntityZ);
                    continue;
                }

                // Если на корабле есть кровать, то передвинуть точку спауна игрока
                ChunkCoordinates bedLocation = ((EntityPlayerMP) entity).getBedLocation();
                if (bedLocation != null && testBB(axisalignedbb, bedLocation.posX, bedLocation.posY, bedLocation.posZ)) {
                    bedLocation.posX = getNewXCoord(bedLocation.posX, bedLocation.posY, bedLocation.posZ, distance, direction);
                    bedLocation.posY = getNewYCoord(bedLocation.posX, bedLocation.posY, bedLocation.posZ, distance, direction);
                    bedLocation.posZ = getNewZCoord(bedLocation.posX, bedLocation.posY, bedLocation.posZ, distance, direction);
                    ((EntityPlayerMP) entity).setSpawnChunk(bedLocation, false);
                }

                ((EntityPlayerMP) entity).setPositionAndUpdate(newEntityX, newEntityY, newEntityZ);
                
                if (restorePositions) { continue; }
                
                if (toSpace) {
                    if (entity instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), WarpDrive.instance.spaceDimID, new SpaceTeleporter(DimensionManager.getWorld(WarpDrive.instance.spaceDimID), 0, MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ)));
                        //((EntityPlayerMP) entity).setPositionAndUpdate(newEntityX, newEntityY, newEntityZ);
                        //if (!((EntityPlayerMP) entity).capabilities.isCreativeMode) {
                        //    ((EntityPlayerMP) entity).capabilities.allowFlying = true;
                        //}
                    } else {
                        entity.travelToDimension(WarpDrive.instance.spaceDimID);
                    }
                } else if (fromSpace) {
                    if (entity instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), 0, new SpaceTeleporter(DimensionManager.getWorld(0), 0, MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ)));
                        //((EntityPlayerMP) entity).setPositionAndUpdate(newEntityX, newEntityY, newEntityZ);
                    } else {
                        entity.travelToDimension(0);
                    }
                }
            }
        }

        return true;
    }

    /**
     * Проверка на вхождение точки в область (bounding-box)
     */
    public boolean testBB(AxisAlignedBB axisalignedbb, int x, int y, int z) {
        return axisalignedbb.minX <= (double) x && axisalignedbb.maxX >= (double) x && axisalignedbb.minY <= (double) y && axisalignedbb.maxY >= (double) y && axisalignedbb.minZ <= (double) z && axisalignedbb.maxZ >= (double) z;
    }

    /**
     * Получение новой координаты X (сдвиг)
     *
     * @param oldX старая координата X
     * @param oldY старая координата Y
     * @param oldZ старая координата Z
     * @param distance расстояние для перемещения
     * @param direction направление пермещения
     */
    public int getNewXCoord(int oldX, int oldY, int oldZ, int distance, int direction) {
        moveX = 0;
        moveZ = 0;
        // System.out.println("old: (" + oldX + "; " + oldZ + ") dis: " + distance + " dir: " + direction);
        int movementVector[] = getVector(direction);
        // System.out.println("Vector: (" + movementVector[0] + "; 0; " + movementVector[2]);

        moveX = movementVector[0] * distance;
        moveZ = movementVector[2] * distance;
        int result = oldX;

        if (direction != -1 && direction != -2) {
            result += moveX;
        }

        if (this.isCoordJump) {
            result = destX + (xCoord - oldX);
        }        

        return result;
    }

    /**
     * Получение новой координаты Y (сдвиг)
     *
     * @param oldX старая координата X
     * @param oldY старая координата Y
     * @param oldZ старая координата Z
     * @param distance расстояние для перемещения
     * @param direction направление пермещения
     */
    public int getNewYCoord(int i, int oldY, int k, int distance, int direction) {
        int result = oldY;

        if (direction == -1 || direction == -2) {
            if (direction == -1) {
                result += distance;
            } else {
                result -= distance;
            }
        }

        if (result >= 255) {
            result = 255;
        }

        if (result <= 0) {
            result = 3;
        }

        return result;
    }

    /**
     * Получение новой координаты Z (сдвиг)
     *
     * @param oldX старая координата X
     * @param oldY старая координата Y
     * @param oldZ старая координата Z
     * @param distance расстояние для перемещения
     * @param direction направление пермещения
     */
    public int getNewZCoord(int oldX, int oldY, int oldZ, int distance, int direction) {
        moveX = 0;
        moveZ = 0;
        // System.out.println("old: (" + oldX + "; " + oldZ + ") dis: " + distance + " dir: " + direction);
        int movementVector[] = getVector(direction);
        // System.out.println("Vector: (" + movementVector[0] + "; 0; " + movementVector[2]);
        moveX = movementVector[0] * distance;
        moveZ = movementVector[2] * distance;
        int result = oldZ;

        if (direction != -1 && direction != -2) {
            result += moveZ;
        }

        if (this.isCoordJump) {
            result = destZ + (zCoord - oldZ);
        }

        return result;
    }

    // Получение вектора в зависимости от направления прыжка
    public int[] getVector(int i) {
        int v[] = {
            0, 0, 0
        };

        switch(i) {
            case -1:
                v[1] = 1;
                break;

            case -2:
                v[1] = -1;
                break;

            case 0:
                v[0] = dx;
                v[2] = dz;
                break;

            case 180:
                v[0] = -dx;
                v[2] = -dz;
                break;

            case 90:
                v[0] = dz;
                v[2] = -dx;
                break;

            case 270:
                v[0] = -dz;
                v[2] = dx;
        }

        return v;
    }

    /**
     * Проверка возможности установки корабля в месте, удалённом от корабля на
     * определённом расстоянии в сторону прыжка
     *
     * @param i
     * @return true, если корабль уместился на новом месте
     */
    public boolean checkMovement(int testDistance) {
        if ((dir == -1 && maxY + testDistance > 255) && !toSpace) {
            System.out.println("[JUMP] Reactor will blow due +high limit");
            return false;
        }

        if ((dir == -2 && minY - testDistance <= 8) && !fromSpace) {
            blowY = minY - testDistance;
            blowX = xCoord;
            blowZ = zCoord;

            System.out.println("[JUMP] Reactor will blow due -low limit");
            return false;
        }

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int newX = getNewXCoord(x, y, z, testDistance, dir);
                    int newY = getNewYCoord(x, y, z, testDistance, dir);
                    int newZ = getNewZCoord(x, y, z, testDistance, dir);

                    if (isBlockInShip(newX, newY, newZ)) {
                        continue;
                    }

                    int blockID = worldObj.getBlockId(newX, newY, newZ);
                    int blockOnShipID = worldObj.getBlockId(x, y, z);

                    if (blockOnShipID == Block.bedrock.blockID) {
                        return false;
                    }

                    if (blockOnShipID != 0 && blockID != 0 /*&& blockID != 9 && blockID != 8*/ && blockID != 18) {
                        blowX = x;
                        blowY = y;
                        blowZ = z;

                        System.out.println((new StringBuilder()).append("[JUMP] Reactor will blow due BlockID ").append((new StringBuilder()).append(blockID).append(" at (").append(newX).append(";").append(newY).append(";").append(newZ).append(")").toString()).toString());
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Точка находится в варп-поле корабля?
     *
     * @param x
     * @param y
     * @param z
     * @return true, если находится
     */
    public boolean isBlockInShip(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public void turnOffModem(IPeripheral p) {
        if (p.getType() == "modem") {
            String[] methods = p.getMethodNames();
            for(int i = 0; i < methods.length; i++) {
                if (methods[i] == "closeAll") {
                    try {
                        p.callMethod(null, i, null);
                    } catch (Exception e) {
                        // ignore iy
                    }
                    return;
                }
            }
        }
    }

    
    /**
     * Перемещение одиночного блока на новое место
     *
     * @param indexInShip индекс блока в сохранённом в памяти корабле
     * @param distance расстояние для перемещения
     * @param direction направление перемещения
     * @return состояние перемещения
     */
    public boolean moveBlockSimple(int indexInShip, int distance, int direction, boolean toSpace, boolean fromSpace) {
        try {
            // OutOfBounds workaround
            if (indexInShip == ship.length) {
                indexInShip--;
            }
            
            JumpBlock shipBlock = ship[indexInShip];

            if (shipBlock == null) {
                return false;
            }

            int oldX = shipBlock.x;
            int oldY = shipBlock.y;
            int oldZ = shipBlock.z;
            
            World spaceWorld = DimensionManager.getWorld(WarpDrive.instance.spaceDimID);
            World surfaceWorld = DimensionManager.getWorld(0);
            int newY, newX, newZ;
            
            if (!toSpace && !fromSpace) 
            {
                newX = getNewXCoord(oldX, oldY, oldZ, distance, direction);
                newY = getNewYCoord(oldX, oldY, oldZ, distance, direction);
                newZ = getNewZCoord(oldX, oldY, oldZ, distance, direction);
            } else {
                // Если прыжок из космоса, то нужно поднять корабль до неба
                distance = 0;
                
                if (fromSpace) {
                    newY =  255 - this.shipDown - this.shipUp + oldY - this.yCoord -1;
                } else {
                    newY = oldY;
                }
                
                newX = oldX;
                newZ = oldZ;
            }
            
            int blockID = shipBlock.blockID;
            int blockMeta = shipBlock.blockMeta;

            if (!toSpace && !fromSpace)
            {
                mySetBlock(worldObj, newX, newY, newZ, blockID, blockMeta, 2);
            } else if (toSpace)
            {
                mySetBlock(spaceWorld, newX, newY, newZ, blockID, blockMeta, 2);
            } else if (fromSpace) {
                mySetBlock(surfaceWorld, newX, newY, newZ, blockID, blockMeta, 2);
            }

            NBTTagCompound oldnbt = new NBTTagCompound();


            if (shipBlock.blockTileEntity != null && blockID != 159 && blockID != 149 && blockID != 156 && blockID != 146 && blockID != 145) {
                // Turn off modems
                if (shipBlock.blockTileEntity instanceof IPeripheral) {
                    IPeripheral p = (IPeripheral)shipBlock.blockTileEntity;
                    turnOffModem(p);
                }
                if (shipBlock.blockTileEntity instanceof ITurtleAccess) {
                    ITurtleAccess a = (ITurtleAccess)shipBlock.blockTileEntity;
                    IPeripheral pl = a.getPeripheral(TurtleSide.Left);
                    if (pl != null) turnOffModem(pl);
                    IPeripheral pr = a.getPeripheral(TurtleSide.Right);
                    if (pr != null) turnOffModem(pr);
                }
                
                shipBlock.blockTileEntity.writeToNBT(oldnbt);
                TileEntity newTileEntity = null;
                // CC's computers and turtles moving workaround
                if (blockID == 1225 || blockID == 1227 || blockID == 1228) {
                    oldnbt.setInteger("x", newX);
                    oldnbt.setInteger("y", newY);
                    oldnbt.setInteger("z", newZ);
                
                    newTileEntity = TileEntity.createAndLoadEntity(oldnbt);
                    newTileEntity.invalidate();
                } else {
                    if (!toSpace && !fromSpace) {
                        newTileEntity = worldObj.getBlockTileEntity(newX, newY, newZ);     
                    } else if (toSpace) {
                        newTileEntity = spaceWorld.getBlockTileEntity(newX, newY, newZ); 
                    } else if (fromSpace) {
                        newTileEntity = surfaceWorld.getBlockTileEntity(newX, newY, newZ); 
                    }
                    
                    if (newTileEntity == null) {
                        System.out.println("[EJ] Error moving tileEntity! TE is null");
                        return false;
                    }
                    
                    newTileEntity.invalidate();
                    
                    newTileEntity.readFromNBT(oldnbt);
                    if (!toSpace && !fromSpace)
                    {
                        worldObj.setBlockTileEntity(newX, newY, newZ, newTileEntity);
                    } else if (toSpace)
                    {
                        //newTileEntity.worldObj = spaceWorld;
                        spaceWorld.setBlockTileEntity(newX, newY, newZ, newTileEntity);
                    } else if (fromSpace) {
                        //newTileEntity.worldObj = surfaceWorld;
                        surfaceWorld.setBlockTileEntity(newX, newY, newZ, newTileEntity);                    
                    }                    
                }

                newTileEntity.worldObj = targetWorld;
                newTileEntity.validate();
                
                if (!toSpace && !fromSpace)
                {
                    worldObj.setBlockTileEntity(newX, newY, newZ, newTileEntity);
                } else if (toSpace)
                {
                    spaceWorld.setBlockTileEntity(newX, newY, newZ, newTileEntity);
                } else if (fromSpace) {
                    surfaceWorld.setBlockTileEntity(newX, newY, newZ, newTileEntity);                    
                }
                
                worldObj.removeBlockTileEntity(oldX, oldY, oldZ);  
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
    }

    @Override
    protected void entityInit() {        

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound var1) {
    }

    // Own implementation of setting blocks withow light recalculation in optimization purposes
    public boolean mySetBlock(World w, int x, int y, int z, int blockId, int blockMeta, int par6)
    {
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000)
        {
            if (y < 0)
            {
                return false;
            }
            else if (y >= 256)
            {
                return false;
            }
            else
            {
                w.markBlockForUpdate(x, y, z);
                Chunk chunk = w.getChunkFromChunkCoords(x >> 4, z >> 4);

                return myChunkSBIDWMT(chunk, x & 15, y, z & 15, blockId, blockMeta);
            }
        }
        else
        {
            return false;
        }
    }
    
    // Incapsulation violation warning:
    // field Chunk.storageArrays has been turned from private to public in class Chunk.java
    public boolean myChunkSBIDWMT(Chunk c, int x, int y, int z, int blockId, int blockMeta)
    {
        int j1 = z << 4 | x;

        if (y >= c.precipitationHeightMap[j1] - 1)
        {
            c.precipitationHeightMap[j1] = -999;
        }

        int k1 = c.heightMap[j1];
        int l1 = c.getBlockID(x, y, z);
        int i2 = c.getBlockMetadata(x, y, z);

        if (l1 == blockId && i2 == blockMeta)
        {
            return false;
        }
        else
        {
            ExtendedBlockStorage extendedblockstorage = c.storageArrays[y >> 4];

            if (extendedblockstorage == null)
            {
                if (blockId == 0)
                {
                    return false;
                }

                extendedblockstorage = c.storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !c.worldObj.provider.hasNoSky);
            }

            int j2 = c.xPosition * 16 + x;
            int k2 = c.zPosition * 16 + z;

            extendedblockstorage.setExtBlockID(x, y & 15, z, blockId);

            if (l1 != 0)
            {
                if (!c.worldObj.isRemote)
                {
                    Block.blocksList[l1].breakBlock(c.worldObj, j2, y, k2, l1, i2);
                }
                else if (Block.blocksList[l1] != null && Block.blocksList[l1].hasTileEntity(i2))
                {
                    TileEntity te = worldObj.getBlockTileEntity(j2, y, k2);
                    if (te != null && te.shouldRefresh(l1, blockId, i2, blockMeta, worldObj, j2, y, k2))
                    {
                        c.worldObj.removeBlockTileEntity(j2, y, k2);
                    }
                }
            }

            if (extendedblockstorage.getExtBlockID(x, y & 15, z) != blockId)
            {
                return false;
            }
            else
            {
                extendedblockstorage.setExtBlockMetadata(x, y & 15, z, blockMeta);

                // Removed light recalcalations
                /*if (flag)
                {
                    c.generateSkylightMap();
                }
                else
                {
                    if (c.getBlockLightOpacity(par1, par2, par3) > 0)
                    {
                        if (par2 >= k1)
                        {
                            c.relightBlock(par1, par2 + 1, par3);
                        }
                    }
                    else if (par2 == k1 - 1)
                    {
                        c.relightBlock(par1, par2, par3);
                    }

                    c.propagateSkylightOcclusion(par1, par3);
                }*/

                TileEntity tileentity;

                if (blockId != 0)
                {
                    if (Block.blocksList[blockId] != null && Block.blocksList[blockId].hasTileEntity(blockMeta))
                    {
                        tileentity = c.getChunkBlockTileEntity(x, y, z);

                        if (tileentity == null)
                        {
                            tileentity = Block.blocksList[blockId].createTileEntity(c.worldObj, blockMeta);
                            c.worldObj.setBlockTileEntity(j2, y, k2, tileentity);
                        }

                        if (tileentity != null)
                        {
                            tileentity.updateContainingBlockInfo();
                            tileentity.blockMetadata = blockMeta;
                        }
                    }
                }

                c.isModified = true;
                return true;
            }
        }
    }    
}