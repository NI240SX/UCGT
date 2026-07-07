package fr.ni240sx.ucgt.streamEditor;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import fr.ni240sx.ucgt.geometryFile.StreamChunksOffsets;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.*;
import fr.ni240sx.ucgt.streamEditor.streamBlocks.CARP;
import javafx.scene.Group;
import javafx.scene.Node;

public class WorldController {
	public File l8r;
	public File streaml8r;
	
	public List<Block> l8rBlocks = new ArrayList<>();

	public List<ChunksTrackPathZones.Zone> zones1;
	public List<ChunksMarkers.Marker> markers;
	public List<ChunksBoundaries.ChunkBoundary> boundaries;
	public List<ChunksElevationRules.ElevationRuleTriangle> elevationRuleTriangles;
	public List<ChunksRelations.ChunksRelated> relatedChunks;
	public List<BarrierSplines.Spline> barrierSplines;
	public List<StreamChunksOffsets.ChunkInfo> visualChunks;
	public CARP worldGrid;
	
    public List<StreamController> streamControllers = new ArrayList<>();
    public List<VisualController> visualControllers = new ArrayList<>();

	public AtomicInteger phaser = new AtomicInteger();
	
	public ConcurrentHashMap<Integer,VisualController.ModelController> models = new ConcurrentHashMap<>();
	public ConcurrentHashMap<Integer, VisualController.TextureController> textures = new ConcurrentHashMap<>();
	
    
	WorldController(File f) throws Exception{
		l8r = f;
		streaml8r = new File(f.getParent() + File.separator + "STREAM" + f.getName());
		
		ByteBuffer in;
		{
			var fis = new FileInputStream(f);
			in = ByteBuffer.wrap(fis.readAllBytes());
			in.order(ByteOrder.LITTLE_ENDIAN);
			fis.close();
		}
		
		Block block;

		if (Block.debug) System.out.println("\\/=== Blocks in region file ===\\/");
		while (in.hasRemaining()) {
//			var blockID = in.getInt(in.position());
//			var blockLength = in.getInt(in.position()+4);
//			var blockStart = in.position();
//			if ((blockID & 0x80000000) != 0) blockLength = 0; //ignore containers

			if ((block = Block.read(in)) != null) l8rBlocks.add(block);

			//in.position(blockStart+blockLength);

			/*
			   	12410300 ChunksEmpty
			   	47410380 ChunksZonesContainer
			   		4A410300 ChunksZones TRACK_PATH_ZONES zones ? -> cops stuff maybe ->>> MINIMAP_ICON_HIDING_SPOT from there
			   	46410300 ChunksData1 TRACK_POSITION_MARKERS positions -> NIS markers ->>> MINIMAP_ICON_PURSUIT_BREAKER from there
			   	50410380 container
			 * 		51410300 counts (400, 655) then list of chunk ids in int16 -> 1, 2, ..., 1001, 1002, ..., 2001, ..., 5001 (length 655*int16), then 0s (881*int16 but Z0 + 792*(Y+X+W+U))...
			   		52410300 ChunksZones2 MAY CONTAIN ELEVATION DATA HASH
			   		56410300 elevation data (hashes only 2 values BE6566DE and 927B806A (file hash))
			 * 		55410300 fixups or smth ??????? LS26/LS18/LS76/LS30 4*76-byte
			 * 				11 11 LSxx 3x0 1-int16 id-int16 7x0 0-int16 numIDs-int16 IDs-int16 list -> preculler something but doesn't match preculler data !
			   		53410300 ChunksRelated chunks to load ? list 11 11 0 int16 id int16 num int16 total int16 num then list of int16s (some ids are 22000+ /24000+ -> YXWU ???)
			 * 		50420300 alignment- 3 3 2 0 -> Weatherman
			 * 	01020304 no fucking clue (integrity check of some sort ?) -> seemingly completely ignored in code
			 * 	padding
			 * 	00B00380 SPLINE_PACK empty 
			 * 	padding
			   	10410300 TRACK_STREAMING_SECTION ChunksOffsets
			 * 	13410300 TRACK_STREAMING_DISC_BUNDLE empty
			 * 	11410300 TRACK_STREAMING_INFO 0 0
			 * 	padding
			 * 		@767504 dec
			 * 	08410300 SCENERY_OVERRIDE_INFOS -> chunkID int16 int16 ->>> ModelSlotPool smth
			 * 		@939460 dec
			 * 	09410300 SCENERY_GROUPS int16 -> BARRIER SPLINE INFO : 11 11 BARRIER_SPLINE_1004 int16 listlength(int16) type(int32) list of int16 -> ids still unknown
			 * 							first entry 13371 ids type 0 up to about 28600
			 * 	padding
			 * 		@1027552 dec
			 * 	0B410380 SCENERY_HEIRARCHIES container 
			 * 	list of 0C410300 ?????? -> hash, count, list of (hash, hash, 0, stuff(flags?))
			 * 	padding
			 * 	00B80300 CARP_WGRID CARP vertex data
			 * 			CDat
			 * 			Map
			 * 			RNgp
			 * 			CGrd
			 * 			cl
			 * 			RNhd
			 * 			RNnd
			 * 			RNpf
			 * 			RNrd
			 * 			RNsg
			 * 	padding
			 * 	00B90380 BOUNDS_PACK ?????? event sequencer/smackable/something ? plain text XOs_...
			 * 			contains Havok stuff -> Collisions, update collisions editor
			 * 	padding
			 * 	0A40A380 ANIMMAN_PACK empty
			 * 		@2508448 dec
			   	4D410300 TRACK_PATH_BARRIERS hash float2 float2 0 -> BARRIER SPLINES segments point and next point; 0 = sorting
			 * 	EOF
			 * 
			 */
		}
		if (Block.debug) System.out.println("/\\=== Blocks in region file ===/\\");
		for (var b : l8rBlocks) switch (b.getBlockID()){
		case ChunksTrackPathContainer:
			zones1 = ((ChunksZonesContainer)b).zones.zones;
			break;
		case ChunksPositionMarkers:
			markers = ((ChunksMarkers)b).markers;
			break;
		case VisibleSections:
			boundaries = ((ChunksStreamingContainer)b).boundaries.boundaries;
			elevationRuleTriangles = ((ChunksStreamingContainer)b).elevationRules.elevationRuleTriangles;
			relatedChunks = ((ChunksStreamingContainer)b).relatedChunks.chunksRelated;
			break;			
		case ChunksOffsets:
			visualChunks = ((StreamChunksOffsets)b).chunkInfos;
			break;
		case TrackPathBarriers:
			barrierSplines = ((BarrierSplines)b).splines;
			break;
		case CarpWorldGrid:
			worldGrid = ((CarpWorldGrid)b).CARP;
			break;
		default:
		}
		
//		System.out.println("%d boundaries".formatted(boundaries.size()));
//		System.out.println("%d preculler data".formatted(relatedChunks.size()));
		
		for (var linked : relatedChunks) {
			streamControllers.add(new StreamController(linked, boundaries, elevationRuleTriangles));
		}
		missing: for (var b : boundaries) {
			for (var linked : relatedChunks) if (linked.ID == b.ID) continue missing;
			//missing ones
			streamControllers.add(new StreamController(b, relatedChunks, elevationRuleTriangles));
		}
		for (var c : visualChunks) {
			visualControllers.add(new VisualController(c));
		}
		
	}
	
	
	public int renderWorldFromChunk(StreamController chunk, Group worldGroup) {
		final var phase = phaser.incrementAndGet();

		if (chunk == null) {
			javafx.application.Platform.runLater(() -> worldGroup.getChildren().clear());
			for (var c : visualControllers) c.unload(models, textures);
			return 0;
		}
		
		try {
			final var toDisplay = chunk.getVisualChunks(visualControllers);
			
			if (phaser.get() != phase) return 1; //skip if new chunk requested fast
			
			System.out.println("Starting to render section %s".formatted(chunk.toString()));
			
			for (var c : toDisplay) c.getContent(streaml8r, models, textures);
			
//			if (phaser.get() != phase) return 1; //skip if new chunk requested fast

//			var newNodes = new ArrayList<Node>();
//			for (var c : toDisplay) if (!c.isReady()) newNodes.addAll(c.renderContent(models, textures));

			var keptNodes = new ArrayList<Node>();
			for (var c : toDisplay) keptNodes.addAll(c.renderContent(models));

//			var latch = new CountDownLatch(1);
			
//			AtomicInteger fxStatus = new AtomicInteger(0);
			javafx.application.Platform.runLater(() -> {
				try {
				    for (Node n : keptNodes) {
				    	if (n == null) System.err.println("NULL node found!");
				    	else if (n.getParent() != null && n.getParent() != worldGroup) {
				            System.err.println("Node already has parent: " + n);
				        }
				    }

				    worldGroup.getChildren().setAll(keptNodes);
//				    worldGroup.getChildren().addAll(newNodes);
//					latch.countDown();
				} catch(Exception | Error e) {
					e.printStackTrace();
					worldGroup.getChildren().clear();
//					fxStatus.set(-1);
//					latch.countDown();
				}
			});
			
//			latch.await();
//			if (fxStatus.get() != 0) return fxStatus.get();
			
//			if (phaser.get() != phase) return 1; //skip if new chunk requested fast

			for (var c : visualControllers) if (!toDisplay.contains(c)) c.unload(models, textures);
			
			for (var c : toDisplay) c.updateMaterials(textures);

			
			return 0;
		} catch (Exception | Error e) {
			e.printStackTrace();
			return -1;
		}
	}


	public void doMemoryCleanup() {
		for (var c : visualControllers) c.unload(models, textures);
		models.clear();
		textures.clear();
		System.gc();
	}
	
}
