package fr.ni240sx.ucgt.binstuff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

// Contains code from 379Felipe
public class Hash {
	
	//TODO change the display of unknown hashes to their hex code + eventual given description
	
	public String label;
	public int binHash; //memory
	public int reversedBinHash; //file
	public byte[] reversedBinHashBytes;

	public int vltHash;
	public int reversedVltHash;
	
	/**
	 * Bin or Vlt Hash only !
	 * @param label
	 * @param hex
	 * @param type "BIN" or "VLT"
	 */
	public Hash(String label, int hex, String type) {
		switch(type) {
		case "BIN":
			this.label = label;
			this.binHash = hex;
			this.reversedBinHash = Integer.reverseBytes(this.binHash);
			this.reversedBinHashBytes = intToBytes(reversedBinHash);
			break;
		case "VLT":
			this.label = label;
			this.vltHash = hex;
			this.reversedVltHash = Integer.reverseBytes(this.vltHash);
			break;
		}
	}
	
	/**
	 * @param label
	 */
	public Hash(String label) {
		this.label = label;
		this.binHash = findBinHash(label);
		this.vltHash = findVltHash(label);
		this.reversedBinHash = Integer.reverseBytes(this.binHash);
		this.reversedVltHash = Integer.reverseBytes(vltHash);
		this.reversedBinHashBytes = intToBytes(reversedBinHash);
//		System.out.println(label + "=" + Integer.toHexString(this.binHash));
	}
	
	/**
	 * BIN Hash only ! Force a label on a hash
	 * @param label
	 * @param hex
	 */
	public Hash(String label, int hex) {
		this.label = label;
		this.binHash = hex;
//		this.vltHash = findVltHash(label);
		this.reversedBinHash = Integer.reverseBytes(this.binHash);
//		this.reversedVltHash = Integer.reverseBytes(vltHash);
		this.reversedBinHashBytes = intToBytes(reversedBinHash);
//		System.out.println(label + "=" + Integer.toHexString(this.binHash));
		
	}
	
	//                         0   1   2   3   4   5   6   7   8   9   A   B   C   D   E   F
	static char[] binChars = {'!','"','#','ù','%','&','\'','(',')','*','+','ù','-','.','/','0',	//0
							  '1','2','3','4','5','6','7','8','9',':',';','<','=','>','?','@',	//1
							  'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',	//2
							  'Q','R','S','T','U','V','W','X','Y','Z','[','\\',']','ù','_','ù',	//3
							  'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p',	//4
							  'q','r','s','t','u','v','w','x','y','z','ù','ù','ù','ù','ù','ù'};
	

	public static short getCharHash(char search) {
		short i=0;
		while (i<binChars.length && binChars[i]!=search) {
			i++;
		}
		return i;
	}

	public static char reverseCharHash(byte search) {
		if (search<binChars.length)return binChars[search];
		else return 'ù';
	}
	
	
	public static int findBinHash(String s) {
		s = s.strip();
		if (!s.isBlank()) {
			int l = getCharHash(s.charAt(0));
			for (int i=1; i<s.length(); i++) {
				l = getCharHash(s.charAt(i))+33*l+33;
			}
//			System.out.println(Integer.toHexString(l));
			return l;
			
		} else return 0;	
	}
	
	
	public static int findVltHash(String s) {
		s = s.strip();
		if (!s.isBlank()) {
			
            byte[] arr = s.getBytes();
            int a = 0x9E3779B9;
            int b = 0x9E3779B9;
            int c = 0xABCDEF00;
            int v1 = 0;
            int v2 = arr.length;
            
            ByteBuffer bb = ByteBuffer.wrap(arr);
			bb.order(ByteOrder.LITTLE_ENDIAN);
            while (v2 >= 12)
            {	
                a += bb.getInt(v1);
                b += bb.getInt(v1+4);
                c += bb.getInt(v1+8);
                int[] abc = Mix32_1(a, b, c);
                a = abc[0];
                b = abc[1];
                c = abc[2];	
                v1 += 12;
                v2 -= 12;
            }			

            c += arr.length;
        	
            switch (v2)
            {
                case 11:
                    c += arr[10 + v1] << 24;
                case 10:
                    c += arr[9 + v1] << 16;
                case 9:
                    c += arr[8 + v1] << 8;
                case 8:
                    b += arr[7 + v1] << 24;
                case 7:
                    b += arr[6 + v1] << 16;
                case 6:
                    b += arr[5 + v1] << 8;
                case 5:
                    b += arr[4 + v1];
                case 4:
                    a += arr[3 + v1] << 24;
                case 3:
                    a += arr[2 + v1] << 16;
                case 2:
                    a += arr[1 + v1] << 8;
                case 1:
                    a += arr[v1];
                    break;
                default:
                    break;
            }

            return Mix32_2(a, b, c);
            
		} else return 0;	
	}
	
	private static int[] Mix32_1(int a, int b, int c)
    {	
        a = c >>> 13 ^ (a - b - c); 
        b = a << 8 ^ (b - c - a);
        c = b >>> 13 ^ (c - a - b);
        a = c >>> 12 ^ (a - b - c);
        b = a << 16 ^ (b - c - a);
        c = b >>> 5 ^ (c - a - b);
        a = c >>> 3 ^ (a - b - c);
        b = a << 10 ^ (b - c - a);
        c = b >>> 15 ^ (c - a - b);
        return new int[]{a,b,c};
    }
    private static int Mix32_2(int a, int b, int c)
    {
        a = c >>> 13 ^ (a - b - c);
        b = a << 8 ^ (b - c - a);
        c = b >>> 13 ^ (c - a - b);
        a = c >>> 12 ^ (a - b - c);
        b = a << 16 ^ (b - c - a);
        c = b >>> 5 ^ (c - a - b);
        a = c >>> 3 ^ (a - b - c);
        b = a << 10 ^ (b - c - a);
        return b >>> 15 ^ (c - a - b);
    }
	
	
	@Override
	public String toString() {
		return "["+label + ": BIN=" + Integer.toHexString(this.binHash)+", VLT="+ Integer.toHexString(vltHash) +"]";
	}

	public static void main(String[] a) {
//		new Hash("[texture]",-1367985330);

		Hash test0 = new Hash("TEST");
		Hash test1 = new Hash("AAA_AAA_AAA");
		Hash test2 = new Hash("BMW_M3_E92_08");
		Hash test3 = new Hash("mit_ecl_gt_06");
		System.out.println(test0);
		System.out.println(test1);
		System.out.println(test2);
		System.out.println(test3);
	}

	private static byte[] intToBytes(final int data) {
	    return new byte[] {
	        (byte)((data >> 24) & 0xff),
	        (byte)((data >> 16) & 0xff),
	        (byte)((data >> 8) & 0xff),
	        (byte)((data >> 0) & 0xff),
	    };
	}
	
	
	
	public static ArrayList<Hash> loadHashes(File f) {
		ArrayList<Hash> l = new ArrayList<Hash>();		
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String key;
			while ((key = br.readLine())!=null){
				l.add(new Hash(key));
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return l;
	}
	
	/**
	 * @param hash to look for
	 * @param hashlist to look into
	 * @param defaultName fallback name
	 * @param type "BIN" or "VLT"
	 * @return
	 */
	public static Hash guess(int hash, List<Hash> hashlist, String defaultName, String type) {
		if (hash==0) return new Hash("");
		for (Hash h : hashlist) {
			if (h.binHash == hash || h.reversedBinHash == hash || h.vltHash == hash || h.reversedVltHash == hash) {
				return h;
			}
		}
		return new Hash(defaultName, hash, type);
	}
	
}
