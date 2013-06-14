/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cr0s.WarpDrive;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.Direction;
import ic2.api.energy.tile.IEnergySink;
import java.util.ArrayList;
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
import net.minecraftforge.common.DimensionManager;

/**
 *
 * @author Cr0s
 */
public class TileEntityReactor extends TileEntity implements IEnergySink {
    // = Настройки ядра =

    // Готовность к исполнению режима (прыжок и пр.)
    public Boolean ready;
    // Ядро собрано неправильно
    public Boolean invalidAssembly = false;
    // Состояние бита запуска
    public Boolean launchState = false;
    // Счётчик тиков
    int ticks;
    // = Ориентация в пространстве =
    public final int JUMP_UP = -1;
    public final int JUMP_DOWN = -2;
    int dx, dz; // Горизонтальные векторы (1,0) (-1,0) (0,1) (0,-1) для определения носа корабля
    int direction; // Направление прыжка (в градусах, или JUMP_UP, JUMP_DOWN)
    int distance;  // Расстояние прыжка
    // Параметры варп-прямоугольника
    // Расчитываются из габаритов корабля
    int maxX, maxY, maxZ;
    int minX, minY, minZ;
    // Габариты корабля
    int shipFront, shipBack;
    int shipLeft, shipRight;
    int shipUp, shipDown;
    int shipHeight, shipWidth, shipLength;
    int shipSize = 0; // Длина корабля в направлении прыжка
    int shipVolume; // Примерный объем корабля (проиведение 3 измерений)
    // Текущий режим ядра
    int currentMode = 0;
    
    // = Энергия =
    int currentEnergyValue = 0;        // Текущее значение энергии
    int maxEnergyValue = 10000000; // 10 миллионов eU
    
    // = Константы =
    private final int ENERGY_PER_BLOCK_MODE1 = 1; // eU
    private final int ENERGY_PER_DISTANCE_MODE1 = 10; // eU
    private final int ENERGY_PER_BLOCK_MODE2 = 100; // eU
    private final int ENERGY_PER_DISTANCE_MODE2 = 100; // eU    
    private final int ENERGY_PER_ENTITY_TO_SPACE = 100000; // eU
    private final byte MODE_BASIC_JUMP = 1; // Ближний прыжок 0-128
    private final byte MODE_LONG_JUMP = 2;  // Дальний прыжок 0-12800
    private final byte MODE_COLLECT_PLAYERS = 3; // Сбор привязанных к ядру игроков
    private final byte MODE_BEACON_JUMP = 4;     // Jump ship by beacon
    private final byte MODE_TELEPORT = 0;   // Телепортация игроков в космос
    private final int MAX_JUMP_DISTANCE_BY_COUNTER = 128; // Максимальное значение длинны прыжка с счётчика длинны
    private final int MAX_SHIP_VOLUME_ON_SURFACE = 50000;  // Максимальный объем корабля для прыжков не в космосе (50k блоков)
    private final int MAX_SHIP_SIDE = 100; // Максимальная длинна одного из измерений корабля (ширина, высота, длина)
    int cooldownTime = 0;
    private final int COOLDOWN_INTERVAL_SECONDS = 5;

    // = Привязка игроков =
    
    public String coreState = "";
    
    public TileEntityProtocol controller;
    
