package rs.ac.bg.etf.pp1.helpers.codegeneration;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class ControlFlowCodeGenerationManager {
	
	private Stack<List<Integer>> destinationFromAndBlocksToFix = new Stack<List<Integer>>();
	private Stack<List<Integer>> destinationFromOrBlocksToFix = new Stack<List<Integer>>();
	
	private Stack<List<Integer>> destinationFromIfBlockToFix = new Stack<List<Integer>>();
	
	private Stack<Integer> beginingDestinationOfWhile = new Stack<Integer>();
	private Stack<List<Integer>> destinationsFromBreakBlockToFix = new Stack<List<Integer>>();
	
	public void startIf() {
		destinationFromAndBlocksToFix.push(new ArrayList<Integer>());
		destinationFromOrBlocksToFix.push(new ArrayList<Integer>());
		destinationFromIfBlockToFix.push(new ArrayList<Integer>());
	}
	
	public void finishIf() {
		destinationFromAndBlocksToFix.pop();
		destinationFromOrBlocksToFix.pop();
		destinationFromIfBlockToFix.pop();
	}
	
	public void fixupDestinationsFromAndBlock() {
		for (int destination : destinationFromAndBlocksToFix.peek()) {
			Code.fixup(destination);
		}
		destinationFromAndBlocksToFix.peek().clear();
	}
	
	public void addAndBlockDestinationToFix(int destination) {
		destinationFromAndBlocksToFix.peek().add(destination);
	}
	
	public void fixupDestinationsFromOrBlock() {
		for (int destination : destinationFromOrBlocksToFix.peek()) {
			Code.fixup(destination);
		}
		destinationFromOrBlocksToFix.peek().clear();
	}
	
	public void addOrBlockDestinationToFix(int destination) {
		destinationFromOrBlocksToFix.peek().add(destination);
	}
	
	public void fixupDestinationsFromIfBlock() {
		for (int destination : destinationFromIfBlockToFix.peek()) {
			Code.fixup(destination);
		}
		destinationFromIfBlockToFix.peek().clear();
	}
	
	public void addIfBlockDestinationToFix(int destination) {
		destinationFromIfBlockToFix.peek().add(destination);
	}
	
	public int getOperationCodeForRelop(Relop relop) {
		int operationCode = 0;
		
		if(relop instanceof Equals) {
			operationCode = Code.eq; 			
		} else if(relop instanceof NotEquals) {
			operationCode = Code.ne; 			
		} else if(relop instanceof Less) {
			operationCode = Code.lt; 			
		} else if(relop instanceof LessEquals) {
			operationCode = Code.le; 			
		} else if(relop instanceof Greater) {
			operationCode = Code.gt; 			
		} else if(relop instanceof GreaterEquals) {
			operationCode = Code.ge; 			
		}
		
		return operationCode;
	}
	
	public boolean isInsideIfElse(StatementIfEnd statementIfEnd) {
		return statementIfEnd.getParent() instanceof StatementIfElse;
	}
	
	public void startWhile(int destinationOfBeginingOfWhile) {
		destinationFromAndBlocksToFix.push(new ArrayList<Integer>());
		destinationFromOrBlocksToFix.push(new ArrayList<Integer>());
		destinationsFromBreakBlockToFix.push(new ArrayList<Integer>());
		beginingDestinationOfWhile.push(destinationOfBeginingOfWhile);
	}
	
	public void finishWhile() {
		destinationFromAndBlocksToFix.pop();
		destinationFromOrBlocksToFix.pop();
		destinationsFromBreakBlockToFix.pop();
	}
	
	public void fixupDestinationsFromBreakBlock() {
		for (int destination : destinationsFromBreakBlockToFix.peek()) {
			Code.fixup(destination);
		}
		destinationFromOrBlocksToFix.peek().clear();
	}
	
	public void addBreakBlockDestinationToFix(int destination) {
		destinationsFromBreakBlockToFix.peek().add(destination);
	}
	
	public void jumpToBeginingOfWhile() {
		Code.putJump(beginingDestinationOfWhile.pop());
	}
	
	public void continueToBeginingOfWhile() {
		Code.putJump(beginingDestinationOfWhile.peek());
	}
}
