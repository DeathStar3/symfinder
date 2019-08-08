/*
This file is part of symfinder.

symfinder is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

symfinder is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with symfinder.  If not, see <http://www.gnu.org/licenses/>.

Copyright 2018-2019 Johann Mortara <johann.mortara@univ-cotedazur.fr>
Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>
*/

//	data stores
var graph, store;

//	filtered types
var filters = [];
var filterIsolated = false;
var jsonFile, jsonStatsFile;

var firstTime = true;

function getFilterItem(filter) {
    return '' +
        '<li class="list-group-item d-flex justify-content-between align-items-center" id="' + filter + '" data-toggle="list"\n' +
        '               role="tab" aria-controls="profile">'
        + filter +
        '<button type="button btn-dark" class="close" aria-label="Close">\n' +
        '  <span aria-hidden="true">&times;</span>\n' +
        '</button>' +
        '</li>';
}

function displayGraph(jsonFile, jsonStatsFile, nodefilters = [], filterIsolated = false) {

    d3.selectAll("svg > *").remove();
    filters = nodefilters;
    this.jsonFile = jsonFile;
    this.jsonStatsFile = jsonStatsFile;
    this.filterIsolated = filterIsolated;
    if (firstTime) {
        filters.forEach(filter => {
            $("#list-tab").append(getFilterItem(filter));
        });
        firstTime = false;
    }

    generateGraph();
}

function generateGraph() {

    var width = window.innerWidth,
        height = window.innerHeight - 10;


    //	svg selection and sizing
    var svg = d3.select("svg").attr("width", width).attr("height", height);

    svg.append('defs').append('marker')
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

    //	d3 color scales
    var color = d3.scaleLinear()
        .range(["#FFFFFF", '#FF0000'])
        .interpolate(d3.interpolateRgb);

    //add encompassing group for the zoom
    var g = svg.append("g")
        .attr("class", "everything");

    var link = g.append("g").selectAll(".link"),
        node = g.append("g").selectAll(".node"),
        label = g.append("g").selectAll(".label");

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

    function displayData() {
        //	data read and store
        d3.json(jsonFile, function (err, gr) {

            d3.json(jsonStatsFile, function (err, stats) {
                var statisticsContent =
                    "Number of methods VPs: " + stats["methodsVPs"] + "<br>" +
                    "Number of constructors VPs: " + stats["constructorsVPs"] + "<br>" +
                    "Number of method level VPs: " + stats["methodLevelVPs"] + "<br>" +
                    "Number of class level VPs: " + stats["classLevelVPs"] + "<br>" +
                    "Number of methods variants: " + stats["methodsVariants"] + "<br>" +
                    "Number of constructors variants: " + stats["constructorsVariants"] + "<br>" +
                    "Number of method level variants: " + stats["methodLevelVariants"] + "<br>" +
                    "Number of class level variants: " + stats["classLevelVariants"];
                document.getElementById("statistics").innerHTML = statisticsContent;

            });

            if (err) throw err;

            var sort = gr.nodes.filter(a => a.types.includes("CLASS")).map(a => parseInt(a.constructors)).sort((a, b) => a - b);
            color.domain([sort[0] - 3, sort[sort.length - 1]]); // TODO deal with magic number

            var nodeByID = {};


            graph = gr;
            store = $.extend(true, {}, gr);

            graph.nodes.forEach(function (n) {
                n.radius = n.types.includes("CLASS") ? 10 + n.methods : 10;
                nodeByID[n.name] = n;
            });

            graph.links.forEach(function (l) {
                l.sourceTypes = nodeByID[l.source].types;
                l.targetTypes = nodeByID[l.target].types;
            });

            store.nodes.forEach(function (n) {
                n.radius = n.types.includes("CLASS") ? 10 + n.methods : 10;
            });

            store.links.forEach(function (l) {
                l.sourceTypes = nodeByID[l.source].types;
                l.targetTypes = nodeByID[l.target].types;
            });

            graph.nodes = gr.nodes.filter(n => !filters.some(filter => n.name.startsWith(filter)));
            graph.links = gr.links.filter(l => !filters.some(filter => l.source.startsWith(filter)) && !filters.some(filter => l.target.startsWith(filter)));

            if (filterIsolated) {
                var nodesToKeep = new Set();
                graph.links.forEach(l => {
                    nodesToKeep.add(l.source);
                    nodesToKeep.add(l.target);
                });
                graph.nodes = gr.nodes.filter(n => nodesToKeep.has(n.name));
            }
            update();
        });
    }


    //	general update pattern for updating the graph
    function update() {
        //	UPDATE
        let dataSource = graph;
        node = node.data(dataSource.nodes, function (d) {
            return d.name;
        });
        //	EXIT
        node.exit().remove();
        //	ENTER
        var newNode = node.enter().append("g").append("circle")
            .attr("class", "node")
            .style("stroke-dasharray", function (d) {
                return d.types.includes("ABSTRACT") ? "3,3" : "3,0"
            })
            .style("stroke", "black")
            .style("stroke-width", function (d) {
                return d.nbVariants
            })
            .attr("r", function (d) {
                return d.radius
            })
            .attr("fill", function (d) {
                return d.types.includes("INTERFACE") ? d3.rgb(0, 0, 0) : d3.rgb(color(d.constructors))
            })
            .attr("name", function (d) {
                return d.name
            })
            .call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended)
            );

        //Zoom functions
        function zoom_actions() {
            g.attr("transform", d3.event.transform)
        }

        //add zoom capabilities
        var zoom_handler = d3.zoom()
            .on("zoom", zoom_actions);

        zoom_handler(svg);

        newNode.append("title").text(function (d) {
            return "types: " + d.types + "\n" + "name: " + d.name;
        });

        //	ENTER + UPDATE
        node = node.merge(newNode);

        //	UPDATE
        link = link.data(dataSource.links, function (d) {
            return d.name;
        });
        //	EXIT
        link.exit().remove();
        //	ENTER
        newLink = link.enter().append("line")
            .attr("stroke-width", 1)
            .attr("class", "link")
            .attr('marker-start', "url(#arrowhead)")
            .style("pointer-events", "none");

        newLink.append("title")
            .text(function (d) {
                return "source: " + d.source + "\n" + "target: " + d.target;
            });
        //	ENTER + UPDATE
        link = link.merge(newLink);

        //  UPDATE
        label = label.data(dataSource.nodes, function (d) {
            return d.name;
        });
        //	EXIT
        label.exit().remove();
        //  ENTER
        var newLabel = label.enter().append("g").append("text")
            .attr("dx", -5)
            .attr("dy", ".35em")
            .attr("fill", function (d) {
                var nodeColor = d.types.includes("INTERFACE") ? d3.rgb(0, 0, 0) : d3.rgb(color(d.constructors));
                return contrastColor(nodeColor);
            })
            .text(function (d) {
                return ["STRATEGY", "FACTORY"].filter(p => d.types.includes(p)).map(p => p[0]).join(", ");
            });

        //	ENTER + UPDATE
        label = label.merge(newLabel);

        d3.selectAll("circle.node").on("click", function () {
            addFilter(d3.select(this).attr("name"));
        });

        //	update simulation nodes, links, and alpha
        simulation
            .nodes(dataSource.nodes)
            .on("tick", ticked);

        simulation.force("link")
            .links(dataSource.links);

        simulation.alpha(1).alphaTarget(0).restart();
    }

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

    //	tick event handler with bounded box
    function ticked() {
        node
        // .attr("cx", function(d) { return d.x = Math.max(radius, Math.min(width - radius, d.x)); })
        // .attr("cy", function(d) { return d.y = Math.max(radius, Math.min(height - radius, d.y)); });
            .attr("cx", function (d) {
                return d.x;
            })
            .attr("cy", function (d) {
                return d.y;
            });

        link
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

        label
            .attr("x", function (d) {
                return d.x;
            })
            .attr("y", function (d) {
                return d.y;
            });
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

    displayData();

}

