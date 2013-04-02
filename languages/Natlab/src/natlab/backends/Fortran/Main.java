package natlab.backends.Fortran;

import java.io.*;

import natlab.tame.BasicTamerTool;
import natlab.tame.valueanalysis.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.aggrvalue.*;
import natlab.toolkits.filehandling.GenericFile;
import natlab.toolkits.path.FileEnvironment;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class Main {
	public static void main(String[] args) {
		/**
		 * This main method is just for testing, doesn't follow the convention when passing a 
		 * file to a program, please replace "fileDir and fileIn" below with your real testing 
		 * file directory and its name, and you can pass the type info of the input argument 
		 * to the program, currently, the type info is composed like double&3*3&REAL.
		 */
		String fileDir = "/home/xu/for_test/";
	    String fileIn = fileDir+"testBuiltin2.m";
	    GenericFile gFile = GenericFile.create(fileIn);
		FileEnvironment env = new FileEnvironment(gFile); //get path environment obj
		BasicTamerTool tool = new BasicTamerTool();
		ValueAnalysis<AggrValue<BasicMatrixValue>>  analysis = tool.analyze(args, env);
		int size = analysis.getNodeList().size();
		
		/**
		 * generate the Fortran AST and then let the AST toString itself.
		 */
		Program prg = new Program();
		for (int i=0;i<=size-1;i++) {
			System.out.println("\n~~~~~~~~~~~~~~~~Analysis during Code Generation~~~~~~~~~~~~~~~~~~~~~~~\n");
			prg.setSubProgram(FortranCodeASTGenerator.FortranProgramGen(analysis, size, i, fileDir), i);
			System.out.println("\n~~~~~~~~~~~~~~~~~~~~~Generated Fortran Code~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			StringBuffer sb = new StringBuffer();
			prg.getSubProgram(i).pp(sb);
			System.out.println(sb);
			try {
				String pFilename = prg.getSubProgram(i).getProgramTitle().getProgramName();
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
