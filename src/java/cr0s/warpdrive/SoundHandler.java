package cr0s.warpdrive;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class SoundHandler {
    @SideOnly(Side.CLIENT)
    @ForgeSubscribe
    public void onSoundLoad(SoundLoadEvent event) {
        try {
        	WarpDrive.debugPrint("[WarpDrive] Registering sound files...");
            event.manager.addSound("warpdrive:warp_4s.ogg");
            event.manager.addSound("warpdrive:warp_10s.ogg");
            event.manager.addSound("warpdrive:warp_30s.ogg");
            event.manager.addSound("warpdrive:hilaser.ogg");
            event.manager.addSound("warpdrive:midlaser.ogg");
            event.manager.addSound("warpdrive:lowlaser.ogg");
            event.manager.addSound("warpdrive:cloak.ogg");
            event.manager.addSound("warpdrive:decloak.ogg");
        } catch (Exception e) {
            System.err.println("Failed to register sound: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}