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
  Another way to chain/merge actors
  this actor expect message: GenerateReport, similar to GenerateDataParallel in DataGeneratorActor
  The difference is, it will create a DedicatedActor, hand over the job of collecting data from Data1Actor and Data3Actor to it.
*/
case class GenerateReport(reportNumber: Int)
class ReportGeneratorActor(implicit val ec: ExecutionContext) extends Actor {

  override def receive: Receive = {
    case message: GenerateReport =>

      val data1Actor = context.actorOf(Props(new Data1Actor(message.reportNumber)))
      val data3Actor = context.actorOf(Props(new Data3Actor(message.reportNumber)))
      val mergeDataActor = context.actorOf(Props(new MergeActor(message.reportNumber)))
      val dedicatedActor = context.actorOf(Props(new DedicatedActor(sender, mergeDataActor)(ec)))
      data1Actor.tell(GetData1(), dedicatedActor)
      data3Actor.tell(GetData3(), dedicatedActor)
  }

}
