package cr0s.serverMods;

import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;

/**
 * РђРІС‚РѕСЂРёР·Р°С†РёСЏ РЅРёРє.РїР°СЂРѕР»СЊ
 * @author Cr0s
 */
public class LoginHookClass implements IConnectionHandler {

    private String kickReason = "";
    private File uFile;

    public LoginHookClass() {
        uFile = MinecraftServer.getServer().getFile("users.txt");
    }

    private void checkLogin(NetLoginHandler netHandler) throws FileNotFoundException, IOException {
        String s = netHandler.clientUsername;
        System.out.println("[SERVER MODS] Logging in user: " + s);
        
        BufferedReader bufferedreader = new BufferedReader(new FileReader(uFile));

        if (s.indexOf(".") == -1 || s.split("\\.").length != 2) {
            kickReason = "РќРёРєРЅРµР№Рј Рё РїР°СЂРѕР»СЊ РґРѕР»Р¶РЅС‹ Р±С‹С‚СЊ СЂР°Р·РґРµР»РµРЅС‹ С‚РѕС‡РєР°РјРё.";
            bufferedreader.close();
            return;
        }

        if (!s.matches("^[a-zA-Z0-9_.]+$")) {
            kickReason = "Р�РјСЏ РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ РёР»Рё РїР°СЂРѕР»СЊ СЃРѕРґРµСЂР¶Р°С‚ РЅРµРґРѕРїСѓСЃС‚РёРјС‹Рµ СЃРёРјРІРѕР»С‹.";
            bufferedreader.close();
            return;
        }

        String s4 = s.split("\\.")[0].trim();
        String s5 = s.split("\\.")[1].trim();

        if (s4.length() < 2 && !s4.equals("Q")) {
            kickReason = "Р�РјСЏ РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ СЃР»РёС€РєРѕРј РєРѕСЂРѕС‚РєРѕРµ.";
            bufferedreader.close();
            return;
        }

        if (s5.length() < 3) {
            kickReason = "РџР°СЂРѕР»СЊ СЃР»РёС€РєРѕРј РєРѕСЂРѕС‚РєРёР№.";
            bufferedreader.close();
            return;
        }

        if (s4.length() > 15) {
            kickReason = "\u0421\u043B\u0438\u0448\u043A\u043E\u043C \u0434\u043B\u0438\u043D\u043D\u044B\u0439 \u043B\u043E\u0433\u0438\u043D! (>15)";
            bufferedreader.close();
            return;
        }

        String s1;

        while ((s1 = bufferedreader.readLine()) != null) {
            String s2;
            String s3;

            try {
                s2 = s1.split("\\.")[0];
                s3 = s1.split("\\.")[1];
            } catch (Exception exception) {
                kickReason = "login.password error, database is corrupted.";
                bufferedreader.close();
                return;
            }

            if (s2.toLowerCase().equals(s4.toLowerCase())) {
                if (!s3.equals(s5)) {
                    kickReason = "РќРµРїСЂР°РІРёР»СЊРЅС‹Р№ РїР°СЂРѕР»СЊ!";
                    System.out.println((new StringBuilder()).append(netHandler.clientUsername).append(" failed to login (pwd: ").append(s3).append(")").toString());
                    bufferedreader.close();
                    return;
                } else {
                    bufferedreader.close();
                    return;
                }
            }
        }

        bufferedreader.close();
        
        // РЎРѕР·РґР°С‘Рј РЅРѕРІС‹Р№ Р°РєРєР°СѓРЅС‚
        PrintWriter printwriter = new PrintWriter(new FileWriter(uFile, true));
        printwriter.println(s);
        printwriter.close();
        
        kickReason = "";
    }

    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) {
        kickReason = "";
        
        try {
            checkLogin(netHandler);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LoginHookClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoginHookClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // РќРµ РєРёРєР°С‚СЊ
        if (kickReason.isEmpty()) {
            // РЈРґР°Р»РёС‚СЊ РїР°СЂРѕР»СЊ РёР· РёРјРµРЅРё РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ
            netHandler.clientUsername = netHandler.clientUsername.split("\\.")[0];
        }
        
        return kickReason;
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void connectionClosed(INetworkManager manager) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}