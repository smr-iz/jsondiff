package helper

import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

/**
  * Created by semir on 10.12.2018.
  */
class BaseSpec extends PlaySpec with OneAppPerSuite {
  implicit override lazy val app: Application = new GuiceApplicationBuilder().build()
}
