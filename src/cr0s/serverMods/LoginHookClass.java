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
import com.google.common.base.Strings;

public class LoginHookClass implements IConnectionHandler
{
	private String checkLogin(String s) throws FileNotFoundException, IOException
	{
		if (s.indexOf(".") == -1 || s.split("\\.").length != 2)
			return "Никнейм и пароль должны быть разделены точками.";

		if (!s.matches("^[a-zA-Z0-9_.]+$"))
			return "Имя пользователя или пароль содержат недопустимые символы.";

		String s4 = s.split("\\.")[0].trim();
		String s5 = s.split("\\.")[1].trim();

		if (s4.length() < 2 && !s4.equals("Q"))
			return "Имя пользователя слишком короткое.";

		if (s5.length() < 3)
			return "Пароль слишком короткий.";

		if (s4.length() > 15)
			return "Слишком длинный логин! (>15)";

		BufferedReader bufferedreader = new BufferedReader(new FileReader(MinecraftServer.getServer().getFile("users.txt")));

		String s1;

		while ((s1 = bufferedreader.readLine()) != null)
		{
			String s2;
			String s3;

			try
			{
				s2 = s1.split("\\.")[0];
				s3 = s1.split("\\.")[1];
			}
			catch (Exception exception)
			{
				bufferedreader.close();
				return "login.password error, database is corrupted.";
			}

			if (s2.toLowerCase().equals(s4.toLowerCase()))
			{
				if (!s3.equals(s5))
				{
					System.out.println((new StringBuilder()).append(s).append(" failed to login (pwd: ").append(s3).append(")").toString());
					bufferedreader.close();
					return "Неправильный пароль!";
				}
				else
				{
					bufferedreader.close();
					return "";
				}
			}
		}

		bufferedreader.close();
		PrintWriter printwriter = new PrintWriter(new FileWriter(MinecraftServer.getServer().getFile("users.txt"), true));
		printwriter.println(s);
		printwriter.close();
		return "";
	}

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
	{
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
	{
		String kickReason = "";
		String s = netHandler.clientUsername;

		try
		{
			kickReason = checkLogin(s);
			System.out.println("[SERVER MODS] Logging in user: " + s + " Result: " + kickReason);
		}
		catch (FileNotFoundException ex)
		{
			Logger.getLogger(LoginHookClass.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (IOException ex)
		{
			Logger.getLogger(LoginHookClass.class.getName()).log(Level.SEVERE, null, ex);
		}

		if (Strings.isNullOrEmpty(kickReason))
			netHandler.clientUsername = s.split("\\.")[0];

		return kickReason;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
	{
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
	{
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void connectionClosed(INetworkManager manager)
	{
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
	{
	   //throw new UnsupportedOperationException("Not supported yet.");
	}
}