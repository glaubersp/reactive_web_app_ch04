package binders
import play.api.i18n.Lang
import play.api.mvc.{PathBindable, QueryStringBindable}

object PathBinders {
  implicit object LangPathBindable extends PathBindable[Lang] {

    def bind(key: String, value: String): Either[String, Lang] = {
      Lang.get(value).toRight(s"Language $value not recognized!")
    }

    def unbind(key: String, value: Lang): String = value.code
  }
}
