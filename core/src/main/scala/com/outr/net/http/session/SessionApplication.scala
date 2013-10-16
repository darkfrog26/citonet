package com.outr.net.http.session

import com.outr.net.http.{Cookie, HttpHandler, HttpApplication}
import com.outr.net.http.request.HttpRequest
import org.powerscala.Unique
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.handler.HandlerApplication
import org.powerscala.log.Logging
import org.powerscala.concurrent.Time._

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait SessionApplication[S <: Session] extends HandlerApplication with Logging {
  private var sessions = Map.empty[String, S]

  def session = stack[S]("session")

  addHandler(new SessionCreateHandler)      // Add a handler to set the session (high priority)

  protected def cookieName: String = getClass.getSimpleName

  protected def createSession(request: HttpRequest, id: String): S

  override def update(delta: Double) = {
    super.update(delta)

    // Update each session in this application
    sessions.values.foreach {
      case session => session.update(delta)
    }
  }

  protected[session] def remove(id: String) = synchronized {
    sessions -= id
  }

  class SessionCreateHandler extends HttpHandler {
    def priority = HttpHandler.Highest    // Make sure the session appears early

    def onReceive(request: HttpRequest, response: HttpResponse) = SessionApplication.this.synchronized {
      val session = request.cookie(cookieName) match {      // Find the cookie for the session
        case Some(cookie) => {                // Cookie found
          val id = cookie.value
          info(s"Cookie found: $cookieName with id: $id")
          sessions.get(id) match {
            case Some(s) => s
            case None => createSession(request, id)
          }
        }
        case None => {
          val id = Unique()
          info(s"Cookie not found! $cookieName")
          createSession(request, id)
        }
      }
      sessions += session.id -> session
      // TODO: do we need to set the domain?
      response.setCookie(Cookie(name = cookieName, value = session.id, maxAge = 1.years))
    }
  }
}

object SessionApplication {
  def apply[S <: Session]() = HttpApplication().asInstanceOf[SessionApplication[S]]
}