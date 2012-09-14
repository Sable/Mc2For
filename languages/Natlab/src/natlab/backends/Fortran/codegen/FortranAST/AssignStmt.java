package natlab.backends.Fortran.codegen.FortranAST;


public class AssignStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 18

    public AssignStmt() {
        super();

        setChild(null, 0);
        setChild(null, 1);
    }

    // Declared in FortranIR.ast line 18
    public AssignStmt(String p0, Variable p1, Exp p2) {
        setRuntimeCheck(p0);
        setChild(p1, 0);
        setChild(p2, 1);
    }

    public Object clone() throws CloneNotSupportedException {
        AssignStmt node = (AssignStmt)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          AssignStmt node = (AssignStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        AssignStmt res = (AssignStmt)copy();
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
    // Declared in FortranIR.ast line 18
    private String tokenString_RuntimeCheck;
    public void setRuntimeCheck(String value) {
        tokenString_RuntimeCheck = value;
    }
    public String getRuntimeCheck() {
        return tokenString_RuntimeCheck;
    }


    // Declared in FortranIR.ast line 18
    public void setVariable(Variable node) {
        setChild(node, 0);
    }
    public Variable getVariable() {
        return (Variable)getChild(0);
    }

    public Variable getVariableNoTransform() {
        return (Variable)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 18
    public void setExp(Exp node) {
        setChild(node, 1);
    }
    public Exp getExp() {
        return (Exp)getChild(1);
    }

    public Exp getExpNoTransform() {
        return (Exp)getChildNoTransform(1);
    }


}
