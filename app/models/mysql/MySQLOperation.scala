package models.mysql

import java.sql.Connection

import anorm.{ResultSetParser, Row, SimpleSql}

/**
  * Created by semir on 9.12.2018.
  */
trait MySQLOperation[T] {
  def perform()(implicit c: Connection): T
}

object MySQLOperation {

  case class Apply(op: SimpleSql[Row],
                   timeoutSeconds: Int) extends MySQLOperation[List[Row]] {
    def perform()(implicit c: Connection): List[Row] = {
      op.withQueryTimeout(Some(timeoutSeconds)).as(op.defaultParser.*)
    }
  }

  case class ExecuteUpdate(op: SimpleSql[Row],
                           timeoutSeconds: Int) extends MySQLOperation[Int] {
    def perform()(implicit c: Connection): Int = {
      op.withQueryTimeout(Some(timeoutSeconds)).executeUpdate()
    }
  }

  case class ExecuteInsert[A](op: SimpleSql[Row],
                              timeoutSeconds: Int,
                              generatedKeysParser: ResultSetParser[A]) extends MySQLOperation[A] {
    def perform()(implicit c: Connection): A = {
      op.withQueryTimeout(Some(timeoutSeconds)).executeInsert(generatedKeysParser)
    }
  }
}
