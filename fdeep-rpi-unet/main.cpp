#include <fdeep/fdeep.hpp>
#include <fdeep/tensor3.hpp>
#include "EasyBMP.h"
#include <math.h>

int main()
{
    fplus::stopwatch stopwatch;
    const auto model = fdeep::load_model("unet.json", false);
    std::cout << "Load: " << fplus::show_float(0, 6, stopwatch.elapsed()) << std::endl;

    int nX = 0;
    int nY = 0;

    fdeep::tensor3 result(fdeep::shape3(1,1,1), {1});

    BMP bmp;
    bmp.ReadFromFile("input.bmp");

    const int patch_size = 128;
    int size = patch_size * patch_size;
    int offx = nX * patch_size;
    int offy = nY * patch_size;
    
    fdeep::shape3 shape(1, patch_size,patch_size);

    std::vector<float> data;
    for(int y = 0; y < patch_size; y++)
    {
        for(int x = 0; x < patch_size; x++)
        {
            data.push_back( (float)bmp.GetPixel(offx + x, offy + y).Blue/255.0);
        }
    }

    result = fdeep::tensor3(shape, fplus::make_shared_ref<std::vector<float>>(data));

    try
    {
        stopwatch.reset();
        const auto results = model.predict({result});
        std::cout << "Predict: " << fplus::show_float(0, 6, stopwatch.elapsed()) << std::endl;

        std::vector<uint8_t> in = fdeep::tensor3_to_bytes(result);
        std::vector<uint8_t> out = fdeep::tensor3_to_bytes(results[0]);

        uint32_t width = results[0].shape().width_;
        uint32_t height = results[0].shape().height_;
        uint32_t depth = results[0].shape().depth_;
        int size = width * height * depth;

        float* fdata = new float[size];
        width = 128;
        height = 128;
        memset(fdata, 0, size);

        for(int y = 0; y < height; y++)
            for(int x = 0; x < width; x++)
                fdata[x+y*width] = results[0].get(0, x, y);

        BMP bmp;
        bmp.SetSize(width, height);

        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {

                float val = fdata[x + y * width];
                val = val < 0.5 ? 0 : 255;

                RGBApixel pixel;
                pixel.Red = val;
                pixel.Green = val;
                pixel.Blue = val;
                pixel.Alpha = 255;
                bmp.SetPixel(x, y, pixel);
            }
        }

        bmp.WriteToFile("output.bmp");
    }
    catch(std::runtime_error e)
    {
        std::cout << "EXCEPTION: " << e.what() << std::endl;
    }

    return 0;
}
