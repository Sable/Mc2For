package natlab.backends.Fortran.codegen.caseHandler;

import ast.Name;
import natlab.backends.Fortran.codegen.*;
import natlab.tame.tir.*;

public class HandleCaseTIRArraySetStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRArraySetStmt(){
		
	}
	
	public FortranCodeGenerator getFortran(FortranCodeGenerator fcg, TIRArraySetStmt node){
		if (Debug) System.out.println("in an arrayset statement!");
		fcg.buf.append(node.getArrayName().getVarName()+"("+node.getIndizes().toString().replace("[", "").replace("]", "")+")"+" = "+node.getValueName().getVarName()+";");
		for(Name index : node.getIndizes().asNameList()){
			fcg.arrayIndexParameter.add(index.getVarName());
		}
		return fcg;
	}
}
