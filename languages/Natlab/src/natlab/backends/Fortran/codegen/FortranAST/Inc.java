package natlab.backends.Fortran.codegen.FortranAST;


public class Inc extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 41

    public Inc() {
        super();

    }

    // Declared in FortranIR.ast line 41
    public Inc(String p0) {
        setName(p0);
    }

    public Object clone() throws CloneNotSupportedException {
        Inc node = (Inc)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          Inc node = (Inc)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        Inc res = (Inc)copy();
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
    // Declared in FortranIR.ast line 41
    private String tokenString_Name;
    public void setName(String value) {
        tokenString_Name = value;
    }
    public String getName() {
        return tokenString_Name;
    }


    // Declared in PrettyPrinter.jadd at line 192

    public void pp() {
    	System.out.print(getName());
    }

}
