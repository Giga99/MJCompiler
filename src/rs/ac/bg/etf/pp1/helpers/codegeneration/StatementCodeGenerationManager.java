package rs.ac.bg.etf.pp1.helpers.codegeneration;


import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class StatementCodeGenerationManager {

	public boolean useBRead(Obj designatorObj) {
		return designatorObj.getType() == Tab.charType;
	}

	public boolean useBPrint(Struct exprType) {
		return exprType == Tab.charType;
	}
	
	public boolean isPrintWidthDefined(StatementPrint statementPrint) {
		return statementPrint.getPrintNumOptional() instanceof PrintNumConst;
	}
	
	public int getPrintWidth(StatementPrint statementPrint) {
		return ((PrintNumConst) statementPrint.getPrintNumOptional()).getN1();
	}
}
