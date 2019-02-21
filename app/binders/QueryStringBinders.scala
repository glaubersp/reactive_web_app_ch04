package binders
import play.api.i18n.Lang
import play.api.mvc.QueryStringBindable

object QueryStringBinders {
  implicit def queryStringBindable(
    implicit stringBinder: QueryStringBindable[String]
  ): QueryStringBindable[Lang] =
    new QueryStringBindable[Lang] {

      override def bind(
        key: String,
        params: Map[String, Seq[
          String
        ]]
      ): Option[Either[String, Lang]] = {
        for {
          targetLang <- stringBinder.bind("targetLang", params)
        } yield {
          targetLang match {
            case Right(lang) => Right(Lang(lang))
            case _           => Left("Unable to bind a Lang")
          }
        }
      }
      override def unbind(
        key: String,
        value: Lang
      ): String = value.code
    }

}
