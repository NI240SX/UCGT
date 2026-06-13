package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.util.ArrayList;
import java.util.List;

//public enum VertexFormat {
//	Pos0_Tex0("Pos0_Tex0", 24),
//	Pos0_Tex0_Tex1("Pos0_Tex0_Tex1", 32),
//	Pos0_Tex0s32("Pos0_Tex0s32", 20),
//	Pos0_Col0_Tex0s32_Norm0s("Pos0_Col0_Tex0s32_Norm0s", 32),
//	Pos0_Col0_Tex0s32_Norm0s_Tan0s("Pos0_Col0_Tex0s32_Norm0s_Tan0s", 40),
//	Pos0_Col0_Tex0s32_Tex1s32("Pos0_Col0_Tex0s32_Tex1s32", 28),
//	Pos0_Col0f4_Tex0_Norm0s("Pos0_Col0f4_Tex0_Norm0s", 48),
//	Pos0_Col0f4_Tex0("Pos0_Col0f4_Tex0", 40),
//	Pos0f3_Tex0s32_Norm0s("Pos0f3_Tex0s32_Norm0s", 24),
//	Pos0_Tex0_Norm0_Tan0("Pos0_Tex0_Norm0_Tan0", 56),
//	Pos0_Tex0s32_Norm0s("Pos0_Tex0s32_Norm0s", 28),
//	Pos0_Col0_Tex0s32("Pos0_Col0_Tex0s32", 24),
//	Pos0_Col0_Tex0s32_Tex1s32_Norm0s_Tan0s("Pos0_Col0_Tex0s32_Tex1s32_Norm0s_Tan0s", 44),
//	Pos0_Col0_Tex0s32_Tex1s32_Norm0s("Pos0_Col0_Tex0s32_Tex1s32_Norm0s", 36),
//	Pos0_Col0_Tex0s32_Tex1s32_Tex2s32_Norm0s_Tan0s("Pos0_Col0_Tex0s32_Tex1s32_Tex2s32_Norm0s_Tan0s", 48),
//	Pos0f3_Norm0s("Pos0f3_Norm0s", 20),
//	Pos0_Tex0s32_BlendWeight0_BlendIndices0_Norm0s_Tan0s("Pos0_Tex0s32_BlendWeight0_BlendIndices0_Norm0s_Tan0s", 44),
//	//cars
//	Pos0s10_Tex0s32_Col0_Norm0s_Tan0s("Pos0s10_Tex0s32_Col0_Norm0s_Tan0s", 32),
//	Pos0s10_Tex0s1_Col0_Norm0s_Tan0s("Pos0s10_Tex0s1_Col0_Norm0s_Tan0s", 32),
//	Pos0f3_Norm0f3_Col0_Tex0_Tan0f3("Pos0f3_Norm0f3_Col0_Tex0_Tan0f3",48),
//	Pos0s10_Tex0s8_Col0_Norm0s_Tan0s("Pos0s10_Tex0s8_Col0_Norm0s_Tan0s",32),
//	
//	//X360 cars
//	Pos0h4_Norm0d4n_Tan0d4n_Col0_Tex0h2("Pos0h4_Norm0d4n_Tan0d4n_Col0argb_Tex0h2", 24),
//	//PS3 cars
//	Pos0h4_Norm0cmp3n_Tan0cmp3n_Col0_Tex0h2("Pos0h4_Norm0cmp3n_Tan0cmp3n_Col0argb_Tex0h2", 24),
//	
//	//proshit world
//	Pos0f3_Norm0f3_Col0_Tex0_Tex1("Pos0f3_Norm0f3_Col0_Tex0_Tex1",44),
//	Pos0f3_Col0_Norm0f4_Tan0f3_Tex0("Pos0f3_Col0_Norm0f4_Tan0f3_Tex0",52),
//	Pos0f3_Norm0f3_Col0_Tex0_Tex1_xf4_xb8("Pos0f3_Norm0f3_Col0_Tex0_Tex1_xf4_xb8",68),
//	Pos0f3_Col0_Tex0_Tex1_Tan0s_Norm0s("Pos0f3_Col0_Tex0_Tex1_Tan0s_Norm0s",48),
//	
//	//carbob world
//	Pos0f3_Norm0f3_Col0_Tex0("Pos0f3_Norm0f3_Col0_Tex0",36);

public class VertexFormat {
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
	public final List<Component> components;
	
