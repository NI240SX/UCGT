package fr.ni240sx.ucgt.compression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Compression {
	
	public static void main(String[] args) {
//		decompress(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BASE_A_2"));
//		decompress(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BODY_A_2"));
//		decompress(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BODY_A_1"));
//		byte[] decomp = decompress(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BRAKELIGHT_GLASS_LEFT_A"));
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BRAKELIGHT_GLASS_LEFT_A_decomp"));
//			fos.write(decomp);
			fos.write(decompress(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BRAKELIGHT_GLASS_LEFT_A")));
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

	private static byte[] decompress(File f) throws InterruptedException {
		FileInputStream fis;
		try {
			System.out.println("Decompressing file : "+f.getName());
			fis = new FileInputStream(f);
			byte [] arr = new byte[(int)f.length()];
			fis.read(arr);
			fis.close();
			ByteBuffer in = ByteBuffer.wrap(arr);
			String compressionType = String.valueOf(new char[] {(char)(in.get()), (char)(in.get()), (char)(in.get()), (char)(in.get())});
			System.out.println("Compression type : "+compressionType);
			
			switch(compressionType) {
			case "RFPK":
				return RefPackDecompress.decompress(in);
			default:
				System.out.println("Compression type not supported.");	
				return null;
			}
						
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
}
