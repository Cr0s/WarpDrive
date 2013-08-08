package shipmod.entity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.network.NetworkHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumEntitySize;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import shipmod.ShipMod;
import shipmod.chunk.LocatedBlock;
import shipmod.chunk.MobileChunk;
import shipmod.control.ShipController;
import shipmod.control.ShipControllerClient;
import shipmod.util.AABBRotator;
import shipmod.util.MathHelperMod;

public class EntityShip extends EntityBoat implements IEntityAdditionalSpawnData
{
	public int fuelLevel = 0;
    public static final float BASE_FORWARD_SPEED = 0.006F;
    public static final float BASE_TURN_SPEED = 0.34F;
    public static final float BASE_LIFT_SPEED = 0.005F;
    private MobileChunk shipChunk;
    private ShipCapabilities capabilities;
    private ShipController controller;
    public float motionYaw;
    public int frontDirection;
    public int seatX;
    public int seatY;
    public int seatZ;
    private Entity prevRiddenByEntity;
    private boolean isFlying;
    protected float groundFriction;
    protected float horFriction;
    protected float vertFriction;
    private int[] layeredBlockVolumeCount;
    private boolean riddenByOtherPlayer;
    private boolean syncPosWithServer;
    @SideOnly(Side.CLIENT)
    private int boatPosRotationIncrements;
    @SideOnly(Side.CLIENT)
    private double boatX;
    @SideOnly(Side.CLIENT)
    private double boatY;
    @SideOnly(Side.CLIENT)
    private double boatZ;
    @SideOnly(Side.CLIENT)
    private double boatPitch;
    @SideOnly(Side.CLIENT)
    private double boatYaw;
    @SideOnly(Side.CLIENT)
    private double boatVelX;
    @SideOnly(Side.CLIENT)
    private double boalVelY;
    @SideOnly(Side.CLIENT)
    private double boatVelZ;

    public ArrayList<EntityLivingBase> playersOnShip = new ArrayList<EntityLivingBase>();
    public int health;
    
    private int sourceYaw;
    private boolean explosionFlag = false;
    
