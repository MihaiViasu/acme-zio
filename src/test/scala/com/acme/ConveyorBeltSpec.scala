package com.acme

import com.acme.domain.ConveyorBelt
import com.acme.domain.live.ConveyorBeltLive
import zio.*
import zio.test.*
import zio.test.Assertion.*
import com.acme.model.Component
import com.acme.model.Component.*
import zio.stm.TQueue

object ConveyorBeltSpec extends ZIOSpecDefault:

  def spec = suite("ConveyorBelt concurrency tests")(
    test("only one worker can take the last item concurrently") {
      for {
        q        <- TQueue.bounded[Component](ConveyorBeltLive.capacity).commit
        belt     <- ZIO.service[ConveyorBelt]
        _        <- ZIO.foreach(List(MainUnit, Mop, Broom))(belt.offer)
        items    <- belt.peekAll()
        _        <- ZIO.logInfo(items.toString)
        results  <- Ref.make(List.empty[String])
        predicate = (c: Component) => c == MainUnit
        workers = List("Worker 1", "Worker 2", "Worker 3").map { name =>
                    for {
                      taken <- belt.grabIfNeeded(predicate)
                      _ <- taken match
                             case Some(c) => results.update(s"$name-took-$c" :: _)
                             case None    => results.update(s"$name-none" :: _)
                    } yield ()
                  }
        _              <- ZIO.collectAllPar(workers)
        resultList     <- results.get
        _              <- ZIO.logInfo(resultList.toString)
        remainingItems <- belt.peekAll()
      } yield assert(resultList.count(_.contains("took")))(
        equalTo(1)
      ) && assert(remainingItems)(
        equalTo(List(Mop, Broom))
      )
    }.provideLayer(ConveyorBeltLive.layer) @@ TestAspect.repeat(Schedule.recurs(1000))
  )
