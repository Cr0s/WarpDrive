package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.EnumOptions;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public final class EntityCamera extends EntityLivingBase
{
    public int xCoord;
    public int yCoord;
    public int zCoord;

    private EntityPlayer player;

    private Minecraft mc = Minecraft.getMinecraft();

    private int zoomNumber = 0;

    private int closeWaitTicks = 0;
    private int zoomWaitTicks = 0;
    private int fireWaitTicks = 0;
    private boolean isActive = true;
    private int bootUpTicks = 20;

    public EntityCamera(World world) {
        super(world);
		// TODO Auto-generated constructor stub
	}
    
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
    
    private void closeCamera() {
    	if (!isActive) {
    		return;
    	}
    	    	
		ClientCameraUtils.resetViewpoint();
    	worldObj.removeEntity(this);
    	isActive = false;
    }
    
    @Override
    public void onEntityUpdate() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
        	if (player == null || player.isDead) {
        		WarpDrive.debugPrint("" + this + " Player is null or dead, closing camera...");
        		closeCamera();
        		return;
        	}
        	if (!ClientCameraUtils.isValidContext(worldObj)) {
        		WarpDrive.debugPrint("" + this + " Invalid context, closing camera...");
        		closeCamera();
        		return;
        	}
        	
        	int blockID = worldObj.getBlockId(xCoord, yCoord, zCoord);
            mc.renderViewEntity.rotationYaw = player.rotationYaw;
            //mc.renderViewEntity.rotationYawHead = player.rotationYawHead;
            mc.renderViewEntity.rotationPitch = player.rotationPitch;

            WarpDrive.instance.debugMessage = "Mouse " + Mouse.isButtonDown(0) + " " + Mouse.isButtonDown(1) + " " + Mouse.isButtonDown(2) + " " + Mouse.isButtonDown(3)
            			+ "\nBackspace " + Keyboard.isKeyDown(Keyboard.KEY_BACKSLASH) + " Space " + Keyboard.isKeyDown(Keyboard.KEY_SPACE) + " Shift " + "";
            // Perform zoom
            if (Mouse.isButtonDown(0)) {
            	zoomWaitTicks++;
            	if (zoomWaitTicks == 2) {
            		zoomWaitTicks = 0;
            		zoom();
                }
            } else {
            	zoomWaitTicks = 0;
            }

            if (bootUpTicks > 0) {
            	bootUpTicks--;
            } else {
	            if (Mouse.isButtonDown(1)) {
	            	closeWaitTicks++;
	            	if (closeWaitTicks == 2) {
	            		closeWaitTicks = 0;
	    	    		closeCamera();
	            	}
	            } else {
	            	closeWaitTicks = 0;
	            }
            }
            
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            	fireWaitTicks++;
            	if (fireWaitTicks == 2) {
                    fireWaitTicks = 0;

                    // Make a shoot with camera-laser
                    if (blockID == WarpDriveConfig.laserCamID) {
                        sendTargetPacket();
                    }
            	}
            } else {
                fireWaitTicks = 0;
            }
            
            GameSettings gamesettings = mc.gameSettings;
            int dx = 0, dy = 0, dz = 0;
            if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                dy = -1;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                dy = 2;
            } else if (Keyboard.isKeyDown(gamesettings.keyBindLeft.keyCode)) {
                dz = -1;
            } else if (Keyboard.isKeyDown(gamesettings.keyBindRight.keyCode)) {
                dz = 1;
            } else if (Keyboard.isKeyDown(gamesettings.keyBindForward.keyCode)) {
                dx = 1;
            } else if (Keyboard.isKeyDown(gamesettings.keyBindBack.keyCode)) {
                dx = -1;
            }

            setPosition(xCoord + dx, yCoord + dy, zCoord + dz);
        }
    }

    public void zoom() {
        if (zoomNumber == 0) {
            mc.gameSettings.setOptionFloatValue(EnumOptions.FOV, -0.75F);
            mc.gameSettings.setOptionFloatValue(EnumOptions.SENSITIVITY, 0.4F);
        } else if (zoomNumber == 1) {
            mc.gameSettings.setOptionFloatValue(EnumOptions.FOV, -1.25F);
            mc.gameSettings.setOptionFloatValue(EnumOptions.SENSITIVITY, 0.3F);
        } else if (zoomNumber == 2) {
            mc.gameSettings.setOptionFloatValue(EnumOptions.FOV, -1.6F);
            mc.gameSettings.setOptionFloatValue(EnumOptions.SENSITIVITY, 0.15F);
        } else if (zoomNumber == 3) {
            mc.gameSettings.setOptionFloatValue(EnumOptions.FOV, WarpDrive.normalFOV);
            mc.gameSettings.setOptionFloatValue(EnumOptions.SENSITIVITY, WarpDrive.normalSensitivity);
        }
        zoomNumber = (zoomNumber + 1) % 4;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return false;
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getAttributeMap().func_111150_b(SharedMonsterAttributes.attackDamage).setAttribute(1.0D);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        xCoord = nbttagcompound.getInteger("x");
        yCoord = nbttagcompound.getInteger("y");
        zCoord = nbttagcompound.getInteger("z");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setInteger("x", xCoord);
        nbttagcompound.setInteger("y", yCoord);
        nbttagcompound.setInteger("z", zCoord);
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

    // Camera orientation refresh to server packet
    public void sendTargetPacket() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
            DataOutputStream outputStream = new DataOutputStream(bos);

            try {
                outputStream.writeInt(xCoord);
                outputStream.writeInt(yCoord);
                outputStream.writeInt(zCoord);
                outputStream.writeFloat(mc.renderViewEntity.rotationYaw);
                outputStream.writeFloat(mc.renderViewEntity.rotationPitch);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "WarpDriveLaserT";
            packet.data = bos.toByteArray();
            packet.length = bos.size();
            PacketDispatcher.sendPacketToServer(packet);
            WarpDrive.debugPrint("" + this + " Packet '" + packet.channel + "' sent (" + xCoord + ", " + yCoord + ", " + zCoord + ") yawn " + mc.renderViewEntity.rotationYaw + " pitch " + mc.renderViewEntity.rotationPitch);
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