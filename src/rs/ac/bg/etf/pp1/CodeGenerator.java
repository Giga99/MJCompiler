package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.ConstValueBool;
import rs.ac.bg.etf.pp1.ast.ConstValueChar;
import rs.ac.bg.etf.pp1.ast.ConstValueNumber;
import rs.ac.bg.etf.pp1.ast.Designator;
import rs.ac.bg.etf.pp1.ast.DesignatorStatementAssignExprSuccess;
import rs.ac.bg.etf.pp1.ast.DesignatorStatementDec;
import rs.ac.bg.etf.pp1.ast.DesignatorStatementInc;
import rs.ac.bg.etf.pp1.ast.ExprNegativeFirstTerm;
import rs.ac.bg.etf.pp1.ast.FactorBool;
import rs.ac.bg.etf.pp1.ast.FactorChar;
import rs.ac.bg.etf.pp1.ast.FactorNumber;
import rs.ac.bg.etf.pp1.ast.MethodAnyReturnType;
import rs.ac.bg.etf.pp1.ast.MethodDecl;
import rs.ac.bg.etf.pp1.ast.MethodName;
import rs.ac.bg.etf.pp1.ast.MethodVoidReturnType;
import rs.ac.bg.etf.pp1.ast.Mulop;
import rs.ac.bg.etf.pp1.ast.PrintNumConst;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.ast.StatementEmptyReturn;
import rs.ac.bg.etf.pp1.ast.StatementPrint;
import rs.ac.bg.etf.pp1.ast.StatementRead;
import rs.ac.bg.etf.pp1.ast.StatementValueReturn;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.ast.TermListMultiple;
import rs.ac.bg.etf.pp1.ast.TermMultipleFactor;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.ac.bg.etf.pp1.helpers.Utils;
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

	public int getMainFunctionPc() {
		return mainFunctionPc;
	}

	public void reportError(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" on line ").append(line);
		log.error(msg.toString());
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
}
