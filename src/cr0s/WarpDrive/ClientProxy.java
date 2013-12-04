package cr0s.WarpDrive;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy
{
    @Override
    public void registerRenderers()
    {
    }

    @Override
    public void renderBeam(World world, Vector3 position, Vector3 target, float red, float green, float blue, int age, int energy)
    {
        //System.out.println("Rendering beam...");
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXBeam(world, position, target, red, green, blue, age, energy));
    }
}