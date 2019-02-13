package jobs

import akka.actor.{ActorRef, ActorSystem}
import javax.inject.{Inject, Named}
import play.api.inject.{SimpleModule, _}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class OffLordJob @Inject()(actorSystem: ActorSystem, @Named("offload-schelduler") schelduler: ActorRef)(implicit executionContext: ExecutionContext) {

  actorSystem.scheduler.schedule(
    initialDelay = 0 microsecond,
    interval = 60 seconds,
    receiver = schelduler,
    message = "tx_check_tick"
  )

}

class OffloadModule extends SimpleModule(bind[OffLordJob].toSelf.eagerly())