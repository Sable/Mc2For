package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.shape.*;
import natlab.tame.valueanalysis.components.constant.*;

public class HandleCaseTIRArrayGetStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRArrayGetStmt(){
		
	}
	/**
	 * ArrayGetStmt: Statement ::= <lhsVariable> [lhsIndex] <rhsVariable> <rhsIndex>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRArrayGetStmt node){
		if (Debug) System.out.println("in an arrayget statement!");
		
		ArrayGetStmt stmt = new ArrayGetStmt();
		String indent = new String();
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		
		ArrayList<String> args = new ArrayList<String>();
		args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
		
		//TODO I need know the array's shape and index' shape!
		String lhsVariable = node.getLHS().getNodeString().replace("[", "").replace("]", "");
		String lhsIndexString;
		String rhsArrayName = node.getArrayName().getVarName();
		
		Shape arrayShape = ((HasShape)(fcg.analysis.getNodeList()
		.get(fcg.index).getAnalysis().getCurrentOutSet().get(rhsArrayName).getSingleton())).getShape();
		ArrayList<Integer> dimension = new ArrayList<Integer>(arrayShape.getDimensions());
		
		if(args.size()==1){
			Shape indexShape = ((HasShape)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(0)).getSingleton())).getShape();
			if(args.contains(":")){
				//TODO
			}
			else if(indexShape.isScalar()){
				Constant index = ((HasConstant)(fcg.analysis.getNodeList()
						.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(0)).getSingleton())).getConstant();
				double doubleIndex = (Double)index.getValue();
				int intIndex = (int)doubleIndex;
				int firstIndex = intIndex/dimension.get(1)+1;
				int secondIndex = intIndex%dimension.get(1);
				stmt.setlhsVariable(lhsVariable);
				stmt.setrhsVariable(rhsArrayName);
				stmt.setrhsIndex(Integer.toString(firstIndex)+","+Integer.toString(secondIndex));
			}
			else{
				//TODO
			}
		}
		else if(args.size()==2){
			for(int i=0;i<2;i++){
				System.out.println(args.get(i));
				if(args.get(i).equals(":")){
					//do nothing
					System.out.println(args.get(i));
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
		
		for(String indexName : args){
			if(indexName.equals(":")){
				//ignore this
			}
			else{
				fcg.arrayIndexParameter.add(indexName);
			}
		}
		return stmt;
	}
}
