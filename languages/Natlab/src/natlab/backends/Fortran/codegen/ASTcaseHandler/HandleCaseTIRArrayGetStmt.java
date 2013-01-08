package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.shape.*;
import natlab.tame.valueanalysis.components.constant.*;

public class HandleCaseTIRArrayGetStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRArrayGetStmt(){
		
	}
	/**
	 * ArrayGetStmt: Statement ::= [RuntimeCheck] [BackupVar] [ArrayConvert] <lhsVariable> [lhsIndex] <rhsVariable> <rhsIndex>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRArrayGetStmt node){
		if (Debug) System.out.println("in an arrayget statement!");
		
		ArrayGetStmt stmt = new ArrayGetStmt();
		String indent = new String();
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		
		String lhsVariable = node.getLHS().getNodeString().replace("[", "").replace("]", "");
		if(fcg.isSubroutine==true){
			/**
			 * if input argument on the LHS of assignment stmt, we assume that this input argument maybe modified.
			 */
			if(fcg.inArgs.contains(lhsVariable)){
				if (Debug) System.out.println("subroutine's input "+lhsVariable+" has been modified!");
				/**
				 * here we need to detect whether it is the first time this variable put in the set,
				 * because we only want to back up them once.
				 */
				if(fcg.inputHasChanged.contains(lhsVariable)){
					//do nothing.
					if (Debug) System.out.println("encounter "+lhsVariable+" again.");
				}
				else{
					if (Debug) System.out.println("first time encounter "+lhsVariable);
					fcg.inputHasChanged.add(lhsVariable);
					BackupVar backupVar = new BackupVar();
					backupVar.setBlock(lhsVariable+"_backup = "+lhsVariable+";\n");
					stmt.setBackupVar(backupVar);
				}
				lhsVariable = lhsVariable+"_backup";
			}
			else{
				//do nothing
			}
		}
		else{
			//do nothing
		}
		/**
		 * at least, we need the information of rhs array's shape and its corresponding index's shape (maybe value).
		 */
		String rhsArrayName = node.getArrayName().getVarName();
		Shape rhsArrayShape = ((HasShape)(fcg.analysis.getNodeList().get(fcg.index)
				.getAnalysis().getCurrentOutSet().get(rhsArrayName).getSingleton())).getShape();
		ArrayList<Integer> rhsArrayDimension = new ArrayList<Integer>(rhsArrayShape.getDimensions());
		/**
		 * args is index as ArrayList
		 */
		ArrayList<String> args = new ArrayList<String>();
		args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
		/**
		 * TODO currently, only support array has at most two dimensions and the number of index can be one or two, fix this later.
		 * matrix indexing in Matlab is so powerful and flexible, but this cause mapping matrix indexing in Matlab to Fortran is so complicated!!!
		 */
		/**
		 * i.e. b = a(:), if a is a vector, this case is the simplest one. If a is a multi-dimensional array, this case will be complicated.
		 * In Matlab, the interpreter can interpret it, while in Fortran the array assignmnet should be conformable.
		 * for example, a is 2 by 3 array, and b is 1 by 3 array, when we do "b=a(1:3)" in Matlab, it's okay (remember Matlab and Fortran is column major),
		 * b will be assigned with the first three entries of a. But in Fortran, the compiler will throw errors about this,
		 * we should modify the assignment to "b(1,1)=a(1,1);b(1,2)=a(2,1);b(1,3)=a(1,2)" (because a is 2 by 3 and b is 1 by 3).
		 * but this still remains a big problem, what if b = a(1:10000), the inlined code will be super long and disgusting...
		 * so currently, my solution is to use a temporary array as a intermediate array to achieve this array get assignment.
		 * i.e.
		 * b = a(1:3)
		 * --->
		 * do tmp_a_column = 1,3
         *    do tmp_a_row = 1,2
         *       a_vector(1,(tmp_a_column-1)*2+tmp_a_row)=a(tmp_a_row,tmp_a_column);
         *    enddo
         * enddo
         * b(1,1:3) = a_vector(1,1:3); 
		 */
		if(rhsArrayDimension.size()==1){
			/**
			 * this cannot happen, because in our inference system, we assume that every array has at least two dimensions,
			 * we make this assumption based on Matlab conventions.
			 */
		}
		else if(rhsArrayDimension.size()==2){
			if(args.size()==1){
				/**
				 * there are two cases,
				 * 1. the index is ":", in Matlab, it means all the entries or elements in this array;
				 * 2. a variable, this variable also has two possibilities: first, it's a scalar; second, it's a vector, like 1:3.
				 */
				if(args.contains(":")){
					// b = a(:)  ---> b = transpose(a_vector) brilliant!
					if(fcg.arrayConvert.contains(rhsArrayName)){
						if (Debug) System.out.println(rhsArrayName+" has already been converted.");
					}
					else{
						if (Debug) System.out.println("rhs array "+rhsArrayName+"'s index is not conformable, need convert.");
						String currentIndent = "";
						for(int i=0;i<fcg.indentNum;i++){
							currentIndent = currentIndent+fcg.indent;
						}
						ArrayConvert arrayConvert = new ArrayConvert();
						StringBuffer arrayConvertBF = new StringBuffer();
						arrayConvertBF.append(currentIndent+"do tmp_"+rhsArrayName+"_column = 1,"+rhsArrayDimension.get(1)+"\n");
						arrayConvertBF.append(currentIndent+fcg.indent+"do tmp_"+rhsArrayName+"_row = 1,"+rhsArrayDimension.get(0)+"\n");
						arrayConvertBF.append(currentIndent+fcg.indent+fcg.indent+rhsArrayName+"_vector(1,(tmp_"+rhsArrayName+"_column-1)*"
								+rhsArrayDimension.get(0)+"+tmp_"+rhsArrayName+"_row)="+rhsArrayName+"(tmp_"+rhsArrayName+"_row,tmp_"+rhsArrayName+"_column);\n");
						arrayConvertBF.append(currentIndent+fcg.indent+"enddo\n");
						arrayConvertBF.append(currentIndent+"enddo\n");
						arrayConvert.setBlock(arrayConvertBF.toString());
						stmt.setArrayConvert(arrayConvert);
						/**
						 * store those temporary variables.
						 */
						ArrayList<Integer> shape1 = new ArrayList<Integer>();
						shape1.add(1);
						shape1.add(1);
						BasicMatrixValue tmp1 = 
								new BasicMatrixValue(PrimitiveClassReference.INT8,(new ShapeFactory()).newShapeFromIntegers(shape1));
						fcg.tmpVariables.put("tmp_"+rhsArrayName+"_column", tmp1);
						fcg.tmpVariables.put("tmp_"+rhsArrayName+"_row", tmp1);
						ArrayList<Integer> shape2 = new ArrayList<Integer>();
						shape2.add(1);
						shape2.add(rhsArrayDimension.get(0)*rhsArrayDimension.get(1));
						BasicMatrixValue tmp2 = 
								new BasicMatrixValue(PrimitiveClassReference.DOUBLE,(new ShapeFactory()).newShapeFromIntegers(shape2));
						fcg.tmpVariables.put(rhsArrayName+"_vector", tmp2);
						/**
						 * put rhs array's name in arrayConvert HashSet, avoid inlining convert code again.
						 */
						fcg.arrayConvert.add(rhsArrayName);
					}
					stmt.setlhsVariable(lhsVariable);
					stmt.setrhsVariable("transpose");
					stmt.setrhsIndex(rhsArrayName+"_vector");
				}
				else{
					Shape indexShape = ((HasShape)(fcg.analysis.getNodeList()
							.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(0)).getSingleton())).getShape();
					ArrayList<Integer> indexDimension = new ArrayList<Integer>(indexShape.getDimensions());
					
					if(indexShape.isScalar()){
						Constant index = ((HasConstant)(fcg.analysis.getNodeList()
								.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(0)).getSingleton())).getConstant();
						if(index!=null){
							double doubleIndex = (Double)index.getValue();
							int intIndex = (int)doubleIndex;
							if(fcg.arrayConvert.contains(rhsArrayName)){
								if (Debug) System.out.println(rhsArrayName+" has already been converted.");
							}
							else{
								if (Debug) System.out.println("rhs array "+rhsArrayName+"'s index is not conformable, need convert.");
								String currentIndent = "";
								for(int i=0;i<fcg.indentNum;i++){
									currentIndent = currentIndent+fcg.indent;
								}
								ArrayConvert arrayConvert = new ArrayConvert();
								StringBuffer arrayConvertBF = new StringBuffer();
								arrayConvertBF.append(currentIndent+"do tmp_"+rhsArrayName+"_column = 1,"+rhsArrayDimension.get(1)+"\n");
								arrayConvertBF.append(currentIndent+fcg.indent+"do tmp_"+rhsArrayName+"_row = 1,"+rhsArrayDimension.get(0)+"\n");
								arrayConvertBF.append(currentIndent+fcg.indent+fcg.indent+rhsArrayName+"_vector(1,(tmp_"+rhsArrayName+"_column-1)*"
										+rhsArrayDimension.get(0)+"+tmp_"+rhsArrayName+"_row)="+rhsArrayName+"(tmp_"+rhsArrayName+"_row,tmp_"+rhsArrayName+"_column);\n");
								arrayConvertBF.append(currentIndent+fcg.indent+"enddo\n");
								arrayConvertBF.append(currentIndent+"enddo\n");
								arrayConvert.setBlock(arrayConvertBF.toString());
								stmt.setArrayConvert(arrayConvert);
								/**
								 * store those temporary variables.
								 */								
								ArrayList<Integer> shape1 = new ArrayList<Integer>();
								shape1.add(1);
								shape1.add(1);
								BasicMatrixValue tmp1 = 
										new BasicMatrixValue(PrimitiveClassReference.INT8,(new ShapeFactory()).newShapeFromIntegers(shape1));
								fcg.tmpVariables.put("tmp_"+rhsArrayName+"_column", tmp1);
								fcg.tmpVariables.put("tmp_"+rhsArrayName+"_row", tmp1);
								ArrayList<Integer> shape2 = new ArrayList<Integer>();
								shape2.add(1);
								shape2.add(rhsArrayDimension.get(0)*rhsArrayDimension.get(1));
								BasicMatrixValue tmp2 = 
										new BasicMatrixValue(PrimitiveClassReference.DOUBLE,(new ShapeFactory()).newShapeFromIntegers(shape2));
								fcg.tmpVariables.put(rhsArrayName+"_vector", tmp2);
								/**
								 * put rhs array's name in arrayConvert HashSet, avoid inlining convert code again.
								 */
								fcg.arrayConvert.add(rhsArrayName);								
							}							
							stmt.setlhsVariable(lhsVariable);
							stmt.setrhsVariable(rhsArrayName+"_vector");
							stmt.setrhsIndex("1,"+intIndex);
						}
						else{
							if(fcg.arrayConvert.contains(rhsArrayName)){
								if (Debug) System.out.println(rhsArrayName+" has already been converted.");
							}
							else{
								if (Debug) System.out.println("rhs array "+rhsArrayName+"'s index is not conformable, need convert.");
								String currentIndent = "";
								for(int i=0;i<fcg.indentNum;i++){
									currentIndent = currentIndent+fcg.indent;
								}
								ArrayConvert arrayConvert = new ArrayConvert();
								StringBuffer arrayConvertBF = new StringBuffer();
								arrayConvertBF.append(currentIndent+"do tmp_"+rhsArrayName+"_column = 1,"+rhsArrayDimension.get(1)+"\n");
								arrayConvertBF.append(currentIndent+fcg.indent+"do tmp_"+rhsArrayName+"_row = 1,"+rhsArrayDimension.get(0)+"\n");
								arrayConvertBF.append(currentIndent+fcg.indent+fcg.indent+rhsArrayName+"_vector(1,(tmp_"+rhsArrayName+"_column-1)*"
										+rhsArrayDimension.get(0)+"+tmp_"+rhsArrayName+"_row)="+rhsArrayName+"(tmp_"+rhsArrayName+"_row,tmp_"+rhsArrayName+"_column);\n");
								arrayConvertBF.append(currentIndent+fcg.indent+"enddo\n");
								arrayConvertBF.append(currentIndent+"enddo\n");
								arrayConvert.setBlock(arrayConvertBF.toString());
								stmt.setArrayConvert(arrayConvert);
								/**
								 * store those temporary variables.
								 */								
								ArrayList<Integer> shape1 = new ArrayList<Integer>();
								shape1.add(1);
								shape1.add(1);
								BasicMatrixValue tmp1 = 
										new BasicMatrixValue(PrimitiveClassReference.INT8,(new ShapeFactory()).newShapeFromIntegers(shape1));
								fcg.tmpVariables.put("tmp_"+rhsArrayName+"_column", tmp1);
								fcg.tmpVariables.put("tmp_"+rhsArrayName+"_row", tmp1);
								ArrayList<Integer> shape2 = new ArrayList<Integer>();
								shape2.add(1);
								shape2.add(rhsArrayDimension.get(0)*rhsArrayDimension.get(1));
								BasicMatrixValue tmp2 = 
										new BasicMatrixValue(PrimitiveClassReference.DOUBLE,(new ShapeFactory()).newShapeFromIntegers(shape2));
								fcg.tmpVariables.put(rhsArrayName+"_vector", tmp2);
								/**
								 * put rhs array's name in arrayConvert HashSet, avoid inlining convert code again.
								 */
								fcg.arrayConvert.add(rhsArrayName);								
							}
							stmt.setlhsVariable(lhsVariable);
							stmt.setrhsVariable(rhsArrayName+"_vector");
							stmt.setrhsIndex("1,"+args.get(0));					
						}
					}
					else{
						/**
						 * TODO actually, here we need range information instead of shape information,
						 * i.e. b = a(3:7)
						 */
						if(indexShape.isShapeExactlyKnown()){
							StringBuffer indexAsString = new StringBuffer();
							for(int i=0;i<indexDimension.size();i++){
								indexAsString.append(indexDimension.get(i));
								if(i<indexDimension.size()-1){
									indexAsString.append(":");
								}
							}
							if(fcg.arrayConvert.contains(rhsArrayName)){
								if (Debug) System.out.println(rhsArrayName+" has already been converted.");
							}
							else{
								String currentIndent = "";
								for(int i=0;i<fcg.indentNum;i++){
									currentIndent = currentIndent+fcg.indent;
								}
								ArrayConvert arrayConvert = new ArrayConvert();
								StringBuffer arrayConvertBF = new StringBuffer();
								arrayConvertBF.append(currentIndent+"do tmp_"+rhsArrayName+"_column = 1,"+rhsArrayDimension.get(1)+"\n");
								arrayConvertBF.append(currentIndent+fcg.indent+"do tmp_"+rhsArrayName+"_row = 1,"+rhsArrayDimension.get(0)+"\n");
								arrayConvertBF.append(currentIndent+fcg.indent+fcg.indent+rhsArrayName+"_vector(1,(tmp_"+rhsArrayName+"_column-1)*"
										+rhsArrayDimension.get(0)+"+tmp_"+rhsArrayName+"_row)="+rhsArrayName+"(tmp_"+rhsArrayName+"_row,tmp_"+rhsArrayName+"_column);\n");
								arrayConvertBF.append(currentIndent+fcg.indent+"enddo\n");
								arrayConvertBF.append(currentIndent+"enddo\n");
								arrayConvert.setBlock(arrayConvertBF.toString());
								stmt.setArrayConvert(arrayConvert);
								/**
								 * store those temporary variables.
								 */									
								ArrayList<Integer> shape1 = new ArrayList<Integer>();
								shape1.add(1);
								shape1.add(1);
								BasicMatrixValue tmp1 = 
										new BasicMatrixValue(PrimitiveClassReference.INT8,(new ShapeFactory()).newShapeFromIntegers(shape1));
								fcg.tmpVariables.put("tmp_"+rhsArrayName+"_column", tmp1);
								fcg.tmpVariables.put("tmp_"+rhsArrayName+"_row", tmp1);
								ArrayList<Integer> shape2 = new ArrayList<Integer>();
								shape2.add(1);
								shape2.add(rhsArrayDimension.get(0)*rhsArrayDimension.get(1));
								BasicMatrixValue tmp2 = 
										new BasicMatrixValue(PrimitiveClassReference.DOUBLE,(new ShapeFactory()).newShapeFromIntegers(shape2));
								fcg.tmpVariables.put(rhsArrayName+"_vector", tmp2);
								/**
								 * put rhs array's name in arrayConvert HashSet, avoid inlining convert code again.
								 */
								fcg.arrayConvert.add(rhsArrayName);	
							}
							stmt.setlhsVariable(lhsVariable);
							lhsIndex lhsIndex = new lhsIndex();
							lhsIndex.setName("1,"+indexAsString.toString());
							stmt.setlhsIndex(lhsIndex);
							stmt.setrhsVariable(rhsArrayName+"_vector");
							stmt.setrhsIndex("1,"+indexAsString.toString());
						}
						else{
							//TODO
							stmt.setlhsVariable(lhsVariable);
							stmt.setrhsVariable(rhsArrayName);
							stmt.setrhsIndex(args.get(0));
						}
					}
				}
			}
			else if(args.size()==2){
				//TODO
				for(int i=0;i<args.size();i++){
					if (Debug) System.out.println(args.get(i));
					if(args.get(i).equals(":")){
						//do nothing
					}
					else if(((HasConstant)(fcg.analysis.getNodeList()
							.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(i)).getSingleton())).getConstant()!=null){
						//the index is constant, do nothing
					}
					else if(((HasShape)(fcg.analysis.getNodeList()
							.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(i)).getSingleton())).getShape().isScalar()==false){
						//we know ir translate array(1:2,1:2) to mc_t1=1:2, mc_t2=1:2, array(mc_t1, mc_t2), 
						//but Fortran seems not allow array to be index, so we need to translate the index back.
						Shape index = ((HasShape)(fcg.analysis.getNodeList()
								.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(i)).getSingleton())).getShape();
						ArrayList<Integer> dims = new ArrayList<Integer>(index.getDimensions());
						if(Shape.isDimensionExactlyKnow(dims)){
							String newIndex = "";
							for(int j=0;j<dims.size();j++){
								newIndex = newIndex + dims.get(j).toString();
								if((j+1)!=dims.size()){
									newIndex = newIndex + ":";
								}
							}
							args.remove(i);
							args.add(i, newIndex);
						}
						else{
							/**
							 * insert rumtime check!!!
							 */
							
						}
					}
				}
				if(args.contains(":")){
					/**
					 * for Fortran, the lhs and rhs should be exactly the same shape, so for partial array index assignment,
					 * the lhs should also be explicitly showed the index.
					 * i.e. in MATLAB, you can do this a = b(1:2,:), the interpreter will runtime check the shape of a,
					 * but for Fortran, you should both declare the shape of a explicitly and add the index to the a,
					 * like a(1:2,:)=b(1:2,:).
					 */
					stmt.setlhsVariable(lhsVariable);
					lhsIndex lhsIndex = new lhsIndex();
					String rhsIndexString = args.toString().replace("[", "").replace("]", "");
					lhsIndex.setName(rhsIndexString);
					stmt.setlhsIndex(lhsIndex);
					stmt.setrhsVariable(rhsArrayName);
					stmt.setrhsIndex(rhsIndexString);
				}
				else{			
					stmt.setlhsVariable(lhsVariable);
					stmt.setrhsVariable(rhsArrayName);
					String rhsIndexString = args.toString().replace("[", "").replace("]", "");
					stmt.setrhsIndex(rhsIndexString);
				}
			}
			else{
				//TODO
			}
		}
		else{
			/**
			 * rhs array's dimension is more than two.
			 */
		}
		
		for(ast.Name indexName : node.getIndizes().asNameList()){
			//if(indexName.getID().equals(":")){
			if(indexName==null){
				//ignore this
			}
			else{
				if(indexName.tmpVar){
					//do nothing, already been replaced.
				}
				else{
					fcg.arrayIndexParameter.add(indexName.getID());
				}
			}
		}
		return stmt;
	}
}
