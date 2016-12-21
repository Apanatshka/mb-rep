package org.metaborg.scalaterms

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import org.metaborg.scalaterms.STerm.{Cons, Int => SInt, List => SList, Real, Tuple, String => SString}

object PrettyPrinter {
  def pp(term: TermLike): String = pp(term.toSTerm)

  private def pp(level: Int)(term: STerm): String = term match {
    case SInt(value, origin) => whitespace(level) + value.toString
    case Real(value, origin) => whitespace(level) + value.toString
    case SString(value, origin) => whitespace(level) + value.toString
    case SList(value, origin) => ppSeq(level, "[", value.map(_.toSTerm), "]")
    case Tuple(value, origin) => ppSeq(level, "(", value, ")")
    case Cons(value, children, origin) => ppSeq(level, s"$value(", children, ")")
  }

  private def ppSeq(level: Int, prefix: String, seq: Seq[STerm], suffix: String): String =
    whitespace(level) +
    (seq.length match {
      case 0 => prefix + suffix
      case _ =>
        s"""|$prefix
            |${seq.map(pp(level + 1)).mkString(",\n")}
            |${whitespace(level)}$suffix""".stripMargin
    })


  private def whitespace(level: Int) = " " * (level * 2)

  def pp(term: STerm): String = pp(0)(term)
}
