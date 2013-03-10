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
	 * ArrayGetStmt: Statement ::= [RuntimeCheck] [ArrayConvert] <lhsVariable> [lhsIndex] <rhsVariable> <rhsIndex>;
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
				}
				lhsVariable = lhsVariable+"_copy";
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
		//TODO re-implement!
		
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
