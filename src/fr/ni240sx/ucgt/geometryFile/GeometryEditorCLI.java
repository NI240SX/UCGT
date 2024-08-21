package fr.ni240sx.ucgt.geometryFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

import fr.ni240sx.ucgt.geometryFile.io.WavefrontOBJ;
import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;

public class GeometryEditorCLI {

	public static final String programVersion = "1.2.0.1";
	public static final String programBuild = "2024.08.21";
	
	public static Geometry geom;
//	public static ArrayList<String> commandsHistory = new ArrayList<>();
	
	public static void main(String[] args) {
		
		if (args.length >0) {
			//launching with arguments runs a special mode without user input
			String concatenatedArgs = "";
			for (var s : args) {
				if (s.contains(" ")) concatenatedArgs += "\"" +s + "\" ";
				else concatenatedArgs += s + " ";
			}
//			System.out.println("Arguments provided : "+concatenatedArgs);
			parseCommand(concatenatedArgs);
			
		} else {
			//launching without arguments runs the command-line utility
			
			showHelp();
			System.out.print("\n> ");
	
	        var in = new BufferedReader(new InputStreamReader(System.in));
	        String l;
	        
	        try {
				while (!(l = in.readLine()).equals("exit")) {
					parseCommand(l);
					System.out.print("\n> ");
				}
			} catch (Exception e) {
				System.out.println("An unexpected error happened while parsing user input, the program has to close.");
				e.printStackTrace();
			}
	        
		}

	}

	@SuppressWarnings("unused")
	public static void parseCommand(String l) {
		try{
			String src, dst;
			switch (l.split(" ")[0]) {
	
			case "help":
			case "?":
				showHelp();
				break;
				
			case "compile":
				compile(l);
				break;
				
			case "decompile":
				decompile(l);
				break;
				
			case "convert":
				convert(l);
				break;
				
			case "script":
				script(l);
				break;

			case "dump":
				dump(l);
				break;
				
			case "replace":
				replace(l);
				break;
				
			case "trol":
				System.out.println("The Grand Syndicate of Trolling agrees with your decision. Therefore I shall NOT nuke your coputer !");
				break;
				
			default:
				System.out.println("Unknown command : \""+l+"\". Type \"help\" for a list of commands.");
			}
			
			
		} catch (Exception e) {
			System.out.println("An unexpected error happened while parsing the command : "+e.getMessage());
			e.printStackTrace();
		}
	}

	private static void script(String l) {
		String src;
		try {
			l = l.substring(7);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
			} else {
				src = l.split(" ")[0];
			}
			
			try {
				System.out.println("Reading script...");
				BufferedReader script = new BufferedReader(new FileReader(new File(src)));
				String scriptCommand;
				while ((scriptCommand = script.readLine()) != null) if (!scriptCommand.isBlank()) {
					parseCommand(scriptCommand);
				}
				script.close();
//					Geometry.ctkConfigToUCGTConfig(new File(src), new File(dst));
				System.out.println("Script fully executed !");
				
			} catch (Exception e) {
				System.out.printf("Error reading script : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : script <file>");}
	}

	/**
	 * replace <source folder> <destination file> [file with blocks definitions]
	 */
	private static void replace(String l) {
		String src;
		String dst;
		String definitions = null;
		try {
			l = l.substring(8);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				l = l.substring(src.length()+2);
			} else {
				src = l.split(" ")[0];
				l = l.substring(src.length()+1);
			}
			if (!src.endsWith("\\")) src += "\\";
			
			//read second path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				dst = l.split("\"")[0];
				l = l.substring(dst.length()+1);
			} else {
				dst = l.split(" ")[0];
				l = l.substring(dst.length());
			}

			//read type if existing
			if (l.length() > 1) {
				l = l.substring(1);
				if (l.startsWith("\"")) {
					l = l.substring(1);
					definitions = l.split("\"")[0];
				} else {
					definitions = l.split(" ")[0];
				}
			}
			
