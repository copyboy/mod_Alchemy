package client.net.mcft.copy.alchemy;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

import client.net.mcft.copy.alchemy.geometry.BoundingBox;

public class RenderCircle extends Render {

    private RenderBlocks blockRenderer = new RenderBlocks();
    
	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		EntityCircle circle = (EntityCircle)entity;

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glPushMatrix();
		
		loadTexture("/alchemy/circles.png");
		
		// Move to circle position and orientation
		GL11.glTranslated(x, y, z);
		switch (circle.direction) {
			case 0:	GL11.glRotatef(90.0f, -1,  0, 0); break;
			case 1:	GL11.glRotatef(90.0f,  1,  0, 0); break;
			case 2: break;
			case 3: GL11.glRotatef(180.0f, 0,  1, 0); break;
			case 4:	GL11.glRotatef(90.0f,  0,  1, 0); break;
			case 5: GL11.glRotatef(90.0f,  0, -1, 0); break;
		}
		GL11.glTranslated(0, 0, -0.002);
		
		// Get vector and texture bounds.
		BoundingBox bb = BoundingBox.fromCenterAndSize(0, 0, circle.radius * 2, circle.radius * 2);
		BoundingBox uv = BoundingBox.one.move(0, 0).mult(1 / 8.0);
		Tessellator tes = Tessellator.instance;
		tes.startDrawingQuads();
		tes.addVertexWithUV(bb.minX, bb.minY, 0, uv.minX, uv.minY);
		tes.addVertexWithUV(bb.minX, bb.maxY, 0, uv.minX, uv.maxY);
		tes.addVertexWithUV(bb.maxX, bb.maxY, 0, uv.maxX, uv.maxY);
		tes.addVertexWithUV(bb.maxX, bb.minY, 0, uv.maxX, uv.minY);
		tes.draw();
		
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
	}

}
