name := "RxScalaDict"

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
	"com.netflix.rxjava" % "rxjava-core"  % "0.12.3-SNAPSHOT" ,
	"com.netflix.rxjava" % "rxjava-swing" % "0.12.3-SNAPSHOT" ,
	"com.netflix.rxjava" % "rxjava-scala" % "0.12.3-SNAPSHOT"
)

// also depends on dict4j.jar from http://dict4j.sourceforge.net/, which is in ./lib

