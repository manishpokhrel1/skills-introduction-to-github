#pragma once

#include <vector>

class WakeDetector {
public:
    WakeDetector();
    bool detect(const std::vector<int16_t>& samples);
};
