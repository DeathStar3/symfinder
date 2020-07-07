//
// Created by nathan on 20/11/2019.
//

#ifndef SYMFINDER_CPP_EXAMPLES_LONG_CPP
#define SYMFINDER_CPP_EXAMPLES_LONG_CPP

#include "Value.cpp"

class Long : public Value<long> {
public:
    explicit Long(long value) : Value(value) {}

    void increment() override {
        value_ += 1;
    }
};


#endif //SYMFINDER_CPP_EXAMPLES_INTEGER_CPP
