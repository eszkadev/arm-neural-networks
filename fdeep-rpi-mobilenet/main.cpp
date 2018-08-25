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

    std::vector<unsigned char> pixels;
    pixels.reserve(HEIGHT * WIDTH * CHANNELS);

    for (int y = 0; y < HEIGHT; y++)
    {
        for (int x = 0; x < WIDTH; x++)
        {
		pixels.push_back((float)bmp.GetPixel(x, y).Red);
		pixels.push_back((float)bmp.GetPixel(x, y).Green);
		pixels.push_back((float)bmp.GetPixel(x, y).Blue);
        }
    }

    auto input = fdeep::tensor3_from_bytes(pixels.data(), HEIGHT, WIDTH, CHANNELS,
-1.0, 1.0);

    try
    {
	stopwatch.reset();
        const auto results = model.predict({input});
        std::cout << "Predict: " << fplus::show_float(0, 6, stopwatch.elapsed()) << std::endl;


        std::vector<uint8_t> output = fdeep::tensor3_to_bytes(results[0]);

        int maxpos = 0;
        float max = -1;
	int i = 0;
        for(auto value : *(results[0].as_vector()))
        {
            if(value > max)
            {
                maxpos = i;
                max = value;
            }
	    i++;
        }

        std::cout << "Result: " << maxpos << std::endl;
    }
    catch(std::runtime_error e)
    {
        std::cout << "EXCEPTION: " << e.what() << std::endl;
    }

    //std::cout << "END" << std::endl;
    return 0;
}
