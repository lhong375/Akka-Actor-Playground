package akka.actor.playground
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

/**
  DedicatedActor is made to work for ReportGeneratorActor
  this actor is like a state machine, when it receive message of type Data3Obj, it switch to the state where it waits for Data1Obj
  and vise versa.
  when both messages are here, it pass it to MergeActor for a sum.
  when response come back from MergeActor, it returns the report to the sender
*/
class DedicatedActor(originSender: ActorRef, mergeActor: ActorRef)(implicit ec: ExecutionContext) extends Actor {
  def receive: Receive = {
    case message: Data3Obj =>
      println("receive Data3Obj")
      context.become(waitingForData1(message))
    case message: Data1Obj =>
      println("receive Data1Obj")
      context.become(waitingForData3(message))
    case _ =>
      println("receive Bad Type")
      //throw new NoSuchMethodException
  }

  private def waitingForData1(data3Obj: Data3Obj): Receive = {
    case message: Data1Obj =>
      mergeActor ! GetMergedData(message, data3Obj)
      context.become(returnReport())
    case _ =>
      println("receive Bad Type while waitingForData1")
  }

  private def waitingForData3(data1Obj: Data1Obj): Receive = {
    case message: Data3Obj =>
      mergeActor ! GetMergedData(data1Obj, message)
      context.become(returnReport())
      case _ =>
        println("receive Bad Type while waitingForData3")
  }

  private def returnReport(): Receive = {
    case report: MergedDataObj =>
      println(" $$$$$ returnReport $$$$$"+report)//logger.debug(s"Received report")
      originSender ! report
      context stop self
  }
}
