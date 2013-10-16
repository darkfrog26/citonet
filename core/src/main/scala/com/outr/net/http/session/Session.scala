package com.outr.net.http.session

import org.powerscala.concurrent.Temporal
import org.powerscala.event.Listenable
import org.powerscala.concurrent.Time._
import org.powerscala.Storage
import org.powerscala.property.Property
import org.powerscala.event.processor.UnitProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Session extends Temporal with Listenable with Storage[Any, Any] {
  /**
   * Fired when a session value changes in this session.
   */
  val changed = new UnitProcessor[SessionValueChange]("changed")

  abstract override protected def set(key: Any, value: Option[Any]) = {
    val original = get(key)
    super.set(key, value)
    changed.fire(SessionValueChange(key, original, value))
  }

  /**
   * The timeout for this session in seconds without any communication.
   *
   * Defaults to 30 minutes.
   */
  def timeout: Double = 30.minutes

  /**
   * Creates a property that is bound to the supplied key in this Session. This will reflect changes made directly to
   * the property as well as changes made apart from the property directly to session and key.
   *
   * @param key to use
   * @param default the default value to set and the value to set back to if the key is removed
   * @param manifest the manifest for the property type
   * @tparam T the type of property to store
   * @return Property[T]
   */
  def property[T](key: Any, default: T)(implicit manifest: Manifest[T]) = {
    val p = Property[T](default = Option(default))(this, manifest)
    changed.on {
      case evt => if (evt.key == key) {   // Update the property when the key is modified externally
        p := evt.newValue.getOrElse(default).asInstanceOf[T]
      }
    }
    p.change.on {
      case evt => update(key, evt.newValue)
    }
    p
  }

  def dispose() = {
    clear()
    // TODO: remove from SessionApplication
  }
}
