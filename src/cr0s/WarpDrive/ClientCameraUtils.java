package cr0s.WarpDrive;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.EnumOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class ClientCameraUtils
{
    public static EntityPlayer playerData;
    public static float oldFOV;
    public static float oldSens;

    public static void setupViewpoint(Entity entity)
    {
        Minecraft mc = Minecraft.getMinecraft();

        if (entity == null)
        {
            System.out.println("Null");
        }

        System.out.println("Setting viewpoint: " + entity.toString());
        mc.renderViewEntity = (EntityLivingBase)entity;
        mc.gameSettings.thirdPersonView = 0;
        oldFOV = mc.gameSettings.fovSetting;
        oldSens = mc.gameSettings.mouseSensitivity;
        WarpDrive.instance.isOverlayEnabled = true;
    }

    public static void resetCam()
    {
        Minecraft mc = Minecraft.getMinecraft();
        mc.renderViewEntity = playerData;
        playerData = null;
        mc.gameSettings.thirdPersonView = 0;
        mc.gameSettings.setOptionFloatValue(EnumOptions.FOV, oldFOV);
        mc.gameSettings.setOptionFloatValue(EnumOptions.SENSITIVITY, oldSens);
        WarpDrive.instance.isOverlayEnabled = false;
    }
}
