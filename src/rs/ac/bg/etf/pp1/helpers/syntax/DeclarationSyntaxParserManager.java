package rs.ac.bg.etf.pp1.helpers.syntax;

import rs.ac.bg.etf.pp1.ast.ProgramVarDecList;
import rs.ac.bg.etf.pp1.ast.VarDeclaration;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class DeclarationSyntaxParserManager {
	
	public boolean isSymbolAlreadyDeclaredInCurrentScope(String symbolName) {
		return Tab.currentScope.findSymbol(symbolName) != null;
	}
	
	public boolean typesNotMatching(Struct symbolType, Struct valueType) {
		return !valueType.assignableTo(symbolType);
	}
	
	public Obj getObjFromTableBySymbolName(String symbolName) {
		return Tab.currentScope.findSymbol(symbolName);
	}
	
	public boolean isGlobalVar(VarDeclaration varDeclaration) {
		return varDeclaration.getParent().getParent().getParent() instanceof ProgramVarDecList;
	}
}
