package net.mcft.copy.alchemy;

import net.minecraft.src.*;

public class EntityCircle extends Entity {

	/* The direction the circle is facing.
	 * 0 = bottom, 1 = top, 2 = east, 3 = west, 4 = north, 5 = south. */
	public int direction;
	
	public boolean active;
	public float activeTimer;
	public int blockId = -1;
	
	public EntityCircle(World world) {
		super(world);
	}
	public EntityCircle(World world, int x, int y, int z, int direction) {
		this(world);
		this.direction = direction;
		
		setSize(1, 1);
		setPosition(x + 0.5, y + 0.5, z + 0.5);
		yOffset = 0.0f;
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
		
		int x = (int)posX;
		int y = (int)posY;
		int z = (int)posZ;
		
		if (blockId == -1)
			blockId = worldObj.getBlockId(x, y, z); // Store block ID.
		else if (blockId != worldObj.getBlockId(x, y, z))
			setDead(); // Destroy if block ID changed.
		if (active) activeTimer = Math.min(activeTimer + 0.08f, 1);
		else activeTimer = 0;
	}
	
	@Override
	public int getBrightnessForRender(float par1) {
		int skylight = (int)(activeTimer * 0xFF);
		int blocklight = 0x80;
		return (active ? (skylight << 16 | blocklight) : 0);
	}

}
