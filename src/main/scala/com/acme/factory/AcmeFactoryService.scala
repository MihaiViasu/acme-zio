package com.acme.factory

import com.acme.domain.{ConveyorBelt, Supplier}
import com.acme.factory.config.FactoryConfig
import zio.URIO

trait AcmeFactoryService:
  def build: URIO[FactoryConfig & ConveyorBelt & Supplier, Unit]
