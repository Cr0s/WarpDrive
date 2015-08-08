/**
 *
 */
package cr0s.warpdrive.conf;

import net.minecraft.world.gen.feature.WorldGenerator;

/**
 * @author Francesco
 *
 */
public abstract class DeployableStructure extends WorldGenerator {

	private int height;
	private int width;
	private int length;

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
