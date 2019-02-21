package filters

import akka.stream.Materializer
import akka.util.ByteString
import javax.inject.Inject
import play.api.http.{HeaderNames, HttpEntity}
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.concurrent.ExecutionContext

class ScoreFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext)
    extends EssentialFilter {
  override def apply(
    nextFilter: EssentialAction
  ): EssentialAction = new EssentialAction {
    override def apply(
      rh: RequestHeader
    ): Accumulator[ByteString, Result] = {

      val accumulator = nextFilter(rh)

      accumulator.mapFuture(res => {
        var score: String = ""
        if (res.header.status == 200 || res.header.status == 406) {
          val correct = res.session(rh).get("correct").getOrElse("0")
          val wrong = res.session(rh).get("wrong").getOrElse("0")
          score = s"\nYour current score is: $correct correct answers and $wrong wrong answers."
        }

        res.body.consumeData
          .map(bytes => {
            val entity = HttpEntity
              .Strict(bytes.++(score.getBytes()), rh.headers.get(HeaderNames.CONTENT_TYPE))
            res.copy(body = entity)
          })

      })

    }
  }
}
