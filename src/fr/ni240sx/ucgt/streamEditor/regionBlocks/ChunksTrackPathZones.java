package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Hash;
import fr.ni240sx.ucgt.streamEditor.StreamController;
import javafx.scene.DepthTest;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class ChunksTrackPathZones implements Block {

	public ArrayList<TrackPathZone> trackPathZones = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.ChunksTrackPathZones;}

	public ChunksTrackPathZones(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while(in.position() < blockStart+blockLength) {
			trackPathZones.add(new TrackPathZone(in));
		}
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		out.write(buf.array());

		for (var z : trackPathZones) out.write(z.save());
		
		buf = ByteBuffer.wrap(new byte[4]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(out.size()-8);

		var arr = out.toByteArray();
		arr[4] = buf.array()[0];
		arr[5] = buf.array()[1];
		arr[6] = buf.array()[2];
		arr[7] = buf.array()[3]; //writes the correct size
		
		return arr;

	}

	
	public static class TrackPathZone{
		public eTrackPathZoneType type;
		public float[] pos = new float[2];

		public float[] boundsMin = new float[2];
		public float[] boundsMax = new float[2];
		public float[][] points;
		
		public float[] dir = new float[2];
//		public int[] unk_i = new int[3];
		public float elev;
		public byte zoneSource, cachedIndex;
		public short visitInfo;
		
		public int[] data = new int[4];
		
		TrackPathZone(ByteBuffer in){
			type = eTrackPathZoneType.get(in.getInt());
			/*
			3 = tunnel, 
			4 = overpass, 
			6 = road names ???, 
			8 = hiding spots,
			9 = regions ? PH N/S/GCH/SH/Canyons/PC, 
			13 = forbidden locations for cops ?
			15 = highway names ???
			16 = interchanges, random parking lots and the whole gold coast highway ???
			
			50 = PC
			51 = PH
			52 = Canyons
			53 = SH
			 */
			
			pos[X] = in.getFloat(); //Position
			pos[Y] = in.getFloat();
			dir[0] = in.getFloat(); //Direction
			dir[1] = in.getFloat();
			
			elev = in.getFloat(); //Elevation
			zoneSource = in.get(); //ZoneSource
			cachedIndex = in.get(); //CachedIndex
			visitInfo = in.getShort(); //VisitInfo
			var pUserData = in.getInt(); //pUserData
	
			if (pUserData != 0) System.out.println("pUserData = "+pUserData);
			
			boundsMin[X] = in.getFloat();
			boundsMin[Y] = in.getFloat();
			boundsMax[X] = in.getFloat();
			boundsMax[Y] = in.getFloat();
	
			data[0] = in.getInt();
			data[1] = in.getInt();
			data[2] = in.getInt();
			data[3] = in.getInt();
	
			var numPoints = in.getShort();
			var MemoryImageSize = in.getShort(); //MemoryImageSize, thisDataLength = 68+numPoints*8
			if (MemoryImageSize != 68+numPoints*8) System.out.println("MemoryImageSize = "+MemoryImageSize+", expected = "+(68+numPoints*8));
			points = new float[numPoints][2];
			for (int i=0; i<numPoints; i++) {
				points[i][X] = in.getFloat();
				points[i][Y] = in.getFloat();
			}
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(68+numPoints()*8);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(type.id);
			bb.putFloat(pos[X]);
			bb.putFloat(pos[Y]);
			bb.putFloat(dir[0]);
			bb.putFloat(dir[1]);
//			bb.putInt(unk_i[0]);
//			bb.putInt(unk_i[1]);
//			bb.putInt(unk_i[2]);
			bb.putFloat(elev);
			bb.put(zoneSource);
			bb.put(cachedIndex);
			bb.putShort(visitInfo);
			bb.putInt(0); //pUserData
			
			bb.putFloat(boundsMin[X]);
			bb.putFloat(boundsMin[Y]);
			bb.putFloat(boundsMax[X]);
			bb.putFloat(boundsMax[Y]);
			bb.putInt(data[0]);
			bb.putInt(data[1]);
			bb.putInt(data[2]);
			bb.putInt(data[3]);
			bb.putShort((short) numPoints());
			bb.putShort((short)(68+numPoints()*8));
			for (int i=0; i<numPoints(); i++) {
				bb.putFloat(points[i][X]);
				bb.putFloat(points[i][Y]);
			}
			return bb.array();
		}
		
		public int numPoints() {
			return points.length;
		}

		
		private MeshView flatMeshView;
		private PhongMaterial mat;

		public MeshView getFlatMesh() {
			if (flatMeshView != null) return flatMeshView;
//			float depth = (visualChunks == null) ? -5 : 0;
			float depth = -type.id;
			var mesh = new TriangleMesh();
			mesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
			mesh.getTexCoords().addAll(0, 0);

			mesh.getPoints().addAll((boundsMax[X]+boundsMin[X])/2, 
					(boundsMax[Y]+boundsMin[Y])/2, depth);
			for (int i=0; i<numPoints(); i++) {
				mesh.getPoints().addAll(points[i][X], points[i][Y], depth);
				mesh.getFaces().addAll(0, 0, (i)%(numPoints())+1, 0, (i+1)%(numPoints())+1, 0);
			}
			
			flatMeshView = new MeshView(mesh);
			flatMeshView.setMaterial(mat = new PhongMaterial());
			flatMeshView.setCullFace(CullFace.NONE);
			flatMeshView.setDrawMode(DrawMode.FILL);
			flatMeshView.setBlendMode(BlendMode.ADD);
			flatMeshView.setDepthTest(DepthTest.ENABLE);
			flatMeshView.setViewOrder(-type.id);
			
			var t = new Tooltip("Type = "+type+
					"\nDirection = ("+dir[0]+";"+dir[1]+
					")\nElevation = "+elev+
					"\nZoneSource ="+zoneSource+
					"\nCachedIndex ="+cachedIndex+
					"\nVisitInfo ="+visitInfo+
					"\nData0 = "+((data[0] > 0 && data[0] < 5001) ? StreamController.getSectionIDAsString(data[0]) : Hash.getBIN(data[0]))+
					"\nData1 = "+((data[1] > 0 && data[1] < 5001) ? StreamController.getSectionIDAsString(data[1]) : Hash.getBIN(data[1]))+
					"\nData2 = "+((data[2] > 0 && data[2] < 5001) ? StreamController.getSectionIDAsString(data[2]) : Hash.getBIN(data[2]))+
					"\nData3 = "+((data[3] > 0 && data[3] < 5001) ? StreamController.getSectionIDAsString(data[3]) : Hash.getBIN(data[3]))
					);
			flatMeshView.setOnMouseEntered(e -> t.show(flatMeshView, e.getScreenX()+10, e.getScreenY()+10));
			flatMeshView.setOnMouseExited(e -> t.hide());
			
			resetMeshColor();
			
			return flatMeshView;		
		}

		public void setMeshColor(Color col) {
			if (mat != null) mat.setDiffuseColor(col);
		}
		public void resetMeshColor() {
			if (mat != null) mat.setDiffuseColor(Color.hsb(type.id, 0.5, 0.5, 0.2));
		}
		public void brightMeshColor() {
			if (mat != null) mat.setDiffuseColor(Color.hsb(type.id, 1, 1));
		}

	}
}