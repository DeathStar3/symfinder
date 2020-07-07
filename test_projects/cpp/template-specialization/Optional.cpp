//
// Created by nathan on 02/12/2019.
//

template<class T>
class Optional {
public:
    Optional() : value_{nullptr} {}

    explicit Optional(T* value) : value_{value} {}

    void empty() {
        value_ = nullptr;
    }

    bool isPresent() {
        return value_ != nullptr;
    }

    T get() {
        return value_;
    }
private:
    T* value_;
};

class Long : public Optional<long> {
public:
    Long() : Optional() {}

    Long(long* value) : Optional(value) {}
};
