cmake_minimum_required(VERSION 3.4.1)

project(metronome)

# Note that the OBOE projects themselves set an OBOE_DIR variable. Use a different name.
set(OBOE_ROOT_DIR /Users/jed/code/oboe)
set(PARSELIB_DIR ${OBOE_ROOT_DIR}/samples/parselib)
set(IOLIB_DIR ${OBOE_ROOT_DIR}/samples/iolib)

# Add the Oboe library as a subproject. Since Oboe is an out-of-tree source library we must also
# specify a binary directory
add_subdirectory(
        ${OBOE_ROOT_DIR} ./oboe-bin
)

include(${PARSELIB_DIR}/src/main/cpp/CMakeLists.txt)
include(${IOLIB_DIR}/src/main/cpp/CMakeLists.txt)

# Include the Oboe headers and shared sample code
include_directories(
        ${OBOE_ROOT_DIR}/include
        ${OBOE_ROOT_DIR}/src/flowgraph
        ${OBOE_ROOT_DIR}/src/flowgraph/resampler
        ${PARSELIB_DIR}/src/main/cpp
        ${IOLIB_DIR}/src/main/cpp
)

set(APP_SOURCES
        jni_bridge.cpp
)

add_library(
        metronome SHARED
        ${APP_SOURCES}
)

target_compile_options(
        metronome PRIVATE
        -v
        -std=c++17
        -Wall
        -Wshadow
        -Wshadow-field
        -Ofast
        "$<$<CONFIG:DEBUG>:-Werror>"
)

# Specify the libraries which our native library is dependent on, including Oboe
target_link_libraries(
        metronome

        -Wl,--whole-archive
        iolib
        parselib
        -Wl,--no-whole-archive

        oboe

        # Links the target library to the log library
        # included in the NDK.
        log
)

# Enable logging of D,V for debug builds
target_compile_definitions(
        metronome PUBLIC
        $<$<CONFIG:DEBUG>:METRONOME_ENABLE_LOGGING=1>)

