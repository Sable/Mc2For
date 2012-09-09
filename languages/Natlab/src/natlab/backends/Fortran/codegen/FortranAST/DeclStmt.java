package natlab.backends.Fortran.codegen.FortranAST;
public class DeclStmt extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 7

    public DeclStmt() {
        super();

        setChild(new Opt(), 0);
        setChild(new Opt(), 1);
        setChild(null, 2);
    }

    // Declared in FortranIR.ast line 7
    public DeclStmt(String p0, Opt p1, Opt p2, VariableList p3) {
        setType(p0);
        setChild(p1, 0);
        setChild(p2, 1);
        setChild(p3, 2);
    }

    public Object clone() throws CloneNotSupportedException {
        DeclStmt node = (DeclStmt)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          DeclStmt node = (DeclStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        DeclStmt res = (DeclStmt)copy();
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
    return 3;
  }
    // Declared in FortranIR.ast line 7
    private String tokenString_Type;
    public void setType(String value) {
        tokenString_Type = value;
    }
    public String getType() {
        return tokenString_Type;
    }


    // Declared in FortranIR.ast line 7
    public void setKeywordListOpt(Opt opt) {
        setChild(opt, 0);
    }

    public boolean hasKeywordList() {
        return getKeywordListOpt().getNumChild() != 0;
    }

    public KeywordList getKeywordList() {
        return (KeywordList)getKeywordListOpt().getChild(0);
    }

    public void setKeywordList(KeywordList node) {
        getKeywordListOpt().setChild(node, 0);
    }
    public Opt getKeywordListOpt() {
        return (Opt)getChild(0);
    }

    public Opt getKeywordListOptNoTransform() {
        return (Opt)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 7
    public void setShapeInfoOpt(Opt opt) {
        setChild(opt, 1);
    }

    public boolean hasShapeInfo() {
        return getShapeInfoOpt().getNumChild() != 0;
    }

    public ShapeInfo getShapeInfo() {
        return (ShapeInfo)getShapeInfoOpt().getChild(0);
    }

    public void setShapeInfo(ShapeInfo node) {
        getShapeInfoOpt().setChild(node, 0);
    }
    public Opt getShapeInfoOpt() {
        return (Opt)getChild(1);
    }

    public Opt getShapeInfoOptNoTransform() {
        return (Opt)getChildNoTransform(1);
    }


    // Declared in FortranIR.ast line 7
    public void setVariableList(VariableList node) {
        setChild(node, 2);
    }
    public VariableList getVariableList() {
        return (VariableList)getChild(2);
    }

    public VariableList getVariableListNoTransform() {
        return (VariableList)getChildNoTransform(2);
    }


}
