package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;

public class HandleCaseTIRWhileStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRWhileStmt(){
		
	}
	/**
	 * WhileStmt: Statement ::= <Condition> WhileBlock: StatementSection;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRWhileStmt node){
		if (Debug) System.out.println("in while statement.");
		if (Debug) System.out.println(node.getCondition().getVarName());
		
		WhileStmt stmt = new WhileStmt();
		String indent = new String();
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		stmt.setCondition(node.getCondition().getVarName());
		
		fcg.isIfWhileForBlock = true;
		StatementSection whileStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = whileStmtSec;
		fcg.indentNum = fcg.indentNum + 1;
		fcg.iterateStatements(node.getStatements());
		stmt.setWhileBlock(whileStmtSec);
		fcg.indentNum = fcg.indentNum - 1;
		fcg.isIfWhileForBlock = false;
		return stmt;
	}
}