function addFilter(value) {
    if (value) {
        $("#list-tab").append(getFilterItem(value));
        filters.push(value);
        displayGraph(jsonFile, jsonStatsFile, filters, filterIsolated);
    }
}

$(document).on('click', ".list-group-item", function (e) {
    e.preventDefault();
    $('.active').removeClass('active');
});

$("#add-filter-button").on('click', function (e) {
    e.preventDefault();
    let input = $("#package-to-filter");
    let inputValue = input.val();
    input.val("");
    addFilter(inputValue);
});

$("#filter-isolated").on('click', function (e) {
    e.preventDefault();
    var filtered = $(this).attr("aria-pressed") === "false";
    $(this).text(filtered ? "Unfilter isolated nodes" : "Filter isolated nodes");
    displayGraph(jsonFile, jsonStatsFile, filters, filtered);
});

$(document).on('click', ".close", function (e) {
    e.preventDefault();
    let removedFilter = $(e.target.parentElement.parentElement).attr("id");
    $(e.target.parentElement.parentElement).remove();
    filters.splice(filters.indexOf(removedFilter), 1);
    displayGraph(jsonFile, jsonStatsFile, filters, filterIsolated);
});

$('#hide-info-button').click(function(){
    $(this).text(function(i,old){
        return old === 'Show project information' ?  'Hide project information' : 'Show project information';
    });
});

$('#hide-legend-button').click(function(){
    $(this).text(function(i,old){
        return old === 'Hide legend' ?  'Show legend' : 'Hide legend';
    });
});