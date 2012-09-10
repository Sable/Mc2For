package FortranAST;
public class DeclarationSection extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 6

    public DeclarationSection() {
        super();

        setChild(new List(), 0);
    }

    // Declared in FortranIR.ast line 6
    public DeclarationSection(List p0) {
        setChild(p0, 0);
    }

    public Object clone() throws CloneNotSupportedException {
        DeclarationSection node = (DeclarationSection)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          DeclarationSection node = (DeclarationSection)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        DeclarationSection res = (DeclarationSection)copy();
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
    // Declared in FortranIR.ast line 6
    public void setDeclStmtList(List list) {
        setChild(list, 0);
    }

    public int getNumDeclStmt() {
        return getDeclStmtList().getNumChild();
    }

    public DeclStmt getDeclStmt(int i) {
        return (DeclStmt)getDeclStmtList().getChild(i);
    }

    public void addDeclStmt(DeclStmt node) {
        List list = getDeclStmtList();
        list.setChild(node, list.getNumChild());
    }

    public void setDeclStmt(DeclStmt node, int i) {
        List list = getDeclStmtList();
        list.setChild(node, i);
    }
    public List getDeclStmtList() {
        return (List)getChild(0);
    }

    public List getDeclStmtListNoTransform() {
        return (List)getChildNoTransform(0);
    }


}
