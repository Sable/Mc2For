package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;

public class HandleCaseTIRArrayGetStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRArrayGetStmt(){
		
	}
	/**
	 * ArrayGetStmt: Statement ::= <lhsVariable> [lhsIndex] <rhsVariable> <rhsIndex>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRArrayGetStmt node){
		if (Debug) System.out.println("in an arrayget statement!");
		
		ArrayGetStmt stmt = new ArrayGetStmt();
		String indent = new String();
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		String indexList = node.getIndizes().toString();
		String[] tokens = indexList.replace("[", "").replace("]", "").split("[,]");
		ArrayList<String> tokensAsArray = new ArrayList<String>();
		for(String indexName : tokens){
			tokensAsArray.add(indexName);
		}
		if(tokensAsArray.contains(":")){		
			stmt.setlhsVariable(node.getLHS().getNodeString().replace("[", "").replace("]", ""));
			lhsIndex lhsIndex = new lhsIndex();	
			lhsIndex.setName(node.getIndizes().toString().replace("[", "").replace("]", "")+")");
			stmt.setlhsIndex(lhsIndex);
			stmt.setrhsVariable(node.getArrayName().getVarName());
			stmt.setrhsIndex(node.getIndizes().toString().replace("[", "").replace("]", ""));
		}
		else{
			stmt.setlhsVariable(node.getLHS().getNodeString().replace("[", "").replace("]", ""));
			stmt.setrhsVariable(node.getArrayName().getVarName());
			stmt.setrhsIndex(node.getIndizes().toString().replace("[", "").replace("]", ""));
		}
		for(String indexName : tokens){
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
