import akka.actor._
import scala.concurrent.{ ExecutionContext, Future }
import akka.pattern.ask

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
//import akka.stream.scaladsl._
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
        val res =  new Data2Obj(count_Data1)
        println(" ***** GetData1 End, report#"+reportNumber+", return count_Data1="+count_Data1)
        sender ! res
  }

  def sleep(time: Long): Unit = Thread.sleep(time)
}

class Data2Actor(reportNumber: Int) extends Actor {
  implicit val system = context.system

  implicit val ec: ExecutionContext = system.dispatcher
  var count_Data2 = 100
  def incrementAndPrint { count_Data2 += 100; /*println(" +++++ Data2#"+count_Data2)*/ }
  def receive = {
    case gd2: GetData2 =>
        println(" ++++++ GetData2 Start #"+reportNumber)
        sleep(250)
        incrementAndPrint
        val res =  new Data2Obj(count_Data2+gd2.data1.count)
        println(" ===== GetData2 End, report#"+reportNumber+", return res="+res)
        sender ! res
  }

  def sleep(time: Long): Unit = Thread.sleep(time)
}

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

class MergeActor(reportNumber: Int) extends Actor {
  implicit val system = context.system

  implicit val ec: ExecutionContext = system.dispatcher
  var count_Merge = 0
  def incrementAndPrint { count_Merge += 1; /*println(" >>>>> Merge#"+count_Merge)*/ }
  override def receive = {
    case gmd: GetMergedData =>
        println(" >>>>> GetMergedData report#"+reportNumber)
        incrementAndPrint
        val res:MergedDataObj =  new MergedDataObj(gmd.data1.count + gmd.data3.count)
        sender ! res
  }

}

case class GenerateDataSeq(reportNumber: Int, startTime: Long)
case class GenerateDataParallel(reportNumber: Int, startTime: Long)
class DataGeneratorActor extends Actor {
  implicit val system = context.system
  implicit lazy val timeout = Timeout(5.seconds)

  implicit val ec: ExecutionContext = system.dispatcher
  override def receive: Receive = {
    case message: GenerateDataSeq =>
      val data1Actor = context.actorOf(Props(new Data1Actor(message.reportNumber)))
      val data1ObjFuture: Future[Data1Obj] = (data1Actor ? GetData1()).mapTo[Data1Obj]

      val data2Actor = context.actorOf(Props(new Data2Actor(message.reportNumber)))
      val anyFuture = for {
        data1Obj <- data1ObjFuture
      } yield data2Actor ? GetData2(data1Obj)

      anyFuture.onComplete {
        case Success(x) =>
          println("DataGeneratorActor GenerateDataSeq inside Success, but we are not done yet")
          //x.pipeTo(sender())
          x.mapTo[Data2Obj].map(data2obj =>
            println("DataGeneratorActor GenerateDataSeq res="+data2obj+" reportNumber="+message.reportNumber+", delta="+(System.currentTimeMillis-message.startTime))
          )
        case Failure(e) => e.printStackTrace
      }
      //sender() ! anyFuture
      //anyFuture.pipeTo(sender())

    case message: GenerateDataParallel =>
      val data1Actor = context.actorOf(Props(new Data1Actor(message.reportNumber)))
      val data1ObjFuture: Future[Data1Obj] = (data1Actor ? GetData1()).mapTo[Data1Obj]
      val data3Actor = context.actorOf(Props(new Data3Actor(message.reportNumber)))
      val data3ObjFuture: Future[Data3Obj] = (data3Actor ? GetData3()).mapTo[Data3Obj]
      val dataMergeActor = context.actorOf(Props(new MergeActor(message.reportNumber)))

      val report = for {
        data1ObjResult <- data1ObjFuture
        data3ObjResult <- data3ObjFuture
      } yield dataMergeActor ? GetMergedData(data1ObjResult, data3ObjResult)


      report.onComplete {
        case Success(x) =>
          println("DataGeneratorActor GenerateDataParallel Success!")
          //x.pipeTo(sender())
          x.mapTo[MergedDataObj].map(mergedDataObj =>
            println("DataGeneratorActor GenerateDataParallel mergedDataObj="+mergedDataObj+" reportNumber="+message.reportNumber+", delta="+(System.currentTimeMillis-message.startTime))
          )
        case Failure(e) => e.printStackTrace
      }
  }

}

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

case class Message1()
case class Message2()
class TestActor() extends Actor {
  def receive: Receive = {
    case message: Message1 => {
      println("Message1");
    }
    case message: Message2 => {
      println("Message2");
    }
    case _ => {
      println("Bad Message")
    }
  }
}

object Main extends App {

  implicit val system       = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit lazy val timeout = Timeout(5.seconds)

/*
  val startTime = time
  val dataGActor = system.actorOf(Props(new DataGeneratorActor()), name = "dataGeneratorActor")
  //dataGActor ! GenerateDataSeq(1, startTime)
  //dataGActor ! GenerateDataSeq(2, startTime)
  dataGActor ! GenerateDataParallel(1, startTime)
  //dataGActor ! GenerateDataParallel(2, startTime)
*/


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
      println("!!!!!2nd reportGeneratorActor Success!!!!! delta="+delta+" report="+x)
    case Failure(e) => e.printStackTrace
  }
  println("AFTER onComplete anotherReport\n")



  sleep(3000)


  def sleep(time: Long): Unit = Thread.sleep(time)

  def delta = System.currentTimeMillis - startTime
  def time = System.currentTimeMillis
}
