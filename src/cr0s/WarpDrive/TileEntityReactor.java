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
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 *
 * @author user
 */
public class TileEntityReactor extends TileEntity implements IEnergySink {

    //public TileEntityReactor(World var1) {
    //    super();
    //    worldObj = var1;
    //}
    
    // = Настройки ядра =
    // Счётчики габаритов, переключатель режимов, переключатель длинны прыжка
    TileEntity gabarits1, gabarits2, modeCounter, lengthCounter;
    // RedPower IO-Expander для визуальной сигнализации состояния ядра (4 лампочки)
    TileEntity IOX;
    public final int IOX_ASSEMBLY_FAILURE = 15;    // [=|||]
    public final int IOX_LOW_POWER = 240;          // [|=||]
    public final int IOX_SHIP_IS_TOO_BIG = 3840;   // [||=|]
    public final int IOX_COOLDOWN = -4096;         // [|||=] (61440)
    public final int IOX_1 = -817;
    public final int IOX_NOT_ENOUGH_SPACE = -4081;
    // Сигнальный кабель ядра
    TileEntity redPowerCable;
    /* Состояние RedPower-кабеля указывают на параметры:
     * На входе:
     * 1) Направление прыжка
     * 2) Включение прыжка
     * 
     * На выходе:
     * 1) Готовность к прыжку
     * 
     * Состояние кабеля задаётся 16 битами:
     * Биты: |0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|
     * Цвет:  Б О     Ж   Р         С   З К Ч 
     * 
     * Blue     -- Прыжок вперёд    (С) 
     * Yellow   -- Прыжок назад     (Ж) 
     * 
     * Red      -- Прыжок вправо    (К)  
     * Green    -- Прыжок влево     (З) 
     * 
     * White    -- Прыжок вверх     (Б)
     * Black    -- Прыжок вниз      (Ч)
     * 
     * Pink     -- Включение прыжка (Р)
     * Orange   -- Бит ошибки       (О) (Не для чтения)
     * 
     */
    Boolean[] cableStates; // Состояния сигнального кабеля
    // Состояния направлений движения
    Boolean up, down;
    Boolean left, right;
    Boolean front, back;
    // Готовность к исполнению режима (прыжок и пр.)
    public Boolean ready;
    // Ядро собрано неправильно
    public Boolean invalidAssembly;
    // Состояние бита запуска (Розовый шнур)
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
    private final byte MODE_TELEPORT = 9;   // Телепортация игроков в космос
    private final int MAX_JUMP_DISTANCE_BY_COUNTER = 128; // Максимальное значение длинны прыжка с счётчика длинны
    private final int MAX_SHIP_VOLUME_ON_SURFACE = 10000;  // Максимальный объем корабля для прыжков не в космосе
    private final int MAX_SHIP_SIDE = 100; // Максимальная длинна одного из измерений корабля (ширина, высота, длина)
    int cooldownTime = 0;
    private final int MINIMUM_COOLDOWN_TIME = 5;
    private final int TICK_INTERVAL = 1;

    // = Привязка игроков =
    public ArrayList<String> players = new ArrayList();
    public String playersString = "";
    
    public String coreState = "";
    
    @SideOnly(Side.SERVER)
    @Override
    public void updateEntity() {
        if (ticks++ < 20 * TICK_INTERVAL) {
            return;
        }
        ticks = 0;

        readAllStates();
        
        // Телепортер в космос
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

                    if (entity instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), WarpDrive.instance.spaceDimID, new SpaceTeleporter(DimensionManager.getWorld(WarpDrive.instance.spaceDimID), 0, x, 256, z));

                        // Создаём платформу
                        if (DimensionManager.getWorld(WarpDrive.instance.spaceDimID).isAirBlock(x, 254, z)) {
                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlockWithNotify(x, 254, z, Block.stone.blockID);

                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlockWithNotify(x + 1, 254, z, Block.stone.blockID);
                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlockWithNotify(x - 1, 254, z, Block.stone.blockID);

                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlockWithNotify(x, 254, z + 1, Block.stone.blockID);
                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlockWithNotify(x, 254, z - 1, Block.stone.blockID);

                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlockWithNotify(x + 1, 254, z + 1, Block.stone.blockID);
                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlockWithNotify(x - 1, 254, z - 1, Block.stone.blockID);

                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlockWithNotify(x + 1, 254, z - 1, Block.stone.blockID);
                            DimensionManager.getWorld(WarpDrive.instance.spaceDimID).setBlockWithNotify(x - 1, 254, z + 1, Block.stone.blockID);
                        }

