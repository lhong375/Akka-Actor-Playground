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

case class GetData3()
case class GetMergedData(data1: Data1Obj, data3: Data3Obj)

case class Data1Obj(count: Int)

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

//  val resList = dataGActor ? GenerateDataListSeq(1, startTime, Seq(1,2,3))
//  resList.onComplete {
//    case Success(x) =>
//      println("##### GenerateDataListSeq Success with res:"+x)
//  }

  //Actor response get assembled in seq
  dataGActor ! GenerateDataSeq(1, startTime)

//  val res = dataGActor ? GenerateDataSeq2(1, startTime)
//  val data2ObjFuture: Future[Data2Obj] = res.mapTo[Data2Obj]
//  data2ObjFuture.onComplete {
//    case Success(data2obj) =>
//      println("DataGeneratorActor GenerateDataSeq res="+data2obj)
//    case Failure(e) => e.printStackTrace
//  }

  //merge response of actors in a parallel way
  //dataGActor ! GenerateDataParallel(1, startTime)


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
