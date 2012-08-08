package natlab.backends.Fortran.codegen;

import ast.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


import natlab.tame.classes.reference.ClassReference;
import natlab.tame.tir.*;

import natlab.tame.tir.analysis.TIRAbstractNodeCaseHandler;
import natlab.tame.valueanalysis.ValueAnalysis;
import natlab.tame.valueanalysis.ValueAnalysisPrinter;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.backends.Fortran.codegen.FortranMapping;

public class FortranCodeGenerator extends TIRAbstractNodeCaseHandler{
	ValueAnalysis<AggrValue<BasicMatrixValue>> analysis;
	private StringBuffer buf;
	private StringBuffer buf2;
	private FortranMapping FortranMap;
	private HashMap<String, Collection<ClassReference>> symbolMap = new HashMap<String, Collection<ClassReference>>(); 
	private String symbolMapKey;
	private ArrayList<String> forStmtParameter;
	static boolean Debug = true;
	
	private FortranCodeGenerator(ValueAnalysis<AggrValue<BasicMatrixValue>> analysis2, String classname) {
		this.buf = new StringBuffer();
		this.buf2 = new StringBuffer();
		this.FortranMap = new FortranMapping();
		this.analysis = analysis2;
		this.forStmtParameter = new ArrayList<String>();
		//buf.append("public class "+classname+" {\n");
		((TIRNode)analysis2.getNodeList().get(0).getAnalysis().getTree()).tirAnalyze(this);
	}
	
	
	public static String FortranCodePrinter(
			ValueAnalysis<AggrValue<BasicMatrixValue>> analysis2, String classname){
		return new FortranCodeGenerator(analysis2, classname).buf2.toString();
	
	}
	
	
	@Override
	public void caseASTNode(ASTNode node) {
				
	}
	public void caseTIRFunction(TIRFunction node){
		String indent = node.getIndent();
		boolean first = true;
		ArrayList<String> inArgs = new ArrayList<String>();
		//buf.append(indent + "public static def " );
		printStatements(node.getStmts());
		//Write code for nested functions here
		//buf.append(indent + "}//end of function\n}//end of class\n");
		buf.append(indent + "      stop\n      end");
		
		if (Debug) System.out.println("the parameters in for stmt: "+forStmtParameter);
		
		buf2.append(indent + "      program ");
		// TODO - CHANGE IT TO DETERMINE RETURN TYPE		
		buf2.append(node.getName());
		buf2.append("\n      implicit none");
		/*buf.append("(");
		first = true;
		for(Name param : node.getInputParams()) {
			if(!first) {
				buf.append(", ");
			}
			buf.append(param.getPrettyPrinted()+": "+FortranMap.getFortranTypeMapping(getArgumentType(analysis, node, param.getID())) );
			symbolMap.put(param.getID().toString(), getAnalysisValue(analysis, node, param.getID()));
			first = false;
		}*/
		
		//System.out.println(this.analysis.getNodeList().get(0).getAnalysis().getOutFlowSets());
		if (Debug) System.out.println(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().keySet()+"\n");
		
		for(String variable : this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().keySet()){
			if(forStmtParameter.contains(variable)){
				if (Debug) System.out.println("variable "+variable+" is a for stmt parameter.");
				if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable));
				
				//complex or not others, like real, integer or something else
				/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
					if (Debug) System.out.println("COMPLEX here!");
					buf.append("\n      complex");
				}
				else{
					buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				}*/
				buf2.append("\n      " + FortranMap.getFortranTypeMapping("int8"));
				//parameter
				if(((BasicMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
					if (Debug) System.out.println("add parameter here!");
					buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
				}
				else{
					buf2.append(" :: " + variable);
				}
			}
			else{
				if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable));
				
