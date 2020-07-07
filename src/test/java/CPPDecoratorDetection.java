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

import neo4j_types.DesignPatternType;
import neo4j_types.EntityType;
import neo4j_types.RelationType;
import org.junit.Test;
import org.neo4j.driver.types.Node;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CPPDecoratorDetection extends Neo4jTest {

    @Test
    public void decoratorNameDetected() {
        runTest(graph->{
            Node window = graph.createNode("Window",EntityType.CLASS);
            Node hugeWindow = graph.createNode("HugeWindow",EntityType.CLASS);

            graph.linkTwoNodes(window,hugeWindow,RelationType.EXTENDS);

            Node decorator = graph.createNode("WindowDecorator",EntityType.CLASS);

            graph.linkTwoNodes(window,decorator,RelationType.EXTENDS);



            Node windowAttribute = graph.createNode("window", EntityType.ATTRIBUTE);
            graph.setNodeAttribute(windowAttribute,"type","Window");
            graph.linkTwoNodes(decorator,windowAttribute,RelationType.ATTRIBUTE);

            graph.detectCPPDecoratorPatterns();

            assertTrue(graph.getNode("WindowDecorator").get().hasLabel(DesignPatternType.DECORATOR.toString()));
        });
    }

    @Test
    public void decoratorObjectStructureDetected() {
        runTest(graph->{
            Node window = graph.createNode("Window",EntityType.CLASS);
            Node hugeWindow = graph.createNode("HugeWindow",EntityType.CLASS);

            graph.linkTwoNodes(window,hugeWindow,RelationType.EXTENDS);

            Node decorator = graph.createNode("WindowSublimer",EntityType.CLASS);

            graph.linkTwoNodes(window,decorator,RelationType.EXTENDS);

            Node damidoDecorator = graph.createNode("Damido",EntityType.CLASS);
            Node lauraDecorator = graph.createNode("LauraR",EntityType.CLASS);
            graph.linkTwoNodes(decorator,damidoDecorator,RelationType.EXTENDS);
            graph.linkTwoNodes(decorator,lauraDecorator,RelationType.EXTENDS);

            Node windowAttribute = graph.createNode("window", EntityType.ATTRIBUTE);
            graph.setNodeAttribute(windowAttribute,"type","Window");
            graph.linkTwoNodes(decorator,windowAttribute,RelationType.ATTRIBUTE);

            graph.detectCPPDecoratorPatterns();

            assertTrue(graph.getNode("WindowSublimer").get().hasLabel(DesignPatternType.DECORATOR.toString()));
        });
    }

}