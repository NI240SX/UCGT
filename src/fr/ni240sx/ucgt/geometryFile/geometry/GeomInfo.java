package fr.ni240sx.ucgt.geometryFile.geometry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.GeometryEditorCLI;
import fr.ni240sx.ucgt.geometryFile.Platform;

public class GeomInfo extends Block {

	@Override
	public BlockType getBlockID() {return BlockType.Geom_Info;}
	public static final int usualLength = 144;
	
	public int const01=0, const02=3, platf=PLATF_UNDERCOVER, partsCount=0;
	public String filename="Compiled with UCGT v"+GeometryEditorCLI.programVersion+" | needeka", blockname="DEFAULT";
	public int const1=128, const21=CONST21_UNDERCOVER, const22=CONST21_UNDERCOVER, const23=CONST23_UNDERCOVER, const24=CONST23_UNDERCOVER;

	public static final int PLATF_UNDERCOVER = 30;
	public static final int PLATF_LEGACY = 29;

	public static final int CONST21_UNDERCOVER = 4403336;
	public static final int CONST23_UNDERCOVER = 4403344;
	
	public static final int CONST21_PROSTREET = 4391048;
	public static final int CONST23_PROSTREET = 4391056;

	public GeomInfo(ByteBuffer in) {
		var blockLength = in.getInt();
		if (blockLength != usualLength) System.out.println("[WARN] Unexpected info block length : "+blockLength+" instead of 144.");
		var blockStart = in.position();
		
		const01 = in.getInt();
		const02 = in.getInt();
		platf = in.getInt();
		partsCount = in.getInt();
		
		filename = Block.readString(in);
		in.position(blockStart+72);
		blockname = Block.readString(in);
		in.position(blockStart+blockLength-32);
		const1 = in.getInt();
		in.getInt();
		in.getInt();
		in.getInt();
		const21 = in.getInt();
		const22 = in.getInt();
		const23 = in.getInt();
		const24 = in.getInt();
	}

	public GeomInfo() {
	}
	
	public void setPlatform(Platform plat) {
		switch(plat) {
		case PC:
		case X360:
			platf = PLATF_UNDERCOVER;
			const21 = CONST21_UNDERCOVER;
			const22 = CONST21_UNDERCOVER;
			const23 = CONST23_UNDERCOVER;
			const24 = CONST23_UNDERCOVER;
			break;
		case Carbon_PC:
			platf = PLATF_LEGACY;
			const21 = 0;
			const22 = 0;
			const23 = 0;
			const24 = 0;
			break;
		case Prostreet_PC:
		case Prostreet_X360:
			platf = PLATF_LEGACY;
			const21 = CONST21_PROSTREET;
			const22 = CONST21_PROSTREET;
			const23 = CONST23_PROSTREET;
			const24 = CONST23_PROSTREET;
			break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + plat);
		}
		
	}

	public void setHeaderVanilla() {
		filename = "GEOMETRY.BIN";
		blockname = "DEFAULT";
	}
	
	public void setHeaderCtk() {
		filename = "NFS-CarToolkit by nfsu360 | Jul  7 2020";
		blockname = "TOOLKIT";
	}

	public int filenameType() {
		if (filename.equals("GEOMETRY.BIN")) return 0;
		if (filename.startsWith("Compiled with UCGT")) return 1;
		if (filename.startsWith("NFS-CarToolkit by nfsu360")) return 1;
		return 2;
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException {

		var buf = ByteBuffer.wrap(new byte[usualLength+8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey()); 
		buf.putInt(usualLength); //length for later
		
		buf.putInt(const01);
		buf.putInt(const02);
		buf.putInt(platf);
		buf.putInt(partsCount);
		
		Block.putString(buf, filename, 55);
		buf.position(80); //go to second string
		Block.putString(buf, blockname, 39);
		buf.position(120);

		buf.putInt(const1);
		buf.putInt(0);
		buf.putInt(0);
		buf.putInt(0);

		buf.putInt(const21);
		buf.putInt(const22);
		buf.putInt(const23);
		buf.putInt(const24);
		
		return buf.array();	
	}

}
