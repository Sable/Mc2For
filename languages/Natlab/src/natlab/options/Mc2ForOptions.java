package natlab.options;

import natlab.options.Options;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Mc2ForOptions extends Options {
	// private JCommander jct=null;
	@Parameter(names = { "--codegen" }, description = "Transform MATLAB to Fortran with run-time ABC code")
	protected boolean codegen = false;
	
	public boolean codegen() {
		return codegen;
	}

	@Parameter(names = { "--nocheck" }, description = "Transform MATLAB to Fortran without run-time array bounds checking code")
	protected boolean nocheck = false;
	
	public boolean nocheck() {
		return nocheck;
	}
	
	// public void getUsage(){
	// 	jct.usage();
	// }
}
