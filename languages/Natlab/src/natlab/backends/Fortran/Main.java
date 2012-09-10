package natlab.backends.Fortran;

import natlab.tame.BasicTamerTool;
import natlab.tame.valueanalysis.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.aggrvalue.*;
import natlab.toolkits.filehandling.genericFile.GenericFile;
import natlab.toolkits.path.FileEnvironment;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.Program;

public class Main {
	
	public static void main(String[] args) {
		String fileDir = "/home/xuli/for_test/";
	    String fileIn = fileDir+"hello.m";
	    GenericFile gFile = GenericFile.create(fileIn);
		FileEnvironment env = new FileEnvironment(gFile); //get path environment obj
		BasicTamerTool tool = new BasicTamerTool();
		ValueAnalysis<AggrValue<BasicMatrixValue>>  analysis = tool.analyze(args, env);
	    /**
	     * pretty print the generated code.
	     */
		String fortranCode;
		int size = analysis.getNodeList().size();
		for(int i=0;i<=size-1;i++){
			System.out.println("\n~~~~~~~~~~~~~~~~Analysis during Code Generation~~~~~~~~~~~~~~~~~~~~~~~\n");
			fortranCode = FortranCodePrettyPrinter.FortranCodePrinter(analysis, size, i, fileDir);
			System.out.println("\n~~~~~~~~~~~~~~~~~~~~~Generated Fortran Code~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			System.out.println(fortranCode);
		}
		
		/**
		 * generate the Fortran AST and then let the AST toString itself.
		 */
		Program program = new Program();
	}
}
