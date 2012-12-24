package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

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
		
		if(fcg.isSubroutine==true){
			/**
			 * if input argument on the LHS of assignment stmt, we assume that this input argument maybe modified.
			 */
			if(fcg.inArgs.contains(node.getArrayName().getVarName())){
				if (Debug) System.out.println("subroutine's input "+node.getArrayName().getVarName()+" has been modified!");
				/**
				 * here we need to detect whether it is the first time this variable put in the set,
				 * because we only want to back up them once.
				 */
				if(fcg.inputHasChanged.contains(node.getArrayName().getVarName())){
					//do nothing.
					if (Debug) System.out.println("encounter "+node.getArrayName().getVarName()+" again.");
				}
				else{
					if (Debug) System.out.println("first time encounter "+node.getArrayName().getVarName());
					fcg.inputHasChanged.add(node.getArrayName().getVarName());
					BackupVar backupVar = new BackupVar();
					backupVar.setName(node.getArrayName().getVarName()+"_backup = "+node.getArrayName().getVarName()+";\n");
					stmt.setBackupVar(backupVar);
				}
				stmt.setlhsVariable(node.getArrayName().getVarName()+"_backup");
			}
			else{
				stmt.setlhsVariable(node.getArrayName().getVarName());
			}
		}
		else{
			stmt.setlhsVariable(node.getArrayName().getVarName());
		}
		
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
				.get(node.getValueName().getVarName()).getSingleton())).isConstant())){
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
