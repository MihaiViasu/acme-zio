package com.acme.factory.config

import zio.{ZIO, ZIOAppArgs, ZLayer}

case class FactoryConfig(duration: Int)

object FactoryConfigLive:
  val layer: ZLayer[ZIOAppArgs, Nothing, FactoryConfig] =
    ZLayer.fromZIO {
      for {
        args <- ZIO.serviceWith[ZIOAppArgs](_.getArgs)
        duration <- ZIO
                      .fromOption(args.headOption.map(_.toInt))
                      .orElseSucceed(30)
        _ <- ZIO.logInfo(s"Factory will run for: $duration seconds")
      } yield FactoryConfig(duration)
    }
