package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

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
		
		ArrayList<String> args = new ArrayList<String>();
		int numArgs = node.getLHS().getChild(1).getNumChild();
		for (int i=0;i<numArgs;i++){
			args.add(node.getLHS().getChild(1).getChild(i).getNodeString());
		}
		
		stmt.setlhsVariable(node.getArrayName().getVarName());
		stmt.setlhsIndex(node.getIndizes().toString().replace("[", "").replace("]", ""));
		stmt.setrhsVariable(node.getValueName().getVarName());
		
		for(String indexName : args){
			if(indexName.equals(":")){
				//ignore this
			}
			else{
				fcg.arrayIndexParameter.add(indexName);
			}
		}
		
		return stmt;
	}
}
