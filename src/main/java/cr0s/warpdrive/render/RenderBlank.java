package cr0s.warpdrive.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.client.IRenderHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RenderBlank extends IRenderHandler
{
    @SideOnly(Side.CLIENT)
    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc)
    {
    }
}
