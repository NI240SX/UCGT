package fr.ni240sx.ucgt.damageFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;

public class Damage extends Block {

	float[] floats1_0;
	String carname;
	int[][] hashesTable;
	DamageMarker[] markers;
	DamageBody[] bodies;
	
	@Override
	public BlockType getBlockID() {return BlockType.Damage;}

	public Damage(ByteBuffer in) {
		//BIN BLOCK ID 0D460300
		
		in.order(ByteOrder.LITTLE_ENDIAN);
		
		var blockLength = in.getInt(); //starts like a BIN block
		var blockStart = in.position();

		var unkID = in.getInt();
		var unkLength = in.getInt();
		var totalFileLength = in.getInt(); //blockLength+8
		in.getInt(); //0
		in.position(in.position() + 12); //skip AA padding
		in.getInt(); //0
		in.position(in.position() + 76); //skip AA padding
		carname = Block.readString(in);
		in.position(in.position()+32-carname.length()-1);
		in.getInt(); //000101AA
		in.getInt(); //0
		in.getInt(); //8988883C
		in.getInt(); //0
		in.position(in.position()+212);
		in.position(in.position() + 16); //skip AA padding
		var hash0_1 = in.getInt(); //??
		var count0_1 = in.getInt(); //62, 58 on traffic
		count0_1 = in.getInt(); //62, 58 on traffic
		in.getInt(); //01AAAAAA
		in.getInt(); //0
		in.position(in.position()+600); //FEFFFFFF padding
//		while(in.getInt()==0xFEFFFFFF);
//		in.position(in.position()-4);
		
		in.getInt(); //0
		in.position(in.position() + 12); //skip AA padding
		in.getInt(); //0
		in.position(in.position() + 12); //skip AA padding
		in.getInt(); //0
		in.getInt(); //000000AA
		in.position(in.position() + 24); //skip 00 padding
		in.position(in.position() + 12); //skip AA padding
		
		
		//hashed stuff + floats, varies in length (1732 on several 2door racer cars, 
		// 1740 on copmidsize, 1748 on evo x, 1680 on trafsml)
		// offset in file 1088, offset in block 1080
		//64 bytes of stuff, variable count of floats one after another
		
		/*
		 * EVO X
		 * hashed stuff header
		 * D02DA903 21000000 21000000 01AAAAAA 	?,33,33,?	min=parts count +3 ?
		 * E09CA303 21000000 40000000 01AAAAAA 	?,33,64,?
		 * E0A8A303 22000000 40000000 01AAAAAA 	?,34,64,?
		 * 60909903 30758E00 80A79703 70549703
		 * number of floats : 374 or 375 (ends with 0x00000000) 374/11=34
		 * second hashed stuff header
		 * D05A7F00 52000000 80000000 01AAAAAA 	82,128
		 * B0817F00 03000000 10000000 01AAAAAA 	3,16
		 * B0367F00 1F000000 20000000 01AAAAAA 	31,32
		 * F0EB9703 50529703 30349703 70309703
		 * hashed shit -> PARTS ?
		 * 90F69303 908E8E00 30758E00 10738E00 
		 * 50E78D00 30608D00 703F8D00 50338D00 
		 * 60259A03 20179A03 000C9A03 E0009A03 
		 * C0F59903 A0EA9903 A0DE9903 40D69903 
		 * 00C89903 E0BC9903 C0B19903 A0A69903 
		 * 809B9903 60909903 C0889903 407D9903 
		 * 006F9903 E0639903 C0589903 A04D9903 
		 * 00AAA303 C0A6A303
		 * 
		 * 
		 * M3 E92
		 * hashed stuff header
		 * E0DD9A03 1D000000 1D000000 01AAAAAA 	29,29	min=parts count +3 ?
		 * 50159103 1D000000 20000000 01AAAAAA 	29,32
		 * 20F89303 1E000000 20000000 01AAAAAA 	30,32
		 * 50D68D03 10299203 D0F08F03 C09D8F03
		 * number of floats : 374 or 375 (ends with 0x00000000) 374/11=34
		 * second hashed stuff header
		 * D05A7F00 52000000 80000000 01AAAAAA	82,128
		 * B0817F00 03000000 10000000 01AAAAAA 	3,16
		 * B0367F00 1F000000 20000000 01AAAAAA 	31,32
		 * 40359003 A09B8F03 607E8F03 506C9203
		 * hashed shit
		 * 705C9203 F04A9203 903A9203 10299203 
		 * 30259203 B0889103 10679103 30619103 
		 * 10549103 F0489103 D03D9103 B0329103 
		 * 90279103 701C9103 70109103 10089103 
		 * D0F99003 80798F03 40678F03 50D68D03 
		 * E0418C03 907A7F00 800A9403 C0019403 
		 * C0299903 60239903
		 * 
		 * 
		 * TRFSML
		 * hashed stuff header
		 * 3020A103 10000000 10000000 01AAAAAA 	16,16	min=parts count +3 ?
		 * C0129903 10000000 10000000 01AAAAAA 	16,16
		 * B0B89803 10000000 10000000 01AAAAAA 	16,16
		 * 60B38D00 40838E00 20139903 10C09803
		 * number of floats : 374 or 375 (ends with 0x00000000) 374/11=34
		 * second hashed stuff header
		 * 405C7F00 4F000000 80000000 01AAAAAA 	79,128
		 * B0817F00 02000000 10000000 01AAAAAA 	2,16
		 * 20387F00 1D000000 20000000 01AAAAAA 	29,32
		 * 90579903 F0BD9803 40299703 60A38E00
		 * hashed shit
		 * 40838E00 20818E00 60E88D00 80E28D00 
		 * E0D98D00 60D08D00 E0C68D00 60BD8D00 
		 * 60B38D00 A0A88D00 E09D8D00 E0739E03 
		 * 60909D03
		 * 
		 * 
		 * COPMID
		 * hashed stuff header
		 * 0089A003 1F000000 1F000000 01AAAAAA 	31,31	min=parts count +3 ?
		 * C08D8E03 1F000000 20000000 01AAAAAA 	31,32
		 * 10779703 20000000 20000000 01AAAAAA 	32,32
		 * 50988D00 40629103 509C9503 40499503
		 * number of floats : 374 or 375 (ends with 0x00000000) 374/11=34
		 * second hashed stuff header
		 * D05A7F00 52000000 80000000 01AAAAAA 	82,128
		 * B0817F00 03000000 10000000 01AAAAAA 	3,16
		 * B0367F00 1F000000 20000000 01AAAAAA 	31,32
		 * C0E09503 20479503 A0359503 80319503
		 * hashed shit
		 * 20D19403 00C59403 80B39403 A0A49403 
		 * 60879403 90F39203 20719103 40629103 
		 * D0DD8F03 20478E03 30358E00 502F8E00 
		 * D0DB8D00 50D28D00 D0C88D00 50BF8D00 
		 * D0B58D00 50AC8D00 D0A28D00 50988D00 
		 * 908D8D00 50828D00 50788D00 D06D8D00 
		 * 50638D00 D0588D00 80A8A303 409DA303
		 * 
		 */
		
		
		

		System.out.println("first hashed stuff header at "+(in.position()));
		// FIRST HASHED STUFF HEADER
		var hash1_1 = in.getInt();
		// m3e92/evo x/trafsml sample values
		var count1_1_0_hashes1_0 = in.getInt(); //29	33	16
		var count1_1_1 = in.getInt(); //29	33	16
		in.getInt(); //01AAAAAA
		
		var hash1_2 = in.getInt();
		var count1_2_0 = in.getInt(); //29	33	16
		var count1_2_1 = in.getInt(); //32	64	16
		in.getInt(); //01AAAAAA
		
		var hash1_3 = in.getInt();
		var count1_3_0_hashes1_1 = in.getInt(); //30	34	16
		var count1_3_1 = in.getInt(); //32	64	16
		in.getInt(); //01AAAAAA
		
		var hash1_4_1 = in.getInt();
		var hash1_4_2 = in.getInt();
		var hash1_4_3 = in.getInt();
		var hash1_4_4 = in.getInt();
		
		floats1_0 = new float[375]; //=125*3
		for (int i=0;i<375;i++) floats1_0[i] = in.getFloat();
//		in.getInt(); //0

		System.out.println("second hashed stuff header at "+(in.position()));
		// SECOND HASHED STUFF HEADER ==> MPOINTS ?
		var hash1_5 = in.getInt();
		var count1_5_0_hashesTable = in.getInt(); 
		var count1_5_1 = in.getInt(); 
		in.getInt(); //01AAAAAA
		
		var hash1_6 = in.getInt();
		var count1_6_0_damageBodiesCount = in.getInt(); 
		var count1_6_1 = in.getInt(); 
		in.getInt(); //01AAAAAA
		
		var hash1_7 = in.getInt();
		var count1_7_0_markersCount = in.getInt(); 
		var count1_7_1 = in.getInt(); 
		in.getInt(); //01AAAAAA
		
		var hash1_8_1 = in.getInt();
		var hash1_8_2 = in.getInt();
		var hash1_8_3 = in.getInt();
		var hash1_8_4 = in.getInt();
		
		int[] hashes1_0 = new int[count1_1_0_hashes1_0  - 3];
		for (int i=0;i<count1_1_0_hashes1_0-3;i++) hashes1_0[i]=in.getInt();
		
		
		//end of this shit
		//skip long AAAAAAAA padding
		/* padding length and end address
		 * EVO X 4596	up to 7432
		 * M3 E92 4580	up to 7400
		 * COPMID 4588	up to 7416
		 * TRFSML 4240	up to 7008
		 */
		while(in.getInt()==0xAAAAAAAA);
		in.position(in.position()-4);
		
		//more goofy ahh hashes
		int[] hashes1_1 = new int[count1_3_0_hashes1_1];
		for (int i=0;i<count1_3_0_hashes1_1;i++) hashes1_1[i]=in.getInt();
		//alignment maybe, found on the copmid
		while(in.getInt()==0x0C0C0C0C);
		in.position(in.position()-4);
		//a float array that's always -.5; -.5; -.5; .5; .5; .5
		float[] floats1_1 = new float[6];
		for (int i=0;i<6;i++) floats1_1[i] = in.getFloat();
		//alignment maybe
		while(in.getInt()==0x0C0C0C0C);
		in.position(in.position()-4);
		
		// more AAAAAAAA padding
		while(in.getInt()==0xAAAAAAAA);
		in.position(in.position()-4);
		
		
		// some hashed data
		/*
		 * EVO X
		 * 1021A903 0A000000 10000000 01AAAAAA 00000000 	10,16
		 * B022A903 0A000000 10000000 01AAAAAA 00000000
		 * M3 E92
		 * 20D19A03 0A000000 10000000 01AAAAAA 00000000 
		 * C0D29A03 0A000000 10000000 01AAAAAA 00000000
		 * TRFSML
		 * 7013A103 0A000000 10000000 01AAAAAA 00000000 
		 * 1015A103 0A000000 10000000 01AAAAAA 00000000
		 * COPMID
		 * F016A403 0A000000 10000000 01AAAAAA 00000000 
		 * 9018A403 0A000000 10000000 01AAAAAA 00000000
		 */

		var hash2_1 = in.getInt();
		in.getInt(); //0A000000	10
		in.getInt(); //10000000	16
		in.getInt(); //01AAAAAA
		in.getInt(); //00000000
		var hash2_2 = in.getInt();
		in.getInt(); //0A000000
		in.getInt(); //10000000
		in.getInt(); //01AAAAAA
		in.getInt(); //00000000
		
		
		//normally there's only one padding here
		while(in.getInt()==0xAAAAAAAA);
		in.position(in.position()-4);
		
		
		System.out.println("damage parts table at "+(in.position())+", expected length "+(count1_5_0_hashesTable+1));
		//hashes table with 20-byte entries
		//usually 83 entries, trafsml 80 entries
		//4 data hashes and some ID
		//first entry might not be part of the table because the declared length is -1
		hashesTable = new int[count1_5_0_hashesTable+1][5];
		for (int i=0;i<count1_5_0_hashesTable+1;i++) {
			hashesTable[i][0] = in.getInt(); //part hash
			hashesTable[i][1] = in.getInt(); //attachment (CAR,BUMPER_FRONT,etc) or marker (for flares)
			hashesTable[i][2] = in.getInt();
			hashesTable[i][3] = in.getInt();
			hashesTable[i][4] = in.getInt(); //ID for type ? 0 marker, ?
			
			// possible IDs
			// 10827F00 -> most detachable panels, bumpers, doors, hood, trunk -> SkinnedBody ?
			// 40807F00 -> mirrors, spoiler, ?	-> RigidBody ?
			// 307C7F00 -> rigidly attached ?	-> RigidChassis ?
			
			//sample parts
			// 484F4F44 484F4F44 484F4F44 D0931820 10827F00
			// HOOD		HOOD		HOOD	?
			// 43484153 43415220 00000000 00000000 307C7F00
			// CHASSIS	CAR
			
			//sample flare 00000000 E1114CFD 00000000 00000000 00000000
			//						MARKER_HEADLIGHT_LEFT
		}
		
		
		//then markers and actual data for shit i guess
		/*
		 * EVO X : 62 markers and attachments (31 markers 2 populated attachments)
		 * M3 E92 : 31 markers 2 populated attachments
		 * TRFSML : 29 markers 2 populated attachments (no HEADLIGHTHAL)
		 * COPMID : 31 markers 2 populated attachments
		 */

		
		int hash3_0 = in.getInt(); //10827F00
		int hash3_1 = in.getInt(); //40807F00
		int hash3_2 = in.getInt(); //TODO may not be present ! (TRFSML)
		markers = new DamageMarker[count1_7_0_markersCount];
		for (int i=0;i<count1_7_0_markersCount;i++) {
			//new marker damage entry: marker + attachment
			markers[i] = new DamageMarker(in);
		}

		//alignment maybe
		while(in.getInt()==0x0C0C0C0C);
		in.position(in.position()-4);
		
		
		//damage bodies definitions
		//SkinnedBody, RigidBody, SkinnedChassis in any order
		bodies = new DamageBody[count1_6_0_damageBodiesCount];
		for (int i=0;i<count1_6_0_damageBodiesCount;i++) {
			//new marker damage entry: marker + attachment
			bodies[i] = new DamageBody(in);

			while(in.getInt()==0x0C0C0C0C);
			in.position(in.position()-4);
		}

		
		//each of the first data block is length 512
		//31 (?) such data blocks for parts on evo x (19392-35263)
		//and 2 more (MARKER_LP_R and smth) 35264-36287
		//-> 33 in total
		//16 such data blocks total on the trfsml
		//these data blocks always start with 0x00000000 AAAAAAAA AAAAAAAA AAAAAAAA

		var blocks1 = new Block1[count1_1_0_hashes1_0];
		for (int i=0;i<count1_1_0_hashes1_0;i++) {
			blocks1[i] = new Block1(in);
		}
		
		//now we get to the interesting stuff
		
		//think to unhash stuff like latches because there's some coming up later
		
		//second type of data block starting from 36288 length 1056 ?
		//blocks start with a unique id and contain a lot of floats
		//34 such blocks, from 36288 to 72191 (evo x)
		
		//maybe hinges or relations or smth (has two hashes eg CAR / DOOR_LEFT)
		
		var blocks2 = new Block2[count1_3_0_hashes1_1];
		for (int i=0;i<count1_3_0_hashes1_1;i++) {
			blocks2[i] = new Block2(in);
		}
		
		
		//large spot of 0x00000000 afterwards
		
		
		
		//only after that there seems to be specific data about parts that's not just headers
		//stuff has variable length, spots of 0x00000000 are either padding between parts or part of the parts
		//this data is probably kinds of meshes or smth
		//first part in 72720 (hood ?) (padding starts in 72192)
		//second in 82704 (bumper front ?)
		//etc
		//damage definition for the whole ass car in 125776 or 125784
		
		//footer data from either 257232 (?) or 261696
		
		while (in.position() < blockStart+blockLength-4) {
			in.getInt();
		}
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

}

class DamageMarker{

