package natlab.backends.Fortran.codegen.FortranAST;


public class ArrayGetStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 43

    public ArrayGetStmt() {
        super();

        setChild(new Opt(), 0);
    }

    // Declared in FortranIR.ast line 43
    public ArrayGetStmt(String p0, Opt p1, String p2, String p3) {
        setlhsVariable(p0);
        setChild(p1, 0);
        setrhsVariable(p2);
        setrhsIndex(p3);
    }

    public Object clone() throws CloneNotSupportedException {
        ArrayGetStmt node = (ArrayGetStmt)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          ArrayGetStmt node = (ArrayGetStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ArrayGetStmt res = (ArrayGetStmt)copy();
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
    // Declared in FortranIR.ast line 43
    private String tokenString_lhsVariable;
    public void setlhsVariable(String value) {
        tokenString_lhsVariable = value;
    }
    public String getlhsVariable() {
        return tokenString_lhsVariable;
    }


    // Declared in FortranIR.ast line 43
    public void setlhsIndexOpt(Opt opt) {
        setChild(opt, 0);
    }

    public boolean haslhsIndex() {
        return getlhsIndexOpt().getNumChild() != 0;
    }

    public lhsIndex getlhsIndex() {
        return (lhsIndex)getlhsIndexOpt().getChild(0);
    }

    public void setlhsIndex(lhsIndex node) {
        getlhsIndexOpt().setChild(node, 0);
    }
    public Opt getlhsIndexOpt() {
        return (Opt)getChild(0);
    }

    public Opt getlhsIndexOptNoTransform() {
        return (Opt)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 43
    private String tokenString_rhsVariable;
    public void setrhsVariable(String value) {
        tokenString_rhsVariable = value;
    }
    public String getrhsVariable() {
        return tokenString_rhsVariable;
    }


    // Declared in FortranIR.ast line 43
    private String tokenString_rhsIndex;
    public void setrhsIndex(String value) {
        tokenString_rhsIndex = value;
    }
    public String getrhsIndex() {
        return tokenString_rhsIndex;
    }


    // Declared in PrettyPrinter.jadd at line 195

    public void pp() {
    	System.out.print(getlhsVariable());
    	if(haslhsIndex()) {
    		System.out.print("(");
    		getlhsIndex().pp();
    		System.out.print(")");
    	}
    	System.out.print(" = "+getrhsVariable()+"("+getrhsIndex()+");");
    }

}
