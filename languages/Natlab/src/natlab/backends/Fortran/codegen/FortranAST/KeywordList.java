package natlab.backends.Fortran.codegen.FortranAST;
public class KeywordList extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 13

    public KeywordList() {
        super();

        setChild(new List(), 0);
    }

    // Declared in FortranIR.ast line 13
    public KeywordList(List p0) {
        setChild(p0, 0);
    }

    public Object clone() throws CloneNotSupportedException {
        KeywordList node = (KeywordList)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          KeywordList node = (KeywordList)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        KeywordList res = (KeywordList)copy();
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
    // Declared in FortranIR.ast line 13
    public void setKeywordList(List list) {
        setChild(list, 0);
    }

    public int getNumKeyword() {
        return getKeywordList().getNumChild();
    }

    public Keyword getKeyword(int i) {
        return (Keyword)getKeywordList().getChild(i);
    }

    public void addKeyword(Keyword node) {
        List list = getKeywordList();
        list.setChild(node, list.getNumChild());
    }

    public void setKeyword(Keyword node, int i) {
        List list = getKeywordList();
        list.setChild(node, i);
    }
    public List getKeywordList() {
        return (List)getChild(0);
    }

    public List getKeywordListNoTransform() {
        return (List)getChildNoTransform(0);
    }


}
