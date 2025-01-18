package fr.ni240sx.ucgt.geometryFile.sorters;

import java.util.Comparator;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.part.MPointPositionCube;

public class MPointPosSorterName implements Comparator<MPointPositionCube>{
	@Override
	public int compare(MPointPositionCube o1, MPointPositionCube o2) {
		return (o1.mpoints.get(0).part.name + Hash.getBIN(o1.mpoints.get(0).nameHash)).
				compareTo((o2.mpoints.get(0).part.name + Hash.getBIN(o2.mpoints.get(0).nameHash)));
	}
}