			try {
				long t = System.currentTimeMillis();
				System.out.println("Starting to replace data...");
				Geometry.replaceInStream(src, dst, definitions);
				System.out.println("Data replaced successfully ! Took "+(System.currentTimeMillis()-t)+" ms.");
			} catch (Exception e) {
				System.out.printf("Error replacing geometries in file : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		} catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : replace <source folder> <destination file> [file with blocks definitions]");}
	}
	
	/**
	 * dump <source> <destination folder> [OBJ/Z3D/ALL] [filter]
	 */
	private static void dump(String l) {
		String src;
		String dst;
		String type = "ALL";
		String filter = null;
		try {
			l = l.substring(5);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				l = l.substring(src.length()+2);
			} else {
				src = l.split(" ")[0];
				l = l.substring(src.length()+1);
			}
			
			//read second path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				dst = l.split("\"")[0];
				l = l.substring(dst.length()+1);
			} else {
				dst = l.split(" ")[0];
				l = l.substring(dst.length());
			}
			if (!dst.endsWith("\\")) dst += "\\";

			//read type if existing
			if (l.length() > 1) {
				l = l.substring(1);
				if (l.startsWith("\"")) {
					l = l.substring(1);
					type = l.split("\"")[0];
					l = l.substring(type.length()+1);
				} else {
					type = l.split(" ")[0];
					l = l.substring(type.length());
				}
			}
			
			//read filter if existing
			if (l.length() > 1) {
				l = l.substring(1);
				if (l.startsWith("\"")) {
					l = l.substring(1);
					filter = l.split("\"")[0];
					l = l.substring(filter.length()+1);
				} else {
					filter = l.split(" ")[0];
					l = l.substring(filter.length());
				}
			}
			
			try {
				long t = System.currentTimeMillis();
				System.out.println("Starting to dump data...");
				Geometry.dumpStream(src, dst, type, filter);
				System.out.println("\nDumping successful ! Took "+(System.currentTimeMillis()-t)+" ms.");
			} catch (Exception e) {
				System.out.printf("Error dumping geometries from file : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		} catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : dump <source> <destination folder> [file type] [filter]");}
	}

	private static void convert(String l) {
		String src;
		String dst;
		try {
			l = l.substring(8);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				if (l.length()>src.length()+2) l = l.substring(src.length()+2);
				else l="";
			} else {
				src = l.split(" ")[0];
				if (l.length()>src.length()+1) l = l.substring(src.length()+1);
				else l="";
			}
			
				//read second path
			if (!l.isBlank()) {
				if (l.startsWith("\"")) {
					l = l.substring(1);
					dst = l.split("\"")[0];
//					l = l.substring(dst.length()+2);
				} else {
					dst = l.split(" ")[0];
//					l = l.substring(dst.length()+1);
				}
			} else dst = src.replace(".txt", ".ini");
			
			try {
				System.out.println("Reading CTK config...");
				Geometry.ctkConfigToUCGTConfig(new File(src), new File(dst));
				System.out.println("UCGT config saved.");
				System.out.println("Operation successful !");
				
			} catch (Exception e) {
				System.out.printf("Error converting config : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : convert <TXT CTK config> [INI UCGT config]");}
	}

	private static void decompile(String l) {
		String src;
		String dst;
		try {
			l = l.substring(10);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				l = l.substring(src.length()+2);
			} else {
				src = l.split(" ")[0];
				l = l.substring(src.length()+1);
			}
			
			//read second path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				dst = l.split("\"")[0];
//					l = l.substring(dst.length()+2);
			} else {
				dst = l.split(" ")[0];
//					l = l.substring(dst.length()+1);
			}
			
			try {
				long t = System.currentTimeMillis();
				System.out.println("Loading geometry...");
				geom = Geometry.load(new File(src));
				System.out.println("Geometry read in "+(System.currentTimeMillis()-t)+" ms."
						+ "\nSaving 3D model...");
				t = System.currentTimeMillis();
				try {
					if (dst.endsWith(".z3d")) {
						ZModelerZ3D.save(geom, dst);
						System.out.println("ZModeler scene saved in "+(System.currentTimeMillis()-t)+" ms.");
				        geom.writeConfig(new File(dst.replace(".z3d", "")+".ini"));
					} else {
						WavefrontOBJ.save(geom, dst);
						System.out.println("Wavefront saved in "+(System.currentTimeMillis()-t)+" ms.");
				        geom.writeConfig(new File(dst.replace(".obj", "")+".ini"));
					}
					System.out.println("Operation successful !");
					
				} catch (Exception e) {
					System.out.printf("Error saving 3D model : %s\n", e.getMessage());
					e.printStackTrace();
				}
			} catch (Exception e) {
				System.out.printf("Error loading Geometry : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : decompile <BIN source> <OBJ/Z3D output>");}
	}

	private static void compile(String l) {
		String src;
		String dst;
		try {
			l = l.substring(8);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				l = l.substring(src.length()+2);
			} else {
				src = l.split(" ")[0];
				l = l.substring(src.length()+1);
			}
			
			//read second path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				dst = l.split("\"")[0];
//					l = l.substring(dst.length()+2);
			} else {
				dst = l.split(" ")[0];
//					l = l.substring(dst.length()+1);
			}
			
			if (!new File(dst.replace(".BIN", "-BACKUP.BIN")).isFile() && new File(dst).isFile()) {
				//create vanilla backup if not existing
				new File(dst).renameTo(new File(dst.replace(".BIN", "-BACKUP.BIN")));
			}

			try {
				geom = Geometry.importFromFile(new File(src));
				try {
					geom.save(new File(dst));
					System.out.println("Operation successful !");
					
				} catch (Exception e) {
					System.out.printf("Error saving Geometry : %s\n", e.getMessage());
					e.printStackTrace();
				}
			} catch (Exception e) {
				System.out.printf("Error loading the provided file and converting it into a Geometry : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		} catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : compile <OBJ/Z3D source> <BIN output>");}
	}

	public static void showHelp() {
		System.out.printf("""
				 ===   UCGT by ni240sx   === 
				Geometry Editor Command-Line
				Version %s - %s
				
				Supported files :
					NFS Undercover .BIN and .BUN files
					Wavefront .OBJ 3D models
					ZModeler 2 .Z3D 3D scenes
					UCGT .INI configuration files
					CTK .TXT configuration files
				
				Commands :
					compile <OBJ/Z3D source> <BIN output>
					    - compile a 3D model and its corresponding config to a BIN file containing a single geometry
					    
					decompile <BIN source> <OBJ/Z3D output>
					    - extract a BIN file containing a single geometry to a 3D model and a configuration file
					    
					dump <source> <destination folder> [file type] [filter]
					    - extract all geometries contained into the source BIN/BUN file to the output folder
					      file type is whether to export OBJ or Z3D, if left blank both will be exported
					      filter will allow to export only matching blocks and names (X0, Road, Chop, XBu, etc)
					    
					replace <source folder> <destination> [blocks definitions]
					    - compile and replace all geometries in the destination with 3D models from the source folder
					      if needed, it can also update blocks definitions offsets (eg edit the map without unpacking)
					      in that case, [blocks definitions] will be the path to the L8R_MW2.BUN file
					    
					convert <TXT CTK config> [INI UCGT config]
					    - convert a CTK .txt config to an UCGT .ini config
					    
					script <file>
					    - load a script containing multiple of these commands

					help
					    - show this info
					    
					exit
					    - exits the program
				""", programVersion, programBuild);
	}
}
