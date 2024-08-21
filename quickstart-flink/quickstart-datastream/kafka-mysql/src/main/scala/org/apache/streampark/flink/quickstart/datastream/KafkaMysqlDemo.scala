
package org.apache.streampark.flink.quickstart.datastream

import org.apache.commons.lang3.StringUtils
import org.apache.streampark.common.util.JsonUtils
import org.apache.streampark.flink.connector.kafka.source.KafkaSource
import org.apache.streampark.flink.core.scala.FlinkStreaming
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.streampark.flink.connector.jdbc.sink.JdbcSink
import org.json4s.DefaultFormats

import scala.language.implicitConversions
import scala.util.Random

object KafkaMysqlDemo extends FlinkStreaming {

  implicit val stringType: TypeInformation[String] = TypeInformation.of(classOf[String])
  implicit val logType: TypeInformation[Log] = TypeInformation.of(classOf[Log])
  implicit lazy val formats: DefaultFormats.type = org.json4s.DefaultFormats

  override def handle(): Unit = {
    val source = KafkaSource()
      .getDataStream[String]()
      .map(x => JsonUtils.read[Log](x.value))
      .filter(l => StringUtils.isNotBlank(l.message))
      .map { log =>
        val map: Map[String, String] = log.message.split("\\s").filter(s => s.contains(":")).map(s => {
          val strings: Array[String] = s.split(":", 2)
          val key: String = strings(0)
          val value: String = strings(1).replaceAll("'", "")
          (key, value)
        }).toMap
        val cid = map.getOrElse("cid", "0")
        log.copy(cid = cid)
      }.filter(log => StringUtils.isNotBlank(log.cid) && !log.cid.equals("0") && !log.cid.equals("null"))
      .filter(_ => Random.nextInt(100) > 90)


    JdbcSink().sink[Log](source)(log =>
      s"""
         |insert into demo_log(`hostname`,`namespace`,`log_type`,`cid`,`timestamp`)
         |value('${log.hostname}','${log.namespace}','${log.log_type}','${log.cid}','${log.`@timestamp`}')
         |""".stripMargin
    )
  }

}

case class Log(hostname: String, namespace: String, log_type: String, cid: String, message: String, `@timestamp`: String) extends Serializable

