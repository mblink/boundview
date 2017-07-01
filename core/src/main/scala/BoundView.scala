import scalaz.Monoid
import scalaz.std.function._
import scalaz.syntax.either._
import scalaz.syntax.functor._
import scalaz.syntax.std.option._
import scalaz.\/
import shapeless.{HList, HNil, Poly1, Typeable}
import shapeless.labelled.{field, FieldType}
import shapeless.ops.hlist.Mapper
import shapeless.syntax.typeable._

object boundview {

  sealed trait Trigger
  case object Click extends Trigger
  case object Change extends Trigger

  trait DomError
  case object MissingElement extends DomError
  case object ValueTypeError extends DomError

  case class Binding[I, A](sel: String, ev: Trigger, upd: (I, A) => A)
  case class DomBinding[I, A](
    binding: Binding[I, A],
    read: String => \/[DomError, I],
    bind: String => Trigger => (A => A) => Unit)

  case class CustomReader[I](read: String => \/[DomError, I])

  trait LPRD extends Poly1 {
    implicit def readIAFromString[I: Typeable, K <: Symbol, A](implicit
        readString: String => \/[DomError, String],
        binder: String => Trigger => (A => A) => Unit) =
      at[FieldType[K, Binding[I, A]]] {
        (bind) => field[K](DomBinding(bind, readString.map(_.flatMap(_.cast[I] \/> ValueTypeError)), binder)) }
  }

  object ReadFromDom extends LPRD {
    implicit def readHNil = at[HNil](identity[HNil])
    implicit def readIACustom[I, K <: Symbol, A](implicit
        reader: CustomReader[I],
        binder: String => Trigger => (A => A) => Unit) =
      at[FieldType[K, Binding[I, A]]] {
        (bind) => field[K](DomBinding(bind, reader.read, binder)) }
  }

  object BindToDom extends Poly1 {
    implicit def bindHNil = at[HNil](identity[HNil])
    implicit def bindIA[K <: Symbol, I, A] = at[FieldType[K, DomBinding[I, A]]] {
      (bind) => field[K](bind.read(bind.binding.sel)
                    .map(i => bind.bind(bind.binding.sel)(bind.binding.ev)(bind.binding.upd(i, _)))) }
  }

  implicit class DomBinder[H <: HList](h: H) {
    def readFromDom(implicit map: Mapper[ReadFromDom.type, H]) = h.map(ReadFromDom)
    def bindToDom(implicit map: Mapper[BindToDom.type, H]) = h.map(BindToDom)
  }


  trait Renderer[Render] {
    def renderBinding[I, A](binding: Binding[I, A]): Render
    def renderView[A](view: BoundView[A, Render]): Render => Render
  }

  implicit class BindingOps[I, A, R](bind: Binding[I, A])(implicit r: Renderer[R]) {
    def render = r.renderBinding(bind)
  }

  case class BoundView[A: Monoid, Render](
    //bindings: List[Binding[A]],
    view: (A) => Render)(implicit val render: Renderer[Render]) {
      lazy val zero = implicitly[Monoid[A]].zero

      def renderAs[NewRender](postRender: Render => NewRender)(
              implicit nr: Renderer[NewRender]): BoundView[A, NewRender] =
        BoundView(view.map(postRender))(implicitly, nr)
  }

  implicit class ViewOps[A, R](view: BoundView[A, R])(implicit val r: Renderer[R]) {
    def render = r.renderView(view)
  }

  trait TypeOf[T] { type Out = T }

  implicit class TypeOfOps[A](a: A) {
    def typeOf = new TypeOf[A] {}
  }

}
