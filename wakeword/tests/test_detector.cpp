#include "../include/wake_detector.h"
#include <cassert>
int main(){
    WakeDetector d;
    std::vector<int16_t> s(512,0);
    bool r = d.detect(s);
    (void)r;
    return 0;
}
