package models

import dat.{ShipSnapshotRest, ShipSnapshotWithName}
import scalikejdbc._
import util.scalikejdbc.BulkInsert._

/**
 *
 * @param slot: Shipとは異なり、直接MasterのIDを参照している
 */
class DeckShipSnapshot(
  val id: Long,
  val memberId: Long,
  val deckId: Long,
  val num: Short,
  val shipId: Int,
  val lv: Short,
  val exp: Int,
  val nowhp: Short,
  val slot: List[Int],
  val fuel: Int,
  val bull: Int,
  val dockTime: Long,
  val cond: Short,
  val karyoku: Short,
  val raisou: Short,
  val taiku: Short,
  val soukou: Short,
  val kaihi: Short,
  val taisen: Short,
  val sakuteki: Short,
  val lucky: Short,
  val locked: Boolean,
  val created: Long,
  val maxhp: Short) {

  def copy(
    id: Long = this.id,
    memberId: Long = this.memberId,
    deckId: Long = this.deckId,
    num: Short = this.num,
    shipId: Int = this.shipId,
    lv: Short = this.lv,
    exp: Int = this.exp,
    nowhp: Short = this.nowhp,
    slot: List[Int] = this.slot,
    fuel: Int = this.fuel,
    bull: Int = this.bull,
    dockTime: Long = this.dockTime,
    cond: Short = this.cond,
    karyoku: Short = this.karyoku,
    raisou: Short = this.raisou,
    taiku: Short = this.taiku,
    soukou: Short = this.soukou,
    kaihi: Short = this.kaihi,
    taisen: Short = this.taisen,
    sakuteki: Short = this.sakuteki,
    lucky: Short = this.lucky,
    locked: Boolean = this.locked,
    created: Long = this.created,
    maxhp: Short = this.maxhp): DeckShipSnapshot = {
    new DeckShipSnapshot(
      id = id,
      memberId = memberId,
      deckId = deckId,
      num = num,
      shipId = shipId,
      lv = lv,
      exp = exp,
      nowhp = nowhp,
      slot = slot,
      fuel = fuel,
      bull = bull,
      dockTime = dockTime,
      cond = cond,
      karyoku = karyoku,
      raisou = raisou,
      taiku = taiku,
      soukou = soukou,
      kaihi = kaihi,
      taisen = taisen,
      sakuteki = sakuteki,
      lucky = lucky,
      locked = locked,
      created = created,
      maxhp = maxhp)
  }

  def save()(implicit session: DBSession = DeckShipSnapshot.autoSession): DeckShipSnapshot = DeckShipSnapshot.save(this)(session)

  def destroy()(implicit session: DBSession = DeckShipSnapshot.autoSession): Unit = DeckShipSnapshot.destroy(this)(session)

  def toShip: Ship = {
    Ship(id.toInt, shipId, memberId, lv, exp, nowhp, slot, fuel, bull, dockTime, cond, karyoku, raisou, taiku, soukou, kaihi, taisen, sakuteki, lucky, locked, created, maxhp)
  }

}


object DeckShipSnapshot extends SQLSyntaxSupport[DeckShipSnapshot] {

  override val tableName = "deck_ship_snapshot"

  override val columns = Seq("id", "member_id", "deck_id", "num", "ship_id", "lv", "exp", "nowhp", "slot", "fuel", "bull", "dock_time", "cond", "karyoku", "raisou", "taiku", "soukou", "kaihi", "taisen", "sakuteki", "lucky", "locked", "created", "maxhp")

