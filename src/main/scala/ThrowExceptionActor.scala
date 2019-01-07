package akka.actor.playground.exception

import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{ Failure, Success }

import akka.pattern.{ ask, pipe }
import akka.actor.{ Actor, ActorLogging, Props }
import scala.concurrent.{ Await, ExecutionContext, Future }

class ThrowExceptionActor extends Actor {

  implicit val system = context.system
  implicit val ec: ExecutionContext = system.dispatcher
  implicit lazy val timeout = Timeout(5.seconds)
  override def receive: Receive = {
    case te: ThrowException =>
      throw new Exception("Exception from ThrowExceptionActor.receive.ThrowException")
      sender ! 1
    case tef: ThrowExceptionFromFuture =>
      val originalSender = sender()
      val fut = Future {
        val lst = List(1,2,3)
        originalSender ! lst(5)
      }
    case tef: PipeBackExceptionFromFuture =>
      val fut = Future {
        val lst = List(1,2,3)
        lst(5)
      }
      fut pipeTo sender()

  }
}
