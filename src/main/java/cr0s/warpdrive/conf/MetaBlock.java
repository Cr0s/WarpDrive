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
	
	public static final int MAX_METADATA = 16;//I think this is 16?

	private static HashMap<String, MetaBlock[]> metablocks = new HashMap<String, MetaBlock[]>();
	
	private Block block;
	private int metadata;

	private MetaBlock(Block b, int metadata) {
		this.block = b;
		this.metadata = metadata;
	}

	/**
	 * @return the block
	 */
	public Block getBlock() {
		return block;
	}

	/**
	 * @return the metadata
	 */
	public int getMetadata() {
		return metadata;
	}
	
	/**
	 * Gets an instance of a metablock with the given block and metadata information.
	 * 
	 * @param b
	 *            Block to use
	 * @param metadata
	 *            Metadata to use. Must be less than MAX_METADATA
	 * @return A MetaBlock
	 */
	public static MetaBlock getMetaBlock(Block b, int metadata) {
		
		if (metadata < 0 || metadata >= MAX_METADATA)
			throw new IllegalArgumentException("Metadata to get MetaBlock must be > 0 or < " + MAX_METADATA);
		
		MetaBlock[] block = metablocks.get(b.getUnlocalizedName());
		
		if (block == null) {
			block = new MetaBlock[MAX_METADATA];
			metablocks.put(b.getUnlocalizedName(), block);
		}
		
		if (block[metadata] == null) {
			block[metadata] = new MetaBlock(b, metadata);
		}
		
		return block[metadata];
	}
	
	@Override
	public int compareTo(Object o) {
		int strComp = block.getUnlocalizedName().compareTo(((MetaBlock) o).block.getUnlocalizedName());
		return strComp != 0 ? strComp : metadata - ((MetaBlock) o).metadata;
	}

	@Override
	public String toString() {
		return block.getUnlocalizedName() + ":" + metadata;
	}
	
}
