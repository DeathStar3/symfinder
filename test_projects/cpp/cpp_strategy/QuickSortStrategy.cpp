//
// Created by ThePrecisionBro on 26/11/2019.
//


#ifndef QUICK_SORT
#define QUICK_SORT

#include <vector>
#include <iostream>
#include <functional>
#include "SortStrategy.cpp"

namespace QuickSort {
    class QuickSortStrategy : public Sorting::ISortStrategy {
    public:
        void sort(std::vector<int> &vec) override {
            std::cout << "Sorting using quick sort" << std::endl;
            _QuickSort(vec);
        }

    private:
        void _QuickSort(std::vector<int> &vec) {
            if (vec.empty()) return;
            using size = std::vector<int>::size_type;
            auto partition = [&vec](size low, size high) {
                int pivot = vec[high];
                size i = low;
                for (size j = low; j != high; ++j)
                    if (vec[j] <= pivot)
                        std::swap(vec[i++], vec[j]);
                std::swap(vec[i], vec[high]);
                return i;
            };

            std::function<void(size, size)> quickSort = [&](size low, size high) {
                if (low >= high) return;
                size pi = partition(low, high);
                quickSort(low, pi - 1);
                quickSort(pi + 1, high);
            };

            quickSort(0, vec.size() - 1);
        }
    };
}
#endif
