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

import {NodesFilter} from "./nodes-filter.js";
import {PackageColorer} from "./package-colorer.js";
import {VariantsFilter} from "./variants-filter.js";
import {IsolatedFilter} from "./isolated-filter.js";

class Graph {
//	data stores
    graph;
    store;

    width;
    height;

    filterIsolated;
    filterVariants;

    jsonFile;
    jsonStatsFile;


    filter;
    packageColorer;

    svg; g; link; node; label;

    //	d3 color scales
    color;

    constructor(jsonFile, jsonStatsFile, nodeFilters) {
        this.jsonFile = jsonFile;
        this.jsonStatsFile = jsonStatsFile;
        this.filter = new NodesFilter("#add-filter-button", "#package-to-filter", "#list-tab", nodeFilters, async () => await this.displayGraph());
        this.packageColorer = new PackageColorer("#add-package-button", "#package-to-color", "#color-tab", [], async () => await this.displayGraph());
        if(sessionStorage.getItem("firstTime") === null){
            sessionStorage.setItem("firstTime", "true");
        }
        this.color = d3.scaleLinear();
        this.setButtonsClickActions();
    }


    async displayGraph() {
        if (sessionStorage.getItem("firstTime") === "true") {
            sessionStorage.setItem("filteredIsolated", "false");
            sessionStorage.setItem("filteredVariants", "true");
            sessionStorage.setItem("firstTime", "false");
        }
        d3.selectAll("svg > *").remove();
        this.filterIsolated = sessionStorage.getItem("filteredIsolated") === "true";
        this.filterVariants = sessionStorage.getItem("filteredVariants") === "true";
        await this.generateGraph();
        return this.graph;
    }

    async generateGraph() {

        this.width = window.innerWidth;
        this.height = window.innerHeight - 10;

        this.generateStructure(this.width, this.height);

        await this.getData(this);

    }

    generateStructure(width, height) {
        //	svg selection and sizing
        this.svg = d3.select("svg").attr("width", width).attr("height", height);

        this.svg.append('defs').append('marker')
            .attr('id', 'arrowhead')
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", -5)
            .attr("refY", 0)
            .attr("markerWidth", 4)
            .attr("markerHeight", 4)
            .attr("orient", "auto")
            .append("path")
            .attr("d", "M0,0L10,-5L10,5")
            .attr('fill', 'gray')
            .style('stroke', 'none');


        //add encompassing group for the zoom
        this.g = this.svg.append("g")
            .attr("class", "everything");

        this.link = this.g.append("g").selectAll(".link");
        this.node = this.g.append("g").selectAll(".node");
        this.label = this.g.append("g").selectAll(".label");
    }

    async getData(graph) {
        return new Promise((resolve, reject) => {
            d3.queue()
                .defer(d3.json, graph.jsonFile)
                .defer(d3.json, graph.jsonStatsFile)
                .await((err, gr, stats) => {
                    if (err) throw err;
                    graph.displayData(gr, stats);
                    graph.update();
                    resolve();
                });
        });

    }

