package natlab.backends.Fortran.codegen_readable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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

	/**
	 * This main method is just for testing, doesn't follow the convention when passing a 
	 * file to a program, please replace "fileDir and entry" below with your real testing 
	 * file directory and entry func, and you can pass the type info of the input argument 
	 * to the program, currently, the type info is composed like double&3*3&REAL.
	 */
	public static void main(String[] args) {
		String fileDir = "/home/aaron/Dropbox/benchmarks/sample/";
	    String entryPointFile = "drv_babai";
	    GenericFile gFile = GenericFile.create(fileDir + entryPointFile + ".m");
		FileEnvironment env = new FileEnvironment(gFile); //get path environment obj
		BasicTamerTool tool = new BasicTamerTool();
		ValueAnalysis<AggrValue<BasicMatrixValue>>  analysis = tool.analyze(args, env);
		int size = analysis.getNodeList().size();
		
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
	        // System.err.println("remaining variables: \n"+remainingVars);
	        
	        /*
	         * Fortran code generation.
	         */
	        System.err.println("pretty print the generated Fortran code:");
	        Subprogram subprogram = FortranCodeASTGenerator.generateFortran(
	        		(Function)fTree, 
	        		currentOutSet, 
	        		remainingVars, 
	        		entryPointFile);
	        StringBuffer sb = new StringBuffer();
	        String currentFunction = subprogram.getProgramTitle().getProgramName();
	        if (!currentFunction.equals(entryPointFile)) {
	        	sb.append("MODULE mod_"+currentFunction+"\n\nCONTAINS\n\n");
	        	subprogram.pp(sb);
	        	sb.append("\nEND MODULE");
	        }
	        else {
	        	subprogram.pp(sb);
	        }
	        System.out.println(sb);
			
			// write the generated fortran code to files.
			try {
				BufferedWriter out = new BufferedWriter(
						new FileWriter(fileDir + currentFunction + ".f95"));  
		        out.write(sb.toString());  
		        out.flush();  
		        out.close(); 
			} catch(IOException e) {
				System.err.println(e);
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
	        // System.err.println("remaining variables: \n"+remainingVars);
	        
	        /*
	         * Fortran code generation.
	         */
	        System.err.println("pretty print the generated Fortran code:");
	        Subprogram subprogram = FortranCodeASTGenerator.generateFortran(
	        		(Function)fTree, 
	        		currentOutSet, 
	        		remainingVars, 
	        		fileEnvironment.getMainFile().getName().replace(".m", ""));
	        StringBuffer sb = new StringBuffer();
	        String currentFunction = subprogram.getProgramTitle().getProgramName();
	        if (!currentFunction.equals(fileEnvironment.getMainFile().getName().replace(".m", ""))) {
	        	sb.append("MODULE mod_"+currentFunction+"\n\nCONTAINS\n\n");
	        	subprogram.pp(sb);
	        	sb.append("\nEND MODULE");
	        }
	        else {
	        	subprogram.pp(sb);
	        }
	        System.out.println(sb);
			
	        // write the transformed result to files.
	        try {
	        	BufferedWriter out = new BufferedWriter(new FileWriter(
	        			fileEnvironment.getPwd().getPath()
	        			+ "/"
	        			+ function.getName() + ".f95"));
	        	out.write(sb.toString());
	        	out.flush();
	        	out.close();
	        } catch (IOException e) {
	        	System.err.println(e);
	        }
        }
	}
}
