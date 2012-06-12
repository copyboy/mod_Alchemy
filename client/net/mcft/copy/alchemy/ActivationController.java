package net.mcft.copy.alchemy;

import net.minecraft.client.Minecraft;
import net.minecraft.src.*;

public class ActivationController {

	public boolean stopped = false;
	public EntityPlayer player;
	public EntityCircle circle;
	
	public ActivationController(EntityPlayer player, EntityCircle circle) {
		this.player = player;
		this.circle = circle;
	}
	
	public void start() {
		circle.active = true;
	}
	
	public void update() {
		if (!player.isSneaking() || player.getCurrentEquippedItem() != null ||
			circle.isDead || !player.isEntityAlive())
			// Abort if player isn't sneaking, holding an item, dead or circle was destroyed.
			abort();
	}
	
	public void abort() {
		circle.active = false;
		stopped = true;
	}
	
	public void finish() {
		circle.setDead();
		stopped = true;
		
		int x = (int)circle.posX;
		int y = (int)circle.posY;
		int z = (int)circle.posZ;
		World world = circle.worldObj;
		
		// Remove and drop the block.
		int blockId = world.getBlockId(x, y, z);
		int blockMetadata = world.getBlockMetadata(x, y, z);
		Block block = Block.blocksList[blockId];
		world.setBlockWithNotify(x, y, z, 0);
		block.harvestBlock(world, player, x, y, z, blockMetadata);
	}

}
