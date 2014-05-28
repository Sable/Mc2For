package natlab.backends.Fortran.codegen_readable.astCaseHandler;

import natlab.backends.Fortran.codegen_readable.FortranCodeASTGenerator;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.FWhileStmt;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.Statement;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.StatementSection;
import ast.WhileStmt;

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
