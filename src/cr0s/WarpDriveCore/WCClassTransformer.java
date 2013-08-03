package cr0s.WarpDriveCore;


import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class WCClassTransformer implements net.minecraft.launchwrapper.IClassTransformer {

	private HashMap<String,String> nodemap = new HashMap<String, String>();
	
	private final String GRAVITY_MANAGER_CLASS = "cr0s/WarpDrive/GravityManager";
	
	public WCClassTransformer() {
		// Obfuscated Notch methods
        nodemap.put("worldClass", "abv");
        nodemap.put("playerMP", "ju");
        nodemap.put("netLoginHandler", "jx");
        nodemap.put("confManagerClass", "hm");
        nodemap.put("createPlayerMethod", "a");
        nodemap.put("createPlayerDesc", (new StringBuilder()).append("(Ljava/lang/String;)L").append((String)nodemap.get("playerMP")).append(";").toString());
        nodemap.put("respawnPlayerMethod", "a");
        nodemap.put("respawnPlayerDesc", (new StringBuilder()).append("(L").append((String)nodemap.get("playerMP")).append(";IZ)L").append((String)nodemap.get("playerMP")).append(";").toString());
        nodemap.put("itemInWorldManagerClass", "jv");
        nodemap.put("attemptLoginMethodBukkit", "attemptLogin");
        nodemap.put("attemptLoginDescBukkit", (new StringBuilder()).append("(L").append((String)nodemap.get("netLoginHandler")).append(";Ljava/lang/String;Ljava/lang/String;)L").append((String)nodemap.get("playerMP")).append(";").toString());
        nodemap.put("playerControllerClass", "bcz");
        nodemap.put("playerClient", "bdf");
        nodemap.put("netClientHandler", "bct");
        nodemap.put("createClientPlayerMethod", "a");
        nodemap.put("createClientPlayerDesc", (new StringBuilder()).append("(L").append((String)nodemap.get("worldClass")).append(";)L").append((String)nodemap.get("playerClient")).append(";").toString());
        nodemap.put("entityLivingClass", "oe");
        nodemap.put("moveEntityMethod", "e");
        nodemap.put("moveEntityDesc", "(FF)V");
        nodemap.put("entityItemClass", "sr");
        nodemap.put("onUpdateMethod", "l_");
        nodemap.put("onUpdateDesc", "()V");
        nodemap.put("entityRendererClass", "bfb");
        nodemap.put("updateLightmapMethod", "h");
        nodemap.put("updateLightmapDesc", "(F)V");
        nodemap.put("player", "ue");
        nodemap.put("containerPlayer", "vu");
        nodemap.put("invPlayerClass", "uc");
        nodemap.put("minecraft", "ats");
        nodemap.put("session", "aup");
        nodemap.put("guiPlayer", "axs");
        nodemap.put("thePlayer", "g");
        nodemap.put("displayGui", "a");
        nodemap.put("guiScreen", "avv");
        nodemap.put("displayGuiDesc", (new StringBuilder()).append("(L").append((String)nodemap.get("guiScreen")).append(";)V").toString());
        nodemap.put("runTick", "k");
        nodemap.put("runTickDesc", "()V");
        nodemap.put("clickMiddleMouseButton", "W");
        nodemap.put("clickMiddleMouseButtonDesc", "()V");
        nodemap.put("itemRendererClass", "bfg");
        nodemap.put("renderOverlaysMethod", "b");
        nodemap.put("renderOverlaysDesc", "(F)V");		
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		
		if (nodemap == null) {
			System.out.println("========= NODEMAP IS NULL!!! ========");
			return bytes;
		}
		
		if (name.replace('.', '/').equals(nodemap.get("entityLivingClass"))) {
			bytes = transformEntityLiving(bytes);
        } else
    	if(name.replace('.', '/').equals(nodemap.get("entityItemClass"))) {
            bytes = transformEntityItem(bytes);
    	}
		
		return bytes;
	}

	private byte[] transformEntityItem( byte[] bytes) {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);
        int operationCount = 2;
        int injectionCount = 0;
        Iterator methods = node.methods.iterator();
        do
        {
            if(!methods.hasNext())
                break;
            MethodNode methodnode = (MethodNode)methods.next();
            if(methodnode.name.equals(nodemap.get("onUpdateMethod")) && methodnode.desc.equals(nodemap.get("onUpdateDesc")))
            {
                int count = 0;
                while(count < methodnode.instructions.size()) 
                {
                    AbstractInsnNode list = methodnode.instructions.get(count);
                    if(list instanceof LdcInsnNode)
                    {
                        LdcInsnNode nodeAt = (LdcInsnNode)list;
                        if(nodeAt.cst.equals(Double.valueOf(0.039999999105930328D)))
                        {
                            VarInsnNode beforeNode = new VarInsnNode(25, 0);
                            MethodInsnNode overwriteNode = new MethodInsnNode(184, GRAVITY_MANAGER_CLASS, "getItemGravity", (new StringBuilder()).append("(L").append((String)nodemap.get("entityItemClass")).append(";)D").toString());
                            methodnode.instructions.insertBefore(nodeAt, beforeNode);
                            methodnode.instructions.set(nodeAt, overwriteNode);
                            injectionCount++;
                        }
                        if(nodeAt.cst.equals(Double.valueOf(0.98000001907348633D)))
                        {
                            VarInsnNode beforeNode = new VarInsnNode(25, 0);
                            MethodInsnNode overwriteNode = new MethodInsnNode(184, GRAVITY_MANAGER_CLASS, "getItemGravity2", (new StringBuilder()).append("(L").append((String)nodemap.get("entityItemClass")).append(";)D").toString());
                            methodnode.instructions.insertBefore(nodeAt, beforeNode);
                            methodnode.instructions.set(nodeAt, overwriteNode);
                            injectionCount++;
                        }
                    }
                    count++;
                }
            }
        } while(true);
        ClassWriter writer = new ClassWriter(1);
        node.accept(writer);
        bytes = writer.toByteArray();
        System.out.println((new StringBuilder()).append("[WDCore] WarpDrive successfully injected bytecode into: ").append(node.name).append(" (").append(injectionCount).append(" / ").append(operationCount).append(")").toString());
        return bytes;
	}

	private byte[] transformEntityLiving(byte[] bytes) {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);
        int operationCount = 1;
        int injectionCount = 0;
        Iterator methods = node.methods.iterator();
        do
        {
            if(!methods.hasNext())
                break;
            MethodNode methodnode = (MethodNode)methods.next();
            if(methodnode.name.equals(nodemap.get("moveEntityMethod")) && methodnode.desc.equals(nodemap.get("moveEntityDesc")))
            {
                int count = 0;
                while(count < methodnode.instructions.size()) 
                {
                    AbstractInsnNode list = methodnode.instructions.get(count);
                    if(list instanceof LdcInsnNode)
                    {
                        LdcInsnNode nodeAt = (LdcInsnNode)list;
                        if(nodeAt.cst.equals(Double.valueOf(0.080000000000000002D)))
                        {
                            VarInsnNode beforeNode = new VarInsnNode(25, 0);
                            MethodInsnNode overwriteNode = new MethodInsnNode(184, GRAVITY_MANAGER_CLASS, "getGravityForEntity", (new StringBuilder()).append("(L").append((String)nodemap.get("entityLivingClass")).append(";)D").toString());
                            methodnode.instructions.insertBefore(nodeAt, beforeNode);
                            methodnode.instructions.set(nodeAt, overwriteNode);
                            injectionCount++;
                        }
                    }
                    count++;
                }
            }
        } while(true);
        ClassWriter writer = new ClassWriter(1);
        node.accept(writer);
        bytes = writer.toByteArray();
        System.out.println((new StringBuilder()).append("[WDCore] WarpDrive successfully injected bytecode into: ").append(node.name).append(" (").append(injectionCount).append(" / ").append(operationCount).append(")").toString());
        return bytes;
	}
}
