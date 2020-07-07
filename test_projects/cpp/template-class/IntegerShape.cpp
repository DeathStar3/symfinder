#include <iostream>
#include "ShapeTemplate.cpp"

class IntegerShape : public ShapeTemplate<int>
{
public:
    // default constructor
    IntegerShape() : width_{0}, length_{0}, dimension_{0} {}

    IntegerShape(int width, int length) : width_{width}, length_{length}, dimension_{0} {}

    // destructor
    ~IntegerShape() override = default;

    int area() override
    {
      return width_ * length_;
    }

    int perimeter() override
    {
      return 2 * (width_ + length_);
    }

    void draw(int x, int y)
    {
      std::cout << "Drawing at " << x << ", " << y << std::endl;
    }
private:
    const int width_, length_, dimension_;
};