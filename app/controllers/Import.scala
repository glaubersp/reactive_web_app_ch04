package controllers

import javax.inject.Inject
import models.Vocabulary
import play.api.i18n.Lang
import play.api.mvc._
import services.VocabularyService

class Import @Inject()(cc: ControllerComponents)(vocabularyService: VocabularyService)
    extends AbstractController(cc) {

  def importWord(
    sourceLanguage: Lang,
    word: String,
    targetLanguage: Lang,
    translation: String
  ): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (!supportedLangs.availables.contains(sourceLanguage)) {
      NotFound(s"Language ${sourceLanguage.code} not found!")
    } else if (!supportedLangs.availables
                 .contains(targetLanguage)) {
      NotFound(s"Language ${targetLanguage.code} not found!")
    } else {
      val added =
        vocabularyService.addVocabulary(
          Vocabulary(sourceLanguage, targetLanguage, word, translation)
        )
      if (added)
        Ok(vocabularyService.printVocabulary())
      else
        Conflict(s"Entry $word already exists!")
    }
  }

}
