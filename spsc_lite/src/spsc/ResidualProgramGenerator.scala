package spsc

import Util._
import scala.collection.jcl.LinkedHashSet

class ResidualProgramGenerator(val tree: Tree) {
  import ResidualProgramGenerator._
  
  private def walk(node: Node): Term = 
    if (node.repeated != null) {
      val sign = signatures(node.repeated)
      if (node.repeated.outs.size == 1)
        FCall(sign.name, sign.args.map(sub(_, Util.findSub(node.repeated.expr, node.expr))))
      else
        GCall(sign.name, sign.args.map(sub(_, Util.findSub(node.repeated.expr, node.expr))))
    } else node.expr match {
      case v: Var => v
      case Cons(name, args) => Cons(name, node.outs.map(e => walk(e.child)))
      case Let(term, bs) =>
        val s = Map(bs.map{_._1} zip node.outs.tail.map(e => walk(e.child)) :_*)
        sub(walk(node.outs.head.child), s)
      case call : Call => 
        if (node.outs.head.branch == null) {
          tree.leafs.find(_.repeated == node) match {
            case None => walk(node.outs.head.child)
            case Some(fc1) => {
              val newName = if (node == tree.root) call.f else rename(call.f, node == tree.root)
              val signature = Signature(newName, getVars(call).toList)
              signatures(node) = signature
              val body = walk(node.outs.head.child)
              defs += FFun(signature.name, signature.args, body)
              body
            }
          }
        } else {
          val patternVar = node.outs.head.branch._1
          val vars = (getVars(call) - patternVar).toList
          val signature = Signature(rename(call.f, false), patternVar :: vars)
          signatures(node) = signature
          for (edge <- node.outs){
            val pat = edge.branch._2
            defs += GFun(signature.name, pat, vars, walk(edge.child))
          }
          GCall(signature.name, patternVar :: vars)
      }
  }
  
  private var signatures = scala.collection.mutable.Map[Node, Signature]()
  private val defs = new scala.collection.mutable.ListBuffer[Def]
  private val fnames = scala.collection.mutable.Set[String]()
  private val rootName = tree.root.expr.asInstanceOf[FCall].name
  
  private def generateProgram(): Program = {
    val t = walk(tree.root)
    val rootCall = tree.root.expr.asInstanceOf[FCall]
    signatures.get(tree.root) match {
      case None => defs += FFun(rootCall.name, rootCall.args.map(_.asInstanceOf[Var]), t)
      case _ =>
    }    
    val newDefs = new scala.collection.mutable.ListBuffer[Def]
    for (d <- defs) {
      newDefs += renameVarsInDefinition(d)
    }
    Program(newDefs.toList.sort((e1, e2) => (e1.name compareTo e2.name) < 0))
  }
  
  private def getVars(t: Term): LinkedHashSet[Var] = {
    val vars = new LinkedHashSet[Var]()
    t match {
      case v: Var => vars + v
      case c: Cons => c.args.map(arg => {vars ++ getVars(arg)})
      case f: FCall => f.args.map(arg => {vars ++ getVars(arg)})
      case g: GCall => (g.args).map(arg => {vars ++ getVars(arg)})
    }
    vars
  }
  
  private def rename(name: String, isOriginalNameAllowed: Boolean) = {
    if (isOriginalNameAllowed && !fnames.contains(name)){
      fnames += name
      name
    } else {
      var index = 1
      var newName = name + index
      while(rootName==newName || fnames.contains(newName)){
        index += 1
        newName = name + index
      }
      fnames += newName
      newName
    }
  }
}

object ResidualProgramGenerator{
  case class Signature(name: String, args: List[Var])
  def generateResidualProgram(tree: Tree) = new ResidualProgramGenerator(tree).generateProgram()
  val letters = "abcdefghijklmnopqrstuvwxyz".toArray
  def getVar(n: Int): Var = Var(new String(letters.slice(n, n+1)))
  def renameVarsInDefinition(d: Def) = d match {
    case f: FFun =>
      val args = f.args
      val renaming = Map() ++ ((args) zip (args.indices.map(getVar(_)))) 
      FFun(f.name, f.args.map(renaming(_)), sub(f.term, renaming))
    case g: GFun =>
      val args = g.p.args ::: g.args
      val renaming = Map() ++ ((args) zip (args.indices.map(getVar(_))))
      GFun(g.name, Pattern(g.p.name, g.p.args.map(renaming(_))), g.args.map(renaming(_)), sub(g.term, renaming))    
  }
}