				//complex or not others, like real, integer or something else
				/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
					if (Debug) System.out.println("COMPLEX here!");
					buf.append("\n      complex");
				}
				else{
					buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				}*/
				buf2.append("\n      " + FortranMap.getFortranTypeMapping(((BasicMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				//parameter
				if(((BasicMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
					if (Debug) System.out.println("add parameter here!");
					buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
				}
				else{
					//dimension
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().isScalar()==false){
						if (Debug) System.out.println("add dimension here!");
						buf2.append(" , dimension(");
						ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().getDimensions());
						if (Debug) System.out.println(dim);
						boolean cont = false;
						for(Integer inte : dim){
							if(cont){
								buf2.append(",");
							}
							buf2.append("1:" + inte.toString());
							cont = true;
						}
						buf2.append(")");
						buf2.append(" :: " + variable);
					}
					else{
						buf2.append(" :: " + variable);
					}
				}
			}
		}
		buf2.append("\n");
		buf2.append(buf);
	}
	
	@Override
	public void caseTIRAbstractAssignStmt(TIRAbstractAssignStmt node) {
	   
		if (node instanceof TIRAbstractAssignToVarStmt){
			handleTIRAbstractAssignToVarStmt(node);
		}
		else if (node instanceof TIRAbstractAssignToListStmt){
		//	for(ast.Name name : ((TIRAbstractAssignToListStmt)node).getTargets().asNameList()){
		//		vars.add(name.getID());
			handleTIRAbstractAssignToListStmt(node);		
		}
					
		//TODO implement other cases here - refer to ValueAnalysisPrinter
		/*
		else if (node instanceof TIRAbstractAssignToListStmt){
			for(ast.Name name : ((TIRAbstractAssignToListStmt)node).getTargets().asNameList()){
				vars.add(name.getID());				
			}
		} else if (node instanceof TIRArraySetStmt){
			vars.add(((TIRArraySetStmt)node).getArrayName().getID());
		} else if (node instanceof TIRCellArraySetStmt){
			vars.add(((TIRCellArraySetStmt)node).getCellArrayName().getID());
		} else if (node instanceof TIRDotSetStmt){
			vars.add(((TIRDotSetStmt)node).getDotName().getID());
		};
		*/
		//printVars(analysis.getOutFlowSets().get(node), vars);
	}
	
	public void handleTIRAbstractAssignToListStmt(TIRAbstractAssignStmt node){
		
		
		String LHS;
		
		ArrayList<String> vars = new ArrayList<String>();
		for(ast.Name name : ((TIRAbstractAssignToListStmt)node).getTargets().asNameList()){
		 vars.add(name.getID());
		}
		
		if (1==vars.size()){ //only one variable on LHS
			symbolMapKey = vars.get(0);
			LHS = symbolMapKey;
			
			if(true == symbolMap.containsKey(symbolMapKey)) //variable already defined and analyzed
			{
				//buf.append(((TIRAbstractAssignToVarStmt)node).getPrettyPrintedLessComments());
				buf.append("      "+LHS + " = ");
			}
			else
			{
				buf.append("      "+LHS.toString()+" = ");
				//use varname to get the name of the method/operator/Var
			}
			    makeExpression(node);
				symbolMap.put(node.getLHS().getNodeString(), getAnalysisValue(analysis, node,LHS));
			
			
			
		}
		else if(0==vars.size()){
			//TODO
			  makeExpression(node);
		}
		
		
	}
	
	
	
	public void makeExpression(TIRAbstractAssignStmt node)
	{
		/* Change for built-ins with n args
		 * Currently it handles general case built-ins with one or two args only
		 */
		
		int RHStype;
		String RHS;
		String Operand1, Operand2, prefix="";
		String ArgsListasString;
		RHStype = getRHSType(node);
		RHS = getRHSExp(node);
		//TODO
		
		Operand1 = getOperand1(node);
		Operand2 = getOperand2(node);
		
		ArrayList<String> Args = new ArrayList<String>();
		
		
		if (Operand2 != "" && Operand2 != null)
			prefix = ", ";
		switch(RHStype)
		{
		case 1:
			buf.append(Operand1+" "+RHS+" "+Operand2+" ;");
			break;
		case 2:
			buf.append(RHS+""+Operand1+" ;"); //TODO test this
			break;
		case 3:
			Args = GetArgs(node);
			ArgsListasString = GetArgsListasString(Args);
			buf.append(RHS+"("+ArgsListasString+");");
			//buf.append("      " + RHS + ArgsListasString);
			break;
		case 4:
			Args = GetArgs(node);
			ArgsListasString = GetArgsListasString(Args);
			buf.append(RHS+"("+ArgsListasString+");");
			break;
		case 5:
			buf.append(RHS+";");
			break;
		case 6:
			Args = GetArgs(node);
			ArgsListasString = GetArgsListasString(Args);
			buf.append("      "+RHS+ArgsListasString);
			break;
		default:
			buf.append("//is it an error?");	
			break;
		}
	}
	
	
	public String getOperand1(TIRAbstractAssignStmt node){
		if(node.getRHS().getChild(1).getNumChild() >= 1)
			return node.getRHS().getChild(1).getChild(0).getNodeString();
		else
			return "";
	}
	
	public String getOperand2(TIRAbstractAssignStmt node){
		if(node.getRHS().getChild(1).getNumChild() >= 2)
			return node.getRHS().getChild(1).getChild(1).getNodeString();
		else
			return "";
	}
	
	public int getRHSType(TIRAbstractAssignStmt node){
		String RHSName;
		RHSName = node.getRHS().getVarName();
		if (true==FortranMap.isBinOperator(RHSName))
		{
			return 1; //"binop";
		}
		else if (true==FortranMap.isUnOperator(RHSName))
		{
		   return 2; //"unop";
		}
		
		else if (true==FortranMap.isFortranDirectBuiltin(RHSName))
		{
			return 3; // "builtin";
		}
		else if (true == FortranMap.isMethod(RHSName))
		{
			return 4; // "method";
		}
		else if (true==FortranMap.isBuiltinConst(RHSName))
		{
			return 5; // "builtin";
		}
		else if (true==FortranMap.isIOOperation(RHSName))
		{
			return 6; // "IO OPeration";
		}
		else
		{
			return 0; // "default";
		}
	}
	
	public String getRHSExp(TIRAbstractAssignStmt node){
		String RHS = null;
		String RHSName;
		RHSName = node.getRHS().getVarName();
		if (true==FortranMap.isBinOperator(RHSName))
		{
			RHS= FortranMap.getFortranBinOpMapping(RHSName);
		}
		else if (true==FortranMap.isUnOperator(RHSName))
		{
		   RHS= FortranMap.getFortranUnOpMapping(RHSName);
		}
		
		else if (true==FortranMap.isFortranDirectBuiltin(RHSName))
		{
			RHS= FortranMap.getFortranDirectBuiltinMapping(RHSName);
		}
		else if (true==FortranMap.isBuiltinConst(RHSName))
		{
			RHS= FortranMap.getFortranBuiltinConstMapping(RHSName);
		}
		else if (true == FortranMap.isMethod(RHSName))
		{
			RHS= FortranMap.getFortranMethodMapping(RHSName);
		}
		else if (true == FortranMap.isIOOperation(RHSName))
		{
			RHS= FortranMap.getFortranIOOperationMapping(RHSName);
		}
		else
		{
			RHS = "//cannot process it yet";
		}
		return RHS;
	}
	
	
	public void handleTIRAbstractAssignToVarStmt(TIRAbstractAssignStmt node){
		//vars.add(((TIRAbstractAssignToVarStmt)node).getTargetName().getID());
		//if already present in symbolMap=>has been analyzed else define
		String LHS;
		//ArrayList<String> vars = new ArrayList<String>();
		symbolMapKey = ((TIRAbstractAssignToVarStmt)node).getTargetName().getID();
		LHS = symbolMapKey;
		if(((BasicMatrixValue)(this.analysis.getNodeList().get(0).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).isConstant()){
			System.out.println(LHS+" is a constant");
		}
		else{
			if(true == symbolMap.containsKey(symbolMapKey)) //variable already defined and analyzed
			{
				buf.append(((TIRAbstractAssignToVarStmt)node).getPrettyPrintedLessComments());
			}
			else 
			{   
				
				String type = FortranMap.getFortranTypeMapping(getLHSType(analysis,node,LHS ));
				buf.append("      "+node.getLHS().getNodeString()+" = ");
				if (type == "String")
				{
					//type = makeFortranStringLiteral(type);
					buf.append(makeFortranStringLiteral(node.getRHS().getNodeString()) + ";");
				}
				else
				buf.append(node.getRHS().getNodeString() + ";");
				//TODO check for expression on RHS
				//TODO check for built-ins
				//TODO check for operators
				//add to symbol Map
				symbolMap.put(node.getLHS().getNodeString(), getAnalysisValue(analysis, node,LHS));
				
				
			}
		}
	}
		
	ArrayList<String> GetArgs(TIRAbstractAssignStmt node)
	{
		ArrayList<String> Args = new ArrayList<String>();
		int numArgs = node.getRHS().getChild(1).getNumChild();
		for (int i=0;i<numArgs;i++)
		{
			Args.add(node.getRHS().getChild(1).getChild(i).getNodeString());
		}
		
		return Args;
	}
	
	
	String GetArgsListasString(ArrayList<String> Args)
	{
	   String prefix ="";
	   String ArgListasString="";
	   for (String arg : Args)
	   {
		   ArgListasString = ArgListasString+prefix+arg;
		   prefix=", ";
	   }
	   return ArgListasString;
	}
	
	public void caseTIRIfStmt(TIRIfStmt node){
		System.out.println("in if statement.");
		System.out.println(node.getConditionVarName().getID());
		buf.append("      if ("+node.getConditionVarName().getID()+") then\n");
		printStatements(node.getIfStameents());
		buf.append("      else\n");
		printStatements(node.getElseStatements());
		buf.append("      endif");
		return;
	}
	
	public void caseTIRWhileStmt(TIRWhileStmt node){
		System.out.println("in while statement.");
		System.out.println(node.getCondition().getVarName());
		buf.append("      do while ("+node.getCondition().getVarName()+")\n");
		printStatements(node.getStatements());
		buf.append("      enddo");
		return;
	}
	//TODO
	public void caseTIRForStmt(TIRForStmt node){
		System.out.println("in for statement.");
		System.out.println(node.getLoopVarName().getVarName());
		buf.append("      do "+node.getLoopVarName().getVarName()+" = "+node.getLowerName().getVarName()+" , "+node.getUpperName().getVarName()+"\n");
		printStatements(node.getStatements());
		buf.append("      enddo");
		forStmtParameter.add(node.getLoopVarName().getVarName());
		forStmtParameter.add(node.getLowerName().getVarName());
		forStmtParameter.add(node.getUpperName().getVarName());
	}
	/**********************HELPER METHODS***********************************/
	private String getLHSType(ValueAnalysis<?> analysis,
			TIRAbstractAssignStmt node, String SymbolMapKey) {
		//node.getTargetName().getID()
		return analysis.getNodeList().get(0).getAnalysis().getOutFlowSets().get(node).get(SymbolMapKey).getMatlabClasses().toArray()[0].toString();
		
	}


	
	private static String getArgumentType(ValueAnalysis<?> analysis, TIRFunction node, String paramID){
		//System.out.println(analysis.getOutFlowSets().get(node).get(paramID).toString());

		return analysis.getNodeList().get(0).getAnalysis().getOutFlowSets().get(node).get(paramID).getMatlabClasses().toArray()[0].toString();
	}
	
	//get analysis value for Function node
	private static Collection<ClassReference> getAnalysisValue(ValueAnalysis<?> analysis, TIRFunction node, String ID){
		return analysis.getNodeList().get(0).getAnalysis().getOutFlowSets().get(node).get(ID).getMatlabClasses();

		//return analysis.getOutFlowSets().get(node).get(paramID).getMatlabClasses().toArray()[0].toString();
	}
	
	
	//get analysis value for abstract assignment node
	private static Collection<ClassReference> getAnalysisValue(ValueAnalysis<?> analysis, TIRAbstractAssignStmt node, String ID){
		return analysis.getNodeList().get(0).getAnalysis().getOutFlowSets().get(node).get(ID).getMatlabClasses();

		//return analysis.getOutFlowSets().get(node).get(paramID).getMatlabClasses().toArray()[0].toString();
	}

	private String makeFortranStringLiteral(String StrLit)
	{
		
		if(StrLit.charAt(0)=='\'' && StrLit.charAt(StrLit.length()-1)=='\'')
		{
			
			return "\""+(String) StrLit.subSequence(1, StrLit.length()-1)+"\"";
			
		}
		else
		return StrLit;
	}
	
	
	private void printStatements(ast.List<ast.Stmt> stmts){
		for(ast.Stmt stmt : stmts) {
			int length = buf.length();
			((TIRNode)stmt).tirAnalyze(this);
			if (buf.length() > length) buf.append('\n');
		}
	}
	
}


