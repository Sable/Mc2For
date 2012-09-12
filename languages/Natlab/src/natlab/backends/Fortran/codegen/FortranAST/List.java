package natlab.backends.Fortran.codegen.FortranAST;


public class List extends ASTNode implements Cloneable {
    // Declared in null line 0

    public List() {
        super();

    }

    public Object clone() throws CloneNotSupportedException {
        List node = (List)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          List node = (List)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        List res = (List)copy();
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
     public List add(ASTNode node) {
          addChild(node);
          return this;
     }

}
