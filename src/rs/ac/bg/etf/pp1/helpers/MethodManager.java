package rs.ac.bg.etf.pp1.helpers;

import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class MethodManager {

	private String MAIN_METHOD = "main";

	private Struct currentMethodReturnType;
	private String currentMethodName;
	private Obj currentMethod;
	private boolean isMethodReturnedCorrectly;
	private boolean mainMethodExists = false;
	private int numberOfFormParms = 0;

	public void setCurrentMethodReturnType(Struct methodReturnType) {
		currentMethodReturnType = methodReturnType;
		isMethodReturnedCorrectly = methodReturnType == Tab.noType;
	}
	
	public void setCurrentMethod(String methodName) {
		currentMethodName = methodName;
		currentMethod = Tab.insert(Obj.Meth, methodName, currentMethodReturnType);
	}
	
	public Obj getCurrentMethod() {
		return currentMethod;
	}

	public boolean isMethodCorrect() {
		if (currentMethodName.equals(MAIN_METHOD)) {
			mainMethodExists = true;
			return currentMethodReturnType == Tab.noType && numberOfFormParms == 0;
		} else {
			return isMethodReturnedCorrectly;
		}
	}
	
	public boolean isMainMethodPresent() {
		return mainMethodExists;
	}
	
	public void reset() {
		currentMethodReturnType = null;
		currentMethodName = null;
		currentMethod = null;
	}
}