    @SideOnly(Side.SERVER)
    @Override
    public void updateEntity() {        
        TileEntity c = findControllerBlock();
        
        if (c != null) {
            this.controller = (TileEntityProtocol)c;
            this.currentMode = controller.getMode();
            
            if (this.controller.isSummonAllFlag()) {
                summonPlayers();
                controller.setSummonAllFlag(false);
            } else if (!this.controller.getToSummon().isEmpty()) {
                summonSinglePlayer(this.controller.getToSummon());
                this.controller.setToSummon("");
            }
        } else {
            invalidAssembly = true;
            return;
        }
        
        switch (currentMode) {
            case MODE_TELEPORT:
                if (isChestSummonMode()) {
                    if (ticks++ < 20) {
                        return;
                    }       
                    
                    if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
                        summonPlayersByChestCode();
                        ticks = 0;
                    }
                } else {
                    teleportPlayersToSpace();
                }
            case MODE_BASIC_JUMP:
            case MODE_LONG_JUMP:
            case MODE_BEACON_JUMP:               
                if (controller == null) { return; }
                coreState = "Energy: " + currentEnergyValue;
                
                if (controller.isJumpFlag()) {
                    // Set up activated animation
                    if (worldObj.getBlockMetadata(xCoord, yCoord, zCoord) == 0)
                    {
                        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2); // Set block state to "active"   
                        makePlayersOnShipDrunk();
                    }
                    
                    // Awaiting cooldown time
                    if ((currentMode != MODE_BASIC_JUMP) && cooldownTime++ < COOLDOWN_INTERVAL_SECONDS * 20)
                    {
                        return;
                    }
                    
                    cooldownTime = 0; // Reset cooldown
                    
                    System.out.println("[W-C] Jumping!");
                    
                    prepareToJump();
                    doJump(currentMode == MODE_LONG_JUMP);
                    
                    controller.setJumpFlag(false);
                } else
                {
                    worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2); // Deactivate block animation
                }
                break;
        }
    }
   
    public void makePlayersOnShipDrunk() {
        prepareToJump();
        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
        
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);  
        for (Object o : list) {
            if (o == null || !(o instanceof EntityPlayer)) {
                continue;
            }
            
            // Set "drunk" effect
            ((EntityPlayer)o).addPotionEffect(new PotionEffect(Potion.confusion.id, 180, 0, true));
        }
    }
    
    public void summonPlayers() {
        calculateSpatialShipParameters();
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);

        for (int i = 0; i < controller.players.size(); i++) {
            String nick = controller.players.get(i);
            EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(nick);

            if (player != null && !testBB(aabb, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ))) {
                summonPlayer(player, xCoord + dx, yCoord, zCoord + dz);
            }
        }
    }
    
    public void summonSinglePlayer(String nickname) {
        calculateSpatialShipParameters();
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);

        for (int i = 0; i < controller.players.size(); i++) {
            String nick = controller.players.get(i);
            EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(nick);

            if (player != null && nick.equals(nickname) && !testBB(aabb, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ))) {
                summonPlayer(player, xCoord + dx, yCoord, zCoord + dz);
                return;
            }
        }        
    }
    
    public void summonPlayer(EntityPlayerMP player, int x, int y, int z) {
        if (this.currentEnergyValue - this.ENERGY_PER_ENTITY_TO_SPACE >= 0) {
             player.setPositionAndUpdate(x, y, z);

             if (player.dimension != worldObj.provider.dimensionId) {
                 player.mcServer.getConfigurationManager().transferPlayerToDimension(player, this.worldObj.provider.dimensionId, new SpaceTeleporter(DimensionManager.getWorld(this.worldObj.provider.dimensionId), 0, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ)));
             }

             this.currentEnergyValue -= this.ENERGY_PER_ENTITY_TO_SPACE;
         }        
    }
    
    public void prepareToJump() {
        this.direction = controller.getDirection();
        
        this.shipFront = controller.getFront();
        this.shipRight = controller.getRight();
        this.shipUp    = controller.getUp();
        
        this.shipBack  = controller.getBack();
        this.shipLeft  = controller.getLeft();
        this.shipDown  = controller.getDown();
        
        this.distance  = Math.min(128, controller.getDistance());
        
        calculateSpatialShipParameters();
    }
    
    
    public void calculateSpatialShipParameters() {
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
        
        this.shipSize = 0;
        
        switch (this.direction) {
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
  
        // Проверка размеров корабля
        if (shipLength > MAX_SHIP_SIDE || shipWidth > MAX_SHIP_SIDE || shipHeight > MAX_SHIP_SIDE) {
            coreState = "Energy: " + currentEnergyValue + "; Ship blocks: " + shipVolume + "\n";
            this.coreState += "\n * Ship is too big (w: " + shipWidth + "; h: " + shipHeight + "; l: " + shipLength + ")";
            System.out.println(coreState);
            this.controller.setJumpFlag(false);
            return;            
        }
        
        this.shipVolume = getRealShipVolume();
        
        if (shipVolume > MAX_SHIP_VOLUME_ON_SURFACE && worldObj.provider.dimensionId != WarpDrive.instance.spaceDimID) {
            coreState = "Energy: " + currentEnergyValue + "; Ship blocks: " + shipVolume + "\n";
            this.coreState += "\n * Ship is too big (w: " + shipWidth + "; h: " + shipHeight + "; l: " + shipLength + ")";
        }        
    }

    private void doBeaconJump() {
        if (currentEnergyValue != maxEnergyValue)
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
            if (player.dimension != worldObj.provider.dimensionId) {
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
            currentEnergyValue = 0;
            
            System.out.println("[TE-WC] Moving ship to a beacon (" + beaconX + "; " + yCoord + "; " + beaconZ + ")");
            EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, 1, 0, dx, dz, this);

            jump.Xmax = maxX;
            jump.Xmin = minX;
            jump.Zmax = maxZ;
            jump.Zmin = minZ;
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
            jump.destZ = beaconZ;
            
            jump.on = true;

            worldObj.spawnEntityInWorld(jump);
            coreState = "";            
        } else
        {
            System.out.println("[TE-WC] Beacon not found.");
        }
    }    
    
    public void doJump(boolean longjump) {  
        if (currentMode == this.MODE_BEACON_JUMP)
        {
            System.out.println("[TE-WC] Performing beacon jump...");
            doBeaconJump();
            return;
        }
        
        if ((currentMode == this.MODE_BASIC_JUMP || currentMode == this.MODE_LONG_JUMP) && !invalidAssembly) {
            coreState = "Energy: " + currentEnergyValue + "; Ship blocks: " + shipVolume + "\n";
            coreState += "* Need " + Math.max(0, calculateRequiredEnergy(shipVolume, distance) - currentEnergyValue) + " eU to jump";                
        }        
        
        // Подготовка к прыжку
        if ((currentMode == this.MODE_BASIC_JUMP || currentMode == this.MODE_LONG_JUMP)) {
            System.out.println("[WP-TE] Energy: " + currentEnergyValue + " eU");
            System.out.println("[WP-TE] Need to jump: " + calculateRequiredEnergy(shipVolume, distance) + " eU");

            // Подсчёт необходимого количества энергии для прыжка
            if (this.currentEnergyValue - calculateRequiredEnergy(shipVolume, distance) < 0) {
                System.out.println("[WP-TE] Insufficient energy to jump");
                coreState = "Energy: " + currentEnergyValue + "; Ship blocks: " + shipVolume + "\n";
                coreState += "* LOW POWER. Need " + (calculateRequiredEnergy(shipVolume, distance) - currentEnergyValue) + " eU to jump";
                this.controller.setJumpFlag(false);
                return;
            }

            // Потребить энергию
            this.currentEnergyValue -= calculateRequiredEnergy(shipVolume, distance);

            System.out.println((new StringBuilder()).append("Jump params: X ").append(minX).append(" -> ").append(maxX).append(" blocks").toString());
            System.out.println((new StringBuilder()).append("Jump params: Y ").append(minY).append(" -> ").append(maxY).append(" blocks").toString());
            System.out.println((new StringBuilder()).append("Jump params: Z ").append(minZ).append(" -> ").append(maxZ).append(" blocks").toString());

            //System.out.println("[WC-TE] Distance: " + distance + "; shipSize: " + shipSize);
            if (this.currentMode == this.MODE_BASIC_JUMP) {
                distance += shipSize;       
            }            
            
            // Дальний прыжок в космосе в 100 раз дальше
            if (currentMode == this.MODE_LONG_JUMP && (direction != -1 && direction != -2)) {
                if (worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID) {
                    distance *= 100;
                } else if (worldObj.provider.dimensionId != WarpDrive.instance.spaceDimID) {
                    distance *= 10;
                }
            }

            System.out.println((new StringBuilder()).append("[JUMP] Totally moving ").append((new StringBuilder()).append(shipVolume).append(" blocks to length ").append(distance).append(" blocks, direction: ").append(direction).toString()).toString());

            EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, distance, direction, dx, dz, this);

            jump.Xmax = maxX;
            jump.Xmin = minX;
            jump.Zmax = maxZ;
            jump.Zmin = minZ;
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
            
            jump.xCoord = xCoord;
            jump.yCoord = yCoord;
            jump.zCoord = zCoord;
     
            jump.on = true;

            worldObj.spawnEntityInWorld(jump);
            coreState = "";
        }
    }

    public void teleportPlayersToSpace() {
        if (currentMode == MODE_TELEPORT && worldObj.provider.dimensionId != WarpDrive.instance.spaceDimID) {
            if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
                AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(xCoord - 5, yCoord - 5, zCoord - 5, xCoord + 5, yCoord + 5, zCoord + 5);
                List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

                for (Object o : list) {
                    if (currentEnergyValue - ENERGY_PER_ENTITY_TO_SPACE < 0) {
                        return;
                    }

                    currentEnergyValue -= ENERGY_PER_ENTITY_TO_SPACE;

                    Entity entity = (Entity) o;
                    int x = MathHelper.floor_double(entity.posX);
                    int z = MathHelper.floor_double(entity.posZ);

                    //int y = MathHelper.floor_double(entity.posY);
                    
                    final int WOOL_BLOCK_ID = 35;
                    
                    int newY;
                    
                    for (newY = 254; newY > 0; newY--) {
                        if (DimensionManager.getWorld(WarpDrive.instance.spaceDimID).getBlockId(x, newY, z) == WOOL_BLOCK_ID) {
                            break;
                        }
                    }
                    
                    if (newY <= 0) { newY = 254; }
                    
                    if (entity instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), WarpDrive.instance.spaceDimID, new SpaceTeleporter(DimensionManager.getWorld(WarpDrive.instance.spaceDimID), 0, x, 256, z));

                        // Создаём платформу
                        if (DimensionManager.getWorld(WarpDrive.instance.spaceDimID).isAirBlock(x, newY, z)) {
                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlock(x, newY, z, Block.stone.blockID, 0, 2);

                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlock(x + 1, newY, z, Block.stone.blockID, 0, 2);
                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlock(x - 1, newY, z, Block.stone.blockID, 0, 2);

                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlock(x, newY, z + 1, Block.stone.blockID, 0, 2);
                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlock(x, newY, z - 1, Block.stone.blockID, 0, 2);

                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlock(x + 1, newY, z + 1, Block.stone.blockID, 0, 2);
                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlock(x - 1, newY, z - 1, Block.stone.blockID, 0, 2);

                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlock(x + 1, newY, z - 1, Block.stone.blockID, 0, 2);
                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlock(x - 1, newY, z + 1, Block.stone.blockID, 0, 2);
                        }

                        // Перемещаем на платформу
                        ((EntityPlayerMP) entity).setPositionAndUpdate(x, newY + 2, z);
                    }
                }
            }
        }        
    }
    
    public void summonPlayersByChestCode() {
        if (worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord) == null) { return; }
        TileEntityChest chest = (TileEntityChest)worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
        EntityPlayerMP player;
        
        for (int i = 0; i < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); i++)
        {
            player = (EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(i);
            
            if (checkPlayerInventory(chest, player)) {
                System.out.println("[P] Summoning " + player.username);
                summonPlayer(player, xCoord, yCoord + 2, zCoord);
            }
        }        
    } 
    
    public boolean checkPlayerInventory(TileEntityChest chest, EntityPlayerMP player) {
        Boolean result = false;
        final int MIN_KEY_LENGTH = 5;
        
        int keyLength = 0;
        
        for (int index = 0; index < chest.getSizeInventory(); index++) {
            ItemStack chestItem = chest.getStackInSlot(index);
            ItemStack playerItem = player.inventory.getStackInSlot(9 + index);

            if (chestItem == null || playerItem == null) { continue; }

            //System.out.println(player.username + " " + index + " -> " + chestItem + " = " + playerItem);

            if (chestItem.itemID != playerItem.itemID || chestItem.getItemDamage() != playerItem.getItemDamage() || chestItem.stackSize != playerItem.stackSize) {
                return false;
            } else { result = true; }
            
            keyLength++;
        }    
        
        if (keyLength < MIN_KEY_LENGTH) { 
            System.out.println("[ChestCode] Key is too short: " + keyLength + " < " + MIN_KEY_LENGTH);
            return false; 
        }
        
        return result;
    }
    
    public Boolean isChestSummonMode() {
        TileEntity te = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
        
        if (te != null) {
            return (te instanceof TileEntityChest);
        }
        
        return false;
    }
    
    /*
     * Проверка на вхождение точки в область (bounding-box)
     */
    public boolean testBB(AxisAlignedBB axisalignedbb, int x, int y, int z) {
        return axisalignedbb.minX <= (double) x && axisalignedbb.maxX >= (double) x && axisalignedbb.minY <= (double) y && axisalignedbb.maxY >= (double) y && axisalignedbb.minZ <= (double) z && axisalignedbb.maxZ >= (double) z;
    }    

    public String getCoreState() {
        return "[WarpCore] " + this.coreState;
    }
    
    public int calculateRequiredEnergy(int shipVolume, int jumpDistance) {
        int energyValue = 0;

        switch (currentMode) {
            case MODE_BASIC_JUMP:
                energyValue = (ENERGY_PER_BLOCK_MODE1 * shipVolume) + (ENERGY_PER_DISTANCE_MODE1 * jumpDistance);
                break;
            case MODE_LONG_JUMP:
                energyValue = (ENERGY_PER_BLOCK_MODE2 * shipVolume) + (ENERGY_PER_DISTANCE_MODE2 * jumpDistance);
                break;
            case MODE_BEACON_JUMP:
                energyValue = this.maxEnergyValue;
        }

        return energyValue;
    }
    
    /*
     * Получить реальное количество блоков, из которых состоит корабль 
     */
    public int getRealShipVolume() {
        int shipVol = 0;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int blockID = worldObj.getBlockId(x, y, z);

                    // Пропускаем пустые блоки воздуха
                    if (blockID != 0) {
                        shipVol++;
                    }
                }
            }
        }

        return shipVol;
    }    

    public TileEntity findControllerBlock() {
        TileEntity result;

        result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);
        if (result != null && result instanceof TileEntityProtocol) {
            dx = 1;
            dz = 0;
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);
        if (result != null && result instanceof TileEntityProtocol) {
            dx = -1;
            dz = 0;
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);
        if (result != null && result instanceof TileEntityProtocol) {
            //System.out.println("[WP-TE] (x y z+1) TileEntity: " + result.toString() + " metadata: " + worldObj.getBlockMetadata(xCoord, yCoord, zCoord +1));
            dx = 0;
            dz = 1;
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);
        if (result != null && result instanceof TileEntityProtocol) {
            dx = 0;
            dz = -1;
            return result;
        }

        return null;
    }

    // Сколько нужно энергии
    @Override
    public int demandsEnergy() {
        return (maxEnergyValue - currentEnergyValue);
    }

    /*
     * Принятие энергии на вход
     */
    @Override
    public int injectEnergy(Direction directionFrom, int amount) {
        // Избыток энергии
        int leftover = 0;

        currentEnergyValue += amount;
        if (currentEnergyValue > maxEnergyValue) {
            leftover = (currentEnergyValue - maxEnergyValue);
            currentEnergyValue = maxEnergyValue;
        }

        return leftover;
    }

    // Максимально возможный входной поток энергии, в нашем случае -- неограниченный
    @Override
    public int getMaxSafeInput() {
        return Integer.MAX_VALUE;
    }

    // Принимать ли энергию
    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) {
        return true; // Принимаем энергию отовсюду
    }

    // Блок является составляющим энергосети
    @Override
    public boolean isAddedToEnergyNet() {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        currentEnergyValue = tag.getInteger("energy");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("energy", currentEnergyValue);
    }
}
