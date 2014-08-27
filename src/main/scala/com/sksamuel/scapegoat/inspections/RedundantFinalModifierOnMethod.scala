package com.sksamuel.scapegoat.inspections

import scala.reflect.internal.Flags

import com.sksamuel.scapegoat.{ Inspection, InspectionContext, Inspector, Levels }

class RedundantFinalModifierOnMethod extends Inspection {

  override def inspector(context: InspectionContext): Inspector = new Inspector(context) {

    import context.global._

    override def postTyperTraverser = Some apply new context.Traverser {

      override def inspect(tree: Tree): Unit = {
        tree match {
          case dd: DefDef if dd.symbol != null && dd.symbol.isSynthetic    =>
          case DefDef(mods, _, _, _, _, _) if mods.hasFlag(Flags.ACCESSOR) =>
          case DefDef(_, nme.CONSTRUCTOR, _, _, _, _)                      =>
          case dd @ DefDef(mods, _, _, _, _, _) if mods.isFinal &&
            (tree.symbol.enclClass.isFinal ||
              tree.symbol.enclClass.isCase ||
              tree.symbol.enclClass.isModuleOrModuleClass ||
              tree.symbol.enclClass.isPackageObjectOrClass) =>
            context.warn("Redundant final modifier on method",
              tree.pos,
              Levels.Info,
              "Method cannot be overriden, final modifer is redundant",
              RedundantFinalModifierOnMethod.this)
          case _ => continue(tree)
        }
      }
    }
  }
}
