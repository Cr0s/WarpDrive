package cr0s.WarpDrive;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public final class EntitySphereGen extends Entity {
    public int xCoord;
    public int yCoord;
    public int zCoord;
    
    public int type;
    
    public int blockID;
    public double radius, radiusSq, radius1Sq, surfaceRadius;
    public int ceilRadius;
    public boolean hollow = false;
    
    private final int BLOCKS_PER_TICK = 2500;
    private final int TYPE_STAR = 1;
    
    private int currX, currY, currZ;
    int blocksPassed, blocksTotal;
    
    public boolean on;
    
    public EntitySphereGen(World world) {
        super(world);
    }

    public EntitySphereGen(World world, int x, int y, int z, int type) {
        super(world);
        
        this.xCoord = x;
        this.posX = (double) x;

        this.yCoord = y;
        this.posY = (double) y;

        this.zCoord = z;
        this.posZ = (double) z;
        
        this.currX = 0;
        this.currY = 0;
        this.currZ = 0;
         
        this.type = type;
        
        if (type == TYPE_STAR) 
        {
            blockID = Block.glowStone.blockID;
            //hollow = true;
            surfaceRadius = 10D;
            radius = 64D;
        }
        
        //radius += 0.5D;
        radiusSq = radius * radius;
        radius1Sq = (radius - 1.0D) * (radius - 1.0D); 

        ceilRadius = (int) Math.ceil(radius);  
        
        startGenerate();
    }

    public void startGenerate() {
        this.on = true;
        System.out.println("[SGEN] Generating sphere on (" + xCoord + "; " + yCoord + "; " + zCoord + ") with radius " + ceilRadius + " type: " + type);        
    }
    
    public void killEntity() {
        on = false;
        worldObj.removeEntity(this);
    }

    @SideOnly(Side.SERVER)
    @Override
    public void onUpdate() {
        if (!on)
        {
            System.out.println("[SGEN] onUpdate(): entity disabled.");
            killEntity();
            return;
        }
        
        System.out.print("[SGEN] Tick of generation...");
        
        blocksPassed = 0;
        
        for (; currX <= ceilRadius; currX++) {
            if (blocksPassed > BLOCKS_PER_TICK) break;
            
            for (currY = 0; currY <= ceilRadius; currY++) {
                if (blocksPassed > BLOCKS_PER_TICK) break;
                
                for (currZ = 0; currZ <= ceilRadius; currZ++) {
                    if (blocksPassed > BLOCKS_PER_TICK) break; 
                                       
                    double dSq = lengthSq(currX, currY, currZ);                  
                    
                    blocksTotal++;
                    blocksPassed++;
                    
                    //if (blocksTotal % 1000 == 0) {
                        //System.out.println("[SGEN]: current (" + currX + "; " + currY + "; " + currZ + "); Blocks passed: " + blocksPassed + ", total: " + blocksTotal);
                    //}                    
                    
                   // if (currX > ceilRadius && currY > ceilRadius && currZ > ceilRadius)
                   // {
                   //     System.out.println("[SGEN] DONE");
                   //     killEntity();
                   //     return;            
                   // }
                    
                    if (dSq > radiusSq) {
                        continue;
                    }

                    if ((hollow) && (
                          (dSq < radius1Sq) || ((lengthSq(currX + 1, currY, currZ) <= radiusSq) && (lengthSq(currX, currY + 1, currZ) <= radiusSq) && (lengthSq(currX, currY, currZ + 1) <= radiusSq))))
                    {
                          continue;
                    }

                    int  meta = 0;

                    worldObj.setBlock(xCoord + currX, yCoord + currY, zCoord + currZ, blockID, meta, 0);
                    worldObj.setBlock(xCoord - currX, yCoord + currY, zCoord + currZ, blockID, meta, 0);


                    worldObj.setBlock(xCoord + currX, yCoord - currY, zCoord + currZ, blockID, meta, 0);
                    worldObj.setBlock(xCoord + currX, yCoord + currY, zCoord - currZ, blockID, meta, 0);


                    worldObj.setBlock(xCoord - currX, yCoord - currY, zCoord + currZ, blockID, meta, 0);
                    worldObj.setBlock(xCoord + currX, yCoord - currY, zCoord - currZ, blockID, meta, 0);


                    worldObj.setBlock(xCoord - currX, yCoord + currY, zCoord - currZ, blockID, meta, 0);
                    worldObj.setBlock(xCoord - currX, yCoord - currY, zCoord - currZ, blockID, meta, 0);
                }
            }
        }

        System.out.println(" [tick done]");
    }
    
    private static double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }    
    
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
    }

    @Override
    protected void entityInit() {
        System.out.println("[SGEN] entityInit() called");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound var1) {
    }
}