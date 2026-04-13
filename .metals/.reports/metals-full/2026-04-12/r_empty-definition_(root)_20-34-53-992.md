file:///C:/Users/blill/OneDrive%20-%20LUT%20University/A%20Scala/Energalize/energialize/src/main/scala/com/example/energialize/Main.scala
empty definition using pc, found symbol in pc: 
semanticdb not found
empty definition using fallback
non-local guesses:
	 -IOApp.
	 -IOApp#
	 -IOApp().
	 -scala/Predef.IOApp.
	 -scala/Predef.IOApp#
	 -scala/Predef.IOApp().
offset: 163
uri: file:///C:/Users/blill/OneDrive%20-%20LUT%20University/A%20Scala/Energalize/energialize/src/main/scala/com/example/energialize/Main.scala
text:
```scala
import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.server.Router

object Main extends IOA@@pp.Simple:

  val helloRoute = HttpRoutes.of[IO] {
    case GET -> Root / "hello" =>
      Ok("Hello from http4s + cats-effect!")
  }

  val httpApp = Router(
    "/" -> helloRoute
  ).orNotFound

  val run =
    EmberServerBuilder
      .default[IO]
      .withHost("0.0.0.0")
      .withPort(8080)
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.never)
```


#### Short summary: 

empty definition using pc, found symbol in pc: 