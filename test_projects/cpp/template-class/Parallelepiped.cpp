#include <iostream>
#include "ShapeTemplate.cpp"

template<typename T>
class Parallelepiped : public ShapeTemplate<double>
{
public:
    // default constructor
    Parallelepiped() : width_{0.0}, length_{0.0} {}

    Parallelepiped(double width, double length) : width_{width}, length_{length} {}

    // destructor
    ~Parallelepiped() override = default;

    double area() override
    {
        return width_ * length_;
    }

    double perimeter() override
    {
      return 2 * (width_ + length_);
    }

    void draw(int x, int y)
    {
      std::cout << "Drawing at " << x << ", " << y << std::endl;
    }

    T getColor() {
        return color_;
    }
private:
    const double width_, length_;
    T color_;
};