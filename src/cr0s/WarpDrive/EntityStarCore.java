package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public final class EntityStarCore extends Entity
{
    public int xCoord;
    public int yCoord;
    public int zCoord;

    private int radius;

    private final int KILL_RADIUS = 60;
    private final int BURN_RADIUS = 200;
    //private final int ROCKET_INTERCEPT_RADIUS = 100; //disabled

    private final int ENTITY_ACTION_INTERVAL = 10; // ticks

    private int ticks = 0;

    public EntityStarCore(World world)
    {
        super(world);
    }

    public EntityStarCore(World world, int x, int y, int z, int radius)
    {
        super(world);
        this.xCoord = x;
        this.posX = (double) x;
        this.yCoord = y;
        this.posY = (double) y;
        this.zCoord = z;
        this.posZ = (double) z;
        this.radius = radius;
    }

    private void actionToEntitiesNearStar()
    {
        int xmax, ymax, zmax, x1, x2, z1, z2;
        int xmin, ymin, zmin;
        final int CUBE_SIDE = this.radius + KILL_RADIUS + BURN_RADIUS;// + ROCKET_INTERCEPT_RADIUS;
        x1 = xCoord + CUBE_SIDE;
        x2 = xCoord - CUBE_SIDE;

        if (x1 < x2)
        {
            xmin = x1;
            xmax = x2;
        }
        else
        {
            xmin = x2;
            xmax = x1;
        }

        z1 = zCoord + CUBE_SIDE;
        z2 = zCoord - CUBE_SIDE;

        if (z1 < z2)
        {
            zmin = z1;
            zmax = z2;
        }
        else
        {
            zmin = z2;
            zmax = z1;
        }

        ymax = yCoord + CUBE_SIDE;
        ymin = yCoord - CUBE_SIDE;
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xmin, ymin, zmin, xmax, ymax, zmax);
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, aabb);

        /*System.out.println("-");
        System.out.println("Coordinates: (" + xCoord + "; " + yCoord + "; " + zCoord + "). Cube side: " + CUBE_SIDE);
        System.out.println("[" + this + "] X: " + xmin + " -> " + xmax);
        System.out.println("[" + this + "] Y: " + ymin + " -> " + ymax);
        System.out.println("[" + this + "] Z: " + zmin + " -> " + zmax);
        */
        for (Object o : list)
        {
            if (o == null || !(o instanceof Entity))
            {
                continue;
            }

            if (o instanceof EntityLivingBase)
            {
                EntityLivingBase entity = (EntityLivingBase)o;

                //System.out.println("Found: " + entity.getEntityName() + " distance: " + entity.getDistanceToEntity(this));
                if (entity.getDistanceToEntity(this) <= (this.radius + KILL_RADIUS))
                {
                    // 100% kill, ignores any protection
                    entity.attackEntityFrom(DamageSource.onFire, 9000);
                }
                else if (entity.getDistanceToEntity(this) <= (this.radius + BURN_RADIUS))
                {
                    // burn entity to 100 seconds
                    entity.setFire(100);
                    entity.attackEntityFrom(DamageSource.onFire, 1);
                }
            }/* else { // Intercept ICBM rocket and kill

                Entity entity = (Entity) o;
                if (entity.getDistanceToEntity(this) <= (this.radius + ROCKET_INTERCEPT_RADIUS)) {
                    System.out.println("[SC] Intercepted entity: " + entity.getEntityName());
                    worldObj.removeEntity(entity);
                }
            }*/
        }
    }

    public void killEntity()
    {
        worldObj.removeEntity(this);
    }

    @Override
    public void onUpdate()
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return;
        }

        if (++ticks > ENTITY_ACTION_INTERVAL)
        {
            ticks = 0;
            actionToEntitiesNearStar();
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        this.xCoord = nbttagcompound.getInteger("x");
        this.yCoord = nbttagcompound.getInteger("y");
        this.zCoord = nbttagcompound.getInteger("z");
        this.radius = nbttagcompound.getInteger("radius");
    }

    @Override
    protected void entityInit()
    {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setInteger("x", this.xCoord);
        nbttagcompound.setInteger("y", this.yCoord);
        nbttagcompound.setInteger("z", this.zCoord);
        nbttagcompound.setInteger("radius", this.radius);
    }
    
    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return false;
    }    
}