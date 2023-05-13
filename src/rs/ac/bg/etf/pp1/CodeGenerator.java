package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.MethodAnyReturnType;
import rs.ac.bg.etf.pp1.ast.MethodDecl;
import rs.ac.bg.etf.pp1.ast.MethodName;
import rs.ac.bg.etf.pp1.ast.MethodVoidReturnType;
import rs.ac.bg.etf.pp1.ast.StatementEmptyReturn;
import rs.ac.bg.etf.pp1.ast.StatementValueReturn;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.ac.bg.etf.pp1.helpers.codegeneration.MethodCodeGenerationManager;
import rs.ac.bg.etf.pp1.helpers.syntax.MethodSyntaxParsingManager;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;

public class CodeGenerator extends VisitorAdaptor {
	private int mainFunctionPc;
	
	private MethodCodeGenerationManager methodManager = new MethodCodeGenerationManager();

	public int getMainFunctionPc() {
		return mainFunctionPc;
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
}
