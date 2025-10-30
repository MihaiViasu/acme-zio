package com.acme.factory.live

import com.acme.domain.live.WorkerLive
import com.acme.domain.{ConveyorBelt, Supplier, Worker}
import com.acme.factory.AcmeFactoryService
import com.acme.factory.config.FactoryConfig
import zio.stream.ZStream
import zio.{URIO, URLayer, ZIO, ZLayer, durationInt}

class AcmeFactoryServiceLive(config: FactoryConfig, supplier: Supplier, dryWorker: Worker, wetWorker: Worker)
    extends AcmeFactoryService:

  def build: URIO[ConveyorBelt, Unit] =
    for {
      _       <- ZIO.logInfo("Acme Factory Service Started")
      pipeline = ZStream.mergeAllUnbounded()(supplier.stream(), dryWorker.stream(), wetWorker.stream())
      _       <- pipeline.interruptAfter(config.duration.seconds).runDrain
      _       <- ZIO.logInfo("Acme Factory Service Stopped")
    } yield ()

object AcmeFactoryServiceLive:
  lazy val layer: URLayer[FactoryConfig & ConveyorBelt & Supplier, AcmeFactoryService] =
    ZLayer {
      for {
        config    <- ZIO.service[FactoryConfig]
        supplier  <- ZIO.service[Supplier]
        dryWorker <- ZIO.service[Worker].provideLayer(WorkerLive.layer(WorkerLive.dry))
        wetWorker <- ZIO.service[Worker].provideLayer(WorkerLive.layer(WorkerLive.wet))
      } yield AcmeFactoryServiceLive(config, supplier, dryWorker, wetWorker)
    }
