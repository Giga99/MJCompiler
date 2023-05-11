package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.helpers.*;
import rs.ac.bg.etf.pp1.helpers.syntax.ControlFlowManager;
import rs.ac.bg.etf.pp1.helpers.syntax.DeclarationManager;
import rs.ac.bg.etf.pp1.helpers.syntax.DesignatorStatementManager;
import rs.ac.bg.etf.pp1.helpers.syntax.ExprManager;
import rs.ac.bg.etf.pp1.helpers.syntax.MethodManager;
import rs.ac.bg.etf.pp1.helpers.syntax.StatementManager;
import rs.ac.bg.etf.pp1.tabextended.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {
	private Logger log = Logger.getLogger(getClass());

	private boolean errorDetected = false;
	private int numberOfVars;

	private DeclarationManager declarationManager = new DeclarationManager();
	private MethodManager methodManager = new MethodManager();
	private ExprManager exprManager = new ExprManager();
	private StatementManager statementManager = new StatementManager();
	private DesignatorStatementManager designatorStatementManager = new DesignatorStatementManager();
	private ControlFlowManager controlFlowManager = new ControlFlowManager();

	private Struct currentType = null;

	public void reportError(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" on line ").append(line);
		log.error(msg.toString());
	}

	public void reportInfo(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" on line ").append(line);
		log.info(msg.toString());
	}

	public boolean passed() {
		return !errorDetected;
	}
	
	public int getNumberOfVars() {
		return numberOfVars;
	}

	public void visit(ProgName progName) {
		numberOfVars = 0;
		progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
		Tab.openScope();
		reportInfo("ProgName", progName);
	}

	public void visit(Program program) {
		numberOfVars = Tab.currentScope.getnVars();
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
		if (!methodManager.isMainMethodPresent()) {
			reportError("Main method doesn't exist", program);
		}
		reportInfo("Program", program);
	}

	public void visit(Type type) {
		String typeName = type.getTypeName();
		Obj typeNode = Tab.find(typeName);

		if (typeNode == Tab.noObj) {
			reportError("Type " + typeName + " not found in the Symbol Table", type);
			type.struct = Tab.noType;
		} else {
			if (typeNode.getKind() == Obj.Type) {
				type.struct = typeNode.getType();
			} else {
				reportError("Type " + typeName + " is not a valid type in the Symbol Table", type);
				type.struct = Tab.noType;
			}
		}

		currentType = type.struct;
		reportInfo("Type", type);
	}

	/* Rules for the const declaration */
	
	public void visit(ConstDecl constDecl) {
		reportInfo("ConstDecl", constDecl);
	}

	public void visit(ConstDeclListItem constDeclListItem) {
		String constName = constDeclListItem.getConstName();
		Struct valueType = constDeclListItem.getConstValue().obj.getType();
		if (declarationManager.isSymbolAlreadyDeclaredInCurrentScope(constName)) {
			reportError("Symbol " + constName + " is already declared in this scope", constDeclListItem);
		} else if (declarationManager.typesNotMatching(currentType, valueType)) {
			reportError("Symbol type " + currentType.getKind() + " is not matching with the value type " + valueType.getKind(), constDeclListItem);
		} else {
			Obj obj = Tab.insert(Obj.Con, constName, currentType);
			obj.setAdr(constDeclListItem.getConstValue().obj.getAdr());
			constDeclListItem.getConstValue().obj = obj;
		}
		reportInfo("ConstDeclListItem", constDeclListItem);
	}

	public void visit(ConstValueNumber constValueNumber) {
		Obj obj = Tab.insert(Obj.Con, "constInt", Tab.intType);
		obj.setAdr(constValueNumber.getValue());
		constValueNumber.obj = obj;
		reportInfo("ConstValueNumber", constValueNumber);
	}

	public void visit(ConstValueChar constValueChar) {
		Obj obj = Tab.insert(Obj.Con, "constChar", Tab.charType);
		obj.setAdr(constValueChar.getValue());
		constValueChar.obj = obj;
		reportInfo("ConstValueChar", constValueChar);
	}

	public void visit(ConstValueBool constValueBool) {
		Obj obj = Tab.insert(Obj.Con, "constBool", TabExtended.boolType);
		obj.setAdr(constValueBool.getValue().equals("true") ? 1 : 0);
		constValueBool.obj = obj;
		reportInfo("ConstValueBool", constValueBool);
	}

	/* Rules for the variable declaration */
	
	public void visit(VarDecl varDecl) {
		reportInfo("VarDecl", varDecl);
	}

	public void visit(DefaultVar defaultVar) {
		String varName = defaultVar.getVarName();
		if (declarationManager.isSymbolAlreadyDeclaredInCurrentScope(varName)) {
			reportError("Symbol " + varName + " is already declared in this scope", defaultVar);
		} else {
			Tab.insert(Obj.Var, varName, currentType);
			reportInfo("DefaultVar", defaultVar);
		}
	}

	public void visit(ArrayVar arrayVar) {
		String arrayName = arrayVar.getArrayName();
		if (declarationManager.isSymbolAlreadyDeclaredInCurrentScope(arrayName)) {
			reportError("Symbol " + arrayName + " is already declared in this scope", arrayVar);
		} else {
			Tab.insert(Obj.Var, arrayName, declarationManager.getArrayTypeForGivenType(currentType));
			reportInfo("ArrayVar", arrayVar);
		}
	}

	/* Rules for the method declaration */

	public void visit(MethodVoidReturnType methodVoidReturnType) {
		methodManager.setCurrentMethodReturnType(Tab.noType);
		reportInfo("MethodVoidReturnType", methodVoidReturnType);
	}

	public void visit(MethodAnyReturnType methodAnyReturnType) {
		methodManager.setCurrentMethodReturnType(currentType);
		reportInfo("MethodAnyReturnType", methodAnyReturnType);
	}

	public void visit(MethodName methodName) {
		methodName.obj = methodManager.setCurrentMethod(methodName.getMethodName());
		Tab.openScope();
		reportInfo("MethodName", methodName);
	}

	public void visit(MethodDecl methodDecl) {
		Tab.chainLocalSymbols(methodManager.getCurrentMethod());
		Tab.closeScope();
		if (methodManager.isMethodCorrect()) {
			methodManager.finishMethod();
			reportInfo("MethodDecl", methodDecl);
		} else {
			reportError("Method " + methodDecl.getMethodName().getMethodName() + " is not declared correctly, either the return is not present or it returns the wrong type or the main is not declared correctly", methodDecl);
		}
	}

	public void visit(FormParamDeclarationVar formParamDeclarationVar) {
		String varName = formParamDeclarationVar.getVarName();
		if (declarationManager.isSymbolAlreadyDeclaredInCurrentScope(varName)) {
			reportError("Symbol " + varName + " is already declared in this scope", formParamDeclarationVar);
		} else {
			methodManager.addFormParam(currentType);
			Tab.insert(Obj.Var, varName, currentType);
			reportInfo("FormParamDeclarationVar", formParamDeclarationVar);
		}
	}

	public void visit(FormParamDeclarationArray formParamDeclarationArray) {
		String arrayName = formParamDeclarationArray.getArrayName();
		if (declarationManager.isSymbolAlreadyDeclaredInCurrentScope(arrayName)) {
			reportError("Symbol " + arrayName + " is already declared in this scope", formParamDeclarationArray);
		} else {
			methodManager.addFormParam(currentType);
			Tab.insert(Obj.Var, arrayName, declarationManager.getArrayTypeForGivenType(currentType));
			reportInfo("FormParamDeclarationArray", formParamDeclarationArray);
		}
	}

	/* Rules for the expressions */

	public void visit(ExprPositiveFirstTerm exprPositiveFirstTerm) {
		exprPositiveFirstTerm.struct = exprPositiveFirstTerm.getTermList().struct;
		reportInfo("ExprPositiveFirstTerm", exprPositiveFirstTerm);
	}

	public void visit(ExprNegativeFirstTerm exprNegativeFirstTerm) {
		Struct termType = exprNegativeFirstTerm.getTermList().struct;
		if (exprManager.isTermTypeInt(termType)) {
			exprNegativeFirstTerm.struct = termType;
			reportInfo("ExprNegativeFirstTerm", exprNegativeFirstTerm);
		} else {
			reportError("Type in expression starting with negative term must be int", exprNegativeFirstTerm);
			exprNegativeFirstTerm.struct = Tab.noType;
		}
	}

	public void visit(TermListSingle termListSingle) {
		termListSingle.struct = termListSingle.getTerm().struct;
		reportInfo("TermListSingle", termListSingle);
	}

	public void visit(TermListMultiple termListMultiple) {
		Term term = termListMultiple.getTerm();
		TermList termList = termListMultiple.getTermList();
		if (exprManager.areCompatibleTypesInAddopExpr(term, termList)) {
			termListMultiple.struct = term.struct;
			reportInfo("TermListMultiple", termListMultiple);
		} else {
			reportError("Type in expression with multiple terms must be int", termListMultiple);
			termListMultiple.struct = Tab.noType;
		}
	}

	public void visit(TermSingleFactor termSingleFactor) {
		termSingleFactor.struct = termSingleFactor.getFactor().struct;
		reportInfo("TermSingleFactor", termSingleFactor);
	}

	public void visit(TermMultipleFactor termMultipleFactor) {
		Term term = termMultipleFactor.getTerm();
		Factor factor = termMultipleFactor.getFactor();
		if (exprManager.areCompatibleTypesInMulopExpr(term, factor)) {
			termMultipleFactor.struct = term.struct;
			reportInfo("TermMultipleFactor", termMultipleFactor);
		} else {
			reportError("Type in expression multiple factors must be int", termMultipleFactor);
			termMultipleFactor.struct = Tab.noType;
		}
	}

	/* Rules for the factor in expressions */

	public void visit(FactorNumber factorNumber) {
		factorNumber.struct = Tab.intType;
		reportInfo("FactorNumber", factorNumber);
	}

	public void visit(FactorChar factorChar) {
		factorChar.struct = Tab.charType;
		reportInfo("FactorChar", factorChar);
	}

	public void visit(FactorBool factorBool) {
		factorBool.struct = TabExtended.boolType;
		reportInfo("FactorBool", factorBool);
	}

	public void visit(FactorExpr factorExpr) {
		factorExpr.struct = factorExpr.getExpr().struct;
		reportInfo("FactorExpr", factorExpr);
	}

	public void visit(FactorNewTypeExpr factorNewTypeExpr) {
		Expr exprForSizeOfArray = factorNewTypeExpr.getExpr();
		if (exprManager.isCorrectTypeForSizeOfArray(exprForSizeOfArray)) {
			factorNewTypeExpr.struct = new Struct(Struct.Array, factorNewTypeExpr.getType().struct);
			reportInfo("FactorNewTypeExpr", factorNewTypeExpr);
		} else {
			reportError("Type in expression for the size of the array must be int", factorNewTypeExpr);
			factorNewTypeExpr.struct = Tab.noType;
		}
	}

	public void visit(FactorDesignator factorDesignator) {
		factorDesignator.struct = factorDesignator.getDesignator().obj.getType();
		reportInfo("FactorDesignator", factorDesignator);
	}

	public void visit(FactorMethodCall factorMethodCall) {
		Designator designator = factorMethodCall.getMethodNameCall().getDesignator();
		if (!methodManager.isDesignatorMethod(designator)) {
			reportError("Accessed designator " + designator.obj.getName() + " is not a method", factorMethodCall);
			factorMethodCall.struct = Tab.noType;
		} else if (!methodManager.methodHaveNoFormalParams(designator)) {
			reportError("You need to include actual params as this method has formal params", factorMethodCall);
			factorMethodCall.struct = Tab.noType;
		} else {
			factorMethodCall.struct = designator.obj.getType();
			reportInfo("FactorMethodCall", factorMethodCall);
		}
	}

	public void visit(FactorMethodCallWithActParams factorMethodCallWithActParams) {
		Designator designator = factorMethodCallWithActParams.getMethodNameCall().getDesignator();
		if (!methodManager.isDesignatorMethod(designator)) {
			reportError("Accessed designator " + designator.obj.getName() + " is not a method", factorMethodCallWithActParams);
			factorMethodCallWithActParams.struct = Tab.noType;
		} else if (!methodManager.areActParamsMathcingWithFormParamsForMethodDesignator(designator)) {
			reportError("Actual params in call of method " + designator.obj.getName() + " doesn't match formal params", factorMethodCallWithActParams);
			factorMethodCallWithActParams.struct = Tab.noType;
		} else {
			factorMethodCallWithActParams.struct = designator.obj.getType();
			reportInfo("FactorMethodCallWithActParams", factorMethodCallWithActParams);
		}
	}

	/* Rules for using variable or accessing element in the array */

	public void visit(DesignatorIdent designatorIdent) {
		String designatorName = designatorIdent.getDesignatorName();
		Obj designator = Tab.find(designatorName);
		if (designator == Tab.noObj) {
			reportError("Designator " + designatorName + " was not found", designatorIdent);
		} else {
			designatorIdent.obj = designator;
			reportInfo("DesignatorIdent", designatorIdent);
		}
	}

	public void visit(DesignatorArray designatorArray) {
		Designator designator = designatorArray.getDesignator();
		if (!exprManager.isCorrectTypeForIndexOfArray(designatorArray.getExpr())) {
			reportError("Type in expression for the index of the array must be int", designatorArray);
			designatorArray.obj = Tab.noObj;
		} else if (!exprManager.isDesignatorArray(designator)) {
			reportError("Type of the designator of the array must be array", designatorArray);
			designatorArray.obj = Tab.noObj;
		} else {
			Obj oldDesignatorObj = designator.obj;
			Obj newDesignatorObj = new Obj(Obj.Elem, oldDesignatorObj.getName(), oldDesignatorObj.getType().getElemType());
			designatorArray.obj = newDesignatorObj;
			reportInfo("DesignatorArray", designatorArray);
		}
	}

	/* Rules for the actual parameters */
	
	public void visit(MethodNameCall methodNameCall) {
		methodManager.startAddingActParams();
		reportInfo("MethodNameCall", methodNameCall);
	}

	public void visit(ActParamsSingle actParamsSingle) {
		methodManager.addActParam(actParamsSingle.getExpr().struct);
		reportInfo("ActParamsSingle", actParamsSingle);
	}

	public void visit(ActParamsMultiple actParamsMultiple) {
		methodManager.addActParam(actParamsMultiple.getExpr().struct);
		reportInfo("ActParamsMultiple", actParamsMultiple);
	}
	
	/* Rules for the statement */
	
	public void visit(StatementRead statementRead) {
		Designator designator = statementRead.getDesignator();
		if (designator.obj == null) {
			reportError("Designator doesn't exist", statementRead);
		} else if (!statementManager.isDesignatorKindCompatibleWithRead(designator)) {
			reportError("Kind of designator " + designator.obj.getName() + " is not compatible with read statement, it should be Var or Elem", statementRead);
		} else if (!statementManager.isDesignatorTypeCompatibleWithRead(designator)) {
			reportError("Type of designator " + designator.obj.getName() + " is not compatible with read statement, it should be int, char or bool", statementRead);
		} else {
			reportInfo("StatementRead", statementRead);
		}
	}
	
	public void visit(StatementPrint statementPrint) {
		if (!statementManager.isExprTypeCompatibleWithPrint(statementPrint.getExpr().struct)) {
			reportError("Expr type in the print statement must be int, char or bool", statementPrint);
		} else {
			reportInfo("StatementPrint", statementPrint);
		}
	}
	
	public void visit(StatementEmptyReturn statementEmptyReturn) {
		if (!methodManager.isAnalyzerCurrentlyInMethod()) {
			reportError("The return statement must be in the method", statementEmptyReturn);
		} else if (!methodManager.isReturnExprTypeCompatibleWithCurrentMethodReturnType(Tab.noType)) {
			reportError("The method doesn't return the correct type, it should return " + methodManager.getCurrentMethodReturnTypeFriendlyName(), statementEmptyReturn);
		} else {
			reportInfo("StatementEmptyReturn", statementEmptyReturn);
		}
	}
	
	public void visit(StatementValueReturn statementValueReturn) {
		if (!methodManager.isAnalyzerCurrentlyInMethod()) {
			reportError("The return statement must be in the method", statementValueReturn);
		} else if (!methodManager.isReturnExprTypeCompatibleWithCurrentMethodReturnType(statementValueReturn.getExpr().struct)) {
			reportError("The method doesn't return the correct type, it should return " + methodManager.getCurrentMethodReturnTypeFriendlyName(), statementValueReturn);
		} else {
			reportInfo("StatementValueReturn", statementValueReturn);
		}
	}
	
	public void visit(StatementIf statementIf) {
		if (statementIf.getIfCondition() instanceof ConditionSuccess) {
			Condition condition = ((ConditionSuccess) statementIf.getIfCondition()).getCondition();
			if (!controlFlowManager.isConditionTypeCorrectForControlFlow(condition)) {
				reportError("Condition inside parenthesis of the if must be of type boolean", statementIf);
			} else {
				reportInfo("StatementIf", statementIf);
			}
		}
	}
	
	public void visit(StatementIfElse statementIfElse) {
		if (statementIfElse.getIfCondition() instanceof ConditionSuccess) {
			Condition condition = ((ConditionSuccess) statementIfElse.getIfCondition()).getCondition();
			if (!controlFlowManager.isConditionTypeCorrectForControlFlow(condition)) {
				reportError("Condition inside parenthesis of the ifelse must be of type boolean", statementIfElse);
			} else {
				reportInfo("StatementIfElse", statementIfElse);
			}
		}
	}

	/* Rules for the loops */
	
	public void visit(StatementWhile statementWhile) {
		controlFlowManager.decreaseNumberOfNestedLoops();
		reportInfo("StatementWhile", statementWhile);
	}
	
	public void visit(StatementMap statementMap) {
		controlFlowManager.decreaseNumberOfNestedLoops();
		Designator assignMapDesignator = statementMap.getStatementMapHead().getDesignator();
		Designator mappedDesignator = statementMap.getStatementMapHead().getDesignator1();
		StatementMapIdent mapVariable = statementMap.getStatementMapIdent();
		String mapVariableName = mapVariable.getMapVariableName();
		if (!controlFlowManager.isDesignatorTypeCompatibleWithMap(assignMapDesignator)) {
			reportError("Designator " + assignMapDesignator.obj.getName() + " must be array when calling assigning map result into it", statementMap);
		} else if (!controlFlowManager.isDesignatorTypeCompatibleWithMap(mappedDesignator)) {
			reportError("Designator " + mappedDesignator.obj.getName() + " must be array when calling map on it", statementMap);
		} else if (!controlFlowManager.isMapVariableDefined(mapVariableName)) {
			reportError("Symbol " + mapVariableName + " is not declared in this scope", statementMap);
		} else {
			Obj mapVar = controlFlowManager.getObjFromTableBySymbolName(mapVariableName);
			mapVariable.obj = mapVar;
			Obj resultingArrayObj = new Obj(Obj.Var, "mapResultingArray", new Struct(Struct.Array, statementMap.getExpr().struct));
			if (!controlFlowManager.isMapVarTypeCompatibleWithMap(mapVar)) {
				reportError("Map variable " + mapVar.getName() + " must be variable when calling map", statementMap);
			} else if(!controlFlowManager.areTypesCompatibleInMap(mappedDesignator, mapVar)) {
				reportError("Elements in array " + mappedDesignator.obj.getName() + " and map variable " + mapVar.getName() + " must have the same type", statementMap);
			} else if (!controlFlowManager.areResultingArrayTypeAndArrayToAssignToTypeInMapCompatible(assignMapDesignator, resultingArrayObj)) {
				String assignMapDesignatorElemTypeFriendlyName = Utils.getFriendlyNameForType(assignMapDesignator.obj.getType());
				String resultingArrayElemTypeFriendlyName = Utils.getFriendlyNameForType(resultingArrayObj.getType());
				reportError("Array to be assigned into " + assignMapDesignator.obj.getName() + "(" + assignMapDesignatorElemTypeFriendlyName + ") and resulting array(" + resultingArrayElemTypeFriendlyName + ") must have the same type", statementMap);
			} else {
				reportInfo("StatementMap", statementMap);
			}
			designatorStatementManager.removeMapVariable(mapVariableName);
		}
	}
	
	public void visit(StatementBreak statementBreak) {
		if (!controlFlowManager.isBreakAllowed()) {
			reportError("Break is not allowed outside of the loops", statementBreak);
		} else {
			reportInfo("StatementBreak", statementBreak);
		}
	}
	
	public void visit(StatementContinue statementContinue) {
		if (!controlFlowManager.isBreakAllowed()) {
			reportError("Continue is not allowed outside of the loops", statementContinue);
		} else {
			reportInfo("StatementContinue", statementContinue);
		}
	}
	
	public void visit(StatementWhileHead statementWhileHead) {
		controlFlowManager.increaseNumberOfNestedLoops();
		reportInfo("StatementWhileHead", statementWhileHead);
	}
	
	public void visit(StatementMapHead statementMapHead) {
		controlFlowManager.increaseNumberOfNestedLoops();
		reportInfo("StatementMapHead", statementMapHead);
	}
	
	public void visit(StatementMapIdent statementMapIdent) {
		designatorStatementManager.addMapVariable(statementMapIdent.getMapVariableName());
		reportInfo("StatementMapIdent", statementMapIdent);
	}
	
	/* Rules for the designator statement */
	
	public void visit(DesignatorStatementAssignExprSuccess designatorStatementAssignExprSuccess) {
		Designator designator = designatorStatementAssignExprSuccess.getDesignator();
		Expr expr = designatorStatementAssignExprSuccess.getExpr();
		if (!designatorStatementManager.isDesignatorKindCorrectForAssign(designator)) {
			reportError("Designator " + designator.obj.getName() + " has to be variable or element of the array", designatorStatementAssignExprSuccess);
		} else if(!designatorStatementManager.isSameTypeOfDesignatorAndExprInAssign(designator, expr)) {
			reportError("Designator " + designator.obj.getName() + "(" + Utils.getFriendlyNameForType(designator.obj.getType()) + ") and Expr(" + Utils.getFriendlyNameForType(expr.struct) + ") don't have the same type", designatorStatementAssignExprSuccess);
		} else {
			reportInfo("DesignatorStatementAssignExprSuccess", designatorStatementAssignExprSuccess);
		}
	}
	
	public void visit(DesignatorStatementInc designatorStatementInc) {
		Designator designator = designatorStatementInc.getDesignator();
		if (!designatorStatementManager.isDesignatorKindCorrectForIncAndDec(designator)) {
			reportError("Designator " + designator.obj.getName() + " has to be variable or element of the array", designatorStatementInc);
		} else if (!designatorStatementManager.isDesignatorTypeCorrectForIncAndDec(designator)) {
			reportError("Designator " + designator.obj.getName() + " has to be int", designatorStatementInc);
		} else {
			reportInfo("DesignatorStatementInc", designatorStatementInc);
		}
	}
	
	public void visit(DesignatorStatementDec designatorStatementDec) {
		Designator designator = designatorStatementDec.getDesignator();
		if (!designatorStatementManager.isDesignatorKindCorrectForIncAndDec(designator)) {
			reportError("Designator " + designator.obj.getName() + " has to be variable or element of the array", designatorStatementDec);
		} else if (!designatorStatementManager.isDesignatorTypeCorrectForIncAndDec(designator)) {
			reportError("Designator " + designator.obj.getName() + " has to be int", designatorStatementDec);
		} else {
			reportInfo("DesignatorStatementDec", designatorStatementDec);
		}
	}
	
	public void visit(DesignatorStatementMethodCall designatorStatementMethodCall) {
		Designator designator = designatorStatementMethodCall.getMethodNameCall().getDesignator();
		if (!methodManager.isDesignatorMethod(designator)) {
			reportError("Accessed designator " + designator.obj.getName() + " is not a method", designatorStatementMethodCall);
		} else if (!methodManager.methodHaveNoFormalParams(designator)) {
			reportError("You need to include actual params as this method has formal params", designatorStatementMethodCall);
		} else {
			reportInfo("DesignatorStatementMethodCall", designatorStatementMethodCall);
		}
	}
	
	public void visit(DesignatorStatementMethodCallWithActParams designatorStatementMethodCallWithActParams) {
		Designator designator = designatorStatementMethodCallWithActParams.getMethodNameCall().getDesignator();
		if (!methodManager.isDesignatorMethod(designator)) {
			reportError("Accessed designator " + designator.obj.getName() + " is not a method", designatorStatementMethodCallWithActParams);
		} else if (!methodManager.areActParamsMathcingWithFormParamsForMethodDesignator(designator)) {
			reportError("Actual params in call of method " + designator.obj.getName() + " doesn't match formal params", designatorStatementMethodCallWithActParams);
		} else {
			reportInfo("DesignatorStatementMethodCallWithActParams", designatorStatementMethodCallWithActParams);
		}
	}
	
	/* Rules for the condition */
	
	public void visit(ConditionSingleItem conditionSingleItem) {
		CondTerm condTerm = conditionSingleItem.getCondTerm();
		if (controlFlowManager.isCondTermTypeCorrectForControlFlow(condTerm)) {
			conditionSingleItem.struct = condTerm.struct;
			reportInfo("ConditionSingleItem", conditionSingleItem);
		} else {
			reportError("CondTerm inside Condition must be of type boolean", conditionSingleItem);
			conditionSingleItem.struct = Tab.noType;
		}
	}
	
	public void visit(ConditionList conditionList) {
		CondTerm condTerm = conditionList.getCondTerm();
		Condition condition = conditionList.getCondition();
		if (controlFlowManager.areCompatibleTypesInConditionList(condTerm, condition)) {
			conditionList.struct = condTerm.struct;
			reportInfo("ConditionList", conditionList);
		} else {
			reportError("Types in ConditionList must be compatible", conditionList);
			conditionList.struct = Tab.noType;
		}
	}
	
	public void visit(CondTermSingleItem condTermSingleItem) {
		CondFact condFact = condTermSingleItem.getCondFact();
		if (controlFlowManager.isCondFactTypeCorrectForControlFlow(condFact)) {
			condTermSingleItem.struct = condFact.struct;
			reportInfo("CondTermSingleItem", condTermSingleItem);
		} else {
			reportError("CondFact inside CondTerm must be of type boolean", condTermSingleItem);
			condTermSingleItem.struct = Tab.noType;
		}
	}
	
	public void visit(CondTermList condTermList) {
		CondFact condFact = condTermList.getCondFact();
		CondTerm condTerm = condTermList.getCondTerm();
		if (controlFlowManager.areCompatibleTypesInCondTermList(condFact, condTerm)) {
			condTermList.struct = condTerm.struct;
			reportInfo("CondTermList", condTermList);
		} else {
			reportError("Types in CondTermList must be compatible", condTermList);
			condTermList.struct = Tab.noType;
		}
	}
	
	public void visit(CondFactSingleExpr condFactSingleExpr) {
		Expr expr = condFactSingleExpr.getExpr();
		if (controlFlowManager.isExprTypeCorrectForControlFlow(expr)) {
			condFactSingleExpr.struct = expr.struct;
			reportInfo("CondFactSingleExpr", condFactSingleExpr);
		} else {
			reportError("Expr inside CondFact must be of type boolean", condFactSingleExpr);
			condFactSingleExpr.struct = Tab.noType;
		}
	}
	
	public void visit(CondFactDoubleExpr condFactDoubleExpr) {
		Expr leftExpr = condFactDoubleExpr.getExpr();
		Relop relop = condFactDoubleExpr.getRelop();
		Expr rightExpr = condFactDoubleExpr.getExpr1();
		if (controlFlowManager.areExprTypesCorrectForRelop(leftExpr, relop, rightExpr)) {
			condFactDoubleExpr.struct = TabExtended.boolType;
			reportInfo("CondFactDoubleExpr", condFactDoubleExpr);
		} else {
			reportError("Expr inside CondFactDoubleExpr are not compatible with Relop, arrays can only be used with equals and not equals or leftExpr is not compatible with rightExpr", condFactDoubleExpr);
			condFactDoubleExpr.struct = Tab.noType;
		}
	}
}
