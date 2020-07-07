template<typename T>
class ShapeTemplate {
public:
    virtual ~ShapeTemplate() = default;
    virtual T area() = 0;
    virtual T perimeter() = 0;
};