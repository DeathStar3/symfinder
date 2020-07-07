//
// Created by nathan on 17/10/2019.
//

#ifndef SYMFINDER_CPP_EXAMPLES_RECTANGLE_H
#define SYMFINDER_CPP_EXAMPLES_RECTANGLE_H

#include "Shape.h"

class Rectangle : public Shape {
public:
    // default constructor
    Rectangle() : width_{0.0}, length_{0.0} {}

    Rectangle(double width, double length) : width_{width}, length_{length} {}

    // destructor
    ~Rectangle() override = default;

    virtual double area() override;
    virtual double perimeter() override;
    void draw(int, int);
private:
    const double width_, length_;
};


#endif //SYMFINDER_CPP_EXAMPLES_RECTANGLE_H
