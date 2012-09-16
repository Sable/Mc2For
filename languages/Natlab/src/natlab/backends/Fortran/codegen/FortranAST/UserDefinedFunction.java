package natlab.backends.Fortran.codegen.FortranAST;


public class UserDefinedFunction extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 31

    public UserDefinedFunction() {
        super();

    }

    // Declared in FortranIR.ast line 31
    public UserDefinedFunction(String p0, String p1) {
        setFuncName(p0);
        setArgsList(p1);
    }

    public Object clone() throws CloneNotSupportedException {
        UserDefinedFunction node = (UserDefinedFunction)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          UserDefinedFunction node = (UserDefinedFunction)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        UserDefinedFunction res = (UserDefinedFunction)copy();
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
    // Declared in FortranIR.ast line 31
    private String tokenString_FuncName;
    public void setFuncName(String value) {
        tokenString_FuncName = value;
    }
    public String getFuncName() {
        return tokenString_FuncName;
    }


    // Declared in FortranIR.ast line 31
    private String tokenString_ArgsList;
    public void setArgsList(String value) {
        tokenString_ArgsList = value;
    }
    public String getArgsList() {
        return tokenString_ArgsList;
    }


}
