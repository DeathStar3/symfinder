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

describe("Testing JSON output for multiple_vp", () => {

    var jsonData, jsonStatsData;

    beforeAll(async (done) => {
        const [graph, stats] = await getJsonData("tests/data/multiple_vp.json", "tests/data/multiple_vp-stats.json");
        jsonData = graph;
        jsonStatsData = stats;
        done();
    });

    it('there should be 1 class level VP', () => {
        expect(jsonStatsData.classLevelVPs).toBe(1);
    });
    it('there should be 0 class level variant', () => {
        expect(jsonStatsData.classLevelVariants).toBe(0);
    });
    it('there should be 2 method level VPs', () => {
        expect(jsonStatsData.methodLevelVPs).toBe(2);
    });
    it('there should be 1 method VP', () => {
        expect(jsonStatsData.methodsVPs).toBe(1);
    });
    it('there should be 1 constructor VP', () => {
        expect(jsonStatsData.constructorsVPs).toBe(1);
    });
    it('there should be 2 method variants', () => {
        expect(jsonStatsData.methodsVariants).toBe(2);
    });
    it('there should be 2 constructor variants', () => {
        expect(jsonStatsData.constructorsVariants).toBe(2);
    });

});

describe("Testing JSON output for generics", () => {

    var jsonData, jsonStatsData;

    beforeAll(async (done) => {
        const [graph, stats] = await getJsonData("tests/data/generics.json", "tests/data/generics-stats.json");
        jsonData = graph;
        jsonStatsData = stats;
        done();
    });

    it('there should be a node called MyPair', () => {
        expect(jsonData.nodes.filter(n => n.name === "MyPair").length).toBe(1);
    });
    it('MyPair should be a strategy', () => {
        expect(getNodeWithName(jsonData, "MyPair").types.includes("STRATEGY")).toBeTruthy();
    });

});

describe("Importing a class from another package", () => {

    var jsonData, jsonStatsData;

    beforeAll(async (done) => {
        const [graph, stats] = await getJsonData("tests/data/import_from_different_package.json", "tests/data/import_from_different_package-stats.json");
        jsonData = graph;
        jsonStatsData = stats;
        done();
    });

    it('there should be a node called AbstractAlgo', () => {
        expect(jsonData.nodes.filter(n => n.name === "abs.AbstractAlgo").length).toBe(1);
    });
    it('there should be 1 class level variant', () => {
        expect(jsonStatsData.classLevelVariants).toBe(1);
    });

});

describe("Importing a whole package", () => {

    var jsonData, jsonStatsData;

    beforeAll(async (done) => {
        const [graph, stats] = await getJsonData("tests/data/import_from_different_package_all_package_imported.json", "tests/data/import_from_different_package_all_package_imported-stats.json");
        jsonData = graph;
        jsonStatsData = stats;
        done();
    });

    it('there should be a node called AbstractAlgo', () => {
        expect(jsonData.nodes.filter(n => n.name === "abs.AbstractAlgo").length).toBe(1);
    });
    it('there should be 1 class level variant', () => {
        expect(jsonStatsData.classLevelVariants).toBe(1);
    });

});

describe("Inner class", () => {

    var jsonData, jsonStatsData;

    beforeAll(async (done) => {
        const [graph, stats] = await getJsonData("tests/data/inner_class.json", "tests/data/inner_class-stats.json");
        jsonData = graph;
        jsonStatsData = stats;
        done();
    });

    it('there should be no variant', () => {
        expect(jsonStatsData.classLevelVariants).toBe(0);
    });

});

describe("Inner class defined before fields", () => {

    var jsonData, jsonStatsData;

    beforeAll(async (done) => {
        const [graph, stats] = await getJsonData("tests/data/inner_class_before_fields.json", "tests/data/inner_class_before_fields-stats.json");
        jsonData = graph;
        jsonStatsData = stats;
        done();
    });

    it('there should be no variant', () => {
        expect(jsonStatsData.classLevelVariants).toBe(0);
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