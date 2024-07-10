package fr.ni240sx.ucgt.geometryFile.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.io.zModBlocks.*;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.sorters.PartSorterKitNameLod;

public class ZModelerZ3D {

	public static ArrayList<ZModInfo> info = new ArrayList<>();
	public static HashMap<Integer, ZModBlock> blocks = new HashMap<>();
	
	public static Scene scene;
	
	private static int UID; // for block creation
	
	public static boolean useCompression = true;
	
	public static void load(Geometry geom, File f) throws Exception{

		System.out.println("Note : Z3D support is complex to implement because of the lack of documentation, and therefore still in beta. If you face issues, please report them !");
		long time = System.currentTimeMillis();

		info.clear();
		blocks.clear();
		FileInputStream fis = new FileInputStream(f);
		byte [] arr = new byte[(int)f.length()];
		fis.read(arr);
		fis.close();
		var in = ByteBuffer.wrap(arr);
		in.order(ByteOrder.LITTLE_ENDIAN);
		var signature = String.valueOf(new char[] {(char)(in.get()), (char)(in.get()), (char)(in.get()), (char)(in.get())});
		var verMajor = in.getShort();
		var verMinor = in.getShort();
		var numDecl = in.getInt();
		System.out.println("Number of declarations : "+numDecl);
		switch (signature) {
		case "Z3DM":
			throw new Exception("ZModeler 1 file detected : "+f.getName()+". Not implemented, please use ZModeler 2 (version 2.2.6 recommended).");
		case "ZM2S":
			System.out.println("ZModeler 2 file detected : "+f.getName());
			if (!(verMajor==2 && verMinor==6)) System.out.println("This file was saved with a ZModeler2 version different from 2.2.6 (2."+verMajor+"."+verMinor+"). Issues may occur.");
			while (readBlock(in)) {
				//read data blocks until ENDF reached
			}		
			
			//read scene nodes within root and add a part if they're meshnodes
			//scene is the starting point -> iterate on simpleNode children -> associated Node or meshNode
			//we have material UIDs from the vertices which is enough to get their names back and that should be all
			//fairly simple !
			scene.rootNode.addToGeomRecursively(geom);

	        System.out.println("Z3D read in "+(System.currentTimeMillis()-time)+" ms.");
			time = System.currentTimeMillis();
			
			//postprocess geometry parts, add missing info	
			geom.rebuild();

	        System.out.println("Post-treatment done in "+(System.currentTimeMillis()-time)+" ms.");
//			for (var i : info) {
//				System.out.println("Info string : "+i.name+" = "+i.value+", editable = "+i.isEditable);
//			}
			
			break;
		default:
			System.out.println("Invalid ZModeler file.");
		}
		
	}
	
