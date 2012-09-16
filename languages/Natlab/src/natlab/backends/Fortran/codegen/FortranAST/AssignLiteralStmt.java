package natlab.backends.Fortran.codegen.FortranAST;


public class AssignLiteralStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 19

    public AssignLiteralStmt() {
        super();

        setChild(null, 0);
    }

    // Declared in FortranIR.ast line 19
    public AssignLiteralStmt(String p0, Variable p1, String p2) {
        setRuntimeCheck(p0);
        setChild(p1, 0);
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
    return 1;
  }
    // Declared in FortranIR.ast line 19
    private String tokenString_RuntimeCheck;
    public void setRuntimeCheck(String value) {
        tokenString_RuntimeCheck = value;
    }
    public String getRuntimeCheck() {
        return tokenString_RuntimeCheck;
    }


    // Declared in FortranIR.ast line 19
    public void setVariable(Variable node) {
        setChild(node, 0);
    }
    public Variable getVariable() {
        return (Variable)getChild(0);
    }

    public Variable getVariableNoTransform() {
        return (Variable)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 19
    private String tokenString_Literal;
    public void setLiteral(String value) {
        tokenString_Literal = value;
    }
    public String getLiteral() {
        return tokenString_Literal;
    }


}
