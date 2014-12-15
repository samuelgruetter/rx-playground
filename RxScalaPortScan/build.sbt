name := "RxScalaPortScan"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
   "io.reactivex" %% "rxscala" % "0.22.0"
)

scalacOptions ++= Seq("-deprecation", "-feature")
