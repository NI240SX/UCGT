package fr.ni240sx.ucgt.geometryFile.textures;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;

public class TexList extends Block {
	
	public List<Integer> textures = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {
		return BlockType.TPK_TexList;
	}

	public TexList(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		while (in.position()< blockStart+blockLength) {
			textures.add(in.getInt());
			in.getInt();
		}
		in.position(blockStart+blockLength);		
	}


	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var out = ByteBuffer.wrap(new byte[textures.size()*8+8]);
		out.order(ByteOrder.LITTLE_ENDIAN);
		out.putInt(getBlockID().getKey());
		out.putInt(textures.size()*8);

		for (var t : textures) {
			out.putInt(t);
			out.putInt(0);
		}
		
		return out.array();
	}
	
	public TexList(List<NFSTexture> textures) {
		textures.forEach(t -> this.textures.add(t.binKey));
	}

	public void refresh(List<NFSTexture> tex) {
		textures.clear();
		tex.forEach(t -> this.textures.add(t.binKey));
//		textures.sort(new TexturesKeySorter());
	}

}

class TexturesKeySorter implements Comparator<Integer>{

	@Override
	public int compare(Integer o1, Integer o2) {
		return Integer.compareUnsigned(o1, o2);
	}
}