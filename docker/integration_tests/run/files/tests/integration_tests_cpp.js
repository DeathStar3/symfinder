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

describe("Basic inheritance with Shape Class", () => {

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/shape.json", "tests/data/shape-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('there should be 0 method VPs', () => {
            expect(jsonStatsData.methodsVPs).toBe(0);
        });
        it('there should be 0 method variants', () => {
            expect(jsonStatsData.methodsVariants).toBe(0);
        });
        it('there should be 1 constructor VPs', () => {
            expect(jsonStatsData.constructorsVPs).toBe(1);
        });
        it('there should be 2 constructor variants', () => {
            expect(jsonStatsData.constructorsVariants).toBe(2);
        });
        it('there should be 1 method level VPs', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(1);
        });
        it('there should be 2 method level variants', () => {
            expect(jsonStatsData.methodLevelVariants).toBe(2);
        });
        it('there should be 1 class level VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(1);
        });
        it('there should be 2 class level variants', () => {
            expect(jsonStatsData.classLevelVariants).toBe(2);
        });
        it('there should be 2 VPs', () => {
            expect(jsonStatsData.VPs).toBe(2);
        });
        it('there should be 4 variants', () => {
            expect(jsonStatsData.variants).toBe(4);
        });

    });

});

describe("Basic inheritance with Shape Class (with headers)", () => {

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/shape-h.json", "tests/data/shape-h-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('there should be 0 method VPs', () => {
            expect(jsonStatsData.methodsVPs).toBe(0);
        });
        it('there should be 0 method variants', () => {
            expect(jsonStatsData.methodsVariants).toBe(0);
        });
        it('there should be 1 constructor VPs', () => {
            expect(jsonStatsData.constructorsVPs).toBe(1);
        });
        it('there should be 2 constructor variants', () => {
            expect(jsonStatsData.constructorsVariants).toBe(2);
        });
        it('there should be 1 method level VPs', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(1);
        });
        it('there should be 2 method level variants', () => {
            expect(jsonStatsData.methodLevelVariants).toBe(2);
        });
        it('there should be 1 class level VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(1);
        });
        it('there should be 2 class level variants', () => {
            expect(jsonStatsData.classLevelVariants).toBe(2);
        });
        it('there should be 2 VPs', () => {
            expect(jsonStatsData.VPs).toBe(2);
        });
        it('there should be 4 variants', () => {
            expect(jsonStatsData.variants).toBe(4);
        });

    });

});

describe("Macro", () => {

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/macro.json", "tests/data/macro-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('there should be 0 method VPs', () => {
            expect(jsonStatsData.methodsVPs).toBe(0);
        });
        it('there should be 0 method variants', () => {
            expect(jsonStatsData.methodsVariants).toBe(0);
        });
        it('there should be 0 constructor VPs', () => {
            expect(jsonStatsData.constructorsVPs).toBe(0);
        });
        it('there should be 0 constructor variants', () => {
            expect(jsonStatsData.constructorsVariants).toBe(0);
        });
        it('there should be 0 method level VPs', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(0);
        });
        it('there should be 0 method level variants', () => {
            expect(jsonStatsData.methodLevelVariants).toBe(0);
        });
        it('there should be 1 class level VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(1);
        });
        it('there should be 1 class level variants', () => {
            expect(jsonStatsData.classLevelVariants).toBe(1);
        });
        it('there should be 1 VPs', () => {
            expect(jsonStatsData.VPs).toBe(1);
        });
        it('there should be 1 variants', () => {
            expect(jsonStatsData.variants).toBe(1);
        });

    });

});

