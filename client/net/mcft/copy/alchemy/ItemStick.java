package client.net.mcft.copy.alchemy;

import client.net.minecraft.src.mod_Alchemy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemStick extends Item {

	public ItemStick() {
		super(24);
		setItemName("stick");
		setIconCoord(0, 0);
		setFull3D();
	}

	@Override
	public String getTextureFile() {
		return "/alchemy/items.png";
	}

	@Override
	public int getIconIndex(ItemStack stack, int renderPass,
			EntityPlayer player, ItemStack usingItem, int useRemaining) {
		return iconIndex + ((player.getItemInUse() == stack) ? 16 : 0);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int useDuration) {
		mod_Alchemy.instance.finishDrawing();
	}

	@Override
	public ItemStack onFoodEaten(ItemStack stack, World world, EntityPlayer player) { return stack; }

	@Override
	public int getMaxItemUseDuration(ItemStack stack) { return 32000; }

	@Override
	public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.bow; }

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, getMaxItemUseDuration(stack));
		return stack;
	}

}
