package com.acme.domain.live

import com.acme.domain.ConveyorBelt
import com.acme.model.Component
import zio.stm.{STM, TQueue}
import zio.{Chunk, UIO, ULayer, ZLayer}

class ConveyorBeltLive(queue: TQueue[Component]) extends ConveyorBelt:

  override def offer(component: Component): UIO[Unit] =
    queue.offer(component).commit.unit

  override def grabIfNeeded(needed: Component => Boolean): UIO[Option[Component]] =
    (for {
      c     <- queue.peek
      taken <- if (needed(c)) queue.take.as(Some(c)) else STM.succeed(None)
    } yield taken).commit

  override def destroyLastItem(): UIO[Component] =
    queue.take.commit

  override def peekAll(): UIO[Chunk[Component]] =
    queue.peekAll.commit

object ConveyorBeltLive:
  val capacity = 10
  val layer: ULayer[ConveyorBelt] = ZLayer {
    for {
      queue <- TQueue.bounded[Component](ConveyorBeltLive.capacity).commit
    } yield ConveyorBeltLive(queue)
  }
