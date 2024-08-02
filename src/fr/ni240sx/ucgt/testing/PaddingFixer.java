package fr.ni240sx.ucgt.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.dbmpPlus.DBMP;
import fr.ni240sx.ucgt.dbmpPlus.DBMPPlus;

public class PaddingFixer {

	@SuppressWarnings("unused")
	public static ArrayList<Block> load(File f, int modulo) throws IOException {
		var fis = new FileInputStream(f);
		var fileToBytes = new byte[(int)f.length()];
		fis.read(fileToBytes);
		fis.close();
		
		var blocks = new ArrayList<Block>();
		
		var in = ByteBuffer.wrap(fileToBytes);
		in.order(ByteOrder.LITTLE_ENDIAN);
		
		while (true) {
			try {
				blocks.add(readBlock(in));
			} catch (Exception e) {
				//EOF reached, most likely
				break;
			}
		}
		
		return blocks;
		
	}

	public static void save(File f, int modulo, ArrayList<Block> blocks) throws FileNotFoundException, IOException {
		var fos = new FileOutputStream(f);
		int outPos = 0;
		for (var b : blocks) if (b.type != 0) {
			var bb = ByteBuffer.wrap(new byte[8]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(b.type);
			bb.putInt(b.data.length);
			fos.write(bb.array());
			fos.write(b.data);
			outPos += b.data.length+8;
			outPos += makePadding(fos, modulo, outPos);
		}
		fos.close();
	}
	
	public static int makePadding(FileOutputStream fos, int modulo, int outPos) throws IOException {
		if (outPos % modulo != 0) {
			var length = (modulo - outPos % modulo) - 8;
			if ((modulo - outPos % modulo) < 8) {
				//padding length : outPos % modulo + modulo - 8
				length += modulo;
			}
			var bb = ByteBuffer.wrap(new byte[8]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(0); //padding block id
			bb.putInt(length);
			fos.write(bb.array());
			fos.write(new byte[length]);
			return length+8;
		}
		return 0;
	}

	public static Block readBlock(ByteBuffer bb) {
		var b = new Block();
		b.type = bb.getInt();
		var length = bb.getInt();
		if (b.type != 0) {
			b.data = new byte[length];
			bb.get(b.data);
		} else {
			bb.position(bb.position()+length);
		}
		return b;
	}
	
	public static void main(String[] args) throws IOException {
		final var vinylsFile = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\VINYLS\\VINYLS.BIN");
		final var modulo = 2048;
		final var dbmpFile = new File("G:\\Autres ordinateurs\\Mon ordinateur\\a UCE BETA 1 PREVIEW PACK\\DATA\\CARS\\VINYLS\\DBMPVECTORVINYL_UCE1.3+.bin");
		
		var db = DBMP.loadDBMP(dbmpFile, false);
		
		var hashlist = new ArrayList<Hash>();
		var partIDMap = new HashMap<Hash,Byte>();
		
		for (var p : db.dBMPParts) {
			var h = new Hash(p.getAttributeString("NAME_OFFSET").value1);
			hashlist.add(h);
			partIDMap.put(h, p.getAttributeCarPartID("PARTID_UPGRADE_GROUP").level);
		}
		
		var blocks = load(vinylsFile, modulo);
		
		for (int i=0; i<blocks.size(); i++) {
			if (blocks.get(i).type == 0) {
				blocks.remove(i);
				i--;
			}
		}
		
		for (var b : blocks) {
			//find name
			var bb = ByteBuffer.wrap(b.data);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			b.name = Hash.guess(bb.getInt(24), hashlist, "UNKNOWN NAME HASH", "BIN");
			b.level = partIDMap.get(b.name);
			System.out.println(b.name.label+" level "+b.level);
		}
		blocks.sort(new VinylsSorter());
		save(vinylsFile, modulo, blocks);
	}
	
}
class Block{
	int type;
	byte[] data;
	Hash name;
	byte level;
}
class VinylsSorter implements Comparator<Block>{

	@Override
	public int compare(Block o1, Block o2) {
		return (levelMapping(o1.level)+o1.name.label).compareTo(levelMapping(o2.level)+o2.name.label);
	}
	
	public static String levelMapping(int level) {
		switch(level) {
		case 6: return "01";
		case 1: return "02";
		case 0: return "03";
		case 7: return "04";
		case 21: return "05";
		case 12: return "06";
		case 14: return "07";
		case 4: return "08";
		case 13: return "09";
		case 5: return "10";
		case 3: return "11";
		case 19: return "12";
		case 18: return "13";
		case 10: return "14";
		case 2: return "15";
		case 17: return "16";
		case 16: return "17";
		case 15: return "18";
		case 22: return "19";
		case 25: return "20";
		default : return "00";
		}
	}
}