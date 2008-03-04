package spsc;

import SmallLanguage._
import SmallLanguageTermAlgebra._

object ProcessTree {
  def apply(expr: Expression) = 
    new ProcessTree(new Node(expr, null, Nil))
  
  class Node(val expr: Expression, val in: Edge, var outs: List[Edge]) {
    override def toString = toString("")
      
    def toString(indent: String): String = {
      val sb = new StringBuilder(indent + "|__" + expr)
      for (edge <- outs) {
        sb.append("\n  " + indent + "|" + edge.substitution.toList.map(kv => kv._1 + "=" + kv._2).mkString("", ", ", ""))
        sb.append("\n" + edge.child.toString(indent + "  "))
      }
      sb.toString
    }
      
    def toSVG(trX: Int, trY: Int): scala.xml.NodeBuffer = {
      def childrenToSVG() = {
        val children = new scala.xml.NodeBuffer
        var trChX = (width - childrenWidth)/2
        for (out<-outs) {
          val child = out.child
          children += 
            <line x1={"" + (trX+width/2)} y1={""+(trY+30)} x2={"" + (trX+trChX+child.width/2)} y2={""+(trY+100)}/>
          children +=
            <text x={"" + (trX + trChX + child.width/2)} y = {"" + (80 + trY)}>{out.substitution.toList.map(kv => kv._1 + "=" + kv._2).mkString("", ", ", "")}</text>
          children ++= child.toSVG(trX + trChX, trY + 100)
          trChX += child.width
        }
        children
      }
    <rect x={"" + (trX + (width - rectWidth)/2)} y={"" + trY} width={"" + rectWidth} height="30" />
    <text x={"" + (trX + width/2)} y ={"" + (trY + 15)}>{expr.toString}</text> 
    &+ childrenToSVG
    }
    
    
    
      
    lazy val width: Int = {
      val myWidth = rectWidth + 40
      Math.max(myWidth, childrenWidth)
    }
    
    lazy val height: Int = {
      var childrenHeight = 0
      for (out <- outs) childrenHeight = Math.max(out.child.height, childrenHeight)
      if (childrenHeight > 0) childrenHeight + 100 else 30
    }
    
    lazy val childrenWidth: Int = {
      var childrenWidth = 0
      for (out <- outs){
        childrenWidth += out.child.width
      }
      childrenWidth
    }
    
    
    lazy val rectWidth: Int = expr.toString.length*6 + 10
    

    def ancestors(): List[Node] = if (in == null) Nil else in.parent :: in.parent.ancestors

    def isProcessed: Boolean = expr match {
      case Constructor(_, Nil) => true
      case v : Variable => true
      case l: LetExpression => false
      case _ => {
        var edge = in
        while (edge != null) {
          val node1 = edge.parent
          if (!isTrivial(node1.expr) && equivalent(expr.asInstanceOf[Term], node1.expr.asInstanceOf[Term])) return true
          edge = node1.in
        }
        false
      }
    }
  }
  
  class Edge(val parent: Node, var child: Node, val substitution: Map[Variable, Term]) {
    override def toString = "Edge("+ substitution + ", " + child + ")"
  }
}

import ProcessTree._
class ProcessTree {
  var rootNode: Node = null
  private var leafs_ = List[Node]()
  
  def this(root: Node) {
   this()
   rootNode = root
   leafs_ = List[Node](rootNode)
  }
  
  def leafs = leafs_
  
  def addChildren(node: Node, children: List[Pair[Term, Map[Variable, Term]]]) = {
    assume(leafs_.contains(node))
    leafs_ = leafs_.remove(_ == node)
    val edges = new scala.collection.mutable.ListBuffer[Edge]
    for (pair <- children){
      val edge = new Edge(node, null, pair._2)
      val childNode = new Node(pair._1, edge, Nil)
      leafs_ = childNode :: leafs
      edge.child = childNode
      edges += edge
    }
    node.outs = edges.toList
  }
  
  def replace(node: Node, exp: Expression) = {
    // the node can be not leaf - but from any part of tree
    leafs_ = leafs_.remove(_ == node)
    leafs_ = leafs_.remove(_.ancestors.contains(node))
    val childNode = new Node(exp, node.in, Nil)
    // the node can be root node:
    if (node == rootNode){
      rootNode = childNode
    } else {
      node.in.child = childNode
    }
    leafs_ = childNode :: leafs
  }
  
  def isClosed = leafs_.forall(_.isProcessed)
  
  override def toString = rootNode.toString
  
  def toSVG = 
   <svg xmlns="http://www.w3.org/2000/svg" width={"" + rootNode.width} height={"" + rootNode.height} >
   <defs>
   <style type="text/css">
   <![CDATA[
   rect {fill: none;stroke: black; stroke-width: 1;}
   text {text-anchor: middle; font-family: monospace; font-size: 10px;}
   line {stroke: black; stroke-width: 1}]]></style>
   </defs>
   {rootNode.toSVG(0, 0)}
   </svg>
}

