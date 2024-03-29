package rs.ac.bg.etf.pp1.helpers.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.helpers.Utils;
import rs.ac.bg.etf.pp1.helpers.base.MethodManager;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class MethodSyntaxParsingManager extends MethodManager {

	private Obj currentMethod;
	private boolean mainMethodExists = false;
	private List<Struct> formParams = new ArrayList<Struct>();
	private Stack<List<Struct>>  allActParams = new Stack<List<Struct>>();
	private Map<String, List<Struct>> formalParamsPerMethods = new HashMap<String, List<Struct>>();
	
	public Obj setCurrentMethod(String methodName) {
		currentMethodName = methodName;
		currentMethod = Tab.insert(Obj.Meth, methodName, currentMethodReturnType);
		return currentMethod;
	}
	
	public Obj getCurrentMethod() {
		return currentMethod;
	}

	public boolean isMethodCorrect() {
		int numberOfFormParms = formParams.size();
		if (isMainMethod(currentMethodName)) {
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
	
	public int addFormParam(Struct formParamType) {
		formParams.add(formParamType);
		return formParams.size() - 1;
	}
	
	public void startAddingActParams() {
		allActParams.push(new ArrayList<Struct>());
	}
	
	public void addActParam(Struct actParamType) {
		allActParams.peek().add(actParamType);
	}
	
	public boolean isDesignatorMethod(Designator designator) {
		return designator.obj.getKind() == Obj.Meth;
	}
	
	public boolean areActParamsMathcingWithFormParamsForMethodDesignator(Designator designator) {
		List<Struct> formParams = getFormParamsForMethodDesignator(designator);
		List<Struct> actParams = allActParams.pop();
		
		if (formParams.size() != actParams.size()) return false;
		
		for (int i = 0; i < formParams.size(); i++) {
			Struct formType = formParams.get(i);
			Struct actType = actParams.get(i);
			if (!actType.assignableTo(formType)) return false;
		}
		
		return true;
	}
	
	public boolean methodHaveNoFormalParams(Designator designator) {
		List<Struct> formParams = getFormParamsForMethodDesignator(designator);
		return formParams.size() == 0;
	}
	
	public boolean isReturnExprTypeCompatibleWithCurrentMethodReturnType(Struct exprType) {
		isMethodReturnedCorrectly = currentMethodReturnType == exprType;
		return isMethodReturnedCorrectly;
	}
	
	public String getCurrentMethodReturnTypeFriendlyName() {
		return Utils.getFriendlyNameForType(currentMethodReturnType);
	}
	
	public boolean isAnalyzerCurrentlyInMethod() {
		return currentMethod != null;
	}
	
	private List<Struct> getFormParamsForMethodDesignator(Designator designator) {
		String methodName = designator.obj.getName();
		List<Struct> formParams = formalParamsPerMethods.get(methodName);
		
		if (formParams == null) {
			formParams = new ArrayList<Struct>();
			Obj predefinedMethod = Tab.find(methodName);
			if (predefinedMethod != null && predefinedMethod.getKind() == Obj.Meth) {
				for (Obj localSymbol : predefinedMethod.getLocalSymbols()) {
					formParams.add(localSymbol.getType());
				}
				return formParams;
			}
		}
		
		return formParams;
	}
}
