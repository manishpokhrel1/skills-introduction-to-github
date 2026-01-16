#include "../include/audio_capture.h"
#include <iostream>
int main(){
    AudioCapture ac;
    auto frame = ac.captureFrame();
    std::cout << "Captured " << frame.size() << " samples\n";
    return 0;
}
