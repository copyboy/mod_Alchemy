package net.mcft.copy.alchemy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.*;

public class EntityCircle extends Entity {

	/* The radius of the circle in blocks. */
	public float radius;
	/* The direction the circle is facing.
	 * 0 = bottom, 1 = top, 2 = east, 3 = west, 4 = north, 5 = south. */
	public int direction;
	
	public boolean active;
	public float activeTimer;
	
	int[] attachedBlockIds;
	
	public EntityCircle(World world) {
		super(world);
	}
	public EntityCircle(World world, double x, double y, double z, float radius, int direction) {
		this(world);
		this.radius = radius;
		this.direction = direction;

		setPosition(x, y, z);
		setSize(radius * 2, radius * 2);
		yOffset = 0.0f;
	}
	
	public static EntityCircle createFromBlockPosition(World world, int blockX, int blockY, int blockZ, double x, double y, float radius, int direction) {
		double posX = blockX, posY = blockY, posZ = blockZ;
		switch (direction) {
			case 0:            posX +=     x; posZ += 1 - y; break;
			case 1: posY += 1; posX +=     x; posZ +=     y; break;
			case 2:            posX +=     x; posY +=     y; break;
			case 3: posZ += 1; posX += 1 - x; posY +=     y; break;
			case 4:            posZ += 1 - x; posY +=     y; break;
			case 5: posX += 1; posZ +=     x; posY +=     y; break;
		}
		return new EntityCircle(world, posX, posY, posZ, radius, direction);
	}
	
	@Override
	protected void entityInit() {  }
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		direction = compound.getByte("direction");
	}
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setByte("direction", (byte)direction);
	}
	
	@Override
	public void onUpdate() {
		if (isDead || worldObj.isRemote) return;

		int i = 0;
		List<BlockPosition> attached = getAttachedBlocks();
		if (attachedBlockIds == null) {
			attachedBlockIds = new int[attached.size()];
			for (BlockPosition pos : attached)
				attachedBlockIds[i++] = worldObj.getBlockId(pos.x, pos.y, pos.z);
		} else {
			for (BlockPosition pos : attached)
				if (attachedBlockIds[i++] != worldObj.getBlockId(pos.x, pos.y, pos.z)) {
					setDead();
					break;
				}
		}
		
		if (!active) activeTimer = 0;
	}
	
	@Override
	public int getBrightnessForRender(float par1) {
		int skylight = (int)(activeTimer * 0xF0);
		int blocklight = 0x80;
		return (active ? (skylight << 16 | blocklight) : 0);
	}
	
	/*
	 * Returns the blocks this circle is attached too.
	 * Breaking one of these blocks would destroy it.
	 */
	// TODO: This looks horrible, there has to be a better way to do this.
	List<BlockPosition> attachedBlocks;
	public List<BlockPosition> getAttachedBlocks() {
		if (attachedBlocks != null)
			return attachedBlocks;
		int minX, minY, minZ, maxX, maxY, maxZ;
		double px = posX, py = posY, pz = posZ;
		switch (direction) {
			case 0: py += 0.5; break;
			case 1: py -= 0.5; break;
			case 2: pz += 0.5; break;
			case 3: pz -= 0.5; break;
			case 4: px += 0.5; break;
			case 5: px -= 0.5; break;
		}
		minX = maxX = (int)Math.floor(px);
		minY = maxY = (int)Math.floor(py);
		minZ = maxZ = (int)Math.floor(pz);
		switch (direction) {
			case 0: case 1:
				minX = (int)Math.floor(px - radius);
				minZ = (int)Math.floor(pz - radius);
				maxX = (int)Math.floor(px + radius);
				maxZ = (int)Math.floor(pz + radius);
				break;
			case 2: case 3:
				minX = (int)Math.floor(px - radius);
				minY = (int)Math.floor(py - radius);
				maxX = (int)Math.floor(px + radius);
				maxY = (int)Math.floor(py + radius);
				break;
			case 4: case 5:
				minZ = (int)Math.floor(pz - radius);
				minY = (int)Math.floor(py - radius);
				maxZ = (int)Math.floor(pz + radius);
				maxY = (int)Math.floor(py + radius);
				break;
		}
		List<BlockPosition> list = new ArrayList<BlockPosition>();
		for (int x = minX; x <= maxX; x++)
			for (int y = minY; y <= maxY; y++)
				for (int z = minZ; z <= maxZ; z++)
					list.add(new BlockPosition(x, y, z));
		attachedBlocks = list;
		return list;
	}

}
