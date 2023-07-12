#include <jni.h>
#include <string>
#include <cstdio>
#include <unistd.h>
#include "sndfile.hh"
#include <fstream>

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_example_database_1test_audioclassify_AcousticSceneClassification_readWAVFile(
        JNIEnv *env,
        jobject /* this */,
        jstring path) {

    auto buffer = new float[441000];

    const char *nativeString = env->GetStringUTFChars(path, 0);

    SndfileHandle file = SndfileHandle(nativeString);

    file.read(buffer, 441000);

    env->ReleaseStringUTFChars(path, nativeString);

    jfloatArray result;
    result = env->NewFloatArray(441000);
    env->SetFloatArrayRegion(result, 0, 441000, buffer);
    free(buffer);

    return result;
}