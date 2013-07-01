package cr0s.WarpDrive;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * @author Cr0s
 */
public class SpaceWorldGenerator implements IWorldGenerator {

    // Radius of simple moon
    public final int MOON_RADIUS = 32;
    public final int MOON_CORE_RADIUS = 10;
    
    // Star radius
    public final int STAR_RADIUS = 80;
    
    // Upper than 128 almost nothing will be generated
    public final int Y_LIMIT = 128;
    // Lower limit
    public final int Y_LIMIT_DOWN = 55;

    /**
     * Generator for chunk
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

        // Moon setup
        if (random.nextInt(8000) == 1) {
            System.out.println("Generating moon at " + x + " " + y + " " + z);
            generateSphere2(world, x, y, z, MOON_RADIUS, false, 0, false);
            
            // Generate moon's core
            if (random.nextInt(10) > 3)
            {
                generateSphere2(world, x, y, z, MOON_CORE_RADIUS, false, Block.lavaStill.blockID, false); // Lava core
                generateSphere2(world, x, y, z, MOON_CORE_RADIUS + 1, false, Block.obsidian.blockID, true);  // Obsidian shell
            } else
            {
                generateSphere2(world, x, y, z, MOON_CORE_RADIUS, false, 0, false);
                generateSmallShip(world, x, y, z);
                
            }
            return;
        }

        // FIXME: Star setup
        /*if (random.nextInt(250) == 1) {
            EntitySphereGen esg = new EntitySphereGen(world, x, y, z, 1);
            esg.xCoord = x;
            esg.yCoord = y;
            esg.zCoord = z;
            
            esg.on = true;
            
            world.spawnEntityInWorld(esg);
                     
            return;
        }*/       
        
        // Simple asteroids
        if (random.nextInt(500) == 1) {
            System.out.println("Generating asteroid at " + x + " " + y + " " + z);
            generateAsteroid(world, x, y, z, 6, 11);
            return;
        }

        // Random asteroid of block
        if (random.nextInt(1000) == 1) {
            generateRandomAsteroid(world, x, y, z, 6, 11);
        } 

        // Ice asteroid
        if (random.nextInt(2000) == 1) {
            System.out.println("Generating ice asteroid at " + x + " " + y + " " + z);
            generateAsteroidOfBlock(world, x, y, z, 6, 11, Block.ice.blockID);
            
            return;
        }        
        
        // Asteroid field
        if (random.nextInt(3500) == 1) {
            System.out.println("Generating asteroid field at " + x + " " + y + " " + z);
            generateAsteroidField(world, x, y, z);
            
            return;
        }        
        
