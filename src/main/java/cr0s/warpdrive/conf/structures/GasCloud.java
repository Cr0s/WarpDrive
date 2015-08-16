package cr0s.warpdrive.conf.structures;

import java.awt.Color;
import java.util.Random;

import net.minecraft.block.Block;

import org.w3c.dom.Element;

import cr0s.warpdrive.WarpDrive;

public class GasCloud extends Orb {

	private Color color;
	
	public GasCloud(int diameter, Color color) {
		super(diameter);

		this.color = color;
	}

	@Override
	public void loadFromXmlElement(Element e) {
		//Not supported
	}

	@Override
	public Block getBlockForRadius(Random r, int radius) {
		return WarpDrive.blockGas; //TODO: Add color support
	}
}
