package com.acme.domain

import zio.UIO
import zio.stream.UStream

trait Worker:
  def take(): UIO[Unit]
  def stream(): UStream[Unit]
