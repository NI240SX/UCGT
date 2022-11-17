package testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
			fis.close();
			
			
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
			
			
			Hash potentialpart = null;
			int potentialpartoff = 0;
			byte step = 0;
			boolean allPartsFound = false;
			int off=0;
//			int progressstep = (int) (f.length()/20);
			
			while (1514947658 != (((fileToBytes[off+4] & 0xFF) << 24) | ((fileToBytes[off+3] & 0xFF) << 16)
			        | ((fileToBytes[off+2] & 0xFF) << 8) | (fileToBytes[off+1] & 0xFF))) {	//stop searching when 4a444c5a (JDLZ) is found
				
//				if (off%progressstep == 0) System.out.println("Progress "+ (100*off/f.length() +1)+"%");
				
				for (Replacements r : replacements) { //optimization has to be done probably
/*					if (fileToBytes[off] == r.part.reversedBinHashBytes[0]) {
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
						}else if (step == 2 && fileToBytes[off] == r.part.reversedBinHashBytes[3] && potentialpart == r.part) { //misses one more condition ?
							//potential last byte of a part to replace found
							if (potentialpartoff <= off-4) potentialpart = null;
							if (potentialpart !=null) {
								potentialpart = null;
								if (r.seen == 1) {
									r.position = ((fileToBytes[off+4] & 0xFF) << 24) | ((fileToBytes[off+3] & 0xFF) << 16)
									        | ((fileToBytes[off+2] & 0xFF) << 8) | (fileToBytes[off+1] & 0xFF);

									System.out.println("part " + r.part.label + " found | offset (decimal) = " + r.position);
								}
								r.seen++;
							}
						}
					}*/
					if (fileToBytes[off] == r.part.reversedBinHashBytes[0] 
						&& fileToBytes[off+1] == r.part.reversedBinHashBytes[1] 
						&& fileToBytes[off+2] == r.part.reversedBinHashBytes[2] 
						&& fileToBytes[off+3] == r.part.reversedBinHashBytes[3]) {
						
						if (r.seen == 1) {
							r.position = ((fileToBytes[off+7] & 0xFF) << 24) | ((fileToBytes[off+6] & 0xFF) << 16)
							        | ((fileToBytes[off+5] & 0xFF) << 8) | (fileToBytes[off+4] & 0xFF);

							System.out.println("part " + r.part.label + " found | offset (decimal) = " + r.position);
						}
						r.seen++;
						
						
						
						
						
					}
					
					
				}
				off++;
			}

			System.out.println("All parts found.");
			
			byte cut = 0;
			
			for (Replacements r: replacements) {
				

				System.out.println("Starting to replace part " + r.part + " at " + (r.position+32));
				
				off = r.position + 32;
				while (1514947658 != (((fileToBytes[off+4] & 0xFF) << 24) | ((fileToBytes[off+3] & 0xFF) << 16)
				        | ((fileToBytes[off+2] & 0xFF) << 8) | (fileToBytes[off+1] & 0xFF))) {	//stop searching when 4a444c5a (JDLZ) is found
					
					for(int i=0; i<r.toReplace.length; i++) {
						
						
						
						
						
						if (fileToBytes[off] == r.toReplace[i].reversedBinHashBytes[0]) {
							//potential beginning of a hash to replace found
							potentialpart = r.toReplace[i];
							potentialpartoff = off;
							step = 0;
							cut = 0;
//							System.out.println("1st byte loop triggered for shader/texture/normalmap " + r.toReplace[i].label +" | "+off);
						}
						if (potentialpart != null) {
							if (step == 0 && fileToBytes[off] == r.toReplace[i].reversedBinHashBytes[1] && potentialpart == r.toReplace[i]) {
								//potential second byte of a part to replace found
								if (potentialpartoff == off-2) cut = 1;
								if (potentialpartoff <= off-3) {
									potentialpart = null;
									step = 0;
								}
								else {
									step = 1;
//									System.out.println("2nd byte loop triggered for shader/texture/normalmap " + r.toReplace[i].label +" | "+off + " right after " + potentialpartoff);
								}
							}else if (step == 1 && fileToBytes[off] == r.toReplace[i].reversedBinHashBytes[2] && potentialpart == r.toReplace[i]) {
								//potential 3rd byte of a part to replace found
								if (cut == 0 && potentialpartoff == off-3) cut = 2;
								if (potentialpartoff <= off-4) {
									potentialpart = null;
									step = 0;
								}
								else {
									step = 2;
//									System.out.println("3rd byte loop triggered for shader/texture/normalmap " + r.toReplace[i].label +" | "+off);
								}
							}else if (step == 2 && fileToBytes[off] == r.toReplace[i].reversedBinHashBytes[3] && potentialpart == r.toReplace[i] /*misses one more condition ?*/) {
								//potential last byte of a part to replace found
								if (cut == 0 && potentialpartoff == off-4) cut = 3;
								if (potentialpartoff <= off-5) {
									potentialpart = null;
									step = 0;
								}
								if (potentialpart !=null) {
									potentialpart = null;
									System.out.println("shader/texture/normalmap " + r.toReplace[i].label + " found | " + (off+1));
									fileToBytes[potentialpartoff] = r.replacements[i].reversedBinHashBytes[0];
									switch (cut) {
									case 1:
										System.out.println("Hash cut after 1 byte; replaced "+potentialpartoff+ "," + (potentialpartoff+2) +","+ (potentialpartoff+3)+","+(potentialpartoff+4));
										fileToBytes[potentialpartoff+2] = r.replacements[i].reversedBinHashBytes[1];
										fileToBytes[potentialpartoff+3] = r.replacements[i].reversedBinHashBytes[2];
										fileToBytes[potentialpartoff+4] = r.replacements[i].reversedBinHashBytes[3];
										break;
									case 2:
										System.out.println("Hash cut after 2 bytes; replaced "+potentialpartoff+ "," + (potentialpartoff+1) +","+ (potentialpartoff+3)+","+(potentialpartoff+4));
										fileToBytes[potentialpartoff+1] = r.replacements[i].reversedBinHashBytes[1];
										fileToBytes[potentialpartoff+3] = r.replacements[i].reversedBinHashBytes[2];
										fileToBytes[potentialpartoff+4] = r.replacements[i].reversedBinHashBytes[3];
										break;
									case 3:
										System.out.println("Hash cut after 3 bytes; replaced "+potentialpartoff+ "," + (potentialpartoff+1) +","+ (potentialpartoff+2)+","+(potentialpartoff+4));
										fileToBytes[potentialpartoff+1] = r.replacements[i].reversedBinHashBytes[1];
										fileToBytes[potentialpartoff+2] = r.replacements[i].reversedBinHashBytes[2];
										fileToBytes[potentialpartoff+4] = r.replacements[i].reversedBinHashBytes[3];
										break;
									case 0:
										System.out.println("Hash not cut; replaced "+potentialpartoff+ "," + (potentialpartoff+1) +","+ (potentialpartoff+2)+","+(potentialpartoff+3));
										fileToBytes[potentialpartoff+1] = r.replacements[i].reversedBinHashBytes[1];
										fileToBytes[potentialpartoff+2] = r.replacements[i].reversedBinHashBytes[2];
										fileToBytes[potentialpartoff+3] = r.replacements[i].reversedBinHashBytes[3];
									}
								}
							}
						}
						
						
						
						
						
						
						
					}
					
					off++;
				}
				System.out.println("End of part " + r.part + " at " + off);
			}
			System.out.println("All replacements done.");
			
			//important crash fix
			fileToBytes[48]=67;
			fileToBytes[49]=97;
			fileToBytes[50]=114;
			fileToBytes[51]=84;
			fileToBytes[52]=111;
			fileToBytes[53]=111;
			fileToBytes[54]=108;
			fileToBytes[55]=83;
			fileToBytes[56]=104;
			fileToBytes[57]=105;
			fileToBytes[58]=116;
			fileToBytes[59]=32;
			fileToBytes[60]=102;
			fileToBytes[61]=105;
			fileToBytes[62]=120;
			fileToBytes[63]=101;
			fileToBytes[64]=100;
			fileToBytes[65]=32;
			fileToBytes[66]=98;
			fileToBytes[67]=121;
			fileToBytes[68]=32;
			fileToBytes[69]=85;
			fileToBytes[70]=67;
			fileToBytes[71]=71;
			fileToBytes[72]=84;
			fileToBytes[73]=32;
			fileToBytes[74]=45;
			fileToBytes[75]=32;
			fileToBytes[76]=78;
			fileToBytes[77]=73;
			fileToBytes[78]=50;
			fileToBytes[79]=52;
			fileToBytes[80]=48;
			fileToBytes[81]=83;
			fileToBytes[82]=88;
			fileToBytes[83]=32;
			fileToBytes[84]=50;
			fileToBytes[85]=48;
			fileToBytes[86]=50;
			fileToBytes[87]=50;
			fileToBytes[104]=68;
			fileToBytes[105]=69;
			fileToBytes[106]=70;
			fileToBytes[107]=65;
			fileToBytes[108]=85;
			fileToBytes[109]=76;
			
			
			
			
			
			
			
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(fileToBytes);
			fos.close();
			System.out.println("File saved.");
			
			
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