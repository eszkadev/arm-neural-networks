#include <fdeep/fdeep.hpp>
#include <fdeep/tensor3.hpp>
#include "EasyBMP.h"
#include <sstream>
#include <ctime>
#include <math.h>

#define HEIGHT 224
#define WIDTH 224
#define CHANNELS 3

int main(int argc, char** argv)
{
    fplus::stopwatch stopwatch;
    const auto model = fdeep::load_model("mobilenet.json", false);
    std::cout << "Load: " << fplus::show_float(0, 6, stopwatch.elapsed()) << std::endl; 

    BMP bmp;
    bmp.ReadFromFile(argv[1]);

    std::vector<float> data;
    data.resize(HEIGHT * WIDTH * CHANNELS);
    for(int y = 0; y < HEIGHT; y++)
    {
        for(int x = 0; x < WIDTH; x++)
        {
            data[x + y*WIDTH] = bmp.GetPixel(x, y).Red / 255.0;
            data[x + y*WIDTH + 1] = bmp.GetPixel(x, y).Green / 255.0;
            data[x + y*WIDTH + 2] = bmp.GetPixel(x, y).Blue / 255.0;
        }
    }

    fdeep::shape3 shape(CHANNELS, WIDTH, HEIGHT);
    auto input = fdeep::tensor3(shape, fplus::make_shared_ref<std::vector<float>>(data));

    try
    {
	stopwatch.reset();
        const auto results = model.predict({input});
        std::cout << "Predict: " << fplus::show_float(0, 6, stopwatch.elapsed()) << std::endl;

        std::vector<uint8_t> output = fdeep::tensor3_to_bytes(results[0]);
        std::cout << "[ ";
        for(int i = 0; i < output.size(); i++)
        {
            auto& value = output[i];
            std::cout << (int)value << ", ";
        }
        std::cout << "] ";
    }
    catch(std::runtime_error e)
    {
        std::cout << "EXCEPTION: " << e.what() << std::endl;
    }

    std::cout << "END" << std::endl;
    return 0;
}
