package net.minecraft.src;

import java.io.File;
import java.util.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.mcft.copy.alchemy.*;
import net.mcft.copy.alchemy.geometry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.src.forge.*;

public class mod_Alchemy extends NetworkMod implements IRenderWorldLastHandler, IHighlightHandler {

	public static mod_Alchemy instance;
	
	int circleOrientation;
	int blockX, blockY, blockZ;
	List<Point> circlePointList;
	
	boolean pressingActivationButton;
	ActivationController activationController;
	
	SaveDataHandler saveDataHandler = new SaveDataHandler();
	
	@Override
	public String getVersion() { return "0.0.0"; }
	@Override
	public boolean clientSideRequired() { return true; }
	@Override
	public boolean serverSideRequired() { return false; }
	
	@Override
	public void load() {
		instance = this;
		Item.stick = new ItemStick();
		
		ModLoader.setInGameHook(this, true, true);
		MinecraftForge.registerSaveHandler(saveDataHandler);
		MinecraftForgeClient.registerHighlightHandler(this);
		MinecraftForgeClient.registerRenderLastHandler(this);

		Minecraft mc = ModLoader.getMinecraftInstance();
		ModLoader.registerKey(this, mc.gameSettings.keyBindAttack, false);
		ModLoader.registerEntityID(EntityCircle.class, "AlchemyCircle", ModLoader.getUniqueEntityId());
		
		preloadTextures();
	}
	
	@Override
	public void addRenderer(Map map) {
		map.put(EntityCircle.class, new RenderCircle());
	}
	
	void preloadTextures() {
		MinecraftForgeClient.preloadTexture("/redstoneTools/items.png");
		MinecraftForgeClient.preloadTexture("/redstoneTools/circles.png");
	}
	
	@Override
	public boolean onTickInGame(float partialTicks, Minecraft mc) {
		EntityPlayerSP player = mc.thePlayer;
		World world = player.worldObj;
		MovingObjectPosition pos = mc.objectMouseOver;
		
		updateDrawing(mc, player, pos);
		updateActivation(mc, player, world, pos);
		
		return true;
	}
	
	void updateDrawing(Minecraft mc, EntityPlayerSP player, MovingObjectPosition pos) {
		ItemStack stack = player.getItemInUse();
		if (stack == null || stack.itemID != Item.stick.shiftedIndex) {
			// Return and clear list if current used item is not ItemStick.
			circlePointList = null;
			return;
		}
		if (pos == null || pos.typeOfHit != EnumMovingObjectType.TILE)
			// Return if mouse isn't pointing to a block.
			return;
		
		// Reset block removing while drawing.
		mc.playerController.resetBlockRemoving();
		
		if (circlePointList == null) {
			circlePointList = new ArrayList<Point>(200);
			circleOrientation = pos.sideHit;
			blockX = pos.blockX;
			blockY = pos.blockY;
			blockZ = pos.blockZ;
		}
		if (circleOrientation != pos.sideHit ||
			(circleOrientation / 2 == 2 && blockX != pos.blockX) ||
			(circleOrientation / 2 == 0 && blockY != pos.blockY) ||
			(circleOrientation / 2 == 1 && blockZ != pos.blockZ))
			// Return if block position or side don't match.
			return;
		
		// Turn hit vector into point.
		Point point = null;
		switch (circleOrientation) {
			case 0: point = new Point(pos.hitVec.xCoord - blockX, 1 - (pos.hitVec.zCoord - blockZ)); break;
			case 1: point = new Point(     pos.hitVec.xCoord - blockX,  pos.hitVec.zCoord - blockZ); break;
			case 2: point = new Point(     pos.hitVec.xCoord - blockX,  pos.hitVec.yCoord - blockY); break;
			case 3: point = new Point(1 - (pos.hitVec.xCoord - blockX), pos.hitVec.yCoord - blockY); break;
			case 4: point = new Point(1 - (pos.hitVec.zCoord - blockZ), pos.hitVec.yCoord - blockY); break;
			case 5: point = new Point(     pos.hitVec.zCoord - blockZ,  pos.hitVec.yCoord - blockY); break;
		}
		if (!circlePointList.isEmpty()) {
			double distance = point.distanceTo(circlePointList.get(circlePointList.size() - 1));
			if (distance < 0.005 || distance > 0.7)
				// Return if new point is too close or far to last point.
				return;
		}
		circlePointList.add(point);
		return;
	}
	
	public void finishDrawing() {
		if (circlePointList == null) return;
		circlePointList = ShapeRecognizer.reduceToCharPoints(circlePointList);
		Shape shape = ShapeRecognizer.recognizeShape(circlePointList);
		circlePointList = null;
		if (shape == null) return;

		World world = ModLoader.getMinecraftInstance().thePlayer.worldObj;
		if (findCircle(world, blockX, blockY, blockZ, circleOrientation) != null)
			// Return if there's already a circle.
			return;
		
		// Temporary: Spawn circle entity when shape is recognized.
		ShapeCircle circle = (ShapeCircle)shape;
		EntityCircle entity = EntityCircle.createFromBlockPosition(
				world, blockX, blockY, blockZ,
				circle.x, circle.y,
				(float)circle.radius, circleOrientation);
		world.spawnEntityInWorld(entity);
	}
	
