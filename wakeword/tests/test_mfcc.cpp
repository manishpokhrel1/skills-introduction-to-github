#include "../include/mfcc.h"
#include <cassert>
int main(){
    std::vector<int16_t> samples(512,0);
    auto mfcc = compute_mfcc(samples);
    assert(mfcc.size()==13);
    return 0;
}
