import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._
import javax.inject.Singleton
import play.api.http.DefaultHttpErrorHandler

@Singleton
class ErrorHandler extends DefaultHttpErrorHandler {

  override protected def onNotFound(
    request: RequestHeader,
    message: String
  ): Future[Result] = {
    Future.successful(
      NotFound("O endpoint %s não foi encontrado.".format(request.uri))
    )
  }

}
