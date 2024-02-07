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
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import binstuff.Hash;

public class GeomCheck {
	
	public static String carname = "";

	public static void main(String[] args) {
		
		try {
			long t = System.currentTimeMillis();
			BufferedWriter log = new BufferedWriter(new FileWriter(new File("GeomCheck.log")));
			log.write("Initializing GeomCheck...\n");
			BufferedReader br = new BufferedReader(new FileReader(new File("GeomCheck.ini")));

			//Config read loop
			String l;
			File f = new File("");
			boolean file = false;
			boolean car = false;
			ArrayList<String> autosculptKits = new ArrayList<String>();
			ArrayList<String> widebodyKits = new ArrayList<String>();
			ArrayList<String> fullReplacementKits = new ArrayList<String>();
			int exhausts = 99;
			
			//checks
			boolean checkLODs = false; //check useless/missing LODs
			boolean checkMaterials = false; //check some part materials
			
			while ((l = br.readLine()) != null) {
				l = l.split("//")[0].split("#")[0]; // comments removal
				if (!l.isBlank() && l.contains("=")) { // reading a proper config
					// file config, mandatory
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
					// customization config, facultative
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
					// checks
					if(l.split("=")[0].strip().toLowerCase().equals("missing/useless lods")
							|| l.split("=")[0].strip().toLowerCase().equals("lods") 
							|| l.split("=")[0].strip().toLowerCase().equals("useless")) {
						if (l.split("=")[1].strip().equals("yes")
								|| l.split("=")[1].strip().equals("true")
								|| l.split("=")[1].strip().equals("1")) checkLODs = true;
					}
					if(l.split("=")[0].strip().toLowerCase().equals("materials")
							|| l.split("=")[0].strip().toLowerCase().equals("parts materials") 
							|| l.split("=")[0].strip().toLowerCase().equals("part materials")) {
						if (l.split("=")[1].strip().equals("yes")
								|| l.split("=")[1].strip().equals("true")
								|| l.split("=")[1].strip().equals("1")) checkMaterials = true;
					}
				}
			}

			if (!file) throw new Exception("File input not specified !");
			if (!car) throw new Exception("Car name not specified !");
			

			if(autosculptKits.isEmpty() && widebodyKits.isEmpty() && fullReplacementKits.isEmpty()) {
				for(int i=1;i<10;i++) fullReplacementKits.add("KIT0"+i);
				for(int i=10;i<100;i++) fullReplacementKits.add("KIT"+i);
				for(int i=1;i<10;i++) fullReplacementKits.add("KITW0"+i);
				for(int i=10;i<100;i++) fullReplacementKits.add("KITW"+i);
			}
			
			if (autosculptKits.contains("KIT00")) autosculptKits.remove("KIT00");
			if (widebodyKits.contains("KIT00")) widebodyKits.remove("KIT00");
			if (fullReplacementKits.contains("KIT00")) fullReplacementKits.remove("KIT00");
			
			for (String k : fullReplacementKits) {
				if (autosculptKits.contains(k)) autosculptKits.remove(k);
				if (widebodyKits.contains(k)) widebodyKits.remove(k);
			}

			log.write("Configuration loaded in " + (System.currentTimeMillis()-t) + " ms.\n");
			t = System.currentTimeMillis();
			
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
        		boolean found = false;
        		for (Hash h : hashes) {
        			if (i == h.reversedBinHash) {
        				found = true;

//            			System.out.println("Part found : Ox" + Integer.toHexString(i) + " = " + h.label);
//            			log.write("Part found : Ox" + Integer.toHexString(i) + " = " + h.label + "\n");
//        				System.out.print("Part found : "+h.label + " | ");
        				//part found
        				String partname = h.label.substring(0, h.label.length() - 2).replace(carname + "_", "");
        				boolean existing = false;
        				Part part = null;
        				for(Part p : Part.allParts) {
        					if ((p.kit + "_" + p.name).equals(partname)){
        						part = p;
        						existing = true;
        						break;
        					}
        				}
        				if (!existing) part = new Part(partname.split("_")[0], partname.replace(partname.split("_")[0] + "_", ""));
        				switch (h.label.split("_")[h.label.split("_").length-1]) {
        				case "A":
        					part.lodAExists = true;
        					break;
        				case "B":
        					part.lodBExists = true;
        					break;
        				case "C":
        					part.lodCExists = true;
        					break;
        				case "D":
        					part.lodDExists = true;
        					break;
        				case "E":
        					part.lodEExists = true;
        				}
//        				System.out.println(part);
        				break;
        			}
        		}
        		if (!found) {
        			System.out.println("Part not found : Ox" + Integer.toHexString(i));
        			log.write("Warning : unable to guess part Ox" + Integer.toHexString(i) + " at " + bb.position() + "\n");
        		}
				bb.getInt(); //jumps the blank 4 bytes between each part
			}
        	
        	Part.allParts.sort(new Comparator<Part>() {
				public int compare(Part p1, Part p2) {
					return (p1.kit + "_" + p1.name).compareTo(p2.kit + "_" + p2.name);
				}
        	});
//        	for(Part p : Part.allParts) {
//        		System.out.println(p);
//        	}
        	
			log.write("Parts guessed in " + (System.currentTimeMillis()-t) + " ms.\n");
			t = System.currentTimeMillis();
			
			hashes = new ArrayList<Hash>();
			for (Part p : Part.allParts) {
				if (p.lodAExists) hashes.add(new Hash(carname+"_"+p.kit+"_"+p.name+"_A"));
				if (p.lodBExists) hashes.add(new Hash(carname+"_"+p.kit+"_"+p.name+"_B"));
				if (p.lodCExists) hashes.add(new Hash(carname+"_"+p.kit+"_"+p.name+"_C"));
				if (p.lodDExists) hashes.add(new Hash(carname+"_"+p.kit+"_"+p.name+"_D"));
			}

			log.write("Part hashes optimized in " + (System.currentTimeMillis()-t) + " ms.\n");
			t = System.currentTimeMillis();
			
			// CHECKS
			
			if (checkLODs) { // useless/missing LODs
				System.out.println("Checking LODs");
				boolean noD = false;
				boolean dupeD = false;
				for (Part p : Part.allParts) {
					if (p.lodDExists && !p.kit.equals("KIT00")) log.write("[LODCHK] Useless LOD : " + p.kit + "_" + p.name + "_D\n");
					if (p.lodEExists) log.write("[LODCHK] Useless LOD : " + p.kit + "_" + p.name + "_E\n");
				}
				for (Part p : Part.allParts) {
					if (!p.lodAExists) log.write("[LODCHK] Missing LOD : " + p.kit + "_" + p.name + "_A\n");
					if (!p.lodBExists) log.write("[LODCHK] Missing LOD : " + p.kit + "_" + p.name + "_B\n");
					if (!p.lodCExists) log.write("[LODCHK] Missing LOD : " + p.kit + "_" + p.name + "_C\n");

					if (!p.lodDExists && p.kit.equals("KIT00") && p.name.equals("BODY")) noD = true;
					if (p.lodDExists && p.kit.equals("KIT00") && !p.name.equals("BODY")) dupeD = true;
				}
				if (noD) log.write("[LODCHK] Critical : missing KIT00_BODY_D \n");
				if (dupeD) log.write("[LODCHK] Warning : several KIT00 LOD D parts detected, please check that this is not caused by duplicated LOD C parts (potential clipping and optimization loss)\n");
				log.write("LODs checked in " + (System.currentTimeMillis()-t) + " ms.\n");
				t = System.currentTimeMillis();
			}
			
			if (checkMaterials) {
				System.out.println("Checking some common materials");
				
				//after the end of the parts list
				// something        first part offset    ?        ?        ?        ?
				//04401300 E0130000 FE39C300 801B0000 67340000 404A0000 00020000 00000000 then cycles with parts
				//second part       FF39C300 00500000 82110000 201B0000 00020000 00000000
				//etc               003AC300 00620000 82110000 201B0000 00020000 00000000 <- kit00 door left btw
				//                  013AC300 00740000 82110000 201B0000 00020000 00000000
				//
				//                  3F9D74FD 802A2900 E5160000 20240000 00020000 00000000 <- kitw01 brakelight left
				//                  409D74FD 80412900 53160000 A0220000 00020000 00000000
				// last part        419D74FD 00582900 52160000 A0220000 00020000 00000000 
				//                  00000000 38000000 00000000 00000000 00000000 00000000 00000000 00000000 
				//                  00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
				bb.getInt();
				bb.order(ByteOrder.LITTLE_ENDIAN);
				
				while ((i = bb.getInt()) != 0) {	//stop searching when 0x00000000 is found
					boolean val = false;
	        		for (Part p : Part.allParts) {
	        			if (i == p.lodAHash.binHash) {
	        				p.lodAPosition = bb.getInt();
//	        				System.out.println(p.name + " lod A at : " + p.lodAPosition);
	        				val = true;
	        				break;
	        			}
	        			if (i == p.lodBHash.binHash) {
	        				p.lodBPosition = bb.getInt();
//	        				System.out.println(p.name + " lod B at : " + p.lodBPosition);
	        				val = true;
	        				break;
	        			}
	        			if (i == p.lodCHash.binHash) {
	        				p.lodCPosition = bb.getInt();
//	        				System.out.println(p.name + " lod C at : " + p.lodCPosition);
	        				val = true;
	        				break;
	        			}
	        			if (i == p.lodDHash.binHash) {
	        				p.lodDPosition = bb.getInt();
//	        				System.out.println(p.name + " lod D at : " + p.lodDPosition);
	        				val = true;
	        				break;
	        			}
	        		}
	        		if (!val) bb.getInt();
					bb.position(bb.position()+16); //jumps the 4x4 bytes
				}
				bb.order(ByteOrder.BIG_ENDIAN);
				
				log.write("Part offsets scanned in " + (System.currentTimeMillis()-t) + " ms.\n");
				t = System.currentTimeMillis();
				
				// WINDOW_FRONT : check it has WINDOW_FRONT and doesn't have the rest
				// WINDOW_FRONT/REAR_LEFT/RIGHT : check the correct material
				// WINDOW_REAR : check it has WINDOW_FRONT and REAR_DEFROSTER and not the others
				
				// BODY_D : check there's no "fancy" shaders besides dullplastic and carskin eg magsilver
				
				
				
				
				
				
				
				log.write("Common materials checked in " + (System.currentTimeMillis()-t) + " ms.\n");
				t = System.currentTimeMillis();
			}
			
			
			
			
			
			
			
			
			/*
			
			
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
					}
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
							}else if (step == 2 && fileToBytes[off] == r.toReplace[i].reversedBinHashBytes[3] && potentialpart == r.toReplace[i] /*misses one more condition ?) {
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
									}else if (step == 2 && fileToBytes[off] == r.toReplace[i].reversedBinHashBytes[3] && potentialpart == r.toReplace[i] /*misses one more condition ?) {
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
			log.write("File " + f.getPath() + " saved in " + (System.currentTimeMillis()-t) + " ms.");*/
			log.close();
			
		} catch (FileNotFoundException e) {
			try {
				if (!new File("GeomCheck.ini").exists()) {
					BufferedWriter bw = new BufferedWriter(new FileWriter(new File("GeomCheck.ini")));
					
					bw.write( "File settings (mandatory)\r\n"
							+ "File =  C:\\Program Files (x86)\\EA Games\\Need for Speed Undercover\\CARS\\AAA_AAA_AAA_01\\GEOMETRY.BIN\\r\\n"
							+ "Car =   AAA_AAA_AAA_01\r\n"
							+ "\r\n"
							+ "Customization settings (optional)\r\n"
							+ "Autosculpt kits =               KIT01-KIT11\r\n"
							+ "Widebody kits =                 KITW01-KITW05\r\n"
							+ "Full replacement widebodies =   #none\r\n"
							+ "Exhausts amount =               5\r\n"
							+ "\r\n"
							+ "Checks (optional)\r\n"
							+ "Missing/useless LODs =          yes\r\n"
							+ "Parts materials =               yes\r\n"
							+ "\r\n");
					bw.close();
					System.out.println("Missing configuration, it has been generated.");
				} else System.out.println("Invalid configuration, check the car's path.");
				
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
				l.add(new Hash(carname + "_KIT00_" + part + "_E"));
				for (int as=0; as<11; as++) {
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_A"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_B"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_C"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_D"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_E"));
				}
			}
			br = new BufferedReader(new FileReader(new File("data/Parts_anykit")));
			while ((part = br.readLine())!=null){
				l.add(new Hash(carname + "_KIT00_" + part + "_A"));
				l.add(new Hash(carname + "_KIT00_" + part + "_B"));
				l.add(new Hash(carname + "_KIT00_" + part + "_C"));
				l.add(new Hash(carname + "_KIT00_" + part + "_D"));
				l.add(new Hash(carname + "_KIT00_" + part + "_E"));
				for (int as=0; as<11; as++) {
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_A"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_B"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_C"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_D"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_E"));
				}
			}
			br = new BufferedReader(new FileReader(new File("data/Parts_exhaust")));
			while ((part = br.readLine())!=null){
				for (int as=0; as<exhausts+1; as++) {
					String nstr;
					if(as<10)nstr = "_0"+as;
					else nstr = "_"+as;
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_A"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_B"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_C"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_D"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_E"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_T0_A"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_T0_B"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_T0_C"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_T0_D"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_T0_E"));
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
					for (int as=0; as<exhausts+1; as++) {
						String nstr;
						if(as<10)nstr = "_0"+as;
						else nstr = "_"+as;
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_D"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_D"));
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
					for (int as=0; as<exhausts+1; as++) {
						String nstr;
						if(as<10)nstr = "_0"+as;
						else nstr = "_"+as;
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_D"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_D"));
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
					for (int as=0; as<exhausts+1; as++) {
						String nstr;
						if(as<10)nstr = "_0"+as;
						else nstr = "_"+as;
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_D"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_D"));
					}
				}
			}
		
//			br = new BufferedReader(new FileReader(new File("data/textures")));
//			String tex;
//			while ((tex = br.readLine())!=null){
//				l.add(new Hash(carname + "_" + tex));
//			}
//			
//			br = new BufferedReader(new FileReader(new File("data/gentextures")));
//			while ((tex = br.readLine())!=null){
//				l.add(new Hash(tex));
//			}
//
//			br = new BufferedReader(new FileReader(new File("data/shaders")));
//			while ((tex = br.readLine())!=null){
//				l.add(new Hash(tex));
//			}
//
//			br = new BufferedReader(new FileReader(new File("data/mpoints")));
//			while ((tex = br.readLine())!=null){
//				l.add(new Hash(tex));
//				for (int as=0; as<11; as++) {
//					l.add(new Hash(tex + "_T" + as));
//				}
//			}
			
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