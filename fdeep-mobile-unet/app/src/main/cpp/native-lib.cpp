#include <jni.h>
#include <fdeep/fdeep.hpp>
#include <android/bitmap.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <string>

static fdeep::model* pModel = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_eszkadev_fdeep_1mobile_1unet_MainActivity_loadModel(
        JNIEnv *pEnv,
        jobject /* aThis */,
        jobject aJavaAssetManager,
        jboolean bRunTests) {
    try {
        AAssetManager* pAssetManager = AAssetManager_fromJava(pEnv, aJavaAssetManager);
        AAsset* pAsset = AAssetManager_open(pAssetManager, (const char *)"unet.json", AASSET_MODE_UNKNOWN);
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
Java_com_eszkadev_fdeep_1mobile_1unet_MainActivity_unloadModel(JNIEnv /* *pEnv */,
                                                               jobject /* aThis */) {
    delete pModel;
}

extern "C" JNIEXPORT void JNICALL
Java_com_eszkadev_fdeep_1mobile_1unet_MainActivity_runModel(
        JNIEnv *pEnv,
        jobject /* aThis */,
        jobject aInputBitmap,
        jobject aOutputBitmap) {
    int nWidth = 256;
    int nHeight = 256;
    int nDepth = 1;
    uint8_t* pBitmapPixels = nullptr;

    // Get input
    AndroidBitmap_lockPixels(pEnv, aInputBitmap, (void**)&pBitmapPixels);

    std::uint8_t* pInputData = new std::uint8_t[nWidth * nHeight * nDepth];
    for(int y = 0; y < nHeight; y++) {
        for (int x = 0; x < nWidth; x++) {
            pInputData[x + y * nWidth] = pBitmapPixels[(x + y * nWidth) * 4];
        }
    }

    AndroidBitmap_unlockPixels(pEnv, aInputBitmap);

    fdeep::tensor3 aInputData = fdeep::tensor3_from_bytes(pInputData, nWidth, nHeight, nDepth);

    // Evaluate
    const auto result = pModel->predict({aInputData});
    std::vector<uint8_t> aResults = fdeep::tensor3_to_bytes(result[0]);

    // Save output
    AndroidBitmap_lockPixels(pEnv, aOutputBitmap, (void**)&pBitmapPixels);
    for(int y = 0; y < nHeight; y++) {
        for (int x = 0; x < nWidth; x++) {
            uint8_t value = aResults[x + y * nWidth];
            long nPos = (x + y * nWidth) * 4;
            pBitmapPixels[0 + nPos] = value;
            pBitmapPixels[1 + nPos] = value;
            pBitmapPixels[2 + nPos] = value;
            pBitmapPixels[3 + nPos] = 255;
        }
    }
    AndroidBitmap_unlockPixels(pEnv, aOutputBitmap);
}