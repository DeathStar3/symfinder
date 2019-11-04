/* Class allowing to easily create a logic allowing to process nodes whose name matches a pattern. */

class NodesFilter {

    constructor(filterButtonSelector, filterInputSelector, filtersListSelector, nodeFilters, displayGraphFunction) {
        this.filterButtonSelector = filterButtonSelector;
        this.filterInputSelector = filterInputSelector;
        this.filtersListSelector = filtersListSelector;
        this.displayGraphFunction = displayGraphFunction;
        this.addFilteringToButton();
        this.addCapacityToRemoveFilter();
        this.filtersList = [];
        nodeFilters.forEach(n => this.addFilter(n))
    }

    addFilteringToButton() {
        $(this.filterButtonSelector).on('click', async e => {
            e.preventDefault();
            let input = $(this.filterInputSelector);
            let inputValue = input.val();
            input.val("");
            await this.addFilter(inputValue);
        });
    }

    addCapacityToRemoveFilter(){
        $(document).on('click', ".close", async e => {
            e.preventDefault();
            let removedFilter = $(e.target.parentElement.parentElement).attr("id");
            $(e.target.parentElement.parentElement).remove();
            this.removeValue(removedFilter);
            await this.displayGraphFunction();
        });
    }
    async addFilter(value) {
        if (value) {
            this.addValue(value);
            $(this.filtersListSelector).append(this.getFilterItem(value));
            await this.displayGraphFunction();
        }
    }

    addValue(value) {
        this.filtersList.push(value);
    }

    removeValue(value) {
        this.filtersList.splice(this.filtersList.indexOf(value), 1);
    }

    getFilterItem(filter) {
        return '' +
            '<li class="list-group-item d-flex justify-content-between align-items-center" id="' + filter + '" data-toggle="list"\n' +
            '               role="tab" aria-controls="profile">'
            + filter +
            '<button type="button btn-dark" class="close" aria-label="Close">\n' +
            '  <span aria-hidden="true">&times;</span>\n' +
            '</button>' +
            '</li>';
    }

    appendFiltersToTab() {
        this.filtersList.forEach(filter => {
            $(this.filtersListSelector).append(this.getFilterItem(filter));
        });
    }

    /**
     * If the filter is a class filter (distinguished by the fact that it contains at least an uppercase letter),
     * we check that the class name matches the filter exactly.
     * Otherwise, the filter is a package filter, so we check that the class name starts with the filter.
     */
    static matchesFilter(name, filter) {
        var sp = filter.split(".");
        return /[A-Z]/.test(sp[sp.length - 1]) ? name === filter : name.startsWith(filter);
    }

    getNodesListWithoutMatchingFilter(listToFilter){
        return listToFilter.filter(n => !this.filtersList.some(filter => NodesFilter.matchesFilter(n.name, filter)))
    }

    getLinksListWithoutMatchingFilter(listToFilter){
        return listToFilter.filter(l => !this.filtersList.some(filter => NodesFilter.matchesFilter(l.source, filter)) && !this.filtersList.some(filter => NodesFilter.matchesFilter(l.target, filter)))
    }

}

export { NodesFilter };
