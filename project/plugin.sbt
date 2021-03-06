// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC3")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.16")
addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")
addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.39"
addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "2.4.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")
addSbtPlugin("com.scalapenos" % "sbt-prompt" % "1.0.2")
