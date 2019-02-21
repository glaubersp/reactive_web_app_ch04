package actors
import akka.actor.{Actor, ActorRef, Props}
import models.Vocabulary
import play.api.i18n.Lang
import services.VocabularyService

class QuizActor(
  out: ActorRef,
  sourceLang: Lang,
  targetLang: Lang,
  vocabularyService: VocabularyService
) extends Actor {
  private var word: String = ""

  override def preStart(): Unit = sendWord()

  def sendWord(): Unit = {
    vocabularyService.findRandomVocabulary(sourceLang, targetLang).map { v: Vocabulary =>
      out ! s"Please translate the word '${v.word}'"
      word = v.word
    } getOrElse {
      out ! s"No word found for '${sourceLang.code}' and '${targetLang.code}'"
    }
  }

  override def receive: Receive = {
    case translation: String =>
      if (vocabularyService.verify(sourceLang, word, targetLang, translation)) {
        out ! s"Correct!\n"
        sendWord()
      } else {
        out ! s"Incorrect translation, try again!"
        out ! s"Please translate the word '${word}'"
      }
    case _ => {
      out ! s"Incorrect translation, try again!"
      out ! s"Please translate the word '${word}'"
    }

  }
}

object QuizActor {

  def props(
    out: ActorRef,
    sourceLang: Lang,
    targetLang: Lang,
    vocabulary: VocabularyService
  ): Props =
    Props(classOf[QuizActor], out, sourceLang, targetLang, vocabulary)
}
