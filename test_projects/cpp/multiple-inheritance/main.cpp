//
// Created by nathan on 27/11/2019.
//

#include <malloc.h>
#include "basic-inheritance/Animal.h"
#include "basic-inheritance/Cow.h"
#include "basic-inheritance/Tiger.h"
#include "diamond-inheritance/Rhombus.h"
#include "diamond-inheritance/Rectangle.h"
#include "diamond-inheritance/Square.h"

int main() {
    Animal* cow = new Cow();
    Animal* tiger = new Tiger();

    Shape* rhombus = new Rhombus();
    Shape* rectangle = new Rectangle();

    Shape* square = new Square();
    Rectangle* square1 = new Square();
    Rhombus* square2 = new Square();

    free(cow);
    free(tiger);
    free(rhombus);
    free(rectangle);
    free(square);
    free(square1);
    free(square2);

    return 0;
}