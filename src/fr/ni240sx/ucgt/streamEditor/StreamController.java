package fr.ni240sx.ucgt.streamEditor;

import java.util.ArrayList;
import java.util.List;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.VisibleSectionBoundaries;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.VisibleSectionBoundaries.VisibleSectionBoundary;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.ChunksElevPolys;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.ChunksElevPolys.ElevPoly;
import javafx.scene.DepthTest;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.DrivableScenerySections;

public class StreamController {
	//controller for streamer chunk -> chunksRelated in code
	public short ID;
	public VisibleSectionBoundaries.VisibleSectionBoundary boundary;
	public DrivableScenerySections.DrivableScenerySection visualChunks;
	public List<ChunksElevPolys.ElevPoly> elevPolies0;
	public List<ChunksElevPolys.ElevPoly> elevPolies1;
	
	private MeshView flatMeshView;
	private PhongMaterial mat;
	
	public StreamController(VisibleSectionBoundary b, List<DrivableScenerySections.DrivableScenerySection> chunksLoader, List<ElevPoly> triangles) {
		boundary = b;
		ID = b.SectionNumber;
		for (var c : chunksLoader) if (c.SectionNumber == ID) {
			visualChunks = c;
		}
		figureElevPolys(triangles);
	}

	public void figureElevPolys(List<ElevPoly> triangles) {
		if (boundary.DepthNames[0] != 0) {
			elevPolies0 = new ArrayList<>();
			for (var t : triangles) if (t.DepthName == boundary.DepthNames[0]){
				elevPolies0.add(t);
			}
		}
		if (boundary.DepthNames[1] != 0) {
			elevPolies1 = new ArrayList<>();
			for (var t : triangles) if (t.DepthName == boundary.DepthNames[1]){
				elevPolies1.add(t);
			}
		}
	}
	
	public StreamController(DrivableScenerySections.DrivableScenerySection linked, List<VisibleSectionBoundary> chunksBoundaries, List<ElevPoly> triangles) {
		visualChunks = linked;
		ID = linked.SectionNumber;
		for (var c : chunksBoundaries) if (c.SectionNumber == ID) {
			boundary = c;
		}
		if (boundary != null) figureElevPolys(triangles);
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
	
	public static String getSectionIDAsString(int ID) {
		return ((char)('A'+(ID/1000))+""+ID%1000);
	}
}
