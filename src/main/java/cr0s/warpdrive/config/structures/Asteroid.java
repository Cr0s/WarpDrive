package cr0s.warpdrive.config.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.w3c.dom.Element;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.MetaBlock;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class Asteroid extends Orb {

	private static final int MIN_RADIUS = 5;

	private Block coreBlock;

	private int maxCoreSize, minCoreSize;

	public Asteroid() {
		super(0); //Diameter not relevant
	}

	@Override
	public void loadFromXmlElement(Element e) throws InvalidXmlException {

		super.loadFromXmlElement(e);

		String coreBlockName = e.getAttribute("coreBlock");
		if (coreBlockName.isEmpty())
			throw new InvalidXmlException("Asteroid is missing a coreBlock!");

		coreBlock = Block.getBlockFromName(coreBlockName);
		if (coreBlock == null)
			throw new InvalidXmlException("Asteroid coreBlock doesnt exist!");

		try {

			maxCoreSize = Integer.parseInt(e.getAttribute("maxCoreSize"));
			minCoreSize = Integer.parseInt(e.getAttribute("minCoreSize"));

		} catch (NumberFormatException gdbg) {
			throw new InvalidXmlException("Asteroid core size dimensions are NaN!");
		}

	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {


		int numberCoreBlocks = minCoreSize + rand.nextInt(maxCoreSize - minCoreSize);
		int randRadius = MIN_RADIUS + rand.nextInt(getRadius() - MIN_RADIUS);

		WarpDrive.logger.info("Asteroid generation: radius=" + randRadius + ", numCoreBlocks=" + numberCoreBlocks);

		//Max theoretical range is a core in a straight line, plus
		ExclusiveLocationFactory locFact = new ExclusiveLocationFactory(randRadius + numberCoreBlocks + 1, x, y, z);

		//Use this to generate a abstract form for the core.
		ArrayList<Location> coreLocations = generateCore(world, rand, x, y, z, numberCoreBlocks, coreBlock, numberCoreBlocks, locFact);

		//Get the initial blocks placed (custom generateCore now just returns them)
		//ArrayList<Location> coreLocations = getAdjacentIdenticalBlocks(world, coreBlock, x, y, z, locFact, true);


		for (int currRad = 1; currRad < randRadius; currRad++) {
			WarpDrive.logger.info("Generating asteroid layer " + currRad + ":" + coreLocations.size());

			//Build a layer over the existing
			coreLocations = addLayer(world, rand, coreLocations, this.getShellForRadius(currRad), locFact);

		}

		return true;
	}

	/**
	 * Get a list of all the blocks that are adjacent to this block, i.e. are within one block radius of the the the passed location.
	 *
	 * If recurse is passed as true, the returned list will also include the results from calling this method on all the blocks that it finds. Thus, it also returns blocks adjacent to the blocks that
	 * are adjacent to the blocks that are adjacent.... you get my meaning.
	 *
	 * Since it uses an ExclusiveLocationFactory, the returned list will be a list of unique locations in no particular order.
	 *
	 * @param world
	 *            World to check the blocks in
	 * @param coreBlock
	 *            The block type that should be looked for
	 * @param xCenter
	 *            The x coordinate to start at
	 * @param yCenter
	 *            The y coordinate to start at
	 * @param zCenter
	 *            The z coordinate to start at
	 * @param locFact
	 *            An ExcelusiveLocationFactory to get locations from.
	 * @param recurse
	 *            Whether to include the output of this method called with each block that the method originally finds.
	 * @return
	 */
	private ArrayList<Location> getAdjacentIdenticalBlocks(World world, Block coreBlock, int xCenter, int yCenter, int zCenter, ExclusiveLocationFactory locFact, boolean recurse) {

		ArrayList<Location> foundBlocks = new ArrayList<Location>();

		//There are a total of 26 blocks around the center one
		for (int x = -1; x <= 1; x++) {

			for (int y = -1; y <= 1; y++) {

				for (int z = -1; z <= 1; z++) {

					//Check if the block is of interest
					if (coreBlock.isAssociatedBlock(world.getBlock(xCenter + x, yCenter + y, zCenter + z))) {

						//Get a location for the block.
						//If the location comes back null, it has already been added, and we dont care about it.
						Location curr = locFact.getLocation(xCenter + x, yCenter + y, zCenter + z);
						if (curr != null) {
							foundBlocks.add(curr);

							//Check whether we should recurse through the blocks,
							// and if so, add all the blocks it finds to the list to return.
							//Make sure not to recurse to the center block, but we are ok with adding it if it is actually the correct block.
							if (recurse && (x != 0 || y != 0 || z != 0))
								foundBlocks.addAll(getAdjacentIdenticalBlocks(world, coreBlock, xCenter + x, yCenter + y, zCenter + z, locFact, true));
						}
					}

				}

			}

		}

		return foundBlocks;
	}

	private ArrayList<Location> addLayer(World world, Random rand, List<Location> blocks, OrbShell orbShell, ExclusiveLocationFactory locFact) {

		ArrayList<Location> addedBlocks = new ArrayList<Location>();

		for (Location baseBlock : blocks) {

			//for each block, get the air blocks adjacent to it, and turn them to a random block from the orbshell.
			for (Location blToChange : getAdjacentIdenticalBlocks(world, Blocks.air, baseBlock.x, baseBlock.y, baseBlock.z, locFact, false)) {

				//Set block without notify
				MetaBlock blType = orbShell.getRandomBlock(rand);
				world.setBlock(blToChange.x, blToChange.y, blToChange.z, blType.block, blType.metadata, 0);

				addedBlocks.add(blToChange);

			}

		}

		return addedBlocks;

	}

	/**
	 * ExclusiveLocationFactory is used so that any one location can only ever be retrieved once.
	 *
	 */
	private class ExclusiveLocationFactory {

		//If a location is true, it has already been retrieved
		private boolean[][][] existingLocs;
		private int centerX, centerY, centerZ;

		/**
		 * Creates a new ExclusiveLocationFactory.
		 *
		 * @param maxRange
		 *            The maximum distance a request could be for
		 * @param centerX
		 *            The center X coordinate
		 * @param centerY
		 *            The center Y coordinate
		 * @param centerZ
		 *            The center Z coordinate
		 */
		public ExclusiveLocationFactory(int maxRange, int centerX, int centerY, int centerZ) {
			existingLocs = new boolean[maxRange][maxRange][maxRange];

			int centerPoint = maxRange / 2;

			this.centerX = centerX + centerPoint;
			this.centerY = centerY + centerPoint;
			this.centerZ = centerZ + centerPoint;
		}

		/**
		 * Gets a location at the coordinates, or null if it has already been retrieved.
		 *
		 * @param x
		 * @param y
		 * @param z
		 * @return
		 */
		public Location getLocation(int x, int y, int z){

			//Get whether the location has already been accessed.
			//Direction doesn't matter, only that there's a difference

			if (!existingLocs[centerX - x][centerY - y][centerZ - z]) {
				existingLocs[centerX - x][centerY - y][centerZ - z] = true;

				return new Location(x, y, z);
			}

			return null;
		}

	}

	/**
	 * Represents a single point in space
	 *
	 */
	private class Location {

		public int x, y, z;

		public Location(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

	}

	/**
	 * Adapted from WorldGenMinable
	 *
	 * @param world
	 * @param rand
	 * @param x
	 * @param y
	 * @param z
	 * @param numberOfBlocks
	 * @param block
	 * @param metadata
	 * @param locFact
	 * @return
	 */
	private ArrayList<Location> generateCore(World world, Random rand, int x, int y, int z, int numberOfBlocks, Block block, int metadata,
			ExclusiveLocationFactory locFact) {

		ArrayList<Location> addedBlocks = new ArrayList<Location>();

		float f = rand.nextFloat() * (float) Math.PI;
		double d0 = x + 8 + MathHelper.sin(f) * numberOfBlocks / 8.0F;
		double d1 = x + 8 - MathHelper.sin(f) * numberOfBlocks / 8.0F;
		double d2 = z + 8 + MathHelper.cos(f) * numberOfBlocks / 8.0F;
		double d3 = z + 8 - MathHelper.cos(f) * numberOfBlocks / 8.0F;
		double d4 = y + rand.nextInt(3) - 2;
		double d5 = y + rand.nextInt(3) - 2;

		for (int l = 0; l <= numberOfBlocks; ++l) {
			double d6 = d0 + (d1 - d0) * l / numberOfBlocks;
			double d7 = d4 + (d5 - d4) * l / numberOfBlocks;
			double d8 = d2 + (d3 - d2) * l / numberOfBlocks;
			double d9 = rand.nextDouble() * numberOfBlocks / 16.0D;
			double d10 = (MathHelper.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0D;
			double d11 = (MathHelper.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0D;
			int i1 = MathHelper.floor_double(d6 - d10 / 2.0D);
			int j1 = MathHelper.floor_double(d7 - d11 / 2.0D);
			int k1 = MathHelper.floor_double(d8 - d10 / 2.0D);
			int l1 = MathHelper.floor_double(d6 + d10 / 2.0D);
			int i2 = MathHelper.floor_double(d7 + d11 / 2.0D);
			int j2 = MathHelper.floor_double(d8 + d10 / 2.0D);

			for (int k2 = i1; k2 <= l1; ++k2) {
				double d12 = (k2 + 0.5D - d6) / (d10 / 2.0D);

				if (d12 * d12 < 1.0D) {
					for (int l2 = j1; l2 <= i2; ++l2) {
						double d13 = (l2 + 0.5D - d7) / (d11 / 2.0D);

						if (d12 * d12 + d13 * d13 < 1.0D) {
							for (int i3 = k1; i3 <= j2; ++i3) {
								double d14 = (i3 + 0.5D - d8) / (d10 / 2.0D);

								if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0D && world.getBlock(k2, l2, i3).isReplaceableOreGen(world, k2, l2, i3, Blocks.air)) {
									world.setBlock(k2, l2, i3, block, metadata, 2);
									addedBlocks.add(locFact.getLocation(k2, l2, i3));
								}
							}
						}
					}
				}
			}
		}

		return addedBlocks;
	}

}
