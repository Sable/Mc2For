package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.shape.*;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class HandleCaseTIRAssignLiteralStmt {
	static boolean Debug = false;
	
	/**
	 * AssignLiteralStmt: Statement ::= <RuntimeAllocate> Variable <Literal>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRAssignLiteralStmt node) {
		if (Debug) System.out.println("in an assignLiteral statement");
		AssignLiteralStmt stmt = new AssignLiteralStmt();
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		Variable var = new Variable();
		String targetName = node.getTargetName().getVarName();
		/*
		 * TODO need more consideration, what if the input argument is an array, 
		 * but inside subroutine, it has been assigned a constant, this problem 
		 * also relates to different shape var merging problem.
		 */
		
		/*
		 * if input argument on the LHS of assignment stmt, 
		 * we assume that this input argument maybe modified.
		 */
		if (fcg.isInSubroutine && fcg.inArgs.contains(targetName)) {
			if (Debug) System.out.println("subroutine's input " + targetName 
					+ " has been modified!");
			fcg.inputHasChanged.add(targetName);
			var.setName(targetName+"_copy");
		}
		else var.setName(targetName);
		stmt.setVariable(var);
		stmt.setLiteral(node.getRHS().getNodeString());
		/*
		 * literal assignment target variable should be a constant, if it's not a constant, 
		 * we need allocate it as a 1 by 1 array.
		 */
		Shape targetVar= fcg.getMatrixValue(targetName).getShape();
		if (!targetVar.isConstant()) {
			RuntimeAllocate rtc = new RuntimeAllocate();
			rtc.setBlock("ALLOCATE("+targetName+"(1, 1));");
			stmt.setRuntimeAllocate(rtc);			
		}
		return stmt;
	}
}