package tool

import models.db.{MapRoute, CellInfo, BattleResult, Basic}
import com.github.nscala_time.time.Imports._
import models.join.Stage
import scalikejdbc._

/**
  * Date:12/23/15.
  * @author ponkotuy
  */
case class BattleScore(monthlyExp: Int, yearlyExp: Int, eo: Int, lastEo: Int) {
  def pp: String = {
    s"${sum}(${toString})"
  }

  def sum: Int = monthlyExp + yearlyExp + eo + lastEo
}

object BattleScore {
  case class FromExp(monthly: Int, yearly: Int)

  object FromExp {
    def empty = FromExp(0, 0)
  }

  def calcFromMemberId(memberId: Long): BattleScore = {
    val exp = fromExp(memberId)
    val now = DateTime.now()
    val eo = calcEo(memberId, new Interval(monthHead(now), now))
    val lastEo = calcEo(memberId, new Interval(monthHead(now - 1.month), monthHead(now))) / 35
    BattleScore(exp.monthly, exp.yearly, eo, lastEo)
  }

  private def fromExp(memberId: Long): FromExp = {
    val b = Basic.b
    val now = DateTime.now()
    Basic.findByUser(memberId).map(_.experience).fold(FromExp.empty) { nowExp =>
      val lastMonthExp =
        Basic.findExpBy(sqls.eq(b.memberId, memberId).and.lt(b.created, monthHead(now).getMillis), b.created.desc).getOrElse(nowExp)
      val lastYearExp =
        Basic.findExpBy(sqls.eq(b.memberId, memberId).and.lt(b.created, yearHead(now).getMillis), b.created.desc).getOrElse(lastMonthExp)
      val monthly = (nowExp - lastMonthExp) * 7 / 10000
      val yearly = (lastMonthExp - lastYearExp) / 50000
      FromExp(monthly, yearly)
    }
  }

  private def calcEo(memberId: Long, interval: Interval): Int = {
    StageInfo.values.map { info =>
      val count = if(info.boss) {
        clearCountFromBattle(memberId, info.stage, interval)
      } else {
        clearCountFromRoute(memberId, info.stage, interval)
      }
      if (info.clear <= count) info.score else 0
    }.sum
  }

  private def clearCountFromBattle(memberId: Long, stage: Stage, interval: Interval): Long = {
    val br = BattleResult.br
    val bosses = findBossCells(stage)
    BattleResult.countBy(
      sqls.eq(br.memberId, memberId)
          .and.eq(br.areaId, stage.area)
          .and.eq(br.infoNo, stage.info)
          .and.in(br.cell, bosses)
          .and.in(br.winRank, Seq("S", "A", "B")) // 厳密には勝利しただけでカウントは進まないが、終わったのに終わってないと判断される方が怖いのでSABまで入れる
          .and(intervalToSQLSyntax(br.created, interval))
    )
  }

  private def clearCountFromRoute(memberId: Long, stage: Stage, interval: Interval): Long = {
    val mr = MapRoute.mr
    val bosses = findBossCells(stage)
    MapRoute.countBy(
      sqls.eq(mr.memberId, memberId)
          .and.eq(mr.areaId, stage.area)
          .and.eq(mr.infoNo, stage.info)
          .and.in(mr.dest, bosses)
          .and(intervalToSQLSyntax(mr.created, interval))
    )
  }

  private def findBossCells(stage: Stage): Seq[Int] = {
    val ci = CellInfo.ci
    val cell = CellInfo.findAllBy(sqls.eq(ci.areaId, stage.area).and.eq(ci.infoNo, stage.info))
    cell.filter(_.boss).map(_.cell)
  }

  private def monthHead(now: DateTime): DateTime =
    new LocalDate(now.getYear, now.getMonthOfYear, 1).toDateTime(LocalTime.MIDNIGHT)
  private def yearHead(now: DateTime): DateTime =
    new LocalDate(now.getYear, 1, 1).toDateTime(LocalTime.MIDNIGHT)

  /**
    * BIGINTカラムの範囲指定を生成
    */
  private def intervalToSQLSyntax(column: SQLSyntax, interval: Interval): SQLSyntax =
    sqls.gt(column, interval.startMillis).and.lt(column, interval.endMillis)
}

/**
  * @param score Clear score
  * @param clear Required clear count
  * @param boss Is exists boss
  */
case class StageInfo(stage: Stage, score: Int, clear: Int, boss: Boolean)

object StageInfo {
  val values = Vector(
    StageInfo(Stage(1, 5), 75, 4, true),
    StageInfo(Stage(1, 6), 75, 7, false),
    StageInfo(Stage(2, 5), 100, 4, true),
    StageInfo(Stage(3, 5), 150, 4, true),
    StageInfo(Stage(4, 5), 180, 5, true),
    StageInfo(Stage(5, 5), 200, 5, true)
  )
}
