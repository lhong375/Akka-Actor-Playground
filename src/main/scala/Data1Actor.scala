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

//receive message GetData1, return obj Data1Obj
class Data1Actor(reportNumber: Int) extends Actor {
  implicit val system = context.system

  implicit val ec: ExecutionContext = system.dispatcher
  var count_Data1 = 0
  def incrementAndPrint { count_Data1 += 1; /*println(" ***** Data1#"+count_Data1)*/ }
  def receive = {
    case message: GetData1 =>
        println(" ***** GetData1 Start, report#"+reportNumber)
        sleep(500)
        incrementAndPrint
        val res =  new Data1Obj(count_Data1)
        println(" ***** GetData1 End, report#"+reportNumber+", return count_Data1="+count_Data1)
        sender ! res
  }

  def sleep(time: Long): Unit = Thread.sleep(time)
}
