import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services._
import logics._
import actors._
import utils.logger._

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    //singletons
    bind(classOf[LogWriter]).asEagerSingleton()
    bind(classOf[TransactionService]).to(classOf[TransactionServiceImp]).asEagerSingleton()
    bind(classOf[BootstrapService]).to(classOf[BootstrapServiceImp]).asEagerSingleton()

    //implementations
    bind(classOf[APIService]).to(classOf[APIServiceImp])
    bind(classOf[RunService]).to(classOf[RunServiceImp])
    bind(classOf[TransactionMonitorService]).to(classOf[TransactionMonitorServiceImp])

    // actors
    bindActor[NotificationManager]("notification-manager")
    bindActor[ArchiveSchelduler]("archive-schelduler")
  }
}