#include "../include/audio_capture.h"

static constexpr size_t FRAME_SIZE = 512;

AudioCapture::AudioCapture() {}

std::vector<int16_t> AudioCapture::captureFrame() {
    return std::vector<int16_t>(FRAME_SIZE, 0);
}