  def apply(dss: SyntaxProvider[DeckShipSnapshot])(rs: WrappedResultSet): DeckShipSnapshot = apply(dss.resultName)(rs)
  def apply(dss: ResultName[DeckShipSnapshot])(rs: WrappedResultSet): DeckShipSnapshot = new DeckShipSnapshot(
    id = rs.get(dss.id),
    memberId = rs.get(dss.memberId),
    deckId = rs.get(dss.deckId),
    num = rs.get(dss.num),
    shipId = rs.get(dss.shipId),
    lv = rs.get(dss.lv),
    exp = rs.get(dss.exp),
    nowhp = rs.get(dss.nowhp),
    slot = rs.string(dss.slot).split(',').toList.map(_.toInt),
    fuel = rs.get(dss.fuel),
    bull = rs.get(dss.bull),
    dockTime = rs.get(dss.dockTime),
    cond = rs.get(dss.cond),
    karyoku = rs.get(dss.karyoku),
    raisou = rs.get(dss.raisou),
    taiku = rs.get(dss.taiku),
    soukou = rs.get(dss.soukou),
    kaihi = rs.get(dss.kaihi),
    taisen = rs.get(dss.taisen),
    sakuteki = rs.get(dss.sakuteki),
    lucky = rs.get(dss.lucky),
    locked = rs.get(dss.locked),
    created = rs.get(dss.created),
    maxhp = rs.get(dss.maxhp)
  )

