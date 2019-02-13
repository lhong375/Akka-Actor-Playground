package akka.actor.playground
import akka.actor._
import scala.concurrent.{ ExecutionContext, Future }
import akka.pattern.{ ask, pipe }

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.Future

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.util.Timeout
import scala.util.{Failure, Success}

import util.Random.nextInt

/**
  One way to chain/merge actors
  This actor expect 2 type of message
  GenerateDataSeq : will call Data1Actor, then pass the response to Data2Actor, then collect response from Data2Actor
  GenerateDataParallel: will call Data1Actor and Data3Actor, in parallel, then call MergeActor to collect response from those 2
*/
case class GenerateDataSeq(reportNumber: Int, startTime: Long)
case class GenerateDataSeq2(reportNumber: Int, startTime: Long)
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
        val data1Actor = context.actorOf(Props(new Data1Actor(message.reportNumber, None)))
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
      val data1Actor = context.actorOf(Props(new Data1Actor(message.reportNumber, None)))
      val data1ObjFuture: Future[Data1Obj] = (data1Actor ? GetData1()).mapTo[Data1Obj]

      val data2Actor = context.actorOf(Props(new Data2Actor(message.reportNumber, None)))

      val originalSender = sender()
      val anyFuture = data1ObjFuture.onComplete{
        case Success(x) =>
          val data2ObjFuture = data2Actor ? GetData2(x)
          data2ObjFuture.pipeTo(originalSender)
        case Failure(e) => e.printStackTrace
      }
//      val anyFuture = for {
//        data1Obj <- data1ObjFuture
//      } yield data2Actor ? GetData2(data1Obj)

//      anyFuture.onComplete {
//        case Success(x) =>
//          println("DataGeneratorActor GenerateDataSeq inside Success, but we are not done yet")
//          x.mapTo[Data2Obj].map(data2obj =>
//            println("DataGeneratorActor GenerateDataSeq res="+data2obj+" reportNumber="+message.reportNumber+", delta="+(System.currentTimeMillis-message.startTime))
//          )
//        case Failure(e) => e.printStackTrace
//      }
    }
    case message: GenerateDataSeq2 => {
        val originalSender = sender()
        val data1Actor = context.actorOf(Props(new Data1Actor(message.reportNumber, None)))
        val data2Actor = context.actorOf(Props(new Data2Actor(message.reportNumber, None)))
        //data2Actor depends on data1Obj to create data2Obj, this will run in seq
        val result = for {
        data1Obj: Data1Obj <- (data1Actor ? GetData1()).mapTo[Data1Obj]
        data2Obj <- data2Actor ? GetData2(data1Obj)
        } yield data2Obj

        /* error handling with .recover
        result.recover{
          case exp:Exception =>
            println("got Exception", exp)
            Future.failed(exp).pipeTo(originalSender)
          case otherExp =>
            println("got Other Exception", otherExp)
            Future.failed(otherExp).pipeTo(originalSender)
        }
        */

        result.pipeTo(originalSender)
    }
    case message: GenerateDataParallel => {
      val data1Actor = context.actorOf(Props(new Data1Actor(message.reportNumber, Some(nextInt(1000)))))
      val data3Actor = context.actorOf(Props(new Data3Actor(message.reportNumber, Some(nextInt(1000)))))
      val dataMergeActor = context.actorOf(Props(new MergeActor(message.reportNumber)))
      //make sure we define the future response here, not in the comprehensive, otherwise it will run in seq
      val data1ObjFuture: Future[Data1Obj] = (data1Actor ? GetData1()).mapTo[Data1Obj]
      val data3ObjFuture: Future[Data3Obj] = (data3Actor ? GetData3()).mapTo[Data3Obj]

      val report = for {
        data1ObjResult <- data1ObjFuture
        data3ObjResult <- data3ObjFuture
      } yield dataMergeActor ? GetMergedData(data1ObjResult, data3ObjResult)

//      above is the same as following:
//      data1ObjFuture.flatMap(data1ObjResult => data3ObjFuture.map(data3ObjResult => dataMergeActor ? GetMergedData(data1ObjResult, data3ObjResult)))



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
