package fr.ni240sx.ucgt.geometryFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.collisionsEditor.CollisionBound;
import fr.ni240sx.ucgt.collisionsEditor.CollisionsEditor;
import fr.ni240sx.ucgt.compression.Compression;
import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.geometryFile.part.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;

public class Part extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part;}
	
	public int partKey;
	
	public int decompressedLength;
	public int compressedLength;
	
	public byte[] compressedData;
	
	public static CompressionLevel defaultCompressionLevel = CompressionLevel.High;
	
	public PartHeader header;
	public TexUsage texusage;
	public Strings strings;
	public Shaders shaderlist;
	public MPoints mpoints;
	public Mesh mesh;
	
	public String kit;
	public String part;
//	public byte autosculptZone = -1;
	public String lod;
	
	public Part(ByteBuffer in, int partKey) {
		this.partKey = partKey;		
		in.order(ByteOrder.LITTLE_ENDIAN);
		in.getInt(); //ID
		var blockLength = in.getInt();
		var blockStart = in.position();
		Block block;
		
		while((in.position() < blockStart+blockLength)) {
			if ((block = Block.read(in)) != null) subBlocks.add(block);
		}
		
		//TODO read blocks properly and find partName
		// SUB-BLOCKS PRE-TREATMENT TO REFERENCE THEM ALL
		// if there's more than one block only the last one is taken into account
		for (var b : subBlocks) {
			switch (b.getBlockID()) {
			case Part_Header:
				header = (PartHeader) b;
				break;
			case Part_TexUsage:
				texusage = (TexUsage) b;
				break;
			case Part_Strings:
				strings = (Strings) b;
				break;
			case Part_ShaderList:
				shaderlist = (Shaders) b;
				break;
			case Part_MPoints:
				mpoints = (MPoints) b;
				break;
			case Part_Mesh:
				mesh = (Mesh) b;
				break;
			case Part_HashAssign:
				break;
			case Part_HashList:
				break;
			case Part_AutosculptLinking:
				break;
			case Part_AutosculptZones:
				break;
			default:
				break;
			}
		}
		
		if (header != null) {
			if (new Hash(header.partName).binHash != partKey) System.out.println("WARNING : incorrect part name "+header.partName);
			
			kit = "KIT" + header.partName.split("_KIT")[1].split("_")[0];
			lod = header.partName.split("_")[header.partName.split("_").length-1];
//			String s = header.partName.split("_")[header.partName.split("_").length-2];
//			if (s.length() == 2 && s.charAt(0) == 'T') autosculptZone = Byte.parseByte(s.substring(1));
			part = header.partName.split(kit+"_")[1].replace("_"+lod, "")/*.replace("_T"+autosculptZone, "")*/;
//			if (autosculptZone == -1) 
//				System.out.println("Kit : "+kit+", part : "+part+", lod : "+lod);
//			else System.out.println("Kit : "+kit+", part : "+part+", autosculpt : T"+autosculptZone+", lod : "+lod);
		}
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		out.write(buf.array());
		
		for (var b : subBlocks) {
			if (b != null) out.write(b.save(out.size()));
		}

		buf = ByteBuffer.wrap(new byte[4]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(out.size()-8);

		var arr = out.toByteArray();
		arr[4] = buf.array()[0];
		arr[5] = buf.array()[1];
		arr[6] = buf.array()[2];
		arr[7] = buf.array()[3]; //writes the correct size
		
		return arr;
	}
	
	public void precompress() throws IOException, InterruptedException {
		
		//TODO check partKey according to the partName
		
		var partBytes = this.save(0);
		decompressedLength = partBytes.length;
		
		compressedData  = Compression.compress(partBytes, "RFPK", defaultCompressionLevel);
		compressedLength = compressedData.length + 24;
	}
	
	
	public static void main(String[] args) {
		try {
			long t = System.currentTimeMillis();
			
//			Block.doNotRead.put(GeomBlock.Part_Mesh, true);
			
			File f;
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BASE_A"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_MUFFLER_05_C"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BRAKE_FRONT_A"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BRAKE_REAR_A"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_FENDER_FRONT_RIGHT_T1_A"));
			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_BUMPER_FRONT_A"));

//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_A"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_A"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_T0_A"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_T0_A"));

//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_C"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_C"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_T0_C"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_T0_C"));

//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_B"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_B"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_T0_B"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_T0_B"));

			byte [] arr = new byte[(int)f.length()];
			fis.read(arr);
			fis.close();
			
			System.out.println("File loaded in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

			var part = new Part(ByteBuffer.wrap(arr), new Hash("AUD_RS4_STK_08_KIT00_BASE_A").binHash);
			
			System.out.println("Part read in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BASE_A-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_MUFFLER_05_C-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BRAKE_FRONT_A-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BRAKE_REAR_A-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_FENDER_FRONT_RIGHT_T1_A-recompiled"));
			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_BUMPER_FRONT_A-recompiled"));

//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_A-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_A-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_T0_A-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_T0_A-recompiled"));

//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_C-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_C-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_T0_C-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_T0_C-recompiled"));

//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_B-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_B-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_LEFT_T0_B-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_DOOR_REAR_RIGHT_T0_B-recompiled"));

			fos.write(part.save(0));
			fos.close();
						
			System.out.println("File saved in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
}