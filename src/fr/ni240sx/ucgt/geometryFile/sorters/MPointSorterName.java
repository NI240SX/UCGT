package fr.ni240sx.ucgt.geometryFile.sorters;

import java.util.Comparator;

import fr.ni240sx.ucgt.geometryFile.part.MPoint;

public class MPointSorterName implements Comparator<MPoint>{
	public int compare(MPoint o1, MPoint o2) {
		return (o1.nameHash.label+o1.parts.get(0)).compareTo((o2.nameHash.label+o2.parts.get(0)));
	}
}