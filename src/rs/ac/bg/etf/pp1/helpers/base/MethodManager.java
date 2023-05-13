package rs.ac.bg.etf.pp1.helpers.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.helpers.Utils;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class MethodManager {

	private String MAIN_METHOD = "main";

	protected String currentMethodName;
	protected Struct currentMethodReturnType;
	protected boolean isMethodReturnedCorrectly = false;
	
	public boolean isMainMethod(String methodName) {
		return methodName.equals(MAIN_METHOD);
	}

	public void setCurrentMethodReturnType(Struct methodReturnType) {
		currentMethodReturnType = methodReturnType;
		isMethodReturnedCorrectly = methodReturnType == Tab.noType;
	}
}
