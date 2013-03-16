package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.constant.Constant;
import natlab.tame.valueanalysis.components.constant.HasConstant;

public class HandleCaseTIRArraySetStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRArraySetStmt(){
		
	}
	/**
	 * ArraySetStmt: Statement ::= <Indent> [RuntimeCheck] <lhsVariable> <lhsIndex> <rhsVariable>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRArraySetStmt node){
		if (Debug) System.out.println("in an arrayset statement!");
		
		ArraySetStmt stmt = new ArraySetStmt();
		String indent = new String();
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		
		String lhsArrayName = node.getArrayName().getVarName();
		if(fcg.isSubroutine==true){
			/**
			 * if input argument on the LHS of assignment stmt, we assume that this input argument maybe modified.
			 */
			if(fcg.inArgs.contains(lhsArrayName)){
				if (Debug) System.out.println("subroutine's input "+lhsArrayName+" has been modified!");
				/**
				 * here we need to detect whether it is the first time this variable put in the set,
				 * because we only want to back up them once.
				 */
				if(fcg.inputHasChanged.contains(lhsArrayName)){
					//do nothing.
					if (Debug) System.out.println("encounter "+lhsArrayName+" again.");
				}
				else{
					if (Debug) System.out.println("first time encounter "+lhsArrayName);
					fcg.inputHasChanged.add(lhsArrayName);
				}
				lhsArrayName = lhsArrayName+"_copy";
			}
			else{
				//do nothing
			}
		}
		else{
			//do nothing
		}
		stmt.setlhsVariable(lhsArrayName);
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
			if(indexArray.get(i).equals(":")){
				indexBuffer.append(":");
			}
			else if(((HasConstant)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(indexArray.get(i)).getSingleton())).getConstant()!=null){
				Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(indexArray.get(i)).getSingleton())).getConstant();
				double dc = (Double) c.getValue();
				int ic = (int) dc;
				indexBuffer.append(ic);
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
				.get(node.getValueName().getVarName()).getSingleton())).hasConstant())){
			Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
					get(node.getValueName().getVarName()).getSingleton())).getConstant();
			stmt.setrhsVariable(c.toString());
		}
		else{
			stmt.setrhsVariable(node.getValueName().getVarName());
		}
		
		for(String indexName : indexArray){
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
