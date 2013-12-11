package natlab.backends.Fortran.codegen_readable.astCaseHandler;

import ast.Name;
import ast.Function;
import java.util.*;

import natlab.backends.Fortran.codegen_readable.*;

public class HandleCaseFunction {
	static boolean Debug =false;
	/**
	 * main entry point MATLAB functions are mapped to main programs in fortran;
	 * functions with only one return value are mapped to functions in fortran;
	 * functions with 0 or more than 1 return values are mapped to subroutines.
	 * 
	 * user-defined functions are mapped to subroutines.
	 *  
	 * Subprogram ::= ProgramTitle DeclarationSection StatementSection;
	 */ 
	public FortranCodeASTGenerator getFortran(
			FortranCodeASTGenerator fcg, 
			Function node) 
	{
		/*
		 * since variable name in matlab is case-sensitive, while in fortran
		 * it's case_insensitive, so we have to rename the variable whose 
		 * name is insensitive equivalent to previous variable.
		 * 
		 * variable names appear in four places:
		 * 1. function parameters, both input and output;
		 * 2. variable declaration section;
		 * 3. parameterized expression;
		 * 4. name expression.
		 */
		/*for (String name : fcg.remainingVars) {
			for (String iterateVar : fcg.remainingVars) {
				if (!name.equals(iterateVar) 
						&& name.toLowerCase().equals(iterateVar.toLowerCase())) {
					if (fcg.sameNameVars.containsKey(name.toLowerCase())) {
						ArrayList<String> valueList = fcg.sameNameVars.get(name);
						if (!valueList.contains(name)) {
							valueList.add(name);
						}
					}
					else {
						ArrayList<String> valueList = new ArrayList<String>();
						valueList.add(name);
						fcg.sameNameVars.put(name.toLowerCase(), valueList);
					}
				}
			}
		}
		if (Debug) System.out.println("variables are " +
				"case-insensitively-equivalent:" + fcg.sameNameVars);*/
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
		 * TODO currently, this case cannot happen...
		 */
		else if (fcg.outRes.size() == 1 && !fcg.userDefinedFunctions.contains(node.getName())) {
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
