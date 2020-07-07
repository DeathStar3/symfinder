//
// Created by nathan on 20/11/2019.
//

#ifndef SYMFINDER_CPP_EXAMPLES_VALUE_CPP
#define SYMFINDER_CPP_EXAMPLES_VALUE_CPP

template <typename T>
class Value {
public:
    explicit Value(T value) : value_{value} {}

    T get() {
        return value_;
    }

    virtual void increment() = 0;
    void foo();

protected:
    T value_;
};


#endif //SYMFINDER_CPP_EXAMPLES_VALUE_CPP
