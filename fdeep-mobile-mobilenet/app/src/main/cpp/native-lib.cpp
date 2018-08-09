#include <jni.h>
#include <fdeep/fdeep.hpp>
#include <android/bitmap.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <string>

static fdeep::model* pModel = nullptr;
static std::uint8_t* pInputData = nullptr;
static std::vector<uint8_t> aOutput;

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

    pInputData = new std::uint8_t[nWidth * nHeight * nDepth];
    for(int y = 0; y < nHeight; y++) {
        for (int x = 0; x < nWidth*4; x+=4) {
            pInputData[x + y * nWidth] = pBitmapPixels[(x + y * nWidth)];
            pInputData[x + 1 + y * nWidth] = pBitmapPixels[(x + 1 + y * nWidth)];
            pInputData[x + 2 + y * nWidth] = pBitmapPixels[(x + 2 + y * nWidth)];
        }
    }

    AndroidBitmap_unlockPixels(pEnv, aInputBitmap);
}

extern "C" JNIEXPORT void JNICALL
Java_com_eszkadev_fdeep_1mobile_1mobilenet_MainActivity_runModel(
        JNIEnv *pEnv,
        jobject /* aThis */) {
    int nWidth = 224;
    int nHeight = 224;
    int nDepth = 3;

    fdeep::tensor3 aInputData = fdeep::tensor3_from_bytes(pInputData, nWidth, nHeight, nDepth);

    // Evaluate
    const auto result = pModel->predict({aInputData});
    //aOutput = fdeep::tensor3_to_bytes(result[0]);
}

extern "C" JNIEXPORT void JNICALL
Java_com_eszkadev_fdeep_1mobile_1mobilenet_MainActivity_fetchOutput(
        JNIEnv *pEnv,
        jobject /* aThis */,
        jobject aOutputBitmap) {
}