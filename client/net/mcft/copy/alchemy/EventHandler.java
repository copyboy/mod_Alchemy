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
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;

public class EventHandler {
	
	HashMap<String, PlayerData> allPlayerData = new HashMap<String, PlayerData>();
	
	@ForgeSubscribe
	public void onWorldLoad(Load evt) {
		
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
	
	@ForgeSubscribe
	public void onWorldSave(Save evt) {
		
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		compound.setTag("players", list);
		for (PlayerData data : allPlayerData.values())
			list.appendTag(data.ToNBT());
		try {
			File saveFile = getSaveFile(evt.world);
			saveFile.createNewFile();
			CompressedStreamTools.write(compound, saveFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
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
		String saveFileName = "/alchemy.dat";
		String saveLocation = "/saves/";
		File savedFile = new File(mc.getMinecraftDir() + saveLocation + worldDirectoryName + saveFileName);
		return savedFile;
		//return new File(mc.mcDataDir, worldDirectoryName + saveLocation + saveFileName);
	}
}
