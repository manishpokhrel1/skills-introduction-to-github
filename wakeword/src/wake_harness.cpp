// Minimal native harness used when `native_test` executable is enabled.
// This file is harmless when built into the shared library and provides
// a small `main` when CMake option `BUILD_NATIVE_TEST` is enabled.

#include <iostream>

extern "C" void native_harness_ping() {
    // placeholder API so tests can link against library symbols if needed
}

#ifdef BUILD_NATIVE_TEST
int main() {
    std::cout << "native_test: harness started" << std::endl;
    // simulate small work
    for (int i = 0; i < 3; ++i) {
        std::cout << "native_test: ping " << i << std::endl;
    }
    std::cout << "native_test: harness finished" << std::endl;
    return 0;
}
#endif
