package fr.ni240sx.ucgt.geometryFile.sorters;

import java.util.Comparator;

import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;

public class MaterialsSorterName implements Comparator<Material>{
	@Override
	public int compare(Material o1, Material o2) {
		return o1.generateName().compareTo(o2.generateName());
	}
}