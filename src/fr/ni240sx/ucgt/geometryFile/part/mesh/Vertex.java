package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.nio.ByteBuffer;

public class Vertex {
	public float posX=0;
	public float posY=0;
	public float posZ=0;
	public float posW=0;
	
	public float texU=0;
	public float texV=0;
	
	public float unknX=0;
	public float unknY=0;

	public float normX=1;
	public float normY=0;
	public float normZ=0;
	public float normW=1;

	public float norm2X=0;
	public float norm2Y=0;
	public float norm2Z=0;
	public float norm2W=0;
	
	public Vertex(ByteBuffer in) {
		posX = (float)(in.getShort())*10/32768;
		posY = (float)(in.getShort())*10/32768;
		posZ = (float)(in.getShort())*10/32768;
		posW = (float)(in.getShort())*10/32768;
		
		texU = (float)(in.getShort())/1024;
		texV = ((float)(in.getShort())/1024);
		
		unknX = (float)(in.getShort())*10/32768;
		unknY = (float)(in.getShort())*10/32768;

		normX = (float)(in.getShort())/32768;
		normY = (float)(in.getShort())/32768;
		normZ = (float)(in.getShort())/32768;
		normW = (float)(in.getShort())/32768;

		norm2X = (float)(in.getShort())/32768;
		norm2Y = (float)(in.getShort())/32768;
		norm2Z = (float)(in.getShort())/32768;
		norm2W = (float)(in.getShort())/32768;
		
//		System.out.println(this);
	}

	public void save(ByteBuffer out) {
		out.putShort((short) (posX*32768/10));
		out.putShort((short) (posY*32768/10));
		out.putShort((short) (posZ*32768/10));
		out.putShort((short) (posW*32768/10));

		out.putShort((short) (texU*1024));
		out.putShort((short) ((texV)*1024));

		out.putShort((short) (unknX*32768/10));
		out.putShort((short) (unknY*32768/10));

		out.putShort((short) (normX*32768));
		out.putShort((short) (normY*32768));
		out.putShort((short) (normZ*32768));
		out.putShort((short) (normW*32768));

		out.putShort((short) (norm2X*32768));
		out.putShort((short) (norm2Y*32768));
		out.putShort((short) (norm2Z*32768));
		out.putShort((short) (norm2W*32768));
	}

	@Override
	public String toString() {
		return "Vertex [posX=" + posX + ", posY=" + posY + ", posZ=" + posZ + ", posW=" + posW + ", texU=" + texU
				+ ", texV=" + texV + ", unknX=" + unknX + ", unknY=" + unknY + ", normX=" + normX + ", normY=" + normY
				+ ", normZ=" + normZ + ", normW=" + normW + ", norm2X=" + norm2X + ", norm2Y=" + norm2Y + ", norm2Z="
				+ norm2Z + ", norm2W=" + norm2W + "]";
	}
}
