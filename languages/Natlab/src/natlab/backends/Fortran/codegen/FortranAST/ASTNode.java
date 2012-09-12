package natlab.backends.Fortran.codegen.FortranAST;



// Generated with JastAdd II (http://jastadd.cs.lth.se) version R20060915

public class ASTNode implements Cloneable {
    // Declared in null line 0

    public ASTNode() {
        super();

    }

    public Object clone() throws CloneNotSupportedException {
        ASTNode node = (ASTNode)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          ASTNode node = (ASTNode)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ASTNode res = (ASTNode)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    public void flushCache() {
    }
   static public boolean generatedWithCircularEnabled = true;
   static public boolean generatedWithCacheCycle = true;
   static public boolean generatedWithComponentCheck = true;
  static public boolean IN_CIRCLE = false;
  static public boolean CHANGE = false;
  static public boolean LAST_CYCLE = false;
  static public java.util.Set circularEvalSet = new java.util.HashSet();
  static public java.util.Stack circularEvalStack = new java.util.Stack();
  static class CircularEvalEntry {
  	 ASTNode node;
  	 String attrName;
  	 Object parameters;
  	 public CircularEvalEntry(ASTNode node, String attrName, Object parameters) {
  	   this.node = node;
   	 this.attrName = attrName;
  		 this.parameters = parameters;
  	 }
  	 public boolean equals(Object rhs) {
  	   CircularEvalEntry s = (CircularEvalEntry) rhs;
  		 if (parameters == null && s.parameters == null)
  			 return node == s.node && attrName.equals(s.attrName);
  		 else if (parameters != null && s.parameters != null)
  			 return node == s.node && attrName.equals(s.attrName) && parameters.equals(s.parameters);
  		 else
  			 return false;
  	 }
  	 public int hashCode() {
  		 return node.hashCode();
  	 }
  }
  public void addEvalEntry(ASTNode node, String attrName, Object parameters) {
    circularEvalSet.add(new CircularEvalEntry(node,attrName,parameters));
  }
  public boolean containsEvalEntry(ASTNode node, String attrName, Object parameters) {
    return circularEvalSet.contains(new CircularEvalEntry(node,attrName,parameters));
  }
  static class CircularStackEntry {
    java.util.Set circularEvalSet;
  	 boolean changeValue;
  	 public CircularStackEntry(java.util.Set set, boolean change) {
  		 circularEvalSet = set;
  		 changeValue = change;
  	 }
  }
  public void pushEvalStack() {
  	 circularEvalStack.push(new CircularStackEntry(circularEvalSet, CHANGE));
  	 circularEvalSet = new java.util.HashSet();
  	 CHANGE = false;
  }
  public void popEvalStack() {
  	 CircularStackEntry c = (CircularStackEntry) circularEvalStack.pop();
  	 circularEvalSet = c.circularEvalSet;
  	 CHANGE = c.changeValue;
  }
  public ASTNode getChild(int i) {
    return getChildNoTransform(i);
  }
  private int childIndex;
  public int getIndexOfChild(ASTNode node) {
    if(node.childIndex < getNumChild() && node == getChildNoTransform(node.childIndex))
      return node.childIndex;
    for(int i = 0; i < getNumChild(); i++)
      if(getChildNoTransform(i) == node) {
        node.childIndex = i;
        return i;
      }
    return -1;
  }

  public void addChild(ASTNode node) {
    setChild(node, getNumChild());
  }
  public ASTNode getChildNoTransform(int i) {
    return children[i];
  }
  protected ASTNode parent;
  protected ASTNode[] children;
  protected int numChildren;
  protected int numChildren() {
    return numChildren;
  }
  public int getNumChild() {
    return numChildren();
  }
  public void setChild(ASTNode node, int i) {
    if(children == null) {
      children = new ASTNode[i + 1];
    } else if (i >= children.length) {
      ASTNode c[] = new ASTNode[i << 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = node;
    if(i >= numChildren) numChildren = i+1;
    if(node != null) { node.setParent(this); node.childIndex = i; }
  }
  public void insertChild(ASTNode node, int i) {
    if(children == null) {
      children = new ASTNode[i + 1];
      children[i] = node;
    } else {
      ASTNode c[] = new ASTNode[children.length + 1];
      System.arraycopy(children, 0, c, 0, i);
      c[i] = node;
      if(i < children.length)
        System.arraycopy(children, i, c, i+1, children.length-i);
      children = c;
    }
    numChildren++;
    if(node != null) { node.setParent(this); node.childIndex = i; }
  }
  public ASTNode getParent() {
    return parent;
  }
  public void setParent(ASTNode node) {
    parent = node;
  }
    // Declared in PrettyPrinter.jadd at line 2

   public void pp() {}

}
