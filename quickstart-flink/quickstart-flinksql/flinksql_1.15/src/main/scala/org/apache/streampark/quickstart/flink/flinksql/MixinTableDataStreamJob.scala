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
package org.apache.streampark.quickstart.flink.flinksql

import org.apache.streampark.flink.core.scala.FlinkStreamTable
import org.apache.flink.api.scala._
import org.apache.flink.table.api.Table

object MixinTableDataStreamJob extends FlinkStreamTable {

  override def handle(): Unit = {

    val mySource = context.$fromCollection(
      List(
        "kafka,apapche kafka",
        "spark,spark",
        "flink,apapche flink",
        "zookeeper,apapche zookeeper",
        "hadoop,apapche hadoop"
      )
    ).map(x => {
      val array = x.split(",")
      Entity(array.head, array.last)
    })

    context.createTemporaryView("mySource", mySource)

    //kafka to table
    val table1: Table = context.from("mySource")
    table1.>>[Entity].print("stream print==>")

  }

}

case class Entity(id: String, name: String)
