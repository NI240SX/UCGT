package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.nio.ByteBuffer;
import java.util.Objects;

public class ZTexture{
	public int UID;
	public String texture;
	public String path;
	
	public ZTexture(ByteBuffer in) {
		UID = in.getInt();
		texture = ZModBlock.readString(in);
		path = ZModBlock.readString(in);
		in.getInt(); //1
		in.get();
		in.getLong(); //0
	}
	
	public ZTexture(String tex, String path) {
		texture = tex;
		this.path = path + "\\" + tex;
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, texture);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ZTexture other = (ZTexture) obj;
		return Objects.equals(path, other.path) && Objects.equals(texture, other.texture);
	}

	public void put(ByteBuffer block) {
		block.putInt(UID);
		ZModBlock.putString(block, texture);
		ZModBlock.putString(block, path);
		block.putInt(1);
		block.put((byte) 0);
		block.putLong(0);
	}

	public int computeLength() {
		//UID 4 + strings 5 + 5 + int 4 + 1 + long 8
		return 27 + texture.length() + path.length();
	}
}