//
// Created by nathan on 17/10/2019.
//

#include "Circle.h"
#include <cmath>

double Circle::area() {
    return M_PI * pow(radius_, 2);
}

double Circle::perimeter() {
    return 2 * M_PI * radius_;
}
