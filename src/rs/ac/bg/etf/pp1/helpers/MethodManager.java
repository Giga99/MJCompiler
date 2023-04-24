package rs.ac.bg.etf.pp1.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class MethodManager {

	private String MAIN_METHOD = "main";

	private Struct currentMethodReturnType;
	private String currentMethodName;
	private Obj currentMethod;
	private boolean isMethodReturnedCorrectly = false;
	private boolean mainMethodExists = false;
	private List<Struct> formParams = new ArrayList<Struct>();
	private List<Struct> actParams = new ArrayList<Struct>();
	private Map<String, List<Struct>> formalParamsPerMethods = new HashMap<String, List<Struct>>();

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
		int numberOfFormParms = formParams.size();
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
	
	public void finishMethod() {
		List<Struct> methodFormParams = new ArrayList<Struct>(formParams);
		formalParamsPerMethods.put(currentMethodName, methodFormParams);
		int numberOfFormParms = formParams.size();
		currentMethod.setLevel(numberOfFormParms);
		currentMethodReturnType = null;
		currentMethodName = null;
		currentMethod = null;
		formParams.clear();
	}
	
	public void addFormParam(Struct formParamType) {
		formParams.add(formParamType);
	}
	
	public void addActParam(Struct actParamType) {
		actParams.add(actParamType);
	}
	
	public boolean isDesignatorMethod(Designator designator) {
		return designator.obj.getKind() == Obj.Meth;
	}
	
	public boolean areActParamsMathcingWithFormParamsForMethodDesignator(Designator designator) {
		List<Struct> formParams = getFormParamsForMethodDesignator(designator);
		
		if (formParams.size() != actParams.size()) return false;
		
		for (int i = 0; i < formParams.size(); i++) {
			Struct formType = formParams.get(i);
			Struct actType = actParams.get(i);
			if (!actType.assignableTo(formType)) return false;
		}
		
		return true;
	}
	
	public void resetActParams() {
		actParams.clear();
	}
	
	private List<Struct> getFormParamsForMethodDesignator(Designator designator) {
		return formalParamsPerMethods.get(designator.obj.getName());
	}
}
