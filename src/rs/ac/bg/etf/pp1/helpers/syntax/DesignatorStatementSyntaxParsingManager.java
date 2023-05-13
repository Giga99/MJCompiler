package rs.ac.bg.etf.pp1.helpers.syntax;


import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class DesignatorStatementSyntaxParsingManager {
	
	private List<String> mapVariables = new ArrayList<String>();

	public boolean isDesignatorKindCorrectForAssign(Designator designator) {
		int designatorKind = designator.obj.getKind();
		return designatorKind == Obj.Var || designatorKind == Obj.Elem;
	}
	
	public boolean isSameTypeOfDesignatorAndExprInAssign(Designator designator, Expr expr) {
		return expr.struct.assignableTo(designator.obj.getType());
	}

	public boolean isDesignatorKindCorrectForIncAndDec(Designator designator) {
		int designatorKind = designator.obj.getKind();
		return designatorKind == Obj.Var || designatorKind == Obj.Elem;
	}

	public boolean isDesignatorTypeCorrectForIncAndDec(Designator designator) {
		Struct designatorType = designator.obj.getType();
		return designatorType == Tab.intType;
	}
	
	public void addMapVariable(String mapVariableName) {
		mapVariables.add(mapVariableName);
	}
	
	public void removeMapVariable(String mapVariableName) {
		mapVariables.remove(mapVariableName);
	}
}
