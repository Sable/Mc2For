package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;

public class HandleCaseTIRCommentStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRCommentStmt(){
		
	}
	
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRCommentStmt node){
		if (Debug) System.out.println("in a comment statement");
		/**
		 * for Natlab, it consider blank line is also a comment statement.
		 */
		CommentStmt stmt = new CommentStmt();
		String indent = new String();
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		if(node.getNodeString().contains("%")){
			stmt.setComment(node.getNodeString().replace("%", ""));			
		}
		return stmt;
	}
}
