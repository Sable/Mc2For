package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;

public class ASTHandleCaseTIRWhileStmt {

	static boolean Debug = false;
	
	public ASTHandleCaseTIRWhileStmt(){
		
	}
	/**
	 * WhileStmt: Statement ::= <Condition> WhileBlock: StatementSection;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRWhileStmt node){
		if (Debug) System.out.println("in while statement.");
		if (Debug) System.out.println(node.getCondition().getVarName());
		
		WhileStmt stmt = new WhileStmt();
		stmt.setCondition(node.getCondition().getVarName());
		
		fcg.isIfWhileForBlock = true;
		StatementSection whileStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = whileStmtSec;
		fcg.interateStatements(node.getStatements());
		stmt.setWhileBlock(whileStmtSec);
		fcg.isIfWhileForBlock = false;
		return stmt;
	}
}
