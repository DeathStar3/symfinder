//
// Created by nathan on 26/11/2019.
//

#define BAR

#ifdef BAR

#include "Foo.cpp"

class Bar : Foo {
    const int bar = 0;
};

#endif