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
 * Copyright 2018-2019 Johann Mortara <johann.mortara@univ-cotedazur.fr>
 * Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
 * Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>
 */

package neograph;

import neo4j_types.*;
import org.json.JSONObject;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class NeoGraph {

    private Driver driver;

    public NeoGraph(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public NeoGraph(Driver driver) {
        this.driver = driver;
    }

    public static String getClauseForNodesMatchingLabels(String nodeName, NodeType... types) {
        return Arrays.stream(types).map(nodeType -> nodeName + ":" + nodeType.toString()).collect(Collectors.joining(" OR "));
    }

    /**
     * Creates a node of corresponding name and types and returns it.
     *
     * @param name  Node name
     * @param types Node types
     */
    public Node createNode(String name, NodeType type, NodeType... types) {
        List <NodeType> nodeTypes = new ArrayList <>(Arrays.asList(types));
        nodeTypes.add(type);
        return submitRequest(String.format("CREATE (n:%s { name: $name}) RETURN (n)",
                nodeTypes.stream().map(NodeType::getString).collect(Collectors.joining(":"))),
                "name", name)
                .get(0).get(0).asNode();
    }

    public Optional <Node> getNode(String name) {
        List <Record> recordList = submitRequest("MATCH (n {name: $name}) RETURN (n)", "name", name);
        return recordList.size() == 0 ? Optional.empty() : Optional.of(recordList.get(0).get(0).asNode());
    }

    /**
     * Returns the node labeled CLASS and having the name in parameter
     * As we use a custom index :CLASS(name), this method lowers the time spent to execute the query.
     *
     * @param name node name
     * @return the node if it exists, Optional.empty otherwise
     */
    public Optional <Node> getClassNode(String name) {
        List <Record> recordList = submitRequest("MATCH (n:CLASS {name: $name}) RETURN (n)", "name", name);
        return recordList.size() == 0 ? Optional.empty() : Optional.of(recordList.get(0).get(0).asNode());
    }

    /**
     * Returns the node labeled INTERFACE and having the name in parameter
     * As we use a custom index :INTERFACE(name), this method lowers the time spent to execute the query.
     *
     * @param name node name
     * @return the node if it exists, Optional.empty otherwise
     */
    public Optional <Node> getInterfaceNode(String name) {
        List <Record> recordList = submitRequest("MATCH (n:INTERFACE {name: $name}) RETURN (n)", "name", name);
        return recordList.size() == 0 ? Optional.empty() : Optional.of(recordList.get(0).get(0).asNode());
    }

    /**
     * Returns the node corresponding to the superclass of the node whose name is in parameter
     *
     * @param name node name
     * @return the node if it exists, Optional.empty otherwise
     */
    public Optional <Node> getSuperclassNode(String name) {
        List <Record> recordList = submitRequest("MATCH (s:CLASS)-[:EXTENDS]->(n {name: $name}) RETURN (s)", "name", name);
        return recordList.size() == 0 ? Optional.empty() : Optional.of(recordList.get(0).get(0).asNode());
    }

    /**
     * Returns the list of nodes corresponding to the interfaces implemented by the node whose name is in parameter
     *
     * @param name node name
     * @return the node if it exists, Optional.empty otherwise
     */
    public List <Node> getImplementedInterfacesNodes(String name) {
        List <Record> recordList = submitRequest("MATCH (s:INTERFACE)-[:IMPLEMENTS]->(n {name: $name}) RETURN (s)", "name", name);
        return recordList.size() == 0 ? Collections.emptyList() : recordList.stream().map(record -> record.get(0).asNode()).collect(Collectors.toList());
    }

    public Optional <Node> getNodeWithNameInPackage(String name, String packageName) {
        List <Record> recordList = submitRequest("MATCH (n) WHERE (n:CLASS OR n:INTERFACE) AND n.name STARTS WITH $package AND n.name ENDS WITH $inheritedClassName RETURN (n)", "package", packageName + ".", "inheritedClassName", "." + name);
        return recordList.size() == 0 ? Optional.empty() : Optional.of(recordList.get(0).get(0).asNode());
    }

    /**
     * Returns the node if it exists, creates it and returns it otherwise.
     * As we use qualified names, each name is unique. Therefore, we can match only on node name.
     * If the node does not exist, it is created with the specified types as labels.
     *
     * @param name             Node name
     * @param type             Node type
     * @param createAttributes Node attributes added when creating the node
     * @param matchAttributes  Node attributes added when matching an existing node
     */
    public Node getOrCreateNode(String name, EntityType type, EntityAttribute[] createAttributes, EntityAttribute[] matchAttributes) {
        String onCreateAttributes = createAttributes.length == 0 ?
                "" :
                "ON CREATE SET n:" + Arrays.stream(createAttributes)
                        .map(NodeType::getString)
                        .collect(Collectors.joining(":"));
        String onMatchAttributes = matchAttributes.length == 0 ?
                "" :
                "ON MATCH SET n:" + Arrays.stream(matchAttributes)
                        .map(NodeType::getString)
                        .collect(Collectors.joining(":"));
        return submitRequest(String.format("MERGE (n:%s {name: $name}) %s %s RETURN (n)",
                type.toString(),
                onCreateAttributes,
                onMatchAttributes), "name", name)
                .get(0).get(0).asNode();
    }

    public Node getOrCreateNode(String name, EntityType type) {
        return getOrCreateNode(name, type, new EntityAttribute[]{}, new EntityAttribute[]{});
    }

    public Node getOrCreateNode(String name, EntityType type, EntityAttribute[] attributes) {
        return getOrCreateNode(name, type, attributes, attributes);
    }

    /**
     * Creates the relationship node1 -> node2 of the given type.
     *
     * @param node1 source node
     * @param node2 target node
     */
    public void linkTwoNodes(Node node1, Node node2, RelationType type) {
        submitRequest(String.format("MATCH(a)\n" +
                "WHERE ID(a)=$aId\n" +
                "WITH a\n" +
                "MATCH (b)\n" +
                "WITH a,b\n" +
                "WHERE ID(b)=$bId\n" +
                "CREATE (a)-[r:%s]->(b)", type), "aId", node1.id(), "bId", node2.id());
    }

    /**
     * Returns a map containing for each overloaded method the number of overloads it has in the class.
     * <p>
     * Example of a class containing the following methods:
     * - public void add(Point2D pt)
     * - public void add(Rectangle2D r)
     * - public void add(double newx, double newy)
     * - public PathIterator getPathIterator(AffineTransform at)
     * - public PathIterator getPathIterator(AffineTransform at, double flatness)
     * - public void setFrame(double x, double y, double w, double h)
     * <p>
     * The returned map will be : {"add": 3, "getPathIterator": 2}
     * As setFrame is not overloaded, it will not appear in the map.
     *
     * @param parent
     * @return
     */
    public Map<String, Long> getNbOverloads(String parent) {
        return submitRequest(String.format(
                "MATCH (:CLASS { name: '%s' })-->(a:METHOD) MATCH (:CLASS { name: '%s' })-->(b:METHOD)\n" +
                        "WHERE a.name = b.name AND ID(a) <> ID(b)\n" +
                        "return DISTINCT a.name, count(DISTINCT a)", parent, parent))
                .stream()
                .map(Record::asMap)
                .collect(Collectors.toMap(
                        recordMap -> (String) recordMap.get("a.name"),
                        recordMap -> (Long) recordMap.get("count(DISTINCT a)")));
    }

    public void setNodeAttribute(Node node, String attributeName, Object value) {
        submitRequest(String.format("MATCH (n) WHERE ID(n) = $idNode SET n.%s = $value", attributeName),
                "idNode", node.id(), "value", value);
    }

    public void detectVPsAndVariants() {
        setMethodVPs();
        setMethodVariants();
        setConstructorVPs();
        setConstructorVariants();
        setNbVariantsProperty();
        setVPLabels();
        setMethodLevelVPLabels();
        setVariantsLabels();
    }

    /**
     * This methods runs a query on a graph where Attribute nodes have been created.
     * Such a graph is currently created by the C++ analyser, but not by the Java runner
     * Execution on a java run will not have any effect
     */
    public void detectCPPStrategyPatterns() {
        submitRequest("Match (strategy:CLASS)-[:EXTENDS]-(child1:CLASS)\n" +
                "MATCH (strategy:CLASS)-[:EXTENDS]-(child2:CLASS)\n" +
                "MATCH (n:CLASS)-[:ATTRIBUTE]-(a:ATTRIBUTE)  \n" +
                "WHERE  a.type = strategy.name and not child1=child2\n" +
                "SET strategy:STRATEGY");
        submitRequest("MATCH (n:CLASS)-[:ATTRIBUTE]-(a:ATTRIBUTE)" +
                "WHERE a.type CONTAINS \"Strategy\" " +
                "MATCH(s:CLASS) WHERE s.name=a.type " +
                "SET s:STRATEGY");
    }

    /**
     * Sets the number of methods with different names defined more than once in the class.
     * <p>
     * Example of a class containing the following methods:
     * - public void add(Point2D pt)
     * - public void add(Rectangle2D r)
     * - public void add(double newx, double newy)
     * - public PathIterator getPathIterator(AffineTransform at)
     * - public PathIterator getPathIterator(AffineTransform at, double flatness)
     * <p>
     * Two methods are overloaded, therefore the value returned will be 2.
     * This is independent of the numbers of overloads for each method.
     * If no method is overloaded, the property is set to 0.
     */
    public void setMethodVPs() {
        submitRequest("MATCH (c:CLASS)-->(a:METHOD) MATCH (c:CLASS)-->(b:METHOD)\n" +
                "WHERE a.name = b.name AND ID(a) <> ID(b)\n" +
                "WITH count(DISTINCT a.name) AS cnt, c\n" +
                "SET c.methodVPs = cnt");
        submitRequest("MATCH (c:CLASS)\n" +
                "WHERE NOT EXISTS(c.methodVPs)\n" +
                "SET c.methodVPs = 0");
    }

    /**
     * Sets the number of method variants induced by method VPs.
     * <p>
     * Example of a class containing the following methods:
     * - public void add(Point2D pt)
     * - public void add(Rectangle2D r)
     * - public void add(double newx, double newy)
     * - public PathIterator getPathIterator(AffineTransform at)
     * - public PathIterator getPathIterator(AffineTransform at, double flatness)
     * <p>
     * Two methods are overloaded with respectively 3 and 2 overloads, therefore the value returned will be 5.
     * If no method is overloaded, the property is set to 0.
     */
    public void setMethodVariants() {
        submitRequest("MATCH (c:CLASS)-->(a:METHOD) MATCH (c:CLASS)-->(b:METHOD)\n" +
                "WHERE a.name = b.name AND ID(a) <> ID(b)\n" +
                "WITH count(DISTINCT a) AS cnt, c\n" +
                "SET c.methodVariants = cnt");
        submitRequest("MATCH (c:CLASS)\n" +
                "WHERE NOT EXISTS(c.methodVariants)\n" +
                "SET c.methodVariants = 0");
    }

    /**
     * Sets the number of overloaded constructors in the class.
     * If there is more than a constructor, this means that the constructor is overloaded, hence the value is 1.
     * If there is no overload (i.e. there is 0 or 1 constructor), the property is set to 0.
     */
    public void setConstructorVPs() {
        submitRequest("MATCH (c:CLASS)-->(a:CONSTRUCTOR)\n" +
                "WITH count(a.name) AS cnt, c\n" +
                "SET c.constructorVPs = CASE WHEN cnt > 1 THEN 1 ELSE 0 END");
        submitRequest("MATCH (c:CLASS)\n" +
                "WHERE NOT EXISTS(c.constructorVPs)\n" +
                "SET c.constructorVPs = 0");
    }

    /**
     * Sets the number of overloads of the constructor in the class.
     * If there is no overload (i.e. there is 0 or 1 constructor), the property is set to 0.
     */
    public void setConstructorVariants() {
        submitRequest("MATCH (c:CLASS)-->(a:CONSTRUCTOR)\n" +
                "WITH count(a.name) AS cnt, c\n" +
                "SET c.constructorVariants = CASE WHEN cnt > 1 THEN cnt ELSE 0 END");
        submitRequest("MATCH (c:CLASS)\n" +
                "WHERE NOT EXISTS(c.constructorVariants)\n" +
                "SET c.constructorVariants = 0");
    }

    /**
     * Creates for all class and interfaces nodes a property classVariants expressing the number of subclasses it contains.
     */
    public void setNbVariantsProperty() {
        submitRequest("MATCH (c)-[:EXTENDS|IMPLEMENTS]->(sc:CLASS) WITH count(sc) AS nbVar, c SET c.classVariants = nbVar");
        submitRequest("MATCH (c) WHERE ((c:CLASS OR c:INTERFACE) AND NOT EXISTS (c.classVariants)) SET c.classVariants = 0");
    }

    /**
     * Adds a VP label to the node if it is a VP.
     * A node is a VP if it:
     * - is an abstract class
     * - is an interface
     * - has class or method level variants (subclasses / implementations or methods / constructors overloads)
     * - has a design pattern.
     */
    public void setVPLabels() {
        submitRequest(String.format("MATCH (c) WHERE (NOT c:OUT_OF_SCOPE) AND (c:INTERFACE OR (c:CLASS AND c:ABSTRACT) OR (%s) OR (EXISTS(c.classVariants) AND c.classVariants > 0)) SET c:%s",
                getClauseForHavingDesignPattern("c"),
                EntityAttribute.VP));
    }

    public void setMethodLevelVPLabels() {
        submitRequest(String.format("MATCH (c) WHERE (NOT c:OUT_OF_SCOPE) AND (c.methodVPs > 0 OR c.constructorVPs > 0) SET c:%s",
                EntityAttribute.METHOD_LEVEL_VP));
    }

    public void setVariantsLabels() {
        submitRequest(String.format("MATCH (sc:VP)-[:EXTENDS|IMPLEMENTS]->(c) WHERE c:CLASS OR c:INTERFACE SET c:%s",
                EntityAttribute.VARIANT));
    }

    public void addLabelToNode(Node node, String label) {
        submitRequest(String.format("MATCH (n) WHERE ID(n) = $id SET n:%s RETURN (n)", label), "id", node.id());
    }

    public int getNbNodesHavingDesignPatterns() {
        return submitRequest(String.format("MATCH (n) WHERE %s RETURN COUNT(n)", getClauseForHavingDesignPattern("n")))
                .get(0).get(0).asInt();
    }

    private String getClauseForHavingDesignPattern(String n) {
        return getClauseForNodesMatchingLabels(n, DesignPatternType.values());
    }

    public void writeGraphFile(String filePath) {
        writeToFile(filePath, generateJsonGraph());
    }

    public void writeStatisticsFile(String filePath) {
        writeToFile(filePath, generateStatisticsJson());
    }

    public void writeToFile(String filePath, String content) {
        Path path = Paths.get(filePath);
        try {
            if (path.toFile().getParentFile().exists() || (path.toFile().getParentFile().mkdirs() && path.toFile().createNewFile())) {
                try (BufferedWriter bw = Files.newBufferedWriter(path)) {
                    bw.write(content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get number of subclasses of a class or implementations of an interface
     *
     * @param node Node corresponding to the class
     * @return Number of subclasses or implementations
     */
    public int getNbVariants(Node node) {
        return submitRequest("MATCH (c)-[:EXTENDS|IMPLEMENTS]->(c2:CLASS) " +
                "WHERE ID(c) = $id " +
                "RETURN count(c2)", "id", node.id())
                .get(0).get(0).asInt();
    }

    /**
     * Get total number of variants.
     * This corresponds to the number of variants at class and method level.
     *
     * @return Number of variants
     */
    public int getTotalNbVariants() {
        return getNbClassLevelVariants() + getNbMethodLevelVariants();
    }


    /**
     * Get number of variants at class level.
     * This corresponds to the number of concrete classes without a subclass and extending a class or implementing an interface defined in the project.
     *
     * @return Number of class level variants
     */
    public int getNbClassLevelVariants() {
        return submitRequest("MATCH (c:VARIANT) WHERE NOT c:VP RETURN (COUNT(DISTINCT c))")
                .get(0).get(0).asInt();
    }

    /**
     * Get number of variants at class level.
     * This corresponds to the number of concrete classes without a subclass and extending a class or implementing an interface.
     *
     * @return Number of class level variants
     */
    public int getNbMethodLevelVariants() {
        return getNbMethodVariants() + getNbConstructorVariants();
    }

    /**
     * Get number of variants caused by method overloading.
     * This corresponds to the total number of method variants.
     *
     * @return Number of overloaded methods
     */
    public int getNbMethodVariants() {
        return submitRequest("MATCH (c:CLASS) RETURN (SUM(c.methodVariants))")
                .get(0).get(0).asInt();
    }

    /**
     * Get number of variants caused by constructor overloading.
     * This corresponds to the total number of constructor overloads.
     *
     * @return Number of constructor overloads
     */
    public int getNbConstructorVariants() {
        return submitRequest("MATCH (c:CLASS) RETURN (SUM(c.constructorVariants))")
                .get(0).get(0).asInt();
    }

    /**
     * Get total number of overloaded constructors.
     *
     * @return Number of overloaded constructors
     */
    public int getNbConstructorVPs() {
        return submitRequest("MATCH (c:CLASS) RETURN (SUM(c.constructorVPs))")
                .get(0).get(0).asInt();
    }

    /**
     * Get total number of overloaded methods.
     *
     * @return Number of overloaded methods
     */
    public int getNbMethodVPs() {
        return submitRequest("MATCH (c:CLASS) RETURN (SUM(c.methodVPs))")
                .get(0).get(0).asInt();
    }


    /**
     * Get total number of VPs.
     * This corresponds to the number of VPs at class and method level.
     *
     * @return Number of VPs
     */
    public int getTotalNbVPs() {
        return getNbClassLevelVPs() + getNbMethodLevelVPs();
    }


    /**
     * Get total number of method level VPs.
     * These are :
     * - overloaded methods
     * - overloaded constructors
     *
     * @return Number of method level VPs
     */
    public int getNbMethodLevelVPs() {
        return getNbMethodVPs() + getNbConstructorVPs();
    }

    /**
     * Get total number of class level VPs.
     * These are :
     * - interfaces
     * - abstract classes
     * - extended classes
     *
     * @return Number of class level VPs
     */
    public int getNbClassLevelVPs() {
        return submitRequest("MATCH (c:VP) RETURN COUNT (DISTINCT c)")
                .get(0).get(0).asInt();
    }

    /**
     * Checks whether two nodes have a direct relationship.
     *
     * @param parentNode source node of the relationship
     * @param childNode  destination node of the relationship
     * @return true if a relationship exists, false otherwise
     */
    public boolean relatedTo(Node parentNode, Node childNode) {
        return submitRequest("MATCH(source) WHERE ID(source) = $idSource MATCH(dest) " +
                        "WHERE ID(dest) = $idDest RETURN EXISTS((source)-[]->(dest))",
                "idSource", parentNode.id(), "idDest", childNode.id())
                .get(0).get(0).asBoolean();
    }

    private String generateJsonGraph() {
        return String.format("{\"nodes\":[%s],\"links\":[%s]}", getNodesAsJson(), getLinksAsJson());
    }

    private String getNodesAsJson() {
        String request =
                "MATCH (c) WHERE c:VP OR c:VARIANT OR c:METHOD_LEVEL_VP " +
                        "RETURN collect(c {types:labels(c), .name, .methodVPs, .constructorVPs, .methodVariants, .constructorVariants, .classVariants})";
        return submitRequest(request)
                .get(0)
                .get(0)
                .asList(MapAccessor::asMap)
                .stream()
                .map(o -> new JSONObject(o).toString())
                .collect(Collectors.joining(","));
    }

    private String getLinksAsJson() {
        String request = "MATCH path = (c1:VP)-[r:EXTENDS|IMPLEMENTS]->(c2) WHERE NONE(n IN nodes(path) WHERE n:OUT_OF_SCOPE) RETURN collect({source:c1.name, target:c2.name, type:TYPE(r)})";
        return submitRequest(request)
                .get(0)
                .get(0)
                .asList(MapAccessor::asMap)
                .stream()
                .map(o -> new JSONObject(o).toString())
                .collect(Collectors.joining(","));
    }

    public String generateStatisticsJson() {
        return new JSONObject()
                .put("VPs", getTotalNbVPs())
                .put("methodsVPs", getNbMethodVPs())
                .put("constructorsVPs", getNbConstructorVPs())
                .put("methodLevelVPs", getNbMethodLevelVPs())
                .put("classLevelVPs", getNbClassLevelVPs())
                .put("variants", getTotalNbVariants())
                .put("methodsVariants", getNbMethodVariants())
                .put("constructorsVariants", getNbConstructorVariants())
                .put("methodLevelVariants", getNbMethodLevelVariants())
                .put("classLevelVariants", getNbClassLevelVariants()).toString();
    }

    public int getNbNodes() {
        return submitRequest("MATCH(n) RETURN count(*)").get(0).get(0).asInt();
    }

    public int getNbRelationships() {
        return submitRequest("MATCH (n)-[r]->() RETURN COUNT(r)").get(0).get(0).asInt();
    }

    public int getNbInheritanceRelationships() {
        return submitRequest("MATCH (n)-[r:EXTENDS|IMPLEMENTS]->() RETURN COUNT(r)").get(0).get(0).asInt();
    }

    public void createClassesIndex() {
        submitRequest("CREATE INDEX ON :CLASS(name)");
    }

    public void createInterfacesIndex() {
        submitRequest("CREATE INDEX ON :INTERFACE(name)");
    }

    /**
     * Deletes all nodes and relationships in the graph.
     */
    public void deleteGraph() {
        submitRequest("MATCH (n) DETACH DELETE (n)");
    }

    private List <Record> submitRequest(String request, Object... parameters) {
        int count = 0;
        int maxTries = 10;
        while (true) {
            try (Session session = driver.session()) {
                try (Transaction tx = session.beginTransaction()) {
                    List <Record> result = tx.run(request, parameters(parameters)).list();
                    tx.commit();
                    return result;
                }
            } catch (ServiceUnavailableException e) { // The database is not ready, retry to connect
                System.out.println("Waiting for Neo4j database to be ready...");
                if (++ count == maxTries) {
                    throw e;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void closeDriver() {
        driver.close();
    }

    /**
     * Use a neo4j query to detect if a class is abstract
     *
     * @param node Class Node
     */
    public void detectCPPClassAbstract(Node node) {
        submitRequest("MATCH (n:CLASS)-[:METHOD]-(m:METHOD:ABSTRACT) where ID(n)=$nodeId set n:ABSTRACT", "nodeId", node.id());
    }

    public void detectCPPDecoratorPatterns() {
        submitRequest("MATCH (parent:CLASS)-[:EXTENDS]->(child:CLASS)\n" +
                "WITH COUNT(child) as children,parent\n" +
                "MATCH (parent:CLASS)-[:EXTENDS]->(abstractDecorator:CLASS) \n" +
                "MATCH (abstractDecorator:CLASS)-[:EXTENDS]->(concreteDecorator:CLASS)\n" +
                "WITH COUNT( DISTINCT concreteDecorator) as concreteCount,\t\n" +
                "\t\tabstractDecorator,parent\n" +
                "WHERE concreteCount >=1\n" +
                "MATCH (abstractDecorator)-[:ATTRIBUTE]->(attribute:ATTRIBUTE)\n" +
                "WHERE attribute.type=parent.name\n" +
                "SET abstractDecorator:DECORATOR");
        submitRequest("MATCH (n:CLASS)-[:ATTRIBUTE]-(a:ATTRIBUTE)" +
                "WHERE n.name CONTAINS \"Decorator\" " +
                "SET n:DECORATOR");
    }
}