                        // Перемещаем на платформу
                        ((EntityPlayerMP) entity).setPositionAndUpdate(x, 256, z);

                        // Делаем лётчиком
                        if (!((EntityPlayerMP) entity).capabilities.isCreativeMode) {
                            ((EntityPlayerMP) entity).capabilities.allowFlying = true;
                        }
                    } else {
                        entity.travelToDimension(WarpDrive.instance.spaceDimID);
                    }
                }
            }
        }

        /*if (!canJump() && launchState) {
         setRedPowerStates(false, true);
         System.out.println("[TE-WC] Cooldown time: " + cooldownTime);
         cooldownTime--;
         return;
         }*/

        // 5. Вычисление пространственных параметров корабля
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
            setIOXState(this.IOX_SHIP_IS_TOO_BIG);
            setRedPowerStates(false, true);
            coreState = "Energy: " + currentEnergyValue + "; Ship blocks: " + shipVolume + "\n";
            this.coreState += "\n * Ship is too big (w: " + shipWidth + "; h: " + shipHeight + "; l: " + shipLength + ")";
            System.out.println(coreState);
            return;            
        }
        
        this.shipVolume = getRealShipVolume();
        
        if (shipVolume > MAX_SHIP_VOLUME_ON_SURFACE && worldObj.provider.dimensionId != WarpDrive.instance.spaceDimID) {
            setIOXState(this.IOX_SHIP_IS_TOO_BIG);
            setRedPowerStates(false, true);
            coreState = "Energy: " + currentEnergyValue + "; Ship blocks: " + shipVolume + "\n";
            this.coreState += "\n * Ship is too big (w: " + shipWidth + "; h: " + shipHeight + "; l: " + shipLength + ")";
            //System.out.println(coreState);
            return;
        }   
        
        // Сбор игроков
        if (currentMode == MODE_COLLECT_PLAYERS && launchState) {
            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
            
            for (int i = 0; i < players.size(); i++) {
                String nick = players.get(i);
                EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(nick);
                
                if (player != null) {
                    if (!testBB(aabb, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ)) && (this.currentEnergyValue - this.ENERGY_PER_ENTITY_TO_SPACE >= 0)) {
                        player.setPositionAndUpdate(xCoord + dx, yCoord, zCoord + dz);
                        
                        if (player.dimension != worldObj.provider.dimensionId) {
                            player.mcServer.getConfigurationManager().transferPlayerToDimension(player, this.worldObj.provider.dimensionId, new SpaceTeleporter(DimensionManager.getWorld(this.worldObj.provider.dimensionId), 0, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ)));
                        }
                        
                        this.currentEnergyValue -= this.ENERGY_PER_ENTITY_TO_SPACE;
                        player.sendChatToPlayer("[WarpCore] Welcome aboard, " + player.username + ".");
                    }
                }
            }
            
            setRedPowerStates(false, false);
            return;
        }        
        
        if ((currentMode == this.MODE_BASIC_JUMP || currentMode == this.MODE_LONG_JUMP) && !invalidAssembly && !launchState) {
            coreState = "Energy: " + currentEnergyValue + "; Ship blocks: " + shipVolume + "\n";
            coreState += "* Need " + Math.max(0, calculateRequiredEnergy(shipVolume, distance) - currentEnergyValue) + " eU to jump";                
        } else if (currentMode == 9) {
            coreState = "Energy: " + currentEnergyValue + "; Ship blocks: " + shipVolume + "\n";
            coreState += "* Is possible to teleport " + (currentEnergyValue / this.ENERGY_PER_ENTITY_TO_SPACE) + " players to space";
        }
        
        // Подготовка к прыжку
        if (launchState && (cooldownTime <= 0) && (currentMode == 1 || currentMode == 2)) {
            System.out.println("[WP-TE] Energy: " + currentEnergyValue + " eU");
            System.out.println("[WP-TE] Need to jump: " + calculateRequiredEnergy(shipVolume, distance) + " eU");

            // Подсчёт необходимого количества энергии для прыжка
            if (this.currentEnergyValue - calculateRequiredEnergy(shipVolume, distance) < 0) {
                setIOXState(this.IOX_LOW_POWER);
                setRedPowerStates(false, true);
                System.out.println("[WP-TE] Insufficient energy to jump");
                coreState = "Energy: " + currentEnergyValue + "; Ship blocks: " + shipVolume + "\n";
                coreState += "* LOW POWER. Need " + (calculateRequiredEnergy(shipVolume, distance) - currentEnergyValue) + " eU to jump";
                return;
            }

            // Потребить энергию
            this.currentEnergyValue -= calculateRequiredEnergy(shipVolume, distance);

            System.out.println((new StringBuilder()).append("Jump params: X ").append(minX).append(" -> ").append(maxX).append(" blocks").toString());
            System.out.println((new StringBuilder()).append("Jump params: Y ").append(minY).append(" -> ").append(maxY).append(" blocks").toString());
            System.out.println((new StringBuilder()).append("Jump params: Z ").append(minZ).append(" -> ").append(maxZ).append(" blocks").toString());

            // Получаем расстояние для прыжка
            distance = readCounterMax(lengthCounter);            
            distance = Math.min(MAX_JUMP_DISTANCE_BY_COUNTER, distance);

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

            // public EntityJump(World world, int x, int y, int z, int _dist, int _direction, int _dx, int _dz)
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
            
            this.cooldownTime = 60;

            setRedPowerStates(false, false);

            jump.xCoord = xCoord;
            jump.yCoord = yCoord;
            jump.zCoord = zCoord;

            jump.on = true;

            System.out.println("[TE-WC] Calling onUpdate()...");

            worldObj.spawnEntityInWorld(jump);
            coreState = "";
        }
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
        }

        return energyValue;
    }
    
    public void readAllStates() {
        // 1. Ищем кабель RedPower (шина данных для управления ядром) и IO-Expander
        redPowerCable = findRedpowerCableAndIOX();
        if (redPowerCable != null) {
            cableStates = getRedpowerCableStates(redPowerCable);

            // Задаём состояния
            up = cableStates[0];
            down = cableStates[15];

            front = cableStates[11];
            back = cableStates[4];

            left = cableStates[14];
            right = cableStates[13];

            if (!launchState) {
                launchState = cableStates[6];
            } else {
                launchState = false;
                return;
            }
        } else {
            this.coreState = "ASSEMBLING ERROR: RedPower bundled cable is not connected.";
            invalidAssembly = true;
            return;
        }

        // 2. Ищем счетчик режимов (стоит над ядром, (x, y+1,z))
        modeCounter = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
        if (modeCounter == null || !modeCounter.toString().contains("LogicStorage")) {
            this.coreState = "ASSEMBLING ERROR: \"modes\" counter is not installed.";
            invalidAssembly = true;
            setIOXState(IOX_ASSEMBLY_FAILURE);
            setRedPowerStates(false, true);
            return;
        }

        currentMode = readCounterMax(modeCounter);

        if (!searchParametersCounters()) {
            this.coreState = "ASSEMBLING ERROR: Parameters counters is not installed.";
            invalidAssembly = true;
            setIOXState(IOX_ASSEMBLY_FAILURE);
            setRedPowerStates(false, true);
            return;
        }

        // 3. Задаём геометрические параметры корабля
        shipFront = readCounterMax(gabarits1);
        shipRight = readCounterInc(gabarits1);
        shipUp = readCounterDec(gabarits1);

        shipBack = readCounterMax(gabarits2);
        shipLeft = readCounterInc(gabarits2);
        shipDown = readCounterDec(gabarits2);

        shipLength = shipFront + shipBack + 1;
        shipWidth = shipLeft + shipRight + 1;
        shipHeight = shipUp + shipDown + 1;

        shipVolume = shipLength * shipWidth * shipHeight;

        // 4. Вычисление направления движения
        direction = calculateJumpDirection();
        
        invalidAssembly = false;
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
    
    public void setRedPowerStates(boolean launch, boolean error) {
        if (redPowerCable == null) {
            return;
        }
        NBTTagCompound tag = new NBTTagCompound();
        redPowerCable.writeToNBT(tag);

        byte states[] = tag.getByteArray("pwrs"); // Получить массив состояний кабеля
        if (states == null) {
            return;
        }

        states[6] = (launch) ? (byte) 1 : (byte) 0;
        states[1] = (error) ? (byte) 1 : (byte) 0;

        tag.setByteArray("pwrs", states);
        redPowerCable.readFromNBT(tag);
    }

    public void setCooldownTime(int time) {
        this.cooldownTime = Math.max(MINIMUM_COOLDOWN_TIME, time);
    }

    public boolean canJump() {
        return (cooldownTime <= 0);
    }

    /*
     * 
     * Вычисление направления прыжка в градусах относительно "носа"
     * На данный момент доступно 6 режимов:
     * +--------- Вертикальные ------+
     * |Режим            |Флаги:  u d|
     * +-----------------+-----------+
     * |1. Полёт вверх   |        1 0|
     * |2. Полёт вниз    |        0 1|
     * +-----------------------------+
     * 
     * +-------- Горизонтальные ---------+
     * |Режим            | Флаги: f r b l|
     * +-----------------+---------------+
     * |3. Вперёд        |        1 0 0 0|
     * |4. Вправо        |        0 1 0 0|
     * |5. Назад         |        0 0 1 0|
     * |6. Влево         |        0 0 0 1|
     * +---------------------------------+
     * 
     */
    public int calculateJumpDirection() {
        int result = 0;

        /*
         *              0                      -1
         *            front                    up
         * 270 left     X     right 90         X
         *            back                     down
         *             180                     -2
         */

        if (up) {
            result = JUMP_UP;
        } else if (down) {
            result = JUMP_DOWN;
        } else if (front && !back && !left && !right) {
            result = 0;
        } else if (right && !front && !back && !left) {
            result = 90;
        } else if (back && !front && !right && !left) {
            result = 180;
        } else if (left && !front && !right && !back) {
            result = 270;
        }

        return result;
    }

    // Считывание параметров RedPower-счетчика
    public int readCounterMax(TileEntity counter) {
        try {
            NBTTagCompound tag = new NBTTagCompound();
            counter.writeToNBT(tag);
            return tag.getInteger("max");
        } catch (Exception e) {
        }

        return 0;
    }

    public int readCounterInc(TileEntity counter) {
        try {
            NBTTagCompound tag = new NBTTagCompound();
            counter.writeToNBT(tag);
            return tag.getInteger("inc");
        } catch (Exception e) {
        }

        return 0;
    }

    public int readCounterDec(TileEntity counter) {
        try {
            NBTTagCompound tag = new NBTTagCompound();
            counter.writeToNBT(tag);
            return tag.getInteger("dec");
        } catch (Exception e) {
        }

        return 0;
    }

    public boolean searchParametersCounters() {
        if (Math.abs(dx) == 1 && dz == 0) {
            lengthCounter = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + dx);
            gabarits1 = worldObj.getBlockTileEntity(xCoord + dx, yCoord, zCoord - dx);
            gabarits2 = worldObj.getBlockTileEntity(xCoord - dx, yCoord, zCoord - dx);
        } else if (Math.abs(dz) == 1 && dx == 0) {
            lengthCounter = worldObj.getBlockTileEntity(xCoord - dz, yCoord, zCoord);
            gabarits1 = worldObj.getBlockTileEntity(xCoord + dz, yCoord, zCoord + dz);
            gabarits2 = worldObj.getBlockTileEntity(xCoord + dz, yCoord, zCoord - dz);
        }

        return ((lengthCounter != null) && (lengthCounter.toString().contains("LogicStorage")))
                && ((gabarits1 != null) && (gabarits1.toString().contains("LogicStorage")))
                && ((gabarits2 != null) && (gabarits2.toString().contains("LogicStorage")));
        /*
         switch (dx) {
         case 1:  
         lengthCounter = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);
         gabarits1 = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord - 1);
         lengthCounter = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord - 1);
         break;
                    
         case -1: 
         lengthCounter = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);
         gabarits1 = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord + 1);
         lengthCounter = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord + 1);
         break;
         }
         } else if (Math.abs(dz) == 1) {
         switch (dz) {
         case 1:  
         lengthCounter = worldObj.getBlockTileEntity(xCoord -1, yCoord, zCoord);
         gabarits1 = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord + 1);
         lengthCounter = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord -1);
         break;
                    
         case -1: 
         lengthCounter = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);
         gabarits1 = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord - 1);
         lengthCounter = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord + 1);
         break;
         }            
         }*/
    }

    public TileEntity findRedpowerCableAndIOX() {
        TileEntity result;

        result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);
        if (result != null && result.toString().contains("TileCable")) {
            //System.out.println("[WP-TE] (x+1 y z) TileEntity: " + result.toString() + " metadata: " + worldObj.getBlockMetadata(xCoord +1, yCoord, zCoord));
            dx = 1;
            dz = 0;
            IOX = worldObj.getBlockTileEntity(xCoord + 1, yCoord + 1, zCoord);
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);
        if (result != null && result.toString().contains("TileCable")) {
            //System.out.println("[WP-TE] (x-1 y z) TileEntity: " + result.toString() + " metadata: " + worldObj.getBlockMetadata(xCoord -1, yCoord, zCoord));
            dx = -1;
            dz = 0;
            IOX = worldObj.getBlockTileEntity(xCoord - 1, yCoord + 1, zCoord);
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);
        if (result != null && result.toString().contains("TileCable")) {
            //System.out.println("[WP-TE] (x y z+1) TileEntity: " + result.toString() + " metadata: " + worldObj.getBlockMetadata(xCoord, yCoord, zCoord +1));
            dx = 0;
            dz = 1;
            IOX = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord + 1);
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);
        if (result != null && result.toString().contains("TileCable")) {
            //System.out.println("[WP-TE] (x y z-1) Cable TileEntity: " + result.toString() + " metadata: " + worldObj.getBlockMetadata(xCoord, yCoord, zCoord -1));
            dx = 0;
            dz = -1;
            IOX = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord - 1);
            return result;
        }

        return null;
    }

    public Boolean[] getRedpowerCableStates(TileEntity cable) {
        NBTTagCompound tag = new NBTTagCompound();
        cable.writeToNBT(tag);

        byte states[] = tag.getByteArray("pwrs"); // Получить массив состояний кабеля
        if (states == null) {
            return null;
        }

        Boolean[] locCableStates = new Boolean[16];

        String s = "";//, ss = "";
        for (int i = 0; i < 16; i++) {
            locCableStates[i] = (states[i] != 0);
            s += (states[i] != 0) ? "1" : "0";
            //ss += String.valueOf(states[i]) + " ";
        }

        return locCableStates;
    }

    /**
     * Установка состояния индикаторов IO Expander-a
     *
     * @param WBuf битовая маска состояний
     */
    public void setIOXState(int WBuf) {
        if (IOX == null) {
            return;
        }

        NBTTagCompound tag = new NBTTagCompound();
        IOX.writeToNBT(tag);
        tag.setShort("wb", (short) WBuf);
        IOX.readFromNBT(tag);


        IOX.updateEntity();
        worldObj.scheduleBlockUpdate(IOX.xCoord, IOX.yCoord, IOX.zCoord, 0, 0);
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
        playersString = tag.getString("players");
        updatePlayersList();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("energy", currentEnergyValue);
        
        updatePlayersString();
        tag.setString("players", playersString);
    }
    
    public void attachPlayer(EntityPlayer ep) {
        for (int i = 0; i < players.size(); i++) {
            String nick = players.get(i);
            
            if (ep.username.equals(nick)) {
                ep.sendChatToPlayer("[WarpCore] Detached.");
                players.remove(i);
                return;
            }
        }
        
        ep.attackEntityFrom(DamageSource.generic, 1);
        ep.sendChatToPlayer("[WarpCore] Successfully attached.");
        players.add(ep.username);
        updatePlayersString();
    }
    
    public void updatePlayersString() {
        String nick;
        this.playersString = "";
        
        for (int i = 0; i < players.size(); i++) {
            nick = players.get(i);
            
            this.playersString += nick + "|";
        }
    }
    
    public void updatePlayersList() {
        String[] playersArray = playersString.split("\\|");

        for (int i = 0; i < playersArray.length; i++) {
            String nick = playersArray[i];
            
            if (!nick.isEmpty()) {
                players.add(nick);
            }
        }
    }
    
    public String getAttachedPlayersList() {
        String list = "";
        
        for (int i = 0; i < this.players.size(); i++) {
            String nick = this.players.get(i);
            
            list += nick + ((i == this.players.size() - 1)? "" : ", ");
        }
        
        if (players.isEmpty()) {
            list = "<nobody>";
        }
        
        return list;
    }
}
