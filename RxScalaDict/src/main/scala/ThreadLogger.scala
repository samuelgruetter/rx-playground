import javax.swing.SwingUtilities

object ThreadLogger {
  
  def uiThreadStr: String = {
    if (SwingUtilities.isEventDispatchThread())
      "(UI thread)"
    else
      "(not UI thread)"
  }
  
  def log(whereAreWe: String): Unit = {
    println(s"[${Thread.currentThread().getId()}] $uiThreadStr $whereAreWe")
  }

}