file:///C:/Users/blill/OneDrive%20-%20LUT%20University/A%20Scala/Energalize/energialize/src/main/scala/com/example/energialize/Main.scala
empty definition using pc, found symbol in pc: 
semanticdb not found
empty definition using fallback
non-local guesses:
	 -Simple#
	 -scala/Predef.Simple#
offset: 130
uri: file:///C:/Users/blill/OneDrive%20-%20LUT%20University/A%20Scala/Energalize/energialize/src/main/scala/com/example/energialize/Main.scala
text:
```scala
import cats.effect.*
import sttp.client3.*
import sttp.client3.catshttpclient.CatsHttpClient

object Main extends IOApp.Simple@@:

  def run: IO[Unit] =
    CatsHttpClient.resource[IO]().use { backend =>
      val request = basicRequest
        .get(uri"https://jsonplaceholder.typicode.com/todos/1")
        .response(asStringAlways)

      backend.send(request).flatMap { response =>
        IO.println(response.body)
      }
    }
```


#### Short summary: 

empty definition using pc, found symbol in pc: 