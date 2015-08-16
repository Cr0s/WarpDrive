package cr0s.warpdrive.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.WarpDriveConfig;

public class ClientCameraHandler {
	public static boolean isOverlayEnabled = false;
	
	public static int overlayType = 0;
	public static int zoomIndex = 0;
	public static String overlayLoggingMessage = "";
	public static float originalFOV = 70.0F;
	public static float originalSensitivity = 100.0F;
	
	public static EntityPlayer entityPlayer;
	public static int dimensionId = -666;
	public static int check1_x, check1_y, check1_z;
	public static Block check1_blockId, check2_blockId;
	public static int check2_x, check2_y, check2_z;
	
	public ClientCameraHandler() {
		Minecraft mc = Minecraft.getMinecraft();
		
		if (WarpDriveConfig.LOGGING_CAMERA) {
			WarpDrive.logger.info("FOV is " + mc.gameSettings.fovSetting + " Sensitivity is " + mc.gameSettings.mouseSensitivity);
		}
	}
	
	public static void setupViewpoint(final int type, EntityPlayer parEntityPlayer, final float initialYaw, final float initialPitch,
			final int monitor_x, final int monitor_y, final int monitor_z, final Block blockMonitor,
			final int camera_x, final int camera_y, final int camera_z, final Block blockCamera) {
		Minecraft mc = Minecraft.getMinecraft();
		
		if (parEntityPlayer == null) {
			WarpDrive.logger.error("setupViewpoint with null player => denied");
			return;
		}
		
		// Save initial state
		originalFOV = mc.gameSettings.fovSetting;
		originalSensitivity = mc.gameSettings.mouseSensitivity;
		overlayType = type;
		entityPlayer = parEntityPlayer;
		dimensionId = entityPlayer.worldObj.provider.dimensionId;
		check1_x = monitor_x;
		check1_y = monitor_y;
		check1_z = monitor_z;
		check1_blockId = blockMonitor;
		check2_x = camera_x;
		check2_y = camera_y;
		check2_z = camera_z;
		check2_blockId = blockCamera;
		
		// Spawn camera entity
		EntityCamera entityCamera = new EntityCamera(entityPlayer.worldObj, camera_x, camera_y, camera_z, entityPlayer);
		entityPlayer.worldObj.spawnEntityInWorld(entityCamera);
		// entityCamera.setPositionAndUpdate(camera_x + 0.5D, camera_y + 0.5D, camera_z + 0.5D);
		entityCamera.setLocationAndAngles(camera_x + 0.5D, camera_y + 0.5D, camera_z + 0.5D, initialYaw, initialPitch);
		
		// Update view
		if (WarpDriveConfig.LOGGING_CAMERA) {
			WarpDrive.logger.info("Setting viewpoint to " + entityCamera.toString());
		}
		mc.renderViewEntity = entityCamera;
		mc.gameSettings.thirdPersonView = 0;
		refreshViewPoint();
		isOverlayEnabled = true;
		
		Keyboard.enableRepeatEvents(true);
	}
	
	private static void refreshViewPoint() {
		Minecraft mc = Minecraft.getMinecraft();
		
		switch (zoomIndex) {
		case 0:
			mc.gameSettings.fovSetting = originalFOV;
			mc.gameSettings.mouseSensitivity = originalSensitivity / 2.0F;
			break;
			
		case 1:
			mc.gameSettings.fovSetting = originalFOV / 1.5F;
			mc.gameSettings.mouseSensitivity = originalSensitivity / 3.0F;
			break;
			
		case 2:
			mc.gameSettings.fovSetting = originalFOV / 3.0F;
			mc.gameSettings.mouseSensitivity = originalSensitivity / 6.0F;
			break;
			
		case 3:
			mc.gameSettings.fovSetting = originalFOV / 4.5F;
			mc.gameSettings.mouseSensitivity = originalSensitivity / 9.0F;
			break;
			
		default:
			mc.gameSettings.fovSetting = originalFOV;
			mc.gameSettings.mouseSensitivity = originalSensitivity / 2.0F;
			break;
		}
	}
	
	public static void zoom() {
		Minecraft mc = Minecraft.getMinecraft();
		
		zoomIndex = (zoomIndex + 1) % 4;
		refreshViewPoint();
		if (WarpDriveConfig.LOGGING_CAMERA) {
			mc.thePlayer.sendChatMessage("changed to fovSetting " + mc.gameSettings.fovSetting + " mouseSensitivity " + mc.gameSettings.mouseSensitivity);
		}
	}
	
	public static void resetViewpoint() {
		Minecraft mc = Minecraft.getMinecraft();
		if (entityPlayer != null) {
			mc.renderViewEntity = entityPlayer;
			entityPlayer = null;
			if (WarpDriveConfig.LOGGING_CAMERA) {
				WarpDrive.logger.info("Resetting viewpoint");
			}
		} else {
			WarpDrive.logger.error("reseting viewpoint with invalid player entity?");
		}
		
		Keyboard.enableRepeatEvents(false);
		
		isOverlayEnabled = false;
		mc.gameSettings.thirdPersonView = 0;
		mc.gameSettings.fovSetting = originalFOV;
		mc.gameSettings.mouseSensitivity = originalSensitivity;
		
		entityPlayer = null;
		dimensionId = -666;
	}
	
	public static boolean isValidContext(World worldObj) {
		if (worldObj == null || worldObj.provider.dimensionId != dimensionId) {
			return false;
		}
		if (!worldObj.getBlock(check1_x, check1_y, check1_z).isAssociatedBlock(check1_blockId)) {
			WarpDrive.logger.error("checking viewpoint, found invalid block1 at (" + check1_x + ", " + check1_y + ", " + check1_z + ")");
			return false;
		}
		if (!worldObj.getBlock(check2_x, check2_y, check2_z).isAssociatedBlock(check2_blockId)) {
			WarpDrive.logger.error("checking viewpoint, found invalid block2 at (" + check2_x + ", " + check2_y + ", " + check2_z + ")");
			return false;
		}
		return true;
	}
	
	@SubscribeEvent
	public void onEvent(ClientDisconnectionFromServerEvent event) {
		if (isOverlayEnabled) {
			resetViewpoint();
		}
	}
}
