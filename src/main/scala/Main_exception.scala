package akka.actor.playground.exception

import akka.actor._
import scala.concurrent.{ ExecutionContext, Future }
import akka.pattern.ask

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.Future

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.util.Timeout
import scala.util.{Failure, Success}

case class ThrowException()
case class ThrowExceptionFromFuture()
case class PipeBackExceptionFromFuture()

object Main_exception extends App {

  println("=================== Error Handling Example ===================")

  implicit val system       = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit lazy val timeout = Timeout(5.seconds)

  // >>>>>>>>>> Actor that throws exception directly >>>>>>>>>>
  val throwExceptionActor = system.actorOf(Props(new ThrowExceptionActor()), name = "throwExceptionActor")

  // >>>>>>>>>> Actor that throws exception with proper error handling >>>>>>>>>>
  val throwExceptionActorWithErrorHandling = system.actorOf(Props(new ThrowExceptionActorWithErrorHandling()), name = "throwExceptionActorWithErrorHandling")

  
  // ++++++++++ Throw Exception directly from Actor ++++++++++
  // This will timeout (you will see the exception trace on the console though)
  val res1 = throwExceptionActor ? ThrowException()
  res1.onComplete {
    case Success(x) => println("throwExceptionActor.ThrowException return Success")
    case Failure(ex) => println("throwExceptionActor.ThrowException return Failure: "+ex)
  }

  /*
  // ++++++++++ Throw Exception from a Future called by Actor ++++++++++
  // This will just timeout (you don't even see any exception at all)
  val res2 = throwExceptionActor ? ThrowExceptionFromFuture()
  res2.onComplete {
    case Success(x) => println("throwExceptionActor.ThrowExceptionFromFuture return Success: "+x)
    case Failure(ex) => println("throwExceptionActor.ThrowExceptionFromFuture return Failure: "+ex)
  }


  // ++++++++++ Pipe back the entire future ++++++++++
  // This will pipe back the exception as part of Future complete
  val res3 = throwExceptionActor ? PipeBackExceptionFromFuture()
  res3.onComplete {
    case Success(x) => println("throwExceptionActor.PipeBackExceptionFromFuture return Success:"+x)
    case Failure(ex) => println("throwExceptionActor.PipeBackExceptionFromFuture return Failure: "+ex)
  }




  // ++++++++++ Throw Exception directly from Actor ++++++++++
  val res4 = throwExceptionActorWithErrorHandling ? ThrowException()
  res4.onComplete {
    case Success(x) => println("throwExceptionActorWithErrorHandling.ThrowException return Success")
    case Failure(ex) => println("throwExceptionActorWithErrorHandling.ThrowException return Failure: "+ex)
  }
  */
  /*
  // ++++++++++ Throw Exception from a Future called by Actor ++++++++++
  val res5 = throwExceptionActorWithErrorHandling ? ThrowExceptionFromFuture()
  res5.onComplete {
    case Success(x) => println("throwExceptionActorWithErrorHandling.ThrowExceptionFromFuture return Success")
    case Failure(ex) => println("throwExceptionActorWithErrorHandling.ThrowExceptionFromFuture return Failure: "+ex)
  }

*/


}

//Thanks to https://stackoverflow.com/questions/29794454/resolving-akka-futures-from-ask-in-the-event-of-a-failure
