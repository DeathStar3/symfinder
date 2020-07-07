//
// Created by ThePrecisionBro on 26/11/2019.
//

#ifndef CPP_STRATEGY_ARCHETYPE_SORTSTRATEGY_H
#define CPP_STRATEGY_ARCHETYPE_SORTSTRATEGY_H

#include <vector>

namespace Sorting {
    class ISortStrategy {
    public:
        virtual void sort(std::vector<int> &vec) = 0;
    };

    class Sorter {
    public:
        Sorting::ISortStrategy *strategy{};

        virtual void sort(std::vector<int> &vec) { strategy->sort(vec); }
    };

    class CountingSorter : Sorter {
    public:
        void sort(std::vector<int> &vec) override {
            Sorter::sort(vec);
            this->count += 1;
        }

        int count = 0;
    };
}

#endif //CPP_STRATEGY_ARCHETYPE_SORTSTRATEGY_H

