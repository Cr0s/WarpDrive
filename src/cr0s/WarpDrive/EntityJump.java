package cr0s.WarpDrive;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class EntityJump extends Entity
{
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
    
    public World worldObj;
    
    public boolean on = false;
    public JumpBlock ship[];
    
    public TileEntityReactor reactor;
    
    public EntityJump(World world, int x, int y, int z, int _dist, int _direction, int _dx, int _dz, TileEntityReactor parReactor)
    {
        super(world);
        
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
        this.distance = _dist;
        this.dir = _direction;

        shipLeft = shipRight = shipFront = shipBack = shipDown = shipUp = shipLength = 0;
        this.dx = this.dz = 0;
        this.dx = _dx;
        this.dz = _dz;
        Xmax = Zmax = maxY = Xmin = Zmin = minY = 0;
        
        this.worldObj = world;
        
        System.out.println("[JE] Entity created");
        
        this.reactor = parReactor;
    }

    public void killEntity(String reason) {
        if (!reason.isEmpty()) {
            System.out.println("[JUMP] Killed: " + reason);
        }
        
        worldObj.editingBlocks = false;
        worldObj.setEntityDead(this);     
    }
    
    //@SideOnly(Side.SERVER)
    @Override
    public void onUpdate()
    {
        if (worldObj.editingBlocks || !on || minY < 0 || maxY > 255)
        {
            killEntity("Entity is disabled or Y-coord error! Cannot jump.");
            return;
        }

        System.out.println("[JUMP] onUpdate() called");
        
        // Блокируем мир
        worldObj.editingBlocks = true;

        distance = getPossibleJumpDistance();
        if (distance < 0)
        {
            killEntity("Not enough space for jump.");
            return;
        }

        if (!checkForBedrockOnShip()) {
            killEntity("Is bedrock on the ship. Aborting.");
            return;
        }

        int shipSize = getRealShipSize();  
        
        saveShip(shipSize, false);

        removeShip();
        
        moveShip();        
        
        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(Xmin, minY, Zmin, Xmax, maxY, Zmax);
        moveEntity(axisalignedbb, distance, dir);
        
        // Разблокируем мир
        worldObj.editingBlocks = false;
        
        // Прыжок окончен
        killEntity("");
        on = false;
    }

    public void removeShip() {
        for (JumpBlock jb : ship) {
            if (jb.blockTileEntity != null) {
                worldObj.removeBlockTileEntity(jb.x, jb.y, jb.z);
            }
            
            //System.out.println("[EJ] Removing block: " + jb.x + " " + jb.y + " " + jb.z + " " + jb.blockID);
            worldObj.setBlockAndMetadata(jb.x, jb.y, jb.z, 0, 0);
        }
    }
    
    /**
     * Сохранение корабля в память
     * @param shipSize размер корабля (число блоков для перемещения)
     * @param deleteShip удалять ли блоки корабля сразу после сохранения
     */
    public void saveShip(int shipSize, boolean deleteShip) {
        ship = new JumpBlock[shipSize];
        int index = 0;

        for (int y = minY; y <= maxY; y++)
        {
            for (int x = Xmin; x <= Xmax; x++)
            {
                for (int z = Zmin; z <= Zmax; z++)
                {
                    if (ship == null)
                    {
                        killEntity("ship is null!");
                        return;
                    }

                    int blockID = worldObj.getBlockId(x, y, z);
                    int blockMeta = worldObj.getBlockMetadata(x, y, z);
                    TileEntity tileentity = worldObj.getBlockTileEntity(x, y, z);

                    // Пустые блоки пропускаются
                    if (blockID == 0)
                    {
                        continue;
                    }

                    if (tileentity != null)
                    {
                        ship[index] = new JumpBlock(blockID, blockMeta, tileentity, x, y, z);
                    }
                    else
                    {
                        ship[index] = new JumpBlock(blockID, blockMeta, x, y, z);
                    }

                    if (deleteShip) {
                        if (tileentity != null) {
                            worldObj.removeBlockTileEntity(x, y, z);
                        }
                        worldObj.setBlockAndMetadata(x, y, z, 0, 0);
                    }
                    
                    
                    index++;
                }
            }
        }

        System.out.println((new StringBuilder()).append("[JUMP] Ship saved: ").append((new StringBuilder()).append(ship.length).append(" blocks")).toString());
        
    }
    /*
     * Перемещение корабля
     */
    public void moveShip() {
        for (int indexInShip = 0; indexInShip <= ship.length - 1; indexInShip++)
        {
            moveBlock(indexInShip, distance, dir);
        }        
    }
    
    /**
     * 
     * @return Возвращает новую длинну прыжка (для стыковки), либо -1, если прыжок невозможен в принципе
     */
    public int getPossibleJumpDistance() {
        int testDistance = this.distance;
        boolean canJump;
        
        while (true)
        {
            if (testDistance <= this.shipLength)
            {
                break;
            }

            canJump = checkMovement(testDistance);

            if (canJump)
            {
                break;
            }

            testDistance--;
        }        
        
        return testDistance;
    }
    /*
     * Проверка на наличие запрещённых блоков на корабле (бедрок)
     * 
     * Применяется для предотвращения возможности зацепить варп-полем корабля бедрок и оторвать его
     */
    public boolean checkForBedrockOnShip() {
        for (int y = minY; y <= maxY; y++) {
            for (int x = Xmin; x <= Xmax; x++) {
                for (int z = Zmin; z <= Zmax; z++) {
                    int blockID = worldObj.getBlockId(x, y, z);
                    
                    // Пропускаем пустые блоки воздуха
                    if (blockID == 0) {
                        continue;
                    }
                    
                    // Проверка блока
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
    
    public boolean moveEntity(AxisAlignedBB axisalignedbb, int distance, int direction)
    {
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

        if (list != null)
        {
            for (int index = 0; index < list.size(); index++)
            {
                Object obj = list.get(index);

                if (obj == null || !(obj instanceof Entity))
                {
                    continue;
                }

                Entity entity = (Entity)obj;
                
                int oldEntityX = (int)entity.posX;
                int oldEntityY = (int)entity.posY;
                int oldEntityZ = (int)entity.posZ;
                
                int newEntityX = getNewXCoord(oldEntityX, oldEntityY, oldEntityZ, distance, direction);
                int newEntityY = getNewYCoord(oldEntityX, oldEntityY, oldEntityZ, distance, direction);
                int newEntityZ = getNewZCoord(oldEntityX, oldEntityY, oldEntityZ, distance, direction);
                
                //System.out.println("Entity moving: old (" + oldEntityX + " " + oldEntityY + " " + oldEntityZ + ") -> new (" + newEntityX + " " + newEntityY + " " + newEntityZ);
                
                if (!(entity instanceof EntityPlayerMP))
                {
                    entity.moveEntity(newEntityX, newEntityY, newEntityZ);
                    continue;
                }

                // Если на корабле есть кровать, то передвинуть точку спауна игрока
                ChunkCoordinates bedLocation = ((EntityPlayerMP)entity).getBedLocation();
                if (bedLocation != null && testBB(axisalignedbb, bedLocation.posX, bedLocation.posY, bedLocation.posZ))
                {
                    bedLocation.posX = getNewXCoord(bedLocation.posX, bedLocation.posY, bedLocation.posZ, distance, direction);
                    bedLocation.posY = getNewYCoord(bedLocation.posX, bedLocation.posY, bedLocation.posZ, distance, direction);
                    bedLocation.posZ = getNewZCoord(bedLocation.posX, bedLocation.posY, bedLocation.posZ, distance, direction);
                    ((EntityPlayerMP)entity).setSpawnChunk(bedLocation, false);
                }

                ((EntityPlayerMP)entity).setPositionAndUpdate(newEntityX, newEntityY, newEntityZ);
            }
        }

        return true;
    }

    /*
     * Проверка на вхождение точки в область (bounding-box)
     */
    public boolean testBB(AxisAlignedBB axisalignedbb, int x, int y, int z)
    {
        return axisalignedbb.minX <= (double)x && axisalignedbb.maxX >= (double)x && axisalignedbb.minY <= (double)y && axisalignedbb.maxY >= (double)y && axisalignedbb.minZ <= (double)z && axisalignedbb.maxZ >= (double)z;
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
    public int getNewXCoord(int oldX, int oldY, int oldZ, int distance, int direction)
    {
        moveX = 0;
        moveZ = 0;
       // System.out.println("old: (" + oldX + "; " + oldZ + ") dis: " + distance + " dir: " + direction);
        int movementVector[] = getVector(direction);
       // System.out.println("Vector: (" + movementVector[0] + "; 0; " + movementVector[2]);
        
        moveX = movementVector[0] * distance;
        moveZ = movementVector[2] * distance;
        int result = oldX;

        if (direction != -1 && direction != -2)
        {
            result += moveX;
        }

        //System.out.println("moveX: " + moveX);
        
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
    public int getNewYCoord(int i, int oldY, int k, int distance, int direction)
    {
        int result = oldY;

        if (direction == -1 || direction == -2)
        {
            if (direction == -1)
            {
                result += distance;
            }
            else
            {
                result -= distance;
            }
        }

        if (result >= 255)
        {
            result = 255;
        }

        if (result <= 0)
        {
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
    public int getNewZCoord(int oldX, int oldY, int oldZ, int distance, int direction)
    {
        moveX = 0;
        moveZ = 0;
       // System.out.println("old: (" + oldX + "; " + oldZ + ") dis: " + distance + " dir: " + direction);
        int movementVector[] = getVector(direction);
       // System.out.println("Vector: (" + movementVector[0] + "; 0; " + movementVector[2]);
        moveX = movementVector[0] * distance;
        moveZ = movementVector[2] * distance;
        int result = oldZ;

        if (direction != -1 && direction != -2)
        {
            result += moveZ;
        }

        //System.out.println("moveZ: " + moveZ);
        
        return result;
    }    
    
    public int rotsincos(int i, boolean flag)
    {
        // sin
        if (flag)
        {
            switch (i)
            {
                case 0:
                    return 0;

                case 90:
                    return 1;

                case 180:
                    return 0;

                case 270:
                    return -1;
            }
        }
        // cos
        else
        {
            switch (i)
            {
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
    public int[] getVector(int i)
    {
        int ai[] =
        {
            0, 0, 0
        };
        
        if (dz == 1)
        {
            switch (i)
            {
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
        }
        else if (dz == -1)
        {
            switch (i)
            {
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
        }
        else if (dx == 1)
        {
            switch (i)
            {
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
        }
        else if (dx == -1)
        {
            switch (i)
            {
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
     * Проверка возможности установки корабля в месте, удалённом от корабля на определённом расстоянии в сторону прыжка
     * @param i
     * @return true, если корабль уместился на новом месте
     */
    public boolean checkMovement(int testDistance)
    {
        if (dir == -1 && maxY + testDistance > 255)
        {
            System.out.println("[JUMP] Reactor will blow due +high limit");
            return false;
        }

        if (dir == -2 && minY - testDistance <= 8)
        {
            System.out.println("[JUMP] Reactor will blow due -low limit");
            return false;
        }

        for (int y = minY; y <= maxY; y++)
        {
            for (int x = Xmin; x <= Xmax; x++)
            {
                for (int z = Zmin; z <= Zmax; z++)
                {
                    int newX = getNewXCoord(x, y, z, testDistance, dir);
                    int newY = getNewYCoord(x, y, z, testDistance, dir);
                    int newZ = getNewZCoord(x, y, z, testDistance, dir);

                    if (isBlockInShip(newX, newY, newZ))
                    {
                        continue;
                    }

                    int blockID = worldObj.getBlockId(newX, newY, newZ);
                    int blockOnShipID = worldObj.getBlockId(x, y, z);

                    if (blockOnShipID == Block.bedrock.blockID)
                    {
                        return false;
                    }

                    if (blockOnShipID != 0 && blockID != 0 && blockID != 9 && blockID != 8 && blockID != 18)
                    {
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
    public boolean isBlockInShip(int x, int y, int z)
    {
        return x >= Xmin && x <= Xmax && y >= minY && y <= maxY && z >= Zmin && z <= Zmax;
    }

    /**
     * Перемещение одиночного блока на новое место
     * @param indexInShip
     * @param j
     * @param k
     * @return 
     */
    public boolean moveBlock(int indexInShip, int distance, int direction)
    {
        try
        {
            JumpBlock shipBlock = ship[indexInShip];

            if (shipBlock == null)
            {
                return false;
            }

            int oldX = shipBlock.x;
            int oldY = shipBlock.y;
            int oldZ = shipBlock.z;

            int newY = getNewYCoord(oldX, oldY, oldZ, distance, direction);
            int newX = getNewXCoord(oldX, oldY, oldZ, distance, direction);
            int newZ = getNewZCoord(oldX, oldY, oldZ, distance, direction);
           
            int blockID = shipBlock.blockID;
            int blockMeta = shipBlock.blockMeta;

           // if (div++ % 16 == 0)
            //{
                worldObj.setBlockAndMetadataWithNotify(newX, newY, newZ, blockID, blockMeta);
            //    div = 0;
            //}
            //else
            //{
           //     worldObj.setBlockAndMetadata(newX, newY, newZ, blockID, blockMeta);
           // }
            NBTTagCompound oldnbt = new NBTTagCompound();
            

            if (shipBlock.blockTileEntity != null && blockID != 159 && blockID != 149 && blockID != 156 && blockID != 146 && blockID != 145)
            {
                shipBlock.blockTileEntity.writeToNBT(oldnbt);
                TileEntity newTileEntity = worldObj.getBlockTileEntity(newX, newY, newZ);
                if (newTileEntity == null) return false; // PIZDEC!!!
                newTileEntity.readFromNBT(oldnbt);
                worldObj.setBlockTileEntity(newX, newY, newZ, newTileEntity);
                worldObj.removeBlockTileEntity(oldX, oldY, oldZ);
            }
        }
        catch (Exception exception) { exception.printStackTrace(); }

        return true;
    }

    
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
    }


    @Override
    protected void entityInit() {
        onUpdate();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound var1) {
        
    }
/*
    @Override
    protected void entityInit() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound var1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound var1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/
}