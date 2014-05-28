package natlab.backends.Fortran.codegen_readable.astCaseHandler;

import natlab.backends.Fortran.codegen_readable.FortranCodeASTGenerator;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.FIfStmt;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.Statement;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.StatementSection;
import ast.IfStmt;

public class HandleCaseIfStmt {
	static boolean Debug = false;
	
	/**
	 * IfStmt: Statement 
	 * ::= <Condition> IfBlock: StatementSection [ElseBlock: StatementSection];
	 */
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			IfStmt node) 
	{
		if (Debug) System.out.println("in if statement.");
		FIfStmt stmt = new FIfStmt();
		stmt.setIndent(fcg.getMoreIndent(0));
		node.getIfBlock(0).getCondition().analyze(fcg);
		stmt.setFIfCondition(fcg.sb.toString());
		fcg.sb.setLength(0);
		/*
		 * backup this pointer! and make fcg.stmtSecForIFWhileForBlock 
		 * point back after iterate for block.
		 */
		StatementSection backup = fcg.stmtSecForIfWhileForBlock;
		
		fcg.ifWhileForBlockNest++;
		StatementSection ifStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = ifStmtSec;
		fcg.indentNum++;
		node.getIfBlock(0).getStmtList().analyze(fcg);
		stmt.setFIfBlock(ifStmtSec);
		fcg.indentNum--;
		fcg.ifWhileForBlockNest--;
		
		fcg.stmtSecForIfWhileForBlock = backup;
		
		/*
		 * backup this pointer! and make fcg.stmtSecForIFWhileForBlock 
		 * point back after iterate for block.
		 */
		StatementSection backup2 = fcg.stmtSecForIfWhileForBlock;
		
		fcg.ifWhileForBlockNest++;
		StatementSection elseStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = elseStmtSec;
		fcg.indentNum++;
		node.getElseBlock().getStmtList().analyze(fcg);
		stmt.setFElseBlock(elseStmtSec);
		fcg.indentNum--;
		fcg.ifWhileForBlockNest--;
		
		fcg.stmtSecForIfWhileForBlock = backup2;
				
		return stmt;
	}
}
