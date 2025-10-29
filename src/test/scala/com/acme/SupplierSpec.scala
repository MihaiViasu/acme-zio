package com.acme

import com.acme.domain.{ConveyorBelt, Supplier}
import com.acme.domain.live.{ConveyorBeltLive, SupplierLive}
import com.acme.model.Component.{MainUnit, Mop}
import zio.ZIO
import zio.test._
import zio.test.Assertion._
import zio.durationInt


object SupplierSpec extends ZIOSpecDefault:

  def spec = suite("SupplierLiveSpec") (

    test("should add a few components - parallel") {
      for {
        belt <- ZIO.service[ConveyorBelt]
        supplier <- ZIO.service[Supplier]
        fibers <- ZIO.foreachPar(1 to ConveyorBeltLive.capacity)(_ => supplier.put().fork)
        _ <- TestClock.adjust(2.seconds)
        _ <- ZIO.foreach(fibers)(_.join)
        full <- belt.peekAll()
      } yield assertTrue(full.size == ConveyorBeltLive.capacity)
    },
    test("should destroy an item after 10 seconds of not being enable to add another one to the belt") {
      for {
        belt <- ZIO.service[ConveyorBelt]
        supplier <- ZIO.service[Supplier]
        _ <- ZIO.foreachPar(1 to ConveyorBeltLive.capacity)(_ => belt.offer(MainUnit))
        full <- belt.peekAll()
        fiber <- supplier.put().fork
        _ <- TestClock.adjust(11.seconds)
        _ <- fiber.join
        remaining <- belt.peekAll()
        fiber2 <- supplier.put().fork
        _ <- TestClock.adjust(2.seconds)
        _ <- fiber2.join
        fullAgain <- belt.peekAll()
      } yield assertTrue(
        full.size == ConveyorBeltLive.capacity,
        remaining.size == ConveyorBeltLive.capacity - 1,
        fullAgain.size == ConveyorBeltLive.capacity)
    },
    test("should destroy last item after 10 seconds") {
      for {
        belt <- ZIO.service[ConveyorBelt]
        supplier <- ZIO.service[Supplier]
        _ <- belt.offer(Mop)
        _ <- ZIO.foreachPar(1 until ConveyorBeltLive.capacity)(_ => belt.offer(MainUnit))
        full <- belt.peekAll()
        fiber <- supplier.put().fork
        _ <- TestClock.adjust(11.seconds)
        _ <- fiber.join
        remaining <- belt.peekAll()
      } yield assertTrue(
        full.size == ConveyorBeltLive.capacity,
        remaining.size == ConveyorBeltLive.capacity - 1,
      ) && assert(remaining)(forall(equalTo(MainUnit)))
    }
  ).provideLayer(ConveyorBeltLive.layer >+> SupplierLive.layer)
