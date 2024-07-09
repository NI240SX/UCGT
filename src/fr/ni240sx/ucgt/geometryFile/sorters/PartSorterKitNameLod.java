package fr.ni240sx.ucgt.geometryFile.sorters;

import java.util.Comparator;

import fr.ni240sx.ucgt.geometryFile.Part;

public class PartSorterKitNameLod implements Comparator<Part> {
	@Override
	public int compare(Part a, Part b) {
		return (a.name).compareTo(b.name);
	}
}