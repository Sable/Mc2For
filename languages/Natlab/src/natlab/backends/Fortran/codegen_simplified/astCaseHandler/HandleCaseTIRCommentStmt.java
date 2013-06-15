package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import natlab.tame.tir.*;
import natlab.backends.Fortran.codegen_simplified.*;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.*;

public class HandleCaseTIRCommentStmt {
	static boolean Debug = false;
	
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRCommentStmt node) {
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
