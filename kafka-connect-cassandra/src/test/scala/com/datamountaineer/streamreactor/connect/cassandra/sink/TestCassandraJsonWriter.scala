/*
 * *
 *   * Copyright 2016 Datamountaineer.
 *   *
 *   * Licensed under the Apache License, Version 2.0 (the "License");
 *   * you may not use this file except in compliance with the License.
 *   * You may obtain a copy of the License at
 *   *
 *   * http://www.apache.org/licenses/LICENSE-2.0
 *   *
 *   * Unless required by applicable law or agreed to in writing, software
 *   * distributed under the License is distributed on an "AS IS" BASIS,
 *   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   * See the License for the specific language governing permissions and
 *   * limitations under the License.
 *   *
 */

package com.datamountaineer.streamreactor.connect.cassandra.sink

import com.datamountaineer.streamreactor.connect.cassandra.TestConfig
import com.datamountaineer.streamreactor.connect.cassandra.config.CassandraConfigSink
import org.apache.kafka.connect.sink.SinkTaskContext
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

/**
  * Created by andrew@datamountaineer.com on 04/05/16. 
  * stream-reactor
  */
class TestCassandraJsonWriter extends WordSpec with Matchers with MockitoSugar with BeforeAndAfter with TestConfig {
  before {
    startEmbeddedCassandra()
  }

  "Cassandra JsonWriter should write records to Cassandra" in {
    val session = createTableAndKeySpace(secure = true, ssl = false)
    val context = mock[SinkTaskContext]
    val assignment = getAssignment
    when(context.assignment()).thenReturn(assignment)
    //get test records
    val testRecords1 = getTestRecords(TABLE1)
    val testRecords2 = getTestRecords(TOPIC2)
    val testRecords = testRecords1 ++ testRecords2
    //get config
    val props  = getCassandraConfigSinkProps
    val taskConfig = new CassandraConfigSink(props)

    val writer = CassandraWriter(taskConfig, context)
    writer.write(testRecords)
    Thread.sleep(2000)
    //check we can get back what we wrote
    val res1 = session.execute(s"SELECT * FROM $CASSANDRA_KEYSPACE.$TABLE1")
    res1.all().size() shouldBe testRecords1.size
    //check we can get back what we wrote
    val res2 = session.execute(s"SELECT * FROM $CASSANDRA_KEYSPACE.$TOPIC2")
    res2.all().size() shouldBe testRecords1.size
  }
}
