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
			circle.isDead || !player.isEntityAlive()) {
			// Abort if player isn't sneaking, holding an item, dead or circle was destroyed.
			abort();
			return;
		}
		circle.activeTimer = Math.min(circle.activeTimer + 0.05f, 1);
		if (circle.activeTimer >= 1) finish();
	}
	
	public void abort() {
		circle.active = false;
		stopped = true;
	}
	
	public void finish() {
		circle.setDead();
		stopped = true;
		
		World world = circle.worldObj;
		
		for (BlockPosition pos : circle.getAttachedBlocks()) {
			int blockId = world.getBlockId(pos.x, pos.y, pos.z);
			if (blockId == 0) continue;
			int blockMetadata = world.getBlockMetadata(pos.x, pos.y, pos.z);
			Block block = Block.blocksList[blockId];
			world.setBlockWithNotify(pos.x, pos.y, pos.z, 0);
			block.harvestBlock(world, player, pos.x, pos.y, pos.z, blockMetadata);
		}
	}

}
