package cr0s.warpdrive.core;

import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ClassTransformer implements net.minecraft.launchwrapper.IClassTransformer {
	private HashMap<String, String> nodemap = new HashMap<String, String>();
	
	private final String GRAVITY_MANAGER_CLASS = "cr0s/warpdrive/GravityManager";
	private final String CLOAK_MANAGER_CLASS = "cr0s/warpdrive/data/CloakManager";
	private boolean debugLog = false;
	
	public ClassTransformer() {
		nodemap.put("EntityLivingBase.class", "sv");
		nodemap.put("moveEntityWithHeading.name", "func_70612_e");
		nodemap.put("moveEntityWithHeading.desc", "(FF)V");
		
		nodemap.put("EntityItem.class", "xk");
		nodemap.put("onUpdate.name", "func_70071_h_");
		nodemap.put("onUpdate.desc", "()V");
		
		nodemap.put("WorldClient.class", "bjf");
		nodemap.put("func_147492_c.name", "func_147492_c");
		nodemap.put("func_147492_c.desc", "(IIILnet/minecraft/block/Block;I)Z");
		nodemap.put("setBlock.name", "func_147465_d");
		
		nodemap.put("Chunk.class", "apx");
		nodemap.put("fillChunk.name", "func_76607_a");
		nodemap.put("fillChunk.desc", "([BIIZ)V");
		nodemap.put("generateHeightMap.name", "func_76590_a");
		nodemap.put("generateHeightMap.desc", "()V");
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		if (nodemap == null) {
			System.out.println("Nodemap is null, transformation cancelled");
			return bytes;
		}
		
		String className = name.replace('/', '.');
		
		// if (debugLog) { System.out.println("Checking " + name); }
		if (className.equals(nodemap.get("EntityLivingBase.class")) || className.equals("net.minecraft.entity.EntityLivingBase")) {
			bytes = transformMinecraftEntityLivingBase(bytes);
			
		} else if (className.equals(nodemap.get("EntityItem.class")) || className.equals("net.minecraft.entity.item.EntityItem")) {
			bytes = transformMinecraftEntityItem(bytes);
			
		} else if (className.equals("com.creativemd.itemphysic.physics.ServerPhysic")) {
			bytes = transformItemPhysicEntityItem(bytes);
			
		} else if (className.equals(nodemap.get("WorldClient.class")) || className.equals("net.minecraft.client.multiplayer.WorldClient")) {
			bytes = transformMinecraftWorldClient(bytes);
			
		} else if (className.equals(nodemap.get("Chunk.class")) || className.equals("net.minecraft.world.chunk.Chunk")) {
			bytes = transformMinecraftChunk(bytes);
		}
		
		return bytes;
	}
	
	private byte[] transformMinecraftEntityLivingBase(byte[] bytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		int operationCount = 1;
		int injectedCount = 0;
		Iterator methods = classNode.methods.iterator();
		
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			MethodNode methodnode = (MethodNode) methods.next();
			// if (debugLog) { System.out.println("- Method " + methodnode.name + " " + methodnode.desc); }
			
			if ( (methodnode.name.equals(nodemap.get("moveEntityWithHeading.name")) || methodnode.name.equals("moveEntityWithHeading"))
			  && methodnode.desc.equals(nodemap.get("moveEntityWithHeading.desc")) ) {
				if (debugLog) { System.out.println("Method found!"); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodnode.instructions.size()) {
					AbstractInsnNode abstractNode = methodnode.instructions.get(instructionIndex);
					
					if (abstractNode instanceof LdcInsnNode) {
						LdcInsnNode nodeAt = (LdcInsnNode) abstractNode;
						
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
							if (debugLog) { System.out.println("Injecting into " + classNode.name + "." + methodnode.name + " " + methodnode.desc); }
							injectedCount++;
						}
					}
					
					instructionIndex++;
				}
			}
		} while (true);
		
		if (injectedCount != operationCount) {
			System.out.println("Injection failed for " + classNode.name + " (" + injectedCount + " / " + operationCount + "), aborting...");
		} else {
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			bytes = writer.toByteArray();
			System.out.println("Injection successfull!");
		}
		return bytes;
	}
	
	private byte[] transformMinecraftEntityItem(byte[] bytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		int operationCount = 2;
		int injectedCount = 0;
		Iterator methods = classNode.methods.iterator();
		
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			MethodNode methodnode = (MethodNode) methods.next();
			// if (debugLog) { System.out.println("- Method " + methodnode.name + " " + methodnode.desc); }
			
			if ( (methodnode.name.equals(nodemap.get("onUpdate.name")) || methodnode.name.equals("onUpdate"))
			  && methodnode.desc.equals(nodemap.get("onUpdate.desc")) ) {
				if (debugLog) { System.out.println("Method found!"); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodnode.instructions.size()) {
					AbstractInsnNode abstractNode = methodnode.instructions.get(instructionIndex);
					
					if (abstractNode instanceof LdcInsnNode) {
						LdcInsnNode nodeAt = (LdcInsnNode) abstractNode;
						
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
							if (debugLog) { System.out.println("Injecting into " + classNode.name + "." + methodnode.name + " " + methodnode.desc); }
							injectedCount++;
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
							if (debugLog) { System.out.println("Injecting into " + classNode.name + "." + methodnode.name + " " + methodnode.desc); }
							injectedCount++;
						}
					}
					
					instructionIndex++;
				}
			}
		} while (true);
		
		if (injectedCount != operationCount) {
			System.out.println("Injection failed for " + classNode.name + " (" + injectedCount + " / " + operationCount + "), aborting...");
		} else {
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			bytes = writer.toByteArray();
			System.out.println("Injection successfull!");
		}
		return bytes;
	}
	
	private byte[] transformItemPhysicEntityItem(byte[] bytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		int operationCount = 2;
		int injectedCount = 0;
		Iterator methods = classNode.methods.iterator();
		
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			MethodNode methodnode = (MethodNode) methods.next();
			// if (debugLog) { System.out.println("- Method " + methodnode.name + " " + methodnode.desc); }
			
			if ( (methodnode.name.equals("update"))
			  && methodnode.desc.equals("(Lnet/minecraft/entity/item/EntityItem;)V") ) {
				if (debugLog) { System.out.println("Method found!"); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodnode.instructions.size()) {
					AbstractInsnNode abstractNode = methodnode.instructions.get(instructionIndex);
					
					if (abstractNode instanceof LdcInsnNode) {
						LdcInsnNode nodeAt = (LdcInsnNode) abstractNode;
						
						if (nodeAt.cst.equals(Double.valueOf(0.04D))) {
							VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getItemGravity",
									"(L" + "net/minecraft/entity/item/EntityItem" + ";)D",
									false);
							methodnode.instructions.insertBefore(nodeAt, beforeNode);
							methodnode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { System.out.println("Injecting into " + classNode.name + "." + methodnode.name + " " + methodnode.desc); }
							injectedCount++;
						}
						
						if (nodeAt.cst.equals(Double.valueOf(0.98D))) {
							VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getItemGravity2",
									"(L" + "net/minecraft/entity/item/EntityItem" + ";)D",
									false);
							methodnode.instructions.insertBefore(nodeAt, beforeNode);
							methodnode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { System.out.println("Injecting into " + classNode.name + "." + methodnode.name + " " + methodnode.desc); }
							injectedCount++;
						}
					}
					
					instructionIndex++;
				}
			}
		} while (true);
		
		if (injectedCount != operationCount) {
			System.out.println("Injection failed for " + classNode.name + " (" + injectedCount + " / " + operationCount + "), aborting...");
		} else {
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			bytes = writer.toByteArray();
			System.out.println("Injection successfull!");
		}
		return bytes;
	}
	
	private byte[] transformMinecraftWorldClient(byte[] bytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		int operationCount = 1;
		int injectedCount = 0;
		Iterator methods = classNode.methods.iterator();
		
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			MethodNode methodnode = (MethodNode) methods.next();
			// if (debugLog) { System.out.println("- Method " + methodnode.name + " " + methodnode.desc); }
			
			if ( (methodnode.name.equals(nodemap.get("func_147492_c.name")) || methodnode.name.equals("func_147492_c"))
			  && methodnode.desc.equals(nodemap.get("func_147492_c.desc")) ) {
				if (debugLog) { System.out.println("Method found!"); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodnode.instructions.size()) {
					AbstractInsnNode abstractNode = methodnode.instructions.get(instructionIndex);
					
					if (abstractNode instanceof MethodInsnNode) {
						MethodInsnNode nodeAt = (MethodInsnNode) abstractNode;
						
						if (nodeAt.name.equals(nodemap.get("setBlock.name")) || nodeAt.name.equals("setBlock")) {
							MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									CLOAK_MANAGER_CLASS,
									"onBlockChange",
									"(IIILnet/minecraft/block/Block;II)Z",
									false);
							methodnode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { System.out.println("Injecting into " + classNode.name + "." + methodnode.name + " " + methodnode.desc); }
							injectedCount++;
						}
					}
					
					instructionIndex++;
				}
			}
		} while (true);
		
		if (injectedCount != operationCount) {
			System.out.println("Injection failed for " + classNode.name + " (" + injectedCount + " / " + operationCount + "), aborting...");
		} else {
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			bytes = writer.toByteArray();
			System.out.println("Injection successfull!");
		}
		return bytes;
	}
	
	private byte[] transformMinecraftChunk(byte[] bytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		int operationCount = 1;
		int injectedCount = 0;
		Iterator methods = classNode.methods.iterator();
		
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			MethodNode methodnode = (MethodNode) methods.next();
			// if (debugLog) { System.out.println("- Method " + methodnode.name + " " + methodnode.desc); }
			
			if ( (methodnode.name.equals(nodemap.get("fillChunk.name")) || methodnode.name.equals("fillChunk"))
			  && methodnode.desc.equals(nodemap.get("fillChunk.desc")) ) {
				if (debugLog) { System.out.println("Method found!"); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodnode.instructions.size()) {
					AbstractInsnNode abstractNode = methodnode.instructions.get(instructionIndex);
					if (debugLog) { deasm(abstractNode); }
					
					if (abstractNode instanceof MethodInsnNode) {
						MethodInsnNode nodeAt = (MethodInsnNode) abstractNode;
						
						if ( (nodeAt.name.equals(nodemap.get("generateHeightMap.name")) || nodeAt.name.equals("generateHeightMap"))
						  && nodeAt.desc.equals(nodemap.get("generateHeightMap.desc")) ) {
							MethodInsnNode insertMethodNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									CLOAK_MANAGER_CLASS,
									"onFillChunk",
									"(Lnet/minecraft/world/chunk/Chunk;)V",
									false);
							methodnode.instructions.insertBefore(nodeAt, insertMethodNode);
							instructionIndex++;
							
							VarInsnNode insertVarNode = new VarInsnNode(Opcodes.ALOAD, 0);
							methodnode.instructions.insertBefore(nodeAt, insertVarNode);
							instructionIndex++;
							
							if (debugLog) { System.out.println("Injecting into " + classNode.name + "." + methodnode.name + " " + methodnode.desc); }
							injectedCount++;
						}
					}
					
					instructionIndex++;
				}
			}
		} while (true);
		
		if (injectedCount != operationCount) {
			System.out.println("Injection failed for " + classNode.name + " (" + injectedCount + " / " + operationCount + "), aborting...");
		} else {
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			bytes = writer.toByteArray();
			System.out.println("Injection successfull!");
		}
		return bytes;
	}
	
	private static void deasm(AbstractInsnNode abstractNode) {
		if (abstractNode instanceof VarInsnNode) {
			VarInsnNode node = (VarInsnNode) abstractNode;
			System.out.println("  + Var " + node.var);
			
		} else if (abstractNode instanceof LabelNode) {
			LabelNode node = (LabelNode) abstractNode;
			System.out.println("  + Label " + node.getLabel());
			
		} else if (abstractNode instanceof LineNumberNode) {
			LineNumberNode node = (LineNumberNode) abstractNode;
			System.out.println("  + Line " + node.line);
			
		} else if (abstractNode instanceof InsnNode) {
			InsnNode node = (InsnNode) abstractNode;
			System.out.println("  + Instruction " + node);
			
		} else if (abstractNode instanceof LdcInsnNode) {
			LdcInsnNode node = (LdcInsnNode) abstractNode;
			System.out.println("  + Load " + node.cst);
			
		} else if (abstractNode instanceof FieldInsnNode) {
			FieldInsnNode node = (FieldInsnNode) abstractNode;
			System.out.println("  + Field " + node.owner + " " + node.name + " " + node.desc);
			
		} else if (abstractNode instanceof MethodInsnNode) {
			MethodInsnNode node = (MethodInsnNode) abstractNode;
			System.out.println("  + Method " + node.owner + " " + node.name + " " + node.desc);
			
		} else {
			System.out.println("  + Instruction " + abstractNode.getOpcode() + " " + abstractNode.getType() + " " + abstractNode.toString());
		}

	}
}
