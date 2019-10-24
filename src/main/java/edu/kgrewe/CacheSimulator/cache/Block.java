package edu.kgrewe.CacheSimulator.cache;

/**
 * Represents a block of physical memory.
 * 
 * @author kgrewe
 *
 */
public class Block {
	private long tag;

	public Block(long tag2) {
		setTag(tag2);
	}

	public long getTag() {
		return tag;
	}

	public void setTag(long tag) {
		this.tag = tag;
	}
}
