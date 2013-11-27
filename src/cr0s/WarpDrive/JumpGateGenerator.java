package cr0s.WarpDrive;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class JumpGateGenerator
{
    public static final int GATE_SIZE = 52;
    public static final int GATE_LENGTH = 37;

    public static final int GATE_LENGTH_HALF = GATE_LENGTH / 2;
    public static final int GATE_SIZE_HALF = GATE_SIZE / 2;

    public static void generate(World worldObj, int x, int y, int z)
    {
        for (int length = -GATE_LENGTH_HALF; length < GATE_LENGTH_HALF; length++)
        {
            for (int newZ = z - GATE_SIZE_HALF; newZ <= z + GATE_SIZE_HALF; newZ++)
            {
                worldObj.setBlock(x + (2 * length), y + GATE_SIZE_HALF, newZ, Block.bedrock.blockID);
                worldObj.setBlock(x + (2 * length), y - GATE_SIZE_HALF, newZ, Block.bedrock.blockID);
            }

            for (int newY = y - GATE_SIZE_HALF; newY <= y + GATE_SIZE_HALF; newY++)
            {
                worldObj.setBlock(x + (2 * length), newY, z + GATE_SIZE_HALF, Block.bedrock.blockID);
                worldObj.setBlock(x + (2 * length), newY, z - GATE_SIZE_HALF, Block.bedrock.blockID);
            }
        }

        for (int length = -GATE_LENGTH; length < GATE_LENGTH; length++)
        {
            worldObj.setBlock(x + length, y + GATE_SIZE_HALF, z, Block.bedrock.blockID);
            worldObj.setBlock(x + length, y - GATE_SIZE_HALF, z, Block.bedrock.blockID);
            worldObj.setBlock(x + length, y + GATE_SIZE_HALF, z + 1, Block.bedrock.blockID);
            worldObj.setBlock(x + length, y - GATE_SIZE_HALF, z - 1, Block.bedrock.blockID);
            worldObj.setBlock(x + length, y + GATE_SIZE_HALF, z + 2, Block.bedrock.blockID);
            worldObj.setBlock(x + length, y - GATE_SIZE_HALF, z - 2, Block.bedrock.blockID);
            worldObj.setBlock(x + length, y, z - GATE_SIZE_HALF, Block.bedrock.blockID);
            worldObj.setBlock(x + length, y, z + GATE_SIZE_HALF, Block.bedrock.blockID);
            worldObj.setBlock(x + length, y + 1, z - GATE_SIZE_HALF, Block.bedrock.blockID);
            worldObj.setBlock(x + length, y - 1, z + GATE_SIZE_HALF, Block.bedrock.blockID);
            worldObj.setBlock(x + length, y + 2, z - GATE_SIZE_HALF, Block.bedrock.blockID);
            worldObj.setBlock(x + length, y - 2, z + GATE_SIZE_HALF, Block.bedrock.blockID);
        }
    }
}
