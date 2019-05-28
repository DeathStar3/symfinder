/*
 * This file is part of symfinder.
 *
 * symfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * symfinder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with symfinder.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2018-2019 Johann Mortara <johann.mortara@etu.univ-cotedazur.fr>
 * Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
 * Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.junit.Neo4jRule;

import java.util.function.Consumer;

public class Neo4JTest {

    @Rule
    public Neo4jRule neo4jRule = new Neo4jRule();
    protected GraphDatabaseService graphDatabaseService;

    @Before
    public void setUp() {
        graphDatabaseService = neo4jRule.getGraphDatabaseService();
    }

    @After
    public void tearDown() {
        graphDatabaseService.shutdown();
    }

    protected void runTest(Consumer<NeoGraph> consumer){
        try (Driver driver = GraphDatabase.driver(neo4jRule.boltURI(), Config.build().withoutEncryption().toConfig())) {
            NeoGraph graph = new NeoGraph(driver);
            consumer.accept(graph);
        }
    }

}
