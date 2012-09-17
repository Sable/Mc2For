package natlab.backends.Fortran.codegen.FortranAST;


public class AssignLiteralStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 19

    public AssignLiteralStmt() {
        super();

        setChild(new Opt(), 0);
        setChild(null, 1);
    }

    // Declared in FortranIR.ast line 19
    public AssignLiteralStmt(Opt p0, Variable p1, String p2) {
        setChild(p0, 0);
        setChild(p1, 1);
        setLiteral(p2);
    }

    public Object clone() throws CloneNotSupportedException {
        AssignLiteralStmt node = (AssignLiteralStmt)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          AssignLiteralStmt node = (AssignLiteralStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        AssignLiteralStmt res = (AssignLiteralStmt)copy();
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
    return 2;
  }
    // Declared in FortranIR.ast line 19
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


    // Declared in FortranIR.ast line 19
    public void setVariable(Variable node) {
        setChild(node, 1);
    }
    public Variable getVariable() {
        return (Variable)getChild(1);
    }

    public Variable getVariableNoTransform() {
        return (Variable)getChildNoTransform(1);
    }


    // Declared in FortranIR.ast line 19
    private String tokenString_Literal;
    public void setLiteral(String value) {
        tokenString_Literal = value;
    }
    public String getLiteral() {
        return tokenString_Literal;
    }


    // Declared in PrettyPrinter.jadd at line 85

    public void pp() {
    	if(hasRuntimeCheck()) {
    		System.out.println(getRuntimeCheck());
    	}
    	getVariable().pp();
    	System.out.print(" = "+getLiteral()+";");
    }

}