describe("Shape namespace", () => {

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/shape-namespace.json", "tests/data/shape-namespace-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('there should be 0 method VPs', () => {
            expect(jsonStatsData.methodsVPs).toBe(0);
        });
        it('there should be 0 method variants', () => {
            expect(jsonStatsData.methodsVariants).toBe(0);
        });
        it('there should be 3 constructor VPs', () => {
            expect(jsonStatsData.constructorsVPs).toBe(3);
        });
        it('there should be 6 constructor variants', () => {
            expect(jsonStatsData.constructorsVariants).toBe(6);
        });
        it('there should be 3 method level VPs', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(3);
        });
        it('there should be 6 method level variants', () => {
            expect(jsonStatsData.methodLevelVariants).toBe(6);
        });
        it('there should be 1 class level VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(1);
        });
        it('there should be 3 class level variants', () => {
            expect(jsonStatsData.classLevelVariants).toBe(3);
        });
        it('there should be 4 VPs', () => {
            expect(jsonStatsData.VPs).toBe(4);
        });
        it('there should be 9 variants', () => {
            expect(jsonStatsData.variants).toBe(9);
        });

        // TODO: testing entity attributes "CPP_TEMPLATE" and "CPP_TEMPLATE_INSTANTIATION"
    });

});

describe("Template-Class", () => {

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/template-class.json", "tests/data/template-class-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('there should be 0 method VPs', () => {
            expect(jsonStatsData.methodsVPs).toBe(0);
        });
        it('there should be 0 method variants', () => {
            expect(jsonStatsData.methodsVariants).toBe(0);
        });
        it('there should be 2 constructor VPs', () => {
            expect(jsonStatsData.constructorsVPs).toBe(2);
        });
        it('there should be 4 constructor variants', () => {
            expect(jsonStatsData.constructorsVariants).toBe(4);
        });
        it('there should be 2 method level VPs', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(2);
        });
        it('there should be 4 method level variants', () => {
            expect(jsonStatsData.methodLevelVariants).toBe(4);
        });
        it('there should be 6 class level VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(6);
        });
        it('there should be 4 class level variants', () => {
            expect(jsonStatsData.classLevelVariants).toBe(4);
        });
        it('there should be 8 classes having the VARIANTS label', () => {
            expect(jsonData.nodes.filter(n => n.types.includes("VARIANT")).length).toBe(8);
        });
        it('there should be 8 VPs', () => {
            expect(jsonStatsData.VPs).toBe(8);
        });
        it('there should be 8 variants', () => {
            expect(jsonStatsData.variants).toBe(8);
        });

    });

});
describe("Shape-h Methods", ()=>{
    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/shape-h.json", "tests/data/shape-h-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('there should be 1 abstract node', () => {
            expect(jsonData.nodes.filter((a)=>a.types.includes("ABSTRACT")).length).toBe(1);
        });
    });

})
describe("Checking multiple inheritance", () => {

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/multiple-inheritance.json", "tests/data/multiple-inheritance-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('there should be 0 method VPs', () => {
            expect(jsonStatsData.methodsVPs).toBe(0);
        });
        it('there should be 0 method variants', () => {
            expect(jsonStatsData.methodsVariants).toBe(0);
        });
        it('there should be 0 constructor VPs', () => {
            expect(jsonStatsData.constructorsVPs).toBe(0);
        });
        it('there should be 0 constructor variants', () => {
            expect(jsonStatsData.constructorsVariants).toBe(0);
        });
        it('there should be 0 method level VPs', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(0);
        });
        it('there should be 0 method level variants', () => {
            expect(jsonStatsData.methodLevelVariants).toBe(0);
        });
        it('there should be 6 class level VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(6);
        });
        it('there should be 3 class level variants', () => {
            expect(jsonStatsData.classLevelVariants).toBe(3);
        });
        it('there should be 5 classes having the VARIANTS label', () => {
            expect(jsonData.nodes.filter(n => n.types.includes("VARIANT")).length).toBe(5);
        });
        it('there should be 6 VPs', () => {
            expect(jsonStatsData.VPs).toBe(6);
        });
        it('there should be 3 variants', () => {
            expect(jsonStatsData.variants).toBe(3);
        });

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