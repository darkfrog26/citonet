package com.outr.net.http.session

import com.outr.net.http.{Cookie, HttpHandler}
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

  private val storeToken = s"session-${Unique()}"

  def session = request.store[S](storeToken)

  handlers.add(new SessionCreateHandler, Priority.Critical)      // Add a handler to set the session (critical priority)

  protected def cookieName: String = getClass.getSimpleName.replaceAll("[$]", "")

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
      val session = lookupSession(request) match {
        case Some(s) => s
        case None => createSession(request, Unique())
      }
      session.checkIn()     // Keep the session from timing out
      storeSession(request, session)
      val domain = request.url.domain match {
        case "localhost" => null
        case d => d
      }
      response.setCookie(Cookie(name = cookieName, value = session.id, maxAge = 1.years, domain = domain))
    }
  }

  def lookupAndStoreSession(request: HttpRequest) = lookupSession(request) match {
    case Some(session) => {
      session.checkIn()
      storeSession(request, session)
    }
    case None => // no session
  }

  def lookupSession(request: HttpRequest) = request.cookie(cookieName).map(c => _sessions.get(c.value)).flatten

  def storeSession(request: HttpRequest, session: S) = SessionApplication.this.synchronized {
    _sessions += session.id -> session
    request.store(storeToken) = session
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