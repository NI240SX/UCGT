package fr.ni240sx.ucgt.geometryFile.textures;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.compression.Compression;

public class NFSTexture {

	public int binKey;
	
	int const11=0;
	int imageOffset=0;
	int const14 = -1;
	
	public int imageDataLength, firstMipMapDataLength;
	public int width, height;

	int var24 = Integer.reverseBytes(0x08072200);
//	int const31 = 65536; //colors?
	int mipMapCount = 1;
	int const32 = 0;
	int const33;
	
	short const43 = 256, const44 = 256;
	
	public String name;
	public String format;
	
	public byte[] DDSImage;

	public int decompressedLength;
	public int compressedLength;
	public byte[] compressedData;

	public NFSTexture() {
		
	}
	
	public NFSTexture(ByteBuffer in, int version) throws Exception {
		if (version == 9) {
			if (in.get(136) != 0x44
					&& in.get(137) != 0x58
					&& in.get(138) != 0x54	
					&& in.get(in.limit()-20) == 0x44
					&& in.get(in.limit()-19) == 0x58
					&& in.get(in.limit()-18) == 0x54					
					) { //nfs world, why is it using the fucking version 9
				var fileStart = in.position();
				binKey = in.getInt(in.limit()-200);
				width = in.getInt(in.limit()-180);
				height = in.getInt(in.limit()-176);
				firstMipMapDataLength = in.getInt(in.limit()-188);
				imageDataLength = in.getInt(in.limit()-188);
				in.position(in.limit()-67);
				name = Block.readStringAligned(in);
				Hash.findBIN(name);			
				in.position(in.limit()-32);
				readHeaderPart2(in);
				in.position(fileStart);
			} else {
				//header length 256
				var headerStart = in.position();
				
				readHeaderPart1(in);
				in.position(headerStart+124);
				readHeaderPart2(in);
				in.position(headerStart+256);	
			}

		} else if (version == 8) {
			//header length 148
			var fileStart = in.position();
		
			in.position(in.limit()-148);
			readHeaderPart1(in);						
			in.position(in.limit()-24);
			readHeaderPart2(in);
			in.position(fileStart);
		} else if (version == 5) {
			//header length 148
			var fileStart = in.position();
		
			in.position(in.limit()-144);
			name = Block.readStringAligned(in);
			Hash.findBIN(name);
			binKey = in.getInt(in.limit()-120);
			const11 = in.getInt(in.limit()-116);
			imageDataLength = in.getInt(in.limit()-100);
			firstMipMapDataLength = in.getInt(in.limit()-80);
			width = Short.toUnsignedInt(in.getShort(in.limit()-88));
			height = Short.toUnsignedInt(in.getShort(in.limit()-86));

			in.position(in.limit()-12);
			format = Block.readStringAligned(in); 

			in.position(fileStart);
			
		}
		
		readImage(in);
	}

