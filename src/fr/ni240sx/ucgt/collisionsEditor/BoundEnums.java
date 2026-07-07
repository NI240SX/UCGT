package fr.ni240sx.ucgt.collisionsEditor;

import java.util.ArrayList;

public class BoundEnums {
	public static enum BoundFlags {
	    KFLAG_NONE((byte) 0),
	    KFLAG_DISABLED((byte) 1),
	    KFLAG_INTERNAL((byte) 2),
	    KFLAG_JOINT_INVERT((byte) 4),
	    KFLAG_NO_COLLISION_GROUND((byte) 32),
	    KFLAG_NO_COLLISION_BARRIER((byte) 64),
	    KFLAG_NO_COLLISION_OBJECT((byte) 128);
//	    KFLAG_NO_COLLISION_MASK((byte) 224, "KFLAG_NO_COLLISION_MASK");

	    private final byte value;

	    private BoundFlags(byte value) {
	        this.value = value;
	    }

		public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name();
	    }

	    public static ArrayList<BoundFlags> get(byte val) {
	    	int value = Byte.toUnsignedInt(val);
	    	var list = new ArrayList<BoundFlags>();
//	    	if (value == 224) {
//	    		list.add(KFLAG_NO_COLLISION_MASK);
//	    		return list;
//	    	}
	        for (BoundFlags s : values()) {
	            if ((s.value & value) != 0) list.add(s);
	        }
	        if (list.isEmpty()) list.add(KFLAG_NONE);
	        return list; // Handle invalid value
	    }

	    static byte getFlags(ArrayList<BoundFlags> flags) {
	    	int ret = 0;
	    	for (var f : flags) ret += Byte.toUnsignedInt(f.getValue());
			return (byte) ret;
		}

	    public static BoundFlags get(String name) {
	        for (BoundFlags s : values()) {
	            if (s.name().equals(name)) return s;
	        }
	        return KFLAG_NONE; // Handle invalid name
	    }
	}

	public static enum BoundType {
	    KBOUNDS_INVALID((byte) 0),
	    KBOUNDS_NODE((byte) 1),
	    KBOUNDS_GEOMETRY((byte) 2),
	    KBOUNDS_JOINT((byte) 3),
	    KBOUNDS_CONSTRAINT((byte) 4),
	    KBOUNDS_EMITTER((byte) 5),
	    KBOUNDS_TYPE_COUNT((byte) 6);

	    private final byte value;

	    private BoundType(byte value) {
	        this.value = value;
	    }

	    public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name();
	    }

	    public static BoundType get(byte value) {
	        for (BoundType s : values()) {
	            if (s.value == value) return s;
	        }
	        System.err.println("Unknown BoundType %d".formatted(value));
	        return KBOUNDS_INVALID; // Handle invalid value
	    }

	    public static BoundType get(String name) {
	        for (BoundType s : values()) {
	            if (s.name().equals(name)) return s;
	        }
	        return KBOUNDS_INVALID; // Handle invalid name
	    }
	}

	public static enum BoundShape {
	    KSHAPE_INVALID((byte) 0),
	    KSHAPE_BOX((byte) 1),
	    KSHAPE_SPHERE((byte) 2),
	    KSHAPE_CYLINDER((byte) 3),
	    KSHAPE_MESH((byte) 4),
	    KSHAPE_TYPE_COUNT((byte) 5);

	    private final byte value;

	    private BoundShape(byte value) {
	        this.value = value;
	    }

	    public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name();
	    }

	    public static BoundShape get(byte value) {
	        for (BoundShape s : values()) {
	            if (s.value == value) return s;
	        }
	        System.err.println("Unknown BoundShape %d".formatted(value));
	        return KSHAPE_INVALID; // Handle invalid value
	    }

	    public static BoundShape get(String name) {
	        for (BoundShape s : values()) {
	            if (s.name().equals(name)) return s;
	        }
	        return KSHAPE_INVALID; // Handle invalid name
	    }
	}

	public static enum BoundJointType {
	    KBOUNDS_JOINT_INVALID((byte) 0),
	    KBOUNDS_CONSTRAINT_CONICAL((byte) 1),
	    KBOUNDS_CONSTRAINT_PRISMATIC((byte) 2),
	    KBOUNDS_JOINT_FEMALE((byte) 3),
	    KBOUNDS_JOINT_MALE((byte) 4),
	    KBOUNDS_MALE_POST((byte) 5),
	    KBOUNDS_JOINT_SUBTYPE_COUNT((byte) 6);

	    private final byte value;

	    private BoundJointType(byte value) {
	        this.value = value;
	    }

	    public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name();
	    }

	    public static BoundJointType get(byte value) {
	        for (BoundJointType s : values()) {
	            if (s.value == value) return s;
	        }
	        System.err.println("Unknown BoundJointType %d".formatted(value));
	        return KBOUNDS_JOINT_INVALID; // Handle invalid value
	    }

	    public static BoundJointType get(String name) {
	        for (BoundJointType s : values()) {
	            if (s.name().equals(name)) return s;
	        }
	        return KBOUNDS_JOINT_INVALID; // Handle invalid name
	    }
	}

	public static enum BoundNodeType {
	    KBOUNDS_NODE_INVALID((byte) 0),
	    KBOUNDS_NODE_HEIRARCHY((byte) 1),
	    KBOUNDS_NODE_FRAGMENT((byte) 2),
	    KBOUNDS_NODE_SUBTYPE_COUNT((byte) 3);

	    private final byte value;

	    private BoundNodeType(byte value) {
	        this.value = value;
	    }

	    public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name();
	    }

	    public static BoundNodeType get(byte value) {
	        for (BoundNodeType s : values()) {
	            if (s.value == value) return s;
	        }
	        System.err.println("Unknown BoundNodeType %d".formatted(value));
	        return KBOUNDS_NODE_INVALID; // Handle invalid value
	    }

	    public static BoundNodeType get(String name) {
	        for (BoundNodeType s : values()) {
	            if (s.name().equals(name)) return s;
	        }
	        return KBOUNDS_NODE_INVALID; // Handle invalid name
	    }
	}

	public static enum BoundConstraintType {
	    KBOUNDS_CONSTRAINT_INVALID((byte) 0),
	    KBOUNDS_CONSTRAINT_HINGE((byte) 1),
	    KBOUNDS_CONSTRAINT_BALL_SOCKET((byte) 2),
	    KBOUNDS_CONSTRAINT_SUBTYPE_COUNT((byte) 3);

	    private final byte value;

	    private BoundConstraintType(byte value) {
	        this.value = value;
	    }

	    public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name();
	    }

	    public static BoundConstraintType get(byte value) {
	        for (BoundConstraintType s : values()) {
	            if (s.value == value) return s;
	        }
	        System.err.println("Unknown BoundConstraintType %d".formatted(value));
	        return KBOUNDS_CONSTRAINT_INVALID; // Handle invalid value
	    }

	    public static BoundConstraintType get(String name) {
	        for (BoundConstraintType s : values()) {
	            if (s.name().equals(name)) return s;
	        }
	        return KBOUNDS_CONSTRAINT_INVALID; // Handle invalid name
	    }
	}
}
