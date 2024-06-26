package fr.ni240sx.ucgt.geometryFile.sorters;

import java.util.Comparator;

import fr.ni240sx.ucgt.geometryFile.part.MPoint;

public class MPointSorterName implements Comparator<MPoint>{
	public int compare(MPoint o1, MPoint o2) {
		return (o1.part.kit+o1.part.part+o1.part.lod + o1.nameHash.label).
				compareTo((o2.part.kit+o2.part.part+o2.part.lod + o2.nameHash.label));
	}
}