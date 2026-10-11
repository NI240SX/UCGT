package fr.ni240sx.ucgt.geometryFile.textures;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import fr.ni240sx.ucgt.compression.Compression;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.Hash;

public class NFSTexture {

	public int binKey;
	
	int ClassKey=0;
	int imageOffset=0;
	int paletteOffset = -1;
	
	public int imageDataLength, firstMipMapDataLength;
	public int width, height;

//	int var24 = Integer.reverseBytes(0x08072200);
//	int const31 = 65536; //colors?
	int mipMapCount = 1;
//	int const32 = 0;
//	int const33;
	
	short scaleS = 256, scaleT = 256;
	
	public String name;
//	public String format;
	private int format = 0;
	public CompressionType compression;
	
	public byte[] DDSImage;

	public int decompressedLength;
	public int compressedLength;
	public byte[] compressedData;

	static final int DXT1 = 827611204;
	

	public byte biasLevel = 0;
	public byte renderingOrder = 0;
	public byte scrollType = 0;
	public byte used = 0;
	public byte applyAlphaSort = 0;
	public byte alphaUsageType = 0;
	public byte alphaBlendType = 0;
	public byte flags = 0;
	
	public NFSTexture() {}
	
	public NFSTexture(ByteBuffer in, int version) throws Exception {
		var isXenon = false;
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
				if (readHeaderPart2(in)) {
					//xbox
					in.position(headerStart+4096); //why all that space lost
					isXenon = true;
				} else {
					//pc
					in.position(headerStart+256);	
				}
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
			ClassKey = in.getInt(in.limit()-116);
			imageDataLength = in.getInt(in.limit()-100);
			firstMipMapDataLength = in.getInt(in.limit()-80);
			width = Short.toUnsignedInt(in.getShort(in.limit()-88));
			height = Short.toUnsignedInt(in.getShort(in.limit()-86));

			in.position(in.limit()-12);
//			format = Block.readStringAligned(in); 
			format = in.getInt();

			in.position(fileStart);
			
		}
		
