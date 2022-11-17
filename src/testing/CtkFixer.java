package testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import binstuff.Hash;

public class CtkFixer {
	
	

	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("tofix.txt")));

			File f = new File(br.readLine());
			FileInputStream fis = new FileInputStream(f);
			byte [] fileToBytes = new byte[(int)f.length()];
			fis.read(fileToBytes);

			
			
			String carname = br.readLine();
			String l;
			ArrayList<Replacements> replacements = new ArrayList<Replacements>();
			while ((l = br.readLine()) != null) { //config reading loop
				if (!l.isBlank() && !l.startsWith("#") && !l.startsWith("//")) {
					String[] line = l.split(" ");
					Hash[] toReplace = new Hash[line.length-1];
					Hash[] replacement = new Hash[line.length-1];
					for (int i=1;i<line.length;i++) {
						toReplace[i-1] = new Hash(line[i].split(">")[0].replaceAll("%", carname));
						replacement[i-1] = new Hash(line[i].split(">")[1].replaceAll("%", carname));
					}
					replacements.add(new Replacements(new Hash(carname + "_" + line[0]), toReplace, replacement));
				}
			}
			System.out.println(replacements);
			
			
			Hash currentpart = null;
			Hash potentialpart = null;
			int potentialpartoff = 0;
			byte step = 0;
			boolean allPartsFound = false;
//			int progressstep = (int) (f.length()/20);
			
			for (int off=0; off<f.length(); off++) {
				
//				if (off%progressstep == 0) System.out.println("Progress "+ (100*off/f.length() +1)+"%");
				
				for (Replacements r : replacements) { //optimization has to be done probably
					if (fileToBytes[off] == r.part.reversedBinHashBytes[0]) {
						//potential beginning of a part to replace found
						potentialpart = r.part;
						potentialpartoff = off;
						step = 0;
					}
					if (potentialpart != null) {
						if (step == 0 && fileToBytes[off] == r.part.reversedBinHashBytes[1] && potentialpart == r.part) {
							//potential second byte of a part to replace found
							if (potentialpartoff <= off-2) potentialpart = null;
							else step = 1;
						}else if (step == 1 && fileToBytes[off] == r.part.reversedBinHashBytes[2] && potentialpart == r.part) {
							//potential 3rd byte of a part to replace found
							if (potentialpartoff <= off-3) potentialpart = null;
							else step = 2;
						}else if (step == 2 && fileToBytes[off] == r.part.reversedBinHashBytes[3] && potentialpart == r.part /*misses one more condition ?*/) {
							//potential last byte of a part to replace found
							if (potentialpartoff <= off-4) potentialpart = null;
							if (potentialpart !=null) {
								//part found
								currentpart = potentialpart;
								potentialpart = null;
								if (r.seen == 1) {
									r.position = ((fileToBytes[off+4] & 0xFF) << 24) | ((fileToBytes[off+3] & 0xFF) << 16)
									        | ((fileToBytes[off+2] & 0xFF) << 8) | (fileToBytes[off+1] & 0xFF);

									System.out.println("part " + r.part.label + " found | offset (decimal) = " + r.position);
								}
								if (r.seen == 2) {
									allPartsFound = true;
									System.out.println("All parts found.");
								}
								r.seen++;
							}
						}
					}
						
					
				}
				
				
				if (allPartsFound) break;
				
			}
			
			
			
			
			
			//stop searching when 4a444c5a (JDLZ) is found
			
			
			for (Replacements r: replacements) {
				int off = r.position + 32;
				while (1514947658 != (((fileToBytes[off+4] & 0xFF) << 24) | ((fileToBytes[off+3] & 0xFF) << 16)
				        | ((fileToBytes[off+2] & 0xFF) << 8) | (fileToBytes[off+1] & 0xFF))) {
					
					
					
					
					off++;
				}
				System.out.println("End of part " + r.part + " at " + off);
			}
			
			
		} catch (FileNotFoundException e) {
			System.out.println("Please create and fill the tofix.txt configuration file");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}


class Replacements{
	
	Hash part;
	Hash[] toReplace;
	Hash[] replacements;
	public int seen=0;
	public int position=0;
	
	public Replacements(Hash part, Hash[] toReplace, Hash[] replacements) {
		super();
		this.part = part;
		this.toReplace = toReplace;
		this.replacements = replacements;
	}

	@Override
	public String toString() {
		return "Replacements [part=" + part + ", toReplace=" + Arrays.toString(toReplace) + ", replacements="
				+ Arrays.toString(replacements) + "]\n";
	}
	
}