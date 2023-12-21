package dbmpPlus;

import java.util.ArrayList;

class Part {
	public ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	public String displayName = "MISSING ATTRIBUTES";
	
	public int kitnumber = 99;
	public boolean isWidebody = false;
	public boolean isCarPart = false;
	
	CarPartListCell listCell = new CarPartListCell();
	
	public Part() {
	}
	
	public Part(String kit, String name) {
		AttributeCarPartID id;
		AttributeTwoString partName;
		isCarPart = true;
		addAttribute(new AttributeInteger("PART_NAME_SELECTOR", 0));
		addAttribute(new AttributeInteger("LOD_NAME_PREFIX_SELECTOR", 0));
		addAttribute(new AttributeInteger("MAX_LOD", 4));
		addAttribute(new AttributeString("LOD_CHARACTERS_OFFSET", "ABCDE"));
		addAttribute(new AttributeString("NAME_OFFSET", kit.replace("KIT", "").replace("W", "")));
		addAttribute(id = new AttributeCarPartID("PARTID_UPGRADE_GROUP", PartUndercover.get(name.replace("WIDEBODY_", "")), 0));
		addAttribute(partName = new AttributeTwoString("PART_NAME_OFFSETS", kit, name));
		addAttribute(new AttributeTwoString("LOD_BASE_NAME", kit, name));
		if (name.equals("BODY") || name.equals("WIDEBODY")) addAttribute(new AttributeKey("CV", DBMPPlus.mainDBMP.carname.label + "_CV"));

		//PartID autodetect (problematic : exhausts, driver_female, brake and brakerotor, rollcage, seat, spoiler drag/lip/evo, widebody)
		if (name.equals("WIDEBODY")) id.ID = PartUndercover.BODY;
		else if (name.contains("BRAKEROTOR")) id.ID = PartUndercover.BRAKEROTOR;
		else if (name.contains("BRAKE_")) id.ID = PartUndercover.BRAKE;
		else if (name.contains("EXHAUST_TIPS_LEFT")) id.ID = PartUndercover.EXHAUST_TIPS_LEFT;
		else if (name.contains("EXHAUST_TIPS_RIGHT")) id.ID = PartUndercover.EXHAUST_TIPS_RIGHT;
		else if (name.contains("EXHAUST_TIPS_CENTER")) id.ID = PartUndercover.EXHAUST_TIPS_CENTER;
		else if (name.contains("EXHAUST_")) id.ID = PartUndercover.EXHAUST;
		else if (name.contains("MUFFLER")) id.ID = PartUndercover.EXHAUST;
		else if (name.contains("DRIVER")) id.ID = PartUndercover.DRIVER;
		else if (name.contains("ROLL_CAGE")) id.ID = PartUndercover.ROLL_CAGE;
		else if (name.contains("SEAT")) id.ID = PartUndercover.SEAT;
		else if (name.contains("SPOILER")) id.ID = PartUndercover.SPOILER;
		
		//Widebody fixes for user input
		if (kit.contains("W")) {
			if (name.equals("BODY")) partName.value2 = "WIDEBODY";
			else if (name.contains("BUMPER") || name.contains("DOOR") || name.contains("FENDER") || name.contains("SKIRT")) partName.value2 = "WIDEBODY_" + name;
		}
		
	}
	
	public Part(Part copyFrom) {
		for(Attribute a : copyFrom.attributes) {
			if (a.getClass() == AttributeInteger.class) {
				this.attributes.add(new AttributeInteger((AttributeInteger)a));
			} else if (a.getClass() == AttributeString.class) {
				this.attributes.add(new AttributeString((AttributeString)a));
			} else if (a.getClass() == AttributeTwoString.class) {
				this.attributes.add(new AttributeTwoString((AttributeTwoString)a));
			} else if (a.getClass() == AttributeKey.class) {
				this.attributes.add(new AttributeKey((AttributeKey)a));
			} else if (a.getClass() == AttributeCarPartID.class) {
				this.attributes.add(new AttributeCarPartID((AttributeCarPartID)a));
			} else if (a.getClass() == AttributeBoolean.class) {
				this.attributes.add(new AttributeBoolean((AttributeBoolean)a));
			} else if (a.getClass() == AttributeColor.class) {
				this.attributes.add(new AttributeColor((AttributeColor)a));
			}
		}
		this.isCarPart = copyFrom.isCarPart;
		this.displayName = copyFrom.displayName;
		this.isWidebody = copyFrom.isWidebody;
		this.kitnumber = copyFrom.kitnumber;
	}
	
	public void update() {
		if (getAttribute("LOD_BASE_NAME") != null) isCarPart = true;
		if(isCarPart) {
			if (DBMPPlus.widebodyAutoCorrect && ((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )) != null){
				if (((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value1.contains("W")) {
					((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2 = ((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2.replace("WIDEBODY_", "");
					
					if (((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2.equals("BODY")) ((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2 = "WIDEBODY";
					else if (((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2.contains("BUMPER") 
							|| ((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2.contains("DOOR") 
							|| ((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2.contains("FENDER") 
							|| ((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2.contains("SKIRT")) {
						((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2 = "WIDEBODY_" + ((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2;
					}
				} else {
					if (((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2.equals("WIDEBODY")) ((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2 = "BODY";
					else {
						((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2 = ((AttributeTwoString)getAttribute("PART_NAME_OFFSETS" )).value2.replace("WIDEBODY_", "");
					}
				}
			}
			
			String name1 = "MISSING ATTRIBUTES";
			String name2 = "MISSING ATTRIBUTES";
			for (Attribute attribute : attributes) {
				attribute.update();
				if (attribute.Key.label.equals("PART_NAME_OFFSETS")) {
					AttributeTwoString att2S = (AttributeTwoString)attribute;
					name1 = att2S.value1 + "_" + att2S.value2;
					kitnumber = Integer.valueOf(att2S.value1.substring(att2S.value1.length()-2));
					if (att2S.value1.charAt(3) == 'W') isWidebody = true; else isWidebody = false;
				}
				if (attribute.Key.label.equals("LOD_BASE_NAME")) {
					AttributeTwoString att2S = (AttributeTwoString)attribute;
					name2 = att2S.value1 + "_" + att2S.value2;
				}
			}
			if (name2.equals(name1)) {
				displayName = name1;
			} else {
				displayName = name1 + " (reference to " + name2 + ")";
			}
		} else {
			displayName = ((AttributeString)getAttribute("NAME_OFFSET")).value1;
		}
		listCell.update();
	}
	
	public void addAttribute(Attribute a) {
		attributes.add(a);
		if (getAttribute("LOD_BASE_NAME") != null) isCarPart = true;
		update();
	}
	public Attribute getAttribute(String a) {
		Attribute att = null;
		for (Attribute attribute : attributes) {
			if (attribute.Key.label.equals(a)) {
				att = attribute;
			}
		}
		return att;
	}
	public AttributeInteger getAttributeInteger(String a) {
		AttributeInteger att = null;
		for (Attribute attribute : attributes) {
			if (attribute.Key.label.equals(a)) {
				att = (AttributeInteger)attribute;
			}
		}
		return att;
	}
	public void removeAttribute(String a) {
		for (Attribute attribute : attributes) {
			if (attribute.Key.label.equals(a)) {
				attributes.remove(attribute);
			}
		}
		update();
	}
	
	public String toString() {
		String s = "Part " + displayName;
		for (Attribute a : attributes) {
			s += "; " + a;
		}
		return s;
	}
}