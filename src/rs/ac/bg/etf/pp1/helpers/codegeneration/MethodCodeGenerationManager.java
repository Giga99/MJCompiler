package rs.ac.bg.etf.pp1.helpers.codegeneration;

import java.util.Collection;

import rs.ac.bg.etf.pp1.ast.MethodName;
import rs.ac.bg.etf.pp1.helpers.base.MethodManager;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;

public class MethodCodeGenerationManager extends MethodManager {
	
	private boolean methodHasReturn = false;
	
	public void setCurrentMethod(MethodName methodName) {
		currentMethodName = methodName.getMethodName();
	}
	
	public int getNumberOfParams(Obj methodObj) {
		Collection<Obj> localSymbols = methodObj.getLocalSymbols();
		int count = 0;
		for (Obj symbol: localSymbols) { 
			if (symbol.getFpPos() != -1) count++;
		}
		return count;
	}

	public int getNumberOfLocalVars(Obj methodObj) {
		Collection<Obj> localSymbols = methodObj.getLocalSymbols();
		int count = 0;
		for (Obj symbol: localSymbols) { 
			if (symbol.getFpPos() == -1) count++;
		}
		return count;
	}
	
	public void setMethodHasReturn() {
		methodHasReturn = true;
	}
	
	public boolean isErrorInReturn() {
		return currentMethodReturnType != Tab.noType && !methodHasReturn;
	}
	
	public boolean isVoidMethodWithoutReturn() {
		return currentMethodReturnType == Tab.noType && !methodHasReturn;
	}
	
	public boolean isLenFunction(String methodName) {
		return methodName == "len";
	}
	
	public boolean isChrOrOrdFunction(String methodName) {
		return methodName == "chr" || methodName == "ord";
	}
	
	public void reset() {
		methodHasReturn = false;
	}
	
	public boolean methodHasReturnValue(Obj methodDesignatorObj) {
		return methodDesignatorObj.getType() != Tab.noType;
	}
}