    displayData(gr, stats) {
        //	data read and store

        document.getElementById("statistics").innerHTML =
            // "Number of VPs: " + stats["VPs"] + "<br>" +
            // "Number of methods VPs: " + stats["methodVPs"] + "<br>" +
            // "Number of constructors VPs: " + stats["constructorsVPs"] + "<br>" +
            "Number of class level VPs: " + stats["classLevelVPs"] + "<br>" +
            "Number of method level VPs: " + stats["methodLevelVPs"] + "<br>" +
            // "Number of variants: " + stats["variants"] + "<br>" +
            // "Number of methods variants: " + stats["methodsVariants"] + "<br>" +
            // "Number of constructors variants: " + stats["constructorsVariants"] + "<br>" +
            "Number of class level variants: " + stats["classLevelVariants"] + "<br>" +
            "Number of method level variants: " + stats["methodLevelVariants"];

        var sort = gr.nodes.filter(a => a.types.includes("CLASS")).map(a => parseInt(a.constructorVariants)).sort((a, b) => a - b);
        this.color.domain([sort[0] - 3, sort[sort.length - 1]]); // TODO deal with magic number

        var nodeByID = {};

        this.graph = gr;
        this.store = $.extend(true, {}, gr);

        this.graph.nodes.forEach(function (n) {
            n.radius = n.types.includes("CLASS") ? 10 + n.methodVPs : 10;
            nodeByID[n.name] = n;
        });

        this.graph.links.forEach(function (l) {
            l.sourceTypes = nodeByID[l.source].types;
            l.targetTypes = nodeByID[l.target].types;
        });

        this.store.nodes.forEach(function (n) {
            n.radius = n.types.includes("CLASS") ? 10 + n.methodVPs : 10;
        });

        this.store.links.forEach(function (l) {
            l.sourceTypes = nodeByID[l.source].types;
            l.targetTypes = nodeByID[l.target].types;
        });


        this.graph.nodes = this.filter.getNodesListWithoutMatchingFilter(gr.nodes);
        this.graph.links = this.filter.getLinksListWithoutMatchingFilter(gr.links);

        if (this.filterVariants) {
            var variantsFilter = new VariantsFilter(this.graph.nodes, this.graph.links);
            this.graph.nodes = variantsFilter.getFilteredNodesList();
            this.graph.links = variantsFilter.getFilteredLinksList();
        }

        if (this.filterIsolated) {
            var isolatedFilter = new IsolatedFilter(this.graph.nodes, this.graph.links);
            this.graph.nodes = isolatedFilter.getFilteredNodesList();
        }

    }


    //	general update pattern for updating the graph
    update() {

        //	UPDATE
        this.node = this.node.data(this.graph.nodes, function (d) {
            return d.name;
        });
        //	EXIT
        this.node.exit().remove();
        //	ENTER
        var newNode = this.node.enter().append("circle")
            .attr("class", "node")
            .style("stroke-dasharray", function (d) {
                return d.types.includes("ABSTRACT") ? "3,3" : "3,0"
            })
            .style("stroke", "black")
            .style("stroke-width", function (d) {
                return d.types.includes("ABSTRACT") ? d.classVariants + 1 : d.classVariants;
            })
            .attr("r", function (d) {
                return d.radius
            })
            .attr("fill", (d) => {
                return d.types.includes("INTERFACE") ? d3.rgb(0, 0, 0) : d3.rgb(this.getNodeColor(d.name, d.constructorVariants))
            })
            .attr("name", function (d) {
                return d.name
            });

        newNode.append("title").text(function (d) {
            return "types: " + d.types + "\n" + "name: " + d.name;
        });

        //	ENTER + UPDATE
        this.node = this.node.merge(newNode);

        //	UPDATE
        this.link = this.link.data(this.graph.links, function (d) {
            return d.name;
        });
        //	EXIT
        this.link.exit().remove();
        //	ENTER
        var newLink = this.link.enter().append("line")
            .attr("stroke-width", 1)
            .attr("class", "link")
            .attr("source", d => d.source)
            .attr("target", d => d.target)
            .attr('marker-start', "url(#arrowhead)")
            .style("pointer-events", "none");

        newLink.append("title")
            .text(function (d) {
                return "source: " + d.source + "\n" + "target: " + d.target;
            });
        //	ENTER + UPDATE
        this.link = this.link.merge(newLink);

        //  UPDATE
        this.label = this.label.data(this.graph.nodes, function (d) {
            return d.name;
        });
        //	EXIT
        this.label.exit().remove();
        //  ENTER
        var newLabel = this.label.enter().append("text")
            .attr("dx", -5)
            .attr("dy", ".35em")
            .attr("name", d => d.name)
            .attr("fill", (d) => {
                var nodeColor = d.types.includes("INTERFACE") ? d3.rgb(0, 0, 0) : d3.rgb(this.getNodeColor(d.name, d.constructorVariants));
                return contrastColor(nodeColor);
            })
            .text(function (d) {
                return ["STRATEGY", "FACTORY", "TEMPLATE", "DECORATOR"].filter(p => d.types.includes(p)).map(p => p[0]).join(", ");
            });

        //	ENTER + UPDATE
        this.label = this.label.merge(newLabel);

        // d3.selectAll("circle.node").on("click", () => {
        //     this.filter.addFilter(d3.select(this).attr("name"), );
        // });

        this.addAdvancedBehaviour(newNode, this.width, this.height);
    }