	String name;
	int childhash0;
	int childAttachmentPart; //vlt hash eg BUMPER_REAR, TRUNK, CAR
	int childhash2;
	String childname;

	int int1 = -1;
	int int2 = -1;
	int int3 = -1;
	int int4;
	int int5 = -1;
	
	public DamageMarker(ByteBuffer in) {
//		System.out.println("marker at "+(in.position()));
		name = Block.readString(in);
		//skip padding for string (length 128 with string)
		while(in.get()==(byte)0xCC);
		in.position(in.position()-1);

		childhash0 = in.getInt();
		childAttachmentPart = in.getInt(); //vlt hash eg BUMPER_REAR, TRUNK, CAR
		childhash2 = in.getInt();
		childname = Block.readString(in);
		//skip padding for string (length 128 with string)
		while(in.get()==(byte)0xCC);
		in.position(in.position()-1);

		int1 = in.getInt(); //-1
		int2 = in.getInt();	//-1
		int3 = in.getInt();	//-1 if light else a number eg 29
		int4 = in.getInt();	//varies, always used
		int5 = in.getInt();	//-1 if light else a number eg 30
		
	}
}

class DamageBody{

	String name;

	public DamageBody(ByteBuffer in) {
		name = Block.readString(in);
		//skip padding for string (length 128 with string)
		while(in.get()==0xAA);
		in.position(in.position()-1);
		
		in.getInt(); //00AAAAAA
		in.getFloat(); //0.01	0AD7233C
		in.getFloat(); //2.0	00000040
		in.getInt(); //5

		in.getInt(); //0
		in.getFloat(); //-9.80
		in.getInt(); //0
		in.getFloat(); //0.01
		
		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		in.getFloat(); //1.0
		
		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		
		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		in.getFloat(); //5.0

		in.getInt(); //0
		in.getInt(); //1
		in.getFloat(); //1.0
		in.getInt(); //0

		in.getInt(); //3
		in.getFloat(); //0.001
		in.getFloat(); //0.5
		in.getInt(); //1

		in.getFloat(); //50.0
		in.getFloat(); //0.1
		in.getFloat(); //1000.0
		in.getFloat(); //1000.0
		
		in.getInt(); //1
		in.getInt(); //0
		in.getInt(); //0
		in.getFloat(); //1.0
		
		in.getInt(); //0
		in.getFloat(); //1.0
		in.getInt(); //0
		in.getInt(); //0
		
		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		
		in.getInt(); //1
		in.getInt(); //0
		in.getFloat(); //0.2
		in.getFloat(); //-1.0
		
		in.getFloat(); //1.0
		in.getFloat(); //1.0
		in.getInt(); //4
		
	}
}

class Block1{
	
