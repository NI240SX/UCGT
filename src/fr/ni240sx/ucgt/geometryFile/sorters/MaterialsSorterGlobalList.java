package fr.ni240sx.ucgt.geometryFile.sorters;

import java.util.Comparator;
import java.util.List;

import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;


public class MaterialsSorterGlobalList implements Comparator<Material>{

	List<Material> referenceList;
	
	public MaterialsSorterGlobalList(Geometry geom) {
		referenceList = geom.materials;
	}
	@Override
	public int compare(Material o1, Material o2) {
		return Integer.compare(referenceList.indexOf(o1), referenceList.indexOf(o2));
	}
}