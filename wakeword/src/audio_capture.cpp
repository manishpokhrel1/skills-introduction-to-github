#include "../include/audio_capture.h"

AudioCapture::AudioCapture() {}

std::vector<int16_t> AudioCapture::captureFrame() {
    return std::vector<int16_t>(FRAME_SIZE, 0);
}
