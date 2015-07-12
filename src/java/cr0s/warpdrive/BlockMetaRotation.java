package cr0s.warpdrive;

public class BlockMetaRotation
{
    public final int blockID;
    public final int[] metaRotation;
    private int bitMask;

    protected BlockMetaRotation(int blockid, int[] metarotation, int bitmask)
    {
        if (metarotation.length != 4)
        {
            throw new IllegalArgumentException("MetaRotation int array must have length 4");
        }
        else
        {
            this.blockID = blockid;
            this.metaRotation = metarotation;
            this.bitMask = bitmask;
        }
    }

    public int getRotatedMeta(int currentmeta, int rotate)
    {
        for (int i = 0; i < this.metaRotation.length; ++i)
        {
            if (this.metaRotation[i] == (currentmeta & this.bitMask))
            {
                int mr = currentmeta & ~this.bitMask | this.metaRotation[wrapRotationIndex(i + rotate)] & this.bitMask;
                return mr;
            }
        }

        return currentmeta;
    }

    public static int wrapRotationIndex(int i)
    {
        return i & 3;
    }
}