	public void readImage(ByteBuffer in) throws Exception {
		if (in.position()+imageDataLength > in.capacity() || imageDataLength < 0) {
			throw new Exception("Cannot read image! Invalid header.");
		}
		DDSImage = new byte[imageDataLength+128];
		var writer = ByteBuffer.wrap(DDSImage);
		writer.order(ByteOrder.LITTLE_ENDIAN);
		
		writer.putInt(542327876); //DDS 
		writer.putInt(124);
		writer.putInt(0x000A1007); //TODO flags
		writer.putInt(height);
		writer.putInt(width);
		writer.putInt(this.firstMipMapDataLength); //linearSize
		writer.putInt(1);//depth
		writer.putInt(mipMapCount);// mipmap count
		writer.position(writer.position()+44);
		writer.putInt(32);//pixel format size
		writer.putInt(4); //flags
		Block.putString(writer, format);
		writer.position(writer.position()+16); //not 20 because Block.putString overshoots
		writer.putInt(4096);//idk
		writer.position(writer.position()+16);
		
//		writer.put(in);
//		System.out.println("Reading image "+this.name+" at "+in.position()+" (expected length "+imageDataLength+")");
//		try {
		writer.put(writer.position(), in, in.position(), imageDataLength);
		in.position(in.position()+imageDataLength);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public void readHeaderPart2(ByteBuffer in) {
		//same for v9 and v8, i think
		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		format = Block.readStringAligned(in); // should be always 4 bytes!
		in.getInt(); //0
	}

	public void readHeaderPart1(ByteBuffer in) {
		//same for v9 and v8
		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		binKey = in.getInt();
		
		const11 = in.getInt(); //0x001A93CF most cases, 0x1B81E7B0 wheel misc engine & brakelight damage0, 0x6B6D3253 badging, 0x1DA6C8A6 normalmaps
		imageOffset = in.getInt();
		const14 = in.getInt();
		imageDataLength = in.getInt();
		
		
		in.getInt(); //0
		firstMipMapDataLength = in.getInt(); //imageDataLength again TODO -> pixel count for dxt5
		width = Short.toUnsignedInt(in.getShort());
		height = Short.toUnsignedInt(in.getShort());
		var24 = in.getInt(); //06052400, TODO
		
//		const31 = in.getInt();
		in.getShort();
		mipMapCount = Byte.toUnsignedInt(in.get());
		in.get(); //flag
		const32 = in.getInt(); //0 opaque, 0x00000500 transparent, 0x00000700 badging
		const33 = in.getInt(); //0x00010200 if emissive, 0x00010201 if transparent, 0x01000000 if opaque, 0x01000200 misc and engine
		in.getInt(); //0

		in.getInt(); //0
		in.getInt(); //0
		const43 = in.getShort();
		const44 = in.getShort();

		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		
		in.get(); //name length, # = 35
		name = Block.readStringAligned(in);
		Hash.findBIN(name);
	}

	public byte[] save(int version) {
		
		ByteBuffer out;
		
		int headerLength = switch(version) {
		case 9:
			yield 256;
		default:
			yield 148;
		};
		out = ByteBuffer.wrap(new byte[imageDataLength + headerLength]);
		out.order(ByteOrder.LITTLE_ENDIAN);
		
		if (version == 9) {
			//header length 256
			var headerStart = out.position();
			
			writeHeaderPart1(out, (byte) 35);					
			out.position(headerStart+124); //necessary, otherwise misaligns the DDS header
			writeHeaderPart2(out);
			
			out.position(headerStart+256);

		} else {//if (version == 8) {
			//header length 148
			var fileStart = out.position();
			out.position(out.capacity()-148);
			
			writeHeaderPart1(out, (byte) 35);
			out.position(out.capacity()-24);
			writeHeaderPart2(out);
						
			out.position(fileStart);
		}
		
		var reader = ByteBuffer.wrap(DDSImage);
		reader.order(ByteOrder.LITTLE_ENDIAN);
		reader.position(reader.position()+128); //skip dds header
		
//		writer.put(in);
		out.put(reader);

		return out.array();
	}

	public byte[] writeImageData() {
		
		var out = ByteBuffer.wrap(new byte[imageDataLength]);		
		out.order(ByteOrder.LITTLE_ENDIAN);
		
		var reader = ByteBuffer.wrap(DDSImage);
		reader.order(ByteOrder.LITTLE_ENDIAN);
		reader.position(reader.position()+128); //skip dds header
		
//		writer.put(in);
		out.put(reader);

		return out.array();
	}

	public void writeHeaderPart2(ByteBuffer out) {
		out.putInt(0);
		out.putInt(0);
		out.putInt(0);
		Block.putString(out, format);
		out.putInt(0);
	}

	public byte[] writeHeaderPart2() {
		ByteBuffer out = ByteBuffer.wrap(new byte[24]);
		out.order(ByteOrder.LITTLE_ENDIAN);
		writeHeaderPart2(out);
		return out.array();
	}
	
	public void writeHeaderPart1(ByteBuffer out, byte nameLength) {
		out.putInt(0);
		out.putInt(0);
		out.putInt(0);
		out.putInt(binKey);
		
		out.putInt(const11);
		out.putInt(imageOffset);
		out.putInt(const14);
		out.putInt(imageDataLength);

		out.putInt(0);
		out.putInt(firstMipMapDataLength); //TODO
		out.putShort((short)width);
		out.putShort((short)height);
		out.putInt(var24); //TODO
		
//		out.putInt(const31);
		out.putShort((short) 0);
		out.put((byte) mipMapCount);
		out.put((byte) 0);
		out.putInt(const32); //transparency ? 1280 : 0
		out.putInt(const33); //transparency ? 01020100 : 00000001
		out.putInt(0);

		out.putInt(0);
		out.putInt(0);
		out.putShort(const43);
		out.putShort(const44);

		out.putInt(0);
		out.putInt(0);
		out.putInt(0);

		out.put(nameLength);
		Block.putString(out, name, nameLength);
	}
	
	public byte[] writeHeaderPart1() {
		int stringLength = Block.stringLengthAligned("#"+name);
		ByteBuffer out = ByteBuffer.wrap(new byte[88+stringLength]);
		out.order(ByteOrder.LITTLE_ENDIAN);
		writeHeaderPart1(out, (byte) (stringLength-1));
		return out.array();
	}

	public NFSTexture(File dds, String carname) throws IOException {
		var fis = new FileInputStream(dds);
		this.DDSImage = new byte[(int) dds.length()];
		fis.read(DDSImage);
		fis.close();
		
		this.name = dds.getName().replace(".dds", "");
		if (carname != null) name = name.replace("%", carname);
		this.binKey = Hash.findBIN(name);
		this.height = getIntAt(DDSImage, 12);
		this.width = getIntAt(DDSImage, 16);
		this.mipMapCount = getIntAt(DDSImage, 28);
		this.firstMipMapDataLength = getIntAt(DDSImage, 20);
		this.imageDataLength = 0;
		for (int i=0; i < mipMapCount; i++) {
			this.imageDataLength += this.firstMipMapDataLength/Math.pow(4, i);
		}
		this.format = String.valueOf(new char[] {(char) DDSImage[84], (char) DDSImage[85], (char) DDSImage[86], (char) DDSImage[87]});
		this.const32 = format.equals("DXT1") ? 0 : 1280;
		this.const33 = format.equals("DXT1") ? 0x01000000 : 0x00010201;
	}
	
	@SuppressWarnings("unused")
	private void setMinMipMapSize(int size) { //NO, BREAKS THE RENDERING OF HIGHER MIPMAPS
		var minHeight = this.height / Math.pow(2, this.mipMapCount-1);
		var minWidth= this.width/ Math.pow(2, this.mipMapCount-1);

		while(minHeight < size && minWidth < size && this.mipMapCount > 1) {

//			System.out.println("Removing mip map "+(this.mipMapCount)+" on "+this.name);
			
			this.imageDataLength -= this.firstMipMapDataLength/Math.pow(4, this.mipMapCount-1);
			this.mipMapCount--;

			this.DDSImage = Arrays.copyOf(DDSImage, 128+imageDataLength);
			//should update mipmaps in the dds data but this shouldn't get exported
			
			minHeight = this.height / Math.pow(2, this.mipMapCount-1);
			minWidth= this.width/ Math.pow(2, this.mipMapCount-1);
		}
	}
	
	public NFSTexture(NFSTexture copyFrom) {		
		binKey = copyFrom.binKey;
		
		const11 = copyFrom.const11;
		imageOffset = copyFrom.imageOffset;
		const14 = copyFrom.const14;
		imageDataLength = copyFrom.imageDataLength;

		firstMipMapDataLength = copyFrom.firstMipMapDataLength;
		width = copyFrom.width;
		height = copyFrom.height;
		var24 = copyFrom.var24;

		mipMapCount = copyFrom.mipMapCount;
		const32 = copyFrom.const32;
		const33 = copyFrom.const33;

		const43 = copyFrom.const43;
		const44 = copyFrom.const44;

		name = new String(copyFrom.name);
		format = new String(copyFrom.format);
		
		DDSImage = copyFrom.DDSImage.clone();
	}

	public NFSTexture removeNHighMipMaps(int count) {
		var tex = new NFSTexture(this);
		for (int i=0; i<count; i++) {
			if (tex.mipMapCount == 1) break;
			
//			System.out.println("Removing mip map "+(i+1)+" on "+tex.name);
			
			var header = ByteBuffer.wrap(Arrays.copyOf(tex.DDSImage, 128));
			header.order(ByteOrder.LITTLE_ENDIAN);
			header.putInt(12, tex.height/2); //height
			header.putInt(16, tex.width/2); //width
			header.putInt(28, tex.mipMapCount-1); //mipmap count
			header.putInt(20, tex.firstMipMapDataLength/4); //first mipmap data length
			
			var newImage = Arrays.copyOfRange(tex.DDSImage, 128+tex.firstMipMapDataLength, tex.DDSImage.length);
			
			tex.height /= 2;
			tex.width /= 2;
			tex.mipMapCount -= 1;
			tex.imageDataLength -= tex.firstMipMapDataLength;
			tex.firstMipMapDataLength /= 4;
			
			tex.DDSImage = new byte[tex.imageDataLength+128];
			var dds = ByteBuffer.wrap(tex.DDSImage);
			dds.order(ByteOrder.LITTLE_ENDIAN);
			dds.put(header);
			dds.put(newImage);
		}
		
		return tex;
	}
	
	public NFSTexture removeHighMipMapsUntilSize(int pixels) {
		var tex = new NFSTexture(this);
		while (true) {
			if (tex.height <= pixels || tex.width <= pixels) break;
			if (tex.mipMapCount == 1) break;
			
//			System.out.println("Removing mip map "+(i+1)+" on "+tex.name);
			
			var header = ByteBuffer.wrap(Arrays.copyOf(tex.DDSImage, 128));
			header.order(ByteOrder.LITTLE_ENDIAN);
			header.putInt(12, tex.height/2); //height
			header.putInt(16, tex.width/2); //width
			header.putInt(28, tex.mipMapCount-1); //mipmap count
			header.putInt(20, tex.firstMipMapDataLength/4); //first mipmap data length
			
			var newImage = Arrays.copyOfRange(tex.DDSImage, 128+tex.firstMipMapDataLength, tex.DDSImage.length);
			
			tex.height /= 2;
			tex.width /= 2;
			tex.mipMapCount -= 1;
			tex.imageDataLength -= tex.firstMipMapDataLength;
			tex.firstMipMapDataLength /= 4;
			
			tex.DDSImage = new byte[tex.imageDataLength+128];
			var dds = ByteBuffer.wrap(tex.DDSImage);
			dds.order(ByteOrder.LITTLE_ENDIAN);
			dds.put(header);
			dds.put(newImage);
		}
		
		return tex;
	}

	public static int getIntAt(byte [] array, int i) {
		return (Byte.toUnsignedInt(array[i]) | Byte.toUnsignedInt(array[i+1]) << 8 | Byte.toUnsignedInt(array[i+2]) << 16 | Byte.toUnsignedInt(array[i+3]) << 24);
	}

	
	public void precompress(TPK tpk, int version) {
		
		var partBytes = this.save(version);
		decompressedLength = partBytes.length;
		
		compressedData  = Compression.compress(partBytes, tpk.defaultCompressionType, tpk.defaultCompressionLevel);
		compressedLength = compressedData.length + 24;
	}

	@Override
	public String toString() {
		var h = Hash.getBIN(binKey);
		return h.startsWith("0x") ? h + " (" + this.name + "~)" : h;
	}
}
