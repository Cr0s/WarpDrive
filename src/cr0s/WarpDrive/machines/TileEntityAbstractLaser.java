package cr0s.WarpDrive.machines;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cr0s.WarpDrive.Vector3;
import cr0s.WarpDrive.WarpDrive;

public abstract class TileEntityAbstractLaser extends WarpChunkTE
{
	
	protected void sendLaserPacket(Vector3 source, Vector3 dest, float r, float g, float b, int age, int energy, int radius)
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		//WarpDrive.debugPrint("S:" + side.toString());
		if (side == Side.SERVER)
		{
			//WarpDrive.debugPrint("laser:" + source + ":" + dest + ":" + r + ":" + g + ":" + b + ":" + age);
			if (source == null || dest == null || worldObj == null)
			{
				return;
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
			DataOutputStream outputStream = new DataOutputStream(bos);

			try
			{
				// Write source vector
				outputStream.writeDouble(source.x);
				outputStream.writeDouble(source.y);
				outputStream.writeDouble(source.z);
				// Write target vector
				outputStream.writeDouble(dest.x);
				outputStream.writeDouble(dest.y);
				outputStream.writeDouble(dest.z);
				// Write r, g, b of laser
				outputStream.writeFloat(r);
				outputStream.writeFloat(g);
				outputStream.writeFloat(b);
				// Write age
				outputStream.writeByte(age);
				// Write energy value
				outputStream.writeInt(0);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = "WarpDriveBeam";
			packet.data = bos.toByteArray();
			packet.length = bos.size();
			MinecraftServer.getServer().getConfigurationManager().sendToAllNear(source.intX(), source.intY(), source.intZ(), radius, worldObj.provider.dimensionId, packet);
			MinecraftServer.getServer().getConfigurationManager().sendToAllNear(dest.intX(), dest.intY(), dest.intZ(), radius, worldObj.provider.dimensionId, packet);
		}
	}
}
