package com.acme

import com.acme.domain.live.{ConveyorBeltLive, WorkerConfig, WorkerLive}
import com.acme.domain.{ConveyorBelt, Worker}
import com.acme.model.Component.{Broom, MainUnit, Mop}
import com.acme.model.Robot._
import zio.ZIO
import zio.test._
import zio.test.Assertion._
import zio.durationInt

object WorkerSpec extends ZIOSpecDefault:

  private val dryMoreBrooms: WorkerConfig = WorkerConfig("Dry2000", Map(MainUnit -> 1, Broom -> 10), Dry)
  private val wet: WorkerConfig           = WorkerConfig("Wet2000", Map(MainUnit -> 1, Mop -> 2), Wet)

  def spec = suite("Worker concurrency tests")(
    test("should take a few components - parallel") {
      for {
        belt   <- ZIO.service[ConveyorBelt]
        worker <- ZIO.service[Worker]
        _      <- ZIO.foreachPar(1 to ConveyorBeltLive.capacity)(_ => belt.offer(Broom))
        fibers <- ZIO.foreachPar(1 to ConveyorBeltLive.capacity)(_ => worker.take().fork)
        _      <- ZIO.foreach(fibers)(_.join)
        _      <- TestClock.adjust(1.seconds)
        items  <- belt.peekAll()
      } yield assert(items)(isEmpty)
    }.provideLayer(ConveyorBeltLive.layer >+> WorkerLive.layer(dryMoreBrooms)),
    test("should not take more components - parallel") {
      for {
        belt   <- ZIO.service[ConveyorBelt]
        worker <- ZIO.service[Worker]
        _      <- ZIO.foreachPar(1 to ConveyorBeltLive.capacity)(_ => belt.offer(Mop))
        _      <- ZIO.foreach(1 to 10)(_ => worker.take())
        items  <- belt.peekAll()
      } yield assert(items.size)(equalTo(8))
    }.provideLayer(ConveyorBeltLive.layer >+> WorkerLive.layer(wet))
  )
