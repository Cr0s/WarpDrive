package cr0s.warpdrive.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.network.PacketHandler;

public final class EntityCamera extends EntityLivingBase {
	// entity coordinates (x, y, z) are dynamically changed by player
	
	// camera block coordinates are fixed
	public int cameraX;
	public int cameraY;
	public int cameraZ;
	
	private EntityPlayer player;
	
	private Minecraft mc = Minecraft.getMinecraft();
	
	private int dx = 0, dy = 0, dz = 0;
	
	private int closeWaitTicks = 0;
	private int zoomWaitTicks = 0;
	private int fireWaitTicks = 0;
	private boolean isActive = true;
	private int bootUpTicks = 20;
	
	private boolean isCentered = true;
	
	public EntityCamera(World world, final int x, final int y, final int z, EntityPlayer player) {
		super(world);
		setInvisible(true);
		// yOffset = 1.9F; // set viewpoint inside camera (requires a 3D model of the camera)
		posX = x;
		posY = y;
		posZ = z;
		cameraX = x;
		cameraY = y;
		cameraZ = z;
		this.player = player;
	}
	
	private void closeCamera() {
		if (!isActive) {
			return;
		}
		
		ClientCameraHandler.resetViewpoint();
		worldObj.removeEntity(this);
		isActive = false;
	}
	
	@Override
	public void onEntityUpdate() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			if (player == null || player.isDead) {
				WarpDrive.logger.error(this + " Player is null or dead, closing camera...");
				closeCamera();
				return;
			}
			if (!ClientCameraHandler.isValidContext(worldObj)) {
				WarpDrive.logger.error(this + " Invalid context, closing camera...");
				closeCamera();
				return;
			}
			
			Block block = worldObj.getBlock(cameraX, cameraY, cameraZ);
			mc.renderViewEntity.rotationYaw = player.rotationYaw;
			// mc.renderViewEntity.rotationYawHead = player.rotationYawHead;
			mc.renderViewEntity.rotationPitch = player.rotationPitch;
			
			ClientCameraHandler.overlayLoggingMessage = "Mouse " + Mouse.isButtonDown(0) + " " + Mouse.isButtonDown(1) + " " + Mouse.isButtonDown(2) + " " + Mouse.isButtonDown(3) + "\nBackspace "
					+ Keyboard.isKeyDown(Keyboard.KEY_BACKSLASH) + " Space " + Keyboard.isKeyDown(Keyboard.KEY_SPACE) + " Shift " + "";
			// Perform zoom
			if (Mouse.isButtonDown(0)) {// FIXME merge: main is using right click with Mouse.isButtonDown(1), branch is using left click
				zoomWaitTicks++;
				if (zoomWaitTicks >= 2) {
					zoomWaitTicks = 0;
					ClientCameraHandler.zoom();
				}
			} else {
				zoomWaitTicks = 0;
			}
			
			if (bootUpTicks > 0) {
				bootUpTicks--;
			} else {
				if (Mouse.isButtonDown(1)) {
					closeWaitTicks++;
					if (closeWaitTicks >= 2) {
						closeWaitTicks = 0;
						closeCamera();
					}
				} else {
					closeWaitTicks = 0;
				}
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {// FIXME merge: main is using left click with Mouse.isButtonDown(0), branch is using space bar
				fireWaitTicks++;
				if (fireWaitTicks >= 2) {
					fireWaitTicks = 0;
					
					// Make a shoot with camera-laser
					if (block.isAssociatedBlock(WarpDrive.blockLaserCamera)) {
						PacketHandler.sendLaserTargetingPacket(cameraX, cameraY, cameraZ, mc.renderViewEntity.rotationYaw, mc.renderViewEntity.rotationPitch);
					}
				}
			} else {
				fireWaitTicks = 0;
			}
			
			GameSettings gamesettings = mc.gameSettings;
			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				dy = -1;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				dy = 2;
			} else if (Keyboard.isKeyDown(gamesettings.keyBindLeft.getKeyCode())) {
				dz = -1;
			} else if (Keyboard.isKeyDown(gamesettings.keyBindRight.getKeyCode())) {
				dz = 1;
			} else if (Keyboard.isKeyDown(gamesettings.keyBindForward.getKeyCode())) {
				dx = 1;
			} else if (Keyboard.isKeyDown(gamesettings.keyBindBack.getKeyCode())) {
				dx = -1;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_C)) { // centering view
				dx = 0;
				dy = 0;
				dz = 0;
				isCentered = !isCentered;
				return;
			}
			
			if (isCentered) {
				setPosition(cameraX + 0.5D, cameraY + 0.75D, cameraZ + 0.5D);
			} else {
				setPosition(cameraX + dx, cameraY + dy, cameraZ + dz);
			}
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		this.motionX = this.motionY = this.motionZ = 0.0D;
	}
	
	@Override
	public boolean shouldRenderInPass(int pass) {
		return false;
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		// nothing to save, skip ancestor call
		cameraX = nbttagcompound.getInteger("x");
		cameraY = nbttagcompound.getInteger("y");
		cameraZ = nbttagcompound.getInteger("z");
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		// nothing to save, skip ancestor call
		nbttagcompound.setInteger("x", cameraX);
		nbttagcompound.setInteger("y", cameraY);
		nbttagcompound.setInteger("z", cameraZ);
	}
	
	@Override
	public ItemStack getHeldItem() {
		return null;
	}
	
	@Override
	public void setCurrentItemOrArmor(int i, ItemStack itemstack) {
	}
	
	@Override
	public ItemStack[] getLastActiveItems() {
		return null;
	}
	
	@Override
	public ItemStack getEquipmentInSlot(int i) {
		return null;
	}
}