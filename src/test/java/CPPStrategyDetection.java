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

import neo4j_types.EntityAttribute;
import neo4j_types.EntityType;
import neo4j_types.RelationType;
import org.junit.Test;
import org.neo4j.driver.types.Node;

import static neo4j_types.DesignPatternType.STRATEGY;
import static org.junit.Assert.*;

public class CPPStrategyDetection extends Neo4jTest {

    @Test
    public void strategyNameIsNotEnough() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("ShapeStrategy", EntityType.CLASS);
            graph.detectCPPStrategyPatterns();
            assertFalse(graph.getNode("ShapeStrategy").get().hasLabel(STRATEGY.toString()));
        });
    }

    @Test
    public void strategyNamedReferencedIsDetected() {
        runTest(graph -> {
            Node shapeClass = graph.createNode("ShapeStrategy", EntityType.CLASS);
            Node geometry = graph.createNode("Geometry",EntityType.CLASS);
            Node shapeStrategy = graph.createNode("shapeStrategy",EntityType.ATTRIBUTE);
            graph.setNodeAttribute(shapeStrategy,"type","ShapeStrategy");
            graph.linkTwoNodes(geometry,shapeStrategy,RelationType.ATTRIBUTE);
            graph.detectCPPStrategyPatterns();
            assertTrue(graph.getNode("ShapeStrategy").get().hasLabel(STRATEGY.toString()));
        });
    }

    @Test
    public void strategyObjectHierarchyIsDetected() {
        runTest(graph->{
            Node strategyInterface = graph.createNode("WarGeneralInterface",EntityType.CLASS);
            Node strategySunTzu = graph.createNode("GeneralSunTzu",EntityType.CLASS);
            Node strategyNapoleon = graph.createNode("GeneralNapoleon",EntityType.CLASS);

            graph.linkTwoNodes(strategyInterface,strategySunTzu,RelationType.EXTENDS);
            graph.linkTwoNodes(strategyInterface,strategyNapoleon,RelationType.EXTENDS);

            Node greatestGeneral = graph.createNode("GreatestGeneral",EntityType.CLASS);
            Node generalAttribute = graph.createNode("warStrategy",EntityType.ATTRIBUTE);
            graph.setNodeAttribute(generalAttribute,"type","WarGeneralInterface");

            graph.linkTwoNodes(greatestGeneral,generalAttribute,RelationType.ATTRIBUTE);

            graph.detectCPPStrategyPatterns();

            assertTrue(graph.getNode("WarGeneralInterface").get().hasLabel(STRATEGY.toString()));


        });
    }

}