	void updateActivation(Minecraft mc, EntityPlayer player, World world, MovingObjectPosition pos) {
		int keyCode = mc.gameSettings.keyBindUseItem.keyCode;
		boolean pressingButton = ((keyCode < 0) ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode));
		if (pressingButton && !pressingActivationButton)
			pressActivationButton(mc, player, world, pos);
		if (!pressingButton && pressingActivationButton)
			releaseActivationButton(mc, player, world, pos);
		pressingActivationButton = pressingButton;
		
		if (activationController != null) {
			activationController.update();
			if (activationController.stopped)
				activationController = null;
		}
	}
	
	void pressActivationButton(Minecraft mc, EntityPlayer player, World world, MovingObjectPosition pos) {
		if (!player.isSneaking() || player.getCurrentEquippedItem() != null ||
			pos == null || pos.typeOfHit != EnumMovingObjectType.TILE)
			// Return if key is not attack, player isn't sneaking, holding an item or pointing to a block.
			return;
		
		EntityCircle circle = findCircle(world, pos.blockX, pos.blockY, pos.blockZ, pos.sideHit);
		if (circle == null) return;
		activationController = new ActivationController(player, circle);
		activationController.start();
	}
	void releaseActivationButton(Minecraft mc, EntityPlayer player, World world, MovingObjectPosition pos) {
		if (activationController == null) return;
		activationController.abort();
		activationController = null;
	}
	
	@Override
	public boolean onBlockHighlight(RenderGlobal render, EntityPlayer player,
			                        MovingObjectPosition target, int i, ItemStack stack, float partialTicks) {
		if (stack == null || stack.itemID != Item.stick.shiftedIndex || player.getItemInUse() != stack)
			return false;
		// Only draw selection box (not block breaking) if ItemStick is in use.
		render.drawSelectionBox(player, target, i, stack, partialTicks);
		return true;
	}
	
	@Override
	public void onRenderWorldLast(RenderGlobal renderer, float partialTicks) {
		if (circlePointList == null) return;
		// Show the player what e's drawing.
		
		EntityPlayerSP player = renderer.mc.thePlayer;
		double xDif = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
		double yDif = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
		double zDif = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		
		GL11.glPushMatrix();
		
		// Move to draw position and orientation.
		GL11.glTranslated(blockX - xDif + 0.5, blockY - yDif + 0.5, blockZ - zDif + 0.5);
		switch (circleOrientation) {
			case 0:	GL11.glRotatef(90.0f, -1,  0, 0); break;
			case 1:	GL11.glRotatef(90.0f,  1,  0, 0); break;
			case 2: break;
			case 3: GL11.glRotatef(180.0f, 0,  1, 0); break;
			case 4:	GL11.glRotatef(90.0f,  0,  1, 0); break;
			case 5: GL11.glRotatef(90.0f,  0, -1, 0); break;
		}
		GL11.glTranslatef(-0.5f, -0.5f, -0.502f);
		
		Tessellator tes = Tessellator.instance;
		
		// Draw thicker black line.
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.5f);
		GL11.glLineWidth(8.0f);
		tes.startDrawing(GL11.GL_LINE_STRIP);
		for (Point p : circlePointList)
			tes.addVertex(p.x, p.y, 0);
		tes.draw();
		
		// Draw thinner white line.
		GL11.glColor4f(0.7f, 0.7f, 0.9f, 0.5f);
		GL11.glLineWidth(6.0f);
		tes.startDrawing(GL11.GL_LINE_STRIP);
		for (Point p : circlePointList)
			tes.addVertex(p.x, p.y, 0);
		tes.draw();
		
		GL11.glPopMatrix();
		
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	/* Returns a circle at a position in a world, facing a specific direction, or null if none was found. */
	public EntityCircle findCircle(World world, int blockX, int blockY, int blockZ, int side) {
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
				blockX - 0.1, blockY - 0.1, blockZ - 0.1,
				blockX + 1.1, blockY + 1.1, blockZ + 1.1);
		List circles = world.getEntitiesWithinAABB(EntityCircle.class, aabb);
		for (Object obj : circles) {
			EntityCircle circle = (EntityCircle)obj;
			if (side == -1 || circle.direction == side)
				return circle;
		}
		return null;
	}
	/* Returns a circle at a position in a world, or null if none was found. */
	public EntityCircle findCircle(World world, int blockX, int blockY, int blockZ) {
		return findCircle(world, blockX, blockY, blockZ, -1);
	}

}
