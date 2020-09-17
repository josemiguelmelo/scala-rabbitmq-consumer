package test.unmarshallers

trait MessageUnmarshaller[I, O] {

  def handle(message: I): Option[O]
}
