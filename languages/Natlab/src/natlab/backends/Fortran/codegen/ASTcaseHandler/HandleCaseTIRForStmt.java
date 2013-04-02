package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.tame.tir.*;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class HandleCaseTIRForStmt {
	static boolean Debug = false;
	
	/**
	 * ForStmt: Statement 
	 * ::= <LoopVar> <LowBoundary> [Inc] <UpperBoundary> ForBlock: StatementSection;
	 */
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			TIRForStmt node){
		if (Debug) System.out.println("in for statement.");
		ForStmt stmt = new ForStmt();
		String indent = "";
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		String loopVar = node.getLoopVarName().getVarName();
		stmt.setLoopVar(loopVar);
		fcg.forStmtParameter.add(loopVar);
		/*
		 * lower bound constant replacement
		 */
		String lowerBoundName = node.getLowerName().getVarName();
		if (fcg.getMatrixValue(lowerBoundName).hasConstant()) {
			int intLower = ((Double) fcg.getMatrixValue(lowerBoundName).getConstant().getValue())
					.intValue();
			stmt.setLowBoundary(Integer.toString(intLower));
		}
		else stmt.setLowBoundary(lowerBoundName);
		fcg.forStmtParameter.add(lowerBoundName);
		/*
		 * upper bound constant replacement
		 */
		String upperBoundName = node.getUpperName().getVarName();
		if (fcg.getMatrixValue(upperBoundName).hasConstant()) {
			int intUpper = ((Double) fcg.getMatrixValue(upperBoundName).getConstant().getValue())
					.intValue();
			stmt.setUpperBoundary(Integer.toString(intUpper));
		}
		else stmt.setUpperBoundary(upperBoundName);
		fcg.forStmtParameter.add(upperBoundName);
		/*
		 * if has increment variable
		 */
		if (node.hasIncr()) {
			Inc inc = new Inc();
			/*
			 * increment variable constant replacement
			 */
			String incName = node.getIncName().getVarName();
			if (fcg.getMatrixValue(incName).hasConstant()) {
				int intInc = ((Double) fcg.getMatrixValue(incName).getConstant().getValue())
						.intValue();
				inc.setName(Integer.toString(intInc));
			}
			else inc.setName(incName);
			fcg.forStmtParameter.add(incName);
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
