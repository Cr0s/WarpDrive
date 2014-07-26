package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.*;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet62LevelSound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityCamera extends TileEntity implements IPeripheral {
	private int dx, dz, dy;
	private float yaw, pitch; // laser direction

	private int frequency = -1;	// beam frequency
	private float r, g, b;	  // beam color (corresponds to frequency)

	private boolean isEmitting = false;

	private String[] methodsArray = {
		"freq"
	};

	private final int REGISTRY_UPDATE_INTERVAL_SEC = 10;
	private int ticks = 0;

	private int packetSendTicks = 20;

	@Override
	public void updateEntity() {
		// Update frequency on clients
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			packetSendTicks--;
			if (packetSendTicks == 0) {
				packetSendTicks = 20 * 5;
				sendFreqPacket();
			}
			return;
		}

		ticks++;
		if (ticks > 20 * REGISTRY_UPDATE_INTERVAL_SEC) {
			ticks = 0;
			WarpDrive.instance.cams.updateInRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord), frequency, 0);
		}
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int parFrequency) {
		if (frequency != parFrequency) {
			frequency = parFrequency;
			WarpDrive.debugPrint("" + this + " Frequency set to " + frequency);
		}
        WarpDrive.instance.cams.updateInRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord), frequency, 0);
	}

    @Override
    public void onChunkUnload() {
        WarpDrive.instance.cams.removeFromRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord));
        super.onChunkUnload();
    }

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		frequency = tag.getInteger("frequency");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("frequency", frequency);
	}

	public void sendFreqPacket() {
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
			DataOutputStream outputStream = new DataOutputStream(bos);

			try {
				outputStream.writeInt(xCoord);
				outputStream.writeInt(yCoord);
				outputStream.writeInt(zCoord);
				outputStream.writeInt(frequency);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = "WarpDriveFreq";
			packet.data = bos.toByteArray();
			packet.length = bos.size();
			MinecraftServer.getServer().getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 100, worldObj.provider.dimensionId, packet);
//			WarpDrive.debugPrint("" + this + " Packet '" + packet.channel + "' sent (" + xCoord + ", " + yCoord + ", " + zCoord + ") '" + frequency + "'");
		}
	}

	// IPeripheral methods implementation
	@Override
	public String getType() {
		return "camera";
	}

	@Override
	public String[] getMethodNames() {
		return methodsArray;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
		if (arguments.length == 1) {
			setFrequency(((Double)arguments[0]).intValue());
		}
		return new Integer[] { frequency };
	}

	@Override
	public void attach(IComputerAccess computer) {
	}

	@Override
	public void detach(IComputerAccess computer) {
	}

	@Override
	public boolean equals(IPeripheral other) {
		// TODO Auto-generated method stub
		return false;
	}
}