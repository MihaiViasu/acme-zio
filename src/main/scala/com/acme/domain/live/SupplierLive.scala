package com.acme.domain.live

import com.acme.domain.{ConveyorBelt, Supplier}
import com.acme.model.Component
import zio.{Random, UIO, URLayer, ZIO, ZLayer, durationInt}
import zio.stream.{UStream, ZStream}

class SupplierLive(belt: ConveyorBelt, nextComponent: UIO[Component]) extends Supplier:

  import SupplierLive._
  // issue: when the belt is full and we reach timeout, we destroy the last component,
  // and also we lose the component that we tried to put
  // fix: we can retry to put it back in the next iteration

  override def put(): UIO[Unit] =
    def waitOrRemoveItem(offered: Option[Unit]): UIO[Unit] =
      offered match
        case Some(value) => ZIO.sleep(1.second)
        case None =>
          for {
            item <- belt.destroyLastItem()
            _    <- ZIO.logInfo(s"Belt blocked 10 seconds, last item $item destroyed")
          } yield ()

    for {
      component <- nextComponent
      _         <- ZIO.logInfo(s"Supplier produces $component")
      offered   <- belt.offer(component).timeout(TIMEOUT_LIMIT.seconds)
      _         <- waitOrRemoveItem(offered)
    } yield ()

  def stream(): UStream[Unit] =
    ZStream.repeatZIO(put())

object SupplierLive:
  private val components    = Component.values
  private val TIMEOUT_LIMIT = 10

  val layer: URLayer[ConveyorBelt, SupplierLive] = ZLayer {
    val nextComponent = Random.nextIntBounded(components.length).map(components)
    for {
      belt <- ZIO.service[ConveyorBelt]
    } yield SupplierLive(belt, nextComponent)
  }