	public static void save(Geometry geom, String f) throws IOException{

		System.out.println("Note : Z3D support is complex to implement because of the lack of documentation, and therefore still in beta. If you face issues, please report them !");
		System.out.println("Preparing Z3D...");
		var time = System.currentTimeMillis();
		// first recreate a zmod scene
		// then write it to a file
		info.clear();

		info.add(new ZModInfo("Created with", "UCGT by ni240sx", false));
		info.add(new ZModInfo("Important note", "Z3D support in beta, please report issues !", false));
		
		blocks.clear();
		resetUID();
		
		geom.parts.sort(new PartSorterKitNameLod());
		
		//make blocks in order
		
		//GENERIC HEADER DATA
		
		var Workspace = new Workspace();
		scene = new Scene();
		var IUVNodeSwitcher = new IUVNodeSwitcher();
		var CUserSelectionDialog = new CUserSelectionDialog();
		var CRenderManager = new RenderDataList(); //makes the UIDs for the first 4 extensions in techniques (Techniques4Blocks)
		var CNodesBindService1 = new NodeBinds();
		var CNodesBindService2 = new NodeBinds();
		var CMaterialsService = new MaterialsList();
		for (var m : geom.materials) {
			ZMaterial zmat = new ZMaterial(m);
			CMaterialsService.materials.add(zmat);
			CMaterialsService.materialUIDs.add(zmat.UID);
		}
		var CTexturesService = new TexturePaths(); 
		for (var zmat : CMaterialsService.materials) {
			for (var t=0; t<zmat.binMat.TextureHashes.size(); t++) {
				var ztex = new ZTexture(zmat.binMat.TextureHashes.get(t).label+".dds",".");
				if (!CTexturesService.textures.contains(ztex)) {
					CTexturesService.textures.add(ztex);
					ztex.UID = ZModelerZ3D.createUID();
				}
				if (zmat.binMat.textureUsages.get(t) == TextureUsage.DIFFUSE ||
						zmat.binMat.textureUsages.get(t) == TextureUsage.ALPHA ||
						zmat.binMat.textureUsages.get(t) == TextureUsage.OPACITY) 
					//TODO add normalmaps and emissive textures ?
					zmat.textures.add(zmat.new TexLayer(CTexturesService.textures.get(CTexturesService.textures.indexOf(ztex)).UID));
			}
		}
		CMaterialsService.materials.add(0, new ZMaterial("UCGT_DEFAULT"));
		
		//SCENE, NODES AND PARTS
		scene.rootNode = scene.new simpleNode(new Node());
		//first meshnodes
		for (var p : geom.parts) {
			//TODO add an option to separate by kits ?
			scene.rootNode.childNodes.add(scene.new simpleNode(new MeshNode(p)));
		}
		for (var mpc : geom.mpointsPositions) {
			//TODO classic markers setup with globalized cubes, what about dummies attached to parts ?
			scene.rootNode.childNodes.add(scene.new simpleNode(new MeshNode(mpc)));
		}
//		scene.rootNode.childNodes.add(scene.new simpleNode(new MeshNode(geom.parts.get(0))));

		//then rendertechnique and polymesh
		for (var sn : scene.rootNode.childNodes) {
			if (sn.node.getClass() == MeshNode.class) {
				var mn = (MeshNode)sn.node;
				if (mn.binPart != null) { //binPart is null if it's being created for a marker
					mn.renderTechnique = new MeshRenderTechnique();
					mn.renderTechniqueUID = mn.renderTechnique.UID;
					mn.mesh = new ZMesh(mn.binPart, CMaterialsService.materials);
					mn.meshUID = mn.mesh.UID;
				} else { //marker
					mn.renderTechnique = new MeshRenderTechnique();
					mn.renderTechniqueUID = mn.renderTechnique.UID;
					mn.mesh = new ZMesh(mn.Xmin+0.1f, mn.Ymin+0.1f, mn.Zmin+0.1f);
					mn.meshUID = mn.mesh.UID;
				}
			}
		}

		//then render extensions
		for (var sn : scene.rootNode.childNodes) {
			if (sn.node.getClass() == MeshNode.class) {
				var mn = (MeshNode)sn.node;
				mn.renderTechnique.makeTechniques(); 
				/*
				 * this reserves IDs for 4 techniques per part, listed in renderTechnique.techniques : 
				 * rend::CSemitransparencyExtension
				 * rend::CHighBlendingExtension
				 * rend::CNoBlendingExtension
				 * rend::CNoCullExtension
				 */
			}
		}
		
		//LAST UIDS TO BE MADE, NOT DECLARED ANYWHERE
		CRenderManager.makeLastIDs(); //call after its declaration has been written and before writing its data (the 4 first IDs have declarations, these don't)
		CNodesBindService1.addNodes1();
		CNodesBindService2.addNodes2();
		
		
		//write everything to the blocks list
		ArrayList<ZModBlock> exportBlocks = new ArrayList<>();
		exportBlocks.add(CRenderManager);
		exportBlocks.add(CNodesBindService1);
		exportBlocks.add(CNodesBindService2);
		exportBlocks.add(CMaterialsService);
		exportBlocks.add(CTexturesService);
		exportBlocks.add(Workspace);
		exportBlocks.add(scene);
		exportBlocks.add(IUVNodeSwitcher);
		exportBlocks.add(CUserSelectionDialog);
		exportBlocks.add(CRenderManager.techniques); //4 blocks at once !
		exportBlocks.addAll(CMaterialsService.materials);
		//all Nodes/MeshNodes
		scene.rootNode.addNodesRecursively(exportBlocks);
		//all rendertechniques and polymeshes
		scene.rootNode.addMeshesRecursively(exportBlocks);
		//all render extensions
		scene.rootNode.addExtensionsRecursively(exportBlocks);
		
		//compress data, if necessary
		byte[] compressedData = null;
		int decompressedLength = 0;
		if (useCompression) {
			System.out.println("Writing blocks...");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			for (var b : exportBlocks) b.writeDeclaration(bos);
			for (var b : exportBlocks) b.writeData(bos);
//			bos.toByteArray();
			var arr = bos.toByteArray();
			decompressedLength = arr.length;
			System.out.println("Compressing data...");
			var compressedStream = new ByteArrayOutputStream();
			var compressor = new GZIPOutputStream(compressedStream);
			compressor.write(arr);
			compressor.close();
			compressedData = compressedStream.toByteArray();
		}
		
		//save all blocks one after the other to the file
		FileOutputStream fos = new FileOutputStream(new File(f.replace(".z3d", "")+".z3d"));
		ByteBuffer header = ByteBuffer.wrap(new byte[12]);
		header.order(ByteOrder.LITTLE_ENDIAN);
		header.put(new byte[] {0x5A, 0x4D, 0x32, 0x53, 0x02, 0x00, 0x06, 0x00});
		header.putInt(ZModBlock.getNumDecl(exportBlocks));
		fos.write(header.array()); //write ZM2S magic, versions and number of declarations
		writeInfo(fos);
		if (useCompression) {
			//GZIP header : 10 bytes
			//ZMod header : 2 bytes (try -9608 short)
			assert (compressedData != null);
			ByteBuffer blockHeader = ByteBuffer.wrap(new byte[47]);
			blockHeader.order(ByteOrder.LITTLE_ENDIAN);
			blockHeader.put(new byte[] {0x53, 0x42, 0x53, 0x54}); //SBST
			blockHeader.putInt(0);
			blockHeader.putInt(0);
			blockHeader.putInt(compressedData.length - 10 + 31); //length
			ZModBlock.putString(blockHeader, "core::io::CZIPStream");
			blockHeader.putInt(decompressedLength);	//decompressed length
			blockHeader.putShort((short) -9608); // zmod zip header
			fos.write(blockHeader.array());
			fos.write(compressedData, 10, compressedData.length-10);
			
		} else {
			System.out.println("Writing blocks...");
			for (var b : exportBlocks) b.writeDeclaration(fos);
			for (var b : exportBlocks) b.writeData(fos);
		}
		fos.write(new byte[] {0x45, 0x4E, 0x44, 0x46, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}); //ENDF block
		fos.close();
		System.out.println("Z3D file exported in "+(System.currentTimeMillis()-time)+" ms.");
		
	}
	
