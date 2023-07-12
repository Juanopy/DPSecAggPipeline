#include "Base64Helper.h"


std::string Base64Helper::secByteBlockToBase64String(CryptoPP::SecByteBlock secByteBlock) {
    std::string encoded;
    CryptoPP::Base64Encoder base64(new CryptoPP::StringSink(encoded));

    base64.Put(secByteBlock.BytePtr(), secByteBlock.SizeInBytes());
    base64.MessageEnd();
    return encoded;
}

CryptoPP::SecByteBlock Base64Helper::base64StringToSecByteBlock(std::string base64String) {
    std::string decoded;

    CryptoPP::StringSource ss(base64String, true,
        new CryptoPP::Base64Decoder(
            new CryptoPP::StringSink(decoded)
        ) // Base64Decoder
    ); // StringSource
    CryptoPP::SecByteBlock key(reinterpret_cast<const CryptoPP::byte*>(&decoded[0]), decoded.size());
    return key;
}

std::string Base64Helper::encodeString(std::string string) {
    std::string encoded;

    CryptoPP::StringSource ss(string, true,
        new CryptoPP::Base64Encoder(
            new CryptoPP::StringSink(encoded)
        ) // Base64Encoder
    ); // StringSource

    return encoded;
}

std::string Base64Helper::decodeString(std::string encoded) {
    std::string decoded;

    CryptoPP::StringSource ss(encoded, true,
        new CryptoPP::Base64Decoder(
            new CryptoPP::StringSink(decoded)
        ) // Base64Encoder
    ); // StringSource

    return decoded;
}

/*
std::list<std::string> vectorToBase64List(std::vector<std::string> vector) {
    std::list<std::string> list;
    for (std::string iterator : vector) {
        std::string temp;
        temp = iterator;

    }
}*/
