package akka.actor.playground
import akka.actor._

import scala.concurrent.{ExecutionContext, Future}
import akka.pattern.ask
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.actor.playground.exception.FailurePropatingActor
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.util.Timeout

import scala.util.{Failure, Success}

case class GetData2(data1: Data1Obj)
case class Data2Obj(count: Int)

//take a Int(reportNumber) and sleepTime(optional) to create this actor, expect message GetData2, GetData2 should come with {data1: Int}, sleep 250, returns obj Data2Obj
class Data2Actor(reportNumber: Int, sleepTime: Option[Int]) extends FailurePropatingActor {
  implicit val system = context.system

  implicit val ec: ExecutionContext = system.dispatcher
  var count_Data2 = 100
  def incrementAndPrint { count_Data2 += 100; }
  def receive = {
    case gd2: GetData2 =>
        println(" ++++++ GetData2 Start #"+reportNumber)
        sleepTime match {
          case Some(time) =>
            println("sleep "+time)
            sleep(time)
          case None => ()
        }
        incrementAndPrint
        val res =  new Data2Obj(count_Data2+gd2.data1.count)
        println(" ===== GetData2 End, report#"+reportNumber+", return res="+res)
        //throw new Exception("Exception from Data2Actor.receive.GetData2")
        sender ! res
  }

  def sleep(time: Long): Unit = Thread.sleep(time)
}
