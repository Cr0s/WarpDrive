package shipmod.control;

import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.EnumSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import shipmod.entity.EntityShip;

@SideOnly(Side.CLIENT)
public class ShipKeyHandler extends KeyHandler
{
    private Minecraft mc = Minecraft.getMinecraft();
    public final KeyBinding kbUp;
    public final KeyBinding kbDown;
    public final KeyBinding kbBrake;
    public final KeyBinding kbAlign;

    public ShipKeyHandler(KeyBinding up, KeyBinding down, KeyBinding brake, KeyBinding align)
    {
        super(new KeyBinding[] {up, down, brake, align}, new boolean[] {false, false, false, false});
        this.kbUp = up;
        this.kbDown = down;
        this.kbBrake = brake;
        this.kbAlign = align;
    }

    public String getLabel()
    {
        return "ShipMod key handler";
    }

    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
    {
        if (tickEnd)
        {
            this.keyEvent(kb, true);
        }
    }

    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
    {
        if (tickEnd)
        {
            this.keyEvent(kb, false);
        }
    }

    public void keyEvent(KeyBinding kb, boolean down)
    {
        if (this.mc.thePlayer != null && this.mc.thePlayer.ridingEntity instanceof EntityShip)
        {
            int c = 0;

            if (down)
            {
                if (kb == this.kbBrake)
                {
                    c = 3;
                }
                else if (kb == this.kbAlign)
                {
                    c = 4;
                }
            }

            if (c == 0)
            {
                int ship = 0;

                if (this.kbUp.isPressed())
                {
                    ++ship;
                }

                if (this.kbDown.isPressed())
                {
                    --ship;
                }

                c = ship == 0 ? 0 : (ship < 0 ? 1 : (ship > 0 ? 2 : 0));
            }

            EntityShip var5 = (EntityShip)this.mc.thePlayer.ridingEntity;
            var5.getController().updateControl(var5, this.mc.thePlayer, c);
        }
    }

    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.CLIENT);
    }

    public int getHeightControl()
    {
        if (this.kbAlign.isPressed())
        {
            return 4;
        }
        else if (this.kbBrake.isPressed())
        {
            return 3;
        }
        else
        {
            int i = 0;

            if (this.kbUp.isPressed())
            {
                ++i;
            }

            if (this.kbDown.isPressed())
            {
                --i;
            }

            return i == 0 ? 0 : (i < 0 ? 1 : (i > 0 ? 2 : 0));
        }
    }
}
