package rs.ac.bg.etf.pp1.helpers;

import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class DeclarationManager {
	
	public boolean isSymbolAlreadyDeclaredInCurrentScope(String symbolName) {
		return Tab.currentScope.findSymbol(symbolName) != null;
	}
	
	public boolean typesNotMatching(Struct symbolType, Struct valueType) {
		return !valueType.assignableTo(symbolType);
	}
	
	public Struct getArrayTypeForGivenType(Struct type) {
		return new Struct(Struct.Array, type);
	}
}
