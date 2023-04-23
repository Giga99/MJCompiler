package rs.ac.bg.etf.pp1.tabextended;

import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.visitors.*;

public class TabExtended {

	public static final Struct boolType = new Struct(Struct.Bool);
	
	public static void init() {
		Tab.init();
		Obj newBoolType = new Obj(Obj.Type, "bool", boolType);
		Tab.currentScope.addToLocals(newBoolType);
	}
	
	public static void dump() {
		SymbolTableVisitor visitor = new SymbolTableVisitorExtended();
		Tab.dump(visitor);
	}
}
