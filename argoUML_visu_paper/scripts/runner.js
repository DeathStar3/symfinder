import {Graph} from "./graph.js";

var visualization;

async function display(jsonFile, jsonStatsFile) {
    let filters = [];
    console.log("1");
    visualization = new Graph(jsonFile, jsonStatsFile, filters);
    console.log(visualization);
    await visualization.displayGraph();

}

export {display, visualization}