package com.acme.domain

import com.acme.model.Component
import zio.{Chunk, UIO}

trait ConveyorBelt:
  def offer(component: Component): UIO[Unit]
  def grabIfNeeded(needed: Component => Boolean): UIO[Option[Component]]
  def destroyLastItem(): UIO[Component]
  def peekAll(): UIO[Chunk[Component]]

