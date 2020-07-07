//
// Created by nathan on 17/10/2019.
//

#ifndef SYMFINDER_CPP_EXAMPLES_CIRCLE_CPP
#define SYMFINDER_CPP_EXAMPLES_CIRCLE_CPP


#include "../Shape/Shape.cpp"
#include <cmath>

namespace Dimension2 {
    class Circle : public NoDimension::Tools::Shape {
    public:
        Circle(double radius) : radius_{radius} {}

        Circle(int radius) : radius_{static_cast<double>(radius)} {}

        double area() override {
            return M_PI * pow(radius_, 2);
        }

        double perimeter() override {
            return 2 * M_PI * radius_;
        }

    private:
        const double radius_;
    };
}


#endif //SYMFINDER_CPP_EXAMPLES_CIRCLE_CPP
