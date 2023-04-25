package rs.ac.bg.etf.pp1.helpers;

import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Struct;

public class Utils {

	public static String getFriendlyNameForType(Struct type) {
		if (type == Tab.intType) return "int";
		else if (type == Tab.charType) return "char";
		else if (type == TabExtended.boolType) return "bool";
		else return "void";
	}
}
