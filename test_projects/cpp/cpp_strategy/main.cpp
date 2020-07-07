#include <vector>
#include <memory>
#include <iostream>
#include "QuickSortStrategy.cpp"
#include "BubbleSortStrategy.cpp"
#include "Professor.cpp"
int main()
{
    Sorting::Sorter sorter = Sorting::Sorter();
    std::vector<int> vec{};
    sorter.strategy = new BubbleSort::BubbleSortStrategy();
    sorter.sort(vec);
    for (int i : vec) std::cout << i << " ";
    std::cout << std::endl;
    free(sorter.strategy);

    sorter.strategy =  new QuickSort::QuickSortStrategy();
    sorter.sort(vec);
    for (int i : vec) std::cout << i << " ";
    std::cout << std::endl;
    free(sorter.strategy);
    Proffessor *proffessor = new PCollet();
    proffessor->teach();
    free(proffessor);
    proffessor = new JMortara();
    proffessor->teach();
    free(proffessor);

}