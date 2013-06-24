package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import java.util.ArrayList;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.constant.Constant;
import natlab.tame.valueanalysis.components.shape.*;
import natlab.backends.Fortran.codegen_simplified.*;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.*;

public class HandleCaseTIRArraySetStmt {
	static boolean Debug = false;
	
	/**
	 * ArraySetStmt: Statement ::= 
	 * <Indent> [RuntimeAllocate] [RigorousIndexingTransformation] <lhsVariable> <lhsIndex> <rhsVariable>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRArraySetStmt node) {
		if (Debug) System.out.println("in an arrayset statement!");
		ArraySetStmt stmt = new ArraySetStmt();
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		String lhsArrayName = node.getArrayName().getVarName();
		/*
		 * if an input argument of the function is on the LHS of an assignment stmt, 
		 * we assume that this input argument maybe modified.
		 */
		if (fcg.isInSubroutine && fcg.inArgs.contains(lhsArrayName)) {
			if (Debug) System.out.println("subroutine's input "+lhsArrayName
					+" has been modified!");
			fcg.inputHasChanged.add(lhsArrayName);
			lhsArrayName = lhsArrayName+"_copy";
		}
		/*
		 * at least, we need the information of lhs array's shape 
		 * and its corresponding index's shape (maybe value).
		 */
		@SuppressWarnings("rawtypes")
		Shape lhsArrayShape = fcg.getMatrixValue(lhsArrayName).getShape();
		/*
		 * insert constant variable replacement check for LHS array index.
		 */
		String[] indexString = node.getIndizes().toString().replace("[", "")
				.replace("]", "").split(",");
		ArrayList<String> lhsIndex = new ArrayList<String>();
		for (int i=0; i<indexString.length; i++) {
			if (indexString[i].equals(":")) {
				lhsIndex.add(":");
			}
			else if (fcg.getMatrixValue(indexString[i]).hasConstant() 
					&& fcg.tempVarsBeforeF.contains(indexString[i])) {
				int intValue = ((Double) fcg.getMatrixValue(indexString[i])
						.getConstant().getValue()).intValue();
				lhsIndex.add(String.valueOf(intValue));
			}
			else if (fcg.tempVectorAsArrayIndex.containsKey(indexString[i])) {
				ArrayList<String> colonIndex = fcg.tempVectorAsArrayIndex.get(indexString[i]);
				lhsIndex.add(colonIndex.get(0) + ":" + colonIndex.get(1));
			}
			else {
				lhsIndex.add("INT("+indexString[i]+")");
			}
		}
		/*
		 * insert constant variable replacement check for RHS variable.
		 */
		String valueName = node.getValueName().getVarName();
		if (fcg.getMatrixValue(valueName).hasConstant()) {
			Constant c = fcg.getMatrixValue(valueName).getConstant();
			valueName = c.toString();
		}
		if (lhsArrayShape.getDimensions().size() == lhsIndex.size()) {
			stmt.setlhsVariable(lhsArrayName);
			stmt.setlhsIndex(lhsIndex.toString().replace("[", "").replace("]", ""));
			stmt.setrhsVariable(valueName);
		}
		else {
			// TODO separate linear indexing from other rigorous indexing transformation.
			RigorousIndexingTransformation indexTransform = ArraySetIndexingTransformation
					.getTransformedIndex(lhsArrayName, valueName, lhsArrayShape.getDimensions(), lhsIndex);
			stmt.setRigorousIndexingTransformation(indexTransform);
		}
		return stmt;
	}
}
