package dbmpPlus;

import java.util.ArrayList;

class Part {
	public ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	public String displayName = "MISSING ATTRIBUTES";
	
	public int kitnumber = 99;
	public boolean isWidebody = false;
	public int autosculptTargets = 0;
	
	public void update() {
		String name1 = "MISSING ATTRIBUTES";
		String name2 = "MISSING ATTRIBUTES";
		for (Attribute attribute : attributes) {
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
	}
	
	public void addAttribute(Attribute a) {
		attributes.add(a);
		update();
	}
	public Attribute editAttribute(String a) {
		Attribute att = null;
		for (Attribute attribute : attributes) {
			if (attribute.Key.label.equals(a)) {
				att = attribute;
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