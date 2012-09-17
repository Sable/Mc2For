package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;

public class ASTHandleCaseTIRIfStmt{
	
	static boolean Debug = false;
	
	public ASTHandleCaseTIRIfStmt(){
		
	}
	/**
	 * IfStmt: Statement ::= <Condition> IfBlock: StatementSection [ElseBlock: StatementSection];
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRIfStmt node){
		if (Debug) System.out.println("in if statement.");
		if (Debug) System.out.println(node.getConditionVarName().getID());
		
		IfStmt stmt = new IfStmt();
		stmt.setCondition(node.getConditionVarName().getID());
		
		fcg.isIfWhileForBlock = true;
		StatementSection ifStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = ifStmtSec;
		fcg.interateStatements(node.getIfStameents());
		stmt.setIfBlock(ifStmtSec);
		fcg.isIfWhileForBlock = false;

		fcg.isIfWhileForBlock = true;
		StatementSection elseStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = elseStmtSec;
		fcg.interateStatements(node.getElseStatements());
		stmt.setElseBlock(elseStmtSec);
		fcg.isIfWhileForBlock = false;
		return stmt;
	}
	
}
