package natlab.backends.Fortran.codegen.FortranAST;


public class AbstractAssignToVarStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 21

    public AbstractAssignToVarStmt() {
        super();

    }

    // Declared in FortranIR.ast line 21
    public AbstractAssignToVarStmt(String p0, String p1, String p2) {
        setRuntimeCheck(p0);
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
    return 0;
  }
    // Declared in FortranIR.ast line 21
    private String tokenString_RuntimeCheck;
    public void setRuntimeCheck(String value) {
        tokenString_RuntimeCheck = value;
    }
    public String getRuntimeCheck() {
        return tokenString_RuntimeCheck;
    }


    // Declared in FortranIR.ast line 21
    private String tokenString_TargetVariable;
    public void setTargetVariable(String value) {
        tokenString_TargetVariable = value;
    }
    public String getTargetVariable() {
        return tokenString_TargetVariable;
    }


    // Declared in FortranIR.ast line 21
    private String tokenString_SourceVariable;
    public void setSourceVariable(String value) {
        tokenString_SourceVariable = value;
    }
    public String getSourceVariable() {
        return tokenString_SourceVariable;
    }


}
