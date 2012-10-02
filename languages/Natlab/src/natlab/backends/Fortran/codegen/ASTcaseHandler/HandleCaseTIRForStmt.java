package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;

public class HandleCaseTIRForStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRForStmt(){
		
	}
	/**
	 * ForStmt: Statement ::= <LoopVar> <LowBoundary> [Inc] <UpperBoundary> ForBlock: StatementSection;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRForStmt node){
		if (Debug) System.out.println("in for statement.");
		if (Debug) System.out.println(node.getLoopVarName().getVarName());
		
		ForStmt stmt = new ForStmt();
		
		stmt.setLoopVar(node.getLoopVarName().getVarName());
		fcg.forStmtParameter.add(node.getLoopVarName().getVarName());
		
		stmt.setLowBoundary(node.getLowerName().getVarName());
		fcg.forStmtParameter.add(node.getLowerName().getVarName());
		
		stmt.setUpperBoundary(node.getUpperName().getVarName());
		fcg.forStmtParameter.add(node.getUpperName().getVarName());
		
		Inc inc = new Inc();
		if(node.getIncName()!=null){
			inc.setName(node.getIncName().getVarName());
			fcg.forStmtParameter.add(node.getIncName().getVarName());
			stmt.setInc(inc);
		}
		
		fcg.isIfWhileForBlock = true;
		StatementSection forStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = forStmtSec;
		fcg.iterateStatements(node.getStatements());
		stmt.setForBlock(forStmtSec);
		fcg.isIfWhileForBlock = false;
		
		return stmt;
	}
}
