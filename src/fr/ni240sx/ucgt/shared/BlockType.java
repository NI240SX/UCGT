package fr.ni240sx.ucgt.shared;

public enum BlockType {
	Padding(0x00000000),
	
	UCGT_Data(0x55434754),
	
	Geometry(0x00401380),
	Geom_Header(0x01401380),
	Geom_Info(0x02401300),
	Geom_PartsList(0x03401300),
	Geom_PartsOffsets(0x04401300),
	Geom_UNKNOWN(0x08401380),

	CompressedData(0x22114455),
	
	Part(0x10401380),
	Part_Header(0x11401300),
	Part_TexUsage(0x12401300),
	Part_Strings(0x15401300),
	Part_ShaderList(0x13401300),
	Part_MPoints(0x1A401300),
	Part_Mesh(0x00411380),
	Part_Mesh_Info_PC(0x00491300),
	Part_Mesh_Info_X360(0x004D1300),
	Part_Mesh_Info_PS3(0x004E1300),
	Part_Mesh_ShadersUsage(0x014F1300),
	Part_Mesh_Materials_PC(0x02491300),
	Part_Mesh_LegacyMaterials(0x024b1300),
	Part_Mesh_Materials_X360(0x024D1300),
	Part_Mesh_Materials_PS3(0x024E1300),
	Part_Mesh_VertsHeader(0x024C1300),
	Part_Mesh_Vertices_PC(0x01491300),
	Part_Mesh_LegacyVertices(0x014b1300),
	Part_Mesh_VerticesHeader_PS3(0x014C1300), //not implemented rn
	Part_Mesh_Vertices_X360(0x014D1300),
	Part_Mesh_Vertices_PS3(0x014E1300),
	Part_Mesh_Triangles_PC(0x03491300),
	Part_Mesh_LegacyTriangles(0x034b1300),
	Part_Mesh_Triangles_X360(0x034D1300),
	Part_Mesh_Triangles_PS3(0x034E1300),
	//0x22134000 in game code
	Part_Padding(0x17401300),
	Part_HashList(0x18401300),
	Part_HashAssign(0x19401300),
	Part_AutosculptLinking(0x1E401300),
	Part_AutosculptZones(0x1D401300),
	//0x1F401300 in game code
	//0x20401300 in game code
	//0x21401300 in game code
	//0x01033030
	//0x02033030
	//0x03033030

	TPK(0x000030b3),
	TPK_Header(0x000031b3),
	TPK_Info(0x01003133),
	TPK_TexList(0x02003133),
	TPK_TexOffsets(0x03003133),
	TPK_Names(0x04003133),
	TPK_Formats(0x05003133),
	TPK_Animations(0x002031B3),
	
	Tex_Container(0x000032b3),
	Tex_Header(0x01003233),
	Tex_PackedData(0x02003233),

	NIS_Skeleton(0x0940a300),

	ChunksEmpty(0x12410300),
	ChunksZonesContainer(0x47410380),
	ChunksZones1(0x4A410300), //pursuit ? etc
	ChunksMarkers(0x46410300),
	ChunksContainer2(0x50410380),
	ChunksStreamableIDs(0x51410300), //counts (400, 655) then list of chunk ids in int16 -> 1, 2, ..., 1001, 1002, ..., 2001, ..., 5001 (length 655*int16), then 0s (881*int16 but Z0 + 792*(Y+X+W+U))...
	ChunksBoundaries(0x52410300), //may contain elevation data hash
	ChunksElevationRules(0x56410300),
	ChunksPreculler(0x53410300),
	BarrierSplinesInfo(0x09410300),
	BarrierSplines(0x4D410300),
	ChunksOffsets(0x10410300),

	ChunkObjects(0x00410380), //has everything, incl chop, positioned objects, race barriers, etc
	ChunkObjects_Header(0x01410300), //blockStart+12 has chunkID
	ChunkObjects_Positions(0x03410300),
	ChunkObjects_Objects(0x02410300), //objects list 
	ChunkObjects_Ads(0x04410300), //might be MassiveAds stuff
	ChunkObjects_Bounds(0x05410300), //? not sure
	ChunkObjects_Empty512(0x07410300), //often empty 512, sometimes very long with super sparse data (inner data length 512)
	
	ChunkCollisions(0x01B80300),
	
	Damage(0x0D460300),
	
	INVALID(0xFFFFFFFF);
	
    private final int key;

    BlockType(int key) {
        this.key = Integer.reverseBytes(key);
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name();
    }

    public static BlockType get(int key) {
        for (BlockType c : values()) {
            if (c.key == key) return c;
        }
        return INVALID; // Handle invalid value
    }
}
