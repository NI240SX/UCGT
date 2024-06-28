package fr.ni240sx.ucgt.geometryFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

import fr.ni240sx.ucgt.geometryFile.io.WavefrontOBJ;

public class GeometryEditorCLI {

	public static final String programVersion = "1.0.2";
	public static final String programBuild = "2024.06.28";
	
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

	public static void parseCommand(String l) {
		try{
			String src, dst;
			switch (l.split(" ")[0]) {
	
			case "help":
			case "?":
				showHelp();
				break;
				
			case "compile":
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
							
						} catch (Exception e) {System.out.printf("Error saving Geometry : %s\n", e.getMessage());}
					} catch (Exception e) {System.out.printf("Error loading the provided OBJ and converting it into a Geometry : %s\n", e.getMessage());}
					
				} catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : compile <OBJ source> <BIN output>");}
				break;
				
			case "decompile":
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
								+ "\nSaving OBJ...");
						t = System.currentTimeMillis();
						try {
							WavefrontOBJ.save(geom, dst);
							System.out.println("Wavefront saved in "+(System.currentTimeMillis()-t)+" ms.");
							System.out.println("Operation successful !");
							
						} catch (Exception e) {System.out.printf("Error saving OBJ : %s\n", e.getMessage());}
					} catch (Exception e) {System.out.printf("Error loading Geometry : %s\n", e.getMessage());}
					
				}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : decompile <BIN source> <OBJ output>");}
				break;
				
			case "convert":
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
						
					} catch (Exception e) {System.out.printf("Error converting config : %s\n", e.getMessage());}
					
				}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : convert <TXT CTK config> [INI UCGT config]");}
				break;
				
			case "script":
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
						
					} catch (Exception e) {System.out.printf("Error reading script : %s\n", e.getMessage());}
					
				}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : script <file>");}
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

	public static void showHelp() {
		System.out.printf("""
				 ===   UCGT by ni240sx   === 
				Geometry Editor Command-Line
				Version %s - %s
				
				Commands :
					compile <OBJ source> <BIN output>           - compile a Wavefront OBJ to a Geometry
					decompile <BIN source> <OBJ output>         - extract a Geometry to a Wavefront OBJ
					convert <TXT CTK config> [INI UCGT config]  - convert a CTK .txt config to an UCGT .ini config
					script <file>                               - load a script containing multiple of these commands

					help                                        - show this info
					exit                                        - exits the program
				""", programVersion, programBuild);
	}
}
