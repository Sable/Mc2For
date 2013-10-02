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
	public FortranCodeASTGenerator getFortran(
			FortranCodeASTGenerator fcg, 
			TIRFunction node) 
	{
		fcg.functionName = node.getName();
		for (Name param : node.getInputParams()) {
			fcg.inArgs.add(param.getVarName());
		}
		for (Name result : node.getOutputParams()) {
			fcg.outRes.add(result.getVarName());
		}
		/*
		 *  since all of our input matlab files are matlab functions, 
		 *  we use that entry point file name to determine which function 
		 *  should be transformed as the main program in fortran.
		 */
		if (fcg.entryPointFile.equals(node.getName())) {
			GenerateMainEntryPoint mainEntryPoint = new GenerateMainEntryPoint();
			mainEntryPoint.newMain(fcg, node);
		}
		/* 
		 * transform matlab functions, which are not entry point functions 
		 * but has only one return value, to function files in fortran.
		 */
		else if (fcg.outRes.size() == 1) {
			GenerateFunction function = new GenerateFunction();
			function.newFunction(fcg, node);			
		}
		/*
		 * transform matlab functions, which are not entry point functions 
		 * but has 0 or more than one return values, to subroutines in fortran.
		 */
		else {
			GenerateSubroutine subroutine = new GenerateSubroutine();
			subroutine.newSubroutine(fcg, node);
		}
		return fcg;
	}
}