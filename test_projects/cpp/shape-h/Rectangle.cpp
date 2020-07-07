//
// Created by nathan on 17/10/2019.
//

#include <iostream>
#include "Rectangle.h"

double Rectangle::area() {
    return width_ * length_;
}

void Rectangle::draw(int x, int y) {
    std::cout << "Drawing at " << x << ", " << y << std::endl;
}

double Rectangle::perimeter() {
    return 2 * (width_ + length_);
}

