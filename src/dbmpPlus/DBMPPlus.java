package dbmpPlus;

import java.io.File;

public class DBMPPlus {

	public static void main(String[] args) {
		/*
		System.out.println(new AttributeTwoString("PART_NAME_OFFSETS"));
		System.out.println(new AttributeCarPartID("PARTID_UPGRADE_GROUP"));
		System.out.println(new AttributeKey("CV"));
		
		Part p = new Part();
		p.attributes.add(new AttributeTwoString("PART_NAME_OFFSETS","KIT00","BUMPER_FRONT"));
		p.attributes.add(new AttributeTwoString("LOD_BASE_NAME","KIT00","BUMPER_FRONT"));
		System.out.println(p);
		p.update();
		System.out.println(p);
		((AttributeTwoString)p.editAttribute("PART_NAME_OFFSETS")).value2 = "BUMP";
		System.out.println(p);
		p.update();
		System.out.println(p);

		Part p2 = new Part();
		System.out.println(p2);
		p2.addAttribute(new AttributeTwoString("PART_NAME_OFFSETS","KIT00","BODY"));
		System.out.println(p2);
		p2.addAttribute(new AttributeTwoString("LOD_BASE_NAME","KIT00","BODY"));
		System.out.println(p2);
		((AttributeTwoString)p2.editAttribute("PART_NAME_OFFSETS")).value1 = "KIT11";
		p2.update();
		System.out.println(p2);
		
		DBMP testDBMP = new DBMP("NIS_240_SX_89");
		testDBMP.parts.add(p);
		testDBMP.parts.add(p2);
		System.out.println(testDBMP); */

		DBMP loadTest = DBMP.loadDBMP(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\voitures\\z done\\car bmw e92\\dbmp step 8.bin"));
		/*
		DBMP loadTest = loadDBMP(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\LOT_ELI_111_06.bin"));
		System.out.println(loadTest.displayName());
		loadTest.saveToFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\DBMP EXPORT LOT_ELI_111_06.bin"));
		
		loadTest = loadDBMP(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\MIT_EVO_X_08.bin"));
		loadTest.saveToFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\DBMP EXPORT MIT_EVO_X_08.bin"));
		
		loadTest = loadDBMP(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\BMW_M3_E92_08.bin"));
		loadTest.saveToFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\DBMP EXPORT BMW_M3_E92_08.bin")); */
		
	}
	
}