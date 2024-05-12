package fr.ni240sx.ucgt.geometryFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.compression.Compression;
import fr.ni240sx.ucgt.geometryFile.geometry.*;
import fr.ni240sx.ucgt.testing.GeomDump;

public class Geometry extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Geometry;}
	
	public GeomHeader geomHeader;
	public ArrayList<Part> parts = new ArrayList<Part>();
	
	public static boolean USE_MULTITHREADING = true;
	
	public Geometry(ByteBuffer in) {
		in.order(ByteOrder.LITTLE_ENDIAN);
		in.getInt(); //ID
		/*var blockLength =*/ in.getInt();
		in.getInt();
		in.getInt(); // skip common stuff
//		var blockStart = in.position();
		

		// read the header, if this goes wrong the file is probably corrupted or smth
		geomHeader = (GeomHeader) Block.read(in);
		
//		var keys = GeomDump.generateHashes("AUD_RS4_STK_08", new int[] {0,1,4,6,11}, new int[] {1});
//		var keys = GeomDump.generateHashes("NIS_240_SX_89", new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32}, new int[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16});
		
		
		// decompress and read parts by offsets
		for (var o : geomHeader.partsOffsets.partOffsets.values()) {
			byte[] partData = new byte[o.sizeDecompressed];
			ByteBuffer dataWriter = ByteBuffer.wrap(partData);
			
			in.position(o.offset);
			//loops on the one or multiple compressed blocks
			while (in.position() < o.offset + o.sizeCompressed) {
				CompressedData d = (CompressedData) Block.read(in);
				dataWriter.put(d.decompressionOffset, Compression.decompress(d.data));
			}
			
			dataWriter.position(0);
//			Part p;
			parts.add(new Part(dataWriter, o.partKey));
			
//			FileOutputStream fos;
//			try {
////				fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts\\"+Hash.guess(o.partKey, keys, "DEFAULT", "BIN").label));
//				fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\DecompressedParts-ctk\\"+Hash.guess(o.partKey, keys, "DEFAULT", "BIN").label));
////				fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\NIS_240_SX_89\\DecompressedParts\\"+Hash.guess(o.partKey, keys, "DEFAULT", "BIN").label));
//				//omg this takes forever with the 240SX
//				fos.write(p.save(0));
//				fos.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
		}

//		Block b;

//		// dumb method, works as a first approach but needs to be done as 1. readHeader 2. get the parts list then read them according to the header
//		while((in.position() < blockStart+blockLength-4) && in.hasRemaining()) {
//			subBlocks.add(b=Block.read(in));
//			System.out.println("Block read : "+b.getBlockID().getName());
//		}
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		
		// first compresses all parts to get their compressed and decompressed size
		// using RFPK compression but no blocks, hopefully the game recognizes it properly
		System.out.println("Compressing parts...");

		long t = System.currentTimeMillis();
			
		if (USE_MULTITHREADING) {
			ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			System.out.println("Using "+Runtime.getRuntime().availableProcessors()+" CPUs");
			
			for (final var p : parts){
			    pool.execute(new Runnable() {
			        @Override
			        public void run() {
			        	try {
							p.precompress();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        }
			    });
			}
			pool.shutdown();
			// wait for them to finish for up to one minute.
			pool.awaitTermination(10, TimeUnit.MINUTES);
		} else { // useful for debugging
			for (var p : parts) {
				p.precompress();
			}
		}
		

		System.out.println("Parts compressed in "+(System.currentTimeMillis()-t)+" ms.");
		
		
		geomHeader.partsList.refresh(parts);
		geomHeader.partsOffsets.refresh(parts);

		System.out.println("Part lists and offsets refreshed in "+(System.currentTimeMillis()-t)+" ms.");
		
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[16]); //16
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later
		buf.putInt(0);
		buf.putInt(0);

		out.write(buf.array());

		out.write(geomHeader.save(0)); //temporary without offsets
		
//		// dumb method, works as a first approach but needs to be done as WriteHeader then WriteData
//		for (var b : subBlocks) {
//			if (b != null) out.write(b.save());
//		}

		System.out.println("Saving parts to file...");
		for (var p : parts) {
			Padding.makePadding(out);
			geomHeader.partsOffsets.setOffset(p, out.size());
			out.write(new CompressedData(p.compressedData, p.decompressedLength, 0, 0).save(0)); //decompOffset and suppChunkOffset both set to 0 because no chunks
		}
		
		buf = ByteBuffer.wrap(out.toByteArray());
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.position(4);
		buf.putInt(out.size()-8); //size

		buf.position(16);
		buf.put(geomHeader.save(0)); //save the header again, this time with correct offsets
		
		return buf.array();
	}
	
	public static void main(String[] args) {
		
		try {
			long t = System.currentTimeMillis();
			
//			Block.doNotRead.put(GeomBlock.Geom_PartsList, true);
			
			File f;
			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY-ctk.BIN"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\MER_G63AMG\\GEOMETRY.BIN"));
			byte [] arr = new byte[(int)f.length()];
			fis.read(arr);
			fis.close();
			
			System.out.println("File loaded in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

			var geom = new Geometry(ByteBuffer.wrap(arr));
			
			System.out.println("Geom read in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY-recompiled.BIN"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY-ctk recompiled.BIN"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\NIS_240_SX_89\\GEOMETRY-recompiled.BIN"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\MER_G63AMG\\GEOMETRY-recompiled.BIN"));
			fos.write(geom.save(0));
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
