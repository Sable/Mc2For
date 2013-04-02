package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.tame.tir.*;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class HandleCaseTIRCommentStmt {
	static boolean Debug = false;
	
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			TIRCommentStmt node) {
		if (Debug) System.out.println("in a comment statement");
		/*
		 * in Natlab, it regard blank line also as a comment statement.
		 */
		CommentStmt stmt = new CommentStmt();
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		if (node.getNodeString().contains("%")) {
			stmt.setComment(node.getNodeString().replace("%", ""));			
		}
		return stmt;
	}
}
