package cr0s.warpdrive.conf;

import java.util.HashMap;

import net.minecraft.block.Block;

/**
 * Immutable class used to represent a block with metadata. An instance can only be retrieved with getMetaBlock(), which allows it to reuse instances
 *
 * @author TheNumenorean
 *
 */
public class MetaBlock implements Comparable {
	
	public static final int MAX_METADATA = 16;

	private static HashMap<String, MetaBlock[]> metablocks = new HashMap<String, MetaBlock[]>();
	
	// values can only be read, no need for getter/setter
	public final Block block;
	public final int metadata;

	private MetaBlock(Block block, int metadata) {
		this.block = block;
		this.metadata = metadata;
	}
	
	/**
	 * Gets an instance of a metablock with the given block and metadata information.
	 * 
	 * @param block
	 *            Block to use (non null)
	 * @param metadata
	 *            Metadata to use (0-15)
	 * @return A MetaBlock
	 */
	public static MetaBlock getMetaBlock(Block block, int metadata) {
		
		if (block == null) {
			throw new IllegalArgumentException("Block can't be null");
		}
		
		if (metadata < 0 || metadata >= MAX_METADATA) {
			throw new IllegalArgumentException("Metadata out of range in " + block.getUnlocalizedName() + ":" + metadata + ". Expecting 0 to " + (MAX_METADATA - 1));
		}
		
		MetaBlock[] metablock = metablocks.get(block.getUnlocalizedName());
		
		if (metablock == null) {
			metablock = new MetaBlock[MAX_METADATA];
			metablocks.put(block.getUnlocalizedName(), metablock);
		}
		
		if (metablock[metadata] == null) {
			metablock[metadata] = new MetaBlock(block, metadata);
		}
		
		return metablock[metadata];
	}
	
	@Override
	public int compareTo(Object object) {
		int strComp = block.getUnlocalizedName().compareTo(((MetaBlock) object).block.getUnlocalizedName());
		return strComp != 0 ? strComp : metadata - ((MetaBlock) object).metadata;
	}
	
	@Override
	public String toString() {
		return block.getUnlocalizedName() + ":" + metadata;
	}
	
	@Override
	public int hashCode() {
		return Block.getIdFromBlock(block) << 4 + metadata;
	}
}
