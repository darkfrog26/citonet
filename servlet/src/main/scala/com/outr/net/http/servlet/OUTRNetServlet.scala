package com.outr.net.http.servlet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import javax.servlet.ServletConfig
import org.powerscala.reflect.EnhancedClass
import com.outr.net.http.HttpApplication
import org.powerscala.log.{Level, Logging}
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class OUTRNetServlet extends HttpServlet with Logging {
  private var application: HttpApplication = _

  def init(application: HttpApplication) = {
    this.application = application
    application.initialize()
    info(s"Initialized ${application} as application for servlet successfully.")
  }

  override def init(config: ServletConfig) = if (application == null) {
    val applicationClass = config.getInitParameter("application")
    val clazz: EnhancedClass = Class.forName(applicationClass)
    val companion = clazz.instance.getOrElse(throw new RuntimeException(s"Unable to find companion object for $clazz"))
    init(companion.asInstanceOf[HttpApplication])
  }

  override def destroy() = {
    try {
      application.dispose()
    } catch {
      case t: Throwable => error(s"Exception thrown while attempting to dispose application.", t)
    }

    super.destroy()
  }

  override def doGet(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) = {
    OUTRNetServlet.handle(application, servletRequest, servletResponse)
  }

  override def doPost(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) = {
    OUTRNetServlet.handle(application, servletRequest, servletResponse)
  }
}

object OUTRNetServlet extends Logging {
  def handle(application: HttpApplication, servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) = {
    val request = try {
      ServletConversion.convert(servletRequest)
    } catch {
      case t: Throwable => {
        error(s"Error occurred while parsing request: ${servletRequest.getRequestURL}", t)
        throw t
      }
    }
    application.around(request) {
      try {
        debug(s"Request: $request")
        HttpApplication.around(request) {
          val response = application.onReceive(request, HttpResponse(status = HttpResponseStatus.NotFound))
          val gzip = request.headers.gzipSupport && (response.content == null || useGzip(response.content.contentType.mimeType))
          ServletConversion.convert(request, response, servletResponse, gzip)
        }
      } catch {
        case t: Throwable if t.getClass.getName == "org.eclipse.jetty.io.EofException" => {
          warn(s"End of File exception occurred for: ${request.url}")
        }
        case t: Throwable => {
          error(s"Error occurred on ${request.url} - ${t.getClass.getName}.", t)
          throw t
        }
      }
    }
  }

  def useGzip(mimeType: String) = if (mimeType.startsWith("video/") || mimeType.startsWith("audio/")) {
    false
  } else {
    true
  }
}