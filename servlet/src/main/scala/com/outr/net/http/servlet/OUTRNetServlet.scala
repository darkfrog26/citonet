package com.outr.net.http.servlet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import javax.servlet.ServletConfig
import org.powerscala.reflect.EnhancedClass
import com.outr.net.http.HttpApplication
import org.powerscala.log.{Level, Logging}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class OUTRNetServlet extends HttpServlet with Logging {
  logger.configure {    // TODO: why isn't this working?
    case l => l.withLevel(Level.Debug)
  }

  private var application: HttpApplication = _

  override def init(config: ServletConfig) = {
    val applicationClass = config.getInitParameter("application")
    val clazz: EnhancedClass = Class.forName(applicationClass)
    val companion = clazz.instance.getOrElse(throw new RuntimeException(s"Unable to find companion object for $clazz"))
    application = companion.asInstanceOf[HttpApplication]
    application.init()
  }

  override def doGet(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) = {
    handle(servletRequest, servletResponse)
  }

  override def doPost(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) = {
    handle(servletRequest, servletResponse)
  }

  def handle(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) = {
    val request = ServletConversion.convert(servletRequest)
    try {
      debug(s"Request: $request")
      val response = application.receive(request)
      val gzip = request.headers.gzipSupport
      ServletConversion.convert(response, servletResponse, gzip)
    } catch {
      case t: Throwable if t.getClass.getName == "org.eclipse.jetty.io.EofException" => {
        warn(s"End of File exception occurred for: ${request.url}", t)
      }
      case t: Throwable => {
        error(s"Error occurred on ${request.url} - ${t.getClass.getName}.", t)
        throw t
      }
    }
  }
}