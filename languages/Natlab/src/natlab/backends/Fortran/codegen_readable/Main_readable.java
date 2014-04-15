package natlab.backends.Fortran.codegen_readable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import ast.ASTNode;
import ast.*;

import natlab.options.Options;
import natlab.tame.BasicTamerTool;
import natlab.tame.callgraph.StaticFunction;
import natlab.tame.tamerplus.analysis.AnalysisEngine;
import natlab.tame.tamerplus.transformation.TransformationEngine;
import natlab.tame.valueanalysis.ValueAnalysis;
import natlab.tame.valueanalysis.ValueFlowMap;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.toolkits.filehandling.GenericFile;
import natlab.toolkits.path.FileEnvironment;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.*;

public class Main_readable {
	static boolean Debug =false;
	/**
	 * This main method is just for testing, doesn't follow the convention when passing a 
	 * file to a program, please replace "fileDir and entry" below with your real testing 
	 * file directory and entry func, and you can pass the type info of the input argument 
	 * to the program, currently, the type info is composed like double&3*3&REAL.
	 */
	public static void main(String[] args) {
		String fileDir = "/home/aaron/Dropbox/benchmarks/testload/";
	    String entryPointFile = "test_load";
	    GenericFile gFile = GenericFile.create(fileDir + entryPointFile + ".m");
		FileEnvironment env = new FileEnvironment(gFile); //get path environment obj
		
		BasicTamerTool tool = new BasicTamerTool();
		ValueAnalysis<AggrValue<BasicMatrixValue>>  analysis = tool.analyze(args, env);
		int size = analysis.getNodeList().size();
		Set<String> visitedFunctions = new HashSet<String>();
		
		// preprocess to get all the names of the user defined functions in the program.
		Set<String> userDefinedFunctions = new HashSet<String>();
		for (int i = 0; i < size; i++) {
			String functionName = analysis.getNodeList().get(i).getFunction().getName();
			if (!functionName.equals(entryPointFile)) {
				userDefinedFunctions.add(functionName);
			}
		}
		
		/* 
		 * run tamer plus analysis first, then using the AST from tamer plus 
		 * to generate fortran AST and let the AST pretty print itself. 
		 */
		for (int i = 0; i < size; i++) {
			// currently, I don't know why there are multiple same functions in the node list. TODO
			String functionName = analysis.getNodeList().get(i).getFunction().getName();
			if (!visitedFunctions.contains(functionName)) {
				visitedFunctions.add(functionName);

				/*
				 * type inference.
				 */
				ValueFlowMap<AggrValue<BasicMatrixValue>> currentOutSet = 
						analysis.getNodeList().get(i).getAnalysis().getCurrentOutSet();
				// System.err.println(currentOutSet);
				/*
				 * tamer plus analysis.
				 */
				StaticFunction function = analysis.getNodeList().get(i).getFunction();
				// TamerPlusUtils.debugMode();
				// System.out.println("tamer pretty print: \n"+function.getAst().getPrettyPrinted());
		        TransformationEngine transformationEngine = TransformationEngine
		        		.forAST(function.getAst());
		        AnalysisEngine analysisEngine = transformationEngine
		        		.getAnalysisEngine();
		        @SuppressWarnings("rawtypes")
		        ASTNode fTree = transformationEngine
		        		.getTIRToMcSAFIRWithoutTemp().getTransformedTree();
		        Set<String> remainingVars = analysisEngine
		        		.getTemporaryVariablesRemovalAnalysis().getRemainingVariablesNames();
		        System.err.println("\ntamer plus analysis result: \n" 
		        		+ fTree.getPrettyPrinted() + "\n");
		        if (Debug) System.err.println("remaining variables: \n"+remainingVars);
		        
		        /*
		         * Fortran code generation.
		         */
		        Subprogram subprogram = FortranCodeASTGenerator.generateFortran(
		        		(Function)fTree, 
		        		currentOutSet, 
		        		remainingVars, 
		        		entryPointFile, 
		        		userDefinedFunctions, 
		        		analysisEngine, 
		        		true); // nocheck
		        StringBuffer sb = new StringBuffer();
		        String currentFunction = subprogram.getProgramTitle().getProgramName();
		        String subprogramType = subprogram.getProgramTitle().getProgramType();
		        if (subprogramType.equals("SUBROUTINE")) {
		        	sb.append("MODULE mod_"+currentFunction+"\n\nCONTAINS\n\n");
		        	subprogram.pp(sb);
		        	sb.append("\nEND MODULE");
		        }
		        else {
		        	subprogram.pp(sb);
		        }

		        String output = sb.toString();
				/*
				 * since variable name in matlab is case-sensitive, while in fortran
				 * it's case-insensitive, so we have to rename the variable whose 
				 * name is case-insensitively equivalent to another variable.
				 */
		        Map<String, ArrayList<String>> eqNameVars = new HashMap<String, ArrayList<String>>();
				for (String name : remainingVars) {
					for (String iterateVar : remainingVars) {
						if (!name.equals(iterateVar) 
								&& name.toLowerCase().equals(iterateVar.toLowerCase())) {
							if (eqNameVars.containsKey(name.toLowerCase())) {
								ArrayList<String> valueList = eqNameVars.get(name);
								if (!valueList.contains(name)) {
									valueList.add(name);
								}
							}
							else {
								ArrayList<String> valueList = new ArrayList<String>();
								valueList.add(name);
								eqNameVars.put(name.toLowerCase(), valueList);
							}
						}
					}
				}
				if (Debug) System.out.println("variables are " +
						"case-insensitively equivalent:" + eqNameVars);
		        for (String key : eqNameVars.keySet()) {
		        	for (int j = 0; j < eqNameVars.get(key).size(); j++) {
		        		String tempVar = eqNameVars.get(key).get(j);
		        		if (j != 0) {
		        			output = output.replaceAll("\\b" + tempVar + "\\b", tempVar + "_rn" + j);
		        		}
		        	}
		        }
				
				
		        System.err.println("pretty print the generated Fortran code:");
		        System.out.println(output);
		        
				// write the generated fortran code to files.
				try {
					BufferedWriter out = new BufferedWriter(
							new FileWriter(fileDir + currentFunction + ".f95"));  
			        out.write(output);  
			        out.flush();  
			        out.close(); 
				} catch(IOException e) {
					System.err.println(e);
				}
			}
			else {
				// already visited, do nothing.
			}
        }
	}
	
