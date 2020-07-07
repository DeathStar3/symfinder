//
// Created by nathan on 17/10/2019.
//

#ifndef SYMFINDER_CPP_EXAMPLES_RECTANGLE_CPP
#define SYMFINDER_CPP_EXAMPLES_RECTANGLE_CPP

#include <iostream>
#include "../Shape/Shape.cpp"
class Rectangle : public NoDimension::Tools::Shape {
public:
    // default constructor
    Rectangle() : width_{0.0}, length_{0.0} {}

    Rectangle(double width, double length) : width_{width}, length_{length} {}

    // destructor
    ~Rectangle() override = default;

    double area() override {
        return width_ * length_;
    }

    double perimeter() override {
        return 2 * (width_ + length_);
    }

    void draw(int x, int y) {
        std::cout << "Drawing at " << x << ", " << y << std::endl;
    }
private:
    const double width_, length_;
};


#endif //SYMFINDER_CPP_EXAMPLES_RECTANGLE_CPP