	int flags;
	int hash0_0, hash0_1, hash0_2, hash1_0, hash1_1, hash1_2;
	int count0, count1, count2;
	int partHash;
	short short1,short2,short3,short4;
	int intermInt;

	float[][] matrix0 = new float[4][4]; //matrix 0, 1, 3 and 4 often the same or almost
	float[][] matrix1 = new float[4][4];
	float[][] matrix2 = new float[4][4];
	float[][] matrix3 = new float[4][4];
	float[][] matrix4 = new float[4][4];
	
	public Block1(ByteBuffer in) {
		in.getInt(); //0
		in.getInt(); //AAAAAAAA
		in.getInt(); //AAAAAAAA
		in.getInt(); //AAAAAAAA
		
		flags=in.getInt(); //flags ? 10130000 mostly / 10130800 / 10110800 / 10110100 marker
		in.getShort(); //0
		in.getShort(); //FFFF
		in.getInt(); //0
		in.getInt(); //0

		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0

		in.getInt(); //AAAAAAAA
		in.getInt(); //AAAAAAAA
		in.getInt(); //AAAAAAAA
		in.getInt(); //AAAAAAAA

		//matrix0 (often same, sometimes moved, filled with AAAAAAAA if marker)
		matrix0[0][0]=in.getFloat(); //1.0
		matrix0[0][1]=in.getFloat(); //0.0
		matrix0[0][2]=in.getFloat(); //0.0
		matrix0[0][3]=in.getFloat(); //0.0

		matrix0[1][0]=in.getFloat(); //0.0
		matrix0[1][1]=in.getFloat(); //1.0
		matrix0[1][2]=in.getFloat(); //0.0
		matrix0[1][3]=in.getFloat(); //0.0

		matrix0[2][0]=in.getFloat(); //0.0
		matrix0[2][1]=in.getFloat(); //0.0
		matrix0[2][2]=in.getFloat(); //1.0
		matrix0[2][3]=in.getFloat(); //0.0

		matrix0[3][0]=in.getFloat(); //-0.0
		matrix0[3][1]=in.getFloat(); //0.0
		matrix0[3][2]=in.getFloat(); //0.0
		matrix0[3][3]=in.getFloat(); //1.0

		//matrix1 (often same, sometimes moved)
		matrix1[0][0]=in.getFloat(); //1.0
		matrix1[0][1]=in.getFloat(); //0.0
		matrix1[0][2]=in.getFloat(); //0.0
		matrix1[0][3]=in.getFloat(); //0.0

		matrix1[1][0]=in.getFloat(); //0.0
		matrix1[1][1]=in.getFloat(); //1.0
		matrix1[1][2]=in.getFloat(); //0.0
		matrix1[1][3]=in.getFloat(); //0.0

		matrix1[2][0]=in.getFloat(); //0.0
		matrix1[2][1]=in.getFloat(); //0.0
		matrix1[2][2]=in.getFloat(); //1.0
		matrix1[2][3]=in.getFloat(); //0.0

		matrix1[3][0]=in.getFloat(); //-0.0
		matrix1[3][1]=in.getFloat(); //0.0
		matrix1[3][2]=in.getFloat(); //0.0
		matrix1[3][3]=in.getFloat(); //1.0

		hash0_0=in.getInt(); //hash0 SOMETIMES 0, incl if marker
		count0=in.getInt(); //count0 SOMETIMES 0
		count1=in.getInt(); //count1 (total?) SOMETIMES 0
		in.getInt(); //01AAAAAA

		hash0_1=in.getInt(); //hash1 -> hash0 and hash1 are very close to each other on the same part (car sometimes) SOMETIMES 0
		count0=in.getInt(); //count0 SOMETIMES 0
		count1=in.getInt(); //count1 (total? seems to go by steps 0x10 0x20 0x40, can have multiple in the same car, on first part, 0x20 for Evo and E92, 0x10 for MID and SML)
		in.getInt(); //01AAAAAA

		hash0_2=in.getInt(); //hash2
		partHash=in.getInt(); //PART VLT HASH (eg HOOD, MARKER_LICENSE_PLATE_REAR)
		short1=in.getShort(); //indices likely shared across parts, reminds of material binding in car models, first index 0xFFFF and others 0 for marker
		short2=in.getShort(); //
		short3=in.getShort(); //
		short4=in.getShort(); //indices, sometimes 0xFFFF
		
		
		//matrix2 (wack ass, not normalized or anything, always changes, filled with 0 if marker)
		matrix2[0][0]=in.getFloat(); //1.0
		matrix2[0][1]=in.getFloat(); //0.0
		matrix2[0][2]=in.getFloat(); //0.0
		matrix2[0][3]=in.getFloat(); //0.0

		matrix2[1][0]=in.getFloat(); //0.0
		matrix2[1][1]=in.getFloat(); //1.0
		matrix2[1][2]=in.getFloat(); //0.0
		matrix2[1][3]=in.getFloat(); //0.0

		matrix2[2][0]=in.getFloat(); //0.0
		matrix2[2][1]=in.getFloat(); //0.0
		matrix2[2][2]=in.getFloat(); //1.0
		matrix2[2][3]=in.getFloat(); //0.0

		matrix2[3][0]=in.getFloat(); //-0.0
		matrix2[3][1]=in.getFloat(); //0.0
		matrix2[3][2]=in.getFloat(); //0.0
		matrix2[3][3]=in.getFloat(); //1.0
		

		//matrix3 (often same)
		matrix3[0][0]=in.getFloat(); //1.0
		matrix3[0][1]=in.getFloat(); //0.0
		matrix3[0][2]=in.getFloat(); //0.0
		matrix3[0][3]=in.getFloat(); //0.0

		matrix3[1][0]=in.getFloat(); //0.0
		matrix3[1][1]=in.getFloat(); //1.0
		matrix3[1][2]=in.getFloat(); //0.0
		matrix3[1][3]=in.getFloat(); //0.0

		matrix3[2][0]=in.getFloat(); //0.0
		matrix3[2][1]=in.getFloat(); //0.0
		matrix3[2][2]=in.getFloat(); //1.0
		matrix3[2][3]=in.getFloat(); //0.0

		matrix3[3][0]=in.getFloat(); //-0.0
		matrix3[3][1]=in.getFloat(); //0.0
		matrix3[3][2]=in.getFloat(); //0.0
		matrix3[3][3]=in.getFloat(); //1.0

		intermInt =in.getInt(); //000000FF, not always !
		in.getInt(); //AAAAAAAA
		in.getInt(); //AAAAAAAA
		in.getInt(); //AAAAAAAA
		

		//matrix4 (often same, may have position if marker)
		matrix4[0][0]=in.getFloat(); //1.0
		matrix4[0][1]=in.getFloat(); //0.0
		matrix4[0][2]=in.getFloat(); //0.0
		matrix4[0][3]=in.getFloat(); //0.0

		matrix4[1][0]=in.getFloat(); //0.0
		matrix4[1][1]=in.getFloat(); //1.0
		matrix4[1][2]=in.getFloat(); //0.0
		matrix4[1][3]=in.getFloat(); //0.0

		matrix4[2][0]=in.getFloat(); //0.0
		matrix4[2][1]=in.getFloat(); //0.0
		matrix4[2][2]=in.getFloat(); //1.0
		matrix4[2][3]=in.getFloat(); //0.0

		matrix4[3][0]=in.getFloat(); //0.0
		matrix4[3][1]=in.getFloat(); //0.0
		matrix4[3][2]=in.getFloat(); //0.0
		matrix4[3][3]=in.getFloat(); //1.0

		in.getInt(); //0, sometimes a hash, incl if marker
		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0, sometimes a hash

		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		hash1_0=in.getInt(); //hash

		in.getInt(); //0
		hash1_1=in.getInt(); //sometimes 0
		hash1_2=in.getInt(); //sometimes 0
		in.getInt(); //25 (0x19), sometimes 0, incl if marker

		in.getInt(); //25 (0x19), sometimes 0
		in.getInt(); //01AAAAAA
		in.getInt(); //0
		in.getInt(); //AAAAAAAA
	}
}

class Block2{
	
