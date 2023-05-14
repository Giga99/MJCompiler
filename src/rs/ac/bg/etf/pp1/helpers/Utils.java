package rs.ac.bg.etf.pp1.helpers;

import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Struct;

public class Utils {
	private final static double NUMBER_OF_BYTES = 1024.0;
	private final static double NUMBER_OF_KILO_BYTES = NUMBER_OF_BYTES * NUMBER_OF_BYTES;
	private final static double NUMBER_OF_MEGA_BYTES = NUMBER_OF_BYTES * NUMBER_OF_KILO_BYTES;

	public static Struct getArrayTypeForGivenType(Struct elemType) {
		return new Struct(Struct.Array, elemType);
	}

	public static Struct getMatrixTypeForGivenType(Struct elemType) {
		return new Struct(Struct.Array, new Struct(Struct.Array, elemType));
	}
	
	public static String getUserFriendlyStringForNumberOfBytes(int bytes) {
	    if (bytes < 1024) {
	        return bytes + "B";
	    } else if (bytes < NUMBER_OF_KILO_BYTES) {
	        return String.format("%.2fKB", bytes / NUMBER_OF_BYTES);
	    } else if (bytes < 1024 * 1024 * 1024) {
	        return String.format("%.2fMB", bytes / NUMBER_OF_KILO_BYTES);
	    } else {
	        return String.format("%.2fGB", bytes / NUMBER_OF_MEGA_BYTES);
	    }
	}
	
	public static String getFriendlyNameForType(Struct type) {
		if (type.getKind() == Struct.Array) return getFriendlyNameForType(type.getElemType()) + "[]";
		else return getFriendlyNameForBaseType(type);
	}
	
	private static String getFriendlyNameForBaseType(Struct type) {
		if (type == Tab.intType) return "int";
		else if (type == Tab.charType) return "char";
		else if (type == TabExtended.boolType) return "bool";
		else return "void";
	}
}
