package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import java.util.ArrayList;
import java.util.List;

import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.RigorousIndexingTransformation;
import natlab.tame.valueanalysis.components.shape.DimValue;

public class ArrayGetIndexingTransformation {

	public static RigorousIndexingTransformation getTransformedIndex(
			String lhsVariable, 
			String rhsArrayName, 
			List<DimValue> rhsArrayDimension, 
			ArrayList<String> rhsIndex) {
		RigorousIndexingTransformation transformedArrayGet = new RigorousIndexingTransformation();
		StringBuffer sb = new StringBuffer();
		sb.append("CALL ARRAT_GET");
		sb.append(rhsArrayDimension.size());
		for (int i=0; i<rhsIndex.size(); i++) {
			String[] vectorIndex = rhsIndex.get(i).split(":");
			if (rhsIndex.get(i).equals(":")) {
				sb.append("C");
			}
			else if (vectorIndex.length==2) {
				sb.append("V");
			}
			else {
				sb.append("S");
			}
		}
		sb.append("(");
		sb.append(rhsArrayName+", ");
		for (int i=0; i<rhsArrayDimension.size(); i++) {
			sb.append(rhsArrayDimension.get(i)+", ");
		}
		for (int i=0; i<rhsIndex.size(); i++) {
			String[] vectorIndex = rhsIndex.get(i).split(":");
			if (rhsIndex.get(i).equals(":")) {
				// do nothing.
			}
			else if (vectorIndex.length==2) {
				sb.append(vectorIndex[0]+", "+vectorIndex[1]+", ");
			}
			else {
				sb.append(rhsIndex.get(i)+", ");
			}
		}
		sb.append(lhsVariable+");");
		transformedArrayGet.setFunctionCall(sb.toString());
		return transformedArrayGet;
	}
}
