package fr.ni240sx.ucgt.geometryFile;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.collisionsEditor.CollisionBound.BoundConstraintType;

public enum GeomBlock {
	Padding(0x00000000, "Padding"),
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
	Part_Mesh_Info(0x00491300, "Part_Mesh_Info"),
	Part_Mesh_Shaders(0x014F1300, "Part_Mesh_Shaders"),
	Part_Mesh_Materials(0x02491300, "Part_Mesh_Materials"),
	Part_Mesh_Padding(0x024C1300, "Part_Mesh_Padding?"),
	Part_Mesh_Triangles(0x01491300, "Part_Mesh_Triangles?"),
	Part_Mesh_Indices(0x03491300, "Part_Mesh_Indices?"),
	Part_Padding(0x17401300, "Part_Padding?"),
	Part_HashList(0x18401300, "Part_HashList?"),
	Part_HashAssign(0x19401300, "Part_HashAssign?"),
	INVALID(0xFFFFFFFF, "INVALID");
	
    private final int key;
    private final String name;

    GeomBlock(int key, String name) {
        this.key = Integer.reverseBytes(key);
        this.name = name;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static GeomBlock get(int key) {
        for (GeomBlock c : values()) {
            if (c.key == key) return c;
        }
        return INVALID; // Handle invalid value
    }
}
