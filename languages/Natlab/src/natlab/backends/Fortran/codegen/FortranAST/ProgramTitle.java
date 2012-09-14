package natlab.backends.Fortran.codegen.FortranAST;


public class ProgramTitle extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 4

    public ProgramTitle() {
        super();

        setChild(new Opt(), 0);
    }

    // Declared in FortranIR.ast line 4
    public ProgramTitle(String p0, String p1, Opt p2) {
        setProgramType(p0);
        setProgramName(p1);
        setChild(p2, 0);
    }

    public Object clone() throws CloneNotSupportedException {
        ProgramTitle node = (ProgramTitle)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          ProgramTitle node = (ProgramTitle)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ProgramTitle res = (ProgramTitle)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    public void flushCache() {
        super.flushCache();
    }
  protected int numChildren() {
    return 1;
  }
    // Declared in FortranIR.ast line 4
    private String tokenString_ProgramType;
    public void setProgramType(String value) {
        tokenString_ProgramType = value;
    }
    public String getProgramType() {
        return tokenString_ProgramType;
    }


    // Declared in FortranIR.ast line 4
    private String tokenString_ProgramName;
    public void setProgramName(String value) {
        tokenString_ProgramName = value;
    }
    public String getProgramName() {
        return tokenString_ProgramName;
    }


    // Declared in FortranIR.ast line 4
    public void setProgramParameterListOpt(Opt opt) {
        setChild(opt, 0);
    }

    public boolean hasProgramParameterList() {
        return getProgramParameterListOpt().getNumChild() != 0;
    }

    public ProgramParameterList getProgramParameterList() {
        return (ProgramParameterList)getProgramParameterListOpt().getChild(0);
    }

    public void setProgramParameterList(ProgramParameterList node) {
        getProgramParameterListOpt().setChild(node, 0);
    }
    public Opt getProgramParameterListOpt() {
        return (Opt)getChild(0);
    }

    public Opt getProgramParameterListOptNoTransform() {
        return (Opt)getChildNoTransform(0);
    }


    // Declared in PrettyPrinter.jadd at line 8

	public void pp() {
	    System.out.print(getProgramType()+" "+getProgramName()+"(");
	    getProgramParameterList().pp();
	    System.out.println(")");
	    System.out.println("implict none");
	}

}
