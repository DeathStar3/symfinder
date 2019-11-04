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

import neo4j_types.EntityType;
import org.junit.Test;
import org.neo4j.driver.v1.types.Node;

import java.util.Optional;

import static org.junit.Assert.*;

public class GetNodeTest extends Neo4jTest {

    @Test
    public void getNodeInRealPackage(){
        runTest(graph -> {
            Node classNode = graph.createNode("fr.unice.i3s.TestClass", EntityType.CLASS);
            Optional <Node> foundNode = graph.getNodeWithNameInPackage("TestClass", "fr.unice.i3s");
            assertTrue(foundNode.isPresent());
            assertEquals(classNode, foundNode.get());
        });
    }

    @Test
    public void getNodeInUpperPackage(){
        runTest(graph -> {
            Node classNode = graph.createNode("fr.unice.i3s.TestClass", EntityType.CLASS);
            Optional <Node> foundNode = graph.getNodeWithNameInPackage("TestClass", "fr.unice");
            assertTrue(foundNode.isPresent());
            assertEquals(classNode, foundNode.get());
        });
    }

    @Test
    public void inexistantClassName(){
        runTest(graph -> {
            graph.createNode("fr.unice.i3s.TestClass", EntityType.CLASS);
            Optional <Node> foundNode = graph.getNodeWithNameInPackage("Test", "fr.unice");
            assertFalse(foundNode.isPresent());
        });
    }

    @Test
    public void inexistantPackage(){
        runTest(graph -> {
            graph.createNode("fr.unice.i3s.TestClass", EntityType.CLASS);
            Optional <Node> foundNode = graph.getNodeWithNameInPackage("TestClass", "fr.i3s");
            assertFalse(foundNode.isPresent());
        });
    }

    @Test
    public void incompletePackage(){
        runTest(graph -> {
            graph.createNode("fr.unice.i3s.TestClass", EntityType.CLASS);
            Optional <Node> foundNode = graph.getNodeWithNameInPackage("TestClass", "fr.uni");
            assertFalse(foundNode.isPresent());
        });
    }

}
