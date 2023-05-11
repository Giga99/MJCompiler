package rs.ac.bg.etf.pp1.helpers.syntax;

import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class DeclarationManager {
	
	public boolean isSymbolAlreadyDeclaredInCurrentScope(String symbolName) {
		return Tab.currentScope.findSymbol(symbolName) != null;
	}
	
	public boolean typesNotMatching(Struct symbolType, Struct valueType) {
		return !valueType.assignableTo(symbolType);
	}
	
	public Obj getObjFromTableBySymbolName(String symbolName) {
		return Tab.currentScope.findSymbol(symbolName);
	}
}
