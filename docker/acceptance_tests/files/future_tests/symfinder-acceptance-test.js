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

describe("Acceptance tests for all projects", () => {

    projects = __karma__.config.projects.split(",");

    projects.forEach(project => {

        describe("Acceptance tests for " + project, () => {

            describe("Checking JSON content", () => {

                var jsonData, jsonStatsData;

                beforeAll(async (done) => {
                    const [graph, stats] = await getJsonData("tests/data/" + project + ".json", "tests/data/" + project + "-stats.json");
                    jsonData = graph;
                    jsonStatsData = stats;
                    done();
                });


                it('The number of method VPs corresponds to the number of methods appearing at least two times', () => {
                    expect(jsonData.nodes.every(n => {
                        if (typeof n.methodVPs !== 'undefined') {
                            var nbVPs = n.methodVPs;
                            var nbMethodsWithAtLeastTwoAppearances = n.methods.filter(m => m.number >= 2).length;
                            return nbVPs === nbMethodsWithAtLeastTwoAppearances;
                        }
                        return true;
                    })).toBeTruthy();
                });

                it('The number of constructor VPs corresponds to 1 if there is more than one constructor', () => {
                    expect(jsonData.nodes.every(n => {
                        if ((typeof n.constructorVPs !== 'undefined') && (typeof n.constructors !== 'undefined')) {
                            if (n.constructors.length === 0 || n.constructors[0].number === 1) {
                                return n.constructorVPs === 0;
                            } else {
                                return n.constructorVPs === 1;
                            }
                        }
                        return true;
                    })).toBeTruthy();
                });

                it('The constructors have the same name.', () => {
                    expect(jsonData.nodes.every(n => n.constructors.length <= 1)).toBeTruthy();
                });

                it('The number of method variants corresponds to the number of appearances of methods that appear more than once', () => {
                    expect(jsonData.nodes.every(n => {
                        if (typeof n.methodVariants !== 'undefined') {
                            var nbVariants = n.methodVariants;
                            var nbMethodsWithAtLeastTwoAppearances = n.methods.filter(m => m.number >= 2).map(m => m.number).reduce((a, b) => a + b, 0);
                            return nbVariants === nbMethodsWithAtLeastTwoAppearances;
                        }
                        return true;
                    })).toBeTruthy();
                });

                it('The number of constructor variants corresponds to the number of constructors', () => {
                    expect(jsonData.nodes.every(n => {
                        if ((typeof n.constructorVariants !== 'undefined') && (typeof n.constructors !== 'undefined')) {
                            var nbVariants = n.constructorVariants;
                            if (nbVariants === 0 || nbVariants === 1) {
                                return n.constructors.length === 0 || n.constructors[0].number === 1;
                            } else {
                                return nbVariants === n.constructors[0].number
                            }
                        }
                        return true;
                    })).toBeTruthy();
                });

                it('The number of constructors in the constructors list is at most one', () => {
                    expect(jsonData.nodes.every(n => n.constructors.length < 2)).toBeTruthy();
                });

            });

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

function setStorageValuestoMockVariantsDisplaying() {
    sessionStorage.setItem("firstTime", "false");
    sessionStorage.setItem("filteredIsolated", "false");
    sessionStorage.setItem("filteredVariants", "false");
}