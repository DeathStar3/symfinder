#include <iostream>
#include <cxxabi.h>
#include "dimension2/Rectangle.cpp"
#include "dimension2/Circle.cpp"
#include "dimension3/Circle.cpp"
using namespace Dimension2;
int main() {
    std::cout << "Hello, World!" << std::endl;

    Rectangle *r = new Rectangle(50.0, 100.0);

    std::cout << r->perimeter() << std::endl;
    std::cout << r->area() << std::endl;
    r->draw(2, 5);

    Dimension3::Circle *c = new Dimension3::Circle(10);
    Circle *circle2;
    std::cout << c->perimeter() << std::endl;
    std::cout << c->area() << std::endl;

    std::cout << "Getting the qualified names" << std::endl;

    int status;
    char *realname;

    // typeid
    const std::type_info &ti = typeid(c);
//
    realname = abi::__cxa_demangle(ti.name(), 0, 0, &status);
    std::cout << ti.name() << "\t=> " << realname << "\t: " << status << '\n';
    free(realname);
    const std::type_info &circle_info = typeid(circle2);
//
    realname = abi::__cxa_demangle(circle_info.name(), 0, 0, &status);
    std::cout << circle_info.name() << "\t=> " << realname << "\t: " << status << '\n';
    free(realname);


    return 0;
}

