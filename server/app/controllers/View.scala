package controllers

import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import org.json4s._
import org.json4s.native.Serialization.write
import scalikejdbc.SQLInterpolation._
import build.BuildInfo
import com.ponkotuy.data.GetShip

/**
 *
 * @author ponkotuy
 * Date 14/02/24
 */
object View extends Controller {
  implicit val formats = DefaultFormats

  def index = Action.async {
    Future {
      val newest = models.Admiral.findNewest(limit = 20)
      val lvTops = models.Admiral.findAllLvTop(limit = 20)
      Ok(views.html.index(BuildInfo.version, newest, lvTops))
    }
  }

  def about = Action { Ok(views.html.about()) }

  def statistics = Action.async {
    Future {
      val sCounts = models.CreateShip.materialCount().takeWhile(_._2 > 1)
      val iCounts = models.CreateItem.materialCount().takeWhile(_._2 > 1)
      Ok(views.html.sta.statistics(sCounts, iCounts))
    }
  }

  def cship(fuel: Int, ammo: Int, steel: Int, bauxite: Int, develop: Int) = Action.async {
    Future {
      val mat = models.Mat(fuel, ammo, steel, bauxite, develop)
      val counts = models.CreateShip.countByMatWithMaster(mat)
      val title = s"$fuel/$ammo/$steel/$bauxite/$develop"
      val graphJson = cshipGraphJson(counts, title)
      val sum = counts.map(_._2).sum.toDouble
      val withRate = counts.map { case (ship, count) => (ship.name, count, count/sum) }
      val cships = models.CreateShip.findAllByMatWithName(mat, limit = 100)
      Ok(views.html.sta.cship(title, graphJson, withRate, cships))
    }
  }

  private def cshipGraphJson(counts: List[(models.MasterShipBase, Long)], title: String): String = {
    val sum = counts.map(_._2).sum.toDouble
    val sTypeName = models.MasterStype.findAll().map(ms => ms.id -> ms.name).toMap
    val className = models.MasterShipBase.findAllWithClass().map(msb => msb.ctype -> msb.cls).toMap
    val sTypeCounts = counts.groupBy(_._1.stype).mapValues(_.map(_._2).sum)
    val data = sTypeCounts.map { case (stype, sCount) =>
      val classes = counts.filter(_._1.stype == stype)
      val classCounts = classes.groupBy(_._1.ctype).mapValues(_.map(_._2).sum)
      val children = classCounts.map { case (ctype, cCount) =>
        val children = counts.filter(_._1.ctype == ctype).map { case (msb, count) =>
          Map("name" -> s"${msb.name} $count(${toP(count/sum)}%)", "count" -> count)
        }
        Map("name" -> s"${className(ctype)} $cCount(${toP(cCount/sum)}%)", "children" -> children)
      }
      Map("name" -> s"${sTypeName(stype)} $sCount(${toP(sCount/sum)}%)", "children" -> children)
    }
    write(Map("name" -> title, "children" -> data))
  }

  private def toP(d: Double): String = f"${d*100}%.1f"

  def citem(fuel: Int, ammo: Int, steel: Int, bauxite: Int, sType: Int) = Action.async {
    Future {
      val mat = models.ItemMat(fuel, ammo, steel, bauxite, sType, "")
      val citems = models.CreateItem.findAllByWithName(
        sqls"ci.fuel = $fuel and ci.ammo = $ammo and ci.steel = $steel and ci.bauxite = $bauxite and ms.stype = $sType",
        limit = 100
      )
      val counts = models.CreateItem.countItemByMat(mat)
      val sum = counts.map(_._2).sum.toDouble
      val withRate = counts.map { case (item, count) => (item.name, count, count/sum) }
      val countJsonRaw = counts.map { case (item, count) =>
        Map("label" -> item.name, "data" -> count)
      }
      val st = models.MasterStype.find(sType).get
      val title = s"${st.name}/$fuel/$ammo/$steel/$bauxite"
      Ok(views.html.sta.citem(title, write(countJsonRaw), withRate, citems))
    }
  }

  def fromShip(q: String) = Action.async {
    Future(Ok(views.html.sta.from_ship(q)))
  }

  def dropStage() = Action.async {
    Future {
      val stages = models.BattleResult.countAllByStage()
      Ok(views.html.sta.drop_stage(stages))
    }
  }

  def drop(area: Int, info: Int) = Action.async {
    Future {
      val drops = models.BattleResult.countAllGroupByDrop(area, info)
      val byCell: Seq[(models.CellInfo, List[(Option[GetShip], Long, String)])] =
        drops.groupBy(_._1.point).mapValues { xs =>
          val sum = xs.map(_._2).sum.toDouble
          xs.map { case (drop, count) =>
            val rate = f"${count / sum * 100}%.1f%%"
            (drop.getShip, count, rate)
          }.reverse
        }.toSeq.sortBy(_._1).map { case ((a, i, c), rest) =>
          val cellInfo = models.CellInfo.find(a, i, c)
            .getOrElse(models.CellInfo(a, i, c, "", false, false))
          cellInfo -> rest
        }
      Ok(views.html.sta.drop(s"$area-$info", byCell))
    }
  }
}
