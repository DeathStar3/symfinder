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

let timeout = 500;

function setStorageValuestoMockVariantsDisplaying() {
    sessionStorage.setItem("firstTime", "false");
    sessionStorage.setItem("filteredIsolated", "false");
    sessionStorage.setItem("filteredVariants", "false");
}

describe("Strategy pattern", () => {

    describe("Checking visualization without variants", () => {

        beforeAll(async (done) => {
            await display("tests/data/strategy.json", "tests/data/strategy-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain one node without variants', () => {
            expect(d3.selectAll('circle').size()).toBe(1);
        });
        it('the node should have an S on it', () => {
            expect(d3.select('text[name = "Strategy"]').html()).toBe("S");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking visualization with variants", () => {

        beforeAll(async (done) => {
            setStorageValuestoMockVariantsDisplaying();
            await display("tests/data/strategy.json", "tests/data/strategy-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain three nodes with variants', () => {
            expect(d3.selectAll('circle').size()).toBe(3);
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/strategy.json", "tests/data/strategy-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('The JSON should contain three nodes', () => {
            expect(jsonData.nodes.length).toBe(3);
        });
        it('Strategy is a strategy', () => {
            expect(getNodeWithName(jsonData, "Strategy").types.includes("STRATEGY")).toBeTruthy();
        });
        it('Strategy is a VP', () => {
            expect(getNodeWithName(jsonData, "Strategy").types.includes("VP")).toBeTruthy();
        });
        it('Strategy is the only VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(1);
        });

        afterAll(() => sessionStorage.clear())

    });

});

describe("Factory pattern", () => {

    describe("Checking visualization without variants", () => {

        beforeAll(async (done) => {
            await display("tests/data/factory.json", "tests/data/factory-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain four nodes', () => {
            expect(d3.selectAll('circle').size()).toBe(4);
        });
        it('ShapeFactory node should have an F on it', () => {
            expect(d3.select('text[name = "ShapeFactory"]').html()).toBe("F");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking visualization with variants", () => {

        beforeAll(async (done) => {
            setStorageValuestoMockVariantsDisplaying();
            await display("tests/data/factory.json", "tests/data/factory-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain four nodes', () => {
            expect(d3.selectAll('circle').size()).toBe(4);
        });
        it('ShapeFactory node should have an F on it', () => {
            expect(d3.select('text[name = "ShapeFactory"]').html()).toBe("F");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/factory.json", "tests/data/factory-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('ShapeFactory should be a factory', () => {
            expect(getNodeWithName(jsonData, "ShapeFactory").types.includes("FACTORY")).toBeTruthy();
        });
        it('there should be 3 method level VPs', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(3);
        });
        it('there should be 1 method VP', () => {
            expect(jsonStatsData.methodsVPs).toBe(1);
        });
        it('there should be 2 method variants', () => {
            expect(jsonStatsData.methodsVariants).toBe(2);
        });
        it('there should be 2 constructor VPs', () => {
            expect(jsonStatsData.constructorsVPs).toBe(2);
        });
        it('there should be 4 constructor variants', () => {
            expect(jsonStatsData.constructorsVariants).toBe(4);
        });

        afterAll(() => sessionStorage.clear())

    });

});

describe("Template pattern", () => {

    describe("Checking visualization without variants", () => {

        beforeAll(async (done) => {
            await display("tests/data/template.json", "tests/data/template-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        xit('the graph should contain one node', () => {
            expect(d3.selectAll('circle').size()).toBe(1);
        });
        it('the node should have a T on it', () => {
            expect(d3.select('text[name = "Algorithm"]').html()).toBe("T");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking visualization with variants", () => {

        beforeAll(async (done) => {
            setStorageValuestoMockVariantsDisplaying();
            await display("tests/data/template.json", "tests/data/template-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain two nodes', () => {
            expect(d3.selectAll('circle').size()).toBe(2);
        });
        it('the node should have a T on it', () => {
            expect(d3.select('text[name = "Algorithm"]').html()).toBe("T");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/template.json", "tests/data/template-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('Algorithm should be a template', () => {
            expect(getNodeWithName(jsonData, "Algorithm").types.includes("TEMPLATE")).toBeTruthy();
        });
        it('there should be 0 method level VP', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(0);
        });
        it('there should be 1 class level VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(1);
        });

        afterAll(() => sessionStorage.clear())

    });

});

describe("Decorator pattern", () => {

    describe("Checking visualization without variants", () => {

        beforeAll(async (done) => {
            await display("tests/data/decorator.json", "tests/data/decorator-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        xit('the graph should contain two nodes: the decorator and the Troll interface', () => {
            expect(d3.selectAll('circle').size()).toBe(2);
        });
        xit('the node should have a D on it', () => {
            expect(d3.select('text[name = "com.iluwatar.decorator.ClubbedTroll"]').html()).toBe("D");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking visualization with variants", () => {

        beforeAll(async (done) => {
            setStorageValuestoMockVariantsDisplaying();
            await display("tests/data/decorator.json", "tests/data/decorator-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain four nodes', () => {
            expect(d3.selectAll('circle').size()).toBe(4);
        });
        xit('the node should have a D on it', () => {
            expect(d3.select('text[name = "com.iluwatar.decorator.ClubbedTroll"]').html()).toBe("D");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/decorator.json", "tests/data/decorator-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });


        xit('ClubbedTroll should be a decorator', () => {
            expect(getNodeWithName(jsonData, "com.iluwatar.decorator.ClubbedTroll").types.includes("DECORATOR")).toBeTruthy();
        });
        // TODO update these tests
        xit('there should be 0 method level VP', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(0);
        });
        xit('there should be 1 class level VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(1);
        });

        afterAll(() => sessionStorage.clear())

    });

});

xdescribe("Abstract decorator pattern", () => {

    describe("Checking visualization without variants", () => {

        beforeAll(async (done) => {
            await display("tests/data/abstract_decorator.json", "tests/data/abstract_decorator-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain four nodes', () => {
            expect(d3.selectAll('circle').size()).toBe(4);
        });
        it('the node should have a D on it', () => {
            expect(d3.select('text[name = "TreeDecorator"]').html()).toBe("D");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking visualization with variants", () => {

        beforeAll(async (done) => {
            setStorageValuestoMockVariantsDisplaying();
            await display("tests/data/abstract_decorator.json", "tests/data/abstract_decorator-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain two nodes: the decorator and the ChristmasTree interface', () => {
            expect(d3.selectAll('circle').size()).toBe(2);
        });
        it('the node should have a D on it', () => {
            expect(d3.select('text[name = "TreeDecorator"]').html()).toBe("D");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/abstract_decorator.json", "tests/data/abstract_decorator-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        xit('the node should be a decorator', () => {
            expect(getNodeWithName(jsonData, "TreeDecorator").types.includes("DECORATOR")).toBeTruthy();
        });
        xit('there should be 2 class level VPs', () => {
            expect(jsonStatsData.classLevelVPs).toBe(2);
        });
        it('there should be 0 method level VP', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(0);
        });
        it('there should be 2 variants', () => {
            expect(jsonStatsData.variants).toBe(2);
        });
        it('there should be 2 class level variants', () => {
            expect(jsonStatsData.classLevelVariants).toBe(2);
        });

        afterAll(() => sessionStorage.clear())

    });

});

xdescribe("Generic decorator pattern", () => {

    describe("Checking visualization without variants", () => {

        beforeAll(async (done) => {
            await display("tests/data/generic_decorator.json", "tests/data/generic_decorator-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain two nodes: the decorator and the ChristmasTree interface', () => {
            expect(d3.selectAll('circle').size()).toBe(2);
        });
        it('the node should have a D on it', () => {
            expect(d3.select('text[name = "TreeDecorator"]').html()).toBe("D");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking visualization with variants", () => {

        beforeAll(async (done) => {
            setStorageValuestoMockVariantsDisplaying();
            await display("tests/data/generic_decorator.json", "tests/data/generic_decorator-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain two nodes: the decorator and the ChristmasTree interface', () => {
            expect(d3.selectAll('circle').size()).toBe(2);
        });
        it('the node should have a D on it', () => {
            expect(d3.select('text[name = "TreeDecorator"]').html()).toBe("D");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/generic_decorator.json", "tests/data/generic_decorator-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        xit('the node should be a decorator', () => {
            expect(getNodeWithName(jsonData, "TreeDecorator").types.includes("DECORATOR")).toBeTruthy();
        });

        afterAll(() => sessionStorage.clear())

    });

});

describe("Multiple patterns", () => {

    describe("Checking visualization", () => {

        beforeAll(async (done) => {
            await display("tests/data/multiple_patterns.json", "tests/data/multiple_patterns-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('Factory node should have a F and a S on it', () => {
            expect(d3.select('text[name = "Factory"]').html().includes("F")).toBeTruthy();
            expect(d3.select('text[name = "Factory"]').html().includes("S")).toBeTruthy();
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/multiple_patterns.json", "tests/data/multiple_patterns-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('Factory should be a factory and a strategy', () => {
            expect(getNodeWithName(jsonData, "Factory").types.includes("FACTORY")).toBeTruthy();
            expect(getNodeWithName(jsonData, "Factory").types.includes("STRATEGY")).toBeTruthy();
        });

        afterAll(() => sessionStorage.clear())

    });

});

function getJsonData(file, statsFile) {
    return new Promise(((resolve, reject) => {
        d3.queue()
            .defer(d3.json, file)
            .defer(d3.json, statsFile)
            .await(function (err, data, statsData) {
                resolve([data, statsData]);
            });
    }));
}

function getNodeWithName(jsonData, name) {
    return jsonData.nodes.filter(n => n.name === name)[0];
}