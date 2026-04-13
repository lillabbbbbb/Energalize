error id: file:///C:/Users/blill/OneDrive%20-%20LUT%20University/A%20Scala/Energalize/energialize/build.sbt:
file:///C:/Users/blill/OneDrive%20-%20LUT%20University/A%20Scala/Energalize/energialize/build.sbt
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -scalaVersion.
	 -scalaVersion#
	 -scalaVersion().
	 -scala/Predef.scalaVersion.
	 -scala/Predef.scalaVersion#
	 -scala/Predef.scalaVersion().
offset: 1405
uri: file:///C:/Users/blill/OneDrive%20-%20LUT%20University/A%20Scala/Energalize/energialize/build.sbt
text:
```scala
val Http4sVersion = "0.23.33"
val CirceVersion = "0.14.15"
val MunitVersion = "1.2.4"
val LogbackVersion = "1.5.32"
val MunitCatsEffectVersion = "2.2.0"


//This lazy declaration was automatically created from the Metals template in VS Code
lazy val root = (project in file("."))
  .settings(
    organization := "com.example",
    name := "energialize",
    version := "0.0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"      %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "io.circe"        %% "circe-generic"       % CirceVersion,
      "org.scalameta"   %% "munit"               % MunitVersion           % Test,
      "org.typelevel"   %% "munit-cats-effect"   % MunitCatsEffectVersion % Test,
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion         % Runtime,
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.3" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    assembly / assemblyMergeStrategy := {
      case "module-info.class" => MergeStrategy.discard
      case x => (assembly / assemblyMergeStrategy).value.apply(x)
    }
  )



//
ThisBuild / @@scalaVersion := "3.3.3"
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.5.4",
  "com.softwaremill.sttp.client3" %% "core" % "3.9.5",
  "com.softwaremill.sttp.client3" %% "cats" % "3.9.5"
)
```


#### Short summary: 

empty definition using pc, found symbol in pc: 