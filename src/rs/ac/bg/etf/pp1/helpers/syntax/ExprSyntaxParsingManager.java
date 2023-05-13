package rs.ac.bg.etf.pp1.helpers.syntax;


import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class ExprSyntaxParsingManager {

	public boolean isTermTypeInt(Struct termType) {
		return termType == Tab.intType;
	}
	
	public boolean areCompatibleTypesInAddopExpr(Term term, TermList termList) {
		Struct termType = term.struct.getKind() == Struct.Array ? term.struct.getElemType() : term.struct;
		Struct termListType = termList.struct.getKind() == Struct.Array ? termList.struct.getElemType() : termList.struct;
		return termType == Tab.intType && termListType == Tab.intType;
	}
	
	public boolean areCompatibleTypesInMulopExpr(Term term, Factor factor) {
		Struct termType = term.struct.getKind() == Struct.Array ? term.struct.getElemType() : term.struct;
		Struct factorType = factor.struct.getKind() == Struct.Array ? factor.struct.getElemType() : factor.struct;
		return termType == Tab.intType && factorType == Tab.intType;
	}
	
	public boolean isCorrectTypeForSizeOfArray(Expr expr) {
		return expr.struct == Tab.intType;
	}
	
	public boolean isCorrectTypeForIndexOfArray(Expr expr) {
		return expr.struct == Tab.intType;
	}
	
	public boolean isDesignatorArray(Obj designator) {
		return designator.getType().getKind() == Struct.Array;
	}
	
	public boolean isDesignatorMatrix(Obj designator) {
		return designator.getType().getKind() == Struct.Array && designator.getType().getElemType().getKind() == Struct.Array;
	}
}
