package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import natlab.tame.tir.*;
import natlab.backends.Fortran.codegen_simplified.*;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.*;

public class HandleCaseTIRForStmt {
	static boolean Debug = false;
	
	/**
	 * ForStmt: Statement ::= 
	 * <LoopVar> <LowBoundary> <UpperBoundary> [Inc] ForBlock: StatementSection;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRForStmt node){
		if (Debug) System.out.println("in for statement.");
		ForStmt stmt = new ForStmt();
		String indent = "";
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.standardIndent;
		}
		stmt.setIndent(indent);
		String loopVar = node.getLoopVarName().getVarName();
		stmt.setLoopVar(loopVar);
		/*
		 * lower bound constant replacement
		 */
		String lowerBoundName = node.getLowerName().getVarName();
		if (fcg.getMatrixValue(lowerBoundName).hasConstant() 
				&& fcg.tempVarsBeforeF.contains(lowerBoundName)) {
			int intLower = ((Double) fcg.getMatrixValue(lowerBoundName)
					.getConstant().getValue()).intValue();
			stmt.setLowBoundary(Integer.toString(intLower));
		}
		else stmt.setLowBoundary("INT("+lowerBoundName+")");
		/*
		 * upper bound constant replacement
		 */
		String upperBoundName = node.getUpperName().getVarName();
		if (fcg.getMatrixValue(upperBoundName).hasConstant() 
				&& fcg.tempVarsBeforeF.contains(upperBoundName)) {
			int intUpper = ((Double) fcg.getMatrixValue(upperBoundName)
					.getConstant().getValue()).intValue();
			stmt.setUpperBoundary(Integer.toString(intUpper));
		}
		else stmt.setUpperBoundary("INT("+upperBoundName+")");
		/*
		 * if has increment variable
		 */
		if (node.hasIncr()) {
			Inc inc = new Inc();
			/*
			 * increment variable constant replacement
			 */
			String incName = node.getIncName().getVarName();
			if (fcg.getMatrixValue(incName).hasConstant() 
					&& fcg.tempVarsBeforeF.contains(incName)) {
				int intInc = ((Double) fcg.getMatrixValue(incName)
						.getConstant().getValue()).intValue();
				inc.setName(Integer.toString(intInc));
			}
			else inc.setName("INT("+incName+")");
			stmt.setInc(inc);
		}
		/*
		 * backup this pointer! and make fcg.stmtSecForIFWhileForBlock 
		 * point back after iterate for block.
		 */
		StatementSection backup = fcg.stmtSecForIfWhileForBlock;
		
		fcg.ifWhileForBlockNest++;
		StatementSection forStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = forStmtSec;
		fcg.indentNum++;
		fcg.iterateStatements(node.getStatements());
		stmt.setForBlock(forStmtSec);
		fcg.indentNum--;
		fcg.ifWhileForBlockNest--;
		
		fcg.stmtSecForIfWhileForBlock = backup;
		
		return stmt;
	}
}
