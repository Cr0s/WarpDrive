package shipmod.entity;

import net.minecraft.block.Block;
import shipmod.ShipMod;

public class ShipCapabilities
{
    public float speedMultiplier = 1.0F;
    public float rotationMultiplier = 1.0F;
    public float liftMultiplier = 1.0F;
    public float brakeMult = 0.9F;
    private int balloons = 0;
    private int floaters = 0;
    private int blockCount = 0;
    private float mass = 0.0F;
    private boolean hasSigns = false;

    public boolean canFly()
    {
        return true;
    }

    public float getMass()
    {
        return this.mass;
    }

    public boolean hasSigns()
    {
        return this.hasSigns;
    }

    protected void onChunkBlockAdded(int id, int metadata)
    {
        if (id == Block.signPost.blockID || id == Block.signWall.blockID)
        {
            this.hasSigns = true;
        }

        ++this.blockCount;
    }
}