    addAdvancedBehaviour(newNode, width, height) {
        newNode.call(d3.drag()
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended)
        );

        //	force simulation initialization
        var simulation = d3.forceSimulation()
            .force("link", d3.forceLink().distance(100)
                .id(function (d) {
                    return d.name;
                }))
            .force("charge", d3.forceManyBody()
                .strength(function (d) {
                    return -50;
                }))
            .force("center", d3.forceCenter(width / 2, height / 2));


        //	update simulation nodes, links, and alpha
        simulation
            .nodes(this.graph.nodes)
            //	tick event handler with bounded box
            .on("tick", () => {
                this.node
                // .attr("cx", function(d) { return d.x = Math.max(radius, Math.min(width - radius, d.x)); })
                // .attr("cy", function(d) { return d.y = Math.max(radius, Math.min(height - radius, d.y)); });
                    .attr("cx", function (d) {
                        return d.x;
                    })
                    .attr("cy", function (d) {
                        return d.y;
                    });

                this.link
                    .attr("x1", function (d) {
                        return d.source.x;
                    })
                    .attr("y1", function (d) {
                        return d.source.y;
                    })
                    .attr("x2", function (d) {
                        return d.target.x;
                    })
                    .attr("y2", function (d) {
                        return d.target.y;
                    });

                this.label
                    .attr("x", function (d) {
                        return d.x;
                    })
                    .attr("y", function (d) {
                        return d.y;
                    });
            });

        simulation.force("link")
            .links(this.graph.links);

        simulation.alpha(1).alphaTarget(0).restart();

        //add zoom capabilities
        var zoom_handler = d3.zoom()
            .on("zoom", () => this.g.attr("transform", d3.event.transform));

        zoom_handler(this.svg);

        //	drag event handlers
        function dragstarted(d) {
            if (!d3.event.active) simulation.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
        }

        function dragged(d) {
            d.fx = d3.event.x;
            d.fy = d3.event.y;
        }

        function dragended(d) {
            if (!d3.event.active) simulation.alphaTarget(0);
            d.fx = null;
            d.fy = null;
        }
    }

    getNodeColor(nodeName, valueOnScale){
        var upperRangeColor = this.packageColorer.getColorForName(nodeName);
        return this.color
            .range(["#FFFFFF", upperRangeColor])
            .interpolate(d3.interpolateRgb)(valueOnScale);
    }

    setButtonsClickActions(){
        $(document).on('click', ".list-group-item", e => {
            e.preventDefault();
            $('.active').removeClass('active');
        });

        $("#filter-isolated").on('click', async e => {
            e.preventDefault();
            var previouslyFiltered = sessionStorage.getItem("filteredIsolated") === "true";
            sessionStorage.setItem("filteredIsolated", previouslyFiltered ? "false" : "true");
            $("#filter-isolated").text(previouslyFiltered ? "Unfilter isolated nodes" : "Filter isolated nodes");
            await this.displayGraph();
        });

        $("#filter-variants-button").on('click', async e => {
            e.preventDefault();
            console.log(sessionStorage.getItem("filteredVariants"));
            var previouslyFiltered = sessionStorage.getItem("filteredVariants") === "true";
            sessionStorage.setItem("filteredVariants", previouslyFiltered ? "false" : "true");
            $("#filter-variants-button").text(previouslyFiltered ? "Hide variants" : "Show variants");
            await this.displayGraph();
        });

        $('#hide-info-button').click(function () {
            $(this).text(function (i, old) {
                return old === 'Show project information' ? 'Hide project information' : 'Show project information';
            });
        });

        $('#hide-legend-button').click(function () {
            $(this).text(function (i, old) {
                return old === 'Hide legend' ? 'Show legend' : 'Hide legend';
            });
        });
    }

}

function contrastColor(color) {
    var d = 0;

    // Counting the perceptive luminance - human eye favors green color...
    const luminance = (0.299 * color.r + 0.587 * color.g + 0.114 * color.b) / 255;

    if (luminance > 0.5)
        d = 0; // bright colors - black font
    else
        d = 255; // dark colors - white font

    return d3.rgb(d, d, d);
}

export { Graph };