package rs.ac.bg.etf.pp1.helpers.codegeneration;


import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class DesignatorStatementCodeGenerationManager {

	public boolean isDesignatorElementInArrayOrMatrix(Designator designator) {
		return designator.obj.getKind() == Obj.Elem;
	}
}
