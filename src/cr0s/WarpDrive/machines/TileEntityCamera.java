package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cr0s.WarpDrive.CamRegistryItem;
import cr0s.WarpDrive.WarpDrive;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;

public class TileEntityCamera extends TileEntity implements IPeripheral
{

	private int frequency = -1;	// beam frequency

	private String[] methodsArray =
	{
		"freq"
	};

	private final int REGISTRY_UPDATE_INTERVAL_SEC = 10;
	private int ticks = 0;

	private int packetSendTicks = 20;

	@Override
	public void updateEntity()
	{
		// Update frequency on clients
		if (FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			if (packetSendTicks-- == 0)
			{
				packetSendTicks = 20 * 5;
				sendFreqPacket();
			}

			return;
		}

		if (++ticks > 20 * REGISTRY_UPDATE_INTERVAL_SEC)
		{
			ticks = 0;
			WarpDrive.instance.cams.updateInRegistry(new CamRegistryItem(this.frequency, new ChunkPosition(xCoord, yCoord, zCoord), worldObj).setType(0));
		}
	}

	public int getFrequency()
	{
		return frequency;
	}

	public void setFrequency(int freq)
	{
		frequency = freq;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		frequency = tag.getInteger("frequency");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tag.setInteger("frequency", frequency);
	}

	public void sendFreqPacket()
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();

		if (side == Side.SERVER)
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
			DataOutputStream outputStream = new DataOutputStream(bos);

			try
			{
				// Write source vector
				outputStream.writeInt(xCoord);
				outputStream.writeInt(yCoord);
				outputStream.writeInt(zCoord);
				outputStream.writeInt(this.frequency);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = "WarpDriveFreq";
			packet.data = bos.toByteArray();
			packet.length = bos.size();
			MinecraftServer.getServer().getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 100, worldObj.provider.dimensionId, packet);
		}
	}

	// IPeripheral methods implementation
	@Override
	public String getType()
	{
		return "camera";
	}

	@Override
	public String[] getMethodNames()
	{
		return methodsArray;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		if (arguments.length == 1)
			frequency = ((Double)arguments[0]).intValue();
		return new Integer[] { frequency };
	}

	@Override
	public boolean canAttachToSide(int side)
	{
		return true;
	}

	@Override
	public void attach(IComputerAccess computer)
	{
	}

	@Override
	public void detach(IComputerAccess computer)
	{
	}
}
