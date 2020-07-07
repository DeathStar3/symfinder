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

describe("Generating graph with one node", () => {

    beforeAll(async () => {
        await display("tests/data/graph1.json", "tests/data/stats.json", []);
    });

    it('svg should exist', () => {
        var svg = document.getElementsByTagName('svg');
        expect(svg).not.toBe(null);
    });
    it('generated graph should contain one node', async () => {
        expect(d3.selectAll('circle').size()).toBe(1);
    });

});

describe("Generating graph with two linked nodes", () => {

    beforeAll(async () => {
        await display("tests/data/graph2.json", "tests/data/stats.json", []);
    });

    it('svg should exist', () => {
        var svg = document.getElementsByTagName('svg');
        expect(svg).not.toBe(null);
    });
    it('generated graph should contain two nodes', () => {
        expect(d3.selectAll('circle').size()).toBe(2);
    });
    it('generated graph should contain one link', () => {
        expect(d3.selectAll('line').size()).toBe(1);
    });

});

describe("Generating different types of nodes", () => {

    beforeAll(async () => {
        await display("tests/data/types_of_nodes.json", "tests/data/stats.json", []);
    });

    it('svg should exist', () => {
        var svg = document.getElementsByTagName('svg');
        expect(svg).not.toBe(null);
    });
    it('generated graph should contain seven nodes', () => {
        expect(d3.selectAll('circle').size()).toBe(7);
    });
    it('standard class node should not have a dotted outline', () => {
        expect(d3.select('circle[name = "standardNode"]').style("stroke-dasharray")).toBe("3, 0");
    });
    it('standard class node should not be black', () => {
        expect(d3.select('circle[name = "standardNode"]').attr("fill")).not.toBe("rgb(0, 0, 0)");
    });
    it('abstract class node should have a dotted outline', () => {
        expect(d3.select('circle[name = "abstractClassNode"]').style("stroke-dasharray")).toBe("3, 3");
    });
    it('interface node should be black', () => {
        expect(d3.select('circle[name = "interfaceNode"]').attr("fill")).toBe("rgb(0, 0, 0)");
    });
    it('strategy node should have an S on it', () => {
        expect(d3.select('text[name = "strategyNode"]').html()).toBe("S");
    });
    it('factory node should have an F on it', () => {
        expect(d3.select('text[name = "factoryNode"]').html()).toBe("F");
    });
    it('template node should have a T on it', () => {
        expect(d3.select('text[name = "templateNode"]').html()).toBe("T");
    });
    it('decorator node should have an D on it', () => {
        expect(d3.select('text[name = "decoratorNode"]').html()).toBe("D");
    });

});

describe("Generating different types of nodes", () => {

    beforeAll(async () => {
        await display("tests/data/graph3.json", "tests/data/stats.json", []);
    });

    it('svg should exist', () => {
        var svg = document.getElementsByTagName('svg');
        expect(svg).not.toBe(null);
    });
    it('node size increases with the number of methods overloads', () => {
        expect(d3.select('circle[name = "OneMethodOverload"]').attr("r")).toBeLessThan(d3.select('circle[name = "TenMethodOverloads"]').attr("r"));
    });
    it('node color gets closer to red with the number of methods overloads', () => {
        expect(distanceToRedFromString(d3.select('circle[name = "TenConstructorOverloads"]').attr("fill")))
            .toBeLessThan(distanceToRedFromString(d3.select('circle[name = "OneConstructorOverload"]').attr("fill")));
    });
    it('node stroke-width is the same  for all number of variants', () => {
        expect(d3.select('circle[name = "NoVariant"]').style("stroke-width"))
            .toBe(d3.select('circle[name = "TenVariants"]').style("stroke-width"));
    });
    it('design pattern identifier should become light if the node gets dark', () => {
        expect(d3.select('text[name = "factoryClassNode"]').attr("fill")).toBe("rgb(0, 0, 0)");
        expect(d3.select('text[name = "factoryInterfaceNode"]').attr("fill")).toBe("rgb(255, 255, 255)");
    });

});

describe("Testing utils functions : distanceToRed", () => {

    it('distance from red to red is 0', () => {
        expect(distanceToRed(255, 0, 0)).toBe(0);
    });
    it('distance increases as green increases', () => {
        expect(distanceToRed(255, 10, 0)).toBeGreaterThan(0);
        expect(distanceToRed(255, 10, 0)).toBeLessThan(distanceToRed(255, 100, 0));
    });
    it('distance increases as blue increases', () => {
        expect(distanceToRed(255, 0, 10)).toBeGreaterThan(0);
        expect(distanceToRed(255, 0, 10)).toBeLessThan(distanceToRed(255, 0, 100));
    });
    it('distance increases as green and blue increase', () => {
        expect(distanceToRed(255, 10, 10)).toBeGreaterThan(0);
        expect(distanceToRed(255, 10, 10)).toBeLessThan(distanceToRed(255, 100, 100));
    });

});

xdescribe("Testing utils functions : matchesFilter", () => {

    it('package filtering', () => {
        expect(NodeFilter.matchesFilter("foo.bar.Clazz", "foo.bar")).toBeTruthy();
        expect(NodeFilter.matchesFilter("bar.Clazz", "foo.bar")).toBeFalsy();
        expect(NodeFilter.matchesFilter("foo.bar.Clazz", "bar")).toBeFalsy();
    });
    it('class filtering', () => {
        expect(NodeFilter.matchesFilter("foo.bar.Clazz", "foo.bar.Clazz")).toBeTruthy();
        expect(NodeFilter.matchesFilter("foo.bar.Clazz", "foo.bar.Clazzz")).toBeFalsy();
        expect(NodeFilter.matchesFilter("foo.bar.Clazzz", "foo.bar.Clazz")).toBeFalsy();
    });

});

function distanceToRedFromString(color){
    return distanceToRed(...color.replace(/[^\d,]/g, '').split(','));
}

function distanceToRed(r, g, b) {
    const red = 255 - r;
    const green = 0 - g;
    const blue = 0 - b;
    return red * red + green * green + blue * blue
}
