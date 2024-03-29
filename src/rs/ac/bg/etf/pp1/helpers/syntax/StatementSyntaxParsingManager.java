package rs.ac.bg.etf.pp1.helpers.syntax;


import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class StatementSyntaxParsingManager {

	public boolean isDesignatorKindCompatibleWithRead(Designator designator) {
		int designatorKind = designator.obj.getKind();
		return designatorKind == Obj.Var || designatorKind == Obj.Elem;
	}

	public boolean isDesignatorTypeCompatibleWithRead(Designator designator) {
		Struct designatorType = designator.obj.getType();
		return designatorType == Tab.intType || designatorType == Tab.charType || designatorType == TabExtended.boolType;
	}
	
	public boolean isExprTypeCompatibleWithPrint(Struct exprType) {
		return exprType == Tab.intType || exprType == Tab.charType || exprType == TabExtended.boolType;
	}
}
