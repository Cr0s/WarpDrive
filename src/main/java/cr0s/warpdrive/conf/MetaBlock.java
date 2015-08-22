package cr0s.warpdrive.conf;

import java.util.HashMap;

import net.minecraft.block.Block;

public class MetaBlock implements Comparable {
	
	private static final int MAX_METADATA = 16;//I think this is 16?

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
	
	public static MetaBlock getMetaBlock(Block b, int metadata) {
		
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
		return block.getUnlocalizedName().compareTo(((MetaBlock) o).block.getUnlocalizedName());
	}

	@Override
	public String toString() {
		return block.getUnlocalizedName() + ":" + metadata;
	}
	
}
