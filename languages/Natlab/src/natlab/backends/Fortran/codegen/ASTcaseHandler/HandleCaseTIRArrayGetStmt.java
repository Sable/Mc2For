package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.List;
import java.util.ArrayList;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.shape.DimValue;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class HandleCaseTIRArrayGetStmt {
	static boolean Debug = false;
	
	/**
	 * ArrayGetStmt: Statement ::= <Indent> [RuntimeCheck] 
	 * [RigorousIndexingTransformation] <lhsVariable> [lhsIndex] <rhsVariable> <rhsIndex>;
	 */
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			TIRArrayGetStmt node) {
		if (Debug) System.out.println("in an arrayget statement!");
		ArrayGetStmt stmt = new ArrayGetStmt();
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		String lhsVariable = node.getLHS().getNodeString().replace("[", "").replace("]", "");
		if (fcg.isSubroutine) {
			/*
			 * if an input argument of the function is on the LHS of an assignment stmt, 
			 * we assume that this input argument maybe modified.
			 */
			if (fcg.inArgs.contains(lhsVariable)) {
				if (Debug) System.out.println("subroutine's input "+lhsVariable
						+" has been modified!");
				/*
				 * here we need to detect whether it is the first time this variable put in 
				 * the set, because we only want to back up them once.
				 */
				if (fcg.inputHasChanged.contains(lhsVariable)) {
					// do nothing.
					if (Debug) System.out.println("encounter "+lhsVariable+" again.");
				}
				else {
					if (Debug) System.out.println("first time encounter "+lhsVariable);
					fcg.inputHasChanged.add(lhsVariable);
				}
				lhsVariable = lhsVariable+"_copy";
			}
		}
		/*
		 * at least, we need the information of rhs array's shape 
		 * and its corresponding index's shape (maybe value).
		 */
		String rhsArrayName = node.getArrayName().getVarName();
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
			else if (fcg.getMatrixValue(indexString[i]).hasConstant()) {
				int intValue = ((Double) fcg.getMatrixValue(indexString[i])
						.getConstant().getValue()).intValue();
				rhsIndex.add(String.valueOf(intValue));
				lhsIndex.add("1");
			}
			else if (fcg.tmpVarAsArrayIndex.containsKey(indexString[i])) {
				ArrayList<String> colonIndex = fcg.tmpVarAsArrayIndex.get(indexString[i]);
				rhsIndex.add(colonIndex.get(0) + ":" + colonIndex.get(1));
				List<DimValue> colonShape = fcg.getMatrixValue(indexString[i])
						.getShape().getDimensions();
				lhsIndex.add(colonShape.get(0) + ":" + colonShape.get(1));
			}
			else {
				rhsIndex.add(indexString[i]);
				lhsIndex.add(indexString[i]);
			}
		}
		if (rhsArrayDimension.size() == rhsIndex.size()) {
			stmt.setlhsVariable(lhsVariable);
			stmt.setrhsVariable(rhsArrayName);
			stmt.setrhsIndex(rhsIndex.toString().replace("[", "").replace("]", ""));
			lhsIndex lhsindex = new lhsIndex();
			lhsindex.setName(lhsIndex.toString().replace("[", "").replace("]", ""));
			stmt.setlhsIndex(lhsindex);
		}
		else {
			RigorousIndexingTransformation indexTransform = ArrayGetIndexingTransformation
					.getTransformedIndex(lhsVariable, rhsArrayName, rhsArrayDimension, rhsIndex);
			stmt.setRigorousIndexingTransformation(indexTransform);
		}
		for (ast.Name indexName : node.getIndizes().asNameList()) {
			if (indexName!=null) 
				if (!indexName.tmpVar) fcg.arrayIndexParameter.add(indexName.getID());
		}
		return stmt;
	}
}
