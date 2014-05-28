package natlab.backends.Fortran.codegen_simplified;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.Program;
import natlab.tame.BasicTamerTool;
import natlab.tame.valueanalysis.ValueAnalysis;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.toolkits.filehandling.GenericFile;
import natlab.toolkits.path.FileEnvironment;

public class Main_simplified {

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
		
		// generate the Fortran AST and then let the AST pretty print itself.
		Program program = new Program();
		for (int i = 0; i < size; i++) {
			System.out.println(
					"\n~~~~~~~~~~~~~~~~Analysis during Code Generation~~~~~~~~~~~~~~~~~~~~~~~\n");
			program.setSubprogram(FortranCodeASTGenerator.generateFortran(
					analysis, 
					size, 
					i, 
					entryPointFile, 
					userDefinedFunctions), i);
			
			System.out.println(
					"\n~~~~~~~~~~~~~~~~~~~~~Generated Fortran Code~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			StringBuffer sb = new StringBuffer();
			String currentFunction = program.getSubprogram(i).getProgramTitle().getProgramName();
			if (!currentFunction.equals(entryPointFile)) {
				sb.append("MODULE mod_"+currentFunction+"\n\nCONTAINS\n\n");
				program.getSubprogram(i).pp(sb);
				sb.append("\nEND MODULE");
			}
			else {
				program.getSubprogram(i).pp(sb);
			}
			System.out.println(sb);
			
			// write the generated fortran code to files.
			try {
				BufferedWriter out = new BufferedWriter(
						new FileWriter(fileDir+currentFunction+".f95"));  
		        out.write(sb.toString());  
		        out.flush();  
		        out.close(); 
			} catch(IOException e) {
				System.err.println(e);
			}
		}
	}
}
