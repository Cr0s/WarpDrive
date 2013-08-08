package shipmod;

import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;

public class ConnectionHandler implements IConnectionHandler
{
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {}

    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
    {
        return null;
    }

    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) {}

    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) {}

    public void connectionClosed(INetworkManager manager) {}

    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {}
}
