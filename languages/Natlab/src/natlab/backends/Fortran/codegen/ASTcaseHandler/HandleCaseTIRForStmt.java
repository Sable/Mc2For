package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.backends.Fortran.codegen.*;
import natlab.tame.tir.*;

public class HandleCaseTIRForStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRForStmt(){
		
	}
	
	public FortranCodePrettyPrinter getFortran(FortranCodePrettyPrinter fcg, TIRForStmt node){
		if (Debug) System.out.println("in for statement.");
		if (Debug) System.out.println(node.getLoopVarName().getVarName());
		fcg.buf.append("do "+node.getLoopVarName().getVarName()+" = "+node.getLowerName().getVarName()+" , "+node.getUpperName().getVarName()+"\n");
		fcg.indentFW = true;
		fcg.printStatements(node.getStatements());
		fcg.indentFW = false;
		fcg.buf.append("enddo");
		fcg.forStmtParameter.add(node.getLoopVarName().getVarName());
		fcg.forStmtParameter.add(node.getLowerName().getVarName());
		fcg.forStmtParameter.add(node.getUpperName().getVarName());
		return fcg;
	}
}
