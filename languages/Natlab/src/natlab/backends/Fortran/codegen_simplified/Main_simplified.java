package natlab.backends.Fortran.codegen_simplified;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import natlab.tame.BasicTamerTool;
import natlab.tame.valueanalysis.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.aggrvalue.*;
import natlab.toolkits.filehandling.GenericFile;
import natlab.toolkits.path.FileEnvironment;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.*;

public class Main_simplified {

	/**
	 * This main method is just for testing, doesn't follow the convention when passing a 
	 * file to a program, please replace "fileDir and entry" below with your real testing 
	 * file directory and entry func, and you can pass the type info of the input argument 
	 * to the program, currently, the type info is composed like double&3*3&REAL.
	 */
	public static void main(String[] args) {
		String fileDir = "fileDir";
	    String entryPointFile = "entry";
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
		Program prg = new Program();
		for (int i = 0; i < size; i++) {
			System.out.println(
					"\n~~~~~~~~~~~~~~~~Analysis during Code Generation~~~~~~~~~~~~~~~~~~~~~~~\n");
			prg.setSubprogram(FortranCodeASTGenerator.generateFortran(
					analysis, 
					size, 
					i, 
					entryPointFile, 
					userDefinedFunctions), i);
			
			System.out.println(
					"\n~~~~~~~~~~~~~~~~~~~~~Generated Fortran Code~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			StringBuffer sb = new StringBuffer();
			prg.getSubprogram(i).pp(sb);
			System.out.println(sb);
			
			// write the generated fortran code to files.
			try {
				String pFilename = prg.getSubprogram(i).getProgramTitle().getProgramName();
				BufferedWriter out = new BufferedWriter(new FileWriter(fileDir+pFilename+".f95"));  
		        out.write(sb.toString());  
		        out.flush();  
		        out.close(); 
			} catch(IOException e) {
				System.err.println(e);
			}
		}
	}
}