	public VertexFormat(String name) {
		this.name = name;
//		this.length = length;

//		if (name.contains("Tex2")) numTexChannels = 3;
//		else if (name.contains("Tex1")) numTexChannels = 2;
//		else if (name.contains("Tex0")) numTexChannels = 1;
//		else numTexChannels = 0;

		hasColor = name.contains("Col0") || name.contains("BlendWeight0"); //partial support for NIS models' blend weight
		hasNormals = name.contains("Norm0");
		hasTangents = name.contains("Tan0");

//		has_short4n_10x_position = name.contains("Pos0s10");
		if (name.contains("Pos0s")) {
			int factor = 1;
			for (var s : name.split("_")) if (s.contains("Pos0s")) factor = Integer.parseInt(s.substring(5));
			positionFactor = Short.MAX_VALUE*1.0f/factor;
			positionMin = Short.MIN_VALUE*1.0f/factor;
			positionMax = Short.MAX_VALUE*1.0f/factor;
		} else {
			positionFactor = 1.0f;
			positionMin = Float.NEGATIVE_INFINITY;
			positionMax = Float.POSITIVE_INFINITY;
		}
//		has_short2n_32x_texcoord = name.contains("Tex0s32");
		if (name.contains("Tex0s")) {
			int factor = 1;
			for (var s : name.split("_")) if (s.contains("Tex0s")) factor = Integer.parseInt(s.substring(5));
			texcoordFactor = Short.MAX_VALUE*1.0f/factor;
			texcoordMin = Short.MIN_VALUE*1.0f/factor;
			texcoordMax = Short.MAX_VALUE*1.0f/factor;
		} else {
			texcoordFactor = 1.0f;
			texcoordMin = Float.NEGATIVE_INFINITY;
			texcoordMax = Float.POSITIVE_INFINITY;
		}
		
		var comp = new ArrayList<Component>();
		
		for (var s : name.split("_")) {
			if (s.contains("Pos0s")) s = "Pos0s";
			if (s.contains("Tex0s")) s = "Tex0s";
			comp.add(Component.get(s));
			if (s.startsWith("x")) { //partially unknown
				var num = Integer.parseInt(s.substring(2)); //xb4 -> 4, xf2 -> 2, xb12 -> 12
				for (int i=1; i< num; i++) comp.add(comp.get(comp.size()-1));
			}
		}
		
		
		components = comp;
		
		var len = 0;
		var tc = 0;
		for (var c : components) {
			if (c.componentName.equals("Tex")) tc++;
			len += c.componentLength;
		}
		this.length = len;
		this.numTexChannels = tc;
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

//	static VertexFormat get(String string) {
//		for (var format : values()) {
//			if (format.getName().equals(string)) return format;
//		}
//		return Pos0s10_Tex0s32_Col0_Norm0s_Tan0s; //sounds like a good default...
//	}
	
//	public interface VertexDataRead {
//		void call(Vertex v, ByteBuffer in);
//    }
	
	public enum Component{
		pos_f3("Pos", 3*4, "f3"),
		pos_s("Pos", 4*2, "s"),
		pos_h4("Pos", 4*2,"h4"),
		pos_f4("Pos", 4*4), //always end with the "default" one or catastrophical failure

		tex_s("Tex", 2*2, "s"),
		tex_h2("Tex", 2*2, "h2"),
		tex_f2("Tex", 2*4),
		
		col_argb("Col", 4, "argb"),
		col_f4("Col", 4*4, "f4"),
		col_rgba("Col", 4),

		norm_f3("Norm", 3*4, "f3"),
		norm_s("Norm", 4*2, "s"),
		norm_d4n("Norm", 4, "d4n"),
		norm_cmp3n("Norm", 4, "cmp3n"),
		norm_f4("Norm", 4*4),

		tan_f3("Tan", 3*4, "f3"),
		tan_s("Tan", 4*2, "s"),
		tan_d4n("Tan", 4, "d4n"),
		tan_cmp3n("Tan", 4, "cmp3n"),
		tan_f4("Tan", 4*4),

		blendweight("BlendWeight", 4),
		blendindices("BlendIndices", 4),
		
		unknown_f("xf", 4),
		unknown_b("xb", 1),
		
		invalid("Invalid", 0);
		
		public final String componentName;
		public final String formatName;
		public final int componentLength;
		
		Component(String comp, int length){
			this(comp, length, null);
		}
		Component(String comp, int length, String form) {
			componentName = comp;
			formatName = form;
			componentLength = length;
		}
		
		public static Component get(String s) {
			for (var comp : values()) if (s.startsWith(comp.componentName) && (comp.formatName == null || s.replace(comp.componentName, "").contains(comp.formatName))) {
//				out += s + " -> "+comp.name()+"\n"; //DOES NOT WORK
				return comp;
			}
//			out += "Couldn't parse component type "+s+"\n";
			return invalid;
		}
	}
//	public static String out = "";
}
