package jobs

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import play.api.inject.{SimpleModule, _}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class PruneJob @Inject()(actorSystem: ActorSystem, @Named("transaction-pruner") pruner: ActorRef)(implicit executionContext: ExecutionContext) {

  actorSystem.scheduler.schedule(
    initialDelay = 0 microsecond,
    interval = 60 seconds,
    receiver = pruner,
    message = "tick"
  )

}

class PruneModule extends SimpleModule(bind[PruneJob].toSelf.eagerly())
