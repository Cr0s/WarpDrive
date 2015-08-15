package cr0s.warpdrive.core;

import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ClassTransformer implements net.minecraft.launchwrapper.IClassTransformer {
	private HashMap<String, String> nodemap = new HashMap<String, String>();
	
	private final String GRAVITY_MANAGER_CLASS = "cr0s/warpdrive/GravityManager";
	private boolean debugLog = false;
	
	public ClassTransformer() {
		nodemap.put("entityLivingBaseClass", "sv");
		nodemap.put("moveEntityWithHeadingMethod", "func_70612_e");
		nodemap.put("moveEntityWithHeadingDesc", "(FF)V");
		nodemap.put("entityItemClass", "xk");
		nodemap.put("onUpdateMethod", "func_70071_h_");
		nodemap.put("onUpdateDesc", "()V");
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		if (nodemap == null) {
			System.out.println("Nodemap is null, transformation cancelled");
			return bytes;
		}
		
		String className = name.replace('/', '.');
		
		if (debugLog) { System.out.println("Checking " + name); }
		if (className.equals(nodemap.get("entityLivingBaseClass")) || className.equals("net.minecraft.entity.EntityLivingBase")) {
			bytes = transformEntityLivingBase(bytes, name.contains("."));
		} else if (className.equals(nodemap.get("entityItemClass")) || className.equals("net.minecraft.entity.item.EntityItem")) {
			bytes = transformEntityItem(bytes, name.contains("/"));
		}
		
		return bytes;
	}
	
	private byte[] transformEntityItem(byte[] bytes, final boolean isDevelopment) {
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);
		int operationCount = 2;
		int injectionCount = 0;
		Iterator methods = node.methods.iterator();
		
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			MethodNode methodnode = (MethodNode) methods.next();
			if (debugLog) { System.out.println("- Method " + methodnode.name + " " + methodnode.desc); }
			
			if ( (methodnode.name.equals(nodemap.get("onUpdateMethod")) || methodnode.name.equals("onUpdate"))
			  && methodnode.desc.equals(nodemap.get("onUpdateDesc")) ) {
				if (debugLog) { System.out.println("Method found!"); }
				
				int count = 0;
				
				while (count < methodnode.instructions.size()) {
					AbstractInsnNode list = methodnode.instructions.get(count);
					
					if (list instanceof LdcInsnNode) {
						LdcInsnNode nodeAt = (LdcInsnNode) list;
						
						if (nodeAt.cst.equals(Double.valueOf(0.039999999105930328D))) {
							VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getItemGravity",
									"(L" + "net/minecraft/entity/item/EntityItem" + ";)D",
									false);
							methodnode.instructions.insertBefore(nodeAt, beforeNode);
							methodnode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { System.out.println("Injecting into " + node.name + "." + methodnode.name + " " + methodnode.desc); }
							injectionCount++;
						}
						
						if (nodeAt.cst.equals(Double.valueOf(0.98000001907348633D))) {
							VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getItemGravity2",
									"(L" + "net/minecraft/entity/item/EntityItem" + ";)D",
									false);
							methodnode.instructions.insertBefore(nodeAt, beforeNode);
							methodnode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { System.out.println("Injecting into " + node.name + "." + methodnode.name + " " + methodnode.desc); }
							injectionCount++;
						}
					}
					
					count++;
				}
			}
		} while (true);
		
		if (injectionCount != operationCount) {
			System.out.println("Injection failed for " + node.name + " (" + injectionCount + " / " + operationCount + "), aborting...");
		} else {
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);
			bytes = writer.toByteArray();
			System.out.println("Injection successfull!");
		}
		return bytes;
	}
	
	private byte[] transformEntityLivingBase(byte[] bytes, final boolean isDevelopment) {
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);
		int operationCount = 1;
		int injectionCount = 0;
		Iterator methods = node.methods.iterator();
		
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			MethodNode methodnode = (MethodNode) methods.next();
			
			if ( (methodnode.name.equals(nodemap.get("moveEntityWithHeadingMethod")) || methodnode.name.equals("moveEntityWithHeading"))
			  && methodnode.desc.equals(nodemap.get("moveEntityWithHeadingDesc")) ) {
				if (debugLog) { System.out.println("Method found!"); }
				
				int count = 0;
				
				while (count < methodnode.instructions.size()) {
					AbstractInsnNode list = methodnode.instructions.get(count);
					
					if (list instanceof LdcInsnNode) {
						LdcInsnNode nodeAt = (LdcInsnNode) list;
						
						if (nodeAt.cst.equals(Double.valueOf(0.080000000000000002D))) {
							VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getGravityForEntity",
									"(L" + "net/minecraft/entity/EntityLivingBase" + ";)D",
									false);
							methodnode.instructions.insertBefore(nodeAt, beforeNode);
							methodnode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { System.out.println("Injecting into " + node.name + "." + methodnode.name + " " + methodnode.desc); }
							injectionCount++;
						}
					}
					
					count++;
				}
			}
		} while (true);
		
		if (injectionCount != operationCount) {
			System.out.println("Injection failed for " + node.name + " (" + injectionCount + " / " + operationCount + "), aborting...");
		} else {
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);
			bytes = writer.toByteArray();
			System.out.println("Injection successfull!");
		}
		return bytes;
	}
}
