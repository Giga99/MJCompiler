package rs.ac.bg.etf.pp1.helpers.codegeneration;


import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class ExprCodeGenerationManager {

	public boolean isAddopAddition(Addop addop) {
		return addop instanceof Plus;
	}
	
	public boolean isMulopMultiplication(Mulop mulop) {
		return mulop instanceof Mul;
	}
	
	public boolean isMulopDividion(Mulop mulop) {
		return mulop instanceof Div;
	}
	
	public boolean isCharArray(Struct factorArrayStruct) {
		return factorArrayStruct.getElemType() == Tab.charType;
	}
	
	public boolean isCharMatrix(FactorMatrix factorMatrix) {
		return factorMatrix.struct.getElemType() == Tab.charType;
	}
	
	public boolean isCharVariable(Struct variableStruct) {
		return variableStruct == Tab.charType;
	}
}
