package com.outr.net.http

import org.powerscala.event.Listenable
import java.text.SimpleDateFormat
import com.outr.net.http.response.HttpResponseStatus
import com.outr.net.http.request.HttpRequest
import org.powerscala._
import com.outr.net.http.response.HttpResponse
import org.powerscala.concurrent.{Time, Executor}
import java.util.concurrent.ScheduledFuture
import org.powerscala.concurrent.Time._
import org.powerscala.event.processor.UnitProcessor
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpApplication extends Listenable with HttpHandler with Updatable with Disposable {
  // TODO: support using HttpApplication as a HttpHandler abstractly
  HttpApplication.current = this

  private val stack = new LocalStack[Storage[String, Any]]
  def requestContext = stack.get().getOrElse(throw new RuntimeException("RequestContext is not set for the current thread!"))

  @volatile private var _initialized = false
  @volatile private var running = false
  @volatile private var _updater: ScheduledFuture[_] = _

  val disposed = new UnitProcessor[HttpApplication]("disposed")

  def initialized = _initialized
  def isRunning = running

  /**
   * The frequency the application will be updated in seconds.
   *
   * Defaults to 5 seconds.
   */
  def updateFrequency: Double = 5.seconds

  /**
   * The HttpRequest for the current thread. This will return null if there is no request contextualized.
   */
  def request = requestContext[HttpRequest]("request")

  final def initialize() = synchronized {
    if (!initialized) {
      HttpApplication.current = this
      init()

      var previous = System.nanoTime()
      _updater = Executor.scheduleWithFixedDelay(0.0, updateFrequency) {
        val current = System.nanoTime()
        val delta = Time.fromNanos(current - previous)
        update(delta)
        previous = current
      }
      _initialized = true
      running = true
    }
  }

  /**
   * Called once as the application is initialized.
   */
  protected def init(): Unit

  /**
   * Called once when the application is terminating (not guaranteed to be executed).
   */
  def dispose(): Unit = {
    _updater.cancel(false)

    disposed.fire(this)
    running = false
  }

  protected def processRequest(request: HttpRequest, response: HttpResponse) = {
    onReceive(request, response)
  }

  def receive(request: HttpRequest) = {
    processRequest(request, HttpResponse(status = HttpResponseStatus.NotFound))
  }

  final def receiveContextualized[R](request: HttpRequest)(responseHandler: HttpResponse => R): R = contextualize(request) {
    val response = receive(request)
    responseHandler(response)
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
    HttpApplication.current = this
    stack.context(new MapStorage[String, Any]()) {   // Make the stack available for this context
      requestContext("request") = request                    // Put the request into the storage
      f
    }
  }

  /**
   * Contextualizes the current thread with an existing context. This can be useful for shared state across more
   * than one thread.
   *
   * @param context the context to assign to the current thread
   * @param f the function to execute within this context
   * @tparam T the return type
   * @return T
   */
  def contextualize[T](context: Storage[String, Any])(f: => T) = {
    stack.context(context) {      // Assign an existing context to this thread
      f
    }
  }

  /**
   * This method is invoked per request to allow wrapping around the entire request/response functionality.
   *
   * @param f the function to manage the request / response process.
   */
  def around[R](f: => R): R = f
}

object HttpApplication {
  def DateParser = new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss zzz")

  private val _current = new AtomicReference[HttpApplication]()

  def current_=(application: HttpApplication) = _current.set(application)
  def current = _current.get()

  def apply[T <: HttpApplication]() = current.asInstanceOf[T]
}