        // Diamond asteroid
        if (random.nextInt(10000) == 1) {
            System.out.println("Generating diamond asteroid at " + x + " " + y + " " + z);
            generateAsteroidOfBlock(world, x, y, z, 4, 6, Block.oreDiamond.blockID);
            
            // Diamond block core
            world.setBlock(x, y, z, Block.blockDiamond.blockID, 0, 2);     
            
           // return;
        } 
    }

    private void generateSmallShip(World world, int x, int y, int z) {
        x = x + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(10));
        y = y + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(10));
        z = z + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(10));
            
        System.out.println("Generating small ship at " + x + " " + y + " " + z);
        
        new WorldGenSmallShip(world.rand.nextBoolean()).generate(world, world.rand, x, y, z);
    }
    
    private void generateRandomAsteroid(World world, int x, int y, int z, int asteroidSizeMax, int centerRadiusMax) {
        Random random = new Random();
        
        if (random.nextInt(30) == 1) {
            System.out.println("Generating random asteroid of block at " + x + "; " + y + "; " + z);
            generateAsteroidOfBlock(world, x, y, z, asteroidSizeMax, centerRadiusMax, getRandomSurfaceBlockID(random, false, true));    
        } else {
            generateAsteroid(world, x, y, z, asteroidSizeMax, centerRadiusMax);
        }
    }
    
    /**
     * Asteroid field generator
     * @param world мир
     * @param x координата центра поля
     * @param y координата центра поля
     * @param z координата центра поля
     */
    private void generateAsteroidField(World world, int x, int y, int z) {
        int numOfAsteroids = 15 + world.rand.nextInt(30);
        
        // Minimal distance between asteroids in field
        final int FIELD_ASTEROID_MIN_DISTANCE = 5;
        
        // Maximum distance
        final int FIELD_ASTEROID_MAX_DISTANCE = 100;
        
        // Setting up of big asteroids
        for (int i = 1; i <= numOfAsteroids; i++) {
            int aX = x + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            int aY = y + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            int aZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            
            // Place an asteroid
            generateRandomAsteroid(world, aX, aY, aZ, 4, 6);
        }
      
        // Setting up small asteroids
        for (int i = 1; i <= numOfAsteroids; i++) {
            int aX = x + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            int aY = y + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            int aZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
            
            // Placing
            if (world.rand.nextInt(100) != 0) {
                generateRandomAsteroid(world, aX, aY, aZ, 2, 2);
            } else {
                generateSmallShip(world, aX, aY, aZ);
            }
        }    
    }

    /**
     * Asteroid of block generator
     *
     * @param x x-coord of center
     * @param y center
     * @param z center
     * @param asteroidSizeMax maximum asteroid size (by number of balls it consists)
     * @param centerRadiusMax maximum radius of central ball
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
        
        
        final int CENTER_SHIFT = 2; // Offset from center of central ball

        // Asteroid's center
        generateSphere2(world, x, y, z, centerRadius, true, blockID, false);

        // Asteroids knolls
        for (int i = 1; i <= asteroidSize; i++) {
            int radius = 2 + world.rand.nextInt(centerRadius);

            int newX = x + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
            int newY = y + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
            int newZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));

            generateSphere2(world, newX, newY, newZ, radius, true, blockID, false);
        }
    }    
    
    /**
     * Asteroid generator
     *
     * @param x x-coord of center
     * @param y center
     * @param z center
     * @param asteroidSizeMax maximum asteroid size (by number of balls it consists)
     * @param centerRadiusMax maximum radius of central ball
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
        
        
        final int CENTER_SHIFT = 2; 

        generateSphere2(world, x, y, z, centerRadius, true, 0, false);

        for (int i = 1; i <= asteroidSize; i++) {
            int radius = 2 + world.rand.nextInt(centerRadius);

            int newX = x + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
            int newY = y + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
            int newZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));

            generateSphere2(world, newX, newY, newZ, radius, true, 0, false);
        }
    }

    /**
     * Sphere generator
     * @param world target world
     * @param xCoord center
     * @param yCoord center
     * @param zCoord center
     * @param radius sphere radius
     * @param corrupted skip random blocks when generating (corrupted effect)
     * @param forcedID sphere of specified blocks or random blocks if not specified
     * @return 
     */
    public void generateSphere2(World world, int xCoord, int yCoord, int zCoord, double radius, boolean corrupted, int forcedID, boolean hollow) {
        
        radius += 0.5D; // Radius from center of block
        double radiusSq = radius * radius; // Optimization to avoid sqrts...
        double radius1Sq = (radius - 1.0D) * (radius - 1.0D); // for hollow sphere

        int ceilRadius = (int) Math.ceil(radius);
        
        // Pass the cube and check points for sphere equation x^2 + y^2 + z^2 = r^2
        for (int x = 0; x <= ceilRadius; x++) {
            for (int y = 0; y <= ceilRadius; y++) {
                for (int z = 0; z <= ceilRadius; z++) {
                    double dSq = lengthSq(x, y, z); // Distance from current position to center

                    // Skip too far blocks
                    if (dSq > radiusSq) {
                        continue;
                    }

                    // Hollow sphere condition
                    if ((hollow) && (
                          (dSq < radius1Sq) || ((lengthSq(x + 1, y, z) <= radiusSq) && (lengthSq(x, y + 1, z) <= radiusSq) && (lengthSq(x, y, z + 1) <= radiusSq))))
                    {
                          continue;
                    }

                    
                    // Place blocks
                    int blockID, meta = 0;
                    
                    if (!corrupted || world.rand.nextInt(10) != 1)
                    {
                        blockID = (forcedID == 0) ? getRandomSurfaceBlockID(world.rand, corrupted, false) : forcedID;
                        if (blockID == 39701) { meta = blockID % 10; blockID = blockID / 10; }
                        world.setBlock(xCoord + x, yCoord + y, zCoord + z, blockID, meta, 2);
                        world.setBlock(xCoord - x, yCoord + y, zCoord + z, blockID, meta, 2);
                    }
                    
                    if (!corrupted || world.rand.nextInt(10) != 1)
                    {                    
                        blockID = (forcedID == 0) ? getRandomSurfaceBlockID(world.rand, corrupted, false) : forcedID;
                        if (blockID == 39701) { meta = blockID % 10; blockID = blockID / 10; }
                        world.setBlock(xCoord + x, yCoord - y, zCoord + z, blockID, meta, 2);
                        world.setBlock(xCoord + x, yCoord + y, zCoord - z, blockID, meta, 2);
                    }
                    
                    if (!corrupted || world.rand.nextInt(10) != 1)
                    {                    
                        blockID = (forcedID == 0) ? getRandomSurfaceBlockID(world.rand, corrupted, false) : forcedID;
                        if (blockID == 39701) { meta = blockID % 10; blockID = blockID / 10; }
                        world.setBlock(xCoord - x, yCoord - y, zCoord + z, blockID, meta, 2);
                        world.setBlock(xCoord + x, yCoord - y, zCoord - z, blockID, meta, 2);
                    }
                    
                    if (!corrupted || world.rand.nextInt(10) != 1)
                    {                    
                        blockID = (forcedID == 0) ? getRandomSurfaceBlockID(world.rand, corrupted, false) : forcedID;
                        if (blockID == 39701) { meta = blockID % 10; blockID = blockID / 10; }
                        world.setBlock(xCoord - x, yCoord + y, zCoord - z, blockID, meta, 2);
                        world.setBlock(xCoord - x, yCoord - y, zCoord - z, blockID, meta, 2);
                    }
                }
            }
        }
    }

    private static double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }

    private int getRandomSurfaceBlockID(Random random, boolean corrupted, boolean nocobble) {
        List<Integer> ores = new ArrayList<Integer>();
        ores.add(Block.oreIron.blockID);
        ores.add(Block.oreGold.blockID);
        ores.add(Block.oreCoal.blockID);
        ores.add(Block.oreEmerald.blockID);
        ores.add(Block.oreLapis.blockID);
        ores.add(Block.oreRedstoneGlowing.blockID);
        ores.add(247);//IC2
        ores.add(248);
        ores.add(249);
        if (Loader.isModLoaded("ICBM|Explosion"))
        {
            ores.add(3880);
            ores.add(3970);
            ores.add(39701);
        }
        int blockID = Block.stone.blockID;
        if (corrupted) {
            blockID = Block.cobblestone.blockID;
        }

        if (random.nextInt(10) == 1 || nocobble) {
            blockID = ores.get(random.nextInt(ores.size() - 1));
        } 
        else if (random.nextInt(350) == 1 && Loader.isModLoaded("AppliedEnergistics")) {
            blockID = 902; // quarz (AE)
        }
        else if (random.nextInt(500) == 1) {
            blockID = Block.oreDiamond.blockID;
        }/* else if (random.nextInt(5000) == 1) {
            blockID = 688;
        }*/

        return blockID;
    }
}
