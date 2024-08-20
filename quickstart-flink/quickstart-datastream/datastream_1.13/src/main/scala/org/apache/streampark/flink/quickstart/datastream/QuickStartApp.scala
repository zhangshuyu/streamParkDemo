/*
  * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.streampark.flink.quickstart.datastream

import org.apache.commons.lang3.StringUtils
import org.apache.streampark.common.util.JsonUtils
import org.apache.streampark.flink.connector.elasticsearch7.sink.ES7Sink
import org.apache.streampark.flink.connector.kafka.source.KafkaSource
import org.apache.streampark.flink.core.scala.FlinkStreaming
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.streampark.flink.connector.elasticsearch7.util.ElasticsearchUtils
import org.elasticsearch.action.index.IndexRequest
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization

import scala.language.implicitConversions

object QuickStartApp extends FlinkStreaming {

  implicit val stringType: TypeInformation[String] = TypeInformation.of(classOf[String])
  implicit val logType: TypeInformation[Log] = TypeInformation.of(classOf[Log])
  implicit lazy val formats: DefaultFormats.type = org.json4s.DefaultFormats

  override def handle(): Unit = {
    val source = KafkaSource()
      .getDataStream[String]()
      .map(x => JsonUtils.read[Log](x.value))
//      .filter(l => StringUtils.isNotBlank(l.message))
//      .map{log =>
//        val map = JsonUtils.read[Map[String, Any]](JsonUtils.write(log))
//        val strings = log.message.split("\\s")
//        map
//
//      }


    implicit def indexReq(x: Log): IndexRequest = ElasticsearchUtils.indexRequest(index = "stream_park_test", id = "_doc", source = Serialization.write(x))

    ES7Sink().sink(source)
  }

}

case class Log(hostname: String, namespace: String, log_type: String, message: String, `@timestamp`: String) extends Serializable

