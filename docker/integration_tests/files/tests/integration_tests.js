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

function resetPage() {
    $("#list-tab").empty();
    $("#package-to-filter").val("");
    sessionStorage.clear();
}

describe("Basic inheritance", () => {

    describe("Checking visualization without variants", () => {

        beforeAll(async (done) => {
            await display("tests/data/inheritance.json", "tests/data/inheritance-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain Superclass as it has two variants', () => {
            expect(d3.select('circle[name = "Superclass"]').empty()).toBeFalsy();
        });
        it('the graph should contain the SubclassTwo as it is a VP', () => {
            expect(d3.select('circle[name = "SubclassTwo"]').empty()).toBeFalsy();
        });
        it('the graph should not contain the SubclassOne as it is not a VP', () => {
            expect(d3.select('circle[name = "SubclassOne"]').empty()).toBeTruthy();
        });
        it('there should be one link', () => {
            expect(d3.selectAll('line').size()).toBe(1);
        });
        it('Superclass and SubclassTwo should be linked', () => {
            expect(d3.select('line').attr("target")).toBe("SubclassTwo");
            expect(d3.select('line').attr("source")).toBe("Superclass");
        });

        afterAll(resetPage);

    });

    describe("Checking visualization with variants", () => {

        beforeAll(async (done) => {
            setStorageValuestoMockVariantsDisplaying();
            await display("tests/data/inheritance.json", "tests/data/inheritance-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain Superclass as it has two variants', () => {
            expect(d3.select('circle[name = "Superclass"]').empty()).toBeFalsy();
        });
        it('the graph should contain the SubclassTwo as it is a VP', () => {
            expect(d3.select('circle[name = "SubclassTwo"]').empty()).toBeFalsy();
        });
        it('the graph should contain SubclassOne as it is a variant', () => {
            expect(d3.select('circle[name = "SubclassOne"]').empty()).toBeFalsy();
        });
        it('there should be two links', () => {
            expect(d3.selectAll('line').size()).toBe(2);
        });

        afterAll(resetPage);

    });

});


describe("Language structures", () => {

    describe("Checking visualization without variants", () => {

        beforeAll(async (done) => {
            await display("tests/data/structures.json", "tests/data/structures-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the abstract class should appear', () => {
            expect(d3.select('circle[name = "AbstractClass"]').empty()).toBeFalsy();
        });
        it('the abstract class should have a dotted outline', () => {
            expect(d3.select('circle[name = "AbstractClass"]').style("stroke-dasharray")).toBe("3, 3");
        });
        it('the interface should appear', () => {
            expect(d3.select('circle[name = "Interface"]').empty()).toBeFalsy();
        });
        it('the interface should be black', () => {
            expect(d3.select('circle[name = "Interface"]').attr("fill")).toBe("rgb(0, 0, 0)");
        });
        it('the normal class should not appear', () => {
            expect(d3.select('circle[name = "NormalClass"]').empty()).toBeTruthy();
        });
        it('the normal class being a VP should appear', () => {
            expect(d3.select('circle[name = "NormalClassVP"]').empty()).toBeFalsy();
        });

    });

    describe("Checking visualization with variants", () => {

        beforeAll(async (done) => {
            setStorageValuestoMockVariantsDisplaying();
            await display("tests/data/structures.json", "tests/data/structures-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the normal class should not appear as it is not a variant', () => {
            expect(d3.select('circle[name = "NormalClass"]').empty()).toBeTruthy();
        });

    });

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/structures.json", "tests/data/structures-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('AbstractClass should be an abstract class', () => {
            expect(getNodeWithName(jsonData, "AbstractClass").types.includes("ABSTRACT")).toBeTruthy();
            expect(getNodeWithName(jsonData, "AbstractClass").types.includes("CLASS")).toBeTruthy();
        });
        it('Interface should be an interface and not a class', () => {
            expect(getNodeWithName(jsonData, "Interface").types.includes("INTERFACE")).toBeTruthy();
            expect(getNodeWithName(jsonData, "Interface").types.includes("CLASS")).toBeFalsy();
        });
        it('there should be 2 class level VPs', () => {
            expect(jsonStatsData.classLevelVPs).toBe(2);
        });
        it('there should be 1 method level VP', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(1);
        });
        it('the method level VP should be a constructor VP', () => {
            expect(jsonStatsData.constructorsVPs).toBe(1);
        });
        it('there should be two constructor variants', () => {
            expect(jsonStatsData.constructorsVariants).toBe(2);
        });

    });

});


describe("Comparing metrics evolution", () => {

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/metrics.json", "tests/data/metrics-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        xit('NoConstructorOverload should have 0 constructor VP', () => {
            expect(getNodeWithName(jsonData, "NoConstructorOverload").constructorVPs).toBe(0);
        });
        xit('NoConstructorOverload should have 0 constructor variant', () => {
            expect(getNodeWithName(jsonData, "NoConstructorOverload").constructorVariants).toBe(0);
        });
        it('OneConstructorOverload should have 1 constructor VP', () => {
            expect(getNodeWithName(jsonData, "OneConstructorOverload").constructorVPs).toBe(1);
        });
        it('OneConstructorOverload should have 2 constructor variants', () => {
            expect(getNodeWithName(jsonData, "OneConstructorOverload").constructorVariants).toBe(2);
        });
        it('OneConstructorOverload should have the METHOD_LEVEL_VP label', () => {
            expect(getNodeWithName(jsonData, "OneConstructorOverload").types.includes("METHOD_LEVEL_VP")).toBeTruthy();
        });
        it('TwoConstructorOverloads should have 1 overloaded constructor', () => {
            expect(getNodeWithName(jsonData, "TwoConstructorOverloads").constructorVPs).toBe(1);
        });
        it('TwoConstructorOverloads should have 3 constructor variants', () => {
            expect(getNodeWithName(jsonData, "TwoConstructorOverloads").constructorVariants).toBe(3);
        });
        it('TwoConstructorOverloads should have the METHOD_LEVEL_VP label', () => {
            expect(getNodeWithName(jsonData, "TwoConstructorOverloads").types.includes("METHOD_LEVEL_VP")).toBeTruthy();
        });
        it('OneMethodOverload should have 1 method VP', () => {
            expect(getNodeWithName(jsonData, "OneMethodOverload").methodVPs).toBe(1);
        });
        it('OneMethodOverload should have 2 method variants', () => {
            expect(getNodeWithName(jsonData, "OneMethodOverload").methodVariants).toBe(2);
        });
        it('OneMethodOverload should have the METHOD_LEVEL_VP label', () => {
            expect(getNodeWithName(jsonData, "OneMethodOverload").types.includes("METHOD_LEVEL_VP")).toBeTruthy();
        });
        it('TwoMethodOverloads should have 2 method VPs', () => {
            expect(getNodeWithName(jsonData, "TwoMethodOverloads").methodVPs).toBe(2);
        });
        it('TwoMethodOverloads should have 4 method variants', () => {
            expect(getNodeWithName(jsonData, "TwoMethodOverloads").methodVariants).toBe(4);
        });
        it('TwoMethodOverloads should have the METHOD_LEVEL_VP label', () => {
            expect(getNodeWithName(jsonData, "TwoMethodOverloads").types.includes("METHOD_LEVEL_VP")).toBeTruthy();
        });
        it('there should be 5 method level VPs', () => {
            expect(jsonStatsData.methodLevelVPs).toBe(5);
        });
        it('there should be 2 constructor VPs', () => {
            expect(jsonStatsData.constructorsVPs).toBe(2);
        });
        it('there should be 3 method VPs', () => {
            expect(jsonStatsData.methodsVPs).toBe(3);
        });
        it('there should be 11 method level variants', () => {
            expect(jsonStatsData.methodLevelVariants).toBe(11);
        });
        it('there should be 5 constructor variants', () => {
            expect(jsonStatsData.constructorsVariants).toBe(5);
        });
        it('there should be 6 method variants', () => {
            expect(jsonStatsData.methodsVariants).toBe(6);
        });
        it('there should be 0 class level VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(0);
        });
        it('there should be 0 class level variants', () => {
            expect(jsonStatsData.classLevelVariants).toBe(0);
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