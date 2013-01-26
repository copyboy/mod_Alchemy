package client.net.mcft.copy.alchemy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.src.ModLoader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class SaveDataHandler implements ISaveEventHandler {

	HashMap<String, PlayerData> allPlayerData = new HashMap<String, PlayerData>();
	
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
	
	@Override
	public void onWorldLoad(World world) {
		NBTTagCompound compound;
		try {
			compound = CompressedStreamTools.read(getSaveFile(world));
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
	
	@Override
	public void onWorldSave(World world) {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		compound.setTag("players", list);
		for (PlayerData data : allPlayerData.values())
			list.appendTag(data.ToNBT());
		try {
			File saveFile = getSaveFile(world);
			saveFile.createNewFile();
			CompressedStreamTools.write(compound, saveFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public void onChunkLoad(World world, Chunk chunk) {  }
	@Override
	public void onChunkUnload(World world, Chunk chunk) {  }
	@Override
	public void onChunkSaveData(World world, Chunk chunk, NBTTagCompound data) {  }
	@Override
	public void onChunkLoadData(World world, Chunk chunk, NBTTagCompound data) {  }

}
