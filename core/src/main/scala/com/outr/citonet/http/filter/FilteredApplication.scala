package com.outr.citonet.http.filter

import com.outr.citonet.http.HttpApplication
import com.outr.citonet.http.request.HttpRequest
import com.outr.citonet.http.response.HttpResponse
import scala.annotation.tailrec

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait FilteredApplication extends HttpApplication {
  private var _filters = List.empty[HttpFilter]
  def filters = _filters

  override protected def processRequest(request: HttpRequest) = {
    processFilters(request, filters) match {
      case Left(req) => super.processRequest(request)
      case Right(resp) => resp
    }
  }

  @tailrec
  private def processFilters(request: HttpRequest, filters: List[HttpFilter]): Either[HttpRequest, HttpResponse] = {
    if (filters.isEmpty) {
      Left(request)
    } else {
      filters.head.filter(request) match {
        case Right(resp) => Right(resp)
        case Left(req) => processFilters(req, filters.tail)
      }
    }
  }

  def addFilter[F <: HttpFilter](filter: F): F = synchronized {
    _filters = (filter :: _filters.reverse).reverse.sortBy(f => f.priority)
    filter
  }

  def removeFilter[F <: HttpFilter](filter: F): F = synchronized {
    _filters = _filters.filterNot(f => f == filter)
    filter
  }
}
