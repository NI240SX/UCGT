package fr.ni240sx.ucgt.geometryFile.part.mesh;

public enum VertexFormat {
	Pos0_Tex0("Pos0_Tex0", 24),
	Pos0_Tex0_Tex1("Pos0_Tex0_Tex1", 32),
	Pos0_Tex0s32("Pos0_Tex0s32", 20),
	Pos0_Col0_Tex0s32_Norm0s("Pos0_Col0_Tex0s32_Norm0s", 32),
	Pos0_Col0_Tex0s32_Norm0s_Tan0s("Pos0_Col0_Tex0s32_Norm0s_Tan0s", 40),
	Pos0_Col0_Tex0s32_Tex1s32("Pos0_Col0_Tex0s32_Tex1s32", 28),
	Pos0_Col0f4_Tex0_Norm0s("Pos0_Col0f4_Tex0_Norm0s", 48),
	Pos0_Col0f4_Tex0("Pos0_Col0f4_Tex0", 40),
	Pos0f3_Tex0s32_Norm0s("Pos0f3_Tex0s32_Norm0s", 24),
	Pos0_Tex0_Norm0_Tan0("Pos0_Tex0_Norm0_Tan0", 56),
	Pos0_Tex0s32_Norm0s("Pos0_Tex0s32_Norm0s", 28),
	Pos0_Col0_Tex0s32("Pos0_Col0_Tex0s32", 24),
	Pos0_Col0_Tex0s32_Tex1s32_Norm0s_Tan0s("Pos0_Col0_Tex0s32_Tex1s32_Norm0s_Tan0s", 44),
	Pos0_Col0_Tex0s32_Tex1s32_Norm0s("Pos0_Col0_Tex0s32_Tex1s32_Norm0s", 36),
	Pos0_Col0_Tex0s32_Tex1s32_Tex2s32_Norm0s_Tan0s("Pos0_Col0_Tex0s32_Tex1s32_Tex2s32_Norm0s_Tan0s", 48),
	Pos0f3_Norm0s("Pos0f3_Norm0s", 20),
	Pos0_Tex0s32_BlendWeight0_BlendIndices0_Norm0s_Tan0s("Pos0_Tex0s32_BlendWeight0_BlendIndices0_Norm0s_Tan0s", 44),
	//cars
	Pos0s10_Tex0s32_Col0_Norm0s_Tan0s("Pos0s10_Tex0s32_Col0_Norm0s_Tan0s", 32),
	Pos0s10_Tex0s1_Col0_Norm0s_Tan0s("Pos0s10_Tex0s1_Col0_Norm0s_Tan0s", 32),
	Pos0f3_Norm0f3_Col0_Tex0_Tan0f3("Pos0f3_Norm0f3_Col0_Tex0_Tan0f3",48),
	Pos0s10_Tex0s8_Col0_Norm0s_Tan0s("Pos0s10_Tex0s8_Col0_Norm0s_Tan0s",32),
	
	//X360 cars
	Pos0h4_Norm0d4n_Tan0d4n_Col0_Tex0h2("Pos0h4_Norm0d4n_Tan0d4n_Col0_Tex0h2", 24),
	
	//proshit world
	Pos0f3_Norm0f3_Col0_Tex0_Tex1("Pos0f3_Norm0f3_Col0_Tex0_Tex1",44),
	Pos0f3_Col0_Norm0f4_Tan0f3_Tex0("Pos0f3_Col0_Norm0f4_Tan0f3_Tex0",52),
	Pos0f3_Norm0f3_Col0_Tex0_Tex1_xf4_xb8("Pos0f3_Norm0f3_Col0_Tex0_Tex1_xf4_xb8",68),
	Pos0f3_Col0_Tex0_Tex1_Tan0s_Norm0s("Pos0f3_Col0_Tex0_Tex1_Tan0s_Norm0s",48),
	
	//carbob world
	Pos0f3_Norm0f3_Col0_Tex0("Pos0f3_Norm0f3_Col0_Tex0",36);

	private final String name;
	private final int length;
	
	private final int numTexChannels;
	private final boolean hasColor;
	private final boolean hasNormals;
	private final boolean hasTangents;
	
//	private final boolean has_short4n_10x_position;
//	private final boolean has_short2n_32x_texcoord;
	public final float positionFactor;
	public final float positionMin;
	public final float positionMax;
	public final float texcoordFactor;
	public final float texcoordMin;
	public final float texcoordMax;

//	public final List<VertexDataRead> inCalls;
//	public final List<VertexDataRead> outCalls;
	
	private VertexFormat(String name, int length) {
		this.name = name;
		this.length = length;

		if (name.contains("Tex2")) numTexChannels = 3;
		else if (name.contains("Tex1")) numTexChannels = 2;
		else if (name.contains("Tex0")) numTexChannels = 1;
		else numTexChannels = 0;

		hasColor = name.contains("Col0") || name.contains("BlendWeight0"); //partial support for NIS models' blend weight
		hasNormals = name.contains("Norm0");
		hasTangents = name.contains("Tan0");

//		has_short4n_10x_position = name.contains("Pos0s10");
		if (name.contains("Pos0s")) {
			int factor = 1;
			for (var s : name.split("_")) if (s.contains("Pos0s")) factor = Integer.parseInt(s.substring(5));
			positionFactor = Short.MAX_VALUE/factor;
			positionMin = Short.MIN_VALUE/factor;
			positionMax = Short.MAX_VALUE/factor;
		} else {
			positionFactor = 1.0f;
			positionMin = Float.NEGATIVE_INFINITY;
			positionMax = Float.POSITIVE_INFINITY;
		}
//		has_short2n_32x_texcoord = name.contains("Tex0s32");
		if (name.contains("Tex0s")) {
			int factor = 1;
			for (var s : name.split("_")) if (s.contains("Tex0s")) factor = Integer.parseInt(s.substring(5));
			texcoordFactor = Short.MAX_VALUE/factor;
			texcoordMin = Short.MIN_VALUE/factor;
			texcoordMax = Short.MAX_VALUE/factor;
		} else {
			texcoordFactor = 1.0f;
			texcoordMin = Float.NEGATIVE_INFINITY;
			texcoordMax = Float.POSITIVE_INFINITY;
		}
	}

	public String getName() {
		return name;
	}

	public int getLength() {
		return length;
	}

	public int getNumTexChannels() {
		return numTexChannels;
	}

	public boolean hasColor() {
		return hasColor;
	}

	public boolean hasNormals() {
		return hasNormals;
	}

	public boolean hasTangents() {
		return hasTangents;
	}

//	public boolean has_short4n_10x_position() {
//		return has_short4n_10x_position;
//	}
//
//	public boolean has_short2n_32x_texcoord() {
//		return has_short2n_32x_texcoord;
//	}

	static VertexFormat get(String string) {
		for (var format : values()) {
			if (format.getName().equals(string)) return format;
		}
		return Pos0s10_Tex0s32_Col0_Norm0s_Tan0s; //sounds like a good default...
	}
	
//	public interface VertexDataRead {
//		void call(Vertex v, ByteBuffer in);
//    }
}
