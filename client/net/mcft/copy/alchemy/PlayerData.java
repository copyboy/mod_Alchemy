package client.net.mcft.copy.alchemy;

import net.minecraft.nbt.NBTTagCompound;

public class PlayerData {

	public String name;
	public float experience;
	public float power;
	
	public NBTTagCompound ToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("name", name);
		compound.setFloat("experience", experience);
		compound.setFloat("power", power);
		return compound;
	}
	public static PlayerData FromNBT(NBTTagCompound compound) {
		PlayerData data = new PlayerData();
		data.name = compound.getString("name");
		data.experience = compound.getFloat("experience");
		data.power = compound.getFloat("power");
		return data;
	}

}
