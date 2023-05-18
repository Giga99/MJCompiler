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
	
	private Stack<List<Integer>> destinationsFromAndBlocksToFix = new Stack<List<Integer>>();
	private Stack<List<Integer>> destinationsFromOrBlocksToFix = new Stack<List<Integer>>();
	
	private Stack<List<Integer>> destinationsFromIfBlockToFix = new Stack<List<Integer>>();
	
	private Stack<Integer> beginingDestinationOfWhile = new Stack<Integer>();
	private Stack<List<Integer>> destinationsFromBreakBlockToFix = new Stack<List<Integer>>();

	private Stack<Integer> beginingDestinationOfMap = new Stack<Integer>();
	private Stack<Integer> destinationFromMapBlockToFix = new Stack<Integer>();
	
	public void startIf() {
		destinationsFromAndBlocksToFix.push(new ArrayList<Integer>());
		destinationsFromOrBlocksToFix.push(new ArrayList<Integer>());
		destinationsFromIfBlockToFix.push(new ArrayList<Integer>());
	}
	
	public void finishIf() {
		destinationsFromAndBlocksToFix.pop();
		destinationsFromOrBlocksToFix.pop();
		destinationsFromIfBlockToFix.pop();
	}
	
	public void fixupDestinationsFromAndBlock() {
		for (int destination : destinationsFromAndBlocksToFix.peek()) {
			Code.fixup(destination);
		}
		destinationsFromAndBlocksToFix.peek().clear();
	}
	
	public void addAndBlockDestinationToFix(int destination) {
		destinationsFromAndBlocksToFix.peek().add(destination);
	}
	
	public void fixupDestinationsFromOrBlock() {
		for (int destination : destinationsFromOrBlocksToFix.peek()) {
			Code.fixup(destination);
		}
		destinationsFromOrBlocksToFix.peek().clear();
	}
	
	public void addOrBlockDestinationToFix(int destination) {
		destinationsFromOrBlocksToFix.peek().add(destination);
	}
	
	public void fixupDestinationsFromIfBlock() {
		for (int destination : destinationsFromIfBlockToFix.peek()) {
			Code.fixup(destination);
		}
		destinationsFromIfBlockToFix.peek().clear();
	}
	
	public void addIfBlockDestinationToFix(int destination) {
		destinationsFromIfBlockToFix.peek().add(destination);
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
		destinationsFromAndBlocksToFix.push(new ArrayList<Integer>());
		destinationsFromOrBlocksToFix.push(new ArrayList<Integer>());
		destinationsFromBreakBlockToFix.push(new ArrayList<Integer>());
		beginingDestinationOfWhile.push(destinationOfBeginingOfWhile);
	}
	
	public void finishWhile() {
		destinationsFromAndBlocksToFix.pop();
		destinationsFromOrBlocksToFix.pop();
		destinationsFromBreakBlockToFix.pop();
	}
	
	public void fixupDestinationsFromBreakBlock() {
		for (int destination : destinationsFromBreakBlockToFix.peek()) {
			Code.fixup(destination);
		}
		destinationsFromOrBlocksToFix.peek().clear();
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
	
	public void addDestinationOfBeginingOfMap(int destination) {
		beginingDestinationOfMap.push(destination);
	}
	
	public void jumpToBeginingOfMap() {
		Code.putJump(beginingDestinationOfMap.pop());
	}
	
	public void fixupDestinationFromMapBlock() {
		Code.fixup(destinationFromMapBlockToFix.pop());
	}
	
	public void addMapBlockDestinationToFix(int destination) {
		destinationFromMapBlockToFix.push(destination);
	}
}