	public static void writeInfo(FileOutputStream fos) throws IOException {
		/* header : 16B
		 * numinfos : 4B
		 * then per info
		 * string1length+5B
		 * string2length+5B
		 * bool : 1B
		 */
		//precompute length
		var length = 20; //header + numinfos
		for (var i : info) {
			length += i.name.length()+i.value.length()+11; //2x4B for string lengths, 2x1B for nullending, 1 for bool
		}
		
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		block.put(new byte[] {0x49, 0x4E, 0x46, 0x4F}); //INFO
		block.putInt(0); //UID
		block.putInt(0); //version
		block.putInt(length-16);
		block.putInt(info.size());
		for (var i : info) {
			ZModBlock.putString(block, i.name);
			ZModBlock.putString(block, i.value);
			block.put((byte) (i.isEditable ? 1 : 0));
		}
		fos.write(block.array());
		
	}

	public static boolean readBlock(ByteBuffer in) throws Exception {
		
		String blockType="[UNABLE TO READ TYPE]";
		int blockUID = -1;
		int blockVersion = -1;
		byte[] decompressed = null;
		
		int blockStart=0, blockLength=0;
		
		try {
			blockType = String.valueOf(new char[] {(char)(in.get()), (char)(in.get()), (char)(in.get()), (char)(in.get())});
			blockUID = in.getInt();
			blockVersion = in.getInt();
			blockLength = in.getInt();
			blockStart = in.position();
		
			switch (blockType) {
			case "THMB":{
				//skip thumbnail data
				break;}
				
			case "INFO":{
				int numinfo = in.getInt();
				for (int i=0;i<numinfo;i++) {
					info.add(new ZModInfo(ZModBlock.readString(in), ZModBlock.readString(in), in.get()==1));
				}
				break;}
				
			case "SBST":{
				var substreamType = ZModBlock.readString(in);
				var substreamLength = in.getInt();
				if (substreamType.equals("core::io::CZIPStream")) {
					final int offset = 8;
					byte[] GZippedData = new byte[blockLength-substreamType.length()-9 +offset];
					//make a header
					GZippedData[0] = (byte) 0x1f; //header
					GZippedData[1] = (byte) 0x8b;
					GZippedData[2] = (byte) 8; //method
					GZippedData[3] = (byte) 0; //flags
					//6 bytes of data not read by the GZIPInputStream, but the zipped data itself is offset by two bytes in the substream : 0x78DA (-9608 short)
					
					in.get(GZippedData, offset, blockLength-substreamType.length()-9);
					try {
						GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(GZippedData));
						decompressed = gz.readNBytes(substreamLength); //there's no header and footer, this is the way to get around exceptions
						gz.close();
						
						ByteBuffer stream = ByteBuffer.wrap(decompressed);
						stream.order(ByteOrder.LITTLE_ENDIAN);
						while (stream.hasRemaining()) { //CAN CALL THIS ONLY BECAUSE THE SUBSTREAM LENGTH IS EXACTLY WHAT WE NEED
							readBlock(stream);
	//						System.out.println("block read from substream");
						}
					} catch (IOException e) {System.out.println("Error reading zipped substream : "+e.getMessage());}
				} else {System.out.println("Unsupported substream type : "+substreamType);}
				break;}
				
			case "DECL":{
				var declType = in.getInt();
				String name;
				if (declType == 64) {
					in.getInt();
					name = ZModBlock.readString(in);
				}else if (declType == 66){
					in.getInt();
					name = ZModBlock.readString(in);
				} else {
					name = ZModBlock.readString(in);
				}
				
				/*
				 * names and sequence here + versions + example UIDs in "m3 e36 no compression.z3d"
				 * blank file : UIDs start at 16843107
				 * for saving : start UIDs at 1413956437 ? that's UCGT displayed in a hex editor :trol:
				 * 
				 * block name							ex UID		ver	load
				 * services::CRenderManager				16845332	0	OK	all that changes is UIDs
				 * services::CNodesBindService			16845337	0	OK	all that changes is UIDs
				 * services::CNodesBindService			16845338	0	OK	all that changes is UIDs
				 * services::CMaterialsService			16845339	545	OK	(defines new UIDs inside hence the jump to textures service) just a list of UIDs, it's fine
				 * services::CTexturesService			16845387	547	OK	(immediately followed by data) it's fine too
				 * 
				 * ZModeler::Workspace					16845328	548	!!	clueless
				 * ZModeler::Scene						16845329	0	OK	easy, just a list of nodes
				 * shared::IUVNodeSwitcher				16845330	0	OK	it's empty lol
				 * core::tools::CUserSelectionDialog	16845331	0	OK	it's empty lol
				 * rend::CSemitransparencyExtension		16845333		-	(no data block) -> used in CRenderManager data
				 * rend::CHighBlendingExtension			16845334		-	(no data block)
				 * rend::CNoBlendingExtension			16845335		-	(no data block)
				 * rend::CNoCullExtension				16845336		-	(no data block)
				 * rend::CMaterial						16845340	545	OKish	should be fine, (one per material)	for some reason there's a jump in the IDs here, last mat 386, node 408 -> TEX UIDS
				 * scene::CNode							16845408	0	OK	it's fine, prolly always the same (main empty node, only one)
				 * 
				 * \/ \/ \/ FILE SPECIFIC BLOCKS \/ \/ \/
				 * 
				 * scene::CMeshNode						16845409	545	OKish	(one per part, all listed, has an embedded CNode)
				 * 
				 * rend::CStandardRenderIndxTechnique	16845720	0x01000002	OK	(one per part)
				 * scene::CPolyMesh						16845721	515	OK	(one per part, comes like so : rendertechnique,polymesh, next part, etc)
				 * 										/16846341			last index in file
				 * 
				 * rend::CSemitransparencyExtension		16847582		-	(last one cause they're a LOT, one per part, declared with no matching data blocks)
				 * rend::CHighBlendingExtension			16847583		-	-
				 * rend::CNoBlendingExtension			16847584		-	-
				 * rend::CNoCullExtension				16847585		-	(these don't have corresponding data blocks)
				 * 
				 */
				switch(name) {
				case "services::CRenderManager":
					blocks.put(blockUID, new RenderDataList(blockUID));
					break;
				case "services::CNodesBindService":
					blocks.put(blockUID, new NodeBinds(blockUID));
					break;
				case "services::CMaterialsService":
					blocks.put(blockUID, new MaterialsList(blockUID));
					break;
				case "services::CTexturesService":
					blocks.put(blockUID, new TexturePaths(blockUID));
					break;
				case "ZModeler::Workspace":
					//not read properly, it's only here to hide "Could not link data block UID 0x54434754 to an existing declaration !"
					blocks.put(blockUID, new Workspace());
					break;
				case "ZModeler::Scene":
					blocks.put(blockUID, scene = new Scene(blockUID));
					break;
				case "shared::IUVNodeSwitcher":
					//empty, 00000000 00000000 00000000 00000000
					blocks.put(blockUID, new IUVNodeSwitcher(blockUID));
					break;
				case "core::tools::CUserSelectionDialog":
					//empty, 00000000
					blocks.put(blockUID, new CUserSelectionDialog(blockUID));
					break;
				case "rend::CMaterial":
					blocks.put(blockUID, new ZMaterial(blockUID));
					break;
				case "scene::CNode":
					blocks.put(blockUID, new Node(blockUID));
					break;
				case "scene::CMeshNode":
					blocks.put(blockUID, new MeshNode(blockUID));
					break;
				case "rend::CStandardRenderIndxTechnique":
					blocks.put(blockUID, new MeshRenderTechnique(blockUID));
					break;
				case "scene::CPolyMesh":
					blocks.put(blockUID, new ZMesh(blockUID));
					break;
				case "rend::CHighBlendingExtension":
				case "rend::CNoBlendingExtension":
				case "rend::CNoCullExtension":
				case "rend::CSemitransparencyExtension":
					//skip these, they have no attached block
					break;
				default:
					System.out.println("Unknown block declaration : "+name);
				}
				break;}
				
			case "DATA":{
	//			System.out.println("Data block, id="+blockUID);
				if (blocks.get(blockUID) != null) blocks.get(blockUID).readData(in);
				else System.out.println("Could not link data block UID "+String.format("0x%08X",Integer.reverseBytes(blockUID))+" to an existing declaration !");
				break;}

			case "ENDF":{
				System.out.println("End of file");
				return false;}
			default:
				System.out.printf("Unknown block type : %s, skipping...\n", blockType);
			}
			in.position(blockStart+blockLength); //security in case a block is not read/misread
		} catch (Exception e) {
			System.out.println("An error occured reading "+blockType+" block version "+blockVersion+", UID "+String.format("0x%08X",Integer.reverseBytes(blockUID))+" : "+e.getMessage());
			try {
				if (blockStart != 0 && blockLength != 0) in.position(blockStart+blockLength);
				//try to proceed to the next block anyways
			} catch (@SuppressWarnings("unused") Exception e2) {
				//we are cooked
			}
			if (blockType.equals("SBST")) {
				try {
					System.out.println("Compressed input detected; dumping substream data to substreamdump.dat for further analysis");
					var fos = new FileOutputStream("substreamdump.dat");
					assert (decompressed != null);
					fos.write(decompressed);
					fos.close();
				} catch (Exception e2) {
					System.out.println("Failed to dump substream data  : "+e2.getMessage());
				}
			}
		}
			
		return true;
	}
	
	public static int createUID() {
		return UID++;
	}

	public static void resetUID() {
		UID = 1413956436; //UCGS, first ID will be UCGT
//		UID = 16845328;
	}
	
}


class ZModInfo {
	String name;
	String value;
	boolean isEditable;
	
	public ZModInfo(String n, String v, boolean e) {
		name = n;
		value = v;
		isEditable = e;
	}
}