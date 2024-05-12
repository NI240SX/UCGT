package fr.ni240sx.ucgt.geometryFile.part;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;
import javafx.util.Pair;

public class TexUsage extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part_TexUsage;}

	// texture binhash, usage type
	public ArrayList<Pair<Integer,Integer>> texusage = new ArrayList<Pair<Integer,Integer>>();
	
	public TexUsage(ByteBuffer in) {
		var blockLength = in.getInt();
		
		for (int i=0; i< blockLength/12; i++) {
			texusage.add(new Pair<Integer, Integer>(in.getInt(), in.getInt()));
			in.getInt(); //0
		}
		
//		for (var p : texusage) {
//			System.out.println("Texture "+Integer.toHexString(p.getKey())+" has usage "+Usage.get(p.getValue()).getName());
//		}
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var out = ByteBuffer.wrap(new byte[texusage.size()*12 + 8]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(texusage.size()*12);

		for (var p : texusage) {
			out.putInt(p.getKey());
			out.putInt(p.getValue());
			out.putInt(0);
		}
		
		return out.array();	
	}
}

enum Usage {
	DIFFUSE(0xAE76334E, "DIFFUSE"),
	NORMAL(0x958A9502, "NORMAL"),
	ALPHA(0x5aff315c, "ALPHA"),
	SWATCH(0x0c8c5ed6, "SWATCH"),
	SELFILLUMINATION(0x39143000, "SELFILLUMINATION"),
	INVALID(0xFFFFFFFF, "INVALID");
	
    private final int key;
    private final String name;

    Usage(int key, String name) {
        this.key = Integer.reverseBytes(key);
        this.name = name;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static Usage get(int key) {
        for (Usage c : values()) {
            if (c.key == key) return c;
        }
        return INVALID; // Handle invalid value
    }
}
