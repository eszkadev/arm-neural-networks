#include <jni.h>
#include <fdeep/fdeep.hpp>
#include <android/bitmap.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <string>

static fdeep::model* pModel = nullptr;
static std::vector<unsigned char> aInputData;
static std::vector<fdeep::internal::float_type> aOutput;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_eszkadev_fdeep_1mobile_1mobilenet_MainActivity_loadModel(
        JNIEnv *pEnv,
        jobject /* aThis */,
        jobject aJavaAssetManager,
        jboolean bRunTests) {
    try {
        AAssetManager* pAssetManager = AAssetManager_fromJava(pEnv, aJavaAssetManager);
        AAsset* pAsset = AAssetManager_open(pAssetManager, (const char *)"mobilenet.json", AASSET_MODE_UNKNOWN);
        if (nullptr == pAsset)
            return false;

        long nSize = AAsset_getLength(pAsset);
        char* pBuffer = (char*)malloc(sizeof(char) * nSize);
        AAsset_read(pAsset, pBuffer, nSize);
        AAsset_close(pAsset);

        std::string sContent(pBuffer, nSize);
        pModel = fdeep::read_model_from_string(sContent, bRunTests);

        return true;
    } catch (...) {}

    return false;
}

extern "C" JNIEXPORT void JNICALL
Java_com_eszkadev_fdeep_1mobile_1mobilenet_MainActivity_unloadModel(JNIEnv /* *pEnv */,
                                                               jobject /* aThis */) {
    delete pModel;
}

extern "C" JNIEXPORT void JNICALL
Java_com_eszkadev_fdeep_1mobile_1mobilenet_MainActivity_feedModel(
        JNIEnv *pEnv,
        jobject /* aThis */,
        jobject aInputBitmap) {
    int nWidth = 224;
    int nHeight = 224;
    int nDepth = 3;
    uint8_t* pBitmapPixels = nullptr;

    // Get input
    AndroidBitmap_lockPixels(pEnv, aInputBitmap, (void**)&pBitmapPixels);

    aInputData.clear();
    aInputData.reserve(nWidth * nHeight * nDepth);

    for (int y = 0; y < nHeight; y++)
    {
        for (int x = 0; x < nWidth; x++)
        {
            fdeep::internal::float_type r = pBitmapPixels[(x + y * nWidth) * 4];
            fdeep::internal::float_type g = pBitmapPixels[(x + y * nWidth) * 4 + 1];
            fdeep::internal::float_type b = pBitmapPixels[(x + y * nWidth) * 4 + 2];
            aInputData.push_back(r);
            aInputData.push_back(g);
            aInputData.push_back(b);
        }
    }

    AndroidBitmap_unlockPixels(pEnv, aInputBitmap);
}

extern "C" JNIEXPORT void JNICALL
Java_com_eszkadev_fdeep_1mobile_1mobilenet_MainActivity_runModel(
        JNIEnv /**pEnv*/,
        jobject /* aThis */) {
    int nWidth = 224;
    int nHeight = 224;
    int nDepth = 3;

    auto aInput = fdeep::tensor3_from_bytes(aInputData.data(), nWidth, nHeight, nDepth, -1.0, 1.0);

    // Evaluate
    const auto result = pModel->predict({aInput});
    aOutput = *(result[0].as_vector());
}

extern "C" JNIEXPORT jint JNICALL
Java_com_eszkadev_fdeep_1mobile_1mobilenet_MainActivity_fetchOutput(
        JNIEnv /**pEnv*/,
        jobject /* aThis */) {
    int maxpos = 0;
    fdeep::internal::float_type max = -1;
    int i = 0;

    for(auto value : aOutput)
    {
        if(value > max)
        {
            maxpos = i;
            max = value;
        }
        i++;
    }
    return maxpos;
}