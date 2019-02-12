name := """bio-gateway"""

version := "2.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala, PlayJava, PlayEbean)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.5"

libraryDependencies += guice
libraryDependencies += ehcache
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.196"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.6"
libraryDependencies += "commons-io" % "commons-io" % "2.4"
libraryDependencies += "jarsync" % "jarsync" % "0.2.1"
libraryDependencies += "net.lingala.zip4j" % "zip4j" % "1.3.2"
libraryDependencies += "org.apache.commons" % "commons-compress" % "1.5"
libraryDependencies += "redis.clients" % "jedis" % "2.9.0"
libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.12.6-play26"
libraryDependencies += "org.mockito" % "mockito-core" % "2.8.47"
libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.12" % "2.5.6" % "test"
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.9.0"
libraryDependencies += "org.apache.hadoop" % "hadoop-hdfs" % "2.9.0"
libraryDependencies += "org.ocpsoft.prettytime" % "prettytime" % "1.0.8.Final"
libraryDependencies += "com.rabbitmq" % "amqp-client" % "2.8.1"

unmanagedJars in Compile ++= {
  (file("lib") ** "ojdbc7.jar").classpath
}

// Enable to skip testing:
// test in assembly := {}

mainClass in assembly := Some("play.core.server.ProdServerStart")
fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

assemblyMergeStrategy in assembly := {
  //case x =>
    // For all the other files, use the default sbt-assembly merge strategy, deduplicate
//    val oldStrategy = (assemblyMergeStrategy in assembly).value
//    oldStrategy(x)

  case manifest if manifest.contains("MANIFEST.MF") =>
    // We don't need manifest files since sbt-assembly will create
    // one with the given settings
    MergeStrategy.discard
  case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
    // Keep the content for all reference-overrides.conf files
    MergeStrategy.concat
  case _ =>
    // pick up last version if confict.
    MergeStrategy.last
}


