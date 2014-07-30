package cr0s.WarpDrive.client;

import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cr0s.WarpDrive.CommonProxy;
import cr0s.WarpDrive.FXBeam;
import cr0s.WarpDrive.Vector3;
import cr0s.WarpDrive.WarpDrive;

public class ClientProxy extends CommonProxy
{
    @Override
    public void registerRenderers()
    {
    }

    @Override
    public void renderBeam(World world, Vector3 position, Vector3 target, float red, float green, float blue, int age, int energy)
    {
        //WarpDrive.debugPrint("Rendering beam...");
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXBeam(world, position, target, red, green, blue, age, energy));
    }
}