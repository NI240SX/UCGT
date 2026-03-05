package fr.ni240sx.ucgt.geometryFile.textures;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.compression.Compression;
import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.compression.CompressionType;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.geometry.CompressedData;
import fr.ni240sx.ucgt.geometryFile.geometry.Padding;
import fr.ni240sx.ucgt.geometryFile.gui.CustomProgressBar;

public class TPK extends Block {

	public TPKHeader header;
	public ArrayList<NFSTexture> textures = new ArrayList<>();
	
	/**
	 * version 9: UC
	 * version 8: Prostreet/Carbon/World
	 */
	public int version;
	boolean SAVE_useOffsetsTable = true;
	public CompressionType defaultCompressionType = null;
	public CompressionLevel defaultCompressionLevel = CompressionLevel.High;
	
	@Override
	public BlockType getBlockID() {
		return BlockType.TPK;
	}
	
	/*
	 * ------------------------------------------------------------------------------------------------------------------------
	 * 
	 *                                                   LOAD / SAVE
	 * 
	 * ------------------------------------------------------------------------------------------------------------------------
	 */
	
	public TPK(ByteBuffer in) throws Exception {
		in.order(ByteOrder.LITTLE_ENDIAN);
		//var blockLength = 
				in.getInt();
		var blockStart = in.position();		
		
		Block.read(in); //padding
		in.getInt();
		header = new TPKHeader(in, this);
		
		if (header.offsets == null) {
			SAVE_useOffsetsTable = false;
			//texture packs in stream, etc
			//skip padding
			while (BlockType.get(in.getInt()) != BlockType.Tex_Container) {
				var size = in.getInt();
				in.position(in.position()+size);
			}
			in.getInt();//container size
			in.getInt();//other block id
			var size = in.getInt();
			in.position(in.position()+size);
			in.getInt();//padding
			size = in.getInt();
			in.position(in.position()+size);
			in.getInt(); //final textures container
			in.getInt(); //size
			while (in.getInt() == 0x11111111) {}
			in.position(in.position()-4);
			texloop: for (var t : textures) {
				t.readImage(in);

				try {
//					while (in.getInt() == 0) {}
		            var pad = 0x80 - (in.position()-blockStart+8) % 0x80;
		            if (pad != 0x80) in.position(in.position()+pad);
				} catch (@SuppressWarnings("unused") Exception e) {
					break texloop;
				}
//				in.position(in.position()-4);
			}

			
		} else {
			SAVE_useOffsetsTable = true;
			offsets: for (var o : header.offsets.offsets) { //.values()
				ByteBuffer dataWriter = null;
				try {
					if (o.isCompressed != 0) {
//						defaultCompressionType = CompressionType.RefPack;
						byte[] partData = new byte[o.sizeDecompressed];
						dataWriter = ByteBuffer.wrap(partData);
						dataWriter.order(ByteOrder.LITTLE_ENDIAN);
						
						in.position(o.offset -4); //-4 because we don't have the full file header here
						//loops on the one or multiple compressed blocks
						if (version >= 8) while (in.position() < o.offset -4 + o.sizeCompressed) {
							CompressedData d = (CompressedData) Block.read(in);
							if (defaultCompressionType == null) defaultCompressionType = CompressionType.get(String.valueOf(new char[] {(char)(d.data[0]), (char)(d.data[1]), (char)(d.data[2]), (char)(d.data[3])}));
							var decomp = Compression.decompress(d.data);
							if (decomp==null) continue offsets;
							dataWriter.put(d.decompressionOffset, decomp);
						} else {
							var raw = new byte[o.sizeCompressed];
							in.get(raw);
							dataWriter.put(Compression.decompress(raw));
						}
						
						dataWriter.position(0);
						
						textures.add(new NFSTexture(dataWriter, version));
					} else {
						if (defaultCompressionType == null) defaultCompressionType = CompressionType.RawDecompressed;
						in.position(o.offset -4);
						var limit = in.capacity();
						in.limit(in.position() + o.sizeDecompressed);
						textures.add(new NFSTexture(in, version));	
						in.limit(limit);
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (dataWriter != null) {
						try {
							File f;
							FileOutputStream fos = null;
							try {
								fos = new FileOutputStream(f = new File(header.info.filename + "-"+ String.format("0x%08X",o.partKey)));
							} catch (@SuppressWarnings("unused") Exception e2) {
								if (fos != null) fos.close();
								fos = new FileOutputStream(f = new File("UCGT DEBUG DUMP"));
							}
							fos.write(dataWriter.array());
							fos.close();
							System.out.println("TPK: Dumped raw decompressed data in "+f.getAbsolutePath());
						} catch (@SuppressWarnings("unused") Exception e2) {}
					}
				}
			}
		}
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		return save(currentPosition, null);
	}

	public void save(File file) throws IOException, InterruptedException {
		save(file, null);
	}
	public void save(File f, CustomProgressBar prog) throws IOException, InterruptedException {
		var save = save(0, prog);
		var fos = new FileOutputStream(f);
		fos.write(save);
		fos.close();		
	}
	public byte[] save(int currentPosition, CustomProgressBar prog) throws IOException, InterruptedException {
		
		var progress = new AtomicInteger(0);
		if (SAVE_useOffsetsTable && defaultCompressionType != CompressionType.RawDecompressed) {
			if (prog != null) 
			javafx.application.Platform.runLater(() -> {
				prog.text.setText("Compressing textures...");
				prog.progress.setProgress(0);
			});
			System.out.println("Compressing textures...");
				ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
				System.out.println("Using "+Runtime.getRuntime().availableProcessors()+" CPUs");
				
				for (final var t : textures){
				    pool.execute(() -> {
						try {
							if (prog != null) {
								javafx.application.Platform.runLater(() -> {
									prog.progress.setProgress(((double)progress.incrementAndGet())/textures.size());
									prog.text.setText("Compressing textures "+Math.round(100*((float)(progress.get())/textures.size()))+"%...");
								});
							} else
								System.out.print("\rProgress " + Math.round(100*((float)(progress.incrementAndGet())/textures.size()))+ " %" );
							t.precompress(this, this.version);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}
				pool.shutdown();
				// wait for them to finish for up to one minute.
				pool.awaitTermination(10, TimeUnit.MINUTES);
		}

		if (prog != null) 
			javafx.application.Platform.runLater(() -> {
				prog.text.setText("Refreshing header...");
				prog.progress.setProgress(-1);
			});

		this.header.refresh(this);

		if (prog != null) 
			javafx.application.Platform.runLater(() -> {
				prog.text.setText("Writing file...");
				prog.progress.setProgress(-1);
			});
		
		
		
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]); //16
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		/*
		 * padding 48 -> modulo <=64
		 * header
		 * padding 124 -> modulo <=128
		 * container 000032B3 with the rest of the fucking file
		 * 		010003233 length 24 - nothing nothin 1 unknownhash nothin nothin
		 * 		padding 80 -> modulo <=1024
		 * 		02003233 length the rest of the fucking file - 1111 padding 120 -> modulo <= 128
		 * 			first texture offset as CompressedData
		 * 			when finished, filled with zeroes, no proper padding block
		 * 			next offset modulo <= 128
		 */
		
		out.write(buf.array());
		out.write(Padding.makePadding(currentPosition+out.size(), 64)); //TODO does it require CurrentPosition or not
		var headerPos = out.size();		
		out.write(header.save(0)); //temporary without offsets
		out.write(Padding.makePadding(currentPosition+out.size(), 128));
		

		buf = ByteBuffer.wrap(new byte[8]); //16
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(BlockType.Tex_Container.getKey());
		buf.putInt(-1); //length for later
		out.write(buf.array());
		var texContainerAt = out.size();

		buf = ByteBuffer.wrap(new byte[8+24]); //16
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(BlockType.Tex_Header.getKey());
		buf.putInt(24); //length for later
		buf.putInt(0);
		buf.putInt(0);
		buf.putInt(1);
		buf.putInt(header.info.unknownHash);
		buf.putInt(0);
		buf.putInt(0);
		out.write(buf.array());
		//tex Header at texContainer+8

		out.write(Padding.makePadding(currentPosition+out.size(), 128));

		buf = ByteBuffer.wrap(new byte[8]); //16
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(BlockType.Tex_PackedData.getKey());
		buf.putInt(-1); //length for later
		out.write(buf.array());
		var texDataAt = out.size();
		
		Block.makeAlignment(out, Block.findAlignment(currentPosition+out.size(), 128), (byte) 0x11);
		
		if (SAVE_useOffsetsTable) for (var t : textures) {
			header.offsets.setOffset(t, out.size());
			if (this.defaultCompressionType != CompressionType.RawDecompressed)	{
				out.write(new CompressedData(t.compressedData, t.decompressedLength, 0, 0).save(0)); //decompOffset and suppChunkOffset both set to 0 because no chunks
			} else {
				var length = out.size();
				out.write(t.save(this.version));
				header.offsets.setLengths(t, out.size()-length);
			}
			Block.makeAlignment(out, Block.findAlignment(currentPosition+out.size(), 128), (byte) 0x00);
		} else for (var t : textures) {
			out.write(t.writeImageData());
			Block.makeAlignment(out, Block.findAlignment(currentPosition+out.size(), 128), (byte) 0x00);
		}
	
		buf = ByteBuffer.wrap(out.toByteArray());
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(4, out.size()-8); //size

		buf.putInt(texContainerAt-4, out.size()-texContainerAt);
		buf.putInt(texDataAt-4, out.size()-texDataAt);

		buf.position(headerPos);
		if (SAVE_useOffsetsTable) buf.put(header.save(0)); //save the header again, this time with correct offsets

		return buf.array();
		
		
		
//		var arr = new byte [data.length+8];
//		
//		buf = ByteBuffer.wrap(arr);
//		buf.order(ByteOrder.LITTLE_ENDIAN);
//		buf.putInt(getBlockID().getKey()); 
//		buf.putInt(data.length); //length for later
//		buf.put(data);
//		
//		return buf.array();
	}

	/*
	 * ------------------------------------------------------------------------------------------------------------------------
	 * 
	 *                                                 IMPORT / EXPORT
	 * 
	 * ------------------------------------------------------------------------------------------------------------------------
	 */

	public void exportToFolder(String folder, String carname) throws Exception {
		exportToFolder(new File(folder), carname);
	}
	public void exportToFolder(File folder, String carname) throws Exception {
		if (!folder.exists()) folder.mkdirs();
		if (!folder.isDirectory()) throw new Exception("Not a directory!");
		if (carname == null) carname = Geometry.UNDETERMINED_CARNAME;
		for (var t : textures) {
			File dds;
			if (t.binKey == Hash.findBIN(t.name)) dds = new File(folder.getAbsolutePath() + File.separator + t.name.replace(carname, "%") + ".dds");
			else if (!Hash.getBIN(t.binKey).startsWith("0x")) dds = new File(folder.getAbsolutePath() + File.separator + Hash.getBIN(t.binKey).replace(carname, "%") + ".dds");
			else dds = new File(folder.getAbsolutePath() + File.separator + Hash.getBIN(t.binKey) + " (" +t.name + "~).dds");
			var fos = new FileOutputStream(dds);
			fos.write(t.DDSImage);
			fos.close();
		}
	}
	
	public TPK() {
	}
	
	public TPK(File folder, Geometry geom) throws Exception {
		this(folder, switch(geom.platform) {
		case PC:
		case X360:
			yield 9;
		default:
			yield 8;
		}, geom.defaultCompressionType, geom.defaultCompressionLevel, switch(geom.geomHeader.geomInfo.filenameType()) {
		default:
		case 0: //vanilla
			yield "CarTemplateTextures_"+geom.carname+".tpk";
		case 1: //compiled
		case 2: //custom
			yield geom.geomHeader.geomInfo.filename;
		}, switch(geom.geomHeader.geomInfo.filenameType()) {
		default:
		case 0: //vanilla
			yield "CARTEMPLATETEXTURES_"+geom.carname;
		case 1: //compiled
		case 2: //custom
			yield geom.geomHeader.geomInfo.blockname;
		}, 0, geom.carname);
	}
	public TPK(File folder, int version, CompressionType compType, CompressionLevel compLevel, String filename, String blockname, int unknownHash, String carname) throws Exception {
		if (!folder.isDirectory()) throw new Exception("Not a directory!");
		this.version = version;
		this.defaultCompressionType = compType;
		this.defaultCompressionLevel = compLevel;
		for (var f : folder.listFiles()) if (f.getName().endsWith(".dds")) {
			this.textures.add(new NFSTexture(f, carname));
		}
		this.textures.sort(new TextureKeySorter());
		int length = 0;
		for (var t : this.textures){
            t.imageOffset = length;
            length += t.imageDataLength;
            var pad = 0x80 - length % 0x80;
            if (pad != 0x80) length += pad;
		}
		this.header = new TPKHeader(this, filename, blockname, unknownHash);
	}
	
	public TPK(File folder, int version) throws Exception {
		if (!folder.isDirectory()) throw new Exception("Not a directory!");
		this.version = version;
		this.SAVE_useOffsetsTable = false;
		for (var f : folder.listFiles()) if (f.getName().endsWith(".dds")) {
			this.textures.add(new NFSTexture(f, null));
		}
		this.textures.sort(new TextureKeySorter());
		int length = 0;
		for (var t : this.textures){
            t.imageOffset = length;
            length += t.imageDataLength;
            var pad = 0x80 - length % 0x80;
            if (pad != 0x80) length += pad;
		}
		this.header = new TPKHeader(this, folder.getName().split("-")[1], folder.getName().split("-")[0], 0);
	}
	
	public TPK removeNHighMipMaps(int count) {
		var tpk = new TPK();
		tpk.version = this.version;
		tpk.defaultCompressionType = this.defaultCompressionType;
		tpk.defaultCompressionLevel = this.defaultCompressionLevel;
		tpk.SAVE_useOffsetsTable = this.SAVE_useOffsetsTable;
		for (var t : this.textures) {
			tpk.textures.add(t.removeNHighMipMaps(count));
		}
		tpk.textures.sort(new TextureKeySorter());
		int length = 0;
		for (var t : tpk.textures){
            t.imageOffset = length;
            length += t.imageDataLength;
            var pad = 0x80 - length % 0x80;
            if (pad != 0x80) length += pad;
		}
		tpk.header = new TPKHeader(tpk, this.header.info.filename, this.header.info.blockname, this.header.info.unknownHash);
		return tpk;
	}

	public TPK removeHighMipMapsUntilSize(int pixels) {
		var tpk = new TPK();
		tpk.version = this.version;
		tpk.defaultCompressionType = this.defaultCompressionType;
		tpk.defaultCompressionLevel = this.defaultCompressionLevel;
		tpk.SAVE_useOffsetsTable = this.SAVE_useOffsetsTable;
		for (var t : this.textures) {
			tpk.textures.add(t.removeHighMipMapsUntilSize(pixels));
		}
		tpk.textures.sort(new TextureKeySorter());
		int length = 0;
		for (var t : tpk.textures){
            t.imageOffset = length;
            length += t.imageDataLength;
            var pad = 0x80 - length % 0x80;
            if (pad != 0x80) length += pad;
		}
		tpk.header = new TPKHeader(tpk, this.header.info.filename, this.header.info.blockname, this.header.info.unknownHash);
		return tpk;
	}
}

class TextureKeySorter implements Comparator<NFSTexture>{
	@Override
	public int compare(NFSTexture o1, NFSTexture o2) {
		return Integer.compareUnsigned(o1.binKey, o2.binKey);
	}
}