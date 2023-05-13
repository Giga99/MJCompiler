package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.MethodAnyReturnType;
import rs.ac.bg.etf.pp1.ast.MethodDecl;
import rs.ac.bg.etf.pp1.ast.MethodName;
import rs.ac.bg.etf.pp1.ast.MethodVoidReturnType;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.ac.bg.etf.pp1.helpers.syntax.MethodManager;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;

public class CodeGenerator extends VisitorAdaptor {
	private int mainFunctionPc;
	
	private MethodManager methodManager = new MethodManager();

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
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(MethodVoidReturnType methodVoidReturnType) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(MethodDecl methodDecl) {
		if (methodDecl.getMethodName().obj.getType() != Tab.noType) {
			Code.put(Code.trap);
			Code.put(0);	
		} 
		if (methodDecl.getMethodName().obj.getType() == Tab.noType) {
			Code.put(Code.exit);
			Code.put(Code.return_);
		}
	}
}
