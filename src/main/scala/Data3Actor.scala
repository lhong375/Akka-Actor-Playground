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

//take a Int(reportNumber) to create this actor, expect message GetData3, sleep 250, return obj Data3Obj
class Data3Actor(reportNumber: Int) extends Actor {
  implicit val system = context.system

  implicit val ec: ExecutionContext = system.dispatcher
  var count_Data3 = 0
  def incrementAndPrint { count_Data3 += 1000; /*println(" ===== Data3#"+count_Data3)*/ }
  def receive = {
    case message: GetData3 =>
        println(" ===== GetData3 Start #"+reportNumber)
        sleep(250)
        incrementAndPrint
        val res:Data3Obj =  new Data3Obj(count_Data3)
        println(" ===== GetData3 End, report#"+reportNumber+", return count_Data3="+count_Data3)
        sender ! res
  }

  def sleep(time: Long): Unit = Thread.sleep(time)
}
