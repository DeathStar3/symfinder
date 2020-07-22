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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with symfinder. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2018-2020 Johann Mortara <johann.mortara@univ-cotedazur.fr>
 * Copyright 2018-2020 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
 * Copyright 2018-2020 Philippe Collet <philippe.collet@univ-cotedazur.fr>
 */

import apoc.path.PathExplorer;
import neograph.NeoGraph;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.internal.InProcessNeo4jBuilder;

import java.util.function.Consumer;

public class Neo4jTest {

    private static Neo4j embeddedDatabaseServer;
    protected static GraphDatabaseService graphDatabaseService;

    @BeforeAll
    static void setUp() {
        embeddedDatabaseServer = new InProcessNeo4jBuilder().withProcedure(PathExplorer.class).build();
        graphDatabaseService = embeddedDatabaseServer.defaultDatabaseService();
    }
    	
    @AfterEach
    public void tearDown() {
        graphDatabaseService.executeTransactionally("MATCH (n) DETACH DELETE (n)");
    }

    @AfterAll
    static void tearDownAll() {
        embeddedDatabaseServer.close();
    }

    protected void runTest(Consumer<NeoGraph> consumer){
        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), Config.defaultConfig())) {
            NeoGraph graph = new NeoGraph(driver);
            consumer.accept(graph);
        }
    }

}
