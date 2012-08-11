package natlab.backends.Fortran;

import natlab.tame.BasicTamerTool;
import natlab.tame.valueanalysis.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.aggrvalue.*;
import natlab.toolkits.filehandling.genericFile.GenericFile;
import natlab.toolkits.path.FileEnvironment;
import natlab.backends.Fortran.codegen.*;

public class Main {
	
	public static void main(String[] args) {
		String fileDir = "/home/xuli/for_test/";
	    String fileIn = fileDir+"hello.m";
	    GenericFile gFile = GenericFile.create(fileIn);
		FileEnvironment env = new FileEnvironment(gFile); //get path environment obj
	    String fortranCode;
		BasicTamerTool tool = new BasicTamerTool();
		ValueAnalysis<AggrValue<BasicMatrixValue>>  analysis = tool.analyze(args, env);
		int size = analysis.getNodeList().size();
		for(int i=size-1;i>=0;i--){
			System.out.println("\n~~~~~~~~~~~~~~~~Analysis during Code Generation~~~~~~~~~~~~~~~~~~~~~~~\n");
			fortranCode = FortranCodeGenerator.FortranCodePrinter(analysis, size, i, fileDir);
			System.out.println("\n~~~~~~~~~~~~~~~~~~~~~Generated Fortran Code~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			System.out.println(fortranCode);
		}
	}
}
