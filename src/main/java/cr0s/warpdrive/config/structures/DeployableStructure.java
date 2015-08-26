/**
 *
 */
package cr0s.warpdrive.config.structures;

import net.minecraft.world.gen.feature.WorldGenerator;

/**
 * @author Francesco
 *
 */
public abstract class DeployableStructure extends WorldGenerator {

	protected int height;
	protected int width;
	protected int length;

	public DeployableStructure(int height, int width, int length) {
		this.height = height;
		this.width = width;
		this.length = length;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public int getLength() {
		return length;
	}

}
