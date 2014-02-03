name := "RxScalaDict"

version := "1.0"

scalaVersion := "2.10.+"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
	"com.netflix.rxjava" % "rxjava-core"  % "0.16.+" ,
	"com.netflix.rxjava" % "rxjava-swing" % "0.16.+" ,
	"com.netflix.rxjava" % "rxjava-scala" % "0.16.+"
)

// also depends on dict4j.jar from http://dict4j.sourceforge.net/, which is in ./lib

