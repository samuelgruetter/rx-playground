name := "RxScalaPaint"

version := "1.0"

scalaVersion := "2.10.+"

val rxVersion = "0.20.+"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
	"com.netflix.rxjava" % "rxjava-core"  % rxVersion ,
	"com.netflix.rxjava" % "rxjava-swing" % rxVersion ,
	"com.netflix.rxjava" % "rxjava-scala" % rxVersion
)


