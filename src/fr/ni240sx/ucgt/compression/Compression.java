package fr.ni240sx.ucgt.compression;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Compression {

	public static byte[] decompress(byte[] arr){
		ByteBuffer in = ByteBuffer.wrap(arr);
		String compressionType = String.valueOf(new char[] {(char)(in.get()), (char)(in.get()), (char)(in.get()), (char)(in.get())});
//		System.out.println("Compression type : "+compressionType);
		
		switch(compressionType) {
		case "RFPK":
			return new RefPackDecompress().decompress(in);
		case "JDLZ":
			return JDLZDecompress.decompress(in);
		case "RAWW":
			// raw data
			return Arrays.copyOfRange(arr, 16, arr.length);
		default:
			System.out.println("Compression type not supported.");	
			return null;
		}
	}
	
	public static byte[] compress(byte[] arr, String compressionType){
		return compress(arr, compressionType, CompressionLevel.Ultra);
	}

	public static byte[] compress(byte[] arr, String compressionType, CompressionLevel compressionLevel){
//		System.out.println("Compression type : "+compressionType);
		
		switch(compressionType) {
		case "RFPK":
			return new RefPackCompress().compress(ByteBuffer.wrap(arr), compressionLevel);
		case "JDLZ":
			return JDLZCompress.compress(arr);
		default:
			System.out.println("Compression type not supported.");	
			return null;
		}
	}
}
