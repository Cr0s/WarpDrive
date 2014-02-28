package cr0s.WarpDrive;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy
{
    @Override
    public void registerRenderers()
    {
    }

    @Override
    public void renderBeam(World world, Vector3 position, Vector3 target, float red, float green, float blue, int age, int energy)
    {
        WarpDrive.debugPrint("Rendering beam...");
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXBeam(world, position, target, red, green, blue, age, energy));
    }
}