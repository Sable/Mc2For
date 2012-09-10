package natlab.backends.Fortran.codegen;

import java.util.ArrayList;

import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.tir.TIRAbstractAssignToListStmt;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.shape.ShapeFactory;
import natlab.tame.valueanalysis.constant.*;

public class FortranCodeInliner {

	public FortranCodeInliner(){
		
	}
	
	public FortranCodePrettyPrinter inline(FortranCodePrettyPrinter fcg, TIRAbstractAssignToListStmt node){
		if(node.getRHS().getVarName().equals("horzcat")){
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = fcg.getArgsList(node);
			int argsNum = args.size();
			for(int i=1; i<=argsNum; i++){
				fcg.buf.append(LHS+"(1,"+i+") = "+args.get(i-1)+";");
				if(i<argsNum){
					fcg.buf.append("\n");	
				}
			}
		}
		else if(node.getRHS().getVarName().equals("vertcat")){
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = fcg.getArgsList(node);
			int argsNum = args.size();
			for(int i=1; i<=argsNum; i++){
				fcg.buf.append(LHS+"("+i+",:) = "+args.get(i-1)+"(1,:);");
				if(i<argsNum){
					fcg.buf.append("\n");
				}
			}
		}
		else if(node.getRHS().getVarName().equals("ones")){
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = fcg.getArgsList(node);
			int argsNum = args.size();
			double secondDimensionDbl = (Double)((BasicMatrixValue)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(1)).getSingleton())).getConstant().getValue();
			int secondDimensionInt = (int) secondDimensionDbl;
			for(int i=1; i<=secondDimensionInt; i++){
				fcg.buf.append(LHS+"_columnTmp(1,"+i+") = 1;");
				if(i<secondDimensionInt){
					fcg.buf.append("\n");
				}
			}
			fcg.buf.append("\n");
			double firstDimensionDbl = (Double)((BasicMatrixValue)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(0)).getSingleton())).getConstant().getValue();
			int firstDimensionInt = (int) firstDimensionDbl;
			for(int i=1; i<=firstDimensionInt; i++){
				fcg.buf.append(LHS+"("+i+",:) = "+LHS+"_columnTmp(1,:);");
				if(i<firstDimensionInt){
					fcg.buf.append("\n");
				}
			}
			ArrayList<Integer> shape = new ArrayList<Integer>();
			shape.add(1);
			shape.add(secondDimensionInt);
			BasicMatrixValue tmp = 
					new BasicMatrixValue(new BasicMatrixValue(PrimitiveClassReference.DOUBLE),(new ShapeFactory()).newShapeFromIntegers(shape));
			fcg.tmpVariables.put(LHS+"_columnTmp", tmp);
		}
		return fcg;
	}
}
