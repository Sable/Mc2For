package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import ast.Name;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.constant.Constant;

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
		/**
		 * insert constant variable replacement check for LHS array index.
		 */
		String[] indexString = node.getIndizes().toString().replace("[", "").replace("]", "").split(",");
		ArrayList<String> indexArray = new ArrayList<String>();
		for(String index : indexString){
			indexArray.add(index);
		}
		StringBuffer indexBuffer = new StringBuffer();
		for(int i=0;i<indexArray.size();i++){
			if((((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet()
					.get(indexArray.get(i)).getSingleton())).isConstant())){
				Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(indexArray.get(i)).getSingleton())).getConstant();
				indexBuffer.append(c.toString());
			}
			else{
				indexBuffer.append(indexArray.get(i));
			}
			if(i<indexArray.size()-1){
				indexBuffer.append(",");
			}
			else{
				//do nothing
			}
		}
		stmt.setlhsIndex(indexBuffer.toString());
		/**
		 * insert constant variable replacement check for RHS variable.
		 */
		if((((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet()
				.get(node.getValueName().getVarName()).getSingleton())).isConstant())){
			Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
					get(node.getValueName().getVarName()).getSingleton())).getConstant();
			stmt.setrhsVariable(c.toString());
		}
		else{
			stmt.setrhsVariable(node.getValueName().getVarName());
		}
		
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
