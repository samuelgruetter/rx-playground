name := "RxScalaAdapter"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
	"com.netflix.rxjava" % "rxjava-core"  % "0.11.3"
)

libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest" % "2.0.M6",
	"junit" % "junit" % "4.9",
	"org.mockito" % "mockito-core" % "1.9.5"
)


