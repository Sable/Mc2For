package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import java.util.ArrayList;
import java.util.List;

import natlab.backends.Fortran.codegen_simplified.FortranCodeASTGenerator;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.ArrayGetStmt;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.RigorousIndexingTransformation;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.Statement;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.lhsIndex;
import natlab.tame.tir.TIRArrayGetStmt;
import natlab.tame.valueanalysis.components.shape.DimValue;
import natlab.tame.valueanalysis.components.shape.Shape;

public class HandleCaseTIRArrayGetStmt {
	static boolean Debug = false;
	
	/**
	 * ArrayGetStmt: Statement ::= 
	 * <Indent> [RuntimeAllocate] [RigorousIndexingTransformation] <lhsVariable> [lhsIndex] <rhsVariable> <rhsIndex>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRArrayGetStmt node) {
		if (Debug) System.out.println("in an arrayget statement!");
		ArrayGetStmt stmt = new ArrayGetStmt();
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.standardIndent;
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
		Shape rhsArrayShape = fcg.getMatrixValue(rhsArrayName)
				.getShape();
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
					&& fcg.tempVarsBeforeF.contains(indexString[i])) {
				int intValue = ((Double) fcg.getMatrixValue(indexString[i])
						.getConstant().getValue()).intValue();
				rhsIndex.add(String.valueOf(intValue));
				lhsIndex.add("1");
			}
			else if (fcg.tempVectorAsArrayIndex.containsKey(indexString[i])) {
				ArrayList<String> colonIndex = fcg.tempVectorAsArrayIndex.get(indexString[i]);
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
		if (rhsArrayShape.getDimensions().size() == rhsIndex.size()) {
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
			if (rhsArrayShape.isConstant() && isIndexConstant(rhsIndex)) {
				// perform linear indexing transformation.
				ArrayList<Integer> newIndex = new ArrayList<Integer>();
				int position = 0;
				for (int i=0; i+1<rhsIndex.size(); ) {
					newIndex.add(Integer.parseInt(rhsIndex.get(i)));
					position = ++i;
				}
				for (int i=position; i<rhsArrayShape.getDimensions().size(); i++) {
					newIndex.add(0);
				}
				for (int i=rhsArrayShape.getDimensions().size()-1; i>=position; i--) {
					double remain = getHowNumbersFromTo(rhsArrayShape, position, i);
					int index = (int) Math.ceil(Double.parseDouble(rhsIndex.get(position)) / remain);
					int mod = (int) (Double.parseDouble(rhsIndex.get(position)) % remain);
					if (mod==0) 
						mod = ((DimValue)rhsArrayShape.getDimensions().get(position)).getIntValue();
					newIndex.set(i, index);
					rhsIndex.set(position, String.valueOf(mod));
				}
				stmt.setlhsVariable(lhsVariable);
				stmt.setrhsVariable(rhsArrayName);
				stmt.setrhsIndex(newIndex.toString().replace("[", "").replace("]", ""));
			}
			else {
				RigorousIndexingTransformation indexTransform = ArrayGetIndexingTransformation
						.getTransformedIndex(lhsVariable, rhsArrayName, rhsArrayShape.getDimensions(), rhsIndex);
				stmt.setRigorousIndexingTransformation(indexTransform);
			}
		}
		return stmt;
	}
	
	/****************************helper function**************************/
	private boolean isIndexConstant(ArrayList<String> rhsIndex) {
		for (int i=0; i<rhsIndex.size(); i++) {
			try {
				Integer.parseInt(rhsIndex.get(i));
			} catch(NumberFormatException e) {
				return false;
			}
		}
		return true;
	}
	
	private int getHowNumbersFromTo(Shape rhsArrayShape, int begin, int end) {
		int sum = 1;
		for (int i=begin; i<end; i++) {
			sum *= ((DimValue)rhsArrayShape.getDimensions().get(i)).getIntValue();
		}
		return sum;
	}
}
