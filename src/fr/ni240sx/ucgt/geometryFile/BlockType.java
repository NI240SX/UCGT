package fr.ni240sx.ucgt.geometryFile;

public enum BlockType {
	Padding(0x00000000, "Padding"),
	
	UCGT_Data(0x55434754, "UCGT Data"),
	
	Geometry(0x00401380, "Geometry"),
	Geom_Header(0x01401380, "Geom_Header"),
	Geom_Info(0x02401300, "Geom_Info"),
	Geom_PartsList(0x03401300, "Geom_PartsList"),
	Geom_PartsOffsets(0x04401300, "Geom_PartsOffsets"),
	Geom_UNKNOWN(0x08401380, "Geom_???"),

	CompressedData(0x22114455, "CompressedData"),
	
	Part(0x10401380, "Part"),
	Part_Header(0x11401300, "Part_Header"),
	Part_TexUsage(0x12401300, "Part_TexUsage"),
	Part_Strings(0x15401300, "Part_Strings"),
	Part_ShaderList(0x13401300, "Part_ShaderList"),
	Part_MPoints(0x1A401300, "Part_MPoints"),
	Part_Mesh(0x00411380, "Part_Mesh"),
	Part_Mesh_Info_PC(0x00491300, "Part_Mesh_Info_PC"),
	Part_Mesh_Info_X360(0x004D1300, "Part_Mesh_Info_X360"),
	Part_Mesh_ShadersUsage(0x014F1300, "Part_Mesh_Shaders"),
	Part_Mesh_Materials_PC(0x02491300, "Part_Mesh_Materials_PC"),
	Part_Mesh_Materials_X360(0x024D1300, "Part_Mesh_Materials_X360"),
	Part_Mesh_VertsHeader(0x024C1300, "Part_Mesh_VertsHeader"),
	Part_Mesh_Vertices_PC(0x01491300, "Part_Mesh_Vertices_PC"),
	Part_Mesh_Vertices_X360(0x014D1300, "Part_Mesh_Vertices_X360"),
	Part_Mesh_Triangles_PC(0x03491300, "Part_Mesh_Triangles_PC"),
	Part_Mesh_Triangles_X360(0x034D1300, "Part_Mesh_Triangles_X360"),
	Part_Padding(0x17401300, "Part_Padding?"),
	Part_HashList(0x18401300, "Part_HashList?"),
	Part_HashAssign(0x19401300, "Part_HashAssign?"),
	Part_AutosculptLinking(0x1E401300, "Part_AutosculptLinking"),
	Part_AutosculptZones(0x1D401300, "Part_AutosculptZones"),

	TPK(0x000030b3, "TPK"),
	TPK_Header(0x000031b3, "TPK_Header"),
	TPK_Info(0x01003133, "TPK_Info"),
	TPK_TexList(0x02003133, "TPK_TexList"),
	TPK_TexOffsets(0x03003133, "TPK_TexOffsets"),
	
	Tex_Container(0x000032b3, "Tex_Container"),
	Tex_Header(0x01003233, "Tex_Header"),
	Tex_Data(0x02003233, "Tex_Data"),
	
	NIS_Skeleton(0x0940a300, "NIS_Skeleton?"),
	
	StreamBlocksOffsets(0x10410300, "StreamBlocksOffsets"),
	
	INVALID(0xFFFFFFFF, "INVALID");
	
    private final int key;
    private final String name;

    BlockType(int key, String name) {
        this.key = Integer.reverseBytes(key);
        this.name = name;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static BlockType get(int key) {
        for (BlockType c : values()) {
            if (c.key == key) return c;
        }
        return INVALID; // Handle invalid value
    }
}
