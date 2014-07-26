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
		if (world.provider.dimensionId != WarpDriveConfig.G_SPACE_DIMENSION_ID)
			return;
		int x = (chunkX * 16) + (5 - random.nextInt(10));
		int z = (chunkZ * 16) + (5 - random.nextInt(10));
		if (Math.abs(x) > WarpDrive.WORLD_LIMIT_BLOCKS || Math.abs(z) > WarpDrive.WORLD_LIMIT_BLOCKS)
			return;
		int y = Y_LIMIT_DOWN + random.nextInt(Y_LIMIT - Y_LIMIT_DOWN);
		// Moon setup
		if (random.nextInt(700) == 1)
			generateMoon(world, x, y, z);
		// Simple asteroids
		else if (random.nextInt(150) == 1) {
			generateAsteroidOfBlock(world, x, y, z, 6, 11, -1, 0);
		// Random asteroid of block
		} else if (random.nextInt(400) == 1) {
			generateRandomAsteroid(world, x, y, z, 6, 11);
			if (random.nextBoolean()) {
				generateGasCloudOfColor(world, x, y, z, 6, 11, random.nextInt(12));
			}
		} else if (random.nextInt(200) == 1) {// Ice asteroid
			generateAsteroidOfBlock(world, x, y, z, 6, 11, Block.ice.blockID, 0);
		} else if (random.nextInt(500) == 1) {// Asteroid field
			generateAsteroidField(world, x, y, z);
		} else if (random.nextInt(1400) == 1) {// Diamond asteroid
			generateAsteroidOfBlock(world, x, y, z, 3, 2, Block.oreDiamond.blockID, 0);
			// Diamond block core
			world.setBlock(x, y, z, Block.blockDiamond.blockID, 0, 2);
			if (random.nextBoolean()) {
				generateGasCloudOfColor(world, x, y, z, 6, 11, random.nextInt(12));
			}
		} else if (WarpDriveConfig.isAELoaded && random.nextInt(1600) == 1) {// Quartz asteroid
			generateAsteroidOfBlock(world, x, y, z, 3, 2, WarpDriveConfig.getAEBlock("blkQuartzOre").itemID, WarpDriveConfig.getAEBlock("blkQuartzOre").getItemDamage());
			if (random.nextBoolean()) {
				generateGasCloudOfColor(world, x, y, z, 4, 7, random.nextInt(12));
			}
		}
	}

	public void generateMoon(World world, int x, int y, int z) {
		int coreRadius = 5 + world.rand.nextInt(12);
		int moonRadius = coreRadius + 5 + world.rand.nextInt(26);
		int y2 = Math.max(moonRadius + 6, Math.min(y, 255 - moonRadius - 6));
		System.out.println("Generating moon at " + x + " " + y2 + " " + z);

		int[] block = WarpDriveConfig.getDefaultSurfaceBlock(world.rand, world.rand.nextInt(10) > 8, true);

		// Generate moon's core
		if (block[0] == Block.netherrack.blockID) {
			generateSphereDirect(world, x, y2, z, coreRadius, false, Block.lavaStill.blockID, 0, false); // Lava core
			world.setBlock(x, y2, z, Block.bedrock.blockID, 0, 0);
		} else if (block[0] != Block.whiteStone.blockID) {
			if (world.rand.nextInt(100) >= 10) {
				generateSphereDirect(world, x, y2, z, coreRadius, false, Block.lavaStill.blockID, 0, false); // Lava core
				world.setBlock(x, y2, z, Block.bedrock.blockID, 0, 0);
				generateSphereDirect(world, x, y2, z, coreRadius + 1, false, Block.obsidian.blockID, 0, true);  // Obsidian shell
			} else {
				coreRadius = Math.max(coreRadius, 7);
				generateSphereDirect(world, x, y2, z, coreRadius, false, Block.leaves.blockID, 0, false);
				world.setBlock(x, y2, z, Block.bedrock.blockID, 0, 0);
				generateSmallShip(world, x, y2, z, coreRadius - 6);
			}
		}
		
		// Place bedrock blocks
		world.setBlock(x + coreRadius     , y2                  , z                  , Block.bedrock.blockID, 0, 0);
		world.setBlock(x - coreRadius     , y2                  , z                  , Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2                  , z + coreRadius     , Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2                  , z - coreRadius     , Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2 + coreRadius     , z                  , Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2 - coreRadius     , z                  , Block.bedrock.blockID, 0, 0);
		world.setBlock(x + moonRadius / 2 , y2                  , z                  , Block.bedrock.blockID, 0, 0);
		world.setBlock(x - moonRadius / 2 , y2                  , z                  , Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2                  , z + moonRadius / 2 , Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2                  , z - moonRadius / 2 , Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2 + moonRadius / 2 , z                  , Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2 - moonRadius / 2 , z                  , Block.bedrock.blockID, 0, 0);
		world.setBlock(x + moonRadius - 10, y2                  , z                  , Block.bedrock.blockID, 0, 0);
		world.setBlock(x - moonRadius + 10, y2                  , z                  , Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2                  , z + moonRadius - 10, Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2                  , z - moonRadius + 10, Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2 + moonRadius - 10, z                  , Block.bedrock.blockID, 0, 0);
		world.setBlock(x                  , y2 - moonRadius + 10, z                  , Block.bedrock.blockID, 0, 0);
		// Generate moon's blocks
		generateSphereEntity(world, x, y2, z, moonRadius, false, block[0], block[1], true);
		// Generate moon's atmosphere
		if (world.rand.nextBoolean()) {
			generateGasSphereEntity(world, x, y2, z, moonRadius + 2 + world.rand.nextInt(5), true, world.rand.nextInt(12));
		}
	}

	private void placeStarCore(World world, int x, int y, int z, int radius) {
		EntityStarCore core = new EntityStarCore(world, x, y, z, radius);
		core.xCoord = x;
		core.yCoord = y;
		core.zCoord = z;
		core.setPosition((double)x, (double)y, (double)z);
		world.spawnEntityInWorld(core);
	}
	
	public void generateStar(World world, int x, int y, int z, Integer type) {
		Integer starClass = type == -1 ? world.rand.nextInt(3) : type;
		Integer y2 = y;
		System.out.println("Generating star (class " + starClass + ") at " + x + " " + y + " " + z);

		switch (starClass) {
			case 0: // red dwarf
				y2 = Math.max(RED_DWARF_RADIUS + 6, Math.min(y, 255 - RED_DWARF_RADIUS - 6));
				generateSphereEntity(world, x, y2, z, RED_DWARF_RADIUS, false, Block.blockRedstone.blockID, 0, false);
				// Heliosphere of red gas
				generateGasSphereEntity(world, x, y2, z, RED_DWARF_RADIUS + 6, true, 1);
				placeStarCore(world, x, y2, z, RED_DWARF_RADIUS + 6);
				break;

			case 1:
				y2 = Math.max(YELLOW_GIANT_RADIUS + 6, Math.min(y, 255 - YELLOW_GIANT_RADIUS - 6));
				generateSphereEntity(world, x, y2, z, YELLOW_GIANT_RADIUS, false, Block.glowStone.blockID, 0, false);
				// Heliosphere of yellow gas
				generateGasSphereEntity(world, x, y2, z, YELLOW_GIANT_RADIUS + 6, true, 3);
				placeStarCore(world, x, y2, z, YELLOW_GIANT_RADIUS + 6);
				break;

			case 2:
				y2 = Math.max(YELLOW_SUPERGIANT_RADIUS + 6, Math.min(y, 255 - YELLOW_SUPERGIANT_RADIUS - 6));
				generateSphereEntity(world, x, y2, z, YELLOW_SUPERGIANT_RADIUS, false, Block.glowStone.blockID, 0, false);
				// Heliosphere of yellow gas
				generateGasSphereEntity(world, x, y2, z, YELLOW_SUPERGIANT_RADIUS + 6, true, 3);
				placeStarCore(world, x, y2, z, YELLOW_SUPERGIANT_RADIUS + 6);
				break;
		}
	}

	private void generateSphereEntity(World world, int x, int y, int z, int radius, boolean hollow, int blockID, int blockMeta, boolean generateOres) {
		EntitySphereGen esg = new EntitySphereGen(world, x, y, z, radius, blockID, blockMeta, hollow, true, generateOres);
		world.spawnEntityInWorld(esg);
	}

	private void generateGasSphereEntity(World world, int x, int y, int z, int radius, boolean hollow, int color) {
		EntitySphereGen esg = new EntitySphereGen(world, x, y, z, radius, WarpDriveConfig.gasID, color, hollow, true, false);
		world.spawnEntityInWorld(esg);
	}

	private void generateSmallShip(World world, int x, int y, int z, int jitter) {
		x = x + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(jitter));
		y = y + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(jitter));
		z = z + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(jitter));
		System.out.println("Generating small ship at " + x + "," + y + "," + z);
		new WorldGenSmallShip(world.rand.nextBoolean()).generate(world, world.rand, x, y, z);
	}

	public void generateRandomAsteroid(World world, int x, int y, int z, int asteroidSizeMax, int centerRadiusMax) {
		if (world.rand.nextInt(30) == 1) {
			int[] t = new int[] {0, 0};
			if (world.rand.nextInt(25) == 1) {
				while(t[0] == 0) {
					t = WarpDriveConfig.getRandomNetherBlock(world.rand, 0, 0);
				}
			} else if (world.rand.nextInt(35) == 1) {
				while(t[0] == 0) {
					t = WarpDriveConfig.getRandomEndBlock(world.rand, 0, 0);
				}
			} else {
				while(t[0] == 0) {
					t = WarpDriveConfig.getRandomOverworldBlock(world.rand, 0, 0);
				}
			}
			generateAsteroidOfBlock(world, x, y, z, Math.min(3, asteroidSizeMax),  Math.min(2, centerRadiusMax), t[0], t[1]);
		} else {
			generateAsteroidOfBlock(world, x, y, z, asteroidSizeMax, centerRadiusMax, -1, 0);
		}
	}

	private float binomialRandom(World world) {
		float linear = world.rand.nextFloat();
		// ideal sphere repartition = x ^ 0.5 (sqrt)
		// Dilution but slow to compute = 0.5 * ( x ^ 0.3 + 1 + (x - 1) ^ 3 )
		// Optimized 'pushed out' form = 1.25 - 0.625 / (0.5 + 2 * x)
		// Natural sphere with ring = (1 - x ^ 2.5) * x ^ 0.5 + x ^ 4
		
		// rectangular approach: return 0.5F * linear + 0.5F * linear * linear;
		return 1.25F - 0.625F / (0.5F + 2.0F * linear);
	}
	public void generateAsteroidField(World world, int x, int y, int z) {
		LocalProfiler.start("SpaceWorldGenerator.generateAsteroidField");
		// 6.0.1 au = 120 radius with 60 to 140 big + 60 to 140 small + 5 to 13 gaz
		// 45238 blocks surface with 120 to 280 asteroids => 161 to 376 blocks per asteroid (big & small)
		
		// 6.0.2 av big = 80 to 180 radius with 40 to 90 big + 80 to 200 small + 5 to 13 gaz
		// 20106 to 101787 surface with 120 to 290 asteroids => 69 to 848 blocks per asteroid
		
		// 6.0.2 av small = 30 to 80 radius with 2 to 22 big + 15 to 75 small + 0 to 3 gaz
		// 2827 to 20106 surface with 17 to 97 asteroids => 29 to 1182 blocks per asteroid
		
		// random distanced one = 89727 surface 256 asteroids => 350 blocks per asteroid
		
		/*
		boolean isBig = world.rand.nextInt(3) == 1;
		int numOfBigAsteroids, numOfSmallAsteroids, numOfClouds, maxDistance, maxHeight;
		if (isBig) {
			numOfBigAsteroids = 40 + world.rand.nextInt(50);
			numOfSmallAsteroids = 80 + world.rand.nextInt(120);
			numOfClouds = 5 + world.rand.nextInt(8);
			maxDistance = 80 + world.rand.nextInt(100);
			maxHeight = 40 + world.rand.nextInt(40);
		} else {
			numOfBigAsteroids = 2 + world.rand.nextInt(20);
			numOfSmallAsteroids = 15 + world.rand.nextInt(60);
			numOfClouds = 0 + world.rand.nextInt(3);
			maxDistance = 30 + world.rand.nextInt(50);
			maxHeight = 30 + world.rand.nextInt(30);
		}/**/
		
		float surfacePerAsteroid = 80.0F + world.rand.nextFloat() * 300;
		int maxDistance = 30 + world.rand.nextInt(170);
		int maxDistanceBig = Math.round(maxDistance * (0.6F + 0.2F * world.rand.nextFloat()));
		int maxDistanceSmall = Math.round(maxDistance * 1.1F);
		float bigRatio = 0.3F + world.rand.nextFloat() * 0.3F;
		float surfaceBig = (float) (Math.PI * Math.pow(maxDistanceBig, 2));
		float surfaceSmall = (float) (Math.PI * Math.pow(maxDistanceSmall, 2));
		int numOfBigAsteroids = Math.round(bigRatio * surfaceBig / surfacePerAsteroid);
		int numOfSmallAsteroids = Math.round((1.0F - bigRatio) * surfaceSmall / surfacePerAsteroid);
		int numOfClouds = Math.round(numOfBigAsteroids * 1.0F / (10.0F + world.rand.nextInt(10)));
		int maxHeight = 70 + world.rand.nextInt(50);
		int y2 = Math.min(240 - maxHeight, Math.max(y, maxHeight));
		System.out.println("Generating asteroid field at " + x + "," + y + "," + z
					+ " qty " + numOfBigAsteroids + ", " + numOfSmallAsteroids + ", " + numOfClouds
					+ " over " + maxDistance + ", " + maxHeight + " surfacePerAsteroid " + String.format("%.1f", surfacePerAsteroid));
		
		// Setting up of big asteroids
		for (int i = 1; i <= numOfBigAsteroids; i++) {
			float binomial = binomialRandom(world);
			double bearing = world.rand.nextFloat() * 2.0D * Math.PI;
			double yawn = world.rand.nextFloat() * Math.PI;
			float horizontalRange = Math.max(6.0F, binomial * maxDistanceBig);
			float verticalRange = Math.max(3.0F, binomial * maxHeight);
			
			int aX = (int) (x + Math.round(horizontalRange * Math.cos(bearing)));
			int aY = (int) (y + Math.round(verticalRange * Math.cos(yawn)));
			int aZ = (int) (z + Math.round(horizontalRange * Math.sin(bearing)));
			/*System.out.println(String.format("Big asteroid: %.3f %.3f r %.3f r makes %3d, %3d, %3d", new Object[] {
		       		Double.valueOf(binomial),Double.valueOf(bearing), Double.valueOf(yawn),  
		       		Integer.valueOf(aX), Integer.valueOf(aY), Integer.valueOf(aZ)}));/**/
			// Place an asteroid
			generateRandomAsteroid(world, aX, aY, aZ, 4, 6);
		}
		
		// Setting up small asteroids
		for (int i = 1; i <= numOfSmallAsteroids; i++) {
			float binomial = binomialRandom(world);
			double bearing = world.rand.nextFloat() * 2.0D * Math.PI;
			double yawn = world.rand.nextFloat() * Math.PI;
			float horizontalRange = Math.max(6.0F, binomial * maxDistanceSmall);
			float verticalRange = Math.max(3.0F, binomial * maxHeight);
			
			int aX = (int) (x + Math.round(horizontalRange * Math.cos(bearing)));
			int aY = (int) (y + Math.round(verticalRange * Math.cos(yawn)));
			int aZ = (int) (z + Math.round(horizontalRange * Math.sin(bearing)));
			
			// Placing
			if (world.rand.nextInt(400) != 1) {
				generateRandomAsteroid(world, aX, aY, aZ, 3, 3);
			} else {
				generateSmallShip(world, aX, aY, aZ, 8);
			}
		}
		
		// Setting up gas clouds
		for (int i = 1; i <= numOfClouds; i++) {
			float binomial = binomialRandom(world);
			double bearing = world.rand.nextFloat() * 2.0D * Math.PI;
			double yawn = world.rand.nextFloat() * Math.PI;
			float horizontalRange = Math.max(6.0F, binomial * maxDistanceBig);
			float verticalRange = Math.max(3.0F, binomial * maxHeight);
			
			int aX = (int) (x + Math.round(horizontalRange * Math.cos(bearing)));
			int aY = (int) (y + Math.round(verticalRange * Math.cos(yawn)));
			int aZ = (int) (z + Math.round(horizontalRange * Math.sin(bearing)));
			
			// Placing
			if (world.rand.nextBoolean()) {
				generateGasCloudOfColor(world, aX, aY, aZ, 12, 15, world.rand.nextInt(12));
			}
		}
		
		LocalProfiler.stop();
	}

	/**
	 * Gas cloud generator
	 *
	 * @param x coordinate of center
	 * @param y coordinate of center
	 * @param z coordinate of center
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
	 * @param x coordinate of center
	 * @param y coordinate of center
	 * @param z coordinate of center
	 * @param asteroidSizeMax maximum asteroid size (by number of balls it consists)
	 * @param centerRadiusMax maximum radius of central ball
	 */
	private void generateAsteroidOfBlock(World world, int x, int y, int z, int asteroidSizeMax, int centerRadiusMax, int blockID, int meta) {
		// FIXME: get a proper range of random instead of capping it
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
		int[] t = WarpDriveConfig.getDefaultSurfaceBlock(world.rand, true, false);
		generateSphereDirect(world, x, y, z, centerRadius, true, blockID, meta, false, t);
		// Asteroids knolls
		for (int i = 1; i <= asteroidSize; i++) {
			int radius = 1 + world.rand.nextInt(centerRadius);
			int newX = x + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			int newY = y + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			int newZ = z + (((world.rand.nextBoolean()) ? -1 : 1) * world.rand.nextInt(CENTER_SHIFT + centerRadius / 2));
			generateSphereDirect(world, newX, newY, newZ, radius, true, blockID, meta, false, t);
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
	public void generateSphereDirect(World world, int xCoord, int yCoord, int zCoord, double radius, boolean corrupted, int forcedID, int meta, boolean hollow) {
		if (forcedID == -1) {
			generateSphereDirect(world, xCoord, yCoord, zCoord, radius, corrupted, forcedID, meta, hollow, WarpDriveConfig.getDefaultSurfaceBlock(world.rand, corrupted, false));
		} else {
			generateSphereDirect(world, xCoord, yCoord, zCoord, radius, corrupted, forcedID, meta, hollow, new int[] {forcedID, meta});
		}
	}

	public void generateSphereDirect(World world, int xCoord, int yCoord, int zCoord, double radius, boolean corrupted, int forcedID, int meta, boolean hollow, int[] defaultBlock) {
		radius += 0.5D; // Radius from center of block
		double radiusSq = radius * radius; // Optimization to avoid sqrts...
		double radius1Sq = (radius - 1.0D) * (radius - 1.0D); // for hollow sphere
		int ceilRadius = (int) Math.ceil(radius);

		int[] blockID = new int[] {0, 0};
		if (forcedID != -1) {
			blockID = new int[] {forcedID, meta};
		}
		// Pass the cube and check points for sphere equation x^2 + y^2 + z^2 = r^2
		for (int x = 0; x <= ceilRadius; x++) {
			double x2 = (x + 0.5D) * (x + 0.5D); 
			for (int y = 0; y <= ceilRadius; y++) {
				double y2 = (y + 0.5D) * (y + 0.5D); 
				for (int z = 0; z <= ceilRadius; z++) {
					double z2 = (z + 0.5D) * (z + 0.5D); 
					double dSq = x2 + y2 + z2; // Distance from current position to center

					// Skip too far blocks
					if (dSq > radiusSq)
						continue;

					// Hollow sphere condition
					if ((hollow) && ((dSq < radius1Sq) || ((lengthSq(x + 1.5D, y + 0.5D, z + 0.5D) <= radiusSq) && (lengthSq(x + 0.5D, y + 1.5D, z + 0.5D) <= radiusSq) && (lengthSq(x + 0.5D, y + 0.5D, z + 1.5D) <= radiusSq))))
						continue;

					// Place blocks
					if (!corrupted || world.rand.nextInt(5) != 1) {
						if (forcedID == -1)
							blockID = WarpDriveConfig.getRandomSurfaceBlock(world.rand, defaultBlock[0], defaultBlock[1], false);
						world.setBlock(xCoord + x, yCoord + y, zCoord + z, blockID[0], blockID[1], 2);
						world.setBlock(xCoord - x, yCoord + y, zCoord + z, blockID[0], blockID[1], 2);
					}
					if (!corrupted || world.rand.nextInt(5) != 1) {
						if (forcedID == -1)
							blockID = WarpDriveConfig.getRandomSurfaceBlock(world.rand, defaultBlock[0], defaultBlock[1], false);
						world.setBlock(xCoord + x, yCoord - y, zCoord + z, blockID[0], blockID[1], 2);
						world.setBlock(xCoord + x, yCoord + y, zCoord - z, blockID[0], blockID[1], 2);
					}
					if (!corrupted || world.rand.nextInt(5) != 1) {
						if (forcedID == -1)
							blockID = WarpDriveConfig.getRandomSurfaceBlock(world.rand, defaultBlock[0], defaultBlock[1], false);
						world.setBlock(xCoord - x, yCoord - y, zCoord + z, blockID[0], blockID[1], 2);
						world.setBlock(xCoord + x, yCoord - y, zCoord - z, blockID[0], blockID[1], 2);
					}
					if (!corrupted || world.rand.nextInt(5) != 1) {
						if (forcedID == -1)
							blockID = WarpDriveConfig.getRandomSurfaceBlock(world.rand, defaultBlock[0], defaultBlock[1], false);
						world.setBlock(xCoord - x, yCoord + y, zCoord - z, blockID[0], blockID[1], 2);
						world.setBlock(xCoord - x, yCoord - y, zCoord - z, blockID[0], blockID[1], 2);
					}
				}
			}
		}
	}

	private static double lengthSq(double x, double y, double z)
	{
		return (x * x) + (y * y) + (z * z);
	}
}
