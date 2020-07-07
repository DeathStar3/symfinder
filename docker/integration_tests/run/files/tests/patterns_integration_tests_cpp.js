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


function setStorageValuestoMockVariantsDisplaying() {
    sessionStorage.setItem("firstTime", "false");
    sessionStorage.setItem("filteredIsolated", "false");
    sessionStorage.setItem("filteredVariants", "false");
}

describe("Strategy pattern CPP", () => {

    describe("Checking visualization without variants", () => {

        beforeAll(async (done) => {
            await display("tests/data/cpp_strategy.json", "tests/data/cpp_strategy-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the node should have an S on it', () => {
            expect(d3.select('text[name = "Sorting::ISortStrategy"]').html()).toBe("S");
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking visualization with variants", () => {

        beforeAll(async (done) => {
            setStorageValuestoMockVariantsDisplaying();
            await display("tests/data/cpp_strategy.json", "tests/data/cpp_strategy-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain three nodes with variants', () => {
            expect(d3.selectAll('circle').size()).toBe(8);
        });

        afterAll(() => sessionStorage.clear())

    });

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/cpp_strategy.json", "tests/data/cpp_strategy-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });

        it('The JSON should contain eight nodes', () => {
            expect(jsonData.nodes.length).toBe(8);
        });
        it('Strategy is a strategy', () => {
            expect(getNodeWithName(jsonData, "Sorting::ISortStrategy").types.includes("STRATEGY")).toBeTruthy();
        });
        it('Strategy is a VP', () => {
            expect(getNodeWithName(jsonData, "Sorting::ISortStrategy").types.includes("VP")).toBeTruthy();
        });
        it('There are 2 inheritance VP and 1 Strategy VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(3);
        });

        afterAll(() => sessionStorage.clear())

    });

});

describe("Decorator pattern CPP", () => {

    describe("Checking visualization without variants", () => {

        beforeAll(async (done) => {
            await display("tests/data/cpp_decorator.json", "tests/data/cpp_decorator-stats.json", []);
            setTimeout(() => done(), timeout); // wait
        });

        it('the graph should contain three nodes: the two decorator and the Window interface', () => {
            expect(d3.selectAll('circle').size()).toBe(3);
        });
        it('the structural node should have a D on it', () => {
            expect(d3.select('text[name = "WindowScrollBar"]').html()).toBe("D");
        });
        it('the named node should have a D on it', () => {
            expect(d3.select('text[name = "PrettyDecorator"]').html()).toBe("D");
        });
        afterAll(() => sessionStorage.clear())

    });

    describe("Checking JSON output", () => {

        var jsonData, jsonStatsData;

        beforeAll(async (done) => {
            const [graph, stats] = await getJsonData("tests/data/cpp_decorator.json", "tests/data/cpp_decorator-stats.json");
            jsonData = graph;
            jsonStatsData = stats;
            done();
        });


        it('WindowScrollBar should be a decorator', () => {
            expect(getNodeWithName(jsonData, "WindowScrollBar").types.includes("DECORATOR")).toBeTruthy();
        });
        xit('WindowDecorator should be a decorator', () => {
            expect(getNodeWithName(jsonData, "WindowDecorator").types.includes("DECORATOR")).toBeTruthy();
        });
        it('there should be 0 method level VP', () => {
           expect(jsonStatsData.methodLevelVPs).toBe(0);
        });
        it('there should be 3 class level VP', () => {
            expect(jsonStatsData.classLevelVPs).toBe(3);
        });

        afterAll(() => sessionStorage.clear())

    });

});

