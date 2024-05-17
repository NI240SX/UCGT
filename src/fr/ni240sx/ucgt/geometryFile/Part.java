package fr.ni240sx.ucgt.geometryFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.compression.Compression;
import fr.ni240sx.ucgt.compression.CompressionLevel;

public class Part extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part;}
	
	public int partKey;
	
	public int decompressedLength;
	public int compressedLength;
	
	public byte[] compressedData;
	
	public static CompressionLevel defaultCompressionLevel = CompressionLevel.Minimum;
	
	public Part(ByteBuffer in, int partKey) {
		this.partKey = partKey;		
		in.order(ByteOrder.LITTLE_ENDIAN);
		in.getInt(); //ID
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		Block b;

		while((in.position() < blockStart+blockLength)) {
			subBlocks.add(b=Block.read(in));
		}
		
		//TODO read blocks properly and find partName
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
			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BASE_A"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_MUFFLER_05_C"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BRAKE_FRONT_A"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BRAKE_REAR_A"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_FENDER_FRONT_RIGHT_T1_A"));
			byte [] arr = new byte[(int)f.length()];
			fis.read(arr);
			fis.close();
			
			System.out.println("File loaded in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

			var part = new Part(ByteBuffer.wrap(arr), new Hash("AUD_RS4_STK_08_KIT00_BASE_A").binHash);
			
			System.out.println("Part read in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BASE_A-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_MUFFLER_05_C-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BRAKE_FRONT_A-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KIT00_BRAKE_REAR_A-recompiled"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\AUD_RS4_STK_08_KITW01_FENDER_FRONT_RIGHT_T1_A-recompiled"));
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
