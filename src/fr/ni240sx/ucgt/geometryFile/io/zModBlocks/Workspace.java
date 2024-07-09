package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Workspace extends ZModBlock {

	int int11=1, int12=0, int13=655, int14=446;
	int int21=1, int22=1;
	byte byte23=0;
	int int24=4;
	
	ArrayList<WorkspaceViewport> views = new ArrayList<>();
	
	@Override
	public void readData(ByteBuffer in) {
		// TODO Auto-generated method stub
		//no need to load it
	}

	@Override
	public String getName() {
		return "ZModeler::Workspace";
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		/* header : 16B
		 * something : 4B
		 * blank : 4B
		 * nameLength+5B
		 */
		final var length = 29+getName().length();
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(64);
		block.putInt(0);
		ZModBlock.putString(block, getName());

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		//make viewports
		views.add(new WorkspaceViewport(143872, 669, 335, 1, 2, (short)48, new float[][] {
			{-1,0,0,1},
			{0,1,0,1},
			{1,0.661f,0,1},
			{125,0.785f,0,0}
		}));
		views.add(new WorkspaceViewport(144384, 774, 335, 1, 2, (short)49, new float[][] {
			{0,-1,0,1},
			{0,0,1,1},
			{0,1,0,1},
			{125,0.785f,0,0}
		}));
		views.add(new WorkspaceViewport(143616, 669, 530, 1, 2, (short)50, new float[][] {
			{0,0,1,1},
			{0,1,0,1},
			{0,0.83f,-1,1},
			{125,0.785f,0,0}
		}));
		views.add(new WorkspaceViewport(184320, 774, 530, 3, 2, (short)51, new float[][] {
			{3.08f,-1.13f,2.68f,1},
			{0.20f,0.96f,0.18f,1},
			{-3.17f,1.11f,-2.05f,1},
			{143.72f,0.785f,0.5f,8.59f}
		}));
		
		/* header : 16B
		 * stuff : 29B
		 * 30B per view (4 views normally)
		 * 4B for the number of view-specific data blocks
		 * then the cumulated length of each of these data blocks
		 */
		//precompute length
		var length = 49 + 30*views.size();
		for (var v : views) length += v.getDataLength();
		
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(548); //version
		block.putInt(length-16);

		block.putInt(int11);		block.putInt(int12);		block.putInt(int13);		block.putInt(int14);
		block.putInt(int21);		block.putInt(int22);		block.put(byte23);		block.putInt(int24);
		
		for (var v : views) v.writeDecl(block);
		
		block.putInt(views.size()); //number of data blocks
		for (var v : views) v.writeData(block);
				
		fos.write(block.array());
	}
	
	public class WorkspaceViewport {
	
		public int type;
		/* types :
		 * FRONT =			143616
		 * LEFT =			143872
		 * RIGHT = 			145920
		 * TOP =			144384
		 * PERSPECTIVE =	184320
		 * UV MAPPER = 		405760 (not implemented)
		 */
		public int width, height;
		public int status = 1; //focused = 3
		public int dataBlockID1 = 2;
		public short dataBlockID2 = 48;
		
		public float[][] matrix = new float[][] {
			{0.0f,		0.0f,		1.0f,		1.0f},	//matrix for FRONT
			{0.0f,		1.0f,		0.0f,		1.0f},	
	  		{-0.0f,		-0.0f,		-1.0f,		1.0f},
	  		{100.0f,	0.785f,		3.5f,		7.00f}
		};
		
		/**
		 * creates a viewport
		 * @param type : FRONT=143616, LEFT=143872, RIGHT=145920, TOP=144384, PERSPECTIVE=184320
		 * @param width in pixels
		 * @param height in pixels
		 * @param status (1, or 3 if focused)
		 * @param dataBlockID1 (2)
		 * @param dataBlockID2 (48-51)
		 * @param matrix a 4x4 transformation matrix
		 */
		public WorkspaceViewport(int type, int width, int height, int status, int dataBlockID1, short dataBlockID2,
				float[][] matrix) {
			super();
			this.type = type;
			this.width = width;
			this.height = height;
			this.status = status;
			this.dataBlockID1 = dataBlockID1;
			this.dataBlockID2 = dataBlockID2;
			this.matrix = matrix;
		}

		public void writeDecl(ByteBuffer bb) {
			bb.putInt(0);
			bb.putInt(type);
			bb.putInt(width);
			bb.putInt(height);
			bb.putInt(status);
			bb.putInt(dataBlockID1);
			bb.putShort(dataBlockID2);
			bb.putInt(0);
		}
		
		public int getDataLength() {
			switch(type) {
			case 143616:
			case 143872:
			case 145920:
			case 144384:
				return 297; //281+16
			case 184320:
				return 298; //282+16
			default:
				return 122;
			}
		}

		public void writeData(ByteBuffer bb) {
			bb.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
			bb.putInt(0); //UID
			bb.putInt(0); //version
			bb.putInt(getDataLength()-16); //size of englobing data block, level 0
			
			bb.putInt(dataBlockID1);
			bb.putShort(dataBlockID2);
			
			//data block level 1
			bb.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
			bb.putInt(0); //UID
			bb.putInt(0); //version
			bb.putInt(getDataLength()-38); //size

			//data block level 2
			bb.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
			bb.putInt(0); //UID
			bb.putInt(0); //version
			bb.putInt(getDataLength()-54); //size

			for (int i=0;i<4;i++) for (int j=0;j<4;j++) bb.putFloat(matrix[i][j]);
			
			//view layers
			switch (type) {
			case 143616:
			case 143872:
			case 145920:
			case 144384:
				//flat views
				bb.putInt(3); //3 layers
				makeTripodLayer(bb);
				makeBackgroundImageLayer(bb);
				makeGridLayer(bb);
				break;
			case 184320:
				//3D view
				bb.putInt(3); //3 layers
				makeTripodLayer(bb);
				makeBackgroundImageLayer(bb);
				makeGrid3DLayer(bb);
				break;
			default:
				bb.putInt(0); //no layers
				break;
			}
			
		}
		
		public void makeTripodLayer(ByteBuffer bb) {
			/*
				27 core::ui::CAxesTripodLayer
  				DATA block	id=0	version=0	length=5
  					1b	0i
			 */
			ZModBlock.putString(bb, "core::ui::CAxesTripodLayer");
			bb.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
			bb.putInt(0); //UID
			bb.putInt(0); //version
			bb.putInt(5); //length
			bb.put((byte) 0); // tripod enabled ?
			bb.putInt(0); //tripod size (0,1 or 2 i guess)
		}
		
		public void makeBackgroundImageLayer(ByteBuffer bb) {
			/*
	  			32 core::ui::CBackgroundImageLayer
				DATA block	id=0	version=0	length=27
					0b(1)	0b	1b	0b	0b	0b	0b
					-1f		1f	1f	-1f	0f
			 */
			ZModBlock.putString(bb, "core::ui::CBackgroundImageLayer");
			bb.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
			bb.putInt(0); //UID
			bb.putInt(0); //version
			bb.putInt(27); //length
			bb.put((byte) 0); // enabled ?
			bb.put((byte) 0);
			bb.put((byte) 1);
			bb.put((byte) 0);
			bb.put((byte) 0);
			bb.put((byte) 0);
			bb.put((byte) 0);
			bb.putFloat(-1.0f);
			bb.putFloat(1.0f);
			bb.putFloat(1.0f);
			bb.putFloat(-1.0f);
			bb.putFloat(0.0f);
		}

		public void makeGridLayer(ByteBuffer bb) {
			/*
				21 core::ui::CGridLayer
  				DATA block	id=0	version=0	length=3
  					1b	1b	1b
			 */
			ZModBlock.putString(bb, "core::ui::CGridLayer");
			bb.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
			bb.putInt(0); //UID
			bb.putInt(0); //version
			bb.putInt(3); //length
			bb.put((byte) 1); // enabled
			bb.put((byte) 1); // draw minor
			bb.put((byte) 1); // draw major
		}

		public void makeGrid3DLayer(ByteBuffer bb) {
			/*
				23 core::ui::CGrid3DLayer
  				DATA block	id=0	version=0	length=2
  					0b(1)	0b(1)
			 */
			ZModBlock.putString(bb, "core::ui::CGrid3DLayer");
			bb.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
			bb.putInt(0); //UID
			bb.putInt(0); //version
			bb.putInt(2); //length
			bb.put((byte) 1); // draw minor
			bb.put((byte) 1); // draw major
		}
	}
}
