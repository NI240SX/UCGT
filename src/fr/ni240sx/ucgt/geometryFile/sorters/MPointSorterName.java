package fr.ni240sx.ucgt.geometryFile.sorters;

import java.util.Comparator;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.part.MPoint;

public class MPointSorterName implements Comparator<MPoint>{
	@Override
	public int compare(MPoint o1, MPoint o2) {
		return (o1.part.name + Hash.getBIN(o1.nameHash)).
				compareTo((o2.part.name + Hash.getBIN(o2.nameHash)));
	}
}