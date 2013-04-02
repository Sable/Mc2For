package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.constant.Constant;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class HandleCaseTIRArraySetStmt {
	static boolean Debug = false;
	
	/**
	 * ArraySetStmt: Statement ::= <Indent> [RuntimeCheck] <lhsVariable> <lhsIndex> <rhsVariable>;
	 */
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			TIRArraySetStmt node) {
		if (Debug) System.out.println("in an arrayset statement!");
		ArraySetStmt stmt = new ArraySetStmt();
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		String lhsArrayName = node.getArrayName().getVarName();
		if (fcg.isSubroutine) {
			/*
			 * if an input argument of the function is on the LHS of an assignment stmt, 
			 * we assume that this input argument maybe modified.
			 */
			if (fcg.inArgs.contains(lhsArrayName)) {
				if (Debug) System.out.println("subroutine's input "+lhsArrayName
						+" has been modified!");
				/*
				 * here we need to detect whether it is the first time this variable 
				 * put in the set, because we only want to back up them once.
				 */
				if (fcg.inputHasChanged.contains(lhsArrayName)) {
					// do nothing.
					if (Debug) System.out.println("encounter "+lhsArrayName+" again.");
				}
				else {
					if (Debug) System.out.println("first time encounter "+lhsArrayName);
					fcg.inputHasChanged.add(lhsArrayName);
				}
				lhsArrayName = lhsArrayName+"_copy";
			}
		}
		stmt.setlhsVariable(lhsArrayName);
		/*
		 * insert constant variable replacement check for LHS array index.
		 */
		String[] indexString = node.getIndizes().toString().replace("[", "")
				.replace("]", "").split(",");
		ArrayList<String> indexArray = new ArrayList<String>();
		for (String index : indexString) {
			indexArray.add(index);
		}
		StringBuffer indexBuffer = new StringBuffer();
		for (int i=0;i<indexArray.size();i++) {
			if (indexArray.get(i).equals(":")) {
				indexBuffer.append(":");
			}
			else if (fcg.getMatrixValue(indexArray.get(i)).hasConstant()) {
				int intValue = ((Double) fcg.getMatrixValue(indexArray.get(i))
						.getConstant().getValue()).intValue();
				indexBuffer.append(intValue);
			}
			else {
				indexBuffer.append(indexArray.get(i));
			}
			if (i<indexArray.size()-1) {
				indexBuffer.append(",");
			}
		}
		stmt.setlhsIndex(indexBuffer.toString());
		/*
		 * insert constant variable replacement check for RHS variable.
		 */
		String valueName = node.getValueName().getVarName();
		if (fcg.getMatrixValue(valueName).hasConstant()) {
			Constant c = fcg.getMatrixValue(valueName).getConstant();
			stmt.setrhsVariable(c.toString());
		}
		else stmt.setrhsVariable(valueName);		
		for (String indexName : indexArray) {
			if (!indexName.equals(":")) fcg.arrayIndexParameter.add(indexName);
		}		
		return stmt;
	}
}
