package natlab.backends.Fortran.codegen.ASTcaseHandler;

import ast.Name;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;

public class HandleCaseTIRArraySetStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRArraySetStmt(){
		
	}
	/**
	 * Statement ::= <lhsVariable> <lhsIndex> <rhsVariable>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRArraySetStmt node){
		if (Debug) System.out.println("in an arrayset statement!");
		
		ArraySetStmt stmt = new ArraySetStmt();
		String indent = new String();
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		stmt.setlhsVariable(node.getArrayName().getVarName());
		stmt.setlhsIndex(node.getIndizes().toString().replace("[", "").replace("]", ""));
		stmt.setrhsVariable(node.getValueName().getVarName());
		for(Name index : node.getIndizes().asNameList()){
			fcg.arrayIndexParameter.add(index.getVarName());
		}
		return stmt;
	}
}
