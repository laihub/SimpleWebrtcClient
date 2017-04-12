#include <jni.h>
#include <string>

#include <android/log.h>

#define LOG_TAG "CLWebRtcJni"
#define CLASS_NAME "com/closeli/natives/CLWebRtcNativeBinder"

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C" {
//初始化开始
JNIEXPORT void JNICALL Java_com_closeli_natives_CLWebRtcNativeBinder_init(
        JNIEnv * env, jclass thisClass, jobject context);


JNIEXPORT int JNICALL Java_com_closeli_natives_CLWebRtcNativeBinder_connect(
        JNIEnv * env, jclass thisClass, int peerID);


JNIEXPORT void JNICALL Java_com_closeli_natives_CLWebRtcNativeBinder_disConnect(
        JNIEnv * env, jclass thisClass, int peerID);





//结束
JNIEXPORT void JNICALL Java_com_closeli_natives_CLWebRtcNativeBinder_stop(
        JNIEnv * env, jclass thisClass);
//发送数据
JNIEXPORT void JNICALL Java_com_closeli_natives_CLWebRtcNativeBinder_sendVideoData(
        JNIEnv * env, jclass thisClass, jbyteArray data, jint width, jint height);
}

/////////////////////////////////////////////////////////////////
///////////////////////    线程保护   /////////////////////////////////
/////////////////////////////////////////////////////////////////
static jclass jniClass = NULL;
static JavaVM *mVm = NULL;

//线程保护
pthread_key_t returnKey;
JNIEnv *getEnvInThread(JavaVM *vm, pthread_key_t &key, void (*destructor_function)(void *)) {
    JNIEnv *env = NULL;
    if ((env = (JNIEnv *)pthread_getspecific(key)) == NULL) {
        (vm)->AttachCurrentThread(&env, NULL);
        pthread_key_create(&key, destructor_function);
        pthread_setspecific(key, env);
    }
    return env;
}
void detachThread(void *env)
{
    LOGE("Chat detachThread env::::: %p", env);
    (mVm)->DetachCurrentThread();
}

/////////////////////////////////////////////////////////////////



static jmethodID onReturnID = NULL;

/**
 * 要有类别注册回调
 * @param pdata      返回的RGBA数据
 * @param ulsize     数据长度
 * @param width     图片宽
 * @param height    图片高
 */
void onReturnData(char* pdata, int ulsize, int width, int height)
{
    JNIEnv* env = getEnvInThread(mVm, returnKey, detachThread);

    jbyteArray localArray = env->NewByteArray(ulsize);
    env->SetByteArrayRegion(localArray, 0, ulsize, (jbyte *) pdata);

    env->CallStaticVoidMethod(jniClass, onReturnID, localArray, width, height);

    if (NULL != localArray) {
        env->DeleteLocalRef(localArray);
    }
}


JNIEXPORT void JNICALL Java_com_closeli_natives_CLWebRtcNativeBinder_init(
        JNIEnv * env, jclass thisClass, jobject context) {


    LOGE("initStart ...........");

    jint result = env->GetJavaVM(&mVm);
    LOGE("get vm result: %d", result);
    jclass clazz = env->FindClass(CLASS_NAME);
    jniClass = (jclass)env->NewGlobalRef(clazz);
    LOGE("Init jni environement done");

    //TODO 初始化， 主要要注册 onReturnData 回调

    onReturnID = env->GetStaticMethodID(clazz, "onVideoData", "(I[BII)V");
}


JNIEXPORT int JNICALL Java_com_closeli_natives_CLWebRtcNativeBinder_connect(
        JNIEnv * env, jclass thisClass, int peerID){

    return 0;
}


JNIEXPORT void JNICALL Java_com_closeli_natives_CLWebRtcNativeBinder_disConnect(
        JNIEnv * env, jclass thisClass, int peerID) {

}


JNIEXPORT void JNICALL Java_com_closeli_natives_CLWebRtcNativeBinder_stop(
        JNIEnv * env, jclass thisClass) {

    LOGE("stop ...........");
    //TODO 销毁具柄

}

JNIEXPORT void JNICALL Java_com_closeli_natives_CLWebRtcNativeBinder_sendVideoData(
        JNIEnv * env, jclass thisClass, jbyteArray data, jint width, jint height) {

//    LOGE("sendVideoData ...........");

    jbyte *localArray = NULL;
    if (NULL != data) {
        localArray = env->GetByteArrayElements(data, NULL);
    }

    if (NULL != localArray) {


        char *sendData = (char*) localArray;

        //TODO 发送数据



        env->ReleaseByteArrayElements(data, localArray, 0);
    }
}


