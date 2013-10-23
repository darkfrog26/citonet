package com.outr.net.http.session

import com.outr.net.http.{Cookie, HttpHandler, HttpApplication}
import com.outr.net.http.request.HttpRequest
import org.powerscala.{Priority, Unique}
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.handler.HandlerApplication
import org.powerscala.log.Logging
import org.powerscala.concurrent.Time._
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait SessionApplication[S <: Session] extends HandlerApplication with Logging {
  private var _sessions = Map.empty[String, S]
  lazy val sessions = new Sessions

  def session = requestContext[S]("session")

  handlers.add(new SessionCreateHandler, Priority.Critical)      // Add a handler to set the session (critical priority)

  protected def cookieName: String = getClass.getSimpleName

  protected def createSession(request: HttpRequest, id: String): S

  override def update(delta: Double) = {
    super.update(delta)

    // Update each session in this application
    _sessions.values.foreach {
      case session => session.update(delta)
    }
  }

  protected[session] def remove(id: String) = synchronized {
    _sessions -= id
  }

  override def dispose() = {
    super.dispose()

    _sessions.values.foreach(s => s.dispose())
  }

  class SessionCreateHandler extends HttpHandler {
    def onReceive(request: HttpRequest, response: HttpResponse) = SessionApplication.this.synchronized {
      val session = request.cookie(cookieName) match {      // Find the cookie for the session
        case Some(cookie) => {                // Cookie found
          val id = cookie.value
          _sessions.get(id) match {
            case Some(s) => s
            case None => createSession(request, id)
          }
        }
        case None => {
          val id = Unique()
          createSession(request, id)
        }
      }
      _sessions += session.id -> session
      requestContext("session") = session
      response.setCookie(Cookie(name = cookieName, value = session.id, maxAge = 1.years))
    }
  }

  class Sessions private[session]() {
    def map = _sessions
    def values = _sessions.values
    def valuesByType[T](implicit manifest: Manifest[T]) = {
      _sessions.values.flatMap {
        case session => session.values.collect {
          case v if v.getClass.hasType(manifest.runtimeClass) => v.asInstanceOf[T]
        }
      }
    }
  }
}

object SessionApplication {
  def apply[S <: Session]() = HttpApplication[SessionApplication[S]]()
}