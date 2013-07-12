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
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.DimensionManager;

public class EntityJump extends Entity {
    // Jump vector
    private int moveX;
    private int moveY;
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
    
    private boolean fromSpace, toSpace, betweenWorlds;

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

        if (!isJumping) {
            System.out.println("[JE] Preparing to jump...");

            prepareToJump();

            isJumping = true;
        } else {
            // Skip tick, awaiting chunk generation
            if ((targetWorld == worldObj) && !checkForChunksGeneratedIn(targetWorld)) {
                return;
            }

            if (currentIndexInShip >= ship.length-1) {
                isJumping = false;
                finishJump();
            } else { 
                //moveEntities(true);
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
        IChunkProvider chunkProvider = w.getChunkProvider();
        int x1 = (minX + moveX) >> 4;
        int x2 = (maxX + moveX) >> 4;
        int z1 = (minZ + moveZ) >> 4;
        int z2 = (maxZ + moveZ) >> 4;

        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                if (!chunkProvider.chunkExists(x, z)) {
                    messageToAllPlayersOnShip("Generating chunks...");
                    chunkProvider.provideChunk(x, z);

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

        toSpace   = (dir == -1 && (maxY + distance > 255) && worldObj.provider.dimensionId != WarpDrive.instance.spaceDimID);
        fromSpace = (dir == -2 && (minY - distance < 0) && worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID);

        betweenWorlds = fromSpace || toSpace;

        if (toSpace) {
            targetWorld = DimensionManager.getWorld(WarpDrive.instance.spaceDimID);
        } else if (fromSpace) {
            targetWorld = DimensionManager.getWorld(0);
        } else {
            targetWorld = this.worldObj;
        }

        axisalignedbb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

        // Calculate jump vector
        if (isCoordJump) {
            moveX = destX - xCoord;
            moveZ = destZ - zCoord;
            moveY = 0;
            distance = 0;
        } else {
            if (betweenWorlds) {
                moveX = moveY = 0;
                if (fromSpace) {
                    moveY = 245 - maxY;
                }
                if (toSpace) {
                    moveY = 0;
                }
            } else {
                // Do not check in long jumps
                if (distance < 256) {
                    distance = getPossibleJumpDistance();
                }
                if (distance <= shipLength) {
                    killEntity("Not enough space for jump.");
                    messageToAllPlayersOnShip("Not enough space for jump!");
                    LocalProfiler.stop();
                    return;
                }

                int movementVector[] = getVector(dir);
                moveX = movementVector[0] * distance;
                moveY = movementVector[1] * distance;
                moveZ = movementVector[2] * distance;

                // Нужно не упереться в пол мира и потолок космоса
                if ((maxY + moveY) > 255) {
                    moveY = 255 - maxY;
                }
                if ((minY + moveY) < 5) {
                    moveY = 5 - minY;
                }
            }
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
        moveEntities(false);
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
            if (currentIndexInShip >= ship.length) break;

            moveBlockSimple(currentIndexInShip);
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
        
        // Make an explosion in collision point
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
    public boolean moveEntities(boolean restorePositions) {
        List list = this.entityOnShip;

        if (list != null) {
            for (Object obj : list) {
                if (!(obj instanceof MovingEntity)) {
                    continue;
                }
                
                MovingEntity me = (MovingEntity)obj;
                
                Entity entity = me.entity;
                
                if (me == null) { continue; }

                double oldEntityX = me.oldX;
                double oldEntityY = me.oldY;
                double oldEntityZ = me.oldZ;

                double newEntityX;
                double newEntityY;
                double newEntityZ;

                if (restorePositions) {
                    newEntityX = oldEntityX;
                    newEntityY = oldEntityY;
                    newEntityZ = oldEntityZ;
                } else {
                    newEntityX = oldEntityX + moveX;
                    newEntityY = oldEntityY + moveY;
                    newEntityZ = oldEntityZ + moveZ;
                }

                //System.out.println("Entity moving: old (" + oldEntityX + " " + oldEntityY + " " + oldEntityZ + ") -> new (" + newEntityX + " " + newEntityY + " " + newEntityZ);

                // Travel to another dimension if needed
                if(betweenWorlds && !restorePositions) {
                    if (entity instanceof EntityPlayerMP) {
                        EntityPlayerMP player = (EntityPlayerMP) entity;

                        SpaceTeleporter teleporter = new SpaceTeleporter(DimensionManager.getWorld(targetWorld.provider.dimensionId), 0, MathHelper.floor_double(newEntityX), MathHelper.floor_double(newEntityY), MathHelper.floor_double(newEntityZ));
                        player.mcServer.getConfigurationManager().transferPlayerToDimension(player, targetWorld.provider.dimensionId, teleporter);
                    } else {
                        entity.travelToDimension(targetWorld.provider.dimensionId);
                    }
                }

                // Update position
                if (entity instanceof EntityPlayerMP) {
                    EntityPlayerMP player = (EntityPlayerMP) entity;

                    // Если на корабле есть кровать, то передвинуть точку спауна игрока
                    ChunkCoordinates bedLocation = player.getBedLocation();
                    if (bedLocation != null && testBB(axisalignedbb, bedLocation.posX, bedLocation.posY, bedLocation.posZ)) {
                        bedLocation.posX = bedLocation.posX + moveX;
                        bedLocation.posY = bedLocation.posY + moveY;
                        bedLocation.posZ = bedLocation.posZ + moveZ;
                        player.setSpawnChunk(bedLocation, false);
                    }

                    player.setPositionAndUpdate(newEntityX, newEntityY, newEntityZ);
                } else {
                    entity.moveEntity(newEntityX, newEntityY, newEntityZ);
                    continue;
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

        int movementVector[] = getVector(dir);
        int moveX = movementVector[0] * testDistance;
        int moveY = movementVector[1] * testDistance;
        int moveZ = movementVector[2] * testDistance;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int newX = x + moveX;
                    int newY = y + moveY;
                    int newZ = z + moveZ;

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
    public boolean moveBlockSimple(int indexInShip) {
        try {
            JumpBlock shipBlock = ship[indexInShip];

            if (shipBlock == null) {
                return false;
            }

            int oldX = shipBlock.x;
            int oldY = shipBlock.y;
            int oldZ = shipBlock.z;
            
            World spaceWorld = DimensionManager.getWorld(WarpDrive.instance.spaceDimID);
            World surfaceWorld = DimensionManager.getWorld(0);

            int newX = oldX + moveX;
            int newY = oldY + moveY;
            int newZ = oldZ + moveZ;

            int blockID = shipBlock.blockID;
            int blockMeta = shipBlock.blockMeta;

            mySetBlock(targetWorld, newX, newY, newZ, blockID, blockMeta, 2);

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
                    newTileEntity = targetWorld.getBlockTileEntity(newX, newY, newZ);
                    if (newTileEntity == null) {
                        System.out.println("[EJ] Error moving tileEntity! TE is null");
                        return false;
                    }

                    newTileEntity.invalidate();

                    newTileEntity.readFromNBT(oldnbt);

                    //newTileEntity.worldObj = targetWorld;
                    targetWorld.setBlockTileEntity(newX, newY, newZ, newTileEntity);
                }

                newTileEntity.worldObj = targetWorld;
                newTileEntity.validate();
                
                targetWorld.setBlockTileEntity(newX, newY, newZ, newTileEntity);
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
            ExtendedBlockStorage[] storageArrays = c.getBlockStorageArray();
            ExtendedBlockStorage extendedblockstorage = storageArrays[y >> 4];

            if (extendedblockstorage == null)
            {
                if (blockId == 0)
                {
                    return false;
                }

                extendedblockstorage = storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !c.worldObj.provider.hasNoSky);
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