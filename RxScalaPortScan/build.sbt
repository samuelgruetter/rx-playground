name := "RxScalaPortScan"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

val rxVersion = "0.20.4"

libraryDependencies ++= Seq(
	"com.netflix.rxjava" % "rxjava-scala" % rxVersion
)


scalacOptions ++= Seq("-deprecation", "-feature")

