package rs.ac.bg.etf.pp1.helpers;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class ControlFlowManager {
	
	public boolean isConditionTypeCorrectForControlFlow(Condition condition) {
		return condition.struct == TabExtended.boolType;
	}
	
	public boolean isCondTermTypeCorrectForControlFlow(CondTerm condTerm) {
		return condTerm.struct == TabExtended.boolType;
	}
	
	public boolean isCondFactTypeCorrectForControlFlow(CondFact condFact) {
		return condFact.struct == TabExtended.boolType;
	}
	
	public boolean areCompatibleTypesInConditionList(CondTerm condTerm, Condition condition) {
		Struct condTermType = condTerm.struct.getKind() == Struct.Array ? condTerm.struct.getElemType() : condTerm.struct;
		Struct conditionListType = condition.struct.getKind() == Struct.Array ? condition.struct.getElemType() : condition.struct;
		return condTermType == conditionListType;
	}
	
	public boolean areCompatibleTypesInCondTermList(CondFact condFact, CondTerm condTerm) {
		Struct conditionListType = condFact.struct.getKind() == Struct.Array ? condFact.struct.getElemType() : condFact.struct;
		Struct condTermType = condTerm.struct.getKind() == Struct.Array ? condTerm.struct.getElemType() : condTerm.struct;
		return condTermType == conditionListType;
	}
	
	public boolean isExprTypeCorrectForControlFlow(Expr expr) {
		return expr.struct == TabExtended.boolType;
	}
	
	public boolean areExprTypesCorrectForRelop(Expr leftExpr, Relop relop, Expr rightExpr) {
		Struct leftExprType = leftExpr.struct;
		Struct rightExprType = rightExpr.struct;
		return (relop instanceof Equals || relop instanceof NotEquals)
				? leftExprType.compatibleWith(rightExprType)
				: leftExprType.getKind() != Struct.Array && rightExprType.getKind() != Struct.Array && leftExprType.compatibleWith(rightExprType);
	}
}