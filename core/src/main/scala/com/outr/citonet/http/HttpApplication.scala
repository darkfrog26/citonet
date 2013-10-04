package com.outr.citonet.http

import org.powerscala.property.{ListProperty, Property}
import com.outr.citonet.HasHostAndPort
import org.powerscala.event.Listenable
import java.text.SimpleDateFormat
import com.outr.citonet.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.citonet.http.request.HttpRequest
import scala.annotation.tailrec
import com.outr.citonet.http.filter.HttpFilter

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpApplication extends Listenable {
  private var _filters = List.empty[HttpFilter]
  def filters = _filters

  val bindings = new Property[List[HasHostAndPort]](default = Some(Nil)) with ListProperty[HasHostAndPort]

  def init(): Unit

  protected def onReceive(request: HttpRequest): HttpResponse

  final def receive(request: HttpRequest) = {
    val response = processFilters(request, filters) match {
      case Left(req) => onReceive(req)
      case Right(resp) => resp
    }
    val cached = request.headers.ifModifiedSince match {
      case Some(modified) if response.content.lastModified != -1 => modified >= response.content.lastModified
      case _ => false
    }
    if (cached) {
      response.copy(status = HttpResponseStatus.NotModified, content = null)
    } else {
      response
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

  def addFilter(filter: HttpFilter) = synchronized {
    _filters = (filter :: _filters.reverse).reverse
  }

  def removeFilter(filter: HttpFilter) = synchronized {
    _filters = _filters.filterNot(f => f == filter)
  }
}

object HttpApplication {
  val DateParser = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
}