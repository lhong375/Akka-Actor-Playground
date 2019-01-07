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
  One way to chain/merge actors
  This actor expect 2 type of message
  GenerateDataSeq : will call Data1Actor, then pass the response to Data2Actor, then collect response from Data2Actor
  GenerateDataParallel: will call Data1Actor and Data3Actor, in parallel, then call MergeActor to collect response from those 2
*/
case class GenerateDataSeq(reportNumber: Int, startTime: Long)
case class GenerateDataParallel(reportNumber: Int, startTime: Long)
case class GenerateDataListSeq(reportNumber: Int, startTime: Long, inputList: Seq[Int])
class DataGeneratorActor extends Actor {
  implicit val system = context.system
  implicit lazy val timeout = Timeout(5.seconds)

  implicit val ec: ExecutionContext = system.dispatcher
  override def receive: Receive = {
    case message: GenerateDataListSeq => {
      val initSeq = Seq[Future[Data1Obj]]()

      val resSeq = message.inputList.foldLeft(initSeq)((acc, item) => {
        val data1Actor = context.actorOf(Props(new Data1Actor(message.reportNumber)))
        val data1ObjFuture: Future[Data1Obj] = (data1Actor ? GetData1()).mapTo[Data1Obj]
        acc:+data1ObjFuture
      })
      val originalSender = sender()
      val all = Future.sequence(resSeq)
      all.onComplete {
        case Success(res) =>
          println("DataGeneratorActor GenerateDataListSeq Success "+res)
          originalSender ! res
        case Failure(ex) =>
          println("DataGeneratorActor GenerateDataListSeq Failure "+ex)
      }
    }

    case message: GenerateDataSeq => {
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
    }
    case message: GenerateDataParallel => {
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
          println("DataGeneratorActor GenerateDataParallel inside Success, but we are not done yet")
          x.mapTo[MergedDataObj].map(mergedDataObj =>
            println("DataGeneratorActor GenerateDataParallel mergedDataObj="+mergedDataObj+" reportNumber="+message.reportNumber+", delta="+(System.currentTimeMillis-message.startTime))
          )
        case Failure(e) => e.printStackTrace
      }
    }
  }

}
