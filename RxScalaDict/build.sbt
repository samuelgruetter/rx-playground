name := "RxScalaDict"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
   "io.reactivex" % "rxswing" % "0.21.0",
   "io.reactivex" %% "rxscala" % "0.22.0"
)

// also depends on dict4j.jar from http://dict4j.sourceforge.net/, which is in ./lib

scalacOptions ++= Seq("-deprecation", "-feature")
