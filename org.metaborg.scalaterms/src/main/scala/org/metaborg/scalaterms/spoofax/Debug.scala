package org.metaborg.scalaterms.spoofax

import org.strategoxt.lang.Context

/**
  * Created by jeff on 05/12/16.
  */
object Debug {
  def print(msg: Any)(implicit context: Context) = context.getIOAgent.printError(msg.toString)
}
