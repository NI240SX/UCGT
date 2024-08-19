package fr.ni240sx.ucgt.geometryFile.sorters;

import java.util.Comparator;

import fr.ni240sx.ucgt.geometryFile.Part;

public class PartSorterBinKey implements Comparator<Part> {
	@Override
	public int compare(Part a, Part b) {
		return Integer.compareUnsigned(a.header.binKey, b.header.binKey);
	}
}