	int hash0, parentHash, partHash;

	float[][] matrix0_0 = new float[4][4]; //same matrix twice, usually
	float[][] matrix0_1 = new float[4][4];
	
	float[][] floats = new float[36][3];
	
	public Block2(ByteBuffer in) {
		hash0=in.getInt();//hash
		parentHash=in.getInt();//parent part hash (often CAR)
		partHash=in.getInt();//part hash
		in.getInt();//AAAAAAAA

		//goofy ahh matrix, changes a bit
		matrix0_0[0][0] = in.getInt();//0 -> matrix seems to convert from one axis convention to the other (X Y Z becomes -Y -Z X or smth)
		matrix0_0[0][1] = in.getInt();//0
		matrix0_0[0][2] = in.getInt();//1
		matrix0_0[0][3] = in.getInt();//0
		
		matrix0_0[1][0] = in.getInt();//-1
		matrix0_0[1][1] = in.getInt();//0
		matrix0_0[1][2] = in.getInt();//0
		matrix0_0[1][3] = in.getInt();//0
		
		matrix0_0[2][0] = in.getInt();//0
		matrix0_0[2][1] = in.getInt();//-1
		matrix0_0[2][2] = in.getInt();//0
		matrix0_0[2][3] = in.getInt();//0
		
		matrix0_0[3][0] = in.getInt();//non zero -> part origin/pivot ?
		matrix0_0[3][1] = in.getInt();//non zero
		matrix0_0[3][2] = in.getInt();//non zero
		matrix0_0[3][3] = in.getInt();//1

		
		matrix0_1[0][0] = in.getInt();//0 -> matrix seems to convert from one axis convention to the other (X Y Z becomes -Y -Z X or smth)
		matrix0_1[0][1] = in.getInt();//0
		matrix0_1[0][2] = in.getInt();//1
		matrix0_1[0][3] = in.getInt();//0
	
		matrix0_1[1][0] = in.getInt();//-1
		matrix0_1[1][1] = in.getInt();//0
		matrix0_1[1][2] = in.getInt();//0
		matrix0_1[1][3] = in.getInt();//0
		
		matrix0_1[2][0] = in.getInt();//0
		matrix0_1[2][1] = in.getInt();//-1
		matrix0_1[2][2] = in.getInt();//0
		matrix0_1[2][3] = in.getInt();//0
		
		matrix0_1[3][0] = in.getInt();//non zero -> part origin/pivot ?
		matrix0_1[3][1] = in.getInt();//non zero
		matrix0_1[3][2] = in.getInt();//non zero
		matrix0_1[3][3] = in.getInt();//1
		
		in.position(in.position()+64); //skips AAAAAAAA padding
		
		for (int i=0;i<36;i++) {
			floats[i][0]=in.getFloat(); //often 0
			floats[i][1]=in.getFloat(); //often 0
			floats[i][2]=in.getFloat(); //often 1000
		}
		
		// TODO TO BE CONTINUED
		
		
	}
}