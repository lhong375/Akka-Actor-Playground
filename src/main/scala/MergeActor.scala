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

//take a Int(reportNumber) to create this actor, expect message GetMergedData, GetMergedData should come with {data1:Int, data3:Int}, return obj MergedDataObj as a sum of those 2 Int
class MergeActor(reportNumber: Int) extends Actor {
  implicit val system = context.system

  implicit val ec: ExecutionContext = system.dispatcher
  var count_Merge = 0
  def incrementAndPrint { count_Merge += 1; }
  override def receive = {
    case gmd: GetMergedData =>
        println(" >>>>> GetMergedData report#"+reportNumber)
        incrementAndPrint
        val res:MergedDataObj =  new MergedDataObj(gmd.data1.count + gmd.data3.count)
        sender ! res
  }

}
