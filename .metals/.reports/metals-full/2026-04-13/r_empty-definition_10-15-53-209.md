error id: file:///C:/Users/blill/OneDrive%20-%20LUT%20University/A%20Scala/Energalize/energialize/src/main/scala/com/example/energialize/Main.scala:https.
file:///C:/Users/blill/OneDrive%20-%20LUT%20University/A%20Scala/Energalize/energialize/src/main/scala/com/example/energialize/Main.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -https.
	 -https#
	 -https().
	 -scala/Predef.https.
	 -scala/Predef.https#
	 -scala/Predef.https().
offset: 417
uri: file:///C:/Users/blill/OneDrive%20-%20LUT%20University/A%20Scala/Energalize/energialize/src/main/scala/com/example/energialize/Main.scala
text:
```scala
import sttp.client3.*

@main def run(): Unit =
  val backend = HttpURLConnectionBackend()

  val request = basicRequest
    .get(uri"https://jsonplaceholder.typicode.com/todos/1")

  val response = request.send(backend)

  println(response.body)

  val requestFingrid = basicRequest
    .get(uri"https://jsonplaceholder.typicode.com/todos/1")

  val responseFingrid = requestFingrid.send(backend)

  @@https://data.fingrid.fi/api/datasets/{datasetId}

  println(responseFingrid.body)
```


#### Short summary: 

empty definition using pc, found symbol in pc: 