package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;

public class HandleCaseTIRIfStmt{
	
	static boolean Debug = false;
	
	public HandleCaseTIRIfStmt(){
		
	}
	/**
	 * IfStmt: Statement ::= <Condition> IfBlock: StatementSection [ElseBlock: StatementSection];
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRIfStmt node){
		if (Debug) System.out.println("in if statement.");
		if (Debug) System.out.println(node.getConditionVarName().getID());
		
		IfStmt stmt = new IfStmt();
		String indent = new String();
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		stmt.setCondition(node.getConditionVarName().getID());
		/**
		 * backup this pointer! and make fcg.stmtSecForIFWhileForBlock point back after iterate for block.
		 */
		StatementSection backup = fcg.stmtSecForIfWhileForBlock;
		
		fcg.inIfWhileForBlock = true;
		StatementSection ifStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = ifStmtSec;
		fcg.indentNum++;
		fcg.iterateStatements(node.getIfStameents());
		stmt.setIfBlock(ifStmtSec);
		fcg.indentNum--;
		fcg.inIfWhileForBlock = false;
		
		fcg.stmtSecForIfWhileForBlock = backup;
		
		/**
		 * backup this pointer! and make fcg.stmtSecForIFWhileForBlock point back after iterate for block.
		 */
		StatementSection backup2 = fcg.stmtSecForIfWhileForBlock;
		fcg.inIfWhileForBlock = true;
		StatementSection elseStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = elseStmtSec;
		fcg.indentNum++;
		fcg.iterateStatements(node.getElseStatements());
		stmt.setElseBlock(elseStmtSec);
		fcg.indentNum--;
		fcg.inIfWhileForBlock = false;
		
		fcg.stmtSecForIfWhileForBlock = backup2;
		
		return stmt;
	}
	
}
