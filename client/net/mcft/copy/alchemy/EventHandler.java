package client.net.mcft.copy.alchemy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.src.ModLoader;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class EventHandler {
	
	HashMap<String, PlayerData> allPlayerData = new HashMap<String, PlayerData>();
	
	@ForgeSubscribe
	public void onWorldLoad(EntityJoinWorldEvent evt) {
		
		NBTTagCompound compound;
		try {
			compound = CompressedStreamTools.read(getSaveFile(evt.world));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (compound == null)
			return;
		NBTTagList list = compound.getTagList("players");
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound playerCompound = (NBTTagCompound)list.tagAt(i);
			PlayerData playerData = PlayerData.FromNBT(playerCompound);
			allPlayerData.put(playerData.name, playerData);
		}
	}
	
	public PlayerData getPlayerData(String name) {
		name = name.toLowerCase();
		return allPlayerData.get(name);
	}
	public PlayerData getOrCreatePlayerData(String name) {
		name = name.toLowerCase();
		PlayerData data = getPlayerData(name);
		if (data == null) {
			data = new PlayerData();
			data.name = name;
		}
		return data;
	}
	
	public File getSaveFile(World world) {
		Minecraft mc = ModLoader.getMinecraftInstance();
		String worldDirectoryName = world.getSaveHandler().getSaveDirectoryName();
		String saveFileName = "alchemy.dat";
		String saveLocation;
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		switch(side) {
		case CLIENT: saveLocation = "saves/%s/%s";
		default : saveLocation = "%s/%s";
		}
		return new File(mc.mcDataDir, String.format(saveLocation, worldDirectoryName, saveFileName));
	}

}
