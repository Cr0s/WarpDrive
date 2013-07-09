package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IPeripheral;
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
    public int Xmax;
    public int Zmax;
    public int maxY;
    public int Xmin;
    public int Zmin;
    public int minY;
    public int dx;
    public int dz;
    // Collision point coordinates
    public int blowX, blowY, blowZ;
    boolean needToExplode = false;
    public boolean on = false;
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
        Xmax = Zmax = maxY = Xmin = Zmin = minY = 0;

        
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
        if ((getTargetWorld().provider.dimensionId == worldObj.provider.dimensionId) && !checkForChunksGeneratedIn(getTargetWorld())) {
            return;
        }

        if (!isJumping) {
            this.toSpace   = (dir == -1 && (maxY + distance > 255) && worldObj.provider.dimensionId != WarpDrive.instance.spaceDimID);
            this.fromSpace = (dir == -2 && (minY - distance < 0) && worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID);               

            System.out.println("[JE] Preparing to jump...");
            axisalignedbb = AxisAlignedBB.getBoundingBox(Xmin, minY, Zmin, Xmax, maxY, Zmax);

            prepareToJump();

            isJumping = true;
        } else {
            if (currentIndexInShip >= ship.length-1) {
                isJumping = false;
                finishJump();
            } else { 
                moveEntitys(axisalignedbb, distance, dir, true);
                moveShip();
            }
        }
    }
    
    public World getTargetWorld() {        
        if (toSpace) {
            return DimensionManager.getWorld(WarpDrive.instance.spaceDimID);
        } else if (fromSpace) {
            return DimensionManager.getWorld(0);
        } else {
            return this.worldObj;
        }
    }
    
    public void lockWorlds() {
        System.out.println("Locking worlds...");
        getTargetWorld().isRemote = true;
        
        // When warping between dimensions is need to lock both worlds
        if (getTargetWorld().provider.dimensionId != worldObj.provider.dimensionId) {
            worldObj.isRemote = true; 
        }
    }
    
    public void unlockWorlds() {
        System.out.println("Unlocking worlds..");
        getTargetWorld().isRemote = false;
        
        if (getTargetWorld().provider.dimensionId != worldObj.provider.dimensionId) {
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
                        mySetBlock(worldObj, me.oldX, me.oldY - 2, me.oldZ, Block.dirt.blockID, 0, 1 + 2);
                    } else
                    {
                        if (worldObj.getBlockId(me.oldX, me.oldY - 2, me.oldZ) == Block.dirt.blockID) {
                            mySetBlock(worldObj, me.oldX, me.oldY - 2, me.oldZ, 0, 0, 1 + 2);
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
        for (int y = minY; y <= maxY; y++) {
            for (int x = Xmin; x <= Xmax; x++) {
                for (int z = Zmin; z <= Zmax; z++) {
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
        boolean betweenWorlds;
        
        lockWorlds();

        betweenWorlds = fromSpace || toSpace;
        
        saveEntitys(axisalignedbb);
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
            return;
        }

        if (!checkForBedrockOnShip()) {
            killEntity("Is bedrock on the ship. Aborting.");
            messageToAllPlayersOnShip("Is bedrock on the ship. Aborting.");
            return;
        }

        int shipSize = getRealShipSize();
        saveShip(shipSize);
        setBlocksUnderPlayers(false);
        
        this.currentIndexInShip = 0;   
        
        msCounter = System.currentTimeMillis();
    }

    /**
     * Finish jump: move entities, unlock worlds and delete self
     */
    public void finishJump() {
        moveEntitys(axisalignedbb, distance, dir, false);
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
     * @param deleteShip
     */
    public void saveShip(int shipSize) {
        ship = new JumpBlock[shipSize];
        int index = 0;

        for (int y = minY; y <= maxY; y++) {
            for (int x = Xmin; x <= Xmax; x++) {
                for (int z = Zmin; z <= Zmax; z++) {
                    if (ship == null) {
                        killEntity("ship is null!");
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
    }
    
    /**
     *Ship moving
     */
    public void moveShip() {
        int blocksToMove = Math.min(BLOCKS_PER_TICK, ship.length - this.currentIndexInShip);

        System.out.println("[JE] Moving ship part: " + currentIndexInShip + "/" + ship.length + " [btm: " + blocksToMove + "]");
        
        // 1. Jump to space
        if (toSpace) {
            for (int index = 0; index < blocksToMove; index++) {
                moveBlockToSpace(currentIndexInShip, distance, dir);
                this.currentIndexInShip++;
            }
        // 2. Jump from space
        } else if (fromSpace) {
            for (int index = 0; index < blocksToMove; index++) {
                moveBlockFromSpace(currentIndexInShip, distance, dir);
                this.currentIndexInShip++;
            }
        // 3. Basic jump
        } else {            
            for (int index = 0; index < blocksToMove; index++) {
                moveBlock(currentIndexInShip, distance, dir);
                this.currentIndexInShip++;
            }
        }
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

    /**
     * Check for frobidden blocks on ship (bedrock)
     *
     */
    public boolean checkForBedrockOnShip() {
        for (int y = minY; y <= maxY; y++) {
            for (int x = Xmin; x <= Xmax; x++) {
                for (int z = Zmin; z <= Zmax; z++) {
                    int blockID = worldObj.getBlockId(x, y, z);

                    if (blockID == 0) {
                        continue;
                    }

                    if (blockID == Block.bedrock.blockID) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /*
     * Получить реальное количество блоков, из которых состоит корабль 
     */
    public int getRealShipSize() {
        int shipSize = 0;

        for (int y = minY; y <= maxY; y++) {
            for (int x = Xmin; x <= Xmax; x++) {
                for (int z = Zmin; z <= Zmax; z++) {
                    int blockID = worldObj.getBlockId(x, y, z);

                    // Пропускаем пустые блоки воздуха
                    if (blockID != 0) {
                        shipSize++;
                    }
                }
            }
        }

        return shipSize;
    }

    public void saveEntitys(AxisAlignedBB axisalignedbb) {
        this.entityOnShip = new ArrayList();
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
        
        for (Object o : list) {
            if (o == null || !(o instanceof Entity) || (o instanceof EntityJump)) {
                continue;
            }
            
            Entity entity = (Entity)o;

            MovingEntity movingentity = new MovingEntity(entity, MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
                        
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
    public boolean moveEntitys(AxisAlignedBB axisalignedbb, int distance, int direction, boolean restorePositions) {
        List list = this.entityOnShip;

        if (list != null) {
            for (Object obj : list) {
                if (!(obj instanceof MovingEntity)) {
                    continue;
                }
                
                MovingEntity me = (MovingEntity)obj;
                
                Entity entity = me.entity;
                
                if (me == null) { continue; }
                
                int oldEntityX = me.oldX;
                int oldEntityY = me.oldY;
                int oldEntityZ = me.oldZ;

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

    public int rotsincos(int i, boolean flag) {
        // sin
        if (flag) {
            switch (i) {
                case 0:
                    return 0;

                case 90:
                    return 1;

                case 180:
                    return 0;

                case 270:
                    return -1;
            }
        } // cos
        else {
            switch (i) {
                case 0:
                    return 1;

                case 90:
                    return 0;

                case 180:
                    return -1;

                case 270:
                    return 0;
            }
        }

        return 0;
    }

    // Получение вектора в зависимости от направления прыжка
    // (3,14здец, конечно)
    public int[] getVector(int i) {
        int ai[] = {
            0, 0, 0
        };

        if (dz == 1) {
            switch (i) {
                case 0:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = 1;
                    break;

                case 90:
                    ai[0] = 1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 180:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = -1;
                    break;

                case 270:
                    ai[0] = -1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;
            }
        } else if (dz == -1) {
            switch (i) {
                case 0:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = -1;
                    break;

                case 90:
                    ai[0] = -1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 180:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = 1;
                    break;

                case 270:
                    ai[0] = 1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;
            }
        } else if (dx == 1) {
            switch (i) {
                case 0:
                    ai[0] = 1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 90:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = -1;
                    break;

                case 180:
                    ai[0] = -1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 270:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = 1;
                    break;
            }
        } else if (dx == -1) {
            switch (i) {
                case 0:
                    ai[0] = -1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 90:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = 1;
                    break;

                case 180:
                    ai[0] = 1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 270:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = -1;
                    break;
            }
        }

        return ai;
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
            for (int x = Xmin; x <= Xmax; x++) {
                for (int z = Zmin; z <= Zmax; z++) {
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
        return x >= Xmin && x <= Xmax && y >= minY && y <= maxY && z >= Zmin && z <= Zmax;
    }

    public boolean moveBlock(int indexInShip, int distance, int direction) {
        return moveBlockSimple(indexInShip, distance, direction, false, false);
    }    

    /**
     * Перемещение одиночного блока из обычного мира в космос 
     * @param indexInShip
     * @param distance
     * @param direction
     * @return 
     */
    public boolean moveBlockToSpace(int indexInShip, int distance, int direction) {
        return moveBlockSimple(indexInShip, distance, direction, true, false);
    }
    
    /**
     * Перемещение одиночного блока из космоса в обычный мир
     * @param indexInShip
     * @param distance
     * @param direction
     * @return 
     */
    public boolean moveBlockFromSpace(int indexInShip, int distance, int direction) {
        return moveBlockSimple(indexInShip, distance, direction, false, true);
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

                newTileEntity.worldObj = getTargetWorld();    
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
    public boolean mySetBlock(World w, int par1, int par2, int par3, int par4, int par5, int par6)
    {
        if (par1 >= -30000000 && par3 >= -30000000 && par1 < 30000000 && par3 < 30000000)
        {
            if (par2 < 0)
            {
                return false;
            }
            else if (par2 >= 256)
            {
                return false;
            }
            else
            {
                w.markBlockForUpdate(par1, par2, par3);
                Chunk chunk = w.getChunkFromChunkCoords(par1 >> 4, par3 >> 4);

                return myChunkSBIDWMT(chunk, par1 & 15, par2, par3 & 15, par4, par5);
            }
        }
        else
        {
            return false;
        }
    }
    
    // Incapsulation violation warning:
    // field Chunk.storageArrays has been turned from private to public in class Chunk.java
    public boolean myChunkSBIDWMT(Chunk c, int par1, int par2, int par3, int par4, int par5)
    {
        int j1 = par3 << 4 | par1;

        if (par2 >= c.precipitationHeightMap[j1] - 1)
        {
            c.precipitationHeightMap[j1] = -999;
        }

        int k1 = c.heightMap[j1];
        int l1 = c.getBlockID(par1, par2, par3);
        int i2 = c.getBlockMetadata(par1, par2, par3);

        if (l1 == par4 && i2 == par5)
        {
            return false;
        }
        else
        {
            ExtendedBlockStorage extendedblockstorage = c.storageArrays[par2 >> 4];

            if (extendedblockstorage == null)
            {
                if (par4 == 0)
                {
                    return false;
                }

                extendedblockstorage = c.storageArrays[par2 >> 4] = new ExtendedBlockStorage(par2 >> 4 << 4, !c.worldObj.provider.hasNoSky);
            }

            int j2 = c.xPosition * 16 + par1;
            int k2 = c.zPosition * 16 + par3;

            extendedblockstorage.setExtBlockID(par1, par2 & 15, par3, par4);

            if (l1 != 0)
            {
                if (!c.worldObj.isRemote)
                {
                    Block.blocksList[l1].breakBlock(c.worldObj, j2, par2, k2, l1, i2);
                }
                else if (Block.blocksList[l1] != null && Block.blocksList[l1].hasTileEntity(i2))
                {
                    TileEntity te = worldObj.getBlockTileEntity(j2, par2, k2);
                    if (te != null && te.shouldRefresh(l1, par4, i2, par5, worldObj, j2, par2, k2))
                    {
                        c.worldObj.removeBlockTileEntity(j2, par2, k2);
                    }
                }
            }

            if (extendedblockstorage.getExtBlockID(par1, par2 & 15, par3) != par4)
            {
                return false;
            }
            else
            {
                extendedblockstorage.setExtBlockMetadata(par1, par2 & 15, par3, par5);

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

                if (par4 != 0)
                {
                    if (Block.blocksList[par4] != null && Block.blocksList[par4].hasTileEntity(par5))
                    {
                        tileentity = c.getChunkBlockTileEntity(par1, par2, par3);

                        if (tileentity == null)
                        {
                            tileentity = Block.blocksList[par4].createTileEntity(c.worldObj, par5);
                            c.worldObj.setBlockTileEntity(j2, par2, k2, tileentity);
                        }

                        if (tileentity != null)
                        {
                            tileentity.updateContainingBlockInfo();
                            tileentity.blockMetadata = par5;
                        }
                    }
                }

                c.isModified = true;
                return true;
            }
        }
    }    
}