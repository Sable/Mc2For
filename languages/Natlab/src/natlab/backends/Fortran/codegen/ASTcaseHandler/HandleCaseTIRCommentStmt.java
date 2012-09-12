package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.backends.Fortran.codegen.*;
import natlab.tame.tir.*;

public class HandleCaseTIRCommentStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRCommentStmt(){
		
	}
	
	public FortranCodePrettyPrinter getFortran(FortranCodePrettyPrinter fcg, TIRCommentStmt node){
		if (Debug) System.out.println("in a comment statement");
		/**
		 * for Natlab, it consider blank line is also a comment statement.
		 */
		if(node.getNodeString().contains("%")){
			fcg.buf.append("c     "+node.getNodeString().replace("%", ""));			
		}
		return fcg;
	}
}
