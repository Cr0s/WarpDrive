package cr0s.WarpDrive;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

        @Override
        public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
                if (packet.channel.equals("WarpDriveBeam")) {
                    handleBeam(packet, (EntityPlayer)player);
                }
        }
       
        private void handleBeam(Packet250CustomPayload packet, EntityPlayer player) {
                DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
               
                int dimID;
                Vector3 source, target;
                double sx, sy, sz;
                double tx, ty, tz;
                float r, g, b;
                int age;
                int energy;
               
                World worldObj = player.worldObj;
                
                try {
                    // Read source vector
                	sx = inputStream.readDouble();
                	sy = inputStream.readDouble();
                	sz = inputStream.readDouble();
                	source = new Vector3(sx, sy, sz);
                	
                	// Read target vector
                	tx = inputStream.readDouble();
                	ty = inputStream.readDouble();
                	tz = inputStream.readDouble();      
                	target = new Vector3(tx, ty, tz);
                	
                	// Read r, g, b of laser
                	r = inputStream.readFloat();
                	g = inputStream.readFloat();
                	b = inputStream.readFloat();
                	
                	// Read age
                	age = inputStream.readByte();
                	
                	// Read energy value
                	energy = inputStream.readInt();
                	
                	// Render beam
                	/*System.out.println("sx: " + sx + " sy: " + sy + " sz: " + sz);
                	System.out.println("tx: " + sx + " ty: " + sy + " tz: " + sz);
                	
                	System.out.println("source: " + source);
                	System.out.println("target: " + target);
                	System.out.println("r: " + r);
                	System.out.println("g: " + g);
                	System.out.println("b " + b);
                	System.out.println("age: " + age);
                	System.out.println("energy: " + energy);*/
                	
                	// To avoid NPE at logging in
                	if (worldObj == null) {
                		System.out.println("WorldObj is null");
                		return;
                	}
                	
                	WarpDrive.proxy.renderBeam(worldObj, source.clone(), target.clone(), r, g, b, age, energy);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
        }               
}