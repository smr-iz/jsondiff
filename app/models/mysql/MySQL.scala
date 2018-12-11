package models.mysql

import java.sql.Connection

import anorm.{ResultSetParser, Row, SimpleSql, SqlParser}
import common.Utils
import javax.inject.{Inject, Singleton}
import models.mysql.MySQLOperation.{Apply, ExecuteInsert, ExecuteUpdate}
import play.api.db.DBApi

/**
  * Created by semir on 9.12.2018.
  */
@Singleton
class MySQL @Inject()(dbAPI: DBApi, utils: Utils) {
  lazy val defaultTimeout = utils.getConfigurationInt("db.default.timeout", 10)

  val dbApi: DBApi = dbAPI

  var readOnly: Boolean = false

  /**
    * Executes a custom operation on the database. Provided for asynchronous execution of custom operations that return a list of [[anorm.Row]]s.
    * @param op operation to be performed.
    * @return a [[scala.collection.immutable.List]] containing the resulting rows
    */
  def apply(op: SimpleSql[Row],
            timeoutSeconds: Int = defaultTimeout): List[Row] = {
    withConnection(timeoutSeconds) {
      implicit conn =>
        Apply(op, timeoutSeconds).perform()
    }
  }

  /**
    * Executes a custom operation on the database. Provided for asynchronous execution of custom operations that return an [[scala.Int]] representing number of rows that were updated.
    * @param op operation to be performed.
    * @return a [[scala.collection.immutable.List]] containing the resulting integer
    */
  def executeUpdate(op: SimpleSql[Row],
                    timeoutSeconds: Int = defaultTimeout): Int = {
    if (readOnly) throw new ReadOnlyException

    withConnection(timeoutSeconds) {
      implicit conn =>
        ExecuteUpdate(op, timeoutSeconds).perform()
    }
  }

  def withConnection[T](timeoutSeconds: Int = defaultTimeout)(block: Connection => T): T = {

     dbApi.database("default").withConnection { implicit connection: Connection =>
      connection.setAutoCommit(true)
      block(connection)
    }
  }

}

case class ReadOnlyException() extends Exception("MySQL DB is in read-only mode!")


