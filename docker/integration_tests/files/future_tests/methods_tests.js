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

xdescribe("Testing JSON output for vps_and_variants", () => {

    var jsonData, jsonStatsData;

    beforeEach(async (done) => {
        const [graph, stats] = await getJsonData("tests/data/vps_and_variants.json", "tests/data/vps_and_variants-stats.json");
        jsonData = graph;
        jsonStatsData = stats;
        done();
    });

    it('there should be 6 nodes', () => {
        expect(jsonData.nodes.length).toBe(6);
    });

    describe('OneConstructorOneMethod', () => {

        var node;

        beforeEach(() => {
            node = getNodeWithName(jsonData, "OneConstructorOneMethod");
        });

        it('there should be one method in the methods list', () => {
            expect(node.methods.length).toBe(1);
        });
        it('the method name is method1', () => {
            expect(node.methods[0].name).toBe("method1");
        });
        it('the method appears one time', () => {
            expect(node.methods[0].number).toBe(1);
        });
        it('there should be one constructor in the constructors list', () => {
            expect(node.constructors.length).toBe(1);
        });
        it('the constructor name is OneConstructorOneMethod', () => {
            expect(node.constructors[0].name).toBe("OneConstructorOneMethod");
        });
        it('the constructor appears one time', () => {
            expect(node.constructors[0].number).toBe(1);
        });

    })

    describe('TwoConstructorsOneMethod', () => {

        var node;

        beforeEach(() => {
            node = getNodeWithName(jsonData, "TwoConstructorsOneMethod");
        });

        it('there should be one method in the methods list', () => {
            expect(node.methods.length).toBe(1);
        });
        it('the method name is method1', () => {
            expect(node.methods[0].name).toBe("method1");
        });
        it('the method appears one time', () => {
            expect(node.methods[0].number).toBe(1);
        });
        it('there should be one constructor in the constructors list', () => {
            expect(node.constructors.length).toBe(1);
        });
        it('the constructor name is TwoConstructorsOneMethod', () => {
            expect(node.constructors[0].name).toBe("TwoConstructorsOneMethod");
        });
        it('the constructor appears two times', () => {
            expect(node.constructors[0].number).toBe(2);
        });

    })

    describe('OneConstructorTwoMethods', () => {

        var node;

        beforeEach(() => {
            node = getNodeWithName(jsonData, "OneConstructorTwoMethods");
        });

        it('there should be one method in the methods list', () => {
            expect(node.methods.length).toBe(1);
        });
        it('the method name is method1', () => {
            expect(node.methods[0].name).toBe("method1");
        });
        it('the method appears two times', () => {
            expect(node.methods[0].number).toBe(2);
        });
        it('there should be one constructor in the constructors list', () => {
            expect(node.constructors.length).toBe(1);
        });
        it('the constructor name is OneConstructorTwoMethods', () => {
            expect(node.constructors[0].name).toBe("OneConstructorTwoMethods");
        });
        it('the constructor appears one time', () => {
            expect(node.constructors[0].number).toBe(1);
        });

    })

    describe('TwoConstructorsTwoMethods', () => {

        var node;

        beforeEach(() => {
            node = getNodeWithName(jsonData, "TwoConstructorsTwoMethods");
        });

        it('there should be one method in the methods list', () => {
            expect(node.methods.length).toBe(1);
        });
        it('the method name is method1', () => {
            expect(node.methods[0].name).toBe("method1");
        });
        it('the method appears one time', () => {
            expect(node.methods[0].number).toBe(2);
        });
        it('there should be one constructor in the constructors list', () => {
            expect(node.constructors.length).toBe(1);
        });
        it('the constructor name is TwoConstructorsTwoMethods', () => {
            expect(node.constructors[0].name).toBe("TwoConstructorsTwoMethods");
        });
        it('the constructor appears one time', () => {
            expect(node.constructors[0].number).toBe(2);
        });

    })

    describe('TwoConstructorsNoMethod', () => {

        var node;

        beforeEach(() => {
            node = getNodeWithName(jsonData, "TwoConstructorsNoMethod");
        });

        it('there should be no method in the methods list', () => {
            expect(node.methods.length).toBe(0);
        });
        it('there should be one constructor in the constructors list', () => {
            expect(node.constructors.length).toBe(1);
        });
        it('the constructor name is TwoConstructorsNoMethod', () => {
            expect(node.constructors[0].name).toBe("TwoConstructorsNoMethod");
        });
        it('the constructor appears one time', () => {
            expect(node.constructors[0].number).toBe(2);
        });

    })

    describe('NoConstructorTwoMethods', () => {

        var node;

        beforeEach(() => {
            node = getNodeWithName(jsonData, "NoConstructorTwoMethods");
        });

        it('there should be one method in the methods list', () => {
            expect(node.methods.length).toBe(1);
        });
        it('the method name is method1', () => {
            expect(node.methods[0].name).toBe("method1");
        });
        it('the method appears one time', () => {
            expect(node.methods[0].number).toBe(2);
        });
        it('there should be no constructor in the constructors list', () => {
            expect(node.constructors.length).toBe(0);
        });

    })

});

xdescribe("Testing JSON output for multiple_vp", () => {

    var jsonData, jsonStatsData;

    beforeAll(async (done) => {
        const [graph, stats] = await getJsonData("tests/data/multiple_vp.json", "tests/data/multiple_vp-stats.json");
        jsonData = graph;
        jsonStatsData = stats;
        done();
    });

    it('there should be 1 node', () => {
        expect(jsonData.nodes.length).toBe(1);
    });
    it('there should be 1 method in the methods list', () => {
        expect(jsonData.nodes[0].methods.length).toBe(1);
    });
    it('the method name is method1', () => {
        expect(jsonData.nodes[0].methods[0].name).toBe("method1");
    });
    it('the method appears two times', () => {
        expect(jsonData.nodes[0].methods[0].number).toBe(2);
    });
    it('there should be 1 constructor in the constructors list', () => {
        expect(jsonData.nodes[0].constructors.length).toBe(1);
    });
    it('the constructor name is MultipleVPClass', () => {
        expect(jsonData.nodes[0].constructors[0].name).toBe("MultipleVPClass");
    });
    it('the constructor appears two times', () => {
        expect(jsonData.nodes[0].constructors[0].number).toBe(2);
    });
});