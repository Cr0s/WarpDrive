package cr0s.WarpDrive.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class JumpBlock
{
    public int blockID;
    public int blockMeta;
    public TileEntity blockTileEntity;
    public NBTTagCompound blockNBT;
    public int x;
    public int y;
    public int z;

    public JumpBlock()
    {
    }

    public JumpBlock(int i, int j, int k, int l, int i1)
    {
        blockID = i;
        blockMeta = j;
        blockTileEntity = null;
        x = k;
        y = l;
        z = i1;
    }

    public JumpBlock(int i, int j, TileEntity tileentity, int k, int l, int i1)
    {
        blockID = i;
        blockMeta = j;
        blockTileEntity = tileentity;
        x = k;
        y = l;
        z = i1;
    }
}
