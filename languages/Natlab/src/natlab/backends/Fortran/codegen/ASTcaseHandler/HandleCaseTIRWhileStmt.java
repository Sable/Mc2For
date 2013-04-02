package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.tame.tir.*;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class HandleCaseTIRWhileStmt {
	static boolean Debug = false;
	
	/**
	 * WhileStmt: Statement ::= <Condition> WhileBlock: StatementSection;
	 */
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			TIRWhileStmt node) {
		if (Debug) System.out.println("in while statement.");		
		WhileStmt stmt = new WhileStmt();
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		stmt.setCondition(node.getCondition().getVarName());
		/*
		 * backup this pointer! and make fcg.stmtSecForIFWhileForBlock 
		 * point back after iterate for block.
		 */
		StatementSection backup = fcg.stmtSecForIfWhileForBlock;
		
		fcg.ifWhileForBlockNest++;
		StatementSection whileStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = whileStmtSec;
		fcg.indentNum++;
		fcg.iterateStatements(node.getStatements());
		stmt.setWhileBlock(whileStmtSec);
		fcg.indentNum--;
		fcg.ifWhileForBlockNest--;
		
		fcg.stmtSecForIfWhileForBlock = backup;
		
		return stmt;
	}
}
