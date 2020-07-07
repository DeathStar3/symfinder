//
// Created by nathan on 02/12/2019.
//

#include <climits>
#include <iostream>

template<class T>
class Value {
public:
    unsigned long count() {
        return INT_MAX;
    }
};

template<>
class Value<bool> {
public:
    unsigned long count() {
        return 2;
    }
};

int main() {
    Value<int> value1;
    Value<bool> value2;

    std::cout << value1.count() << std::endl;
    std::cout << value2.count() << std::endl;

    return 0;
}
