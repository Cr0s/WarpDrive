package cr0s.warpdrive.network;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cr0s.warpdrive.network.MessageCloak;
import cr0s.warpdrive.network.MessageFrequency;
import cr0s.warpdrive.network.MessageBeamEffect;
import cr0s.warpdrive.network.MessageTargeting;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.Vector3;

public class PacketHandler {
    public static final SimpleNetworkWrapper simpleNetworkManager = NetworkRegistry.INSTANCE.newSimpleChannel(WarpDrive.MODID);

    public static void init() {
		simpleNetworkManager.registerMessage(MessageBeamEffect.class, MessageBeamEffect.class, 0, Side.CLIENT);
		simpleNetworkManager.registerMessage(MessageFrequency.class , MessageFrequency.class , 1, Side.CLIENT);
		simpleNetworkManager.registerMessage(MessageTargeting.class , MessageTargeting.class , 2, Side.SERVER);
		simpleNetworkManager.registerMessage(MessageCloak.class     , MessageCloak.class     , 3, Side.CLIENT);
    }
	
	// Beam effect sent to client side
	public static void sendBeamPacket(World worldObj, Vector3 source, Vector3 target, float red, float green, float blue, int age, int energy, int radius) {
		assert(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER);
		
		MessageBeamEffect beamMessage = new MessageBeamEffect(source, target, red, green, blue, age, energy);
		
		// small beam are sent relative to beam center
		if (source.distanceTo_square(target) < 3600 /* 60 * 60 */) {
			simpleNetworkManager.sendToAllAround(beamMessage, new TargetPoint(
					worldObj.provider.dimensionId, (source.x + target.x) / 2, (source.y + target.y) / 2, (source.z + target.z) / 2, radius));
		} else {// large beam are sent from both ends
			if (true) {
				List<EntityPlayerMP> playerEntityList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
				int dimensionId = worldObj.provider.dimensionId;
				int radius_square = radius * radius;
				for (int index = 0; index < playerEntityList.size(); index++) {
					EntityPlayerMP entityplayermp = playerEntityList.get(index);

					if (entityplayermp.dimension == dimensionId) {
						Vector3 player = new Vector3(entityplayermp);
						if (source.distanceTo_square(player) < radius_square || target.distanceTo_square(player) < radius_square) {
							simpleNetworkManager.sendTo(beamMessage, entityplayermp);
						}
					}
				}
			} else {
				simpleNetworkManager.sendToAllAround(beamMessage, new TargetPoint(
						worldObj.provider.dimensionId, source.x, source.y, source.z, radius));
				simpleNetworkManager.sendToAllAround(beamMessage, new TargetPoint(
						worldObj.provider.dimensionId, target.x, target.y, target.z, radius));
			}
		}
	}
	
	public static void sendBeamPacketToPlayersInArea(World worldObj, Vector3 source, Vector3 target, float red, float green, float blue, int age, int energy, AxisAlignedBB aabb) {
		assert(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER);
		
		MessageBeamEffect beamMessage = new MessageBeamEffect(source, target, red, green, blue, age, energy);
		// Send packet to all players within cloaked area
		List<Entity> list = worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, aabb);
		for (Entity entity : list) {
			if (entity != null && entity instanceof EntityPlayerMP) {
				PacketHandler.simpleNetworkManager.sendTo(beamMessage, (EntityPlayerMP) entity);
			}
		}
	}
	
	// Monitor/Laser/Camera updating its frequency to client side
	public static void sendFreqPacket(int dimensionId, int xCoord, int yCoord, int zCoord, int frequency) {
		MessageFrequency frequencyMessage = new MessageFrequency(xCoord, yCoord, zCoord, frequency);
		simpleNetworkManager.sendToAllAround(frequencyMessage, new TargetPoint(dimensionId, xCoord, yCoord, zCoord, 100));
		WarpDrive.debugPrint("Sent frequency packet (" + xCoord + ", " + yCoord + ", " + zCoord + ") frequency " + frequency);
	}
	
	// LaserCamera shooting at target (client -> server)
	public static void sendLaserTargetingPacket(int x, int y, int z, float yaw, float pitch) {
		MessageTargeting targetingMessage = new MessageTargeting(x, y, z, yaw, pitch);
		simpleNetworkManager.sendToServer(targetingMessage);
		WarpDrive.debugPrint("Sent targeting packet (" + x + ", " + y + ", " + z + ") yaw " + yaw + " pitch " + pitch);
	}
	
	// Sending cloaking area definition (server -> client)
	public static void sendCloakPacket(EntityPlayer player, AxisAlignedBB aabb, int tier, boolean decloak) {
		MessageCloak cloakMessage = new MessageCloak(aabb, tier, decloak);
		simpleNetworkManager.sendTo(cloakMessage, (EntityPlayerMP) player);
		WarpDrive.debugPrint("Sent cloak packet (aabb " + aabb + ") tier " + tier + " decloak " + decloak);
	}
}