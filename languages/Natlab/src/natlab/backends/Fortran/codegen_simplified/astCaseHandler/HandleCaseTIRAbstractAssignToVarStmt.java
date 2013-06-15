package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.constant.Constant;
import natlab.backends.Fortran.codegen_simplified.*;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.*;

public class HandleCaseTIRAbstractAssignToVarStmt {
	static boolean Debug = false;
	static boolean lhsShapeIsknown = true;
	static boolean rhsShapeIsKnown = true;
	
	/**
	 * AbstractAssignToVarStmt: Statement ::= <RuntimeAllocate> <TargetVariable> <SourceVariable>;
	 * for each statement, currently, we need to insert two check:
	 * 1. for rhs, constant folding check;
	 * 2. for lhs, do we need to inline allocate code check.
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRAbstractAssignToVarStmt node) {
		if (Debug) System.out.println("in an abstractAssignToVar statement");
		AbstractAssignToVarStmt stmt = new AbstractAssignToVarStmt();
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		String targetName = node.getTargetName().getID();
		/*
		 * if an input argument of the function is on the LHS of an assignment stmt, 
		 * we assume that this input argument maybe modified.
		 */
		if (fcg.isInSubroutine && fcg.inArgs.contains(targetName)) {
			if (Debug) System.out.println("subroutine's input "+targetName
					+" has been modified!");
			fcg.inputHasChanged.add(targetName);
			stmt.setTargetVariable(targetName+"_copy");
		}
		else stmt.setTargetVariable(targetName);
		/*
		 * for rhs, insert constant folding check.
		 */
		String rhsNodeString = node.getRHS().getNodeString();
		if (!fcg.isCell(targetName) && fcg.hasSingleton(targetName) 
				&& fcg.getMatrixValue(rhsNodeString).hasConstant() 
				&& (!fcg.inArgs.contains(rhsNodeString)) 
				&& fcg.tamerTmpVar.contains(rhsNodeString)) {
			if (Debug) System.out.println(targetName+" is a constant");
			Constant c = fcg.getMatrixValue(rhsNodeString).getConstant();
			stmt.setSourceVariable(c.toString());
		}
		else {
			if (fcg.inputHasChanged.contains(rhsNodeString)) 
				stmt.setSourceVariable(rhsNodeString+"_copy");
			else stmt.setSourceVariable(rhsNodeString);
		}
		if (fcg.isCell(rhsNodeString) || !fcg.hasSingleton(rhsNodeString)) {
			fcg.forCellArr.put(targetName, fcg.forCellArr.get(rhsNodeString));
		}
		/*
		 * TODO for lhs, insert runtime shape allocate check?
		 */
		return stmt;
	}
}
