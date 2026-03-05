#include <jni.h>
#include <iostream>
#include <Windows.h>

extern "C" {

    auto decompress = (int(__cdecl*)(byte * input, int insize, byte * output)) NULL;

    JNIEXPORT jboolean JNICALL Java_fr_ni240sx_ucgt_compression_Compression_pluginInit() {

        auto lzcLib = LoadLibrary(L"C:\\Users\\gaupp\\git\\UCGT\\bin\\LZCompressLib.dll");

        if (lzcLib == NULL) return false;

        decompress = (int(__cdecl*)(byte * input, int insize, byte * output)) GetProcAddress(lzcLib, "BlockDecompress");

        if (decompress == NULL) return false;

//        std::cout << "UCGT Compression Plugin loaded successfully.\n"; //done in Java for console desync reasons

        return true;
    }

    JNIEXPORT jbyteArray JNICALL Java_fr_ni240sx_ucgt_compression_Compression_decompressPlugin
    (JNIEnv* env, jclass _class, jbyteArray jArr) {

        boolean isCopy;
        jbyte* jb = env->GetByteArrayElements(jArr, &isCopy);
        auto compressedLength = env->GetArrayLength(jArr);

        byte* input = (byte*)jb;
        
        auto signature = ((input[0] << 0) | (input[1] << 8) | (input[2] << 16) | (input[3] << 24));

//        std::cout << signature << "\n";
        switch (signature) {
//        case 1263552082: //disabled because RefPack decompressor in 64 bit LZCompressLib is broken
//            std::cout << "RefPack\n";
//            break;
        case 1514947658:
            //std::cout << "JDLZ\n";
            break;
        case 1179014472:
            //std::cout << "HUFF\n";
            break;
        case 1347243843:
            //std::cout << "COMP\n";
            break;
        default:
            return NULL; //handle in Java
        }


        auto decompressedLength = ((input[8] << 0) | (input[9] << 8) | (input[10] << 16) | (input[11] << 24));

        byte* output = new byte[decompressedLength];

        decompress(input, compressedLength, output);

        env->ReleaseByteArrayElements(jArr, jb, 0);

        auto jout = env->NewByteArray(decompressedLength);
        env->SetByteArrayRegion(jout, 0, decompressedLength, (jbyte*)output);

        delete[] output;
        return jout;
    }

}