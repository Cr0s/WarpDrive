package cr0s.WarpDrive.client;

import cr0s.WarpDrive.CommonProxy;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        System.out.println("[WD] Preloading textures...");
        MinecraftForgeClient.preloadTexture(BLOCK_TEXTURE_ONLINE);
        MinecraftForgeClient.preloadTexture(BLOCK_TEXTURE_OFFLINE);
    }
}