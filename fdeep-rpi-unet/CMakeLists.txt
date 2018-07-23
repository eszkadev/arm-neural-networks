cmake_minimum_required(VERSION 3.0) # minimum requirement for Hunter

project(sample-frugally-deep)

find_package(frugally-deep CONFIG REQUIRED)

SET(GCC_ARM_NEON    "-fopenmp -mcpu=cortex-a53 -mfpu=neon-fp-armv8 -mfp16-format=ieee -ftree-vectorize -fopt-info-vec-optimized -O3")
add_definitions(${GCC_ARM_NEON})
SET(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} ${GCC_ARM_NEON}")
add_compile_options(-D NDEBUG)

add_executable(foo main.cpp EasyBMP.cpp)
target_link_libraries(foo PUBLIC frugally-deep::fdeep) # add frugally-deep and dependencies (libs/includes/flags/definitions)
