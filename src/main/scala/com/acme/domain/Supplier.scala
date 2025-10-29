package com.acme.domain

import zio.UIO
import zio.stream.UStream

trait Supplier:
  def put(): UIO[Unit]
  def stream(): UStream[Unit]
