package rs.ac.bg.etf.pp1.helpers;

import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Struct;

public class Utils {

	public static Struct getArrayTypeForGivenType(Struct elemType) {
		return new Struct(Struct.Array, elemType);
	}
	
	public static String getFriendlyNameForType(Struct type) {
		if (type.getKind() == Struct.Array) return getFriendlyNameForBaseType(type.getElemType()) + "[]";
		else return getFriendlyNameForBaseType(type);
	}
	
	private static String getFriendlyNameForBaseType(Struct type) {
		if (type == Tab.intType) return "int";
		else if (type == Tab.charType) return "char";
		else if (type == TabExtended.boolType) return "bool";
		else return "void";
	}
}
