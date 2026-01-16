#pragma once

#include <cstdint>
#include <vector>

class AudioCapture {
public:
    AudioCapture();
    std::vector<int16_t> captureFrame();
};
