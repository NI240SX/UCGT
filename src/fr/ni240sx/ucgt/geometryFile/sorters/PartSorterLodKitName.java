package fr.ni240sx.ucgt.geometryFile.sorters;

import java.util.Comparator;

import fr.ni240sx.ucgt.geometryFile.Part;

public class PartSorterLodKitName implements Comparator<Part> {
	public int compare(Part a, Part b) {
		return (a.lod + a.kit + a.part).compareTo(b.lod + b.kit + b.part);
	}
}