package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.CondFactDoubleExpr;
import rs.ac.bg.etf.pp1.ast.CondFactSingleExpr;
import rs.ac.bg.etf.pp1.ast.ConditionOrBlock;
import rs.ac.bg.etf.pp1.ast.ConstValueBool;
import rs.ac.bg.etf.pp1.ast.ConstValueChar;
import rs.ac.bg.etf.pp1.ast.ConstValueNumber;
import rs.ac.bg.etf.pp1.ast.Designator;
import rs.ac.bg.etf.pp1.ast.DesignatorArray;
import rs.ac.bg.etf.pp1.ast.DesignatorArrayStart;
import rs.ac.bg.etf.pp1.ast.DesignatorIdent;
import rs.ac.bg.etf.pp1.ast.DesignatorStatementAssignExprSuccess;
import rs.ac.bg.etf.pp1.ast.DesignatorStatementDec;
import rs.ac.bg.etf.pp1.ast.DesignatorStatementInc;
import rs.ac.bg.etf.pp1.ast.DesignatorStatementMethodCall;
import rs.ac.bg.etf.pp1.ast.DesignatorStatementMethodCallWithActParams;
import rs.ac.bg.etf.pp1.ast.Equals;
import rs.ac.bg.etf.pp1.ast.ExprNegativeFirstTerm;
import rs.ac.bg.etf.pp1.ast.FactorArray;
import rs.ac.bg.etf.pp1.ast.FactorBool;
import rs.ac.bg.etf.pp1.ast.FactorChar;
import rs.ac.bg.etf.pp1.ast.FactorDesignator;
import rs.ac.bg.etf.pp1.ast.FactorMatrix;
import rs.ac.bg.etf.pp1.ast.FactorMethodCall;
import rs.ac.bg.etf.pp1.ast.FactorMethodCallWithActParams;
import rs.ac.bg.etf.pp1.ast.FactorNumber;
import rs.ac.bg.etf.pp1.ast.Greater;
import rs.ac.bg.etf.pp1.ast.GreaterEquals;
import rs.ac.bg.etf.pp1.ast.Less;
import rs.ac.bg.etf.pp1.ast.LessEquals;
import rs.ac.bg.etf.pp1.ast.MethodAnyReturnType;
import rs.ac.bg.etf.pp1.ast.MethodDecl;
import rs.ac.bg.etf.pp1.ast.MethodName;
import rs.ac.bg.etf.pp1.ast.MethodVoidReturnType;
import rs.ac.bg.etf.pp1.ast.Mulop;
import rs.ac.bg.etf.pp1.ast.NotEquals;
import rs.ac.bg.etf.pp1.ast.PrintNumConst;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.ast.StatementBreak;
import rs.ac.bg.etf.pp1.ast.StatementContinue;
import rs.ac.bg.etf.pp1.ast.StatementEmptyReturn;
import rs.ac.bg.etf.pp1.ast.StatementIf;
import rs.ac.bg.etf.pp1.ast.StatementIfConditionEnd;
import rs.ac.bg.etf.pp1.ast.StatementIfElse;
import rs.ac.bg.etf.pp1.ast.StatementIfEnd;
import rs.ac.bg.etf.pp1.ast.StatementIfStart;
import rs.ac.bg.etf.pp1.ast.StatementMap;
import rs.ac.bg.etf.pp1.ast.StatementMapHead;
import rs.ac.bg.etf.pp1.ast.StatementPrint;
import rs.ac.bg.etf.pp1.ast.StatementRead;
import rs.ac.bg.etf.pp1.ast.StatementValueReturn;
import rs.ac.bg.etf.pp1.ast.StatementWhile;
import rs.ac.bg.etf.pp1.ast.StatementWhileHead;
import rs.ac.bg.etf.pp1.ast.StatementWhileStart;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.ast.TermListMultiple;
import rs.ac.bg.etf.pp1.ast.TermMultipleFactor;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.ac.bg.etf.pp1.helpers.Utils;
import rs.ac.bg.etf.pp1.helpers.codegeneration.ControlFlowCodeGenerationManager;
import rs.ac.bg.etf.pp1.helpers.codegeneration.DesignatorStatementCodeGenerationManager;
import rs.ac.bg.etf.pp1.helpers.codegeneration.ExprCodeGenerationManager;
import rs.ac.bg.etf.pp1.helpers.codegeneration.MethodCodeGenerationManager;
import rs.ac.bg.etf.pp1.helpers.codegeneration.StatementCodeGenerationManager;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	private final int MAX_SOURCE_CODE_SIZE = 8192;
	private final int CHAR_PRINT_WIDTH = 1;
	private final int INT_PRINT_WIDTH = 5;
	
	private Logger log = Logger.getLogger(getClass());
	private int mainFunctionPc;
	
	private MethodCodeGenerationManager methodManager = new MethodCodeGenerationManager();
	private StatementCodeGenerationManager statementManager = new StatementCodeGenerationManager();
	private DesignatorStatementCodeGenerationManager designatorStatementManager = new DesignatorStatementCodeGenerationManager();
	private ExprCodeGenerationManager exprManager = new ExprCodeGenerationManager();
	private ControlFlowCodeGenerationManager controlFlowManager = new ControlFlowCodeGenerationManager();

	public int getMainFunctionPc() {
		return mainFunctionPc;
	}
	
	public void visit(Program program) {
		if (Code.pc > MAX_SOURCE_CODE_SIZE) {
			reportError("Source code can't be bigger than " + Utils.getUserFriendlyStringForNumberOfBytes(MAX_SOURCE_CODE_SIZE), program);
		}
	}

	public void visit(MethodName methodName) {
		if (methodManager.isMainMethod(methodName.getMethodName())) {
			mainFunctionPc = Code.pc;
		}
		methodName.obj.setAdr(Code.pc);
		Code.put(Code.enter);
		int numberOfParams = methodManager.getNumberOfParams(methodName.obj);
		int numberOfLocalVars = methodManager.getNumberOfLocalVars(methodName.obj);
		Code.put(numberOfParams);
		Code.put(numberOfLocalVars + numberOfParams);
	}
	
	public void visit(MethodAnyReturnType methodAnyReturnType) {
		methodManager.setCurrentMethodReturnType(methodAnyReturnType.getType().struct);
	}
	
	public void visit(MethodVoidReturnType methodVoidReturnType) {
		methodManager.setCurrentMethodReturnType(Tab.noType);
	}
	
	public void visit(MethodDecl methodDecl) {
		if (methodManager.isErrorInReturn()) {
			Code.put(Code.trap);
			Code.put(0);
		} else if(methodManager.isVoidMethodWithoutReturn()) {
			Code.put(Code.exit);
			Code.put(Code.return_);
		}
		methodManager.reset();
	}
	
	public void visit(StatementValueReturn statementValueReturn) {
		methodManager.setMethodHasReturn();
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(StatementEmptyReturn statementEmptyReturn) {
		methodManager.setMethodHasReturn();
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(ConstValueNumber constValueNumber) {
		Obj constObj = Tab.insert(Obj.Con, "$", Tab.intType);
		constObj.setLevel(0);
		constObj.setAdr(constValueNumber.getValue());
		Code.load(constObj);
	}
	
	public void visit(ConstValueChar constValueChar) {
		Obj constObj = Tab.insert(Obj.Con, "$", Tab.charType);
		constObj.setLevel(0);
		constObj.setAdr(constValueChar.getValue());
		Code.load(constObj);
	}

	public void visit(ConstValueBool constValueBool) {
		Obj constObj = Tab.insert(Obj.Con, "$", TabExtended.boolType);
		constObj.setLevel(0);
		constObj.setAdr(constValueBool.getValue().equals("true") ? 1 : 0);
		Code.load(constObj);
	}
	
	public void visit(StatementRead statementRead) {
		Obj designatorObj = statementRead.getDesignator().obj;
		if (statementManager.useBRead(designatorObj)) {
			Code.put(Code.bread);
		} else {
			Code.put(Code.read);
		}
		Code.store(designatorObj);
	}
	
	public void visit(StatementPrint statementPrint) {
		Struct exprType = statementPrint.getExpr().struct;
		
		int width;
		if (statementManager.isPrintWidthDefined(statementPrint)) {
			width = statementManager.getPrintWidth(statementPrint);
		} else if (statementManager.useBPrint(exprType)) {
			width = CHAR_PRINT_WIDTH;
		} else {
			width = INT_PRINT_WIDTH;
		}
		
		Code.loadConst(width);
	
		if (statementManager.useBPrint(exprType)) {
			Code.put(Code.bprint);
		} else {
			Code.put(Code.print);
		}
	}
	
	public void visit(StatementIf statementIf) {
		controlFlowManager.finishIf();
	}
	
	public void visit(StatementIfElse statementIfElse) {
		controlFlowManager.fixupDestinationsFromIfBlock();
		controlFlowManager.finishIf();
	}
	
	public void visit(StatementIfStart statementIfStart) {
		controlFlowManager.startIf();
	}
	
	public void visit(StatementIfConditionEnd statementIfConditionEnd) {
		controlFlowManager.fixupDestinationsFromOrBlock();
	}
	
	public void visit(StatementIfEnd statementIfEnd) {
		if (controlFlowManager.isInsideIfElse(statementIfEnd)) {
			Code.putJump(0);
			controlFlowManager.addIfBlockDestinationToFix(Code.pc - 2);
		}
		
		controlFlowManager.fixupDestinationsFromAndBlock();
	}
	
	public void visit(ConditionOrBlock conditionOrBlock) {
		Code.putJump(0);
		controlFlowManager.addOrBlockDestinationToFix(Code.pc - 2);
		controlFlowManager.fixupDestinationsFromAndBlock();
	}
	
	public void visit(CondFactSingleExpr condFactSingleExpr) {
		Code.loadConst(1);
		Code.putFalseJump(Code.eq, 0);
		controlFlowManager.addAndBlockDestinationToFix(Code.pc - 2);
	}
	
	public void visit(CondFactDoubleExpr condFactDoubleExpr) {
		int operationCode = controlFlowManager.getOperationCodeForRelop(condFactDoubleExpr.getRelop());
		Code.putFalseJump(operationCode, 0);
		controlFlowManager.addAndBlockDestinationToFix(Code.pc - 2);
	}
	
	public void visit(StatementWhile statementWhile) {
		controlFlowManager.jumpToBeginingOfWhile();
		controlFlowManager.fixupDestinationsFromAndBlock();
		controlFlowManager.fixupDestinationsFromBreakBlock();
		controlFlowManager.finishWhile();
	}
	
	public void visit(StatementWhileHead statementWhileHead) {
		controlFlowManager.fixupDestinationsFromOrBlock();
	}
	
	public void visit(StatementWhileStart statementWhileStart) {
		controlFlowManager.startWhile(Code.pc);
	}
	
	public void visit(StatementContinue statementContinue) {
		controlFlowManager.continueToBeginingOfWhile();
	}
	
	public void visit(StatementBreak statementBreak) {
		Code.putJump(0);
		controlFlowManager.addBreakBlockDestinationToFix(Code.pc - 2);
	}
	
	public void visit(StatementMap statementMap) {
		Obj variableInMap = statementMap.getStatementMapHead().getStatementMapIdent().obj;
		Code.store(variableInMap);
		
		Obj index = new Obj(Obj.Var, "index", Tab.intType);
		Code.put(Code.dup);
		Code.store(index);
		
		Code.load(statementMap.getStatementMapHead().getDesignator().obj);
		Code.load(index);
		Code.load(variableInMap);
		if (exprManager.isCharVariable(variableInMap.getType())) {
			Code.put(Code.bastore);
		} else {
			Code.put(Code.astore); 
		}
		
		controlFlowManager.jumpToBeginingOfMap();
		controlFlowManager.fixupDestinationFromMapBlock();
		Code.put(Code.pop);
		Code.put(Code.pop);
		Code.put(Code.pop);
	}
	
	public void visit(StatementMapHead statementMapHead) {
		Obj arrayToAssignTo = statementMapHead.getDesignator().obj;
		Obj arrayToBeMapped = statementMapHead.getDesignator1().obj;
		Obj variableInsideMap = statementMapHead.getStatementMapIdent().obj;
		
		Code.load(arrayToBeMapped);
		Code.put(Code.arraylength);
		Code.put(Code.newarray);
		if (exprManager.isCharArray(arrayToAssignTo.getType())) {
			Code.put(0);
		} else {
			Code.put(1);
		}
		Code.store(arrayToAssignTo);
		
		Code.load(arrayToBeMapped);
		Code.loadConst(-1);

		controlFlowManager.addDestinationOfBeginingOfMap(Code.pc);
		
		Code.loadConst(1);
		Code.put(Code.add);
		
		Code.put(Code.dup2);
		Code.put(Code.dup2);
		
		Code.put(Code.pop);
		Code.put(Code.arraylength);
		
		Code.putFalseJump(Code.lt, 0);
		controlFlowManager.addMapBlockDestinationToFix(Code.pc - 2);
		
		Code.put(Code.dup2);
		Code.put(Code.pop);
		Code.put(Code.aload);
		Code.store(variableInsideMap);
	}
	
	public void visit(DesignatorStatementAssignExprSuccess designatorStatementAssignExprSuccess) {
		Code.store(designatorStatementAssignExprSuccess.getDesignator().obj);
	}
	
	public void visit(DesignatorStatementInc designatorStatementInc) {
		Designator designator = designatorStatementInc.getDesignator();
		if (designatorStatementManager.isDesignatorElementInArrayOrMatrix(designator)) {
			Code.put(Code.dup2);
		}
		Code.load(designator.obj);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(designator.obj);
	}
	
	public void visit(DesignatorStatementDec designatorStatementDec) {
		Designator designator = designatorStatementDec.getDesignator();
		if (designatorStatementManager.isDesignatorElementInArrayOrMatrix(designator)) {
			Code.put(Code.dup2);
		}
		Code.load(designator.obj);
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(designator.obj);
	}
	
	public void visit(DesignatorStatementMethodCall designatorStatementMethodCall) {
		Obj methodDesignatorObj = designatorStatementMethodCall.getMethodNameCall().getDesignator().obj;
		processMethodCall(methodDesignatorObj, true);
	}
	
	public void visit(DesignatorStatementMethodCallWithActParams designatorStatementMethodCallWithActParams) {
		Obj methodDesignatorObj = designatorStatementMethodCallWithActParams.getMethodNameCall().getDesignator().obj;
		processMethodCall(methodDesignatorObj, true);
	}
	
	public void visit(ExprNegativeFirstTerm exprNegativeFirstTerm) {
		Code.put(Code.neg);
	}
	
	public void visit(TermListMultiple termListMultiple) {
		if (exprManager.isAddopAddition(termListMultiple.getAddop())) {
			Code.put(Code.add);
		} else {
			Code.put(Code.sub);
		}
	}
	
	public void visit(TermMultipleFactor termMultipleFactor) {
		Mulop mulop = termMultipleFactor.getMulop();
		if (exprManager.isMulopMultiplication(mulop)) {
			Code.put(Code.mul);
		} else if (exprManager.isMulopDividion(mulop)) {
			Code.put(Code.div);
		} else {
			Code.put(Code.rem);
		}
	}
	
	public void visit(FactorNumber factorNumber) {
		Obj constObj = Tab.insert(Obj.Con, "$", Tab.intType);
		constObj.setLevel(0);
		constObj.setAdr(factorNumber.getN1());
		Code.load(constObj);
	}
	
	public void visit(FactorChar factorChar) {
		Obj constObj = Tab.insert(Obj.Con, "$", Tab.charType);
		constObj.setLevel(0);
		constObj.setAdr(factorChar.getC1());
		Code.load(constObj);
	}
	
	public void visit(FactorBool factorBool) {
		Obj constObj = Tab.insert(Obj.Con, "$", TabExtended.boolType);
		constObj.setLevel(0);
		constObj.setAdr(factorBool.getB1().equals("true") ? 1 : 0);
		Code.load(constObj);
	}
	
	public void visit(FactorDesignator factorDesignator) {
		Code.load(factorDesignator.getDesignator().obj);
	}
	
	public void visit(FactorArray factorArray) {
		Code.put(Code.newarray);
		if (exprManager.isCharArray(factorArray.struct)) {
			Code.put(0);
		} else {
			Code.put(1);
		}
	}
	
	public void visit(FactorMatrix factorMatrix) { // rowCnt, colCnt
		Code.put(Code.dup_x1); // colCnt, rowCnt, colCnts
		Code.put(Code.pop); // colCnt, rowCnt
		
		Code.put(Code.newarray);
		if (exprManager.isCharMatrix(factorMatrix)) {
			Code.put(0);
		} else {
			Code.put(1);
		} // colCnt, firstDimArr
		Obj firstDimension = new Obj(Obj.Var, "firstDimension", Tab.intType);
		Code.store(firstDimension); // colCnt
		
		Code.loadConst(-1); // colCnt, i
		
		int loopBegin = Code.pc;
		
		Code.loadConst(1); // colCnt, i, 1
		Code.put(Code.add); // colCnt, i + 1
		
		Code.put(Code.dup); // colCnt, i, i
		Code.load(firstDimension); // colCnt, i, i, firstDimArr
		Code.put(Code.arraylength); // colCnt, i, i, firstDimArrLength
		Code.putFalseJump(Code.lt, 0); // colCnt, i
		int destinationToFixup = Code.pc - 2;
		
		Code.put(Code.dup2); // colCnt, i, colCnt, i
		Code.put(Code.pop); // colCnt, i, colCnt
		
		Code.put(Code.newarray);
		if (exprManager.isCharMatrix(factorMatrix)) {
			Code.put(0);
		} else {
			Code.put(1);
		} // colCnt, i, secondDimArr
		Code.put(Code.dup2); // colCnt, i, secondDimArr, i, secondDimArr
		Code.load(firstDimension); // colCnt, i, secondDimArr, i, secondDimArr, firstDimArr
		Code.put(Code.dup_x2); // colCnt, i, secondDimArr, firstDimArr, i, secondDimArr, firstDimArr
		Code.put(Code.pop); // colCnt, i, secondDimArr, firstDimArr, i, secondDimArr
		if (exprManager.isCharMatrix(factorMatrix)) {
			Code.put(Code.bastore);
		} else {
			Code.put(Code.astore);
		} // colCnt, i, secondDimArr
		Code.put(Code.pop); // colCnt, i
		
		Code.putJump(loopBegin);
		
		Code.fixup(destinationToFixup);
		
		Code.put(Code.pop);
		Code.put(Code.pop);
		Code.load(firstDimension);
	}
	
	public void visit(FactorMethodCall factorMethodCall) {
		Obj methodDesignatorObj = factorMethodCall.getMethodNameCall().getDesignator().obj;
		processMethodCall(methodDesignatorObj, false);
	}
	
	public void visit(FactorMethodCallWithActParams factorMethodCallWithActParams) {
		Obj methodDesignatorObj = factorMethodCallWithActParams.getMethodNameCall().getDesignator().obj;
		processMethodCall(methodDesignatorObj, false);
	}
	
	public void visit(DesignatorArrayStart designatorArrayStart) {
		Code.load(designatorArrayStart.getDesignator().obj);
	}

	private void reportError(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" on line ").append(line);
		log.error(msg.toString());
	}
	
	private void processMethodCall(Obj methodDesignatorObj, boolean popReturnedValue) {
		String methodName = methodDesignatorObj.getName();
		if (methodManager.isLenFunction(methodName)) {
			Code.put(Code.arraylength);
		} else if (!methodManager.isChrOrOrdFunction(methodName)) {
			int offset = methodDesignatorObj.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(offset);
		}
		
		if (methodManager.methodHasReturnValue(methodDesignatorObj) && popReturnedValue) {
			Code.put(Code.pop);
		}
	}
}
