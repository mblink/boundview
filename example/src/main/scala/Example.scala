import boundview._
import scala.language.existentials
import scalaz.syntax.std.option._
import scalaz.syntax.either._
import scalaz.\/
import shapeless.syntax.singleton._
import shapeless.HNil
import shapeless.record._
import shapeless.syntax.typeable._

object example extends App {

  case class Wut(s: Int)

  val x = HNil + ('foo ->> Binding("foo", Click, (i: () => \/[DomError, Wut], o: Int) => i().map(_.s + o))) +
                 ('bar ->> Binding("bar", Click, (s: () => \/[DomError, String], o: Int) => s().map(_.length + o)))
  implicit val binder = (sel: String) => (t: Trigger) => (f: Int => \/[DomError, Int]) =>
    println(s"${sel} ${f(sel.length + t.toString.length).toString}")
  implicit val reader = (sel: String) => () => sel.right[DomError]
  implicit val wutReader = CustomReader[Wut](s => () => s.length.right[DomError].map(Wut(_)))
  val y = x.readFromDom
  println(y('foo).read("foo")())
  val z = y.bindToDom
}
