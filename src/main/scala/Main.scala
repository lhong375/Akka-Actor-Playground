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


case class GetData1()
case class GetData2(data1: Data1Obj)
case class GetData3()
case class GetMergedData(data1: Data1Obj, data3: Data3Obj)

case class Data1Obj(count: Int)
case class Data2Obj(count: Int)
case class Data3Obj(count: Int)
case class MergedDataObj(count: Int)





object Main extends App {

  implicit val system       = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit lazy val timeout = Timeout(5.seconds)


  //One way to merge/actors

  val startTime = time
  val dataGActor = system.actorOf(Props(new DataGeneratorActor()), name = "dataGeneratorActor")
  dataGActor ! GenerateDataListSeq(1, startTime, Seq(1,2,3))
  //GenerateDataSeq want actor response to come in sequence
  //dataGActor ! GenerateDataSeq(1, startTime)
  //dataGActor ! GenerateDataSeq(2, startTime)
  //GenerateDataParallel can have actor work in parallel, order doesn't matter
  //dataGActor ! GenerateDataParallel(1, startTime)
  //dataGActor ! GenerateDataParallel(2, startTime)


/*
  //Another way to merge/actors
  val startTime = time

  println("!!!!!reportGeneratorActor Init!!!!!")
  val reportGeneratorActor = system.actorOf(Props(new ReportGeneratorActor()), name = "reportGeneratorActor")
  val report = reportGeneratorActor ? GenerateReport(1)
  println("BEFORE onComplete\n")
  report.onComplete {
    case Success(x) =>
      println("!!!!!reportGeneratorActor Success!!!!! delta="+delta+" report="+x)
    case Failure(e) => e.printStackTrace
  }
  println("AFTER onComplete\n")

  val anotherReport = reportGeneratorActor ? GenerateReport(2)
  println("BEFORE onComplete anotherReport\n")
  anotherReport.onComplete {
    case Success(x) =>
      println("!!!!!reportGeneratorActor for 2nd report Success!!!!! delta="+delta+" report="+x)
    case Failure(e) => e.printStackTrace
  }
  println("AFTER onComplete anotherReport\n")

*/

  sleep(3000)


  def sleep(time: Long): Unit = Thread.sleep(time)

  def delta = System.currentTimeMillis - startTime
  def time = System.currentTimeMillis
}
