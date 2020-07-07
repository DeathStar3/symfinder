#include <iostream>
#include "Integer.cpp"
#include "Long.cpp"

template<typename T>
Value<T>* value(T value);

template<>
Value<long>* value(long l) {
    return new Long(l);
}

template<>
Value<int>* value(int l) {
    return new Integer(l);
}

int main() {
    std::cout << "Hello, World!" << std::endl;

    Integer* i = new Integer(42);

    std::cout << i->get() << std::endl;
    i->increment();
    std::cout << i->get() << std::endl;

    Long* l = new Long(666L);

    std::cout << l->get() << std::endl;
    l->increment();
    std::cout << l->get() << std::endl;

    return 0;
}