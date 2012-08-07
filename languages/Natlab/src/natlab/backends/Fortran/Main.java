package natlab.backends.Fortran;

import java.util.Collections;

import java.io.*;

import org.w3c.dom.Document;

import natlab.tame.BasicTamerTool;
import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.aggrvalue.*;
import natlab.tame.valueanalysis.simplematrix.*;
import natlab.tame.valueanalysis.value.Value;
import natlab.toolkits.filehandling.genericFile.GenericFile;
import natlab.toolkits.path.FileEnvironment;
import natlab.backends.Fortran.codegen.*;

public class Main {
	
	public static void main(String[] args) {
		String file = "/home/xuli/for_test/hello";
	    String fileIn = file+".m";
	    String fileOut =  file+".f";
	    GenericFile gFile = GenericFile.create(fileIn);
		FileEnvironment env = new FileEnvironment(gFile); //get path environment obj
	    String fortranCode;
		BasicTamerTool tool = new BasicTamerTool();
		ValueAnalysis<AggrValue<BasicMatrixValue>>  analysis = tool.analyze(args, env);
		
		System.out.println("\n~~~~~~~~~~~~~~~~Analysis during Code Generation~~~~~~~~~~~~~~~~~~~~~~~\n");
		fortranCode = FortranCodeGenerator.FortranCodePrinter(analysis, "testclass");
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~Generated Fortran Code~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		System.out.println(fortranCode);
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write(fortranCode);
			out.close();
			}
			catch (IOException e)
			{
			System.out.println("Exception ");

			}
	}
}
