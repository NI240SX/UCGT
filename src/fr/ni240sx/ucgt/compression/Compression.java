package fr.ni240sx.ucgt.compression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Compression {
	
	public static void main(String[] args) {
//		decompress(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BASE_A_2"));
//		decompress(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BODY_A_2"));
//		decompress(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BODY_A_1"));
//		byte[] decomp = decompress(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BRAKELIGHT_GLASS_LEFT_A"));
		FileOutputStream fos = null;
		try {
//			
//			//RFPK DECOMP
//			
//			fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BRAKELIGHT_GLASS_LEFT_A_decomp"));
//			fos.write(decompressFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BRAKELIGHT_GLASS_LEFT_A")));
//			fos.close();
//
//			fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BODY_A_0_decomp"));
//			fos.write(decompressFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BODY_A_0")));
//			fos.close();
//
//			//RFPK COMP
//			
//			fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BRAKELIGHT_GLASS_LEFT_A_decomp_recomp"));
//			fos.write(compressFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BRAKELIGHT_GLASS_LEFT_A_decomp"), "RFPK"));
//			fos.close();
//			
//			fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BODY_A_0_decomp_recomp"));
//			fos.write(compressFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BODY_A_0_decomp"), "RFPK", 
//					new CompressionLevel(1, 1, 10, 32768)));
//			// BlockInterval, SearchLength, PrequeueLength, QueueLength, SameValToTrack, BruteForceLength
//			fos.close();
//
//			fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BODY_A_0_decomp_recomp_redecomp"));
//			fos.write(RefPackDecompress.decompress(ByteBuffer.wrap(new FileInputStream(
//					new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BODY_A_0_decomp_recomp")
//					).readAllBytes()), false));
//			fos.close();
//
//			
//			
//			fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08\\AUD_RS4_STK_08_KIT00_BASE_A.rfpk"));
//			fos.write(compressFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08\\AUD_RS4_STK_08_KIT00_BASE_A.dat"), "RFPK"));
//			fos.close();
//
//			fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08\\AUD_RS4_STK_08_KIT00_BASE_A.rfpk_redecomp"));
//			fos.write(RefPackDecompress.decompress(ByteBuffer.wrap(new FileInputStream(
//					new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08\\AUD_RS4_STK_08_KIT00_BASE_A.rfpk")
//					).readAllBytes()), false));
//			fos.close();
//		
//			
//			
			//JDLZ

			fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\ctk_NIS_240_SX_89_KIT00_BRAKELIGHT_GLASS_RIGHT_A_decomp"));
			fos.write(decompressFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\ctk_NIS_240_SX_89_KIT00_BRAKELIGHT_GLASS_RIGHT_A")));
			fos.close();
			
		} catch (Exception e) {
			try {
				fos.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	private static byte[] decompressFile(File f){
		FileInputStream fis;
		try {
//			System.out.println("Decompressing file : "+f.getName());
			fis = new FileInputStream(f);
			byte [] arr = new byte[(int)f.length()];
			fis.read(arr);
			fis.close();
			return decompress(arr);
						
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

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
	
	private static byte[] compressFile(File f, String compressionType){
		return compressFile(f, compressionType, CompressionLevel.Max);
	}
	
	private static byte[] compressFile(File f, String compressionType, CompressionLevel compressionLevel){
		FileInputStream fis;
		try {
			System.out.println("Compressing file : "+f.getName());
			fis = new FileInputStream(f);
			byte [] arr = new byte[(int)f.length()];
			fis.read(arr);
			fis.close();
			return compress(arr, compressionType, compressionLevel);
						
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
