package natlab.backends.Fortran.codegen_readable.astCaseHandler;

import ast.ForStmt;

import natlab.backends.Fortran.codegen_readable.*;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.*;

public class HandleCaseForStmt {
	static boolean Debug = false;
	
	/**
	 * ForStmt: Statement 
	 * ::= <LoopVar> <LowBoundary> <UpperBoundary> [Inc] ForBlock: StatementSection;
	 */
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			ForStmt node) 
	{
		if (Debug) System.out.println("in for statement.");
		FForStmt stmt = new FForStmt();
		String indent = "";
		for (int i = 0; i < fcg.indentNum; i++) {
			indent = indent + fcg.standardIndent;
		}
		stmt.setIndent(indent);
		node.getChild(0).getChild(0).analyze(fcg);
		fcg.sb.append(" = ");
		node.getChild(0).getChild(1).analyze(fcg);
		stmt.setFForCondition(fcg.sb.toString());
		fcg.sb.setLength(0);
		/*
		 * backup this pointer! and make fcg.stmtSecForIFWhileForBlock 
		 * point back after iterate for block.
		 */
		StatementSection backup = fcg.stmtSecForIfWhileForBlock;
		
		fcg.ifWhileForBlockNest++;
		StatementSection forStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = forStmtSec;
		fcg.indentNum++;
		node.getStmtList().analyze(fcg);
		stmt.setFForBlock(forStmtSec);
		fcg.indentNum--;
		fcg.ifWhileForBlockNest--;
		
		fcg.stmtSecForIfWhileForBlock = backup;
		
		return stmt;
	}
}
