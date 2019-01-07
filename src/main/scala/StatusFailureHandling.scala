package akka.actor.playground.exception
import akka.actor.{ Actor, Status }

trait StatusFailureHandling { me: Actor =>
  def failureHandling: Receive = {
    case Status.Failure(ex) =>
      throw ex
  }
}
