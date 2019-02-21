import filters.ScoreFilter
import javax.inject.Inject
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

class Filters @Inject()(gzip: GzipFilter, score: ScoreFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(gzip, SecurityHeadersFilter(), score)
}
