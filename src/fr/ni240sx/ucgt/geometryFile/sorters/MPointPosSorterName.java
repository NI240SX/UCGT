package fr.ni240sx.ucgt.geometryFile.sorters;

import java.util.Comparator;

import fr.ni240sx.ucgt.geometryFile.part.MPointPositionCube;

public class MPointPosSorterName implements Comparator<MPointPositionCube>{
	public int compare(MPointPositionCube o1, MPointPositionCube o2) {
		return (o1.mpoints.get(0).part.kit+o1.mpoints.get(0).part.part+o1.mpoints.get(0).part.lod + o1.mpoints.get(0).nameHash.label).
				compareTo((o2.mpoints.get(0).part.kit+o2.mpoints.get(0).part.part+o2.mpoints.get(0).part.lod + o2.mpoints.get(0).nameHash.label));
	}
}