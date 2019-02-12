package constant

import io.ebean.Expr
import io.ebean.Expression

import scala.annotation.tailrec

object PLMWorkflowDisplayFilter {
  val workflows = Seq("ngsRun")

  lazy val condition: Expression = work(workflows.slice(2, workflows.size),
    Expr.or(Expr.eq("WORKFLOW", workflows.head), Expr.eq("WORKFLOW", workflows(1))))

  @tailrec
  private def work(workflows: Seq[String], exp: Expression): Expression = {
    if (workflows.isEmpty)
      exp
    else if (workflows.size == 1)
      Expr.or(Expr.eq("WORKFLOW", workflows.head), exp)
    else
      work(workflows.slice(1, workflows.size - 1), Expr.or(Expr.eq("WORKFLOW", workflows.head), exp))
  }


}
