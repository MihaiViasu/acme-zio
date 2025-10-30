package com.acme

import com.acme.domain.live.{ConveyorBeltLive, SupplierLive}
import com.acme.factory.AcmeFactoryService
import com.acme.factory.config.FactoryConfigLive
import com.acme.factory.live.AcmeFactoryServiceLive
import zio.{ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object AcmeApp extends ZIOAppDefault:

  def run: ZIO[ZIOAppArgs, Nothing, Unit] =
    (for {
      _ <- ZIO.logInfo("ACME app started")
      _ <- ZIO.serviceWithZIO[AcmeFactoryService](_.build)
    } yield ())
      .provideSome[ZIOAppArgs](
        FactoryConfigLive.layer >+>
          ConveyorBeltLive.layer >+>
          SupplierLive.layer >+>
          AcmeFactoryServiceLive.layer
      )
