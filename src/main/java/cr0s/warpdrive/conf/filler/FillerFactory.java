package cr0s.warpdrive.conf.filler;

import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.block.Block;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.MetaBlock;

/**
 * Used for constructing FillerSets from a combination of Weights and Ratios
 *
 */
public class FillerFactory {

	private TreeMap<MetaBlock, Integer> metaBlocksWeight;
	private TreeMap<MetaBlock, Integer> convertedRatioMetaBlocks;

	private int totalWeightNeeded;
	private int totalWeightWeightedBlocks;
	
	private Object lock;

	//Used to keep track of added ratio blocks in case this factory is imported into another.
	private TreeMap<MetaBlock, String> addedRatioMetaBlocks;
	
	/**
	 * Initializes new FillerFactory
	 */
	public FillerFactory() {
		lock = new Object();
		init();
	}

	/**
	 * Clears all data and initializes needed arrays
	 */
	private void init() {

		metaBlocksWeight = new TreeMap<MetaBlock, Integer>();
		convertedRatioMetaBlocks = new TreeMap<MetaBlock, Integer>();

		totalWeightNeeded = 0;
		totalWeightWeightedBlocks = 0;
		
		addedRatioMetaBlocks = new TreeMap<MetaBlock, String>();

	}

	/**
	 * Resets to how this factory was at the start, and ready to create a new FillerSet
	 */
	public void reset() {
		init();
	}

	/**
	 * Convenience method which converts to a MetaBlock and then calls addWeightedMetaBlock
	 *
	 * @param b
	 *            Block to add
	 * @param metadata
	 *            Metadata to add
	 * @param weight
	 *            Weight the block was given
	 */
	public void addWeightedBlock(Block b, int metadata, int weight) {
		addWeightedMetaBlock(MetaBlock.getMetaBlock(b, metadata), weight);
	}

	/**
	 * Adds the given Meta Block as an originally weighted block.
	 *
	 * @param mb
	 *            MetaBlock info to add
	 * @param weight
	 *            The given weight of the block
	 */
	public void addWeightedMetaBlock(MetaBlock mb, int weight) {

		metaBlocksWeight.put(mb, weight);
		totalWeightWeightedBlocks += weight;

	}

	/**
	 * Convenience method which converts to a MetaBlock and then calls addRatioMetaBlock
	 *
	 * @param b
	 *            Block to add
	 * @param metadata
	 *            Block's metadata
	 * @param ratio
	 *            The given ratio, in the original string form
	 */
	public void addRatioBlock(Block b, int metadata, String ratio) {
		addRatioMetaBlock(MetaBlock.getMetaBlock(b, metadata), ratio);
	}

	/**
	 * Add a MetaBlock weith the given ratio. Ratio should be convertible to a double.
	 *
	 * @param mb
	 * @param ratio
	 */
	public void addRatioMetaBlock(MetaBlock mb, String ratio) {
		
		//In case of an import
		addedRatioMetaBlocks.put(mb, ratio);
		
		//Prevent this from being run twice at once
		synchronized (lock) {

			//Clean out anything unnecessary
			ratio = ratio.trim();

			//Make sure it is an actual number
			if (Double.parseDouble(ratio) > 1)
				throw new IllegalArgumentException("Ratio must be less than one");
			
			//Get the position of the period, so that we can find the number of significant digits.
			
			//This will probably be at 1
			int lastDot = ratio.lastIndexOf('.');
			
			int sigFig;
			//Go through and check to find the actual last significant digit.
			for (sigFig = ratio.length(); sigFig > lastDot; sigFig--) {
				
				if (ratio.charAt(sigFig) != '0')
					break;
				
			}
			
			//Check to make sure there was an actual number.
			if (sigFig <= lastDot)
				throw new IllegalArgumentException("Ratio must be greater than zero");

			int ratioInt = Integer.parseInt(ratio.substring(lastDot + 1, sigFig + 1));

			//Weight needed should be a power of ten
			int digits = sigFig - lastDot;
			int weightNeeded = (int) Math.pow(10, digits);

			//Check if the weight needed for accuracy is greater than all the ones until now
			if (weightNeeded > totalWeightNeeded) {

				//Calculate the extra powers needed by existing ratio MetaBlocks
				int additionalPowersNeeded = totalWeightNeeded / weightNeeded;

				//Convert them to the new standard
				for (Entry<MetaBlock, Integer> entry : convertedRatioMetaBlocks.entrySet()) {

					convertedRatioMetaBlocks.put(entry.getKey(), entry.getValue() * additionalPowersNeeded);

				}

				totalWeightNeeded = weightNeeded;

			} else if (weightNeeded < totalWeightNeeded) { //The weight needed is less

				//Calculate the extra powers needed by the new ratio MetaBlock
				int additionalPowersNeeded = weightNeeded / totalWeightNeeded;

				ratioInt *= additionalPowersNeeded;

			}

			convertedRatioMetaBlocks.put(mb, ratioInt);

		}
		
	}

	/**
	 * Construct the actual array that should be used to generate random blocks
	 * 
	 * @return A MetaBlock array
	 */
	public MetaBlock[] constructWeightedMetaBlockList() {

		//Prevent this from being run twice at once
		synchronized (lock) {

			//If there are no ratios, convert directly to an array
			if(convertedRatioMetaBlocks.isEmpty()) {

				MetaBlock[] list = new MetaBlock[totalWeightWeightedBlocks];

				int index = 0;
				for (Entry<MetaBlock, Integer> entry : metaBlocksWeight.entrySet()) {
					
					for (int i = 0; i < entry.getValue(); i++)
						list[index++] = entry.getKey();
					
				}

				return list;

			}

			//Add up ratio weights
			
			MetaBlock[] list = new MetaBlock[totalWeightNeeded];
			
			int ratioTotalWeightUsed = 0;
			int index = 0;
			for (Entry<MetaBlock, Integer> entry : convertedRatioMetaBlocks.entrySet()) {
				
				for (int i = 0; i < entry.getValue(); i++)
					list[index++] = entry.getKey();
				
				ratioTotalWeightUsed += entry.getValue();
			}

			int remainingWeight = totalWeightNeeded - ratioTotalWeightUsed;

			if(remainingWeight < 0)
				throw new IllegalArgumentException("Ratios add up to more than 100%");
			

			if(remainingWeight == 0) {
				WarpDrive.logger.info("Ratios add up perfectly to 100%, skipping weights");
			} else {
				
				//Add

				//Add in weights
				//There are two ways to do this.
				//Option 1: Convert weighted Blocks to ratios compared to each other, and then get that ratio of the remaining
				//Option 2: Treat the largest weight as a filler for unfilled places
				
				//For now using option 1
				//TODO: Make adjustable
				
				for (Entry<MetaBlock, Integer> entry : metaBlocksWeight.entrySet()) {
					
					int converted = entry.getValue() * remainingWeight / totalWeightWeightedBlocks;
					
					for (int i = 0; i < converted; i++)
						list[index++] = entry.getKey();
					
				}

			}

			return list;

		}

	}
	
	/**
	 * Add all the blocks previously added to the given factory to this factory
	 *
	 * @param factory
	 *            Factory to get MetaBlocks from
	 */
	public void addFromFactory(FillerFactory factory) {
		
		for (Entry<MetaBlock, String> addedBlock : factory.addedRatioMetaBlocks.entrySet())
			addRatioMetaBlock(addedBlock.getKey(), addedBlock.getValue());

		for (Entry<MetaBlock, Integer> addedBlock : factory.metaBlocksWeight.entrySet())
			addWeightedMetaBlock(addedBlock.getKey(), addedBlock.getValue());
		

	}

}
