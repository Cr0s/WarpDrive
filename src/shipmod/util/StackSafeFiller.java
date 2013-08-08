package shipmod.util;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.ChunkPosition;

public class StackSafeFiller
{
    private Set<ChunkPosition> filledCoords = new HashSet();
    private ChunkPosition nextPos = null;

    public void fill(int x, int y, int z)
    {
        if (!this.hasNext())
        {
            ChunkPosition pos = new ChunkPosition(x, y, z);

            if (!this.filledCoords.contains(pos))
            {
                this.nextPos = pos;
                this.filledCoords.add(pos);
            }
        }
    }

    public ChunkPosition next()
    {
        ChunkPosition pos = this.nextPos;
        this.nextPos = null;
        return pos;
    }

    public boolean hasNext()
    {
        return this.nextPos != null;
    }
}
