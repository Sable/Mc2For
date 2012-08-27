package natlab.backends.Fortran.codegen.caseHandler;

import natlab.backends.Fortran.codegen.*;
import natlab.tame.tir.*;

public class HandleCaseTIRIfStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRIfStmt(){
		
	}
	
	public FortranCodeGenerator getFortran(FortranCodeGenerator fcg, TIRIfStmt node){
		if (Debug) System.out.println("in if statement.");
		if (Debug) System.out.println(node.getConditionVarName().getID());
		fcg.buf.append("      if ("+node.getConditionVarName().getID()+") then\n");
		fcg.indentIf = true;
		fcg.printStatements(node.getIfStameents());
		fcg.indentIf = false;
		fcg.buf.append("      else\n");
		fcg.indentIf = true;
		fcg.printStatements(node.getElseStatements());
		fcg.indentIf = false;
		fcg.buf.append("      endif");
		return fcg;
	}
}
