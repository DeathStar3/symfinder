//
// Created by ThePrecisionBro on 26/11/2019.
//
#include <vector>
#include <iostream>
#include <functional>
#include "SortStrategy.cpp"

namespace BubbleSort {
    class BubbleSortStrategy : public Sorting::ISortStrategy {
    public:
        void sort(std::vector<int> &vec) override {
            std::cout << "Sorting using bubble sort" << std::endl;
            _BubbleSort(vec);
        }

    private:
        void _BubbleSort(std::vector<int> &vec) {
            if (vec.empty()) return;
            using size = std::vector<int>::size_type;
            for (size i = 0; i != vec.size(); ++i)
                for (size j = 0; j != vec.size() - 1; ++j)
                    if (vec[j] > vec[j + 1])
                        std::swap(vec[j], vec[j + 1]);
        }
    };
}

