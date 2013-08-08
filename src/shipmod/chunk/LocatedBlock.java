package shipmod.chunk;

import net.minecraft.world.ChunkPosition;

public class LocatedBlock
{
    public final int blockID;
    public final int blockMeta;
    public final ChunkPosition coords;

    public LocatedBlock(int id, int meta, ChunkPosition coords)
    {
        this.blockID = id;
        this.blockMeta = meta;
        this.coords = coords;
    }

    public String toString()
    {
        return "LocatedBlock [id=" + this.blockID + ", meta=" + this.blockMeta + ", coords=[" + this.coords.x + ", " + this.coords.y + ", " + this.coords.z + "]]";
    }
}
