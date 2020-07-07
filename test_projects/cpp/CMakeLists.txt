cmake_minimum_required(VERSION 3.10)

project(Shape)
add_executable(Shape shape/main.cpp shape/Shape.cpp shape/Circle.cpp shape/Rectangle.cpp)

project(Shape-h)
add_executable(Shape-h shape-h/main.cpp shape-h/Shape.h shape-h/Circle.h shape-h/Rectangle.h shape-h/Circle.cpp shape-h/Rectangle.cpp)

project(Macro)
add_executable(Macro macro/Foo.cpp macro/main.cpp macro/Bar.cpp)

project(TemplateClass)
add_executable(TemplateClass template-class/Integer.cpp template-class/IntegerShape.cpp template-class/Long.cpp
        template-class/main.cpp template-class/Parallelepiped.cpp template-class/ShapeTemplate.cpp template-class/Value.cpp)

project(ShapeNamespace)
add_executable(ShapeNamespace shape-namespace/main.cpp shape-namespace/Shape/Shape.cpp
        shape-namespace/dimension2/Rectangle.cpp shape-namespace/dimension2/Circle.cpp
        shape-namespace/dimension3/Circle.cpp)
project(cpp_decorator)

add_executable(cpp_decorator cpp_decorator/main.cpp
        cpp_decorator/Window.cpp
        cpp_decorator/WindowScrollBar.cpp
        cpp_decorator/VerticalScrollBarDecorator.cpp
        cpp_decorator/HorizontalScrollBarDecorator.cpp
        cpp_decorator/HorizontalScrollBarDecorator.h
        cpp_decorator/PrettyDecorator.cpp
        cpp_decorator/PrettyDecorator.h
        cpp_decorator/Damido.cpp
        cpp_decorator/Damido.h
        cpp_decorator/LauraR.cpp
        cpp_decorator/LauraR.h)

project(cpp_strategy_archetype)
add_executable(cpp_strategy_archetype cpp_strategy/BubbleSortStrategy.cpp cpp_strategy/main.cpp
        cpp_strategy/QuickSortStrategy.cpp cpp_strategy/SortStrategy.cpp cpp_strategy/Professor.cpp)

project(multiple-inheritance)
add_executable(multiple-inheritance multiple-inheritance/basic-inheritance/Animal.h multiple-inheritance/basic-inheritance/Carnivore.h
        multiple-inheritance/basic-inheritance/Cow.h multiple-inheritance/basic-inheritance/Herbivore.h
        multiple-inheritance/basic-inheritance/Tiger.h multiple-inheritance/diamond-inheritance/Shape.h
        multiple-inheritance/diamond-inheritance/Rhombus.h multiple-inheritance/diamond-inheritance/Rectangle.h
        multiple-inheritance/diamond-inheritance/Square.h multiple-inheritance/main.cpp)

project(template-specialization)
add_executable(template-specialization template-specialization/main.cpp template-specialization/Optional.cpp template-specialization/Optional.cpp)