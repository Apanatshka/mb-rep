package org.metaborg.scalaterms

import org.spoofax.interpreter.terms.IStrategoTerm
import org.spoofax.jsglr.client.imploder.ImploderAttachment

/**
  * Scala representation of the Imploder Attachment on ATerms for the origin information (ImploderAttachment)
  */
class Origin(val filename: String, val line: Int, val column: Int, val startOffset: Int, val endOffset: Int) {
  def toStratego: ImploderAttachment = {
    ImploderAttachment.createCompactPositionAttachment(filename, line, column, startOffset, endOffset)
  }

  def zero: Origin = new Origin(filename, 0, 0, 0, 0)

  def canEqual(other: Any): Boolean = other.isInstanceOf[Origin]

  override def equals(other: Any): Boolean = other match {
    case that: Origin =>
      (that canEqual this) &&
        filename == that.filename &&
        line == that.line &&
        column == that.column &&
        startOffset == that.startOffset &&
        endOffset == that.endOffset
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(filename, line, column, startOffset, endOffset)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Origin($filename, $line, $column, $startOffset, $endOffset)"
}

object Origin {
  def fromStratego(term: IStrategoTerm): Option[Origin] = {
    Option(ImploderAttachment.getCompactPositionAttachment(term, false)).map {
      o => new Origin(filename = o.getLeftToken.getTokenizer.getFilename,
                      line = o.getLeftToken.getLine,
                      column = o.getLeftToken.getColumn,
                      startOffset = o.getLeftToken.getStartOffset,
                      endOffset = o.getRightToken.getEndOffset)
    }
  }
}
