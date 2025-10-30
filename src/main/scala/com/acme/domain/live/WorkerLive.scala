package com.acme.domain.live

import com.acme.domain.{ConveyorBelt, Worker}
import com.acme.model.{Component, Robot}
import com.acme.model.Component.*
import com.acme.model.Robot.*
import zio.{Ref, UIO, URLayer, ZIO, ZLayer, durationInt}
import zio.stream.{UStream, ZStream}

case class WorkerConfig(name: String, neededComponents: Map[Component, Int], robot: Robot)

class WorkerLive(config: WorkerConfig, belt: ConveyorBelt, inventory: Ref[Map[Component, Int]]) extends Worker:

  // good to know
  // take into consideration the same worker take of components is not optimized to be parallelized
  // there is a time between a component take from belt and the inventory update when the same worker
  // can take more object from the belt (if he is sent from another fiber/thread)
  // we can make also this operation transactional using TRef for inventory storing
  // but for now we are good enough

  override def take(): UIO[Unit] =
    for {
      inv       <- inventory.get
      component <- belt.grabIfNeeded(c => inv.getOrElse(c, 0) < config.neededComponents.getOrElse(c, 0))
      _         <- updateInventory(component)
    } yield ()

  private def updateInventory(component: Option[Component]): UIO[Unit] =
    component match
      case Some(comp) =>
        for {
          _ <- inventory.update(inv => inv.updated(comp, inv.getOrElse(comp, 0) + 1))
          _ <- ZIO.logInfo(s"${config.name} took $component")
          _ <- assembleIfReady()
        } yield ()
      case None => ZIO.unit

  private def assembleIfReady(): UIO[Unit] =
    for {
      inv <- inventory.get
      ready = config.neededComponents.forall { case (comp, number) =>
                inv.getOrElse(comp, 0) >= number
              }
      _ <- if (ready) assemble() else ZIO.unit
    } yield ()

  private def assemble() =
    for {
      _ <- ZIO.logInfo(s"${config.name} start assembling ${config.robot}")
      _ <- ZIO.sleep(3.seconds)
      _ <- ZIO.logInfo(s"${config.name} finished ${config.robot} robot")
      _ <- inventory.set(Map.empty)
    } yield ()

  def stream(): UStream[Unit] =
    ZStream.repeatZIO(take())

object WorkerLive:

  val dry: WorkerConfig = WorkerConfig("Dry2000", Map(MainUnit -> 1, Broom -> 2), Dry)
  val wet: WorkerConfig = WorkerConfig("Wet2000", Map(MainUnit -> 1, Mop -> 2), Wet)

  def layer(config: WorkerConfig): URLayer[ConveyorBelt, WorkerLive] = ZLayer {
    for {
      conveyor  <- ZIO.service[ConveyorBelt]
      inventory <- Ref.make(Map.empty[Component, Int])
    } yield WorkerLive(config, conveyor, inventory)
  }
