//
// Created by nathan on 20/11/2019.
//

#ifndef SYMFINDER_CPP_EXAMPLES_INTEGER_CPP
#define SYMFINDER_CPP_EXAMPLES_INTEGER_CPP

#include "Value.cpp"

class Integer : public Value<int> {
public:
    explicit Integer(int integer) : Value(integer) {}

    void increment() override {
        value_ += 1;
    }
};


#endif //SYMFINDER_CPP_EXAMPLES_INTEGER_CPP
