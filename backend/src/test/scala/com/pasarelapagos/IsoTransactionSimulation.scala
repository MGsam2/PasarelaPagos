package com.pasarelapagos

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class IsoTransactionSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .contentTypeHeader("application/octet-stream")
    .shareConnections

  val tramaIsoBytes: Array[Byte] = Array[Byte](
    0x30.toByte, 0x32.toByte, 0x30.toByte, 0x30.toByte,
    0xF2.toByte, 0x30.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
    0x31.toByte, 0x36.toByte, 0x34.toByte, 0x31.toByte, 0x31.toByte, 0x31.toByte, 0x31.toByte, 0x31.toByte, 0x31.toByte, 0x31.toByte, 0x31.toByte, 0x31.toByte, 0x31.toByte, 0x31.toByte,
    0x30.toByte, 0x30.toByte, 0x30.toByte, 0x30.toByte, 0x30.toByte, 0x30.toByte,
    0x30.toByte, 0x30.toByte, 0x30.toByte, 0x30.toByte, 0x30.toByte, 0x30.toByte, 0x30.toByte, 0x31.toByte, 0x30.toByte, 0x30.toByte, 0x30.toByte, 0x30.toByte,
    0x30.toByte, 0x31.toByte, 0x31.toByte, 0x30.toByte, 0x32.toByte, 0x30.toByte, 0x33.toByte, 0x30.toByte, 0x34.toByte, 0x30.toByte,
    0x31.toByte, 0x32.toByte, 0x33.toByte, 0x34.toByte, 0x35.toByte, 0x36.toByte,
    0x33.toByte, 0x32.toByte, 0x30.toByte
  )

  val scn = scenario("Prueba de Carga ISO 8583 a 2000+ TPS")
    .exec(
      http("Post MTI 0200 Octet-Stream")
        .post("/api/iso8583/transacciones")
        .body(ByteArrayBody(tramaIsoBytes))
        .check(status.is(200))
    )

  setUp(
    scn.inject(
      nothingFor(1.second),
      rampUsersPerSec(100).to(2000).during(10.seconds),
      constantUsersPerSec(2000).during(20.seconds)
    )
  ).protocols(httpProtocol)
}