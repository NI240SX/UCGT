package fr.ni240sx.ucgt.binstuff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

// Contains code from 379Felipe
public abstract class Hash {

	public static HashMap<Integer, String> BIN = new HashMap<>();
	public static HashMap<Integer, String> VLT = new HashMap<>();
	
//	public String label;
//	public int binHash; //memory
//	public int reversedBinHash; //file
//	public byte[] reversedBinHashBytes;

//	public int vltHash;
//	public int reversedVltHash;
	
	/**
	 * Bin or Vlt Hash only !
	 * @param label
	 * @param hex
	 * @param type "BIN" or "VLT"
	 */
//	public Hash(String label, int hex, String type) {
//		switch(type) {
//		case "BIN":
//			this.label = label;
//			this.binHash = hex;
//			this.reversedBinHash = Integer.reverseBytes(this.binHash);
//			this.reversedBinHashBytes = intToBytes(reversedBinHash);
//			break;
//		case "VLT":
//			this.label = label;
//			this.vltHash = hex;
//			this.reversedVltHash = Integer.reverseBytes(this.vltHash);
//			break;
//		default:
//			this.label = label;
//			break;
//		}
//	}
//	
//	/**
//	 * @param label
//	 */
//	public Hash(String label) {
//		if (label.startsWith("0x") || label.startsWith("0X")) {
//			//already hashed input, we'll assume it can be either BIN or VLT and will always be used correctly
//			this.label = label;
//			this.binHash = Integer.parseUnsignedInt(label.substring(2), 16);
////			System.out.println("Unknown hash : "+label+", "+binHash);
//			this.reversedBinHash = Integer.reverseBytes(this.binHash);
//			this.reversedBinHashBytes = intToBytes(reversedBinHash);
//			this.vltHash = this.binHash;
//			this.reversedVltHash = this.reversedBinHash;
//		} else {
//			this.label = label;
//			this.binHash = findBinHash(label);
//			this.vltHash = findVltHash(label);
//			this.reversedBinHash = Integer.reverseBytes(this.binHash);
//			this.reversedVltHash = Integer.reverseBytes(vltHash);
//			this.reversedBinHashBytes = intToBytes(reversedBinHash);
//	//		System.out.println(label + "=" + Integer.toHexString(this.binHash));
//		}
//	}
//	
//	/**
//	 * BIN Hash only ! Force a label on a hash
//	 * @param label
//	 * @param hex
//	 */
//	public Hash(String label, int hex) {
//		this.label = label;
//		this.binHash = hex;
////		this.vltHash = findVltHash(label);
//		this.reversedBinHash = Integer.reverseBytes(this.binHash);
////		this.reversedVltHash = Integer.reverseBytes(vltHash);
//		this.reversedBinHashBytes = intToBytes(reversedBinHash);
////		System.out.println(label + "=" + Integer.toHexString(this.binHash));
//		
//	}
	
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
		return 'ù';
	}
	
	private static int findBinHash(String s) {
		s = s.strip();
		if (!s.isBlank()) {
			if (s.startsWith("0x") || s.startsWith("0X")) return Integer.parseUnsignedInt(s.substring(2), 16);

			int l = getCharHash(s.charAt(0));
			for (int i=1; i<s.length(); i++) {
				l = getCharHash(s.charAt(i))+33*l+33;
			}
//			System.out.println(Integer.toHexString(l));
			return l;
			
		}
		return 0;	
	}
	
	private static int findVltHash(String s) {
		s = s.strip();
		if (!s.isBlank()) {
			if (s.startsWith("0x") || s.startsWith("0X")) return Integer.parseUnsignedInt(s.substring(2), 16);
			
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
            
		}
		return 0;	
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
	
//	@Override
//	public String toString() {
//		return "["+label + ": BIN=" + Integer.toHexString(this.binHash)+", VLT="+ Integer.toHexString(vltHash) +"]";
//	}

	public static void main(String[] a) {
//		new Hash("[texture]",-1367985330);

//		Hash test0 = new Hash("TEST");
//		Hash test1 = new Hash("AAA_AAA_AAA");
//		Hash test2 = new Hash("BMW_M3_E92_08");
//		Hash test3 = new Hash("mit_ecl_gt_06");
//		System.out.println(test0);
//		System.out.println(test1);
//		System.out.println(test2);
//		System.out.println(test3);
	}

	public static byte[] intToBytes(final int data) {
	    return new byte[] {
	        (byte)((data >> 24) & 0xff),
	        (byte)((data >> 16) & 0xff),
	        (byte)((data >> 8) & 0xff),
	        (byte)((data >> 0) & 0xff),
	    };
	}
	
//	public static ArrayList<Hash> loadHashes(File f) {
//		ArrayList<Hash> l = new ArrayList<>();		
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(f));
//			String key;
//			while ((key = br.readLine())!=null){
//				if (!key.isBlank()) l.add(new Hash(key.strip()));
//			}
//			br.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return l;
//	}
	public static void addBinHashes(File f) {
		addBinHashes(f, Hash.BIN);
	}
	public static void addBinHashes(File f, HashMap<Integer, String> m){
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String key;
			while ((key = br.readLine())!=null) if (!key.isBlank() && !key.startsWith("#") && !key.startsWith("//")){
				if (!key.isBlank()) m.put(findBinHash(key.strip()), key.strip());
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addVltHashes(File f) {
		addVltHashes(f, Hash.VLT);
	}
	public static void addVltHashes(File f, HashMap<Integer, String> m){
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String key;
			while ((key = br.readLine())!=null) if (!key.isBlank() && !key.startsWith("#") && !key.startsWith("//")){
				if (!key.isBlank()) m.put(findVltHash(key.strip()), key.strip());
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static String addBinHash(String label) {
		if (!BIN.containsKey(findBinHash(label))) BIN.put(findBinHash(label), label);
		return label;
	}
	public static String addVltHash(String label) {
		if (!VLT.containsKey(findVltHash(label))) VLT.put(findVltHash(label), label);
		return label;
	}
	
	
	public static String get(int hash, HashMap<Integer, String> m, String defaultName) {
		if (hash==0) return "";
		if (m.containsKey(hash)) return m.get(hash);
		m.put(hash, defaultName);
		return defaultName;
	}
	public static String getBIN(int hash, String defaultName) {
		if (hash==0) return "";
		if (BIN.containsKey(hash)) return BIN.get(hash);
		BIN.put(hash, defaultName);
		return defaultName;
	}
	public static String getVLT(int hash, String defaultName) {
		if (hash==0) return "";
		if (VLT.containsKey(hash)) return VLT.get(hash);
		VLT.put(hash, defaultName);
		return defaultName;
	}
	
	public static String get(int hash, HashMap<Integer, String> m) {
		if (hash==0) return "";
		if (m.containsKey(hash)) return m.get(hash);
		return String.format("0x%08X", hash);
	}
	public static String getBIN(int hash) {
		if (hash==0) return "";
		if (BIN.containsKey(hash)) return BIN.get(hash);
		return String.format("0x%08X", hash);
	}
	public static String getVLT(int hash) {
		if (hash==0) return "";
		if (VLT.containsKey(hash)) return VLT.get(hash);
		return String.format("0x%08X", hash);
	}
	

	public static int findBIN(String label) {
		var h = findBinHash(label);
		if (!BIN.containsKey(h)) BIN.put(h, label);
		return h;
	}
	public static int findVLT(String label) {
		var h = findVltHash(label);
		if (!VLT.containsKey(h)) VLT.put(h, label);
		return h;
	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + Arrays.hashCode(reversedBinHashBytes);
//		result = prime * result + Objects.hash(binHash, label, reversedBinHash, reversedVltHash, vltHash);
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Hash other = (Hash) obj;
//		return binHash == other.binHash && Objects.equals(label, other.label)
//				&& reversedBinHash == other.reversedBinHash
//				&& Arrays.equals(reversedBinHashBytes, other.reversedBinHashBytes)
//				&& reversedVltHash == other.reversedVltHash && vltHash == other.vltHash;
//	}

	/**
	 * @param hash to look for
	 * @param hashlist to look into
	 * @param defaultName fallback name
	 * @param type "BIN" or "VLT"
	 * @return
	 */
//	public static Hash guess(int hash, List<Hash> hashlist, String defaultName, String type) {
//		if (hash==0) return new Hash("");
//		for (Hash h : hashlist) {
//			if (h.binHash == hash || h.reversedBinHash == hash || h.vltHash == hash || h.reversedVltHash == hash) {
//				return h;
//			}
//		}
//		return new Hash(defaultName, hash, type);
//	}
	
}
