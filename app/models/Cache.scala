package models

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import javax.inject.Inject
import play.api.Logger
import play.api.cache.CacheApi

import scala.concurrent.duration.Duration

/**
  * Created by semir on 8.12.2018.
  */
class Cache @Inject()(cacheApi: CacheApi) {
  val cacheTTL: Long = ConfigFactory.load.getLong("cache.ttl")

  /**
    * Get data from play-cache with given key
    * @param cacheKey
    * @tparam T
    * @return
    */
  def getData[T](cacheKey: String): Option[T] = {
    try {
      val cacheDataAsOpt: Option[T] = cacheApi.get(cacheKey).asInstanceOf[Option[T]]
      cacheDataAsOpt
    } catch {
      case e: Exception =>
        Logger.error(s"Failed to get cache data for key: $cacheKey with error: $e")
        None
    }
  }

  /**
    * Save given data to play-cache
    * @param cacheKey
    * @param cacheData
    * @tparam T
    * @return
    */
  def setData[T](cacheKey: String, cacheData: T): Boolean = {
    try {
      cacheApi.set(cacheKey, cacheData, Duration(cacheTTL, TimeUnit.SECONDS))
      true
    } catch {
      case e: Exception =>
        Logger.error(s"Failed to set cache for key: $cacheKey with error: $e")
        false
    }
  }
}


