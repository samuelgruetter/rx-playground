name := "CovarianceTests"

version := "1.0"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
   "io.reactivex" % "rxjava" % "1.0.0-rc.5"
)

scalacOptions ++= Seq("-deprecation", "-feature")
