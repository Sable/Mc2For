package natlab.backends.Fortran.codegen_readable.astCaseHandler;

import ast.WhileStmt;

import natlab.backends.Fortran.codegen_readable.*;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.*;

public class HandleCaseWhileStmt {
	static boolean Debug = false;
	
	/**
	 * WhileStmt: Statement ::= <Condition> WhileBlock: StatementSection;
	 */
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			WhileStmt node) 
	{
		if (Debug) System.out.println("in while statement.");		
		FWhileStmt stmt = new FWhileStmt();
		stmt.setIndent(fcg.getMoreIndent(0));
		node.getChild(0).analyze(fcg);
		stmt.setFWhileCondition(fcg.sb.toString());
		fcg.sb.setLength(0);
		/*
		 * backup this pointer! and make fcg.stmtSecForIFWhileForBlock 
		 * point back after iterate for block.
		 */
		StatementSection backup = fcg.stmtSecForIfWhileForBlock;
		
		fcg.ifWhileForBlockNest++;
		StatementSection whileStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = whileStmtSec;
		fcg.indentNum++;
		node.getChild(1).analyze(fcg);
		stmt.setFWhileBlock(whileStmtSec);
		fcg.indentNum--;
		fcg.ifWhileForBlockNest--;
		
		fcg.stmtSecForIfWhileForBlock = backup;
		
		return stmt;
	}
}
