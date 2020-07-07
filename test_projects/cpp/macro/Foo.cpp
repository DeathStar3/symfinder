//
// Created by nathan on 26/11/2019.
//

#ifndef SHAPE_H_FOO_H
#define SHAPE_H_FOO_H

#define MACRO1 void foo1() {}
#define MACRO2 void foo2
#define MACRO3 void foo3()
#define MACRO4(integer) const int foo = integer;
#define MACRO5 std::cout << "Macro defined in Foo class" << std::endl;
#define MACRO(num) if (num & 1) {\
               std::cout << "Odd" << std::endl;\
           } else {\
               std::cout << "Even" << std::endl;\
           }

#define FOO(a) static_assert(a, "a")
#define BAR() FOO(true);

#ifdef DEBUG
#  define MOZ_ASSERT(...) MOZ_RELEASE_ASSERT(__VA_ARGS__)
#else
#  define MOZ_ASSERT(...) while(0) {}
#endif /* DEBUG */

#define FOOBAR FOO("FOOBAR");
#define comment /* there is a comment here */
#define comment2 /* there is a comment2 here */ // another one

#include <iostream>
#include <cassert>
#  define MOZ_ASSERTE(...) MOZ_RELEASE_ASSERT(__VA_ARGS__)
#define MOZ_RELEASE_ASSERT(arg)

class Foo {
public:
    Foo() {
        MACRO2();
        MACRO3;
        MACRO(12)

        FOO(true);
        BAR()
        macro();
    }

    MACRO1

    MACRO4(42)

    void macro() {
        MOZ_ASSERT(3+4);
        comment
        FOOBAR
        MOZ_ASSERTE()
        MOZ_ASSERTE(true)
    }
};


#endif //SHAPE_H_FOO_H
