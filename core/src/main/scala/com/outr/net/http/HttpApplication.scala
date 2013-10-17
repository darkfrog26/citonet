package com.outr.net.http

import org.powerscala.event.Listenable
import java.text.SimpleDateFormat
import com.outr.net.http.response.HttpResponseStatus
import com.outr.net.http.request.HttpRequest
import org.powerscala._
import com.outr.net.http.response.HttpResponse
import scala.Some
import org.powerscala.concurrent.{Time, Executor}
import java.util.concurrent.ScheduledFuture
import org.powerscala.concurrent.Time._
import org.powerscala.event.processor.UnitProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpApplication extends Listenable with HttpHandler with Updatable with Disposable {
  private val _stack = new LocalStack[Storage[String, Any]]
  protected def stack = _stack()

  @volatile private var _initialized = false
  @volatile private var _updater: ScheduledFuture[_] = _

  val disposed = new UnitProcessor[HttpApplication]("disposed")

  def initialized = _initialized

  /**
   * The frequency the application will be updated in seconds.
   *
   * Defaults to 5 seconds.
   */
  def updateFrequency: Double = 5.seconds

  /**
   * The HttpRequest for the current thread. This will return null if there is no request contextualized.
   */
  def request = stack[HttpRequest]("request")

  final def initialize() = synchronized {
    if (!initialized) {
      init()

      var previous = System.nanoTime()
      _updater = Executor.scheduleWithFixedDelay(0.0, updateFrequency) {
        val current = System.nanoTime()
        val delta = Time.fromNanos(current - previous)
        update(delta)
        previous = current
      }
      _initialized = true
    }
  }

  /**
   * Called once as the application is initialized.
   */
  protected def init(): Unit

  override def update(delta: Double) = {
    super.update(delta)

    // TODO: update pages
  }

  /**
   * Called once when the application is terminating (not guaranteed to be executed).
   */
  def dispose(): Unit = {
    _updater.cancel(false)

    disposed.fire(this)
  }

  protected def processRequest(request: HttpRequest, response: HttpResponse) = {
    onReceive(request, response)
  }

  def receive(request: HttpRequest) = contextualize(request) {
    val response = processRequest(request, HttpResponse(status = HttpResponseStatus.NotFound))
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

  /**
   * Contextualizes the current thread with this HttpRequest and executes the supplied code block. Upon completion the
   * the context will be returned to its previous state.
   *
   * @param request the HttpRequest to put into the current context
   * @param f the code block to execute
   * @tparam T the return type
   * @return T
   */
  def contextualize[T](request: HttpRequest)(f: => T) = {
    HttpApplication.stack.context(this) {             // Push the current HttpApplication onto its stack
      _stack.context(new MapStorage[String, Any]) {   // Make the stack available for this context
        stack("request") = request                    // Put the request into the storage
        f
      }
    }
  }
}

object HttpApplication {
  val DateParser = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")

  val stack = new LocalStack[HttpApplication]

  def apply() = stack()
}