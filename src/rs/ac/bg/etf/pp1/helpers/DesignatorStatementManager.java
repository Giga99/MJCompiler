package rs.ac.bg.etf.pp1.helpers;


import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class DesignatorStatementManager {

	public boolean isDesignatorKindCorrectForAssign(Designator designator) {
		int designatorKind = designator.obj.getKind();
		return designatorKind == Obj.Var || designatorKind == Obj.Elem;
	}
	
	public boolean isSameTypeOfDesignatorAndExprInAssign(Designator designator, Expr expr) {
		return expr.struct.assignableTo(designator.obj.getType());
	}
}