    public static boolean isAABBInLiquidNotFall(World world, AxisAlignedBB aabb)
    {
        int i = MathHelper.floor_double(aabb.minX);
        int j = MathHelper.floor_double(aabb.maxX + 1.0D);
        int k = MathHelper.floor_double(aabb.minY);
        int l = MathHelper.floor_double(aabb.maxY + 1.0D);
        int i1 = MathHelper.floor_double(aabb.minZ);
        int j1 = MathHelper.floor_double(aabb.maxZ + 1.0D);

        for (int k1 = i; k1 < j; ++k1)
        {
            for (int l1 = k; l1 < l; ++l1)
            {
                for (int i2 = i1; i2 < j1; ++i2)
                {
                    Block block = Block.blocksList[world.getBlockId(k1, l1, i2)];

                    if (block != null && (block.blockMaterial == Material.water || block.blockMaterial == Material.lava))
                    {
                        int j2 = world.getBlockMetadata(k1, l1, i2);
                        double d0 = (double)(l1 + 1);

                        if (j2 < 8)
                        {
                            d0 = (double)(l1 + 1) - (double)j2 / 8.0D;

                            if (d0 >= aabb.minY)
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public EntityShip(World world)
    {
        super(world);
        this.shipChunk = new MobileChunk(world, this);
        this.capabilities = new ShipCapabilities();

        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            this.controller = new ShipControllerClient();
        }
        else
        {
            this.controller = new ShipController();
        }

        this.motionYaw = 0.0F;
        this.layeredBlockVolumeCount = null;
        this.frontDirection = 0;
        this.yOffset = 0.0F;
        this.groundFriction = 0.9F;
        this.horFriction = 0.994F;
        this.vertFriction = 0.95F;
        this.prevRiddenByEntity = null;
        this.isFlying = false;
        this.riddenByOtherPlayer = false;
        this.syncPosWithServer = true;

        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            this.boatPosRotationIncrements = 0;
            this.boatX = this.boatY = this.boatZ = 0.0D;
            this.boatPitch = this.boatYaw = 0.0D;
            this.boatVelX = this.boalVelY = this.boatVelZ = 0.0D;
        }
    }

    @Override
    protected void entityInit()
    {
        this.dataWatcher.addObject(30, Byte.valueOf((byte)0));
    }

    public MobileChunk getShipChunk()
    {
        return this.shipChunk;
    }

    public ShipCapabilities getCapabilities()
    {
        return this.capabilities;
    }

    public ShipController getController()
    {
        return this.controller;
    }

    public void setFrontDirection(int dir)
    {
        this.frontDirection = dir;
    }

    public void onChunkBlockAdded(int id, int metadata)
    {
        if (id != 0)
        {
            this.capabilities.onChunkBlockAdded(id, metadata);
        }
    }

    /**
     * Will get destroyed next tick.
     */
    @Override
    public void setDead()
    {
        super.setDead();
        this.shipChunk.onChunkUnload();
    }

    /**
     * Gets called every tick from main Entity class
     */
    @Override
    public void onEntityUpdate()
    {
        super.onEntityUpdate();

        if (this.shipChunk.chunkUpdated)
        {
            this.shipChunk.chunkUpdated = false;
            this.setSize((float)Math.max(this.shipChunk.maxX() - this.shipChunk.minX(), this.shipChunk.maxZ() - this.shipChunk.minZ()), (float)(this.shipChunk.maxY() - this.shipChunk.minY()));
            World.MAX_ENTITY_RADIUS = Math.max(World.MAX_ENTITY_RADIUS, (double)Math.max(this.width, this.height)) + 2.0D;

            try
            {
                this.fillAirBlocks(new HashSet(), -1, -1, -1);
            }
            catch (StackOverflowError var4)
            {
                System.err.println(var4.toString());
            }

            this.layeredBlockVolumeCount = new int[this.shipChunk.maxX() - this.shipChunk.minX()];

            for (int y = 0; y < this.layeredBlockVolumeCount.length; ++y)
            {
                for (int i = this.shipChunk.minX(); i < this.shipChunk.maxX(); ++i)
                {
                    for (int j = this.shipChunk.minZ(); j < this.shipChunk.maxZ(); ++j)
                    {
                        if (this.shipChunk.isBlockTakingWaterVolume(i, y + this.shipChunk.minY(), j))
                        {
                            ++this.layeredBlockVolumeCount[y];
                        }
                    }
                }
            }

            this.isFlying = this.capabilities.canFly();
        }
    }

    public void setRotatedBoundingBox()
    {
        if (this.shipChunk == null)
        {
            float hw = this.width / 2.0F;
            this.boundingBox.setBounds(this.posX - (double)hw, this.posY, this.posZ - (double)hw, this.posX + (double)hw, this.posY + (double)this.height, this.posZ + (double)hw);
        }
        else
        {
            this.boundingBox.setBounds(this.posX - (double)this.shipChunk.getCenterX(), this.posY, this.posZ - (double)this.shipChunk.getCenterZ(), this.posX + (double)this.shipChunk.getCenterX(), this.posY + (double)this.height, this.posZ + (double)this.shipChunk.getCenterZ());
            AABBRotator.rotateAABBAroundY(this.boundingBox, this.posX, this.posZ, (float)Math.toRadians((double)this.rotationYaw));
        }
    }

    /**
     * Sets the width and height of the entity. Args: width, height
     */
    @Override
    public void setSize(float w, float h)
    {
        float f;

        if (w != this.width || h != this.height)
        {
            this.width = w;
            this.height = h;
            f = w / 2.0F;
            this.boundingBox.setBounds(this.posX - (double)f, this.posY, this.posZ - (double)f, this.posX + (double)f, this.posY + (double)this.height, this.posZ + (double)f);
        }

        f = w % 2.0F;

        if ((double)f < 0.375D)
        {
            this.myEntitySize = EnumEntitySize.SIZE_1;
        }
        else if ((double)f < 0.75D)
        {
            this.myEntitySize = EnumEntitySize.SIZE_2;
        }
        else if ((double)f < 1.0D)
        {
            this.myEntitySize = EnumEntitySize.SIZE_3;
        }
        else if ((double)f < 1.375D)
        {
            this.myEntitySize = EnumEntitySize.SIZE_4;
        }
        else if ((double)f < 1.75D)
        {
            this.myEntitySize = EnumEntitySize.SIZE_5;
        }
        else
        {
            this.myEntitySize = EnumEntitySize.SIZE_6;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)

    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
     * posY, posZ, yaw, pitch
     */
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int incr)
    {
        if (this.riddenByOtherPlayer)
        {
            this.boatPosRotationIncrements = incr + 5;
        }
        else
        {
            double dx = x - this.posX;
            double dy = y - this.posY;
            double dz = z - this.posZ;
            double d = dx * dx + dy * dy + dz * dz;

            if (d < 0.3D)
            {
                return;
            }

            this.syncPosWithServer = true;
            this.boatPosRotationIncrements = incr;
        }

        this.boatX = x;
        this.boatY = y;
        this.boatZ = z;
        this.boatYaw = (double)yaw;
        this.boatPitch = (double)pitch;
        this.motionX = this.boatVelX;
        this.motionY = this.boalVelY;
        this.motionZ = this.boatVelZ;
    }

    @Override
    @SideOnly(Side.CLIENT)

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    public void setVelocity(double x, double y, double z)
    {
        this.boatVelX = this.motionX = x;
        this.boalVelY = this.motionY = y;
        this.boatVelZ = this.motionZ = z;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        this.onEntityUpdate();
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        double horvel = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient() && (this.riddenByOtherPlayer || this.syncPosWithServer))
        {
            this.handleClientUpdate();

            if (this.boatPosRotationIncrements == 0)
            {
                this.syncPosWithServer = false;
            }
        }
        else
        {
            this.handleServerUpdate(horvel);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void handleClientUpdate()
    {
        if (this.boatPosRotationIncrements > 0)
        {
            double dx = this.posX + (this.boatX - this.posX) / (double)this.boatPosRotationIncrements;
            double dy = this.posY + (this.boatY - this.posY) / (double)this.boatPosRotationIncrements;
            double dz = this.posZ + (this.boatZ - this.posZ) / (double)this.boatPosRotationIncrements;
            double ang = MathHelper.wrapAngleTo180_double(this.boatYaw - (double)this.rotationYaw);
            this.rotationYaw = (float)((double)this.rotationYaw + ang / (double)this.boatPosRotationIncrements);
            this.rotationPitch = (float)((double)this.rotationPitch + (this.boatPitch - (double)this.rotationPitch) / (double)this.boatPosRotationIncrements);
            --this.boatPosRotationIncrements;
            this.setPosition(dx, dy, dz);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }
        else
        {
            this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (this.onGround)
            {
                this.motionX *= (double)this.groundFriction;
                this.motionY *= (double)this.groundFriction;
                this.motionZ *= (double)this.groundFriction;
            }

            this.motionX *= (double)this.horFriction;
            this.motionY *= (double)this.vertFriction;
            this.motionZ *= (double)this.horFriction;
        }

        this.setRotatedBoundingBox();
        movePlayers();
    }

    private void movePlayers() {
    	// Grab players on ship
    	List entities = worldObj.getEntitiesWithinAABBExcludingEntity(riddenByEntity, this.boundingBox);
    	if (entities != null && !entities.isEmpty()) {
    		for (Object o : entities) {
    			if (o != null && o instanceof EntityPlayer) {
    				EntityPlayer e = (EntityPlayer)o;
    				
    				if (!e.isSneaking()) {
    					updateRiderPosition(e, seatX, seatY, seatZ);
    				}
    			}
    		}
    	}
    }    
    
    protected void handleServerUpdate(double horvel)
    {
        if (this.riddenByEntity == null && this.prevRiddenByEntity != null)
        {
            this.prevRiddenByEntity.mountEntity(this);

            if (!this.decompileToBlocks(false) && !ShipMod.instance.modConfig.remountOnDecompilationFail)
            {
                this.riddenByEntity.mountEntity((Entity)null);
            }

            this.prevRiddenByEntity = null;
        }

        if (this.riddenByEntity != null)
        {
            this.handlePlayerControl();
            this.prevRiddenByEntity = this.riddenByEntity;
        }

        this.fuelLevel--;
        if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) {    
	        if (fuelLevel == 20 * 10) {
	        	((EntityPlayer)this.riddenByEntity).addChatMessage("[ShipMod] Low fuel level. 10 seconds remaining");
	        }
	        
	        if (fuelLevel <= 0) {
	        	((EntityPlayer)this.riddenByEntity).addChatMessage("[ShipMod] Ship is out of fuel");
	
	        	if (!this.decompileToBlocks(false)) {
	        		dropAsItems();
	        	}
	        }
        }
        double var25 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        double maxvel = (double)ShipMod.instance.modConfig.speedLimit;
        double list;

        if (var25 > maxvel)
        {
            list = maxvel / var25;
            this.motionX *= list;
            this.motionZ *= list;
        }

        this.motionY = MathHelperMod.clamp_double(this.motionY, -maxvel, maxvel);

        if (this.onGround)
        {
            this.motionX *= (double)this.groundFriction;
            this.motionY *= (double)this.groundFriction;
            this.motionZ *= (double)this.groundFriction;
        }

        this.motionYaw *= 0.7F;
        this.rotationYaw += this.motionYaw;
        this.setRotatedBoundingBox();
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.posY = Math.min(this.posY, (double)this.worldObj.getHeight());
        this.motionX *= (double)this.horFriction;
        this.motionY *= (double)this.vertFriction;
        this.motionZ *= (double)this.horFriction;
        this.rotationPitch = 0.0F;

        if (ShipMod.instance.modConfig.shipControlType == 0)
        {
            list = (double)this.rotationYaw;
            double i1 = this.prevPosX - this.posX;
            double k1 = this.prevPosZ - this.posZ;

            if (this.riddenByEntity != null && !this.isBraking() && i1 * i1 + k1 * k1 > 0.01D)
            {
                list = 270.0D - Math.toDegrees(Math.atan2(k1, i1)) + (double)((float)this.frontDirection * 90.0F);
            }

            double i2 = MathHelper.wrapAngleTo180_double(list - (double)this.rotationYaw);
            double maxyawspeed = 2.0D;

            if (i2 > maxyawspeed)
            {
                i2 = maxyawspeed;
            }

            if (i2 < -maxyawspeed)
            {
                i2 = -maxyawspeed;
            }

            this.rotationYaw = (float)((double)this.rotationYaw + i2);
        }

        this.setRotation(this.rotationYaw, this.rotationPitch);
        
        movePlayers();
    }

    private void handlePlayerControl()
    {
        if (this.riddenByEntity instanceof EntityLivingBase)
        {
            double i = (double)((EntityLivingBase)this.riddenByEntity).moveForward;

            if (this.isFlying())
            {
                i *= 0.5D;
            }

            if (ShipMod.instance.modConfig.shipControlType == 1)
            {
                Vec3 dsin = this.worldObj.getWorldVec3Pool().getVecFromPool(this.riddenByEntity.motionX, 0.0D, this.riddenByEntity.motionZ);
                dsin.rotateAroundY((float)Math.toRadians((double)this.riddenByEntity.rotationYaw));
                double steer = (double)((EntityLivingBase)this.riddenByEntity).moveStrafing;
                this.motionYaw = (float)((double)this.motionYaw + steer * 0.3499999940395355D * (double)this.capabilities.rotationMultiplier * (double)ShipMod.instance.modConfig.turnSpeed);
                float yaw = (float)Math.toRadians((double)(180.0F - this.rotationYaw + (float)this.frontDirection * 90.0F));
                dsin.xCoord = this.motionX;
                dsin.zCoord = this.motionZ;
                dsin.rotateAroundY(yaw);
                dsin.xCoord *= 0.9D;
                dsin.zCoord -= i * 0.004999999888241291D * (double)this.capabilities.speedMultiplier;
                dsin.rotateAroundY(-yaw);
                this.motionX = dsin.xCoord;
                this.motionZ = dsin.zCoord;
            }
            else if (ShipMod.instance.modConfig.shipControlType == 0 && i > 0.0D)
            {
                double dsin1 = -Math.sin(Math.toRadians((double)this.riddenByEntity.rotationYaw));
                double dcos = Math.cos(Math.toRadians((double)this.riddenByEntity.rotationYaw));
                this.motionX += dsin1 * 0.004999999888241291D * (double)this.capabilities.speedMultiplier;
                this.motionZ += dcos * 0.004999999888241291D * (double)this.capabilities.speedMultiplier;
            }
        }

        if (this.controller.getShipControl() != 0)
        {
            if (this.controller.getShipControl() == 4)
            {
                this.alignToGrid();
            }
            else if (this.isBraking())
            {
                this.motionX *= (double)this.capabilities.brakeMult;
                this.motionZ *= (double)this.capabilities.brakeMult;

                if (this.isFlying())
                {
                    this.motionY *= (double)this.capabilities.brakeMult;
                }
            }
            else if (this.controller.getShipControl() < 3 && this.capabilities.canFly())
            {
                byte i1;

                if (this.controller.getShipControl() == 2)
                {
                    this.isFlying = true;
                    i1 = 1;
                }
                else
                {
                    i1 = -1;
                }

                this.motionY += (double)((float)i1 * 0.004F * this.capabilities.liftMultiplier);
            }
        }
    }

    public boolean isFlying()
    {
        return true;
    }

    public boolean isBraking()
    {
        return this.controller.getShipControl() == 3;
    }

    @Override
    public boolean func_96092_aw()
    {
        return this.ticksExisted > 60;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void func_70270_d(boolean flag)
    {
        this.riddenByOtherPlayer = flag;
    }

    @Override
    public boolean shouldRiderSit()
    {
        return true;
    }

    @Override
    public void updateRiderPosition()
    {
        this.updateRiderPosition(this.riddenByEntity, this.seatX, this.seatY, this.seatZ);
    }

    public void updateRiderPosition(Entity entity, int seatx, int seaty, int seatz)
    {
        if (entity != null)
        {
            float yaw = (float)Math.toRadians((double)this.rotationYaw);
            int x1 = seatx;
            int y1 = seaty;
            int z1 = seatz;

            if (this.frontDirection == 0)
            {
                z1 = seatz - 1;
            }
            else if (this.frontDirection == 1)
            {
                x1 = seatx + 1;
            }
            else if (this.frontDirection == 2)
            {
                z1 = seatz + 1;
            }
            else if (this.frontDirection == 3)
            {
                x1 = seatx - 1;
            }

            int id = this.shipChunk.getBlockId(x1, MathHelper.floor_double((double)seaty + this.getMountedYOffset() + entity.getYOffset()), z1);

            if (id != 0 && Block.blocksList[id] != null && Block.blocksList[id].isOpaqueCube())
            {
                x1 = seatx;
                y1 = seaty;
                z1 = seatz;
            }

            Vec3 vec = this.worldObj.getWorldVec3Pool().getVecFromPool((double)((float)x1 - this.shipChunk.getCenterX()) + 0.5D, (double)(y1 - this.shipChunk.minY()) + this.getMountedYOffset(), (double)((float)z1 - this.shipChunk.getCenterZ()) + 0.5D);
            vec.rotateAroundY(yaw);
            entity.setPosition(this.posX + vec.xCoord, this.posY + vec.yCoord + entity.getYOffset(), this.posZ + vec.zCoord);
        }
    }

    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
    @Override
    public double getMountedYOffset()
    {
        return (double)this.yOffset + 0.5D;
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    /**
     * Returns a boundingBox used to collide the entity with other entities and blocks. This enables the entity to be
     * pushable on contact, like boats or minecarts.
     */
    @Override
    public AxisAlignedBB getCollisionBox(Entity entity)
    {
        return null;
    }

    /**
     * returns the bounding box for this entity
     */
    @Override
    public AxisAlignedBB getBoundingBox()
    {
        return this.boundingBox;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    @Override
    public boolean canBePushed()
    {
        return false;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    @Override
    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float damage)
    {   	
    	this.health -= damage;
    	System.out.println("[SHIP] Entity damaged: -" + damage + ". Health: " + this.health + "/" + shipChunk.getBlockCount());
    	 
    	if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
    		return true;
    	}
    	
    	if (this.health <= 0 && !this.isDead) {
    		((EntityPlayer)this.riddenByEntity).addChatMessage("[ShipMod] MOVEABLE SHIP IS DESTROYED"); 
    		
    		this.explosionFlag = true;
    		
    		if (!this.decompileToBlocks(false)) {
    			dropAsItems();
    			this.setDead();
    			return true;
    		}  		
    	}
    	
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.5F;
    }

    public float getHorizontalVelocity()
    {
        return (float)Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
    }

    @Override
    public boolean func_130002_c(EntityPlayer entityplayer)
    {
        if (this.riddenByEntity != null && this.riddenByEntity != entityplayer)
        {
            return true;
        }
        else
        {
            if (FMLCommonHandler.instance().getEffectiveSide().isClient() && entityplayer.getDistanceSqToEntity(this) >= 36.0D)
            {
                ByteArrayDataOutput out = ByteStreams.newDataOutput(4);
                out.writeInt(this.entityId);
                Packet250CustomPayload packet = new Packet250CustomPayload("shipInteract", out.toByteArray());
                ((EntityClientPlayerMP)entityplayer).sendQueue.addToSendQueue(packet);
            }

            if (this.riddenByEntity == entityplayer)
            {
                if (ShipMod.instance.modConfig.enableRightClickDismount)
                {
                    this.decompileToBlocks(false);
                }
            }
            else if (!FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
                entityplayer.mountEntity(this);
            }

            return true;
        }
    }

    public void alignToGrid()
    {
        this.rotationYaw = (float)Math.round(this.rotationYaw / 90.0F) * 90.0F;
        this.rotationPitch = 0.0F;
        this.posX = (double)MathHelperMod.round_double(this.posX);
        this.posY = (double)MathHelperMod.round_double(this.posY);
        this.posZ = (double)MathHelperMod.round_double(this.posZ);
        this.motionX = this.motionY = this.motionZ = 0.0D;
    }

    public boolean canDecompile()
    {
        float yaw = (float)Math.round(this.rotationYaw / 90.0F) * 90.0F;
        yaw = (float)Math.toRadians((double)this.rotationYaw);
        float ox = -this.shipChunk.getCenterX();
        float oy = (float)(-this.shipChunk.minY());
        float oz = -this.shipChunk.getCenterZ();
        Vec3 vec = this.worldObj.getWorldVec3Pool().getVecFromPool(0.0D, 0.0D, 0.0D);

        for (int i = this.shipChunk.minX(); i < this.shipChunk.maxX(); ++i)
        {
            for (int j = this.shipChunk.minY(); j < this.shipChunk.maxY(); ++j)
            {
                for (int k = this.shipChunk.minZ(); k < this.shipChunk.maxZ(); ++k)
                {
                    if (!this.shipChunk.isAirBlock(i, j, k))
                    {
                        vec.xCoord = (double)((float)i + ox);
                        vec.yCoord = (double)((float)j + oy);
                        vec.zCoord = (double)((float)k + oz);
                        vec.rotateAroundY(yaw);
                        int ix = MathHelperMod.round_double(vec.xCoord + this.posX);
                        int iy = MathHelperMod.round_double(vec.yCoord + this.posY);
                        int iz = MathHelperMod.round_double(vec.zCoord + this.posZ);
                        int id = this.worldObj.getBlockId(ix, iy, iz);

                        if (id != 0 && Block.blocksList[id] != null && !Block.blocksList[id].isAirBlock(this.worldObj, ix, iy, iz) && !Block.blocksList[id].blockMaterial.isLiquid() && !ShipMod.instance.modConfig.overwritableBlocks.contains(Integer.valueOf(id)))
                        {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public boolean decompileToBlocks(boolean overwrite)
    {    	
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return true;
        }
        else if (!overwrite && !this.canDecompile())
        {
            if (this.prevRiddenByEntity instanceof EntityPlayer)
            {
                ((EntityPlayer)this.prevRiddenByEntity).addChatMessage("Cannot decompile ship here");
            }

            return false;
        }
        else
        {
        	ArrayList<ChunkCoordinates> explosions = new ArrayList<ChunkCoordinates>();
        	ArrayList<TileEntity> IC2MachinesToUpdateFacing = new ArrayList<TileEntity>();
        	
            int currentrotation = Math.round(this.rotationYaw / 90.0F);
            int deltarotation = -(currentrotation & 3);
            this.rotationYaw = (float)currentrotation * 90.0F;
            this.rotationPitch = 0.0F;
            float yaw = (float)Math.toRadians((double)this.rotationYaw);
            this.updateRiderPosition();

            if (!FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
            	this.worldObj.isRemote = true; // lock world from changes
                boolean flag = this.worldObj.getGameRules().getGameRuleBooleanValue("doTileDrops");
                this.worldObj.getGameRules().setOrCreateGameRule("doTileDrops", "false");
                ArrayList list = new ArrayList();
                float ox = -this.shipChunk.getCenterX();
                float oy = (float)(-this.shipChunk.minY());
                float oz = -this.shipChunk.getCenterZ();
                Vec3 vec = this.worldObj.getWorldVec3Pool().getVecFromPool(0.0D, 0.0D, 0.0D);
                
                for (int x = this.shipChunk.minX(); x <= this.shipChunk.maxX(); x++)
                {
                    for (int z = this.shipChunk.minZ(); z <= this.shipChunk.maxZ(); z++)
                    {
                        for (int y = this.shipChunk.minY(); y <= this.shipChunk.maxY(); y++)
                        {
                            TileEntity tileentity = this.shipChunk.getBlockTileEntity(x, y, z);
                            int id = this.shipChunk.getBlockId(x, y, z);
                            int meta = this.shipChunk.getBlockMetadata(x, y, z);

                            if (id == 0)
                            {
                                if (meta == 1)
                                {
                                    continue;
                                }
                            }
                            else if (Block.blocksList[id] == null || Block.blocksList[id].isAirBlock(this.worldObj, x, y, z))
                            {
                                continue;
                            }
                            
                            int newMeta = meta;
                            
                        	for (int i = 0; i < -deltarotation; i++) {
                        		newMeta = ShipMod.instance.metaRotations.rotate90Reverse(id, newMeta);     
                        	}
                        	
                        	if (id == 1225) {
                        		// Rotate computer
                        		for (int i = 0; i < -deltarotation; i++) {
                        			newMeta = ShipMod.instance.metaRotations.rotateComputer90Reverse(newMeta);     
                        		}                
                        	} else if (id == 1226) { // Rotate peripherals (Disk drive, printer, modem, monitors)
                            	NBTTagCompound NBT = new NBTTagCompound();
                            	tileentity.writeToNBT(NBT);                        		
                        		int oldDir = NBT.getInteger("dir");
                        		int newDir = oldDir;
                        		int subType = ShipMod.instance.metaRotations.getCCSubtypeFromMetadata(meta);
                        		
                        		
                        		if (subType == 0) { // Disk drive
                            		for (int i = 0; i < -deltarotation; i++) {
                            			newMeta = ShipMod.instance.metaRotations.rotateCCBlock90Reverse(newMeta);     
                            		}    
                        		} else if (subType == 1) { // Modems
                            		/*System.out.println("Delta: " + deltarotation);
                            		System.out.println("Old dir: " + oldDir);
                            		System.out.println("Old meta: " + meta);
                            		System.out.println("Rot from meta: " + ShipMod.instance.metaRotations.getCCDirectionFromMetadata(meta)); 
                            		*/
                        			newMeta = ShipMod.instance.metaRotations.getCCDirectionFromMetadata(meta);
                            		
                            		for (int i = 0; i < -deltarotation; i++) {
                            			newMeta = ShipMod.instance.metaRotations.rotateCCBlock90Reverse(newMeta); 
                            			newDir = ShipMod.instance.metaRotations.rotateCCBlock90Reverse(newDir); 
                            		}  
                            		
                            		if (newDir >= 2) {
                            			newMeta = 4 + newMeta;
                            		}
                            		//System.out.println("newDir: " + newDir);
                            		NBT.setInteger("dir", newDir);
                            		tileentity.readFromNBT(NBT);
                            		//System.out.println("New meta: " + newMeta);
                        		} else { // Printer, monitors
                            		//System.out.println("Delta: " + deltarotation);
                            		//System.out.println("Old dir: " + oldDir);
                            		
                            		// Rotate peripheral
                            		for (int i = 0; i < -deltarotation; i++) {
                            			newDir = ShipMod.instance.metaRotations.rotateCCBlock90Reverse(newDir);     
                            		}              
                            		
                            		NBT.setInteger("dir", newDir);
                            		tileentity.readFromNBT(NBT);
                            		
                            		//System.out.println("New dir: " + newDir);	
                        		}
                        	} else if (id == 1229) {
                        		int subType = meta >= 0 && meta < 6 ? 1 : (meta >= 6 && meta < 12 ? 2 : 0);
                        		int dir = meta % 6;
                        		/*
                        		System.out.println("Old meta: " + meta);
                        		System.out.println("Subtype: " + subType);
                        		System.out.println("Dir: " + dir);
                        		*/
                        		if (subType == 1 || subType == 2) {
                        			//System.out.println("Found wired modem");
	                        		for (int i = 0; i < -deltarotation; i++) {
	                        			newMeta = ShipMod.instance.metaRotations.rotateCCBlock90Reverse(newMeta);     
	                        		}  
	                        		
	                        		newMeta += 6 * (subType - 1);
                        			
	                        		//System.out.println("New meta: " + newMeta);
                        		}
                        	}
                        	
                            if (newMeta == meta) {
                            	newMeta = ShipMod.instance.metaRotations.getRotatedMeta(id, newMeta, deltarotation);
                            }
                           
                            // Rotate specific machines
                            if (ShipMod.instance.metaRotations.examForIC2Machine(tileentity)) {
                            	short newFacing, oldFacing;
                            	NBTTagCompound NBT = new NBTTagCompound();
                            	tileentity.writeToNBT(NBT);
                            	oldFacing = NBT.getShort("facing");                                 
                            	newFacing = oldFacing;
                            	for (int i = 0; i < -deltarotation; i++) {
                            		newFacing = ShipMod.instance.metaRotations.rotateIC2MachineFacing90Reverse(newFacing);
                            	}
                            	NBT.setShort("facing", newFacing);
                            	tileentity.readFromNBT(NBT);
                            	
                            	// Updating via network
                            	try {
                            		NetworkHelper.updateTileEntityField(tileentity, "facing");
                            	} catch (Exception e) {
                            		e.printStackTrace();
                            	}
                            		IC2MachinesToUpdateFacing.add(tileentity);
                            } else
                            if (ShipMod.instance.metaRotations.examForAEMachine(tileentity)) {
                            	int newFacing, oldFacing;
                            	NBTTagCompound NBT = new NBTTagCompound();
                            	tileentity.writeToNBT(NBT);
                                String tagName = "unknown";
                            	// Select tag name
                            	if (NBT.getTag("r") != null) 
                            		tagName = "r";
                            	else if (NBT.getTag("rot") != null)
                            		tagName = "rot";
                            	else if (NBT.getTag("ori") != null)
                            		tagName = "ori";

                            	if (tagName != "unknown") {
                            		boolean isCableOrBus = (tagName == "ori");
                            		
	                            	if (!isCableOrBus) {
	                            		oldFacing = NBT.getInteger(tagName);
	                            	} else
	                            		oldFacing = NBT.getByte(tagName);
	                            	
	                            	System.out.println(((isCableOrBus) ? "[cable|bus] " : "") + tagName + ": " + oldFacing);                               
	                            	newFacing = oldFacing;
	                            	
	                            	if (!isCableOrBus) {
		                            	for (int i = 0; i < -deltarotation; i++) {
		                            		newFacing = ShipMod.instance.metaRotations.rotateAEMachineFacing90Reverse(newFacing);
		                            	}
	                            	} else {
		                            	for (int i = 0; i < -deltarotation; i++) {
		                            		newFacing = ShipMod.instance.metaRotations.rotateAECableFacing90Reverse(newFacing);
		                            	}	                            		
	                            	}

	                            	if (tagName != "ori") {
	                            		NBT.setInteger(tagName, newFacing);
	                            	} else
	                            		 NBT.setByte(tagName, (byte)newFacing);
	                            	
	                            	tileentity.readFromNBT(NBT);
                            	}
                            }                            
                           
                            //meta = ShipMod.instance.metaRotations.getRotatedMeta(id, meta, deltarotation);
                            vec.xCoord = (double)((float)x + ox);
                            vec.yCoord = (double)((float)y + oy);
                            vec.zCoord = (double)((float)z + oz);
                            vec.rotateAroundY(yaw);
                            int ix = MathHelperMod.round_double(vec.xCoord + this.posX);
                            int iy = MathHelperMod.round_double(vec.yCoord + this.posY);
                            int iz = MathHelperMod.round_double(vec.zCoord + this.posZ);
                            this.worldObj.setBlock(ix, iy, iz, id, newMeta, 2);

                            if (explosionFlag && worldObj.rand.nextInt(30) == 0) {
                            	explosions.add(new ChunkCoordinates(ix, iy, iz));
                            }
                            
                            if (id != this.worldObj.getBlockId(ix, iy, iz))
                            {
                                list.add(new LocatedBlock(id, newMeta, new ChunkPosition(ix, iy, iz)));
                            }
                            else
                            {
                                if (newMeta != this.worldObj.getBlockMetadata(ix, iy, iz))
                                {
                                    this.worldObj.setBlockMetadataWithNotify(ix, iy, iz, newMeta, 2);
                                }

                                if (tileentity != null)
                                {
                                    this.worldObj.setBlockTileEntity(ix, iy, iz, tileentity);
                                    tileentity.blockMetadata = newMeta;
                                }
                            }
                        }
                    }
                }

                this.worldObj.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(flag));
                Iterator var20 = list.iterator();

                while (var20.hasNext())
                {
                    LocatedBlock var21 = (LocatedBlock)var20.next();
                    ShipMod.modLogger.finest("Post-rejoining block: " + var21.toString());
                    this.worldObj.setBlock(var21.coords.x, var21.coords.y, var21.coords.z, var21.blockID, var21.blockMeta, 2);
                }
                
                worldObj.isRemote = false;
                this.setDead();
                
                for (TileEntity machine : IC2MachinesToUpdateFacing) {
                	try {
                		NetworkHelper.updateTileEntityField(machine, "facing");
                	} catch (Exception e) {
                		e.printStackTrace();
                	}
                }
                
                for (ChunkCoordinates e : explosions) {
                	worldObj.newExplosion(null, e.posX, e.posY, e.posZ, 4F, true, true);
                }
                
                explosionFlag = false;
            }

            return true;
        }
    }

    public void dropAsItems()
    {
        for (int i = this.shipChunk.minX(); i < this.shipChunk.maxX(); ++i)
        {
            for (int j = this.shipChunk.minY(); j < this.shipChunk.maxY(); ++j)
            {
                for (int k = this.shipChunk.minZ(); k < this.shipChunk.maxZ(); ++k)
                {
                    this.shipChunk.getBlockTileEntity(i, j, k);
                    int id = this.shipChunk.getBlockId(i, j, k);

                    if (id != 0 && Block.blocksList[id] != null && (worldObj.rand.nextBoolean()))
                    {
                        int meta = this.shipChunk.getBlockMetadata(i, j, k);
                        Block.blocksList[id].dropBlockAsItem(this.worldObj, i, j, k, meta, 0);
                        this.shipChunk.setBlockAsFilledAir(i, j, k);
                        this.shipChunk.setChunkModified();
                    }
                }
            }
        }
    }

    private void fillAirBlocks(Set<ChunkPosition> set, int x, int y, int z)
    {
        if (x >= this.shipChunk.minX() - 1 && x <= this.shipChunk.maxX() && y >= this.shipChunk.minY() - 1 && y <= this.shipChunk.maxY() && z >= this.shipChunk.minZ() - 1 && z <= this.shipChunk.maxZ())
        {
            ChunkPosition pos = new ChunkPosition(x, y, z);

            if (!set.contains(pos))
            {
                set.add(pos);

                if (this.shipChunk.setBlockAsFilledAir(x, y, z))
                {
                    this.fillAirBlocks(set, x, y + 1, z);
                    this.fillAirBlocks(set, x - 1, y, z);
                    this.fillAirBlocks(set, x, y, z - 1);
                    this.fillAirBlocks(set, x + 1, y, z);
                    this.fillAirBlocks(set, x, y, z + 1);
                }
            }
        }
    }

    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    protected void fall(float distance) {}

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(this.shipChunk.getMemoryUsage());
        DataOutputStream out = new DataOutputStream(baos);

        try
        {
            this.shipChunk.writeChunkData(out);
            out.flush();
            out.close();
        }
        catch (IOException var8)
        {
            var8.printStackTrace();
        }

        compound.setByteArray("chunk", baos.toByteArray());
        compound.setByte("seatX", (byte)this.seatX);
        compound.setByte("seatY", (byte)this.seatY);
        compound.setByte("seatZ", (byte)this.seatZ);
        compound.setByte("front", (byte)this.frontDirection);

        if (!this.shipChunk.chunkTileEntityMap.isEmpty())
        {
            NBTTagList tileentities = new NBTTagList();
            Iterator i$ = this.shipChunk.chunkTileEntityMap.values().iterator();

            while (i$.hasNext())
            {
                TileEntity tileentity = (TileEntity)i$.next();
                NBTTagCompound comp = new NBTTagCompound();
                tileentity.writeToNBT(comp);
                tileentities.appendTag(comp);
            }

            compound.setTag("tileent", tileentities);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        byte[] ab = compound.getByteArray("chunk");
        ByteArrayInputStream bais = new ByteArrayInputStream(ab);
        DataInputStream in = new DataInputStream(bais);

        try
        {
            this.shipChunk.readChunkData(in);
            in.close();
        }
        catch (IOException var9)
        {
            var9.printStackTrace();
        }

        if (compound.hasKey("seat"))
        {
            short tileentities = compound.getShort("seat");
            this.seatX = tileentities & 15;
            this.seatY = tileentities >>> 4 & 15;
            this.seatZ = tileentities >>> 8 & 15;
            this.frontDirection = tileentities >>> 12 & 3;
        }
        else
        {
            this.seatX = compound.getByte("seatX");
            this.seatY = compound.getByte("seatZ");
            this.seatZ = compound.getByte("seatZ");
            this.frontDirection = compound.getByte("front");
        }

        NBTTagList var10 = compound.getTagList("tileent");

        if (var10 != null)
        {
            for (int i = 0; i < var10.tagCount(); ++i)
            {
                NBTTagCompound comp = (NBTTagCompound)var10.tagAt(i);
                TileEntity tileentity = TileEntity.createAndLoadEntity(comp);
                this.shipChunk.setBlockTileEntity(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord, tileentity);
            }
        }
    }

    @Override
    public void writeSpawnData(ByteArrayDataOutput out)
    {
        out.writeByte(this.seatX);
        out.writeByte(this.seatY);
        out.writeByte(this.seatZ);
        out.writeByte(this.frontDirection);

        try
        {
            this.shipChunk.writeChunkData(out);
        }
        catch (IOException var3)
        {
            var3.printStackTrace();
        }
    }

    @Override
    public void readSpawnData(ByteArrayDataInput in)
    {
        this.seatX = in.readByte();
        this.seatY = in.readByte();
        this.seatZ = in.readByte();
        this.frontDirection = in.readByte();

        try
        {
            this.shipChunk.readChunkData(in);
        }
        catch (IOException var3)
        {
            var3.printStackTrace();
        }
    }
}
