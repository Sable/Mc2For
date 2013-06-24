package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import ast.Name;
import natlab.tame.tir.*;
import natlab.backends.Fortran.codegen_simplified.*;

public class HandleCaseTIRFunction {
	static boolean Debug =false;
	
	/**
	 * Functions in MATLAB are mapped to programs and subroutines in Fortran, 
	 * SubProgram ::= ProgramTitle DeclarationSection StatementSection;
	 */ 
	public FortranCodeASTGenerator getFortran(FortranCodeASTGenerator fcg, TIRFunction node) {
		fcg.functionName = node.getName();
		for (Name param : node.getInputParams()) {
			fcg.inArgs.add(param.getVarName());
		}
		for (Name result : node.getOutputParams()) {
			fcg.outRes.add(result.getVarName());
		}
		/*
		 * deal with main entry program, actually, sometimes, a subroutine can 
		 * also be with 0 output...TODO think of a better way to distinguish 
		 * whether it is a main entry point or a 0-output subroutine. 
		 */
		if (fcg.outRes.size()==0) {
			CaseNewMainEntryPoint mainEntryPoint = new CaseNewMainEntryPoint();
			mainEntryPoint.newMain(fcg, node);
		}
		/*
		 * deal with subroutines. All the calling functions in MATLAB will be 
		 * mapped to subroutines in generated Fortran, which will be inlined 
		 * inside the main program by using the Fortran key word, "CONTAINS".
		 */
		else {
			CaseNewSubroutine subroutine = new CaseNewSubroutine();
			subroutine.newSubroutine(fcg, node);
		}
		return fcg;
	}
}
