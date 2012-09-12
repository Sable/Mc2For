package natlab.backends.Fortran.codegen.FortranAST;


public class BinaryOperation extends Exp implements Cloneable {
    // Declared in FortranIR.ast line 23

    public BinaryOperation() {
        super();

        setChild(null, 0);
        setChild(null, 1);
    }

    // Declared in FortranIR.ast line 23
    public BinaryOperation(LHSVariable p0, String p1, RHSVariable p2) {
        setChild(p0, 0);
        setOperation(p1);
        setChild(p2, 1);
    }

    public Object clone() throws CloneNotSupportedException {
        BinaryOperation node = (BinaryOperation)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          BinaryOperation node = (BinaryOperation)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        BinaryOperation res = (BinaryOperation)copy();
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
    // Declared in FortranIR.ast line 23
    public void setLHSVariable(LHSVariable node) {
        setChild(node, 0);
    }
    public LHSVariable getLHSVariable() {
        return (LHSVariable)getChild(0);
    }

    public LHSVariable getLHSVariableNoTransform() {
        return (LHSVariable)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 23
    private String tokenString_Operation;
    public void setOperation(String value) {
        tokenString_Operation = value;
    }
    public String getOperation() {
        return tokenString_Operation;
    }


    // Declared in FortranIR.ast line 23
    public void setRHSVariable(RHSVariable node) {
        setChild(node, 1);
    }
    public RHSVariable getRHSVariable() {
        return (RHSVariable)getChild(1);
    }

    public RHSVariable getRHSVariableNoTransform() {
        return (RHSVariable)getChildNoTransform(1);
    }


}
