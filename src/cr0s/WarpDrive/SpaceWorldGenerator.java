package cr0s.WarpDrive;

import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * @author Cr0s
 */
public class SpaceWorldGenerator implements IWorldGenerator
{
	// Radius of simple moon
	public final int MOON_RADIUS = 32;
	public final int MOON_CORE_RADIUS = 10;

	// Star radius
	public final int RED_DWARF_RADIUS = 42;
	public final int YELLOW_GIANT_RADIUS = 64;
	public final int YELLOW_SUPERGIANT_RADIUS = 80;

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
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		if (world.provider.dimensionId != WarpDrive.instance.spaceDimID)
			return;
		int x = (chunkX * 16) + (5 - random.nextInt(10));
		int z = (chunkZ * 16) + (5 - random.nextInt(10));
		if (Math.abs(x) > WarpDrive.WORLD_LIMIT_BLOCKS || Math.abs(z) > WarpDrive.WORLD_LIMIT_BLOCKS)
			return;
		int y = Y_LIMIT_DOWN + random.nextInt(Y_LIMIT - Y_LIMIT_DOWN);
		// Moon setup
		if (random.nextInt(8000) == 1)
			generateMoon(world, x, y, z);
		// Simple asteroids
		else if (random.nextInt(500) == 1)
			generateAsteroid(world, x, y, z, 6, 11);
		// Random asteroid of block
		else if (random.nextInt(1000) == 1)
		{
			generateRandomAsteroid(world, x, y, z, 6, 11);
			if (random.nextBoolean())
				generateGasCloudOfColor(world, x, y, z, 6, 11, random.nextInt(12));
		}
		// Ice asteroid
		else if (random.nextInt(2000) == 1)
			generateAsteroidOfBlock(world, x, y, z, 6, 11, Block.ice.blockID, 0);
		// Asteroid field
		else if (random.nextInt(3500) == 1)
			generateAsteroidField(world, x, y, z);
		// Diamond asteroid
		else if (random.nextInt(10000) == 1)
		{
			generateAsteroidOfBlock(world, x, y, z, 4, 6, Block.oreDiamond.blockID, 0);
			// Diamond block core
			world.setBlock(x, y, z, Block.blockDiamond.blockID, 0, 2);
			if (random.nextBoolean())
				generateGasCloudOfColor(world, x, y, z, 6, 11, random.nextInt(12));
		}
		// Quartz asteroid
		else if (WarpDriveConfig.isAELoaded && random.nextInt(20000) == 1)
		{
			generateAsteroidOfBlock(world, x, y, z, 4, 6, WarpDriveConfig.i.getAEBlock("blkQuartzOre").itemID, WarpDriveConfig.i.getAEBlock("blkQuartzOre").getItemDamage());
			if (random.nextBoolean())
				generateGasCloudOfColor(world, x, y, z, 6, 11, random.nextInt(12));
		}
	}

	public void generateMoon(World world, int x, int y, int z)
	{
		System.out.println("Generating moon at " + x + " " + y + " " + z);
		int t = generateSphereEntity(world, x, y, z, MOON_RADIUS, false, 0, 0);
		// Generate moon's core
		if (t == Block.netherrack.blockID)
			generateSphere2(world, x, y, z, MOON_CORE_RADIUS, false, Block.lavaStill.blockID, 0, false); // Lava core
		else if (t != Block.whiteStone.blockID)
		{
			if (world.rand.nextInt(10) > 2)
			{
				generateSphere2(world, x, y, z, MOON_CORE_RADIUS, false, Block.lavaStill.blockID, 0, false); // Lava core
				generateSphere2(world, x, y, z, MOON_CORE_RADIUS + 1, false, Block.obsidian.blockID, 0, true);  // Obsidian shell
			}
			else
			{
				generateSphere2(world, x, y, z, MOON_CORE_RADIUS, false, 0, 0, false);
				generateSmallShip(world, x, y, z);
			}
		}
		// Generate moon's atmosphere
		if (world.rand.nextBoolean())
			generateGasSphereEntity(world, x, y, z, MOON_RADIUS + 5, true, 1 + world.rand.nextInt(11));
		// Place bedrock blocks
		world.setBlock(x, y, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x + MOON_CORE_RADIUS, y, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x - MOON_CORE_RADIUS, y, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y, z + MOON_CORE_RADIUS, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y, z - MOON_CORE_RADIUS, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y + MOON_CORE_RADIUS, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y - MOON_CORE_RADIUS, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x + MOON_RADIUS / 2, y, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x - MOON_RADIUS / 2, y, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y, z + MOON_RADIUS / 2, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y, z - MOON_RADIUS / 2, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y + MOON_RADIUS / 2, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y - MOON_RADIUS / 2, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x + MOON_RADIUS - 10, y, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x - MOON_RADIUS + 10, y, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y, z + MOON_RADIUS - 10, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y, z - MOON_RADIUS + 10, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y + MOON_RADIUS - 10, z, Block.bedrock.blockID, 0, 0);
		world.setBlock(x, y - MOON_RADIUS + 10, z, Block.bedrock.blockID, 0, 0);
	}

	private void placeStarCore(World world, int x, int y, int z, int radius)
	{
		EntityStarCore core = new EntityStarCore(world, x, y, z, radius);
		core.xCoord = x;
		core.yCoord = y;
		core.zCoord = z;
		core.setPosition((double)x, (double)y, (double)z);
		world.spawnEntityInWorld(core);
	}
	public void generateStar(World world, int x, int y, int z)
	{
		int starClass =  world.rand.nextInt(3);
		System.out.println("Generating star (class " + starClass + ") at " + x + " " + y + " " + z);

		switch (starClass)
		{
			case 0: // red dwarf
				generateSphereEntity(world, x, y, z, RED_DWARF_RADIUS, false, Block.blockRedstone.blockID, 0);
				// Heliosphere of red gas
				generateGasSphereEntity(world, x, y, z, RED_DWARF_RADIUS + 6, true, 1);
				placeStarCore(world, x, y, z, RED_DWARF_RADIUS + 6);
				break;

			case 1: // yellow giant
				generateSphereEntity(world, x, y, z, YELLOW_GIANT_RADIUS, false, Block.glowStone.blockID, 0);
				// Heliosphere of yellow gas
				generateGasSphereEntity(world, x, y, z, YELLOW_GIANT_RADIUS + 6, true, 3);
				placeStarCore(world, x, y, z, YELLOW_GIANT_RADIUS + 6);
				break;

			case 2:
				generateSphereEntity(world, x, y, z, YELLOW_SUPERGIANT_RADIUS, false, Block.glowStone.blockID, 0);
				// Heliosphere of yellow gas
				generateGasSphereEntity(world, x, y, z, YELLOW_SUPERGIANT_RADIUS + 6, true, 3);
				placeStarCore(world, x, y, z, YELLOW_SUPERGIANT_RADIUS + 6);
				break;
		}
	}

	private int generateSphereEntity(World world, int x, int y, int z, int radius, boolean hollow, int blockID, int blockMeta)
	{
		EntitySphereGen esg = new EntitySphereGen(world, x, y, z, radius, blockID, blockMeta, hollow, false);
		esg.xCoord = x;
		esg.yCoord = y;
		esg.zCoord = z;
		world.spawnEntityInWorld(esg);
		return esg.defaultBlock[0];
	}

	private void generateGasSphereEntity(World world, int x, int y, int z, int radius, boolean hollow, int color)
	{
		EntitySphereGen esg = new EntitySphereGen(world, x, y, z, radius, WarpDriveConfig.gasID, color, hollow, true);
		esg.xCoord = x;
		esg.yCoord = y;
		esg.zCoord = z;
		world.spawnEntityInWorld(esg);
	}

	private void generateSmallShip(World world, int x, int y, int z)
	{
		x = x + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(10));
		y = y + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(10));
		z = z + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(10));
		System.out.println("Generating small ship at " + x + " " + y + " " + z);
		new WorldGenSmallShip(world.rand.nextBoolean()).generate(world, world.rand, x, y, z);
	}

	private void generateRandomAsteroid(World world, int x, int y, int z, int asteroidSizeMax, int centerRadiusMax)
	{
		if (world.rand.nextInt(30) == 1)
		{
			int[] t = new int[] {0, 0};
			if (world.rand.nextInt(25) == 1)
				while(t[0] == 0)
					t = WarpDriveConfig.i.getRandomNetherBlock(world.rand, 0, 0);
			else if (world.rand.nextInt(50) == 1)
				while(t[0] == 0)
					t = WarpDriveConfig.i.getRandomEndBlock(world.rand, 0, 0);
			else
				while(t[0] == 0)
					t = WarpDriveConfig.i.getRandomOverworldBlock(world.rand, 0, 0);
			generateAsteroidOfBlock(world, x, y, z, asteroidSizeMax, centerRadiusMax, t[0], t[1]);
		}
		else
			generateAsteroid(world, x, y, z, asteroidSizeMax, centerRadiusMax);
	}

	public void generateAsteroidField(World world, int x, int y, int z)
	{
		int numOfAsteroids = 15 + world.rand.nextInt(30);
		int numOfClouds = 5 + world.rand.nextInt(10);
		// Minimal distance between asteroids in field
		final int FIELD_ASTEROID_MIN_DISTANCE = 5;
		// Maximum distance
		final int FIELD_ASTEROID_MAX_DISTANCE = 100;
		// Setting up of big asteroids
		for (int i = 1; i <= numOfAsteroids; i++)
		{
			int aX = x + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
			int aY = y + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
			int aZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
			// Place an asteroid
			generateRandomAsteroid(world, aX, aY, aZ, 4, 6);
		}
		// Setting up small asteroids
		for (int i = 1; i <= numOfAsteroids; i++)
		{
			int aX = x + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
			int aY = y + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
			int aZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
			// Placing
			if (world.rand.nextInt(100) != 0)
				generateRandomAsteroid(world, aX, aY, aZ, 2, 2);
			else
				generateSmallShip(world, aX, aY, aZ);
		}
		// Setting up gas clouds
		for (int i = 1; i <= numOfClouds; i++)
		{
			int aX = x + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
			int aY = y + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
			int aZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * (FIELD_ASTEROID_MIN_DISTANCE + world.rand.nextInt(FIELD_ASTEROID_MAX_DISTANCE)));
			// Placing
			if (world.rand.nextInt(10) != 0)
				generateGasCloudOfColor(world, aX, aY, aZ, 12, 15, world.rand.nextInt(12));
		}
	}

	/**
	 * Gas cloud generator
	 *
	 * @param x x-coord of center
	 * @param y center
	 * @param z center
	 * @param cloudSizeMax maximum gas cloud size (by number of balls it consists)
	 * @param centerRadiusMax maximum radius of central ball
	 */
	public void generateGasCloudOfColor(World world, int x, int y, int z, int cloudSizeMax, int centerRadiusMax, int color)
	{
		int cloudSize = 1 + world.rand.nextInt(20);
		if (cloudSizeMax != 0)
			cloudSize = Math.min(cloudSizeMax, cloudSize);
		int centerRadius = 1 + world.rand.nextInt(20);
		if (centerRadiusMax != 0)
			centerRadius = Math.min(centerRadiusMax, centerRadius);
		final int CENTER_SHIFT = 2; // Offset from center of central ball
		// Asteroid's center
		generateGasSphereEntity(world, x, y, z, centerRadius, false, color);
		// Asteroids knolls
		for (int i = 1; i <= cloudSize; i++)
		{
			int radius = 2 + world.rand.nextInt(centerRadius);
			int newX = x + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			int newY = y + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			int newZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			generateGasSphereEntity(world, newX, newY, newZ, radius, false, color);
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
	private void generateAsteroidOfBlock(World world, int x, int y, int z, int asteroidSizeMax, int centerRadiusMax, int blockID, int meta)
	{
		int asteroidSize = 1 + world.rand.nextInt(6);
		if (asteroidSizeMax != 0)
			asteroidSize = Math.min(asteroidSizeMax, asteroidSize);
		int centerRadius = 1 + world.rand.nextInt(6);
		if (centerRadiusMax != 0)
			centerRadius = Math.min(centerRadiusMax, centerRadius);
		final int CENTER_SHIFT = 2; // Offset from center of central ball
		// Asteroid's center
		int[] t = WarpDriveConfig.i.getDefaultSurfaceBlock(world.rand, true, false);
		generateSphere2(world, x, y, z, centerRadius, true, blockID, meta, false, t);
		// Asteroids knolls
		for (int i = 1; i <= asteroidSize; i++)
		{
			int radius = 2 + world.rand.nextInt(centerRadius);
			int newX = x + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			int newY = y + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			int newZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			generateSphere2(world, newX, newY, newZ, radius, true, blockID, meta, false, t);
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
	public void generateAsteroid(World world, int x, int y, int z, int asteroidSizeMax, int centerRadiusMax)
	{
		int asteroidSize = 1 + world.rand.nextInt(6);
		if (asteroidSizeMax != 0)
			asteroidSize = Math.min(asteroidSizeMax, asteroidSize);
		int centerRadius = 1 + world.rand.nextInt(6);
		if (centerRadiusMax != 0)
			centerRadius = Math.min(centerRadiusMax, centerRadius);
		final int CENTER_SHIFT = 2;
		int[] t = WarpDriveConfig.i.getDefaultSurfaceBlock(world.rand, true, false);
		generateSphere2(world, x, y, z, centerRadius, true, -1, 0, false, t);
		for (int i = 1; i <= asteroidSize; i++)
		{
			int radius = 2 + world.rand.nextInt(centerRadius);
			int newX = x + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			int newY = y + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			int newZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			generateSphere2(world, newX, newY, newZ, radius, true, -1, 0, false, t);
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
	public void generateSphere2(World world, int xCoord, int yCoord, int zCoord, double radius, boolean corrupted, int forcedID, int meta, boolean hollow)
	{
		if (forcedID == -1)
			generateSphere2(world, xCoord, yCoord, zCoord, radius, corrupted, forcedID, meta, hollow, WarpDriveConfig.i.getDefaultSurfaceBlock(world.rand, corrupted, false));
		else
			generateSphere2(world, xCoord, yCoord, zCoord, radius, corrupted, forcedID, meta, hollow, new int[] {forcedID, meta});
	}

	public void generateSphere2(World world, int xCoord, int yCoord, int zCoord, double radius, boolean corrupted, int forcedID, int meta, boolean hollow, int[] defaultBlock)
	{
		radius += 0.5D; // Radius from center of block
		double radiusSq = radius * radius; // Optimization to avoid sqrts...
		double radius1Sq = (radius - 1.0D) * (radius - 1.0D); // for hollow sphere
		int ceilRadius = (int) Math.ceil(radius);

		int[] blockID;
		if (forcedID == 0)
			blockID = new int[] {0, 0};
		else if (forcedID == -1)
			blockID = new int[] {forcedID, meta};//SRANYA JABA might not have been initialized
		else
			blockID = new int[] {forcedID, meta};
		// Pass the cube and check points for sphere equation x^2 + y^2 + z^2 = r^2
		for (int x = 0; x <= ceilRadius; x++)
			for (int y = 0; y <= ceilRadius; y++)
				for (int z = 0; z <= ceilRadius; z++)
				{
					double dSq = lengthSq(x, y, z); // Distance from current position to center

					// Skip too far blocks
					if (dSq > radiusSq)
						continue;

					// Hollow sphere condition
					if ((hollow) && ((dSq < radius1Sq) || ((lengthSq(x + 1, y, z) <= radiusSq) && (lengthSq(x, y + 1, z) <= radiusSq) && (lengthSq(x, y, z + 1) <= radiusSq))))
						continue;

					// Place blocks
					if (!corrupted || world.rand.nextInt(10) != 1)
					{
						if (forcedID == -1)
							blockID = WarpDriveConfig.i.getRandomSurfaceBlock(world.rand, defaultBlock[0], defaultBlock[1], false);
						world.setBlock(xCoord + x, yCoord + y, zCoord + z, blockID[0], blockID[1], 2);
						world.setBlock(xCoord - x, yCoord + y, zCoord + z, blockID[0], blockID[1], 2);
					}
					if (!corrupted || world.rand.nextInt(10) != 1)
					{
						if (forcedID == -1)
							blockID = WarpDriveConfig.i.getRandomSurfaceBlock(world.rand, defaultBlock[0], defaultBlock[1], false);
						world.setBlock(xCoord + x, yCoord - y, zCoord + z, blockID[0], blockID[1], 2);
						world.setBlock(xCoord + x, yCoord + y, zCoord - z, blockID[0], blockID[1], 2);
					}
					if (!corrupted || world.rand.nextInt(10) != 1)
					{
						if (forcedID == -1)
							blockID = WarpDriveConfig.i.getRandomSurfaceBlock(world.rand, defaultBlock[0], defaultBlock[1], false);
						world.setBlock(xCoord - x, yCoord - y, zCoord + z, blockID[0], blockID[1], 2);
						world.setBlock(xCoord + x, yCoord - y, zCoord - z, blockID[0], blockID[1], 2);
					}
					if (!corrupted || world.rand.nextInt(10) != 1)
					{
						if (forcedID == -1)
							blockID = WarpDriveConfig.i.getRandomSurfaceBlock(world.rand, defaultBlock[0], defaultBlock[1], false);
						world.setBlock(xCoord - x, yCoord + y, zCoord - z, blockID[0], blockID[1], 2);
						world.setBlock(xCoord - x, yCoord - y, zCoord - z, blockID[0], blockID[1], 2);
					}
				}
	}

	private static double lengthSq(double x, double y, double z)
	{
		return (x * x) + (y * y) + (z * z);
	}
}
