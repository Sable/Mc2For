package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import java.util.List;
import java.util.ArrayList;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.shape.DimValue;
import natlab.backends.Fortran.codegen_simplified.*;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.*;

public class HandleCaseTIRArrayGetStmt {
	static boolean Debug = false;
	
	/**
	 * ArrayGetStmt: Statement ::= <Indent> [RuntimeCheck] 
	 * [RigorousIndexingTransformation] <lhsVariable> [lhsIndex] <rhsVariable> <rhsIndex>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRArrayGetStmt node) {
		if (Debug) System.out.println("in an arrayget statement!");
		ArrayGetStmt stmt = new ArrayGetStmt();
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		String lhsVariable = node.getLHS().getNodeString().replace("[", "").replace("]", "");
		String rhsArrayName = node.getArrayName().getVarName();
		/*
		 * if an input argument of the function is on the LHS of an assignment stmt, 
		 * we assume that this input argument maybe modified.
		 */
		if (fcg.isInSubroutine && fcg.inArgs.contains(rhsArrayName)) {
			if (Debug) System.out.println("subroutine's input "+rhsArrayName
					+" has been modified!");
			fcg.inputHasChanged.add(rhsArrayName);
			rhsArrayName = rhsArrayName+"_copy";
		}
		/*
		 * at least, we need the information of rhs array's shape 
		 * and its corresponding index's shape (maybe value).
		 */
		List<DimValue> rhsArrayDimension = fcg.getMatrixValue(rhsArrayName)
				.getShape().getDimensions();
		/*
		 * insert constant variable replacement check for RHS array index.
		 */
		String[] indexString = node.getIndizes().toString().replace("[", "")
				.replace("]", "").split(",");
		ArrayList<String> rhsIndex = new ArrayList<String>();
		ArrayList<String> lhsIndex = new ArrayList<String>();
		for (int i=0; i<indexString.length; i++) {
			if (indexString[i].equals(":")) {
				rhsIndex.add(":");
				lhsIndex.add(":");
			}
			else if (fcg.getMatrixValue(indexString[i]).hasConstant() 
					&& fcg.tamerTmpVar.contains(indexString[i])) {
				int intValue = ((Double) fcg.getMatrixValue(indexString[i])
						.getConstant().getValue()).intValue();
				rhsIndex.add(String.valueOf(intValue));
				lhsIndex.add("1");
			}
			else if (fcg.tmpVectorAsArrayIndex.containsKey(indexString[i])) {
				ArrayList<String> colonIndex = fcg.tmpVectorAsArrayIndex.get(indexString[i]);
				rhsIndex.add(colonIndex.get(0) + ":" + colonIndex.get(1));
				List<DimValue> colonShape = fcg.getMatrixValue(indexString[i])
						.getShape().getDimensions();
				lhsIndex.add(colonShape.get(0) + ":" + colonShape.get(1));
			}
			else {
				rhsIndex.add("INT("+indexString[i]+")");
				lhsIndex.add("INT("+indexString[i]+")");
			}
		}
		if (rhsArrayDimension.size() == rhsIndex.size()) {
			stmt.setlhsVariable(lhsVariable);
			stmt.setrhsVariable(rhsArrayName);
			stmt.setrhsIndex(rhsIndex.toString().replace("[", "").replace("]", ""));
			if (!fcg.getMatrixValue(lhsVariable).getShape().isScalar()) {
				lhsIndex lhsindex = new lhsIndex();
				lhsindex.setName(lhsIndex.toString().replace("[", "").replace("]", ""));
				stmt.setlhsIndex(lhsindex);
			}
		}
		else {
			// TODO separate linear indexing from other rigorous indexing transformation.
			RigorousIndexingTransformation indexTransform = ArrayGetIndexingTransformation
					.getTransformedIndex(lhsVariable, rhsArrayName, rhsArrayDimension, rhsIndex);
			stmt.setRigorousIndexingTransformation(indexTransform);
		}
		return stmt;
	}
}
