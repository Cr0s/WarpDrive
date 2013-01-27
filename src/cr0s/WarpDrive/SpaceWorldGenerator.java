/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cr0s.WarpDrive;

import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import java.util.Vector;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/**
 *
 * @author user
 */
public class SpaceWorldGenerator implements IWorldGenerator {

    // Радиус простой луны
    public final int MOON_RADIUS = 32;
    
    // Радиус звезды
    public final int STAR_RADIUS = 64;
    
    // Выше 128 по Y почти ничего не будет сгенерировано
    public final int Y_LIMIT = 128;
    // Лимит по Y снизу
    public final int Y_LIMIT_DOWN = 55;

    /**
     * Генерация для чанка
     * @param random
     * @param chunkX
     * @param chunkZ
     * @param world
     * @param chunkGenerator
     * @param chunkProvider 
     */
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.dimensionId != WarpDrive.instance.spaceDimID) {
            return;
        }

        int x = (chunkX * 16) + (5 - random.nextInt(10));
        int z = (chunkZ * 16) + (5 - random.nextInt(10));

        int y = Y_LIMIT_DOWN + random.nextInt(Y_LIMIT - Y_LIMIT_DOWN);

        // Установка луны
        if (random.nextInt(10000) == 1) {
            System.out.println("Generating moon at " + x + " " + y + " " + z);
            generateSphere2(world, x, y, z, MOON_RADIUS, false, 0, false);
            return;
        }

        // Установка звезды
        if (random.nextInt(10000) == 1) {
            System.out.println("Generating star at " + x + " " + y + " " + z);
            generateSphere2(world, x, y, z, STAR_RADIUS, false, Block.glowStone.blockID, true);
            generateSphere2(world, x, y, z, STAR_RADIUS -1, false, Block.glowStone.blockID, true);
            generateSphere2(world, x, y, z, STAR_RADIUS -2, false, Block.glowStone.blockID, true);
            return;
        }        
        
        // Простые астероиды
        if (random.nextInt(200) == 1) {
            System.out.println("Generating asteroid at " + x + " " + y + " " + z);
            generateAsteroid(world, x, y, z, 6, 11);
        }

        // Железные астероиды
        if (random.nextInt(2000) == 1) {
            System.out.println("Generating iron asteroid at " + x + " " + y + " " + z);
            generateAsteroidOfBlock(world, x, y, z, 6, 11, Block.oreIron.blockID);
        }        

        // Обсидиановые астероиды
        if (random.nextInt(2000) == 1) {
            System.out.println("Generating obsidian asteroid at " + x + " " + y + " " + z);
            generateAsteroidOfBlock(world, x, y, z, 6, 11, Block.obsidian.blockID);
            
            // Ядро астероида из алмаза
            world.setBlockWithNotify(x, y, z, Block.blockDiamond.blockID);
        }        
        
        // Алмазные астероиды
        if (random.nextInt(10000) == 1) {
            System.out.println("Generating diamond asteroid at " + x + " " + y + " " + z);
            generateAsteroidOfBlock(world, x, y, z, 4, 6, Block.oreDiamond.blockID);
            
            // Ядро астероида из алмаза
            world.setBlockWithNotify(x, y, z, Block.blockDiamond.blockID);            
        }        
        
        // Астероидные поля
        if (random.nextInt(3000) == 1) {
            System.out.println("Generating asteroid field at " + x + " " + y + " " + z);
            generateAsteroidField(world, x, y, z);
        }
    }

    
    
    /**
     * Генератор поля астероидов
     * @param world мир
     * @param x координата центра поля
     * @param y координата центра поля
     * @param z координата центра поля
     */
    private void generateAsteroidField(World world, int x, int y, int z) {
        int numOfAsteroids = 15 + world.rand.nextInt(30);
        
        // Минимальное расстояние между астероидами в поле
        final int FIELD_ASTEROID_MIN_DISTANCE = 5;
        
        // Максимальное расстояние между астероидами
        final int FIELD_ASTEROID_MAX_DISTANCE = 100;
        
        // Разброс больших астероидов
        for (int i = 1; i <= numOfAsteroids; i++) {
            int aX = x + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            int aY = y + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            int aZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            
            // Создаём астероид
            generateAsteroid(world, aX, aY, aZ, 4, 6);
        }

        // Разброс маленьких астероидов
        for (int i = 1; i <= numOfAsteroids; i++) {
            int aX = x + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            int aY = y + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            int aZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            
            // Создаём астероид
            generateAsteroid(world, aX, aY, aZ, 2, 2);
        }    
    }

    /**
     * Генератор астероидов одного типа. Создаёт астероид в точке.
     *
     * @param x координата центра астероида
     * @param y координата центра астероида
     * @param z координата центра астероида
     * @param asteroidSizeMax максимальный размер астероида (по количеству составляющих его сфер)
     * @param centerRadiusMax максимальный радиус центральной сферы
     */
    private void generateAsteroidOfBlock(World world, int x, int y, int z, int asteroidSizeMax, int centerRadiusMax, int blockID) {
        int asteroidSize = 1 + world.rand.nextInt(6);
        
        if (asteroidSizeMax != 0) {
            asteroidSize = Math.min(asteroidSizeMax, asteroidSize);
        }        

        int centerRadius = 1 + world.rand.nextInt(6);
        if (centerRadiusMax != 0) {
            centerRadius = Math.min(centerRadiusMax, centerRadius);
        }          
        
        
        final int CENTER_SHIFT = 2; // Смещение от центральной сферы

        // Центр астероида
        generateSphere2(world, x, y, z, centerRadius, true, blockID, false);

        // Бугры астероида
        for (int i = 1; i <= asteroidSize; i++) {
            int radius = 2 + world.rand.nextInt(centerRadius);

            int newX = x + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
            int newY = y + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
            int newZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));

            generateSphere2(world, newX, newY, newZ, radius, true, blockID, false);
        }
    }    
    
    /**
     * Генератор простых астероидов. Создаёт астероид в точке.
     *
     * @param x координата центра астероида
     * @param y координата центра астероида
     * @param z координата центра астероида
     * @param asteroidSizeMax максимальный размер астероида (по количеству составляющих его сфер)
     * @param centerRadiusMax максимальный радиус центральной сферы
     */
    private void generateAsteroid(World world, int x, int y, int z, int asteroidSizeMax, int centerRadiusMax) {
        int asteroidSize = 1 + world.rand.nextInt(6);
        
        if (asteroidSizeMax != 0) {
            asteroidSize = Math.min(asteroidSizeMax, asteroidSize);
        }        

        int centerRadius = 1 + world.rand.nextInt(6);
        if (centerRadiusMax != 0) {
            centerRadius = Math.min(centerRadiusMax, centerRadius);
        }          
        
        
        final int CENTER_SHIFT = 2; // Смещение от центральной сферы

        // Центр астероида
        generateSphere2(world, x, y, z, centerRadius, true, 0, false);

        // Бугры астероида
        for (int i = 1; i <= asteroidSize; i++) {
            int radius = 2 + world.rand.nextInt(centerRadius);

            int newX = x + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
            int newY = y + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
            int newZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));

            generateSphere2(world, newX, newY, newZ, radius, true, 0, false);
        }
    }

    /**
     * Генератор сферических объектов
     * @param world мир, в котором будет сгенерирована сфера/шар
     * @param xCoord координата центра
     * @param yCoord координата центра
     * @param zCoord координата центра
     * @param radius радиус сферы/шара
     * @param corrupted пропускать случайные блоки при генерации
     * @param forcedID генерировать сферу из определённых блоков, 0 если блоки случайные
     * @return 
     */
    public void generateSphere2(World world, int xCoord, int yCoord, int zCoord, double radius, boolean corrupted, int forcedID, boolean hollow) {
        // FIXME: блокировка мира обязательна?
        //world.editingBlocks = true;
        
        radius += 0.5D; // Отмеряем радиус от центра блока
        double radiusSq = radius * radius; // Возведение радиуса в квадрат в целях оптимизации (избавлпние от кв. корней)
        double radius1Sq = (radius - 1.0D) * (radius - 1.0D); // Квадратный радиус для пустой сферы

        int ceilRadius = (int) Math.ceil(radius); // Округляем радиус
        
        // Обходим куб со стороной, равной радиусу
        // (проверка точек на удовлетворение уравнению сферы x^2 + y^2 + z^2 = r^2)
        for (int x = 0; x <= ceilRadius; x++) {
            for (int y = 0; y <= ceilRadius; y++) {
                for (int z = 0; z <= ceilRadius; z++) {
                    double dSq = lengthSq(x, y, z); // Расстояние от центра до точки

                    // Пропускать блоки, которые удалены от центра больше, чем на радиус
                    if (dSq > radiusSq) {
                        continue;
                    }

                    // Генерация полой сферы
                    if ((hollow) && (
                          (dSq < radius1Sq) || ((lengthSq(x + 1, y, z) <= radiusSq) && (lengthSq(x, y + 1, z) <= radiusSq) && (lengthSq(x, y, z + 1) <= radiusSq))))
                    {
                          continue;
                    }

                    
                    // Ставим блоки по всем осям в текущей точке
                    int blockID;
                    
                    if (!corrupted || world.rand.nextInt(10) != 1)
                    {
                        blockID = (forcedID == 0) ? getRandomSurfaceBlockID(world.rand, corrupted) : forcedID;
                        world.setBlock(xCoord + x, yCoord + y, zCoord + z, blockID);
                        world.setBlock(xCoord - x, yCoord + y, zCoord + z, blockID);
                    }
                    
                    if (!corrupted || world.rand.nextInt(10) != 1)
                    {                    
                        blockID = (forcedID == 0) ? getRandomSurfaceBlockID(world.rand, corrupted) : forcedID;
                        world.setBlock(xCoord + x, yCoord - y, zCoord + z, blockID);
                        world.setBlock(xCoord + x, yCoord + y, zCoord - z, blockID);
                    }
                    
                    if (!corrupted || world.rand.nextInt(10) != 1)
                    {                    
                        blockID = (forcedID == 0) ? getRandomSurfaceBlockID(world.rand, corrupted) : forcedID;
                        world.setBlock(xCoord - x, yCoord - y, zCoord + z, blockID);
                        world.setBlock(xCoord + x, yCoord - y, zCoord - z, blockID);
                    }
                    
                    if (!corrupted || world.rand.nextInt(10) != 1)
                    {                    
                        blockID = (forcedID == 0) ? getRandomSurfaceBlockID(world.rand, corrupted) : forcedID;
                        world.setBlock(xCoord - x, yCoord + y, zCoord - z, blockID);
                         world.setBlock(xCoord - x, yCoord - y, zCoord - z, blockID);
                    }
                }
            }
        }

        // Разблокируем мир
        //world.editingBlocks = false;
    }

    private static double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }

    private int getRandomSurfaceBlockID(Random random, boolean corrupted) {
        int[] ores = {
            Block.oreIron.blockID,
            Block.oreGold.blockID,
            Block.oreCoal.blockID,
            Block.oreEmerald.blockID,
            Block.oreLapis.blockID,
            Block.oreRedstoneGlowing.blockID,};

        int blockID = Block.stone.blockID;
        if (corrupted) {
            blockID = Block.cobblestone.blockID;
        }

        if (random.nextInt(15) == 1) {
            blockID = ores[random.nextInt(ores.length - 1)];
        } else if (random.nextInt(500) == 1) {
            blockID = Block.oreDiamond.blockID;
        }

        return blockID;
    }
}
