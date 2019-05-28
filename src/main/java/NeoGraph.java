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

import neo4j_types.*;
import org.json.JSONObject;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.neo4j.driver.v1.types.MapAccessor;
import org.neo4j.driver.v1.types.Node;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.neo4j.driver.v1.Values.parameters;

public class NeoGraph {

    private Driver driver;

    public NeoGraph(String uri, String user, String password) {
        driver = getDriver(uri, user, password);
    }

    private Driver getDriver(String uri, String user, String password) {
        int count = 0;
        int maxTries = 10;
        while (true) {
            try {
                return GraphDatabase.driver(uri, AuthTokens.basic(user, password));
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

    public NeoGraph(Driver driver) {
        this.driver = driver;
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
                .list().get(0).get(0).asNode();
    }

    public Optional <Node> getNode(String name) {
        List <Record> recordList = submitRequest("MATCH (n {name: $name}) RETURN (n)", "name", name).list();
        return recordList.size() == 0 ? Optional.empty() : Optional.of(recordList.get(0).get(0).asNode());
    }

    /**
     * Returns the node labeled CLASS and having the name in parameter
     * As we use a custom index :CLASS(name), this method lowers the time spent to execute the query.
     * @param name node name
     * @return the node if it exists, Optional.empty otherwise
     */
    public Optional <Node> getClassNode(String name) {
        List <Record> recordList = submitRequest("MATCH (n:CLASS {name: $name}) RETURN (n)", "name", name).list();
        return recordList.size() == 0 ? Optional.empty() : Optional.of(recordList.get(0).get(0).asNode());
    }

    /**
     * Returns the node labeled INTERFACE and having the name in parameter
     * As we use a custom index :INTERFACE(name), this method lowers the time spent to execute the query.
     * @param name node name
     * @return the node if it exists, Optional.empty otherwise
     */
    public Optional <Node> getInterfaceNode(String name) {
        List <Record> recordList = submitRequest("MATCH (n:INTERFACE {name: $name}) RETURN (n)", "name", name).list();
        return recordList.size() == 0 ? Optional.empty() : Optional.of(recordList.get(0).get(0).asNode());
    }

    public Optional <Node> getNodeWithNameInPackage(String name, String packageName) {
        List <Record> recordList = submitRequest("MATCH (n) WHERE (n:CLASS OR n:INTERFACE) AND n.name STARTS WITH $package AND n.name ENDS WITH $inheritedClassName RETURN (n)", "package", packageName+".", "inheritedClassName", "."+name).list();
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
                .list().get(0).get(0).asNode();
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
     *
     * @return
     */
    public Map <String, Long> getNbOverloads(String parent) {
        return submitRequest(String.format(
                "MATCH (:CLASS { name: '%s' })-->(a:METHOD) MATCH (:CLASS { name: '%s' })-->(b:METHOD)\n" +
                        "WHERE a.name = b.name AND ID(a) <> ID(b)\n" +
                        "return DISTINCT a.name, count(DISTINCT a)", parent, parent))
                .list()
                .stream()
                .map(Record::asMap)
                .collect(Collectors.toMap(
                        recordMap -> (String) recordMap.get("a.name"),
                        recordMap -> (Long) recordMap.get("count(DISTINCT a)")));

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
    public void setMethodsOverloads() {
        submitRequest("MATCH (c:CLASS)-->(a:METHOD) MATCH (c:CLASS)-->(b:METHOD)\n" +
                "WHERE a.name = b.name AND ID(a) <> ID(b)\n" +
                "WITH count(DISTINCT a.name) AS cnt, c\n" +
                "SET c.methods = cnt");
        submitRequest("MATCH (c:CLASS)\n" +
                "WHERE NOT EXISTS(c.methods)\n" +
                "SET c.methods = 0");
    }

    /**
     * Sets the number of overloads of the constructor in the class.
     * If there is no overload (i.e. there is 0 or 1 constructor), the property is set to 0.
     */
    public void setConstructorsOverloads() {
        submitRequest("MATCH (c:CLASS)-->(a:CONSTRUCTOR)\n" +
                "WITH count(a.name) AS cnt, c\n" +
                "SET c.constructors = cnt -1");
        submitRequest("MATCH (c:CLASS)\n" +
                "WHERE NOT EXISTS(c.constructors)\n" +
                "SET c.constructors = 0");
    }

    /**
     * Creates for all class and interfaces nodes a property nbVariants expressing the number of subclasses it contains.
     */
    public void setNbVariantsProperty() {
        submitRequest("MATCH (c)-[:EXTENDS|:IMPLEMENTS]->(sc:CLASS) WITH count(sc) AS nbVar, c SET c.nbVariants = nbVar");
        submitRequest("MATCH (c) WHERE ((c:CLASS OR c:INTERFACE) AND NOT EXISTS (c.nbVariants)) SET c.nbVariants = 0");
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
        submitRequest(String.format("MATCH (c) WHERE ((%s) OR c:ABSTRACT OR c:INTERFACE OR (EXISTS(c.nbVariants) AND c.nbVariants > 0) OR c.methods > 0 OR c.constructors > 0) SET c:%s",
                getClauseForNodesMatchingLabels("c",DesignPatternType.values()),
                EntityAttribute.VP));
    }

    public void addLabelToNode(Node node, String label) {
        submitRequest(String.format("MATCH (n) WHERE ID(n) = $id SET n:%s RETURN (n)", label), "id", node.id());
    }

    public int getNbNodesHavingDesignPatterns() {
        return submitRequest(String.format("MATCH (n) WHERE %s RETURN COUNT(n)", getClauseForNodesMatchingLabels("n", DesignPatternType.values())))
                .list().get(0).get(0).asInt();
    }

    public static String getClauseForNodesMatchingLabels(String nodeName, NodeType... types) {
        return Arrays.stream(types).map(nodeType -> nodeName + ":" + nodeType.toString()).collect(Collectors.joining(" OR "));
    }

    public void writeGraphFile(String filePath) {
        writeToFile(filePath, generateJsonGraph());
    }

    public void writeVPGraphFile(String filePath) {
        writeToFile(filePath, generateVPJsonGraph());
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
     *
     * @return Number of subclasses or implementations
     */
    public int getNbVariants(Node node) {
        return submitRequest("MATCH (c)-[:EXTENDS|:IMPLEMENTS]->(c2:CLASS) " +
                "WHERE ID(c) = $id " +
                "RETURN count(c2)", "id", node.id())
                .list().get(0).get(0).asInt();
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
     * This corresponds to the number of concrete classes without a subclass and extending a class or implementing an interface.
     *
     * @return Number of class level variants
     */
    public int getNbClassLevelVariants() {
        return submitRequest("MATCH (c:CLASS) WHERE (NOT c:ABSTRACT) AND ()-[:EXTENDS|:IMPLEMENTS]->(c) AND (NOT (c)-[:EXTENDS]->()) RETURN (COUNT(c))")
                .list().get(0).get(0).asInt();
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
     * This corresponds to the total number of overloaded methods.
     *
     * @return Number of overloaded methods
     */
    public int getNbMethodVariants() {
        return submitRequest("MATCH (c:CLASS)-->(a:METHOD) MATCH (c:CLASS)-->(b:METHOD)\n" +
                "WHERE a.name = b.name AND ID(a) <> ID(b)\n" +
                "return count(DISTINCT a)")
                .list().get(0).get(0).asInt();
    }

    /**
     * Get number of variants caused by constructor overloading.
     * This corresponds to the total number of constructor overloads.
     *
     * @return Number of constructor overloads
     */
    public int getNbConstructorVariants() {
        return submitRequest("MATCH (c:CLASS)-->(a:CONSTRUCTOR) MATCH (c:CLASS)-->(b:CONSTRUCTOR)\n" +
                "WHERE a.name = b.name AND ID(a) <> ID(b)\n" +
                "return count(DISTINCT a)")
                .list().get(0).get(0).asInt();
    }

    /**
     * Get total number of overloaded constructors
     *
     * @return Number of overloaded constructors
     */
    public int getTotalNbOverloadedConstructors() {
        return submitRequest("MATCH (c:CLASS) RETURN (SUM(c.constructors))")
                .list().get(0).get(0).asInt();
    }

    /**
     * Get total number of overloaded methods
     *
     * @return Number of overloaded methods
     */
    public int getTotalNbOverloadedMethods() {
        return submitRequest("MATCH (c:CLASS) RETURN (SUM(c.methods))")
                .list().get(0).get(0).asInt();
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
     * - number of methods overloads
     * - number of overloaded constructors
     *
     * @return Number of method level VPs
     */
    public int getNbMethodLevelVPs() {
        return getTotalNbOverloadedMethods() + getTotalNbOverloadedConstructors();
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
        int nbInterfaces = submitRequest("MATCH (n:INTERFACE) RETURN COUNT (n)").list().get(0).get(0).asInt();
        int nbAbstractClasses = submitRequest("MATCH (n:CLASS:ABSTRACT) RETURN COUNT (n)").list().get(0).get(0).asInt();
        int nbExtendedClasses = submitRequest("MATCH (n:CLASS)-[r:EXTENDS]->() WHERE NOT n:ABSTRACT RETURN COUNT (n)").list().get(0).get(0).asInt(); // we exclude abstracts as they are already counted
        return nbInterfaces + nbAbstractClasses + nbExtendedClasses;
    }

    /**
     * Checks whether two nodes have a direct relationship.
     *
     * @param parentNode source node of the relationship
     * @param childNode  destination node of the relationship
     *
     * @return true if a relationship exists, false otherwise
     */
    public boolean relatedTo(Node parentNode, Node childNode) {
        return submitRequest("MATCH(source) WHERE ID(source) = $idSource MATCH(dest) WHERE ID(dest) = $idDest RETURN EXISTS((source)-[]->(dest))", "idSource", parentNode.id(), "idDest", childNode.id())
                .list().get(0).get(0).asBoolean();
    }

    private String generateJsonGraph() {
        return String.format("{\"nodes\":[%s],\"links\":[%s]}", getNodesAsJson(false), getLinksAsJson(false));
    }

    private String generateVPJsonGraph() {
        return String.format("{\"nodes\":[%s],\"links\":[%s]}", getNodesAsJson(true), getLinksAsJson(true));
    }

    private String getNodesAsJson(boolean onlyVPs) {
        String request = onlyVPs ?
                "MATCH (c:VP) WHERE (c:CLASS OR c:INTERFACE) AND NOT c:OUT_OF_SCOPE RETURN collect({types:labels(c), name:c.name, methods:c.methods, constructors:c.constructors, nbVariants:c.nbVariants})" :
                "MATCH (c) WHERE (c:CLASS OR c:INTERFACE) AND NOT c:OUT_OF_SCOPE RETURN collect({types:labels(c), name:c.name, methods:c.methods, constructors:c.constructors})";
        return submitRequest(request)
                .list()
                .get(0)
                .get(0)
                .asList(MapAccessor::asMap)
                .stream()
                .map(o -> new JSONObject(o).toString())
                .collect(Collectors.joining(","));
    }

    private String getLinksAsJson(boolean onlyVPs) {
        String request = onlyVPs ?
                "MATCH path = (c1:VP)-[r:INNER|:EXTENDS|:IMPLEMENTS]->(c2:VP) WHERE NONE(n IN nodes(path) WHERE n:OUT_OF_SCOPE) RETURN collect({source:c1.name, target:c2.name, type:TYPE(r)})" :
                "MATCH path = (c1)-[r:INNER|:EXTENDS|:IMPLEMENTS]->(c2) WHERE NONE(n IN nodes(path) WHERE n:OUT_OF_SCOPE) RETURN collect({source:c1.name, target:c2.name, type:TYPE(r)})";
        return submitRequest(request)
                .list()
                .get(0)
                .get(0)
                .asList(MapAccessor::asMap)
                .stream()
                .map(o -> new JSONObject(o).toString())
                .collect(Collectors.joining(","));
    }

    public String generateStatisticsJson() {
        return new JSONObject()
                .put("methodsVPs", getTotalNbOverloadedMethods())
                .put("constructorsVPs", getTotalNbOverloadedConstructors())
                .put("methodLevelVPs", getNbMethodLevelVPs())
                .put("classLevelVPs", getNbClassLevelVPs())
                .put("methodsVariants", getNbMethodVariants())
                .put("constructorsVariants", getNbConstructorVariants())
                .put("methodLevelVariants", getNbMethodLevelVariants())
                .put("classLevelVariants", getNbClassLevelVariants()).toString();
    }

    public int getNbNodes() {
        return submitRequest("MATCH(n) RETURN count(*)").list().get(0).get(0).asInt();
    }

    public int getNbRelationships() {
        return submitRequest("MATCH (n)-[r]->() RETURN COUNT(r)").list().get(0).get(0).asInt();
    }

    public int getNbInheritanceRelationships() {
        return submitRequest("MATCH (n)-[r:EXTENDS|:IMPLEMENTS]->() RETURN COUNT(r)").list().get(0).get(0).asInt();
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

    private StatementResult submitRequest(String request, Object... parameters) {
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> tx.run(request, parameters(parameters)));
        }
    }

    public void closeDriver() {
        driver.close();
    }

}
