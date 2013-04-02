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
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			TIRAssignLiteralStmt node) {
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
		if (fcg.isSubroutine) {
			/*
			 * if input argument on the LHS of assignment stmt, 
			 * we assume that this input argument maybe modified.
			 */
			if (fcg.inArgs.contains(targetName)) {
				if (Debug) System.out.println("subroutine's input " + targetName 
						+ " has been modified!");
				/*
				 * here we need to detect whether it is the first time this variable put 
				 * in the set, because we only want to back up them once.
				 */
				if (fcg.inputHasChanged.contains(targetName)) {
					//do nothing.
					if (Debug) System.out.println("encounter "+targetName+" again.");
				}
				else {
					if (Debug) System.out.println("first time encounter "+targetName);
					fcg.inputHasChanged.add(targetName);
				}
				var.setName(targetName+"_copy");
			}
			else var.setName(targetName);
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
			rtc.setBlock("allocate("+targetName+"(1, 1));");
			stmt.setRuntimeAllocate(rtc);			
		}
		return stmt;
	}
}