  val dss = DeckShipSnapshot.syntax("dss")
  val ms = MasterShipBase.syntax("ms")
  val mst = MasterStype.syntax("mst")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[DeckShipSnapshot] = {
    withSQL {
      select.from(DeckShipSnapshot as dss).where.eq(dss.id, id)
    }.map(DeckShipSnapshot(dss.resultName)).single().apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[DeckShipSnapshot] = {
    withSQL(select.from(DeckShipSnapshot as dss)).map(DeckShipSnapshot(dss.resultName)).list().apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(DeckShipSnapshot as dss)).map(rs => rs.long(1)).single().apply().get
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[DeckShipSnapshot] = {
    withSQL {
      select.from(DeckShipSnapshot as dss).where.append(sqls"${where}")
    }.map(DeckShipSnapshot(dss.resultName)).list().apply()
  }

  def findAllByWithName(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ShipSnapshotWithName] = {
    withSQL {
      select.from(DeckShipSnapshot as dss)
        .innerJoin(MasterShipBase as ms).on(dss.shipId, ms.id)
        .innerJoin(MasterStype as mst).on(ms.stype, mst.id)
        .where.append(sqls"${where}")
        .orderBy(dss.num)
    }.map { rs =>
      val ship = DeckShipSnapshot(dss)(rs).toShip
      ShipSnapshotWithName(ship, MasterShipBase(ms)(rs), MasterStype(mst)(rs), ShipSnapshotRest(dss)(rs))
    }.list().apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls"count(1)").from(DeckShipSnapshot as dss).where.append(sqls"${where}")
    }.map(_.long(1)).single().apply().get
  }

  def create(
    memberId: Long,
    deckId: Long,
    num: Short,
    shipId: Int,
    lv: Short,
    exp: Int,
    nowhp: Short,
    slot: List[Int],
    fuel: Int,
    bull: Int,
    dockTime: Long,
    cond: Short,
    karyoku: Short,
    raisou: Short,
    taiku: Short,
    soukou: Short,
    kaihi: Short,
    taisen: Short,
    sakuteki: Short,
    lucky: Short,
    locked: Boolean,
    created: Long,
    maxhp: Short)(implicit session: DBSession = autoSession): DeckShipSnapshot = {
    val generatedKey = withSQL {
      insert.into(DeckShipSnapshot).columns(
        column.memberId,
        column.deckId,
        column.num,
        column.shipId,
        column.lv,
        column.exp,
        column.nowhp,
        column.slot,
        column.fuel,
        column.bull,
        column.dockTime,
        column.cond,
        column.karyoku,
        column.raisou,
        column.taiku,
        column.soukou,
        column.kaihi,
        column.taisen,
        column.sakuteki,
        column.lucky,
        column.locked,
        column.created,
        column.maxhp
      ).values(
          memberId,
          deckId,
          num,
          shipId,
          lv,
          exp,
          nowhp,
          slot.mkString(","),
          fuel,
          bull,
          dockTime,
          cond,
          karyoku,
          raisou,
          taiku,
          soukou,
          kaihi,
          taisen,
          sakuteki,
          lucky,
          locked,
          created,
          maxhp
        )
    }.updateAndReturnGeneratedKey().apply()

    new DeckShipSnapshot(
      id = generatedKey,
      memberId = memberId,
      deckId = deckId,
      num = num,
      shipId = shipId,
      lv = lv,
      exp = exp,
      nowhp = nowhp,
      slot = slot,
      fuel = fuel,
      bull = bull,
      dockTime = dockTime,
      cond = cond,
      karyoku = karyoku,
      raisou = raisou,
      taiku = taiku,
      soukou = soukou,
      kaihi = kaihi,
      taisen = taisen,
      sakuteki = sakuteki,
      lucky = lucky,
      locked = locked,
      created = created,
      maxhp = maxhp)
  }

  def bulkInsert(ss: Seq[Ship], deckId: Long)(implicit session: DBSession = autoSession): Unit = {
    require(ss.nonEmpty)
    val created = System.currentTimeMillis()
    val memberId = ss.head.memberId
    val userSlots = SlotItem.findAllBy(sqls"member_id = ${memberId}").map { item =>
      item.id -> item.slotitemId
    }.toMap
    val slots: Seq[String] = ss.map { ship =>
      ship.slot.map(userSlots.apply).mkString(",")
    }
    applyUpdate {
      insert.into(DeckShipSnapshot).columns(
        column.memberId, column.deckId, column.num, column.shipId,
        column.lv, column.exp, column.nowhp, column.slot,
        column.fuel, column.bull, column.dockTime, column.cond,
        column.karyoku, column.raisou, column.taiku, column.soukou,
        column.kaihi, column.taisen, column.sakuteki, column.lucky,
        column.locked, column.created, column.maxhp
      ).multiValues(
          Seq.fill(ss.size)(memberId), Seq.fill(ss.size)(deckId), (1 to ss.size).toSeq, ss.map(_.shipId),
          ss.map(_.lv), ss.map(_.exp), ss.map(_.nowhp), slots,
          ss.map(_.fuel), ss.map(_.bull), ss.map(_.dockTime), ss.map(_.cond),
          ss.map(_.karyoku), ss.map(_.raisou), ss.map(_.taiku), ss.map(_.soukou),
          ss.map(_.kaihi), ss.map(_.taisen), ss.map(_.sakuteki), ss.map(_.lucky),
          ss.map(_.locked), Seq.fill(ss.size)(created), ss.map(_.maxhp)
        )
    }
  }

  def save(entity: DeckShipSnapshot)(implicit session: DBSession = autoSession): DeckShipSnapshot = {
    withSQL {
      update(DeckShipSnapshot).set(
        column.id -> entity.id,
        column.memberId -> entity.memberId,
        column.deckId -> entity.deckId,
        column.num -> entity.num,
        column.shipId -> entity.shipId,
        column.lv -> entity.lv,
        column.exp -> entity.exp,
        column.nowhp -> entity.nowhp,
        column.slot -> entity.slot,
        column.fuel -> entity.fuel,
        column.bull -> entity.bull,
        column.dockTime -> entity.dockTime,
        column.cond -> entity.cond,
        column.karyoku -> entity.karyoku,
        column.raisou -> entity.raisou,
        column.taiku -> entity.taiku,
        column.soukou -> entity.soukou,
        column.kaihi -> entity.kaihi,
        column.taisen -> entity.taisen,
        column.sakuteki -> entity.sakuteki,
        column.lucky -> entity.lucky,
        column.locked -> entity.locked,
        column.created -> entity.created,
        column.maxhp -> entity.maxhp
      ).where.eq(column.id, entity.id)
    }.update().apply()
    entity
  }

  def destroy(entity: DeckShipSnapshot)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(DeckShipSnapshot).where.eq(column.id, entity.id)
    }.update().apply()
  }

}
