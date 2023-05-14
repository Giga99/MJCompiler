package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.ConstValueBool;
import rs.ac.bg.etf.pp1.ast.ConstValueChar;
import rs.ac.bg.etf.pp1.ast.ConstValueNumber;
import rs.ac.bg.etf.pp1.ast.MethodAnyReturnType;
import rs.ac.bg.etf.pp1.ast.MethodDecl;
import rs.ac.bg.etf.pp1.ast.MethodName;
import rs.ac.bg.etf.pp1.ast.MethodVoidReturnType;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.ast.StatementEmptyReturn;
import rs.ac.bg.etf.pp1.ast.StatementValueReturn;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.ac.bg.etf.pp1.helpers.Utils;
import rs.ac.bg.etf.pp1.helpers.codegeneration.MethodCodeGenerationManager;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;

public class CodeGenerator extends VisitorAdaptor {
	private final int MAX_SOURCE_CODE_SIZE = 8192;
	
	private Logger log = Logger.getLogger(getClass());
	private int mainFunctionPc;
	
	private MethodCodeGenerationManager methodManager = new MethodCodeGenerationManager();

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
		Obj constant = Tab.insert(Obj.Con, "$", Tab.intType);
		constant.setLevel(0);
		constant.setAdr(constValueNumber.getValue());
		Code.load(constant);
	}
	
	public void visit(ConstValueChar constValueChar) {
		Obj constant = Tab.insert(Obj.Con, "$", Tab.charType);
		constant.setLevel(0);
		constant.setAdr(constValueChar.getValue());
		Code.load(constant);
	}

	public void visit(ConstValueBool constValueBool) {
		Obj constant = Tab.insert(Obj.Con, "$", TabExtended.boolType);
		constant.setLevel(0);
		constant.setAdr(constValueBool.getValue().equals("true") ? 1 : 0);
		Code.load(constant);
	}
}
