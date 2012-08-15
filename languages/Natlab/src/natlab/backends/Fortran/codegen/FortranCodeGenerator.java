package natlab.backends.Fortran.codegen;

import ast.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
	private HashMap<String, Collection<ClassReference>> symbolMap;
	private String symbolMapKey;
	private ArrayList<String> forStmtParameter;
	private int callgraphSize;
	private int index;
	private String fileDir;
	private String majorName;
	private ArrayList<String> inArgs;
	private ArrayList<String> outRes;
	private HashMap<String, String> funcNameRep;
	private boolean indentIf;
	private boolean indentFW;
	//the key of this hashmap is the user defined function name, and the value is the corresponding substitute variable name.
	static boolean Debug = false;
	
	private FortranCodeGenerator(ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, int callgraphSize, int index, String fileDir) {
		this.buf = new StringBuffer();
		this.buf2 = new StringBuffer();
		this.FortranMap = new FortranMapping();
		this.symbolMap = new HashMap<String, Collection<ClassReference>>();
		this.analysis = analysis;
		this.forStmtParameter = new ArrayList<String>();
		this.callgraphSize = callgraphSize;
		this.index = index;
		this.fileDir = fileDir;
		this.inArgs = new ArrayList<String>();
		this.outRes = new ArrayList<String>();
		this.funcNameRep = new HashMap<String,String>();
		((TIRNode)analysis.getNodeList().get(index).getAnalysis().getTree()).tirAnalyze(this);
	}
	
	
	public static String FortranCodePrinter(
			ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, int callgraphSize, int index, String fileDir){
		return new FortranCodeGenerator(analysis, callgraphSize, index, fileDir).buf2.toString();
	
	}
	
	
	@Override
	public void caseASTNode(ASTNode node){}
	
	@Override
	public void caseTIRFunction(TIRFunction node){
		this.majorName = node.getName();
		for(Name param : node.getInputParams()){
			inArgs.add(param.getVarName());
		}
		for(Name result : node.getOutputParams()){
			outRes.add(result.getVarName());
		}
		/*
		 *deal with main entry point, main program. 
		 */
		if(index==0){
			String indent = node.getIndent();
			boolean first = true;;
			//buf.append(indent + "public static def " );
			printStatements(node.getStmts());
			//Write code for nested functions here
			//buf.append(indent + "}//end of function\n}//end of class\n");
			buf.append(indent + "      stop\n      end");
			
			if (Debug) System.out.println("the parameters in for stmt: "+forStmtParameter);
			
			buf2.append(indent + "      program ");
			// TODO - CHANGE IT TO DETERMINE RETURN TYPE		
			buf2.append(majorName);
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
			
			//System.out.println(this.analysis.getNodeList().get(index).getAnalysis().getOutFlowSets());
			if (Debug) System.out.println(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().keySet()+"\n");
			
			for(String variable : this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().keySet()){
				if(forStmtParameter.contains(variable)){
					if (Debug) System.out.println("variable "+variable+" is a for stmt parameter.");
					if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
					
					//complex or not others, like real, integer or something else
					/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
						if (Debug) System.out.println("COMPLEX here!");
						buf.append("\n      complex");
					}
					else{
						buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					}*/
					buf2.append("\n      " + FortranMap.getFortranTypeMapping("int8"));
					//parameter
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
						if (Debug) System.out.println("add parameter here!");
						buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
					}
					else{
						buf2.append(" :: " + variable);
					}
				}
				else{
					if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
					
					//complex or not others, like real, integer or something else
					/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
						if (Debug) System.out.println("COMPLEX here!");
						buf.append("\n      complex");
					}
					else{
						buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					}*/
					buf2.append("\n      " + FortranMap.getFortranTypeMapping(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					//parameter
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
						if (Debug) System.out.println("add parameter here!");
						buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
					}
					else{
						//dimension
						if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().isScalar()==false){
							if (Debug) System.out.println("add dimension here!");
							buf2.append(" , dimension(");
							ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().getDimensions());
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
			/*
			 * at the end of declaration, declare those user defined function.
			 */
			for(String key : funcNameRep.keySet()){
				System.out.println(key);
				String variable = funcNameRep.get(key);
				if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
				
				//complex or not others, like real, integer or something else
				/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
					if (Debug) System.out.println("COMPLEX here!");
					buf.append("\n      complex");
				}
				else{
					buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				}*/
				buf2.append("\n      " + FortranMap.getFortranTypeMapping(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				//parameter
				if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
					if (Debug) System.out.println("add parameter here!");
					buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
				}
				else{
					//dimension
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().isScalar()==false){
						if (Debug) System.out.println("add dimension here!");
						buf2.append(" , dimension(");
						ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().getDimensions());
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
						buf2.append(" :: " + key);
					}
					else{
						buf2.append(" :: " + key);
					}
				}
			}
			
			buf2.append("\n");
			buf2.append(buf);
			try{
				BufferedWriter out = new BufferedWriter(new FileWriter(fileDir+majorName+".f"));
				out.write(buf2.toString());
				out.close();
			}
			catch(IOException e){
				System.out.println("Exception ");

			}
		}
		/*
		 * deal with functions or sub-routines
		 */
		else{
			String indent = node.getIndent();
			boolean first = true;
			
			printStatements(node.getStmts());
			buf.append(indent + "      return\n      end");
			
			if (Debug) System.out.println("the parameters in for stmt: "+forStmtParameter);
			
			buf2.append(indent + "      function ");
			buf2.append(majorName);
			buf2.append("(");
			first = true;
			for(Name param : node.getInputParams()) {
				if(!first) {
					buf2.append(", ");
				}
				buf2.append(param.getVarName());
				first = false;
			}
			buf2.append(")");
			buf2.append("\n      implicit none");
			
			if (Debug) System.out.println(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().keySet()+"\n");
			
			for(String variable : this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().keySet()){
				/*
				 * deal with for statement variables...Fortran must declare them integer
				 */
				if(forStmtParameter.contains(variable)){
					if (Debug) System.out.println("variable "+variable+" is a for stmt parameter.");
					if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
					
					//complex or not others, like real, integer or something else
					/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
						if (Debug) System.out.println("COMPLEX here!");
						buf.append("\n      complex");
					}
					else{
						buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					}*/
					buf2.append("\n      " + FortranMap.getFortranTypeMapping("int8"));
					//parameter
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
						if (Debug) System.out.println("add parameter here!");
						buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
					}
					else{
						buf2.append(" :: " + variable);
					}
				}
				/*
				 * general situations...
				 */
				else{
					if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
					
					//complex or not others, like real, integer or something else
					/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
						if (Debug) System.out.println("COMPLEX here!");
						buf.append("\n      complex");
					}
					else{
						buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					}*/
					buf2.append("\n      " + FortranMap.getFortranTypeMapping(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					//parameter
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()&&(inArgs.contains(variable)==false)&&(outRes.contains(variable)==false)){
						if (Debug) System.out.println("add parameter here!");
						buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
					}
					else{
						//dimension
						if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().isScalar()==false){
							if (Debug) System.out.println("add dimension here!");
							buf2.append(" , dimension(");
							ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().getDimensions());
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
							if(outRes.contains(variable)){
								buf2.append(" :: " + majorName);
							}
							else{
								buf2.append(" :: " + variable);
							}
						}
						else{
							if (Debug) System.out.println("Is this variable in the output parameters set: "+outRes.contains(variable));
							if(outRes.contains(variable)){
								buf2.append(" :: " + majorName);
							}
							else{
								buf2.append(" :: " + variable);
							}
						}
					}
				}
			}
			buf2.append("\n");
			buf2.append(buf);
			try{
				BufferedWriter out = new BufferedWriter(new FileWriter(fileDir+node.getName()+".f"));
				out.write(buf2.toString());
				out.close();
			}
			catch(IOException e){
				System.out.println("Exception ");

			}
		}
		
	}
	
	@Override
	public void caseTIRAssignLiteralStmt(TIRAssignLiteralStmt node){
		String LHS;
		LHS = node.getTargetName().getVarName();
		String RHS;
		if(node.getRHS().getRValue() instanceof IntLiteralExpr){
			RHS = ((IntLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString();
		}
		else{
			RHS = ((FPLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString();
		}
		if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).isConstant()){
			if (Debug) System.out.println(LHS+" is a constant");
		}
		else{
			buf.append("      "+LHS+" = "+RHS+";");
		}
	}
	
	@Override
	public void caseTIRAbstractAssignToListStmt(TIRAbstractAssignToListStmt node){
		if(FortranMap.isFortranNoDirectBuiltin(node.getRHS().getVarName())){
			System.out.println("this function has no corresponding builtin function in Fortran...");
			if(node.getRHS().getVarName().equals("horzcat")){
				String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
				ArrayList<String> Args = new ArrayList<String>();
				Args = GetArgs(node);
				ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).getShape().getDimensions());
				int i=1;
				int length = Args.size();
				for(String Arg : Args){
					buf.append("      "+LHS+"(1,"+i+") = "+Arg+";");
					i = i+1;
					if(i<=length){
					buf.append("\n");	
					}
				}
			}
			//TODO add more no direct mapping built-ins
			 
		}
		else{
			String LHS;
			ArrayList<String> vars = new ArrayList<String>();
			for(ast.Name name : node.getTargets().asNameList()){
				vars.add(name.getID());
			}
			
			if (1==vars.size()){ //only one variable on LHS
				symbolMapKey = vars.get(0);
				LHS = symbolMapKey;
				
				if(true == symbolMap.containsKey(symbolMapKey)) //variable already defined and analyzed
				{
					//buf.append(((TIRAbstractAssignToVarStmt)node).getPrettyPrintedLessComments());
					if(outRes.contains(LHS)){
						buf.append("      "+majorName + " = ");
					}
					else{
						buf.append("      "+LHS+" = ");
					}
				}
				else
				{
					if(outRes.contains(LHS)){
						buf.append("      "+majorName + " = ");
					}
					else{
						buf.append("      "+LHS+" = ");
					}
					//use varname to get the name of the method/operator/Var
				}
				    makeExpression(node);
					symbolMap.put(node.getLHS().getNodeString(), getAnalysisValue(analysis, node, LHS));
			}
			else if(0==vars.size()){
				//TODO
				  makeExpression(node);
			}
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
			//TODO, add more similar "method"
			break;
		case 5:
			buf.append(RHS+";");
			break;
		case 6:
			Args = GetArgs(node);
			ArgsListasString = GetArgsListasString(Args);
			buf.append("      "+RHS+ArgsListasString);
			break;
		case 7:
			String RHSName;
			RHSName = node.getRHS().getVarName();
			String LHSName;
			LHSName = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			//XU, a little bit trick, go back to IR to get a better solution
			funcNameRep.put(RHSName, LHSName);
			Args = GetArgs(node);
			ArgsListasString = GetArgsListasString(Args);
			buf.append(RHSName+"("+ArgsListasString+");");
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
			return 7; // "user defined function";
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
	
	@Override
	public void caseTIRAbstractAssignToVarStmt(TIRAbstractAssignToVarStmt node){
		//vars.add(((TIRAbstractAssignToVarStmt)node).getTargetName().getID());
		//if already present in symbolMap=>has been analyzed else define
		String LHS;
		//ArrayList<String> vars = new ArrayList<String>();
		symbolMapKey = node.getTargetName().getID();
		LHS = symbolMapKey;
		if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).isConstant()){
			if (Debug) System.out.println(LHS+" is a constant");
		}
		else{
			if(true == symbolMap.containsKey(symbolMapKey)) //variable already defined and analyzed
			{
				buf.append(node.getPrettyPrintedLessComments());
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
	
	@Override
	public void caseTIRIfStmt(TIRIfStmt node){
		if (Debug) System.out.println("in if statement.");
		if (Debug) System.out.println(node.getConditionVarName().getID());
		buf.append("      if ("+node.getConditionVarName().getID()+") then\n");
		indentIf = true;
		printStatements(node.getIfStameents());
		indentIf = false;
		buf.append("      else\n");
		indentIf = true;
		printStatements(node.getElseStatements());
		indentIf = false;
		buf.append("      endif");
		return;
	}
	
	@Override
	public void caseTIRWhileStmt(TIRWhileStmt node){
		if (Debug) System.out.println("in while statement.");
		if (Debug) System.out.println(node.getCondition().getVarName());
		buf.append("      do while ("+node.getCondition().getVarName()+")\n");
		indentFW = true;
		printStatements(node.getStatements());
		indentFW = false;
		buf.append("      enddo");
		return;
	}
	
	@Override
	public void caseTIRForStmt(TIRForStmt node){
		if (Debug) System.out.println("in for statement.");
		if (Debug) System.out.println(node.getLoopVarName().getVarName());
		buf.append("      do "+node.getLoopVarName().getVarName()+" = "+node.getLowerName().getVarName()+" , "+node.getUpperName().getVarName()+"\n");
		indentFW = true;
		printStatements(node.getStatements());
		indentFW = false;
		buf.append("      enddo");
		forStmtParameter.add(node.getLoopVarName().getVarName());
		forStmtParameter.add(node.getLowerName().getVarName());
		forStmtParameter.add(node.getUpperName().getVarName());
	}
	
	@Override
	public void caseTIRArrayGetStmt(TIRArrayGetStmt node){
		
	}
	
	@Override
	public void caseTIRArraySetStmt(TIRArraySetStmt node){
		
	}
	
	/**********************HELPER METHODS***********************************/
	private String getLHSType(ValueAnalysis<?> analysis,
			TIRAbstractAssignStmt node, String SymbolMapKey) {
		//node.getTargetName().getID()
		return analysis.getNodeList().get(index).getAnalysis().getOutFlowSets().get(node).get(SymbolMapKey).getMatlabClasses().toArray()[0].toString();
		
	}


	
	private String getArgumentType(ValueAnalysis<?> analysis, TIRFunction node, String paramID){
		//System.out.println(analysis.getOutFlowSets().get(node).get(paramID).toString());

		return analysis.getNodeList().get(index).getAnalysis().getOutFlowSets().get(node).get(paramID).getMatlabClasses().toArray()[0].toString();
	}
	
	//get analysis value for Function node
	private Collection<ClassReference> getAnalysisValue(ValueAnalysis<?> analysis, TIRFunction node, String ID){
		return analysis.getNodeList().get(index).getAnalysis().getOutFlowSets().get(node).get(ID).getMatlabClasses();

		//return analysis.getOutFlowSets().get(node).get(paramID).getMatlabClasses().toArray()[0].toString();
	}
	
	
	//get analysis value for abstract assignment node
	private Collection<ClassReference> getAnalysisValue(ValueAnalysis<?> analysis, TIRAbstractAssignStmt node, String ID){
		return analysis.getNodeList().get(index).getAnalysis().getOutFlowSets().get(node).get(ID).getMatlabClasses();

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
			if(indentIf == true){
				buf.append("  ");
			}
			else if(indentFW == true){
				buf.append(" ");
			}
			int length = buf.length();
			((TIRNode)stmt).tirAnalyze(this);
			if (buf.length() > length) buf.append('\n');
		}
	}
	
}


