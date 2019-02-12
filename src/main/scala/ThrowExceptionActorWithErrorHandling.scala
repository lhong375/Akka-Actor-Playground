package akka.actor.playground.exception

import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{ Failure, Success }

import akka.pattern.{ ask, pipe }
import akka.actor.{ Actor, ActorLogging, Props, Status }
import scala.concurrent.{ Await, ExecutionContext, Future }


class ThrowExceptionActorWithErrorHandling extends FailurePropatingActor {
  import context.dispatcher
  import akka.pattern.pipe

  implicit val system = context.system
  implicit val ec: ExecutionContext = system.dispatcher
  implicit lazy val timeout = Timeout(5.seconds)
  override def receive: Receive = {
    case te: ThrowException =>
      throw new Exception("Exception from ThrowExceptionActorWithErrorHandling.receive.ThrowException")
    case tef: ThrowExceptionFromFuture =>
      val fut = Future {
        val lst = List(1,2,3)
        lst(5)
      }
      fut.to(self, sender())
    case tef: ThrowExceptionFromFuture2 =>
        val originalSender = sender()
        val fut = Future {
          val lst = List(1,2,3)
          originalSender ! lst(5)
        }
        fut.to(self)
    case Status.Failure(ex) =>
      println("Handle the exception myself!", ex);
      throw ex
  }
}
