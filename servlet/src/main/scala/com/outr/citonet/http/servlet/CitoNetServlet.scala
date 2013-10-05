package com.outr.citonet.http.servlet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import javax.servlet.ServletConfig
import org.powerscala.reflect.EnhancedClass
import com.outr.citonet.http.HttpApplication
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
class CitoNetServlet extends HttpServlet with Logging {
  private var application: HttpApplication = _

  override def init(config: ServletConfig) = {
    val applicationClass = config.getInitParameter("application")
    val clazz: EnhancedClass = Class.forName(applicationClass)
    val companion = clazz.instance.getOrElse(throw new RuntimeException(s"Unable to find companion object for $clazz"))
    application = companion.asInstanceOf[HttpApplication]
    application.init()
  }

  override def doGet(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) = {
    val request = ServletConversion.convert(servletRequest)
    try {
      val response = application.receive(request)
      val gzip = request.headers.gzipSupport
      ServletConversion.convert(response, servletResponse, gzip)
    } catch {
      case t: Throwable => {
        error(s"Error occurred on ${request.url}.", t)
        throw t
      }
    }
  }
}