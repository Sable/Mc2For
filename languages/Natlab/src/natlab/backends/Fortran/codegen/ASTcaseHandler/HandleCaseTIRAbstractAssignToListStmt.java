package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import natlab.backends.Fortran.codegen.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;

public class HandleCaseTIRAbstractAssignToListStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRAbstractAssignToListStmt(){
		
	}
	
	public FortranCodePrettyPrinter getFortran(FortranCodePrettyPrinter fcg, TIRAbstractAssignToListStmt node){
		if (Debug) System.out.println("in an abstractAssignToList  statement");
		if(fcg.FortranMap.isFortranNoDirectBuiltin(node.getRHS().getVarName())){
			if (Debug) System.out.println("the function \""+node.getRHS().getVarName()+"\" has no corresponding builtin function in Fortran...");
			FortranCodeInliner fci = new FortranCodeInliner();
			fci.inline(fcg, node);
			//TODO add more no direct mapping built-ins
		}
		else{
			String LHS;
			ArrayList<String> vars = new ArrayList<String>();
			for(ast.Name name : node.getTargets().asNameList()){
				vars.add(name.getID());
			}
			/**
			 * deal with difference number of output.
			 */
			if(vars.size()>1){
				/**
				 * this should be a call statement to call a subroutine.
				 */
				ArrayList<String> Args = new ArrayList<String>();
				String ArgsListasString, OutputsListasString;
				Args = fcg.getArgsList(node);
				ArgsListasString = fcg.getArgsListAsString(Args);
				OutputsListasString = fcg.getArgsListAsString(vars);
				fcg.buf.append("call "+node.getRHS().getVarName()+"("+ArgsListasString+", "+OutputsListasString+")");
			}
			else if(1==vars.size()){
				LHS = vars.get(0);
				if(fcg.isSubroutine==true){//which means this statement is in an subroutine
					fcg.buf.append(LHS+" = ");
				}
				else{
					if(fcg.outRes.contains(LHS)){
						fcg.buf.append(fcg.majorName + " = ");
					}
					else{
						fcg.buf.append(LHS+" = ");
					}
				}
				//use varname to get the name of the method/operator/Var
				fcg.makeExpression(node);
			}
			else if(0==vars.size()){
				//TODO
				fcg.makeExpression(node);
			}
		}
		return fcg;
	}
}
