import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.{ read, write, writePretty }
//type DslConversion = Person => JValue
implicit val formats = DefaultFormats
case class Person(var nome: String, var end: String, var idade: Int)
case class CommandMsg(var command:String, var params:List[String], var callback_id:Int)
//case class CommandMsg(var command:String, var params:List[String])
class Teste {

   var parsed = parse("{\"command\":\"tiltMove\",\"params\":[\"135\"],\"callback_id\":3}")
  println(parsed)
  var comando = parsed.extract[CommandMsg]
  println(comando)
  var x = parse("{\"nome\": \"ralth\", \"end\":\"sqs\", \"idade\":2}")
  println(x)
  val pessoa = x.extract[Person]
  println(pessoa)
  println(pessoa.nome+100)
  println(pessoa.end +100)
  println(pessoa.idade +100)
  var outraPessoa = new Person("Adilson","SQS 402, bl D",42)
  println(writePretty(outraPessoa))
  println(write(outraPessoa))
//  compact(render(outraPessoa))
}
new Teste




























// Teste.x
//println(x)