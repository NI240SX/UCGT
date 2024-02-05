package geomCheck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import binstuff.Hash;
import dbmpPlus.AttributeTwoString;
import dbmpPlus.Part;

public class GeomCheck {
	
	

	public static void main(String[] args) {
		
		try {
			BufferedWriter log = new BufferedWriter(new FileWriter(new File("GeomCheck.log")));
			log.write("Initializing GeomCheck...\n");
			BufferedReader br = new BufferedReader(new FileReader(new File("GeomCheck.ini")));

			//Config read loop
			String l;
			File f;
			String carname;
			boolean file = false;
			boolean car = false;
			ArrayList<String> autosculptKits = new ArrayList<String>();
			ArrayList<String> widebodyKits = new ArrayList<String>();
			ArrayList<String> fullReplacementKits = new ArrayList<String>();
			int exhausts = 5;
			
			while ((l = br.readLine()) != null) {
				l = l.split("//")[0].split("#")[0]; // comments removal
				if (!l.isBlank() && l.contains("=")) {
					if(l.split("=")[0].strip().toLowerCase().equals("file") 
							|| l.split("=")[0].strip().toLowerCase().equals("path") 
							|| l.split("=")[0].strip().toLowerCase().equals("file path")) {
						f = new File(l.split("=")[1].strip());
						file = true;
					}
					if(l.split("=")[0].strip().toLowerCase().equals("car") 
							|| l.split("=")[0].strip().toLowerCase().equals("car name") 
							|| l.split("=")[0].strip().toLowerCase().equals("xname")) {
						carname = l.split("=")[1].strip();
						car = true;
					}
					if(l.split("=")[0].strip().toLowerCase().equals("autosculpt kits")
							|| l.split("=")[0].strip().toLowerCase().equals("autosculpt")) {
						for (String s : l.split("=")[1].split(",")) {
							if (s.contains("-")) {
								for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
										i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
									if (i<10) autosculptKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
									else autosculptKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
								}
							} else if (!s.strip().isBlank()) autosculptKits.add(s.strip());
						}
					}
					if(l.split("=")[0].strip().toLowerCase().equals("widebody kits")
							|| l.split("=")[0].strip().toLowerCase().equals("widebody")
							|| l.split("=")[0].strip().toLowerCase().equals("widebodies")) {
						for (String s : l.split("=")[1].split(",")) {
							if (s.contains("-")) {
								for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
										i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
									if (i<10) widebodyKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
									else widebodyKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
								}
							} else if (!s.strip().isBlank()) widebodyKits.add(s.strip());
						}
					}
					if(l.split("=")[0].strip().toLowerCase().equals("full replacement kits")
							|| l.split("=")[0].strip().toLowerCase().equals("full replacement widebody")
							|| l.split("=")[0].strip().toLowerCase().equals("full replacement widebodies")
							|| l.split("=")[0].strip().toLowerCase().equals("replacement")
							|| l.split("=")[0].strip().toLowerCase().equals("base")) {
						for (String s : l.split("=")[1].split(",")) {
							if (s.contains("-")) {
								for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
										i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
									if (i<10) fullReplacementKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
									else fullReplacementKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
								}
							} else if (!s.strip().isBlank()) fullReplacementKits.add(s.strip());
						}
					}
					if(l.split("=")[0].strip().toLowerCase().equals("exhausts") 
							|| l.split("=")[0].strip().toLowerCase().equals("exhausts amount") 
							|| l.split("=")[0].strip().toLowerCase().equals("amount of exhausts")) {
						exhausts = Integer.parseInt(l.split("=")[1].strip());
					}
				}
			}

			if (!file) throw new Exception("File input not specified !");
			if (!car) throw new Exception("Car name not specified !");
			

			if(autosculptKits.isEmpty()) {
				autosculptKits.add("KIT01");
				autosculptKits.add("KIT02");
				autosculptKits.add("KIT03");
				autosculptKits.add("KIT04");
				autosculptKits.add("KIT05");
				autosculptKits.add("KIT06");
				autosculptKits.add("KIT07");
				autosculptKits.add("KIT08");
				autosculptKits.add("KIT11");
				autosculptKits.add("KIT12");
			}
			if(widebodyKits.isEmpty()) {
				widebodyKits.add("KITW01");
				widebodyKits.add("KITW02");
				widebodyKits.add("KITW03");
				widebodyKits.add("KITW04");
				widebodyKits.add("KITW05");
			}
			
			if (autosculptKits.contains("KIT00")) autosculptKits.remove("KIT00");
			if (widebodyKits.contains("KIT00")) widebodyKits.remove("KIT00");
			if (fullReplacementKits.contains("KIT00")) fullReplacementKits.remove("KIT00");
			
			
			long t = System.currentTimeMillis();
			FileInputStream fis = new FileInputStream(f);
			byte [] fileToBytes = new byte[(int)f.length()];
			fis.read(fileToBytes);
			fis.close();

			log.write("Geometry " + f.getPath() + " cached in " + (System.currentTimeMillis()-t) + " ms.\n");
			t = System.currentTimeMillis();
			
			ArrayList<Hash> hashes = generateHashes(carname, autosculptKits, widebodyKits, fullReplacementKits, exhausts);
			log.write("Part and texture hashes generated in " + (System.currentTimeMillis()-t) + " ms.\n");
			t = System.currentTimeMillis();
			
			
			
        	ByteBuffer bb = ByteBuffer.wrap(fileToBytes);
        	bb.position(184);
        	int i;
        	while ((i = bb.getInt()) != 71308032) {	//stop searching when 0x04401300 is found
        		for (Hash h : hashes) {
        			if (i == h.reversedBinHash) {
        				//part found
        				//TODO add it to a parts list (new class with kit, name, loda,b,c,d found) that checks the existence of all lods
        				break;
        			}
        		}
        		
				bb.getInt(); //jumps the blank 4 bytes between each part
			}
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			Hash potentialpart = null;
			int potentialpartoff = 0;
			byte step = 0;
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
			log.write("Part offsets scanned in " + (System.currentTimeMillis()-t) + " ms.\n");
			System.out.println("All parts found.");
			
			for (int i=0; i<replacements.size(); i++) {
				if (replacements.get(i).position == 0) {
					System.out.println("Warning : part " + replacements.get(i).part.label + " not found !");
					log.write("Warning : part " + replacements.get(i).part.label + " not found !\n");
					replacements.remove(i);
					i--;
				}
			}
			

			log.write("Starting to replace...\n");
			t = System.currentTimeMillis();
			
			byte cut = 0;
			
			for (Replacements r: replacements) {
				
				boolean[] val = new boolean[r.toReplace.length];
				
				System.out.println("Starting to replace part " + r.part + " at " + (r.position+32));
				
				off = r.position + 32;
				while (off<fileToBytes.length-4 
						&& (1514947658 != (((fileToBytes[off+4] & 0xFF) << 24) | ((fileToBytes[off+3] & 0xFF) << 16)
				        | ((fileToBytes[off+2] & 0xFF) << 8) | (fileToBytes[off+1] & 0xFF)))) {	//stop searching when 4a444c5a (JDLZ) is found OR WHEN EOF REACHED
					
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
									val[i] = true;
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
				
				for(int i=0; i<val.length; i++) {
					if(!val[i]) {
						
						if(bruteForceFixTripleCut) {
							System.out.print("Warning : attempting to bruteforce " + r.toReplace[i].label + " in part " + r.part.label + " at " + (r.position+32) + " !");
							log.write("Warning : attempting to bruteforce " + r.toReplace[i].label + " in part " + r.part.label + " at " + (r.position+32) + " !");
							
							
							
							
							
							
							potentialpart = null;
							off = r.position + 32;
							int bruteforcePos = 0;
							byte bruteForceBackup = 0;
							boolean firstreplace = true;
							while (off<fileToBytes.length-4 && off<r.position + 32 + 768) {	//stop searching when fuck you OR WHEN EOF REACHED
									
								// force replace the first matching occurence for the first one
								if (firstreplace && fileToBytes[off] == r.toReplace[i].reversedBinHashBytes[0]) {
									bruteforcePos = off;
									bruteForceBackup = fileToBytes[off];
									fileToBytes[off] = r.replacements[i].reversedBinHashBytes[0];
									firstreplace = false;
									System.out.print(" | First byte bruteforced at " + (off) + ".");
									log.write(" | First byte bruteforced at " + (off) + ".");
//										System.out.println("1st byte loop triggered for shader/texture/normalmap " + r.toReplace[i].label +" | "+off);
								}
								
								if (fileToBytes[off] == r.toReplace[i].reversedBinHashBytes[1]) { //ONLY 3 BYTES REMAINING IN PART
									potentialpart = r.toReplace[i];
									potentialpartoff = off;
									step = 1;
									cut = 0;
								}

								if (potentialpart != null) {
									if (step == 1 && fileToBytes[off] == r.toReplace[i].reversedBinHashBytes[2] && potentialpart == r.toReplace[i]) {
										//potential 3rd byte of a part to replace found
										if (cut == 0 && potentialpartoff == off-2) cut = 1;
										if (potentialpartoff <= off-3) {
											potentialpart = null;
											step = 1;
										}
										else {
											step = 2;
//												System.out.println("3rd byte loop triggered for shader/texture/normalmap " + r.toReplace[i].label +" | "+off);
										}
									}else if (step == 2 && fileToBytes[off] == r.toReplace[i].reversedBinHashBytes[3] && potentialpart == r.toReplace[i] /*misses one more condition ?*/) {
										//potential last byte of a part to replace found
										if (cut == 0 && potentialpartoff == off-3) cut = 2;
										if (potentialpartoff <= off-4) {
											potentialpart = null;
											step = 1;
										}
										if (potentialpart !=null) {
											potentialpart = null;
											fileToBytes[potentialpartoff] = r.replacements[i].reversedBinHashBytes[1];
											val[i] = true;
											System.out.println(" | Remnants found at " + (off) + " - potential success.");
											log.write(" | Remnants found at " + (off) + " - potential success.\n");
											switch (cut) {
											case 1:
												System.out.println("Hash cut after 1 byte");
												fileToBytes[potentialpartoff+2] = r.replacements[i].reversedBinHashBytes[2];
												fileToBytes[potentialpartoff+3] = r.replacements[i].reversedBinHashBytes[3];
												break;
											case 2:
												System.out.println("Hash cut after 2 bytes");
												fileToBytes[potentialpartoff+1] = r.replacements[i].reversedBinHashBytes[2];
												fileToBytes[potentialpartoff+3] = r.replacements[i].reversedBinHashBytes[3];
												break;
											case 0:
												System.out.println("Hash not cut");
												fileToBytes[potentialpartoff+1] = r.replacements[i].reversedBinHashBytes[2];
												fileToBytes[potentialpartoff+2] = r.replacements[i].reversedBinHashBytes[3];
											}
											step = 0;
											break;
										}
									}
								}
								off++;
							}
							
							if (firstreplace) {
								if(val[i]) {
									System.out.println(" - probably failed, no initial replacement.");
									log.write(" - probably failed, no initial replacement.\n");
								}else {
									System.out.println(" - probably failed, nothing found.");
									log.write(" - probably failed, nothing found.\n");
								}
							} else if (!val[i]) {
								System.out.println(" - probably failed, no further replacements. Reverting changes.");
								log.write(" - probably failed, no further replacements. Reverting changes.\n");
								fileToBytes[bruteforcePos] = bruteForceBackup;
							}
								
								
								
								
								
							
							
						} else {
							System.out.println("Warning : unable to find " + r.toReplace[i].label + " in part " + r.part.label + " at " + (r.position+32) + " !");
							log.write("Warning : unable to find " + r.toReplace[i].label + " in part " + r.part.label + " at " + (r.position+32) + " !\n");
						}
					}
				}
				
			}
			System.out.println("Finished replacing.");
			log.write("Finished replacing in " + (System.currentTimeMillis()-t) + " ms.\n");
			
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
			

			if (!s.equals(s.replaceAll("the zmod lock is real", ""))) {
				fileToBytes[0] = 1;
				System.out.println("The zmod lock is indeed real.");
			}
			
			
			
			

			log.write("Saving file...\n");
			t = System.currentTimeMillis();
			
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(fileToBytes);
			fos.close();
			System.out.println("File saved.");
			log.write("File " + f.getPath() + " saved in " + (System.currentTimeMillis()-t) + " ms.");
			log.close();
			
		} catch (FileNotFoundException e) {
			try {
				if (!new File("CTKFixer.ini").exists()) {
					BufferedWriter bw = new BufferedWriter(new FileWriter(new File("CTKFixer.ini")));
					
					bw.write("C:\\Program Files (x86)\\EA Games\\Need for Speed Undercover\\CARS\\AAA_AAA_AAA_01\\GEOMETRY.BIN\r\n"
							+ "AAA_AAA_AAA_01\r\n"
							+ "#	^ ALWAYS PUT THE CAR FOLDER THEN XNAME HERE\r\n"
							+ "#\r\n"
							+ "#	FORMATTING : put raw parts then textures/normalmaps/shaders to swap with new shaders\r\n"
							+ "#\r\n"
							+ "#	SMALL EXAMPLE :\r\n"
							+ "# \r\n"
							+ "#	KITW04_BODY_A BADGING_UNIVERSAL>DOORLINE BADGING>%_BADGING_KITW04\r\n"
							+ "#	KITW04_BODY_B BADGING_UNIVERSAL>DOORLINE BADGING>%_BADGING_KITW04\r\n"
							+ "#	KITW04_BODY_C BADGING_UNIVERSAL>DOORLINE BADGING>%_BADGING_KITW04\r\n"
							+ "#\r\n"
							+ "#	KIT01_BRAKELIGHT_LEFT %_KIT00_BRAKELIGHT_OFF>%_KIT01_BRAKELIGHT_OFF %_KIT00_BRAKELIGHT_ON>%_KIT01_BRAKELIGHT_ON\r\n"
							+ "#	KIT01_BRAKELIGHT_RIGHT %_KIT00_BRAKELIGHT_OFF>%_KIT01_BRAKELIGHT_OFF %_KIT00_BRAKELIGHT_ON>%_KIT01_BRAKELIGHT_ON\r\n"
							+ "#");
					bw.close();
					System.out.println("Missing configuration, it has been generated.");
				} else System.out.println("Invalid configuration, check the car's path.");
				
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static ArrayList<Hash> generateHashes(String carname, ArrayList<String> autosculptKits, ArrayList<String> widebodyKits, ArrayList<String> fullReplacementKits, int exhausts) {
		
		ArrayList<Hash> l = new ArrayList<Hash>();
		l.add(new Hash(carname));
		try {
			String part;
			BufferedReader br = new BufferedReader(new FileReader(new File("data/Parts_KIT00")));
			while ((part = br.readLine())!=null){
				l.add(new Hash(carname + "_KIT00_" + part + "_A"));
				l.add(new Hash(carname + "_KIT00_" + part + "_B"));
				l.add(new Hash(carname + "_KIT00_" + part + "_C"));
				l.add(new Hash(carname + "_KIT00_" + part + "_D"));
				for (int as=0; as<11; as++) {
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_A"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_B"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_C"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_D"));
				}
			}
			br = new BufferedReader(new FileReader(new File("data/Parts_anykit")));
			while ((part = br.readLine())!=null){
				l.add(new Hash(carname + "_KIT00_" + part + "_A"));
				l.add(new Hash(carname + "_KIT00_" + part + "_B"));
				l.add(new Hash(carname + "_KIT00_" + part + "_C"));
				l.add(new Hash(carname + "_KIT00_" + part + "_D"));
				for (int as=0; as<11; as++) {
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_A"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_B"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_C"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_D"));
				}
			}
			br = new BufferedReader(new FileReader(new File("data/Parts_exhaust")));
			while ((part = br.readLine())!=null){
				for (int as=0; as<exhausts; as++) {
					String nstr;
					if(as<10)nstr = "_0"+as;
					else nstr = "_"+as;
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_A"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_B"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_C"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_D"));
				}
			}
			
			for (String k : autosculptKits) {
				br = new BufferedReader(new FileReader(new File("data/Parts_anykit")));
				while ((part = br.readLine())!=null){
					l.add(new Hash(carname + "_" + k + "_" + part + "_A"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_B"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_C"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_D"));
					for (int as=0; as<11; as++) {
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_D"));
					}
				}
				br = new BufferedReader(new FileReader(new File("data/Parts_exhaust")));
				while ((part = br.readLine())!=null){
					for (int as=0; as<exhausts; as++) {
						String nstr;
						if(as<10)nstr = "_0"+as;
						else nstr = "_"+as;
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_C"));
					}
				}
			}

			for (String k : widebodyKits) {
				br = new BufferedReader(new FileReader(new File("data/Parts_anykit")));
				while ((part = br.readLine())!=null){
					l.add(new Hash(carname + "_" + k + "_" + part + "_A"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_B"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_C"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_D"));
					for (int as=0; as<11; as++) {
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_D"));
					}
				}
				br = new BufferedReader(new FileReader(new File("data/Parts_exhaust")));
				while ((part = br.readLine())!=null){
					for (int as=0; as<exhausts; as++) {
						String nstr;
						if(as<10)nstr = "_0"+as;
						else nstr = "_"+as;
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_C"));
					}
				}
			}
			
			for (String k : fullReplacementKits) {
				br = new BufferedReader(new FileReader(new File("data/Parts_KIT00")));
				while ((part = br.readLine())!=null){
					l.add(new Hash(carname + "_" + k + "_" + part + "_A"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_B"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_C"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_D"));
					for (int as=0; as<11; as++) {
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_D"));
					}
				}
				
				br = new BufferedReader(new FileReader(new File("data/Parts_anykit")));
				while ((part = br.readLine())!=null){
					l.add(new Hash(carname + "_" + k + "_" + part + "_A"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_B"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_C"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_D"));

					for (int as=0; as<11; as++) {
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_D"));
					}
				}

				br = new BufferedReader(new FileReader(new File("data/Parts_exhaust")));
				while ((part = br.readLine())!=null){
					for (int as=0; as<exhausts; as++) {
						String nstr;
						if(as<10)nstr = "_0"+as;
						else nstr = "_"+as;
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_C"));
					}
				}
			}
		
			br = new BufferedReader(new FileReader(new File("data/textures")));
			String tex;
			while ((tex = br.readLine())!=null){
				l.add(new Hash(carname + "_" + tex));
			}
			
			br = new BufferedReader(new FileReader(new File("data/gentextures")));
			while ((tex = br.readLine())!=null){
				l.add(new Hash(tex));
			}

			br = new BufferedReader(new FileReader(new File("data/shaders")));
			while ((tex = br.readLine())!=null){
				l.add(new Hash(tex));
			}

			br = new BufferedReader(new FileReader(new File("data/mpoints")));
			while ((tex = br.readLine())!=null){
				l.add(new Hash(tex));
				for (int as=0; as<11; as++) {
					l.add(new Hash(tex + "_T" + as));
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		return l;
	}
}