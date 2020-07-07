//
// Created by nathan on 17/10/2019.
//

#ifndef SYMFINDER_CPP_EXAMPLES_SHAPE_CPP
#define SYMFINDER_CPP_EXAMPLES_SHAPE_CPP

namespace NoDimension{
    namespace Tools{
        class Shape {
        public:
            virtual ~Shape() = default;

            virtual double area() = 0;

            virtual double perimeter() = 0;
        };
    }

}



#endif //SYMFINDER_CPP_EXAMPLES_SHAPE_CPP
