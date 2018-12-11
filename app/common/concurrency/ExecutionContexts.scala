package common.concurrency

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}

/**
  * Created by semir on 8.12.2018.
  */
@Singleton
class ExecutionContexts @Inject()(as: ActorSystem){
  implicit val genericOps = as.dispatchers.lookup("contexts.generic-ops")
}
