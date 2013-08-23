package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.EnumOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;

public final class EntityCamera extends EntityLivingBase {
    public int xCoord;
    public int yCoord;
    public int zCoord;
   
    private int ticks = 0;
    private EntityPlayer player;

    private Minecraft mc = Minecraft.getMinecraft();
    private int dx, dy, dz;
    
    private int zoomNumber = 0;
    
    private int waitTicks = 2;
    private int fireWaitTicks = 2;
    
    private float oldFOV;
    private float oldSens;
    
    public EntityCamera(World world, ChunkPosition pos, EntityPlayer player) {  
    	super(world);
    	this.setInvisible(true);
        int x = pos.x;
        int y = pos.y;
        int z = pos.z;
        
        this.xCoord = x;
        this.posX = (double) x;

        this.yCoord = y;
        this.posY = (double) y;

        this.zCoord = z;
        this.posZ = (double) z;
        
        this.player = player;
        
    }
      
    @Override
    public void onEntityUpdate() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
        	mc.renderViewEntity.rotationYaw = player.rotationYaw;
        	//mc.renderViewEntity.rotationYawHead = player.rotationYawHead;
        	mc.renderViewEntity.rotationPitch = player.rotationPitch;
        	
        	// Perform zoom
        	if (Mouse.isButtonDown(1) && waitTicks-- == 2) {
        		waitTicks = 2;
        		zoom();
        	}
        	
        	if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
        		ClientCameraUtils.resetCam();
        		this.setDead();
        	} else if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && fireWaitTicks-- == 0) {
        		fireWaitTicks = 2;
        		// Make a shoot with camera-laser
        		if (worldObj.getBlockId(xCoord, yCoord, zCoord) == WarpDrive.instance.config.laserCamID) {
        			sendTargetPacket();
        		}
        	} else {
        		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && (dy != -2)) {
        			dy = -1;
        		} else if (Keyboard.isKeyDown(Keyboard.KEY_UP) && (dy != 2)) {
        			dy = 2;
        		} else if (Keyboard.isKeyDown(Keyboard.KEY_A) && (dz != -1)) {
        			dz = -1;
        		} else if (Keyboard.isKeyDown(Keyboard.KEY_D) && (dz != 1)) {
        			dz = 1;
        		} else if (Keyboard.isKeyDown(Keyboard.KEY_W) && (dx != 1)) {
        			dx = 1;
        		} else if (Keyboard.isKeyDown(Keyboard.KEY_S) && (dx != -1)) {
        			dx = -1;
        		}
        	}
        	
        	this.setPosition(xCoord + dx, yCoord + dy, zCoord + dz);
        }
    }
    
    public void zoom() {
        if(zoomNumber == 0) {
            this.oldFOV = mc.gameSettings.fovSetting;
            this.oldSens = mc.gameSettings.mouseSensitivity;
            
            mc.gameSettings.setOptionFloatValue(EnumOptions.FOV, -0.75F);
            mc.gameSettings.setOptionFloatValue(EnumOptions.SENSITIVITY, 0.4F);
            ++zoomNumber;
         } else if(zoomNumber == 1) {
            mc.gameSettings.setOptionFloatValue(EnumOptions.FOV, -1.25F);
            mc.gameSettings.setOptionFloatValue(EnumOptions.SENSITIVITY, 0.3F);
            ++zoomNumber;
         } else if(zoomNumber == 2) {
            mc.gameSettings.setOptionFloatValue(EnumOptions.FOV, -1.6F);
            mc.gameSettings.setOptionFloatValue(EnumOptions.SENSITIVITY, 0.15F);
            zoomNumber = 3;
         } else if(zoomNumber == 3) {
            mc.gameSettings.setOptionFloatValue(EnumOptions.FOV, this.oldFOV);
            mc.gameSettings.setOptionFloatValue(EnumOptions.SENSITIVITY, this.oldSens);
            zoomNumber = 0;
         }    	
    }
    
    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return false;
    }    
    
    @Override
    protected void func_110147_ax()
    {
        super.func_110147_ax();
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e).func_111128_a(1.0D);
    }    
    
    @Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        this.xCoord = nbttagcompound.getInteger("x");
        this.yCoord = nbttagcompound.getInteger("y");
        this.zCoord = nbttagcompound.getInteger("z");
    }

    /*@Override
    protected void entityInit() {
    }*/

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("x", this.xCoord);
        nbttagcompound.setInteger("y", this.yCoord);
        nbttagcompound.setInteger("z", this.zCoord);
    }

	@Override
	public ItemStack getHeldItem() {
		return null;
	}

	@Override
	public ItemStack getCurrentItemOrArmor(int i) {
		return null;
	}

	@Override
	public void setCurrentItemOrArmor(int i, ItemStack itemstack) {
	}

	@Override
	public ItemStack[] getLastActiveItems() {
		return null;
	}

    // Camera frequency refresh to clients packet
    public void sendTargetPacket() {              
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.CLIENT) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
            DataOutputStream outputStream = new DataOutputStream(bos);
            try {
                // Write source vector
            	outputStream.writeInt(xCoord);
            	outputStream.writeInt(yCoord);
            	outputStream.writeInt(zCoord);
            	
            	outputStream.writeFloat(mc.renderViewEntity.rotationYaw);
            	outputStream.writeFloat(mc.renderViewEntity.rotationPitch);
            } catch (Exception ex) {
                    ex.printStackTrace();
            }
            
            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "WarpDriveLaserT";
            packet.data = bos.toByteArray();
            packet.length = bos.size();
        	
            PacketDispatcher.sendPacketToServer(packet);
        }
    }	
	
	/*
	@Override
	public void sendChatToPlayer(ChatMessageComponent chatmessagecomponent) {
		
	}

	@Override
	public boolean canCommandSenderUseCommand(int i, String s) {
		return false;
	}

	@Override
	public ChunkCoordinates getPlayerCoordinates() {
		return new ChunkCoordinates(xCoord, yCoord, zCoord);
	}*/
}