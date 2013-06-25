package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import java.util.ArrayList;

import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
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
			indent = indent + fcg.standardIndent;
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
		String[] indexString = node.getIndizes().toString().replace("[", "")
				.replace("]", "").split(",");
		if (!lhsArrayShape.isConstant()) {
			/*
			 * insert run-time array bound check and reallocation.
			 * 1. compare the indices with the size of each dimension;
			 * 2. if the index exceeds the bound, backup the array first;
			 * 3. deallocate the array and reallocate it;
			 * 4. assign backup back to the new array.
			 */
			RuntimeAllocate rta = new RuntimeAllocate();
			StringBuffer tempBuf = new StringBuffer();
			for (int i=0; i<lhsArrayShape.getDimensions().size(); i++) {
				tempBuf.append(indent+lhsArrayName+"_d"+(i+1)+" = SIZE("+lhsArrayName+", "+(i+1)+");\n");
				BasicMatrixValue tmp = new BasicMatrixValue(null, PrimitiveClassReference.INT32, 
						(new ShapeFactory()).getScalarShape(), null);
				fcg.tempVarsFortran.put(lhsArrayName+"_d"+(i+1), tmp);
			}
			tempBuf.append(indent+"IF (");
			for (int i=0; i<lhsArrayShape.getDimensions().size(); i++) {
				if (indexString[i].equals(":")) 
					; // do nothing.
				else if (fcg.getMatrixValue(indexString[i]).hasConstant() 
						&& fcg.tempVarsBeforeF.contains(indexString[i])) {
					int intValue = ((Double) fcg.getMatrixValue(indexString[i])
							.getConstant().getValue()).intValue();
					tempBuf.append("("+String.valueOf(intValue)
							+" > "+lhsArrayName+"_d"+(i+1)+")");
				}
				else if (fcg.getMatrixValue(indexString[i]).getShape().isScalar()) 
					tempBuf.append("("+indexString[i]
							+" > "+lhsArrayName+"_d"+(i+1)+")");
				else if (!fcg.getMatrixValue(indexString[i]).getShape().isScalar() 
						&& fcg.tempVectorAsArrayIndex.containsKey(indexString[i])) {
					tempBuf.append("("+fcg.tempVectorAsArrayIndex.get(indexString[i]).get(1)
							+" > "+lhsArrayName+"_d"+(i+1)+")");
				}
				else {
					// TODO deal with the rest cases.
				}
				if (i+1!=lhsArrayShape.getDimensions().size() && !indexString[i+1].equals(":")) 
					tempBuf.append(" .OR. ");
			}
			tempBuf.append(") THEN\n");
			tempBuf.append(indent+fcg.standardIndent+"IF (ALLOCATED("+lhsArrayName+"_bk)) THEN\n");
			tempBuf.append(indent+fcg.standardIndent+fcg.standardIndent+"DEALLOCATE("+lhsArrayName+"_bk);\n");
			tempBuf.append(indent+fcg.standardIndent+"END IF\n");
			tempBuf.append(indent+fcg.standardIndent+"ALLOCATE("+lhsArrayName+"_bk(");
			for (int i=0; i<lhsArrayShape.getDimensions().size(); i++) {
				tempBuf.append(lhsArrayName+"_d"+(i+1));
				if (i+1!=lhsArrayShape.getDimensions().size())
					tempBuf.append(", ");
			}
			tempBuf.append(");\n");
			tempBuf.append(indent+fcg.standardIndent+lhsArrayName+"_bk = "+lhsArrayName+";\n");
			tempBuf.append(indent+fcg.standardIndent+"DEALLOCATE("+lhsArrayName+");\n");
			for (int i=0; i<lhsArrayShape.getDimensions().size(); i++) {
				if (indexString[i].equals(":")) 
					tempBuf.append(indent+fcg.standardIndent+lhsArrayName+"_d"+(i+1)+"max " +
							"= "+lhsArrayName+"_d"+(i+1)+";\n");
				else if (fcg.getMatrixValue(indexString[i]).hasConstant() 
						&& fcg.tempVarsBeforeF.contains(indexString[i])) {
					int intValue = ((Double) fcg.getMatrixValue(indexString[i])
							.getConstant().getValue()).intValue();
					tempBuf.append(indent+fcg.standardIndent+lhsArrayName+"_d"+(i+1)+"max " +
							"= MAX"+"("+String.valueOf(intValue)
							+", "+lhsArrayName+"_d"+(i+1)+");\n");
				}
				else if (fcg.getMatrixValue(indexString[i]).getShape().isScalar()) 
					tempBuf.append(indent+fcg.standardIndent+lhsArrayName+"_d"+(i+1)+"max " +
							"= MAX"+"("+indexString[i]
							+", "+lhsArrayName+"_d"+(i+1)+");\n");
				else if (!fcg.getMatrixValue(indexString[i]).getShape().isScalar() 
						&& fcg.tempVectorAsArrayIndex.containsKey(indexString[i])) {
					tempBuf.append(indent+fcg.standardIndent+lhsArrayName+"_d"+(i+1)+"max " +
							"= MAX"+"("+fcg.tempVectorAsArrayIndex.get(indexString[i]).get(1)
							+", "+lhsArrayName+"_d"+(i+1)+");\n");
				}
				else {
					// TODO deal with the rest cases.
				}
				BasicMatrixValue tmp = new BasicMatrixValue(null, PrimitiveClassReference.INT32, 
						(new ShapeFactory()).getScalarShape(), null);
				fcg.tempVarsFortran.put(lhsArrayName+"_d"+(i+1)+"max", tmp);				
			}
			tempBuf.append(indent+fcg.standardIndent+"ALLOCATE("+lhsArrayName+"(");
			for (int i=0; i<lhsArrayShape.getDimensions().size(); i++) {
				tempBuf.append(lhsArrayName+"_d"+(i+1)+"max");
				if (i+1!=lhsArrayShape.getDimensions().size()) 
					tempBuf.append(", ");
			}
			tempBuf.append("));\n");
			tempBuf.append(indent+fcg.standardIndent+lhsArrayName+"(");
			for (int i=0; i<lhsArrayShape.getDimensions().size(); i++) {
				tempBuf.append("1:"+lhsArrayName+"_d"+(i+1));
				if (i+1!=lhsArrayShape.getDimensions().size())
					tempBuf.append(", ");
			}
			tempBuf.append(") = "+lhsArrayName+"_bk(");
			for (int i=0; i<lhsArrayShape.getDimensions().size(); i++) {
				tempBuf.append("1:"+lhsArrayName+"_d"+(i+1));
				if (i+1!=lhsArrayShape.getDimensions().size())
					tempBuf.append(", ");
			}
			tempBuf.append(");\n");
			tempBuf.append(indent+"END IF\n");
			rta.setBlock(tempBuf.toString());
			stmt.setRuntimeAllocate(rta);
		}
		/*
		 * insert constant variable replacement check for LHS array index.
		 */
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
