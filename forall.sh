#!/bin/sh

# Usage: ./forall CMD
# Runs CMD in all subdirs which contain a build.sbt file.
# Examples: ./forall.sh 'pwd'
#           ./forall.sh 'sbt eclipse'
#           ./forall.sh "sbt compile"
#           ./forall.sh "sbt 'eclipse with-source=true'"
#           ./forall.sh "grep --color -P -e '0\.20\..' build.sbt"
#           ./forall.sh "sed -i -e 's/0\.20\.+/0.20.4/g' build.sbt"

# Note: It is intended that CovarianceTests and ScalaImplicitsProblem do not compile

for dir in `tree -i -f . | grep -e 'build.sbt' | sed -e 's/\.\///g' | sed -e 's/\/build.sbt//g'`
do
  echo -e "\n\n======$dir======="
  cd $dir
  eval "$1"
  cd ..
done

