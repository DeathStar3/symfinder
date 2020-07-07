import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.internal.InProcessNeo4jBuilder;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.driver.Values.parameters;

public class GetChildrenProcedureTest {
    private static final Config driverConfig = Config.defaultConfig();
    private static Neo4j embeddedDatabaseServer;
    protected static GraphDatabaseService graphDatabaseService;

    @BeforeAll
    static void setUp() {
        embeddedDatabaseServer = new InProcessNeo4jBuilder().withProcedure(GetChildrenProcedure.class).build();
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

    @Test
    public void twoMethods() {

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig) ;
             Session session = driver.session()) {
            long nodeId = session.run("CREATE (n:CLASS {name:'Class1'}) RETURN ID(n)")
                    .single().get(0).asLong();

            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:METHOD {name:'method1'})", parameters("idNode", nodeId));
            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:METHOD {name:'method2'})", parameters("idNode", nodeId));

            Result result = session.run("CALL symfinder.count($idNode, $label) YIELD result as res", parameters("idNode", nodeId, "label", "METHOD"));

            List <Map <String, Object>> x = result.single().get("res").asList(MapAccessor::asMap);
            assertEquals(2, x.size());
            final Map <String, Object> method1 = x.stream().filter(stringObjectMap -> stringObjectMap.containsValue("method1")).findFirst().get();
            assertEquals(1L, method1.get("number"));
            final Map <String, Object> method2 = x.stream().filter(stringObjectMap -> stringObjectMap.containsValue("method2")).findFirst().get();
            assertEquals(1L, method2.get("number"));

        }
    }

    @Test
    public void oneMethod() {

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig) ;
             Session session = driver.session()) {
            long nodeId = session.run("CREATE (n:CLASS {name:'Class1'}) RETURN ID(n)")
                    .single().get(0).asLong();

            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:METHOD {name:'method1'})", parameters("idNode", nodeId));
            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:METHOD {name:'method1'})", parameters("idNode", nodeId));

            Result result = session.run("CALL symfinder.count($idNode, $label) YIELD result as res", parameters("idNode", nodeId, "label", "METHOD"));

            List <Map <String, Object>> x = result.single().get("res").asList(MapAccessor::asMap);
            assertEquals(1, x.size());
            assertEquals(2L, x.get(0).get("number"));
        }
    }

    @Test
    public void oneMethodThreeVariants() {

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig) ;
             Session session = driver.session()) {
            long nodeId = session.run("CREATE (n:CLASS {name:'Class1'}) RETURN ID(n)")
                    .single().get(0).asLong();

            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:METHOD {name:'method1'})", parameters("idNode", nodeId));
            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:METHOD {name:'method1'})", parameters("idNode", nodeId));
            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:METHOD {name:'method1'})", parameters("idNode", nodeId));

            Result result = session.run("CALL symfinder.count($idNode, $label) YIELD result as res", parameters("idNode", nodeId, "label", "METHOD"));

            List <Object> x = result.single().get("res").asList();
            assertEquals(1, x.size());
            assertEquals(3L, ((Map<String, Object>) x.get(0)).get("number"));
        }
    }

    @Test
    public void realUseCase() {

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig) ;
             Session session = driver.session()) {

            long node1Id = session.run("CREATE (n:CLASS {name:'Class1'}) RETURN ID(n)").single().get(0).asLong();
            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:METHOD {name:'method1'})", parameters("idNode", node1Id));
            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:METHOD {name:'method1'})", parameters("idNode", node1Id));

            long node2Id = session.run("CREATE (n:CLASS {name:'Class2'}) RETURN ID(n)").single().get(0).asLong();
            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:METHOD {name:'method1'})", parameters("idNode", node2Id));
            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:CONSTRUCTOR {name:'Class2'})", parameters("idNode", node2Id));
            session.run("MATCH (n) WHERE ID(n) = $idNode CREATE (n)-[r:METHOD]->(m:CONSTRUCTOR {name:'Class2'})", parameters("idNode", node2Id));

            Result result = session.run(
                    "MATCH (c) WHERE c:CLASS " +
                    "CALL symfinder.count(ID(c), \"METHOD\") YIELD result as methods " +
                    "CALL symfinder.count(ID(c), \"CONSTRUCTOR\") YIELD result as constructors " +
                            "RETURN collect(c {.name, methods, constructors})", parameters());

            List <Map <String, Object>> finalResult = result.list().get(0).get(0).asList(MapAccessor::asMap);
            assertEquals(2, finalResult.size());  // Two classes have been visited

            // Class1

            final Map <String, Object> class1 = finalResult.stream().filter(stringObjectMap -> stringObjectMap.containsValue("Class1")).findFirst().get();
            assertTrue(((List<Map<String, Object>>) class1.get("constructors")).isEmpty());
            final List <Map <String, Object>> methods = (List <Map <String, Object>>) class1.get("methods");
            assertEquals(1, methods.size());
            assertEquals("method1", methods.get(0).get("name"));
            assertEquals(2L, methods.get(0).get("number"));

            // Class2

            final Map <String, Object> class2 = finalResult.stream().filter(stringObjectMap -> stringObjectMap.containsValue("Class2")).findFirst().get();
            final List <Map <String, Object>> constructors2 = (List <Map <String, Object>>) class2.get("constructors");
            assertEquals(1, constructors2.size());
            assertEquals("Class2", constructors2.get(0).get("name"));
            assertEquals(2L, constructors2.get(0).get("number"));
            final List <Map <String, Object>> methods2 = (List <Map <String, Object>>) class2.get("methods");
            assertEquals(1, methods2.size());
            assertEquals("method1", methods2.get(0).get("name"));
            assertEquals(1L, methods2.get(0).get("number"));
        }
    }
}