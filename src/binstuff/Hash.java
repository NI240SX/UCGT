package binstuff;

public class Hash {
	
	public String label;
	public int binHash;
	
	
	public Hash(String label) {
		this.label = label;
		this.binHash = findBinHash(label);
		System.out.println(label + "=" + Integer.toHexString(this.binHash));
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
	
	public static void main(String[] a) {
		System.out.println(Integer.toHexString(findBinHash("      test ")));
	}
}
