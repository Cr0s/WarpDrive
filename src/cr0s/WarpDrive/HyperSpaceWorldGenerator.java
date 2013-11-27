package cr0s.WarpDrive;

import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * @author Cr0s
 */
public class HyperSpaceWorldGenerator implements IWorldGenerator
{
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
        if (world.provider.dimensionId != WarpDrive.instance.hyperSpaceDimID)
        {
            // ...
        }
    }
}
