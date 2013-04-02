package natlab.backends.Fortran.codegen.ASTcaseHandler;

import ast.Name;
import natlab.tame.tir.*;
import natlab.backends.Fortran.codegen.*;

public class HandleCaseTIRFunction {
	static boolean Debug =false;
	
	public FortranCodeASTGenerator getFortran(
			FortranCodeASTGenerator fcg, 
			TIRFunction node) {
		fcg.majorName = node.getName();
		for (Name param : node.getInputParams()) {
			fcg.inArgs.add(param.getVarName());
		}
		for (Name result : node.getOutputParams()) {
			fcg.outRes.add(result.getVarName());
		}
		/*
		 *deal with main entry program, actually, sometimes, a subroutine can 
		 *also be with 0 output...TODO think of a better way to distinguish 
		 *whether it is a main entry point or a 0-output subroutine... 
		 */
		if (fcg.outRes.size()==0) {
			CaseNewMainEntryPoint mainEntryPoint = new CaseNewMainEntryPoint();
			mainEntryPoint.newMain(fcg, node);
		}
		/*
		 * deal with functions, not subroutine, because in Fortran, functions 
		 * can only return one value.actually, I can also convert 1-output 
		 * functions in Matlab to Fortran subroutines... moreover, if there 
		 * are arrays in input arguments, we should convert it to subroutine.
		 */
		else if (fcg.outRes.size()==1 && !fcg.hasArrayAsInput()) {
			CaseNewUserDefinedFunc userDefFunc = new CaseNewUserDefinedFunc();
			userDefFunc.newUserDefinedFunc(fcg, node);
		}
		/*
		 * deal with subroutines, which output can be more than one.
		 */
		else {
			CaseNewSubroutine subroutine = new CaseNewSubroutine();
			subroutine.newSubroutine(fcg, node);
		}
		return fcg;
	}
}
