package natlab.backends.Fortran.codegen_simplified;

import java.util.ArrayList;
import java.util.List;

import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.tir.TIRAbstractAssignToListStmt;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.shape.*;
import natlab.tame.valueanalysis.components.constant.*;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.*;
import natlab.backends.Fortran.codegen_simplified.astCaseHandler.*;

public class FortranCodeASTInliner {
	static boolean Debug = false;
	
	@SuppressWarnings("rawtypes")
	public static NoDirectBuiltinExpr inline(FortranCodeASTGenerator fcg, TIRAbstractAssignToListStmt node, 
			List<Shape> currentShape) {
		NoDirectBuiltinExpr noDirBuiltinExpr = new NoDirectBuiltinExpr();
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		/* 
		 * TODO how to deal with the case that the number 
		 * of lhs target variables is more than one?
		 */
		String lhsTarget = node.getLHS().getNodeString().replace("[", "").replace("]", "");
		String cellLHSTarget = lhsTarget;
		StringBuffer tempBuf = new StringBuffer();
		/*
		 * if input arguments on the LHS of an assignment stmt, 
		 * we assume that this input argument may be modified.
		 */
		if (fcg.isInSubroutine && fcg.inArgs.contains(lhsTarget)) {
			if (Debug) System.out.println("subroutine's input "+lhsTarget
					+" has been modified!");
			fcg.inputHasChanged.add(lhsTarget);
			lhsTarget=lhsTarget+"_copy";
		}
		if (!fcg.hasSingleton(lhsTarget)) {
			/*
			 * assign different type value to the same variable, 
			 * we need to generate a derived type in Fortran for 
			 * this variable, the fields in the derived type 
			 * correspond to different type.
			 */
			if (fcg.forCellArr.keySet().contains(lhsTarget)) {
				int fieldNum = 0, i = 0;
				for (BasicMatrixValue fieldValue : fcg.forCellArr.get(lhsTarget)) {
					if (fieldValue.getShape().equals(currentShape.get(0))) {
						fieldNum = i;
					}
					else i++;
				}
				cellLHSTarget = lhsTarget+"%f"+fieldNum;
			}
			else {
				ArrayList<BasicMatrixValue> valueList = new ArrayList<BasicMatrixValue>();
				int length = fcg.getValueSet(lhsTarget).values().toArray().length;
				for (int i=0; i<length; i++) {
					valueList.add((BasicMatrixValue)fcg.getValueSet(lhsTarget).values().toArray()[i]);
				}
				fcg.forCellArr.put(lhsTarget, valueList);
			}
		}
		String rhsFunName = node.getRHS().getVarName();
		ArrayList<String> rhsArgs = new ArrayList<String>();
		rhsArgs = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
		int numOfArgs = rhsArgs.size();
		/*
		 * below are all the cases by enumeration, extendable.
		 */
		tempBuf.append(indent+"!mapping function "+rhsFunName+"\n");
		
		/*********************built-in function enumeration*******************/
		if (rhsFunName.equals("horzcat")) {
			for (int i=1; i<=numOfArgs; i++) {
				/*
				 * need constant folding check.
				 */
				if (fcg.getMatrixValue(rhsArgs.get(i-1)).hasConstant()) {
					Constant c = fcg.getMatrixValue(rhsArgs.get(i-1)).getConstant();
					tempBuf.append(indent+cellLHSTarget+"(1,"+i+") = "+c+";");
				}
				else {
					tempBuf.append(indent+cellLHSTarget+"(1,"+i+") = "+rhsArgs.get(i-1)+";");
				}
				if (i<numOfArgs) tempBuf.append("\n");
			}
			tempBuf.append("\n"+indent+"!mapping function "+rhsFunName
					+" is over.");
			noDirBuiltinExpr.setCodeInline(tempBuf.toString());
		}		
		else if (rhsFunName.equals("vertcat")) {
			for (int i=1; i<=numOfArgs; i++) {
				/*
				 * need constant folding check.
				 */
				if (fcg.getMatrixValue(rhsArgs.get(i-1)).hasConstant()) {
					Constant c = fcg.getMatrixValue(rhsArgs.get(i-1)).getConstant();
					tempBuf.append(indent+cellLHSTarget+"("+i+",1) = "+c+";");
				}
				else {
					tempBuf.append(indent+cellLHSTarget+"("+i+",:) = "+rhsArgs.get(i-1)+"(1,:);");
				}
				if(i<numOfArgs) tempBuf.append("\n");
			}
			tempBuf.append("\n"+indent+"!mapping function "+rhsFunName
					+" is over.");
			noDirBuiltinExpr.setCodeInline(tempBuf.toString());
		}
		else if (rhsFunName.equals("ones")) {
			/*
			 * need constant folding check.
			 */
			if (rhsArgs.size()==1) {
				return noDirBuiltinExpr;
			}
			if (fcg.getMatrixValue(rhsArgs.get(0)).hasConstant()) {
				DoubleConstant c = (DoubleConstant) fcg.getMatrixValue(rhsArgs.get(0))
						.getConstant();
				int ci = c.getValue().intValue();
				tempBuf.append(indent+"DO tmp_"+lhsTarget+"_i = 1 , "+ci+"\n");
			}
			else {
				tempBuf.append(indent+"DO tmp_"+lhsTarget+"_i = 1 , int("+rhsArgs.get(0)+")\n");
			}
			if (fcg.getMatrixValue(rhsArgs.get(1)).hasConstant()) {
				DoubleConstant c = (DoubleConstant) fcg.getMatrixValue(rhsArgs.get(1))
						.getConstant();
				int ci = c.getValue().intValue();
				tempBuf.append(indent+fcg.indent+"DO tmp_"+lhsTarget+"_j = 1 , "+ci+"\n");
			}
			else {
				tempBuf.append(indent+fcg.indent+"DO tmp_"+lhsTarget+"_j = 1 , int("+rhsArgs.get(1)
						+")\n");
			}
			tempBuf.append(indent+fcg.indent+fcg.indent+cellLHSTarget+"(tmp_"+lhsTarget
					+"_i,tmp_"+lhsTarget+"_j) = 1;\n");
			tempBuf.append(indent+fcg.indent+"ENDDO\n");
			tempBuf.append(indent+"ENDDO");
			BasicMatrixValue tmp = new BasicMatrixValue(null, PrimitiveClassReference.INT32, 
					(new ShapeFactory()).getScalarShape(), null);
			fcg.tempVarsFortran.put("tmp_"+lhsTarget+"_i", tmp);
			fcg.tempVarsFortran.put("tmp_"+lhsTarget+"_j", tmp);
			tempBuf.append("\n"+indent+"!mapping function "+rhsFunName
					+" is over.");
			noDirBuiltinExpr.setCodeInline(tempBuf.toString());
		}		
		else if (rhsFunName.equals("zeros")) {
			/*
			 * need constant folding check.
			 */
			if (fcg.getMatrixValue(rhsArgs.get(0)).hasConstant()) {
				DoubleConstant c = (DoubleConstant) fcg.getMatrixValue(rhsArgs.get(0))
						.getConstant();
				int ci = c.getValue().intValue();
				tempBuf.append(indent+"DO tmp_"+lhsTarget+"_i = 1 , "+ci+"\n");
			}
			else {
				tempBuf.append(indent+"DO tmp_"+lhsTarget+"_i = 1 , int("+rhsArgs.get(0)+")\n");
			}
			if (fcg.getMatrixValue(rhsArgs.get(1)).hasConstant()){
				DoubleConstant c = (DoubleConstant) fcg.getMatrixValue(rhsArgs.get(1))
						.getConstant();
				int ci = c.getValue().intValue();
				tempBuf.append(indent+fcg.indent+"DO tmp_"+lhsTarget+"_j = 1 , "+ci+"\n");
			}
			else {
				tempBuf.append(indent+fcg.indent+"DO tmp_"+lhsTarget
						+"_j = 1 , int("+rhsArgs.get(1)+")\n");
			}
			tempBuf.append(indent+fcg.indent+fcg.indent+cellLHSTarget+"(tmp_"+lhsTarget
					+"_i,tmp_"+lhsTarget+"_j) = 0;\n");
			tempBuf.append(indent+fcg.indent+"ENDDO\n");
			tempBuf.append(indent+"ENDO");
			BasicMatrixValue tmp = new BasicMatrixValue(null, PrimitiveClassReference.INT32, 
					(new ShapeFactory()).getScalarShape(), null);
			fcg.tempVarsFortran.put("tmp_"+lhsTarget+"_i", tmp);
			fcg.tempVarsFortran.put("tmp_"+lhsTarget+"_j", tmp);
			tempBuf.append("\n"+indent+"!mapping function "+rhsFunName
					+" is over.");
			noDirBuiltinExpr.setCodeInline(tempBuf.toString());
		}		
		else if (rhsFunName.equals("colon")) {
			/*
			 * Depending on the fact that whether the target variable is temporary, 
			 * we have two solutions for colon.
			 * 1. if it is a temporary variable, it means that this variable will be used 
			 * as an index in the future;
			 * 2. if it is not a temporary variable, it means that this variable is made 
			 * on purpose, we should leave it as people expected.
			 */
			if (node.getTargets().asNameList().get(0).tmpVar) {
				// TODO store the range information of this temp variable for later use.
				for (int i=0 ; i<rhsArgs.size() ; i++) {
					if (fcg.getMatrixValue(rhsArgs.get(i)).hasConstant()) {
						DoubleConstant c = (DoubleConstant) fcg.getMatrixValue(rhsArgs.get(i))
								.getConstant();
						int ci = c.getValue().intValue();
						rhsArgs.remove(i);
						rhsArgs.add(i, String.valueOf(ci));
					}
				}
				fcg.tempVectorAsArrayIndex.put(node.getTargets().asNameList().get(0).getID(), rhsArgs);
			}
			else {
				/*
				 * a=1:10 -> a=[1,2,3,...,10]
				 * a=1:2:10 -> a=[1,3,5,...,9]
				 * so, depends on the number of input parameters, there are two transformations.
				 */
				if (numOfArgs==2) {
					/* a = arg1:arg2
					 * -->
					 * do tmp_a_i = arg1,arg2
				  	 *   a(1,tmp_a_i) = tmp_a_i;
				     * enddo
				     */
					/*
					 * need constant folding check.
					 */
					if (fcg.getMatrixValue(rhsArgs.get(0)).hasConstant() 
							&& fcg.tempVarsBeforeF.contains(rhsArgs.get(0))) {
						DoubleConstant c = (DoubleConstant) fcg.getMatrixValue(rhsArgs.get(0))
								.getConstant();
						int ci = c.getValue().intValue();
						tempBuf.append(indent+"DO tmp_"+lhsTarget+"_i = "+ci);
					}
					else {
						tempBuf.append(indent+"DO tmp_"+lhsTarget+"_i = "+rhsArgs.get(0));
					}
					if (fcg.getMatrixValue(rhsArgs.get(1)).hasConstant() 
							&& fcg.tempVarsBeforeF.contains(rhsArgs.get(1))) {
						DoubleConstant c = (DoubleConstant) fcg.getMatrixValue(rhsArgs.get(1))
								.getConstant();
						int ci = c.getValue().intValue();
						tempBuf.append(","+ci+"\n");
					}
					else {
						tempBuf.append(","+rhsArgs.get(1)+"\n");
					}
					tempBuf.append(fcg.indent+fcg.indent+cellLHSTarget+"(1,tmp_"+lhsTarget
							+"_i) = tmp_"+lhsTarget+"_i;\n");
					tempBuf.append(indent+"ENDDO");
					BasicMatrixValue tmp = new BasicMatrixValue(null, PrimitiveClassReference.INT32, 
							(new ShapeFactory()).getScalarShape(), null);
					fcg.tempVarsFortran.put("tmp_"+lhsTarget+"_i", tmp);
				}
				else if (numOfArgs==3) {
					/* a = lower:inc:upper
					 * -->
					 * tmp_a_index = 1;
					 * do tmp_a_i = lower,upper,inc
				  	 *   a(1,tmp_a_index) = tmp_a_i;
				  	 *   tmp_a_index=tmp_a_index+1;
				     * enddo
				     */
					tempBuf.append("tmp_"+lhsTarget+"_index = 1;\n");
					/*
					 * need constant folding check.
					 */
					if (fcg.getMatrixValue(rhsArgs.get(0)).hasConstant() 
							&& fcg.tempVarsBeforeF.contains(rhsArgs.get(0))) {
						DoubleConstant c = (DoubleConstant) fcg.getMatrixValue(rhsArgs.get(0))
								.getConstant();
						int ci = c.getValue().intValue();
						tempBuf.append(indent+"DO tmp_"+lhsTarget+"_i = "+ci);
					}
					else {
						tempBuf.append(indent+"DO tmp_"+lhsTarget+"_i = "+rhsArgs.get(0));
					}
					if (fcg.getMatrixValue(rhsArgs.get(2)).hasConstant() 
							&& fcg.tempVarsBeforeF.contains(rhsArgs.get(2))) {
						DoubleConstant c = (DoubleConstant) fcg.getMatrixValue(rhsArgs.get(2))
								.getConstant();
						int ci = c.getValue().intValue();
						tempBuf.append(","+ci);
					}
					else {
						tempBuf.append(","+rhsArgs.get(2));
					}
					if (fcg.getMatrixValue(rhsArgs.get(1)).hasConstant() 
							&& fcg.tempVarsBeforeF.contains(rhsArgs.get(1))) {
						DoubleConstant c = (DoubleConstant) fcg.getMatrixValue(rhsArgs.get(1))
								.getConstant();
						int ci = c.getValue().intValue();
						tempBuf.append(","+ci+"\n");
					}
					else {
						tempBuf.append(","+rhsArgs.get(1)+"\n");
					}
					tempBuf.append(fcg.indent+fcg.indent+cellLHSTarget+"(1,tmp_"+lhsTarget
							+"_index) = tmp_"+lhsTarget+"_i;\n");
					tempBuf.append(fcg.indent+fcg.indent+"tmp_"+lhsTarget+"_index = tmp_"
							+lhsTarget+"_index+1;\n");
					tempBuf.append(indent+"ENDDO");
					BasicMatrixValue tmp = new BasicMatrixValue(null, PrimitiveClassReference.INT32, 
							(new ShapeFactory()).getScalarShape(), null);
					fcg.tempVarsFortran.put("tmp_"+lhsTarget+"_i", tmp);
					fcg.tempVarsFortran.put("tmp_"+lhsTarget+"_index", tmp);
				}
				else {
					// TODO this should be an error, throw an exception?
				}
				tempBuf.append("\n"+indent+"!mapping function "+rhsFunName
						+" is over.");
				noDirBuiltinExpr.setCodeInline(tempBuf.toString());
			}
		}		
		else if (rhsFunName.equals("randperm")) {
			/*
			 * a=randperm(6) will get a=[1,4,3,6,5,2],
			 */
			if (numOfArgs==1) {
				if (fcg.getMatrixValue(rhsArgs.get(0)).hasConstant()) {
					DoubleConstant c = (DoubleConstant) fcg.getMatrixValue(rhsArgs.get(0))
							.getConstant();
					int ci = c.getValue().intValue();
					tempBuf.append(indent+"call randperm("+ci+","+cellLHSTarget+")");
				}
				else {
					tempBuf.append(indent+"call randperm("+rhsArgs.get(0)+","+cellLHSTarget+")");
				}
			}
			else if (numOfArgs==2) {
				// TODO
			}
			else {
				// TODO this should be an error, throw an exception?
			}
			tempBuf.append("\n"+indent+"!mapping function "+rhsFunName
					+" is over.");
			noDirBuiltinExpr.setCodeInline(tempBuf.toString());
		}		
		else if (rhsFunName.equals("rand")) {
			// TODO what is people implement a rand function in MATLAB? 
			noDirBuiltinExpr.setCodeInline(indent+"call random_number("+cellLHSTarget+");");
		}
		else if (rhsFunName.equals("cellhorzcat")) {
			ArrayList<BasicMatrixValue> fields = new ArrayList<BasicMatrixValue>();
			for (int i=0; i<rhsArgs.size(); i++) {
				fields.add(fcg.getMatrixValue(rhsArgs.get(i)));				
			}
			fcg.forCellArr.put(lhsTarget, fields);
			for (int i=0; i<rhsArgs.size(); i++) {
				tempBuf.append(indent+lhsTarget+"%f"+i+" = ");
				if (fcg.getMatrixValue(rhsArgs.get(i)).hasConstant() 
						&& fcg.tempVarsBeforeF.contains(rhsArgs.get(i))) {
					Constant c = fcg.getMatrixValue(rhsArgs.get(i))
							.getConstant();
					tempBuf.append(c+";");
				}
				else tempBuf.append(rhsArgs.get(i)+";");
				if (i<rhsArgs.size()-1) tempBuf.append("\n");
			}
			noDirBuiltinExpr.setCodeInline(tempBuf.toString());
		}
		else {
			/*
			 * for those no direct builtins which have not been implemented yet.
			 */
			noDirBuiltinExpr.setCodeInline("!  the built-in function \""+rhsFunName
					+"\" has not been implemented yet, fix it!");
		}
		return noDirBuiltinExpr;
	}
}
