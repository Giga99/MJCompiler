package rs.ac.bg.etf.pp1.tabextended;

import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.visitors.DumpSymbolTableVisitor;

public class SymbolTableVisitorExtended extends DumpSymbolTableVisitor {

	@Override
	public void visitStructNode(Struct structToVisit) {
		switch (structToVisit.getKind()) {
		case Struct.Bool:
			output.append("bool");
			break;

		default:
			super.visitStructNode(structToVisit);
			break;
		}
	}
}
