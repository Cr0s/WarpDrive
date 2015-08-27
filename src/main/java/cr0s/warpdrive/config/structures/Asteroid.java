package cr0s.warpdrive.config.structures;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;

import org.w3c.dom.Element;

import cr0s.warpdrive.config.InvalidXmlException;

public class Asteroid extends Orb {

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

		int maxInc = maxCoreSize - minCoreSize;

		new WorldGenMinable(coreBlock, minCoreSize + rand.nextInt(maxInc), Blocks.air).generate(world, rand, x, y, z);

		//TODO: Build layer around it

		return true;
	}

}
