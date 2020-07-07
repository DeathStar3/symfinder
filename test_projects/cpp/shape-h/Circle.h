//
// Created by nathan on 17/10/2019.
//

#ifndef SYMFINDER_CPP_EXAMPLES_CIRCLE_H
#define SYMFINDER_CPP_EXAMPLES_CIRCLE_H


#include "Shape.h"

class Circle : public Shape {
public:
    Circle(double radius) : radius_{radius} {}

    double area() override;
    double perimeter() override;
private:
    const double radius_;
};


#endif //SYMFINDER_CPP_EXAMPLES_CIRCLE_H
