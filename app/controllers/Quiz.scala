package controllers
import actors.QuizActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.Inject
import play.api.i18n.Lang
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.VocabularyService

class Quiz @Inject()(cc: ControllerComponents)(vocabularyService: VocabularyService)(
  implicit system: ActorSystem,
  mat: Materializer
) extends AbstractController(cc) {

  def quiz(sourceLanguage: Lang, targetLanguage: Option[Lang]): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val targetLanguageFinal = targetLanguage match {
        case Some(lang) => lang
        case None =>
          request.headers.get("X-Target-Language") match {
            case Some(lang) => Lang(lang)
            case _          => Lang("en")
          }
      }
      vocabularyService.findRandomVocabulary(sourceLanguage, targetLanguageFinal) match {
        case Some(vocabulary) =>
          Ok(
            s"Please, translate the word ${vocabulary.word} from ${sourceLanguage.code} to ${targetLanguageFinal.code}"
          )
        case None =>
          NotFound(
            s"No words found for language combination ${sourceLanguage.code} and ${targetLanguageFinal.code}"
          )
      }
    }

  def check(
    sourceLanguage: Lang,
    word: String,
    targetLanguage: Option[Lang],
    translation: String
  ): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val targetLanguageFinal = targetLanguage match {
        case Some(lang) => lang
        case None =>
          request.headers.get("X-Target-Language") match {
            case Some(lang) => Lang(lang)
            case _          => Lang("en")
          }
      }
      val isCorrect =
        vocabularyService.verify(sourceLanguage, word, targetLanguageFinal, translation)
      val correctScore = request.session.get("correct").map(_.toInt).getOrElse(0)
      val wrongScore = request.session.get("wrong").map(_.toInt).getOrElse(0)
      if (isCorrect) {

        Ok.withSession(
          request.session + ("correct" -> (correctScore + 1).toString) + ("wrong" -> wrongScore.toString)
        )
      } else {
        NotAcceptable.withSession(
          request.session + ("correct" -> correctScore.toString) + ("wrong" -> (wrongScore + 1).toString)
        )
      }
    }

  def quizEndpoint(sourceLang: Lang, targetLang: Lang): WebSocket =
    WebSocket.accept[String, String] { request =>
      ActorFlow.actorRef[String, String] { out =>
        QuizActor.props(out, sourceLang, targetLang, vocabularyService)
      }
    }

  def clear(): Action[AnyContent] = Action {
    Ok("Sessão Limpa").withNewSession
  }
}
