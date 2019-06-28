import java.util.Random

import com.github.mnogu.gatling.kafka.Predef._
import io.gatling.core.Predef._
import org.apache.kafka.clients.producer.ProducerConfig

import scala.concurrent.duration._

class BasicSimulation extends Simulation {
  val recordSize: Integer = 100
  val recordSize2: Integer = 200
  val random = new Random(0)
  val kafkaConf = kafka
    .topic("test")
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG -> "1",
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.ByteArraySerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.ByteArraySerializer",
        ProducerConfig.BATCH_SIZE_CONFIG -> "1048576",
        ProducerConfig.COMPRESSION_TYPE_CONFIG -> "snappy"
      )
    )

  def generateMessage(recordSize: Integer): Array[Byte] = {
    var payload = new Array[Byte](recordSize)
    var i = 0
    while ( { i < payload.length }) {
      payload(i) = (random.nextInt(26) + 65).toByte
      i += 1;
      i
    }
    payload
  }

  val scn = scenario("Kafka Test")
    .exec(kafka("request").send(generateMessage(recordSize)))

  val scn2 = scenario("Kafka Test2")
    .exec(kafka("request2").send(generateMessage(recordSize2)))

  setUp(
    scn
      .inject(
        rampUsersPerSec(10) to 100000 during(20 seconds),
        constantUsersPerSec(100000) during (40 seconds) randomized)
      .protocols(kafkaConf),
    scn2
      .inject(
//        nothingFor(60 seconds),
        rampUsersPerSec(10) to 100000 during(20 seconds),
        constantUsersPerSec(100000) during (40 seconds) randomized)
      .protocols(kafkaConf)

  )

}
