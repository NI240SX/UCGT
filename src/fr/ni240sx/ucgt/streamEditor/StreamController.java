package fr.ni240sx.ucgt.streamEditor;

import java.util.ArrayList;
import java.util.List;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.ChunksBoundaries;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.ChunksBoundaries.ChunkBoundary;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.ChunksElevationRules;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.ChunksElevationRules.ElevationRuleTriangle;
import javafx.scene.DepthTest;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.ChunksRelations;

public class StreamController {
	//controller for streamer chunk -> chunksRelated in code
	public short ID;
	public ChunksBoundaries.ChunkBoundary boundary;
	public ChunksRelations.ChunksRelated visualChunks;
	public List<ChunksElevationRules.ElevationRuleTriangle> elevationRuleTriangles;
	
	private MeshView flatMeshView;
	private PhongMaterial mat;
	
	public StreamController(ChunkBoundary b, List<ChunksRelations.ChunksRelated> chunksLoader, List<ElevationRuleTriangle> triangles) {
		boundary = b;
		ID = b.ID;
		for (var c : chunksLoader) if (c.ID == ID) {
			visualChunks = c;
		}
		if (boundary.elevationHash != 0) {
			elevationRuleTriangles = new ArrayList<>();
			for (var t : triangles) if (t.hash == boundary.elevationHash){
				elevationRuleTriangles.add(t);
			}
		}
	}
	
	public StreamController(ChunksRelations.ChunksRelated linked, List<ChunkBoundary> chunksBoundaries, List<ElevationRuleTriangle> triangles) {
		visualChunks = linked;
		ID = linked.ID;
		for (var c : chunksBoundaries) if (c.ID == ID) {
			boundary = c;
		}
		if (boundary != null && boundary.elevationHash != 0) {
			elevationRuleTriangles = new ArrayList<>();
			for (var t : triangles) if (t.hash == boundary.elevationHash){
				elevationRuleTriangles.add(t);
			}
		}
	}
	
	public List<VisualController> getVisualChunks(List<VisualController> all){
		var ls = new ArrayList<VisualController>();
		if (visualChunks.relatedChunkIDs.size() == 0) return ls;
		var chunks = new ArrayList<>(visualChunks.relatedChunkIDs);
		if (WorldEditorGUI.forceLoadY.isSelected()) {
			for (int i=0; i<chunks.size(); i++) {
				if (20000 <= chunks.get(i) && chunks.get(i) < 25000) chunks.set(i, (short) (24000 + chunks.get(i)%1000));
			}
		}
		for (var itm : all) if (chunks.contains(itm.ID) 
				|| itm.ID%1000 == 0
					) if (!ls.contains(itm)) ls.add(itm);
		return ls;
	}
	
	public MeshView getFlatMesh() {
		if (flatMeshView != null) return flatMeshView;
		if (boundary == null) return null;
//		if (visualChunks == null) return null;
		float depth = (visualChunks == null) ? -5 : 0;
		var mesh = new TriangleMesh();
		mesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
		mesh.getTexCoords().addAll(0, 0);

		mesh.getPoints().addAll((boundary.boundsMax[X]+boundary.boundsMin[X])/2, 
				(boundary.boundsMax[Y]+boundary.boundsMin[Y])/2, depth);
		for (int i=0; i<boundary.numPoints(); i++) {
			mesh.getPoints().addAll(boundary.points[i][X], boundary.points[i][Y], depth);
			mesh.getFaces().addAll(0, 0, (i)%(boundary.numPoints())+1, 0, (i+1)%(boundary.numPoints())+1, 0);
		}
		
		flatMeshView = new MeshView(mesh);
		flatMeshView.setMaterial(mat = new PhongMaterial());
		flatMeshView.setCullFace(CullFace.BACK);
		flatMeshView.setDrawMode(DrawMode.FILL);
		flatMeshView.setBlendMode(BlendMode.ADD);
		flatMeshView.setDepthTest(DepthTest.ENABLE);
		flatMeshView.setViewOrder((visualChunks == null) ? 2 : 1);
		
		resetMeshColor();
		
		return flatMeshView;
	}
	public void setMeshColor(Color col) {
		if (mat != null) mat.setDiffuseColor(col);
	}
	public void resetMeshColor() {
		if (mat != null) mat.setDiffuseColor(Color.hsb(ID, 0.5, 0.5, 0.2));
	}
	public void brightMeshColor() {
		if (mat != null) mat.setDiffuseColor(Color.hsb(ID, 1, 1));
	}
	public boolean isPanorama() {
		return visualChunks == null;
	}
	
	@Override
	public String toString() {
		return ((char)('A'+(ID/1000))+""+ID%1000);
	}
}
