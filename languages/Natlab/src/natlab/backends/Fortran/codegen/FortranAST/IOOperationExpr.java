package natlab.backends.Fortran.codegen.FortranAST;


public class IOOperationExpr extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 30

    public IOOperationExpr() {
        super();

        setChild(null, 0);
    }

    // Declared in FortranIR.ast line 30
    public IOOperationExpr(String p0, Variable p1) {
        setIOOperation(p0);
        setChild(p1, 0);
    }

    public Object clone() throws CloneNotSupportedException {
        IOOperationExpr node = (IOOperationExpr)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          IOOperationExpr node = (IOOperationExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        IOOperationExpr res = (IOOperationExpr)copy();
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
    // Declared in FortranIR.ast line 30
    private String tokenString_IOOperation;
    public void setIOOperation(String value) {
        tokenString_IOOperation = value;
    }
    public String getIOOperation() {
        return tokenString_IOOperation;
    }


    // Declared in FortranIR.ast line 30
    public void setVariable(Variable node) {
        setChild(node, 0);
    }
    public Variable getVariable() {
        return (Variable)getChild(0);
    }

    public Variable getVariableNoTransform() {
        return (Variable)getChildNoTransform(0);
    }


}
