//
// Created by ThePrecisionBro on 27/11/2019.
//

//
// Created by ThePrecisionBro on 26/11/2019.
//

#ifndef CPP_STRATEGY_ARCHETYPE_PROFFESSOR
#define CPP_STRATEGY_ARCHETYPE_PROFFESSOR

#include <vector>
#include <iostream>

class Proffessor {
public:
    virtual void teach() = 0;
};

class PCollet : public Proffessor {
public:
    void teach() override { std::cout << "PCollet : Vous avez vue the mandalorian?" << std::endl; }
};

class JMortara : public Proffessor {
public:
    void teach() override {
        std::cout << "JMortara : Despoei tugiù sciü d'u nostru paise\n Se ride au ventu, u meme pavayùn" << std::endl;
    }
};


#endif //CPP_STRATEGY_ARCHETYPE_SORTSTRATEGY_H
