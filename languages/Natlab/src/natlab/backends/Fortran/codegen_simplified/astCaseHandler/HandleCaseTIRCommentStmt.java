package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import natlab.backends.Fortran.codegen_simplified.FortranCodeASTGenerator;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.CommentStmt;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.Statement;
import natlab.tame.tir.TIRCommentStmt;

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
			indent = indent + fcg.standardIndent;
		}
		stmt.setIndent(indent);
		if (node.getNodeString().contains("%")) {
			stmt.setComment(node.getNodeString().replace("%", ""));			
		}
		return stmt;
	}
}