	public static void compile(Options options) {
		FileEnvironment fileEnvironment = new FileEnvironment(options); //get path/files

		//arguments - TODO for now just parse them as inputs
		String args = "double&1*1"; //start with the default
		if (options.arguments() != null && options.arguments().length() > 0){
			args = options.arguments();
		}
		
		// TODO now it's for testing...
		String[] argsList = {args};
		
		BasicTamerTool tool = new BasicTamerTool();
		ValueAnalysis<AggrValue<BasicMatrixValue>>  analysis = tool.analyze(argsList, fileEnvironment);
		int size = analysis.getNodeList().size();
		Set<String> visitedFunctions = new HashSet<String>();
		
		// preprocess to get all the names of the user defined functions in the program.
		Set<String> userDefinedFunctions = new HashSet<String>();
		for (int i = 0; i < size; i++) {
			String functionName = analysis.getNodeList().get(i).getFunction().getName();
			if (!functionName.equals(fileEnvironment.getMainFile().getName().replace(".m", ""))) {
				userDefinedFunctions.add(functionName);
			}
		}
		
		/* 
		 * run tamer plus analysis first, then using the AST from tamer plus 
		 * to generate fortran AST and let the AST pretty print itself. 
		 */
		for (int i = 0; i < size; i++) {
			// currently, I don't know why there are multiple same functions in the node list. TODO
			String functionName = analysis.getNodeList().get(i).getFunction().getName();
			if (!visitedFunctions.contains(functionName)) {
				visitedFunctions.add(functionName);
				/*
				 * type inference.
				 */
				ValueFlowMap<AggrValue<BasicMatrixValue>> currentOutSet = 
						analysis.getNodeList().get(i).getAnalysis().getCurrentOutSet();
				// System.err.println(currentOutSet);
				/*
				 * tamer plus analysis.
				 */
				StaticFunction function = analysis.getNodeList().get(i).getFunction();
				// TamerPlusUtils.debugMode();
				// System.out.println("tamer pretty print: \n"+function.getAst().getPrettyPrinted());
		        TransformationEngine transformationEngine = TransformationEngine
		        		.forAST(function.getAst());
		        AnalysisEngine analysisEngine = transformationEngine
		        		.getAnalysisEngine();
		        @SuppressWarnings("rawtypes")
		        ASTNode fTree = transformationEngine
		        		.getTIRToMcSAFIRWithoutTemp().getTransformedTree();
		        Set<String> remainingVars = analysisEngine
		        		.getTemporaryVariablesRemovalAnalysis().getRemainingVariablesNames();
		        System.err.println("\ntamer plus analysis result: \n" 
		        		+ fTree.getPrettyPrinted() + "\n");
		        if (Debug) System.err.println("remaining variables: \n"+remainingVars);
		        
		        /*
		         * Fortran code generation.
		         */
		        Subprogram subprogram = FortranCodeASTGenerator.generateFortran(
		        		(Function)fTree, 
		        		currentOutSet, 
		        		remainingVars, 
		        		fileEnvironment.getMainFile().getName().replace(".m", ""), 
		        		userDefinedFunctions, 
		        		analysisEngine, 
		        		options.nocheck());
		        StringBuffer sb = new StringBuffer();
		        String currentFunction = subprogram.getProgramTitle().getProgramName();
		        String subprogramType = subprogram.getProgramTitle().getProgramType();
		        if (subprogramType.equals("SUBROUTINE")) {
		        	sb.append("MODULE mod_"+currentFunction+"\n\nCONTAINS\n\n");
		        	subprogram.pp(sb);
		        	sb.append("\nEND MODULE");
		        }
		        else {
		        	subprogram.pp(sb);
		        }
		        
		        String output = sb.toString();
				/*
				 * since variable name in matlab is case-sensitive, while in fortran
				 * it's case-insensitive, so we have to rename the variable whose 
				 * name is case-insensitively equivalent to another variable.
				 */
		        Map<String, ArrayList<String>> eqNameVars = new HashMap<String, ArrayList<String>>();
				for (String name : remainingVars) {
					for (String iterateVar : remainingVars) {
						if (!name.equals(iterateVar) 
								&& name.toLowerCase().equals(iterateVar.toLowerCase())) {
							if (eqNameVars.containsKey(name.toLowerCase())) {
								ArrayList<String> valueList = eqNameVars.get(name);
								if (!valueList.contains(name)) {
									valueList.add(name);
								}
							}
							else {
								ArrayList<String> valueList = new ArrayList<String>();
								valueList.add(name);
								eqNameVars.put(name.toLowerCase(), valueList);
							}
						}
					}
				}
				if (Debug) System.out.println("variables are " +
						"case-insensitively equivalent:" + eqNameVars);
		        for (String key : eqNameVars.keySet()) {
		        	for (int j = 0; j < eqNameVars.get(key).size(); j++) {
		        		String tempVar = eqNameVars.get(key).get(j);
		        		if (j != 0) {
		        			output = output.replaceAll("\\b" + tempVar + "\\b", tempVar + "_rn" + j);
		        		}
		        	}
		        }

		        if (options.nocheck()) {
		        	System.err.println("***without run-time ABC code***");
		        }
		        else {
		        	System.err.println("***with run-time ABC code***");
		        }
		        System.err.println("pretty print the generated Fortran code:");
		        System.out.println(output);
				
		        // write the transformed result to files.
		        try {
		        	BufferedWriter out = new BufferedWriter(new FileWriter(
		        			fileEnvironment.getPwd().getPath()
		        			+ "/"
		        			+ function.getName() + ".f95"));
		        	out.write(output);
		        	out.flush();
		        	out.close();
		        } catch (IOException e) {
		        	System.err.println(e);
		        }				
			}
			else {
				// already visited, do nothing.
			}
        }
	}
}
