package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.helpers.DeclarationManager;
import rs.ac.bg.etf.pp1.helpers.ExprManager;
import rs.ac.bg.etf.pp1.helpers.MethodManager;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {
	private Logger log = Logger.getLogger(getClass());

	private boolean errorDetected = false;

	private DeclarationManager declarationManager = new DeclarationManager();
	private MethodManager methodManager = new MethodManager();
	private ExprManager exprManager = new ExprManager();

	private Struct currentType = null;

	public void reportError(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" on line ").append(line);
		log.error(msg.toString());
	}

	public void reportInfo(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" on line ").append(line);
		log.info(msg.toString());
	}

	public boolean passed() {
		return !errorDetected;
	}

	public void visit(ProgName progName) {
		progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
		Tab.openScope();
	}

	public void visit(Program program) {
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
		if (!methodManager.isMainMethodPresent()) {
			reportError("Main method doesn't exist", program);
		}
	}

	public void visit(Type type) {
		String typeName = type.getTypeName();
		Obj typeNode = Tab.find(typeName);

		if (typeNode == Tab.noObj) {
			reportError("Type " + typeName + " not found in the Symbol Table", type);
			type.struct = Tab.noType;
		} else {
			if (typeNode.getKind() == Obj.Type) {
				type.struct = typeNode.getType();
			} else {
				reportError("Type " + typeName + " is not a valid type in the Symbol Table", type);
				type.struct = Tab.noType;
			}
		}

		currentType = type.struct;
	}

	/* Rules for the const declaration */

	public void visit(ConstDeclListItem constDeclListItem) {
		String constName = constDeclListItem.getConstName();
		Struct valueType = constDeclListItem.getConstValue().obj.getType();
		if (declarationManager.isSymbolAlreadyDeclaredInCurrentScope(constName)) {
			reportError("Symbol " + constName + " is already declared in this scope", constDeclListItem);
		} else if (declarationManager.typesNotMatching(currentType, valueType)) {
			reportError("Symbol type " + currentType.getKind() + " is not matching with the value type "
					+ valueType.getKind(), constDeclListItem);
		} else {
			Obj obj = Tab.insert(Obj.Con, constName, currentType);
			obj.setAdr(constDeclListItem.getConstValue().obj.getAdr());
			constDeclListItem.getConstValue().obj = obj;
		}
	}

	public void visit(ConstValueNumber constValueNumber) {
		Obj obj = Tab.insert(Obj.Con, "constInt", Tab.intType);
		obj.setAdr(constValueNumber.getValue());
		constValueNumber.obj = obj;
	}

	public void visit(ConstValueChar constValueChar) {
		Obj obj = Tab.insert(Obj.Con, "constChar", Tab.charType);
		obj.setAdr(constValueChar.getValue());
		constValueChar.obj = obj;
	}

	public void visit(ConstValueBool constValueBool) {
		Obj obj = Tab.insert(Obj.Con, "constBool", TabExtended.boolType);
		obj.setAdr(constValueBool.getValue().equals("true") ? 1 : 0);
		constValueBool.obj = obj;
	}

	/* Rules for the variable declaration */

	public void visit(DefaultVar defaultVar) {
		String varName = defaultVar.getVarName();
		if (declarationManager.isSymbolAlreadyDeclaredInCurrentScope(varName)) {
			reportError("Symbol " + varName + " is already declared in this scope", defaultVar);
		} else {
			Tab.insert(Obj.Var, varName, currentType);
		}
	}

	public void visit(ArrayVar arrayVar) {
		String arrayName = arrayVar.getArrayName();
		if (declarationManager.isSymbolAlreadyDeclaredInCurrentScope(arrayName)) {
			reportError("Symbol " + arrayName + " is already declared in this scope", arrayVar);
		} else {
			Tab.insert(Obj.Var, arrayName, declarationManager.getArrayTypeForGivenType(currentType));
		}
	}

	/* Rules for the method declaration */

	public void visit(MethodVoidReturnType methodVoidReturnType) {
		methodManager.setCurrentMethodReturnType(Tab.noType);
	}

	public void visit(MethodAnyReturnType methodAnyReturnType) {
		methodManager.setCurrentMethodReturnType(currentType);
	}

	public void visit(MethodName methodName) {
		methodManager.setCurrentMethod(methodName.getMethodName());
		Tab.openScope();
	}

	public void visit(MethodDecl methodDecl) {
		Tab.chainLocalSymbols(methodManager.getCurrentMethod());
		Tab.closeScope();
		if (methodManager.isMethodCorrect()) {
			methodManager.finishMethod();
		} else {
			reportError("Method " + methodDecl.getMethodName().getMethodName()
					+ " is not declared correctly, either the return is not present or it returns the wrong type or the main is not declared correctly",
					methodDecl);
		}
	}

	public void visit(FormParamDeclarationVar formParamDeclarationVar) {
		String varName = formParamDeclarationVar.getVarName();
		if (declarationManager.isSymbolAlreadyDeclaredInCurrentScope(varName)) {
			reportError("Symbol " + varName + " is already declared in this scope", formParamDeclarationVar);
		} else {
			methodManager.addFormParam(currentType);
			Tab.insert(Obj.Var, varName, currentType);
		}
	}

	public void visit(FormParamDeclarationArray formParamDeclarationArray) {
		String arrayName = formParamDeclarationArray.getArrayName();
		if (declarationManager.isSymbolAlreadyDeclaredInCurrentScope(arrayName)) {
			reportError("Symbol " + arrayName + " is already declared in this scope", formParamDeclarationArray);
		} else {
			methodManager.addFormParam(currentType);
			Tab.insert(Obj.Var, arrayName, declarationManager.getArrayTypeForGivenType(currentType));
		}
	}

	/* Rules for the expressions */
	
	public void visit(ExprPositiveFirstTerm exprPositiveFirstTerm) {
		exprPositiveFirstTerm.struct = exprPositiveFirstTerm.getTermList().struct;
	}
	
	public void visit(ExprNegativeFirstTerm exprNegativeFirstTerm) {
		Struct termType = exprNegativeFirstTerm.getTermList().struct;
		if (exprManager.isTermTypeInt(termType)) {
			exprNegativeFirstTerm.struct = termType;
		} else {
			reportError("Type in expression starting with negative term must be int", exprNegativeFirstTerm);
			exprNegativeFirstTerm.struct = Tab.noType;
		}
	}
	
	public void visit(TermListSingle termListSingle) {
		termListSingle.struct = termListSingle.getTerm().struct;
	}
	
	public void visit(TermListMultiple termListMultiple) {
		Term term = termListMultiple.getTerm();
		TermList termList = termListMultiple.getTermList();
		if (exprManager.areCompatibleTypesInAddopExpr(term, termList)) {
			termListMultiple.struct = term.struct;
		} else {
			reportError("Type in expression with multiple terms must be int", termListMultiple);
			termListMultiple.struct = Tab.noType;
		}
	}
	
	public void visit(TermSingleFactor termSingleFactor) {
		termSingleFactor.struct = termSingleFactor.getFactor().struct;
	}
	
	public void visit(TermMultipleFactor termMultipleFactor) {
		Term term = termMultipleFactor.getTerm();
		Factor factor = termMultipleFactor.getFactor();
		if (exprManager.areCompatibleTypesInMulopExpr(term, factor)) {
			termMultipleFactor.struct = term.struct;
		} else {
			reportError("Type in expression multiple factors must be int", termMultipleFactor);
			termMultipleFactor.struct = Tab.noType;
		}
	}
	
	public void visit(FactorNumber factorNumber) {
		factorNumber.struct = Tab.intType;
	}
	
	public void visit(FactorChar factorChar) {
		factorChar.struct = Tab.charType;
	}
	
	public void visit(FactorBool factorBool) {
		factorBool.struct = TabExtended.boolType;
	}
}
