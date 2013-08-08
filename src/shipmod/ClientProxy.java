package shipmod;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.client.settings.KeyBinding;
import shipmod.control.ShipKeyHandler;
import shipmod.entity.EntityShip;
import shipmod.render.RenderShip;

public class ClientProxy extends CommonProxy
{
    private ShipKeyHandler shipKeyHandler;

    public void registerTickHandlers()
    {
        super.registerTickHandlers();
        this.shipKeyHandler = new ShipKeyHandler(new KeyBinding("key.shipmod.up", 45), new KeyBinding("key.shipmod.down", 44), new KeyBinding("key.shipmod.brake", 46), new KeyBinding("key.shipmod.align", 13));
        KeyBindingRegistry.registerKeyBinding(this.shipKeyHandler);
    }

    public void registerLocalization()
    {
        LanguageRegistry lang = LanguageRegistry.instance();
        lang.addStringLocalization(ShipMod.blockMarkShip.getUnlocalizedName().concat(".name"), "en_US", "Shuttle Controller");
        lang.addStringLocalization(this.shipKeyHandler.kbUp.keyDescription, "en_US", "Ascent Ship");
        lang.addStringLocalization(this.shipKeyHandler.kbDown.keyDescription, "en_US", "Descent Ship");
        lang.addStringLocalization(this.shipKeyHandler.kbBrake.keyDescription, "en_US", "Brake Ship");
        lang.addStringLocalization(this.shipKeyHandler.kbAlign.keyDescription, "en_US", "Align Ship");
    }

    public void registerRenderers()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityShip.class, new RenderShip());
    }
}
