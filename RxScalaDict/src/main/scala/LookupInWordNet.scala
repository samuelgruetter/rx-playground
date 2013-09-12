
import scala.collection.JavaConverters._
import scala.collection.Seq
import net.sf.dict4j.DictSession


object LookupInWordNet {

  def printStrategiesAndDatabases() {
    val session = new DictSession("dict.org")
    session.open("test client");
    try {
      println("Strategies:")
      val strategies = session.showStrategies().asScala
      for (strategy <- strategies) {
        println(strategy.getName() + ": " + strategy.getDescription());
      }
      
      println("Databases:")
      val databases = session.showDatabases().asScala
      for (db <- databases) {
        println(db.getName() + ": " + db.getDescription());
      }
    } finally {
      session.close()
      println("closed")
    }
  }
  
  def matchPrefixInWordNet(prefix: String): Seq[String] = {
    ThreadLogger.log("blocking lookup operations")
    
    val session = new DictSession("dict.org")
    session.open("test client");
    try {
      val matches = session.`match`(prefix, "wn", "prefix").asScala
      for (word <- matches) yield word.getWord()
    } finally {
      session.close()
      println("matchPrefixInWordNet closed its session")
    }
  }
  
  

}