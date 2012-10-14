package natlab.backends.Fortran.codegen;

import java.util.ArrayList;

import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.tir.TIRAbstractAssignToListStmt;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.shape.ShapeFactory;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.backends.Fortran.codegen.ASTcaseHandler.*;

public class FortranCodeASTInliner {

	public FortranCodeASTInliner(){
		
	}
	
	public static NoDirectBuiltinExpr inline(FortranCodeASTGenerator fcg, TIRAbstractAssignToListStmt node){
		NoDirectBuiltinExpr noDirBuiltinExpr = new NoDirectBuiltinExpr();
		
		if(node.getRHS().getVarName().equals("horzcat")){
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
			int argsNum = args.size();
			StringBuffer tmpBuf = new StringBuffer();
			for(int i=1; i<=argsNum; i++){
				tmpBuf.append(LHS+"(1,"+i+") = "+args.get(i-1)+";");
				if(i<argsNum){
					tmpBuf.append("\n");	
				}
			}
			noDirBuiltinExpr.setCodeInline(tmpBuf.toString());
		}
		else if(node.getRHS().getVarName().equals("vertcat")){
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
			int argsNum = args.size();
			StringBuffer tmpBuf = new StringBuffer();
			for(int i=1; i<=argsNum; i++){
				tmpBuf.append(LHS+"("+i+",:) = "+args.get(i-1)+"(1,:);");
				if(i<argsNum){
					tmpBuf.append("\n");
				}
			}
			noDirBuiltinExpr.setCodeInline(tmpBuf.toString());
		}
		else if(node.getRHS().getVarName().equals("ones")){
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
			int argsNum = args.size();
			double secondDimensionDbl = (Double)((BasicMatrixValue)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(1)).getSingleton())).getConstant().getValue();
			int secondDimensionInt = (int) secondDimensionDbl;

			StringBuffer tmpBuf = new StringBuffer();
			for(int i=1; i<=secondDimensionInt; i++){
				tmpBuf.append("mc_tmp_"+LHS+"(1,"+i+") = 1;");
				if(i<secondDimensionInt){
					tmpBuf.append("\n");
				}
			}
			tmpBuf.append("\n");
			double firstDimensionDbl = (Double)((BasicMatrixValue)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(0)).getSingleton())).getConstant().getValue();
			int firstDimensionInt = (int) firstDimensionDbl;
			for(int i=1; i<=firstDimensionInt; i++){
				tmpBuf.append(LHS+"("+i+",:) = "+"mc_tmp_"+LHS+"(1,:);");
				if(i<firstDimensionInt){
					tmpBuf.append("\n");
				}
			}
			ArrayList<Integer> shape = new ArrayList<Integer>();
			shape.add(1);
			shape.add(secondDimensionInt);
			BasicMatrixValue tmp = 
					new BasicMatrixValue(PrimitiveClassReference.DOUBLE,(new ShapeFactory()).newShapeFromIntegers(shape));
			fcg.tmpVariables.put("mc_tmp_"+LHS, tmp);
			noDirBuiltinExpr.setCodeInline(tmpBuf.toString());
		}
		else if(node.getRHS().getVarName().equals("zeros")){
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
			int argsNum = args.size();
			double secondDimensionDbl = (Double)((BasicMatrixValue)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(1)).getSingleton())).getConstant().getValue();
			int secondDimensionInt = (int) secondDimensionDbl;

			StringBuffer tmpBuf = new StringBuffer();
			for(int i=1; i<=secondDimensionInt; i++){
				tmpBuf.append("mc_tmp_"+LHS+"(1,"+i+") = 0;");
				if(i<secondDimensionInt){
					tmpBuf.append("\n");
				}
			}
			tmpBuf.append("\n");
			double firstDimensionDbl = (Double)((BasicMatrixValue)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(args.get(0)).getSingleton())).getConstant().getValue();
			int firstDimensionInt = (int) firstDimensionDbl;
			for(int i=1; i<=firstDimensionInt; i++){
				tmpBuf.append(LHS+"("+i+",:) = "+"mc_tmp_"+LHS+"(1,:);");
				if(i<firstDimensionInt){
					tmpBuf.append("\n");
				}
			}
			ArrayList<Integer> shape = new ArrayList<Integer>();
			shape.add(1);
			shape.add(secondDimensionInt);
			BasicMatrixValue tmp = 
					new BasicMatrixValue(PrimitiveClassReference.DOUBLE,(new ShapeFactory()).newShapeFromIntegers(shape));
			fcg.tmpVariables.put("mc_tmp_"+LHS, tmp);
			noDirBuiltinExpr.setCodeInline(tmpBuf.toString());
		}
		return noDirBuiltinExpr;
	}
}
