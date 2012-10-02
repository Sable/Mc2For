package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ast.Name;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;

public class HandleCaseTIRFunction {
	static boolean Debug =false;
	
	public HandleCaseTIRFunction(){
		
	}
	
	public FortranCodeASTGenerator getFortran(FortranCodeASTGenerator fcg, TIRFunction node){
		fcg.majorName = node.getName();
		for(Name param : node.getInputParams()){
			fcg.inArgs.add(param.getVarName());
		}
		for(Name result : node.getOutputParams()){
			fcg.outRes.add(result.getVarName());
		}
		/**
		 *deal with main entry point, main program, actually, sometimes, a subroutine can also be with 0 output...
		 *TODO think of a better way to distinguish whether it is a main entry point or a 0-output subroutine... 
		 */
		if(fcg.outRes.size()==0){
			CaseNewMainEntryPoint mainEntryPoint = new CaseNewMainEntryPoint();
			mainEntryPoint.newMain(fcg, node);
		}
		/**
		 * deal with functions, not subroutine, because in Fortran, functions can essentially only return one value.
		 * actually, I can also convert 1-output functions in Matlab to subroutines...
		 */
		else if(fcg.outRes.size()==1){
			CaseNewUserDefinedFunc userDefFunc = new CaseNewUserDefinedFunc();
			userDefFunc.newUserDefinedFunc(fcg, node);
		}
		/**
		 * deal with subroutines, which output can be more than one.
		 */
		else{
			CaseNewSubroutine subroutine = new CaseNewSubroutine();
			subroutine.newSubroutine(fcg, node);
		}
		return fcg;
	}
}
