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

import {NodesFilter} from "./nodes-filter.js";

class PackageColorer extends NodesFilter {

    constructor(filterButtonSelector, filterInputSelector, filtersListSelector, nodeFilters, displayGraphFunction) {
        super(filterButtonSelector, filterInputSelector, filtersListSelector, nodeFilters, displayGraphFunction);
        this.packagesMap = new Map();

        this.addValue = (n) => {
            this.packagesMap.set(n, this.getNewColor().toString());
        };

        this.removeValue = (n) => {
            this.packagesMap.delete(n);
        };

        this.getFilterItem = (filter) => {
            return '' +
                '<li class="list-group-item d-flex justify-content-between align-items-center" id="' + filter + '" data-toggle="list"\n' +
                '               role="tab" aria-controls="profile" style="background-color: ' + this.packagesMap.get(filter) + '">'
                + filter +
                '<button type="button btn-dark" class="close" aria-label="Close">\n' +
                '  <span aria-hidden="true">&times;</span>\n' +
                '</button>' +
                '</li>';

        };
    }

    /**
     * Inspired by https://stackoverflow.com/questions/43193341/how-to-generate-random-pastel-or-brighter-color-in-javascript
     * @returns {string}
     */
    getNewColor() {
        return "hsl(" + 360 * Math.random() + ',' + 100 + '%,' + 50 + '%)'
    }

    getColorForName(name) {
        var colorFromMap = "";
        var colorFilter = "";
        this.packagesMap.forEach((value, key) => {
            if (name.startsWith(key) && key.length >= colorFilter.length) {
                colorFilter = key;
                colorFromMap = value;
            }
        });
        if (colorFromMap === "") {
            return "#FF0000";
        } else {
            return colorFromMap.toString();
        }
    }

}

export {PackageColorer};