package natlab.backends.Fortran.codegen.FortranAST;


public class AbstractAssignToVarStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 22

    public AbstractAssignToVarStmt() {
        super();

        setChild(new Opt(), 0);
    }

    // Declared in FortranIR.ast line 22
    public AbstractAssignToVarStmt(Opt p0, String p1, String p2) {
        setChild(p0, 0);
        setTargetVariable(p1);
        setSourceVariable(p2);
    }

    public Object clone() throws CloneNotSupportedException {
        AbstractAssignToVarStmt node = (AbstractAssignToVarStmt)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          AbstractAssignToVarStmt node = (AbstractAssignToVarStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        AbstractAssignToVarStmt res = (AbstractAssignToVarStmt)copy();
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
    // Declared in FortranIR.ast line 22
    public void setRuntimeCheckOpt(Opt opt) {
        setChild(opt, 0);
    }

    public boolean hasRuntimeCheck() {
        return getRuntimeCheckOpt().getNumChild() != 0;
    }

    public RuntimeCheck getRuntimeCheck() {
        return (RuntimeCheck)getRuntimeCheckOpt().getChild(0);
    }

    public void setRuntimeCheck(RuntimeCheck node) {
        getRuntimeCheckOpt().setChild(node, 0);
    }
    public Opt getRuntimeCheckOpt() {
        return (Opt)getChild(0);
    }

    public Opt getRuntimeCheckOptNoTransform() {
        return (Opt)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 22
    private String tokenString_TargetVariable;
    public void setTargetVariable(String value) {
        tokenString_TargetVariable = value;
    }
    public String getTargetVariable() {
        return tokenString_TargetVariable;
    }


    // Declared in FortranIR.ast line 22
    private String tokenString_SourceVariable;
    public void setSourceVariable(String value) {
        tokenString_SourceVariable = value;
    }
    public String getSourceVariable() {
        return tokenString_SourceVariable;
    }


    // Declared in PrettyPrinter.jadd at line 92

    public void pp() {
    	if(hasRuntimeCheck()) {
    		System.out.println(getRuntimeCheck());
    	}
    	System.out.print(getTargetVariable()+" = "+getSourceVariable()+";");
    }

}
