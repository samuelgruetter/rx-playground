name := "RxJavaSwingThreads"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
	"com.netflix.rxjava" % "rxjava-core"  % "0.20.4" ,
	"com.netflix.rxjava" % "rxjava-swing" % "0.20.4"
)


scalacOptions ++= Seq("-deprecation", "-feature")

