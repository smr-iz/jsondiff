package common

import javax.inject.{Inject, Singleton}
import play.api.Configuration

/**
  * Created by semir on 9.12.2018.
  */
@Singleton
class Utils @Inject()(val configuration: Configuration) {

  def getConfigurationInt(key: String, defaultValue: Int): Int = {
    configuration.getInt(key).getOrElse(defaultValue)
  }

  def getConfigurationInt(key: String): Option[Int] = {
    configuration.getInt(key)
  }
}

