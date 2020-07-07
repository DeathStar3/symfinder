#include <iostream>
#include "Rectangle.cpp"
#include "Circle.cpp"

int main() {
    std::cout << "Hello, World!" << std::endl;

    Rectangle* r = new Rectangle(50.0, 100.0);

    std::cout << r->perimeter() << std::endl;
    std::cout << r->area() << std::endl;
    r->draw(2, 5);

    Circle* c = new Circle(10);

    std::cout << c->perimeter() << std::endl;
    std::cout << c->area() << std::endl;

    return 0;
}