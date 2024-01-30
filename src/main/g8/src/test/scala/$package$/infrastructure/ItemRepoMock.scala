package $package$.infrastructure

import $package$.domain._
import zio._
import zio.mock._

object ItemRepoMock extends Mock[ItemRepository]:
  object Add     extends Effect[Item, Nothing, Unit]
  object Delete  extends Effect[ItemId, Nothing, Unit]
  object GetAll  extends Effect[Unit, Nothing, List[Item]]
  object GetById extends Effect[ItemId, Nothing, Option[Item]]
  object Update  extends Effect[(ItemId, ItemData), Nothing, Option[Unit]]

  val compose: URLayer[Proxy, ItemRepository] =
    ZLayer.fromFunction { (proxy: Proxy) =>
      new ItemRepository {
        override def add(item: Item) = proxy(Add, item)

        override def delete(id: ItemId) = proxy(Delete, id)

        override def getAll() = proxy(GetAll)

        override def getById(id: ItemId) = proxy(GetById, id)

        override def update(id: ItemId, data: ItemData) = proxy(Update, id, data)
      }
    }