		readImage(in, isXenon);
		
//		throw new RuntimeException("used to export raw texture data because i cba");
	}

	public void readImage(ByteBuffer in, boolean isXenon) throws Exception {
		if (in.position()+imageDataLength > in.capacity() || imageDataLength < 0) {
			throw new Exception("Cannot read image! Invalid header.");
		}
		DDSImage = new byte[imageDataLength+128];
		var writer = ByteBuffer.wrap(DDSImage);
		writer.order(ByteOrder.LITTLE_ENDIAN);
		
		writer.putInt(542327876); //DDS 
		writer.putInt(124);
		var ddsflags = 0x00001007 | 0x00020000; // TEXTURE | MIPMAP
		switch (compression) {
		case DXT1:
		case DXT3:
		case DXT5:
		case DXTN:
			ddsflags |= 0x00080000; //LINEARSIZE
			break;
		default:
			ddsflags |= 0x00000008; //PITCH
			break;
		}
		
		//writer.putInt(0x000A1007);
		writer.putInt(ddsflags);
		writer.putInt(height);
		writer.putInt(width);
		
		switch (compression) {
		case DXT1: //linear size = width*height/2
		case DXT3: //linear size = width*height
		case DXT5: //linear size = width*height
		case DXTN:
			writer.putInt(this.firstMipMapDataLength); //linearSize
			break;
		default:
			writer.putInt((this.width * compression.bitCount + 7) / 8); //pitch
			break;
		}
		writer.putInt(1);//depth
		writer.putInt(mipMapCount);// mipmap count
		writer.position(writer.position()+44);
		writer.putInt(32);//pixel format size
		
//		System.out.println("Texture %s : compression type %s, format %08X".formatted(name, compression.name(), format));

		switch (compression) {
		/* PIXEL FORMAT
		 * size
		 * flags
		 * fourcc
		 * rgb bit count
		 * r bitmask
		 * g bitmask
		 * b bitmask
		 * a bitmask
		 */
		case DXT1:
		case DXT3:
		case DXT5:
		case DXTN:
			writer.putInt(4); //FOURCC
			writer.putInt(format); 
			writer.position(writer.position()+20);
			break;
		case TC_24Bit:
		case TC_32Bit:
			writer.putInt(0x41);
			writer.putInt(0);
			writer.putInt(32);
			writer.putInt(0x00FF0000);
			writer.putInt(0x0000FF00);
			writer.putInt(0x000000FF);
			writer.putInt(0xFF000000);
			break;
		default:
			System.err.println("Texture %s : compression type %s not supported!".formatted(name, compression.name()));
			writer.putInt(0); //unknown
			writer.putInt(0); 
			writer.position(writer.position()+20);
			break;
		}
		writer.putInt(0x00001000 | 0x00400008);//surface flags TEXTURE | MIPMAP
		writer.position(writer.position()+16);
		
//		writer.put(in);
//		System.out.println("Reading image "+this.name+" at "+in.position()+" (expected length "+imageDataLength+")");
//		try {
		var start = in.position();
		if (isXenon) {
			//endian swapper			
			var endianSwapped = ByteBuffer.allocate(imageDataLength);
			in.order(ByteOrder.BIG_ENDIAN);
			endianSwapped.order(ByteOrder.LITTLE_ENDIAN);
			
			int blockPixelSize = 4;
            int texelBytePitch = 8;

			switch(compression) {
			case DXT1:
				while(in.position() < start+imageDataLength && in.hasRemaining()) {
					endianSwapped.putShort(in.getShort());
				}
				endianSwapped.position(0);
				blockPixelSize = 4;
	            texelBytePitch = 8;
	            unswizzle(writer, endianSwapped, blockPixelSize, texelBytePitch);
	            break;
			case DXT3:
			case DXT5:
			case DXTN:
				while(in.position() < start+imageDataLength && in.hasRemaining()) {
					endianSwapped.putShort(in.getShort());
				}
				endianSwapped.position(0);
	            blockPixelSize = 4;
	            texelBytePitch = 16;
	            unswizzle(writer, endianSwapped, blockPixelSize, texelBytePitch);
	            break;
			case TC_32Bit:
				while(in.position() < start+imageDataLength && in.hasRemaining()) {
					endianSwapped.putInt(in.getInt());
				}
				endianSwapped.position(0);
	            blockPixelSize = 1;
	            texelBytePitch = 4;
	            unswizzle(writer, endianSwapped, blockPixelSize, texelBytePitch);
	            break;
            default:
            	in.position(start);
    			while(in.position() < start+imageDataLength && in.hasRemaining()) {
    				writer.putInt(in.getInt());
    			}
            	break;
				
			}
			
			
		} else {
			writer.put(writer.position(), in, in.position(), imageDataLength);
		}
		in.position(start+imageDataLength);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public void unswizzle(ByteBuffer writer, ByteBuffer endianSwapped, int blockPixelSize, int texelBytePitch) {
		var heightInBlocks = height/blockPixelSize;
		var widthInBlocks = width/blockPixelSize;
		
		//unswizzler https://reshax.com/topic/846-unswizzling-xbox-360-textures-condemned-2-lithtech-jupiter-ex/
		for (int j = 0; j < heightInBlocks; j++) {
		    for (int i = 0; i < widthInBlocks; i++) {
		        var blockOffset = j * widthInBlocks + i;
		        var x = XGAddress2DTiledX(blockOffset, widthInBlocks, texelBytePitch);
		        var y = XGAddress2DTiledY(blockOffset, widthInBlocks, texelBytePitch);
		        var srcByteOffset = j * widthInBlocks * texelBytePitch + i * texelBytePitch;
		        var destByteOffset = y * widthInBlocks * texelBytePitch + x * texelBytePitch;

		        if (destByteOffset + texelBytePitch + 128 > writer.capacity()) continue;
		        writer.put(128 + destByteOffset, endianSwapped.array(), srcByteOffset, texelBytePitch);
//	                memcpy(&destData[destByteOffset], &data[srcByteOffset], texelBytePitch);
		    }
		}
	}

	
	//xbox 360 unswizzler, sourced from https://github.com/NCDyson/RareView/blob/master/RareView/Texture.cs
	private static int XGAddress2DTiledX(int Offset, int Width, int TexelPitch) {
        int AlignedWidth = (Width + 31) & ~31;

        int LogBpp = (TexelPitch >> 2) + ((TexelPitch >> 1) >> (TexelPitch >> 2));
        int OffsetB = Offset << LogBpp;
        int OffsetT = ((OffsetB & ~4095) >> 3) + ((OffsetB & 1792) >> 2) + (OffsetB & 63);
        int OffsetM = OffsetT >> (7 + LogBpp);

        int MacroX = ((OffsetM % (AlignedWidth >> 5)) << 2);
        int Tile = ((((OffsetT >> (5 + LogBpp)) & 2) + (OffsetB >> 6)) & 3);
        int Macro = (MacroX + Tile) << 3;
        int Micro = ((((OffsetT >> 1) & ~15) + (OffsetT & 15)) & ((TexelPitch << 3) - 1)) >> LogBpp;

        return Macro + Micro;
    }

    private static int XGAddress2DTiledY(int Offset, int Width, int TexelPitch) {
        int AlignedWidth = (Width + 31) & ~31;

        int LogBpp = (TexelPitch >> 2) + ((TexelPitch >> 1) >> (TexelPitch >> 2));
        int OffsetB = Offset << LogBpp;
        int OffsetT = ((OffsetB & ~4095) >> 3) + ((OffsetB & 1792) >> 2) + (OffsetB & 63);
        int OffsetM = OffsetT >> (7 + LogBpp);

        int MacroY = ((OffsetM / (AlignedWidth >> 5)) << 2);
        int Tile = ((OffsetT >> (6 + LogBpp)) & 1) + (((OffsetB & 2048) >> 10));
        int Macro = (MacroY + Tile) << 3;
        int Micro = ((((OffsetT & (((TexelPitch << 6) - 1) & ~31)) + ((OffsetT & 15) << 1)) >> (3 + LogBpp)) & ~1);

        return Macro + Micro + ((OffsetT & 16) >> 4);
    }
	
	
	public boolean readHeaderPart2(ByteBuffer in) {
		//same for v9 and v8, i think
		int basepos = in.position();
		if (in.getInt() == 0 && in.getInt() == 0 && in.getInt() == 0) { //0
			//PC
			//0 0 0 nfsID 0 0
//			format = Block.readStringAligned(in); // should be always 4 bytes!
			format = in.getInt();
			
			if (format != 0 && in.getInt() == 0 && in.getInt() == 0) return false;
		}
		//xbox
		//hash1 hash2 0 garbage 0 flags(82/113,1,32,26) 0 0
		in.position(basepos + 32); //unknown data
		switch(compression) {
		case DXT1:
			format = 0x31545844;
			break;
		case DXT3:
			format = 0x33545844;
			break;
		case DXT5:
			format = 0x35545844;
			break;
		case DXTN:
			format = 0x4E545844;
			break;
		default:
			break;
		}
		return true;
		
	}

	public void readHeaderPart1(ByteBuffer in) {
		//same for v9 and v8
		in.getInt(); //0 -> _cube_environment
		in.getInt(); //0
		in.getInt(); //0
		binKey = in.getInt();
		
		ClassKey = in.getInt(); //0x001A93CF most cases, 0x1B81E7B0 wheel misc engine & brakelight damage0, 0x6B6D3253 badging, 0x1DA6C8A6 normalmaps
		imageOffset = in.getInt();
		paletteOffset = in.getInt(); //-1
		imageDataLength = in.getInt(); //Size
		
		var paletteSize = in.getInt(); //PaletteSize
		firstMipMapDataLength = in.getInt(); //imageDataLength again TODO -> pixel count for dxt5 -> //Area
		width = Short.toUnsignedInt(in.getShort());
		height = Short.toUnsignedInt(in.getShort());
//		var24 = in.getInt(); //06052400, TODO
		in.get(); //logx
		in.get(); //logy
		compression = CompressionType.Get(in.get()); //compression
		in.get(); //PaletteCompression
		
		in.getShort(); //NumPalettes
		mipMapCount = Byte.toUnsignedInt(in.get());
		in.get(); //flag -> TileableUV
		
		biasLevel = in.get(); //BiasLevel
		renderingOrder= in.get(); //RenderingOrder
		scrollType=in.get(); //ScrollType
		used=in.get(); //Used

		applyAlphaSort=in.get(); //ApplyAlphaSort
		alphaUsageType=in.get(); //AlphaUsageType
		alphaBlendType=in.get(); //AlphaBlendType
		flags=in.get(); //Flags
//		const32 = in.getInt(); //0 opaque, 0x00000500 transparent, 0x00000700 badging
//		const33 = in.getInt(); //0x00010200 if emissive, 0x00010201 if transparent, 0x01000000 if opaque, 0x01000200 misc and engine
		
		in.get(); //MipMapBiasType
		in.get(); //padding
		in.getShort(); //ScrollTimeStep
		in.getShort(); //ScrollSpeedS
		in.getShort(); //ScrollSpeedT
		in.getShort(); //OffsetS
		in.getShort(); //OffsetT
		scaleS = in.getShort();
		scaleT = in.getShort();

		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		
		in.get(); //name length, # = 35
		name = Block.readStringAligned(in);
		Hash.findBIN(name);

	
		if (paletteSize != 0) System.err.println("Texture %s : palette not supported!".formatted(name));
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
		
//		var reader = ByteBuffer.wrap(DDSImage);
//		reader.order(ByteOrder.LITTLE_ENDIAN);
//		reader.position(reader.position()+128); //skip dds header
		
//		writer.put(in);
//		out.put(reader);
		out.put(DDSImage, 128, imageDataLength);

		return out.array();
	}

	public void writeHeaderPart2(ByteBuffer out) {
		out.putInt(0);
		out.putInt(0);
		out.putInt(0);
		
//		Block.putString(out, format);
		out.putInt(format);
		out.putInt(0);
		
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
		
		out.putInt(ClassKey);
		out.putInt(imageOffset);
		out.putInt(paletteOffset);
		out.putInt(imageDataLength);

		out.putInt(0);
		out.putInt(firstMipMapDataLength); //TODO
		out.putShort((short)width);
		out.putShort((short)height);
		out.put((byte)((Math.log(width)/Math.log(2))+0.1)); //0.1 to rule out floating point errors
		out.put((byte)((Math.log(height)/Math.log(2))+0.1));
		out.put(compression.Value());
		out.put((byte)0);
		
//		out.putInt(const31);
		out.putShort((short) 0);
		out.put((byte) mipMapCount);
		out.put((byte) 0); //TileableUV
//		out.putInt(const32); //transparency ? 1280 : 0
//		out.putInt(const33); //transparency ? 01020100 : 00000001
		out.put(biasLevel); //BiasLevel
		out.put(renderingOrder); //RenderingOrder
		out.put(scrollType); //ScrollType
		out.put(used); //Used

		out.put(applyAlphaSort); //ApplyAlphaSort
		out.put(alphaUsageType); //AlphaUsageType
		out.put(alphaBlendType); //AlphaBlendType
		out.put(flags); //Flags

		out.putInt(0);

		out.putInt(0);
		out.putInt(0);
		out.putShort(scaleS);
		out.putShort(scaleT);

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
//		this.imageDataLength = 0;
//		for (int i=0; i < mipMapCount; i++) {
//			this.imageDataLength += this.firstMipMapDataLength/Math.pow(4, i);
//		}
		this.imageDataLength = DDSImage.length - 128;
		
		var formatFlag = getIntAt(DDSImage, 80);
		this.format = 0;
		switch (formatFlag) {
		case 4: //FOURCC
			this.format = getIntAt(DDSImage, 84);
			this.firstMipMapDataLength = getIntAt(DDSImage, 20);
			switch(format) {
			case 0x31545844:
				this.compression = CompressionType.DXT1;
				break;
			case 0x33545844:
				this.compression = CompressionType.DXT3;
				break;
			case 0x35545844:
				this.compression = CompressionType.DXT5;
				break;
			case 0x4E545844:
				this.compression = CompressionType.DXTN;
				break;
			default:
				System.err.println("Error upon importing %s: Unknown DDS FourCC: 0x%08X".formatted(name, format));
				this.compression = CompressionType.Default;
				break;
			}
			break;
		case 0x40: //RGB
			this.format = CompressionType.TC_24Bit.nfsID;
			this.compression = CompressionType.TC_24Bit;
			this.firstMipMapDataLength = width * height * 4;
			break;
		case 0x41: //RGBA
			this.format = CompressionType.TC_32Bit.nfsID;
			this.compression = CompressionType.TC_32Bit;
			this.firstMipMapDataLength = width * height * 4;
			break;
		default:
			this.compression = CompressionType.Default;
			System.err.println("Error upon importing %s: Unknown DDS Type: 0x%08X".formatted(name, formatFlag));
		}
				
//		this.const32 = format == DXT1 ? 0 : 1280;
//		this.const33 = format == DXT1 ? 0x01000000 : 0x00010201;
		if (format == DXT1) {
			biasLevel = 0;
			renderingOrder = 0;
			scrollType = 0;
			used = 0;
			applyAlphaSort = 0;
			alphaUsageType = 0;
			alphaBlendType = 0;
			flags = 1;
		} else {
			biasLevel = 0;
			renderingOrder = 5;
			scrollType = 0;
			used = 0;
			applyAlphaSort = 1;
			alphaUsageType = 2;
			alphaBlendType = 1;
			flags = 0;
		}
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
		
		ClassKey = copyFrom.ClassKey;
		imageOffset = copyFrom.imageOffset;
		paletteOffset = copyFrom.paletteOffset;
		imageDataLength = copyFrom.imageDataLength;

		firstMipMapDataLength = copyFrom.firstMipMapDataLength;
		width = copyFrom.width;
		height = copyFrom.height;
//		var24 = copyFrom.var24;

		mipMapCount = copyFrom.mipMapCount;
		biasLevel = copyFrom.biasLevel;
		renderingOrder = copyFrom.renderingOrder;
		scrollType = copyFrom.scrollType;
		used = copyFrom.used;
		applyAlphaSort = copyFrom.applyAlphaSort;
		alphaUsageType = copyFrom.alphaUsageType;
		alphaBlendType = copyFrom.alphaBlendType;
		flags = copyFrom.flags;
		scaleS = copyFrom.scaleS;
		scaleT = copyFrom.scaleT;

		name = new String(copyFrom.name);
//		format = new String(copyFrom.format);
		format = copyFrom.format;
		
		compression = copyFrom.compression;
		
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
			var newImageLength = tex.DDSImage.length - tex.firstMipMapDataLength;
			
			tex.height /= 2;
			tex.width /= 2;
			tex.mipMapCount -= 1;
			tex.imageDataLength -= tex.firstMipMapDataLength;
			tex.firstMipMapDataLength /= 4;
			
			//tex.DDSImage = new byte[tex.imageDataLength+128];
			tex.DDSImage = new byte[newImageLength];
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
			
			var header = ByteBuffer.wrap(Arrays.copyOf(tex.DDSImage, 128));
			header.order(ByteOrder.LITTLE_ENDIAN);
			header.putInt(12, tex.height/2); //height
			header.putInt(16, tex.width/2); //width
			header.putInt(28, tex.mipMapCount-1); //mipmap count
			header.putInt(20, tex.firstMipMapDataLength/4); //first mipmap data length
			
			var newImage = Arrays.copyOfRange(tex.DDSImage, 128+tex.firstMipMapDataLength, tex.DDSImage.length);
			var newImageLength = tex.DDSImage.length - tex.firstMipMapDataLength;
			
			tex.height /= 2;
			tex.width /= 2;
			tex.mipMapCount -= 1;
			tex.imageDataLength -= tex.firstMipMapDataLength;
			tex.firstMipMapDataLength /= 4;
			
			//tex.DDSImage = new byte[tex.imageDataLength+128];
			tex.DDSImage = new byte[newImageLength];
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

enum CompressionType{
	Default(0),
	TC_4Bit(4,4),
	TC_8Bit(8,8,0x00000029), //8
	TC_16Bit(16,16),
	TC_16Bit_1555(17,16),
	TC_16Bit_565(18,16),
	TC_16Bit_3555(19,16),
	TC_24Bit(24,24),
	TC_32Bit(32,32,0x00000015),
	DXT(33),
	DXT1(34,32,0x31545844), //34
	DXT3(36,32,0x33545844), //36
	DXT5(38,32,0x35545844), //38
	DXTN(39,32,0x4E545844),
	L8(40),
	DXT1_AIR(41),
	DXT1_AIG(42),
	DXT1_AIB(43),
	TC_8Bit_16(128,8,0x00000029), //128
	TC_8Bit_64(129,8,0x00000029), //129
	TC_8Bit_IA8(130,8),
	TC_4Bit_IA8(131,4),
	TC_4Bit_RGB24_A8(140,4),
	TC_8Bit_RGB24_A8(141,8),
	TC_4Bit_RGB16_A8(142,4),
	TC_8Bit_RGB16_A8(143,8);

	private byte val;
	public byte bitCount;
	public int nfsID;

	CompressionType(int i) {
		val = (byte) i;
		bitCount = (byte) 32; //default value for pitch calculation
		nfsID = -1;
	}
	CompressionType(int i, int bc) {
		val = (byte) i;
		bitCount = (byte)bc;
		nfsID = -1;
	}
	CompressionType(int i, int bc, int n) {
		val = (byte) i;
		bitCount = (byte)bc;
		nfsID = n;
	}
	
	byte Value() {return val;}
	
	static CompressionType Get(byte b) {
		for (var ct : values()) {
			if (ct.val == b) return ct;
		}
		return Default;
	}
}