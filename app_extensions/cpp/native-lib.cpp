#pragma once
#include <jni.h>
#include <string>
#include <iostream>
#include <vector>

#include "aes.h"
#include "secblock.h"
#include "cryptlib.h"
#include "rijndael.h"
#include "modes.h"
#include "files.h"
#include "osrng.h"
#include "hex.h"
#include "channels.h"
#include "ida.h"

//Diffie Hellman
#include<tuple>
#include"eccrypto.h"
#include"osrng.h"
#include"oids.h"
#include <iostream>
#include <iomanip>

//Base64
#include <iostream>
#include <base64.h>
#include "cryptlib.h"

//Shamir Secret Sharing
#include <iostream>
#include <vector>
#include "osrng.h"
#include "channels.h"
#include "ida.h"

//SHA2
#include "cryptlib.h"
#include <iostream>
#include "sha.h"
#include "secblock.h"

//PRG
#include <iostream>
#include "cryptlib.h"
#include "secblock.h"
#include "rijndael.h"
#include "osrng.h"
#include "modes.h"
#include "hex.h"
#include "files.h"
#include <vector>
#include <sstream>
#include <ostream>

//AES
#include <iostream>
#include "cryptlib.h"
#include "secblock.h"
#include "rijndael.h"
#include "osrng.h"
#include "modes.h"
#include "hex.h"
#include "files.h"



//********************************************************Helper classes***************************************************************************************

//*******************************************************Start Base64**************************************************************
class Base64Helper{
public:
    std::string secByteBlockToBase64String(CryptoPP::SecByteBlock secByteBlock);
    CryptoPP::SecByteBlock base64StringToSecByteBlock(std::string base64String);
    std::string encodeString(std::string string);
    std::string decodeString(std::string encoded);
};


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

//******************************************************Start Diffie Hellman**********************************************************************

class EcDiffieHellmanHelper{
public:
    std::tuple<CryptoPP::SecByteBlock, CryptoPP::SecByteBlock> generateDhKeys();
    CryptoPP::SecByteBlock generateSharedSecret(CryptoPP::SecByteBlock privKey, CryptoPP::SecByteBlock pubKey);
};

std::tuple<CryptoPP::SecByteBlock, CryptoPP::SecByteBlock> EcDiffieHellmanHelper::generateDhKeys() {
    CryptoPP::OID CURVE = CryptoPP::ASN1::secp256r1();
    CryptoPP::AutoSeededRandomPool rng;

    CryptoPP::ECDH < CryptoPP::ECP >::Domain dh(CURVE);
    CryptoPP::SecByteBlock priv(dh.PrivateKeyLength()), pub(dh.PublicKeyLength());
    dh.GenerateKeyPair(rng, priv, pub);

    return std::tuple<CryptoPP::SecByteBlock, CryptoPP::SecByteBlock>{priv, pub};

}


CryptoPP::SecByteBlock EcDiffieHellmanHelper::generateSharedSecret(CryptoPP::SecByteBlock privKey, CryptoPP::SecByteBlock pubKey) {
    CryptoPP::OID CURVE = CryptoPP::ASN1::secp256r1();
    CryptoPP::ECDH < CryptoPP::ECP >::Domain dh(CURVE);
    CryptoPP::SecByteBlock sharedSecret(dh.AgreedValueLength());
    dh.Agree(sharedSecret, privKey, pubKey);

    return sharedSecret;

}

//**************************************************************Start Shamir Secret Sharing**********************************************************
class ShamirSecretSharingHelper{
public:
    std::vector<std::string> generateShares(std::string secret, int thresh, int amountShares);
    std::string reconstructSecret(int thresh, std::vector<std::string> shares);
};

std::vector<std::string> ShamirSecretSharingHelper::generateShares(std::string secret, int thresh, int amountShares)
{
    std::string message = secret;
    int threshold = thresh;
    int shares = amountShares;
    const unsigned int CHID_LENGTH = 4;



    // ********** Secret Sharing **********//

    CryptoPP::AutoSeededRandomPool rng;

    CryptoPP::ChannelSwitch* channelSwitch = NULLPTR;
    CryptoPP::StringSource source(message, false, new CryptoPP::SecretSharing(rng, threshold, shares, channelSwitch = new CryptoPP::ChannelSwitch));

    std::vector<std::string> strShares(shares);
    CryptoPP::vector_member_ptrs<CryptoPP::StringSink> strSinks(shares);
    std::string channel;

    // ********** Create Shares
    for (unsigned int i = 0; i < shares; i++)
    {
        strSinks[i].reset(new CryptoPP::StringSink(strShares[i]));
        channel = CryptoPP::WordToString<CryptoPP::word32>(i);
        strSinks[i]->Put((const CryptoPP::byte*)channel.data(), CHID_LENGTH);
        channelSwitch->AddRoute(channel, *strSinks[i], CryptoPP::DEFAULT_CHANNEL);
    }
    source.PumpAll();
    /*
    for (int a = 0; a < shares; a++) {
        std::cout << "Share: " << a << " lautet: " << strShares[a] << std::endl;
    }*/

    return strShares;
}

std::string ShamirSecretSharingHelper::reconstructSecret(int thresh, std::vector<std::string> shares)
{
    // ********** Recover secret
    int threshold = thresh;
    std::vector<std::string> strShares = shares;
    const unsigned int CHID_LENGTH = 4;
    std::string channel;
    try
    {
        std::string recovered;
        CryptoPP::SecretRecovery recovery(threshold, new CryptoPP::StringSink(recovered));

        CryptoPP::vector_member_ptrs<CryptoPP::StringSource> strSources(threshold);
        channel.resize(CHID_LENGTH);
        for (unsigned int i = 0; i < threshold; i++)
        {
            strSources[i].reset(new CryptoPP::StringSource(strShares[i], false));
            strSources[i]->Pump(CHID_LENGTH);
            strSources[i]->Get((CryptoPP::byte*)&channel[0], CHID_LENGTH);
            strSources[i]->Attach(new CryptoPP::ChannelSwitch(recovery, channel));
        }

        while (strSources[0]->Pump(256))
        {
            for (unsigned int i = 1; i < threshold; i++)
                strSources[i]->Pump(256);
        }

        for (unsigned int i = 0; i < threshold; i++)
            strSources[i]->PumpAll();
        return recovered;

    }
    catch (const CryptoPP::Exception&)
    {
        std::cout << "Es ist ein Fehler aufgetreten" << std::endl;
    }


}

//*************************************************************************Start Hash*************************************************************************
class Sha2Helper
{
public:
    CryptoPP::SecByteBlock hashMessage(std::string message);
};

CryptoPP::SecByteBlock Sha2Helper::hashMessage(std::string message) {
    CryptoPP::SecByteBlock digest(32 + 16);
    std::string msg = message;
    CryptoPP::SHA384 hash;
    hash.Update((const CryptoPP::byte*)msg.data(), msg.size());
    digest.resize(hash.DigestSize());
    hash.Final((CryptoPP::byte*)&digest[0]);

    return digest;

}

//****************************************************************************Start AES*******************************************************************************
class AESHelper
{
public:
    CryptoPP::SecByteBlock generateAesKey();
    CryptoPP::SecByteBlock generateAesIv();
    std::string encode(std::string plaintext, CryptoPP::SecByteBlock aesKey, CryptoPP::SecByteBlock aesIv);
    std::string decode(std::string ciphertext, CryptoPP::SecByteBlock aesKey, CryptoPP::SecByteBlock aesIv);
};

CryptoPP::SecByteBlock AESHelper::generateAesKey() {
    CryptoPP::AutoSeededRandomPool prng;
    CryptoPP::SecByteBlock key(CryptoPP::AES::DEFAULT_KEYLENGTH);
    prng.GenerateBlock(key, key.size());
    //std::cout << "DER generierte AES key lautet: " << key << std::endl;
    return key;
}

CryptoPP::SecByteBlock AESHelper::generateAesIv() {
    CryptoPP::AutoSeededRandomPool prng;
    CryptoPP::SecByteBlock iv(CryptoPP::AES::BLOCKSIZE);
    prng.GenerateBlock(iv, iv.size());
    return iv;
}

std::string AESHelper::encode(std::string plaintext, CryptoPP::SecByteBlock aesKey, CryptoPP::SecByteBlock aesIv) {
    using namespace CryptoPP;

    HexEncoder encoder(new FileSink(std::cout));

    SecByteBlock key(CryptoPP::AES::DEFAULT_KEYLENGTH);
    key = aesKey;
    SecByteBlock iv(CryptoPP::AES::BLOCKSIZE);
    iv = aesIv;

    std::string plain = plaintext;
    std::string cipher, recovered;

    //std::cout << "plain text: " << plain << std::endl;

    /*********************************\
    \*********************************/

    try
    {
        CBC_Mode< CryptoPP::AES >::Encryption e;
        e.SetKeyWithIV(key, key.size(), iv);

        StringSource s(plain, true,
                       new StreamTransformationFilter(e,
                                                      new StringSink(cipher)
                       ) // StreamTransformationFilter
        ); // StringSource
    }
    catch (const Exception& e)
    {
        std::cerr << e.what() << std::endl;
        exit(1);
    }

    /*********************************\
    \*********************************/

    //std::cout << "key: ";
    encoder.Put(key, key.size());
    encoder.MessageEnd();
    std::cout << std::endl;

    //std::cout << "iv: ";
    encoder.Put(iv, iv.size());
    encoder.MessageEnd();
    std::cout << std::endl;

    //std::cout << "cipher text: ";
    encoder.Put((const byte*)&cipher[0], cipher.size());
    encoder.MessageEnd();
    std::cout << std::endl;
    return cipher;

    /*********************************\
    \*********************************/
}

std::string AESHelper::decode(std::string ciphertext, CryptoPP::SecByteBlock aesKey, CryptoPP::SecByteBlock aesIv) {
    CryptoPP::SecByteBlock key(CryptoPP::AES::DEFAULT_KEYLENGTH);
    key = aesKey;
    CryptoPP::SecByteBlock iv(CryptoPP::AES::BLOCKSIZE);
    iv = aesIv;
    std::string cipher, recovered;
    cipher = ciphertext;
    try
    {
        CryptoPP::CBC_Mode< CryptoPP::AES >::Decryption d;
        d.SetKeyWithIV(key, key.size(), iv);

        CryptoPP::StringSource s(cipher, true,
                                 new CryptoPP::StreamTransformationFilter(d,
                                                                          new CryptoPP::StringSink(recovered)
                                 ) // StreamTransformationFilter
        ); // StringSource

        //std::cout << "recovered text: " << recovered << std::endl;
    }
    catch (const CryptoPP::Exception& e)
    {
        std::cerr << e.what() << std::endl;
        exit(1);
    }

    return recovered;
}














//************************************************************************Start PRG*************************************************************************
class PrgHelper
{
public:
    CryptoPP::SecByteBlock generateRandomFromSeed(CryptoPP::SecByteBlock seed, int l);
    std::vector<CryptoPP::Integer> transformByteBlockToIntegerVector(CryptoPP::SecByteBlock block);
    std::vector<CryptoPP::Integer> addTwoIntegerVectors(std::vector<CryptoPP::Integer> firstVector, std::vector<CryptoPP::Integer> secondVector);
    std::string serialzeIntVector(std::vector <CryptoPP::Integer> intVector);
    std::vector<CryptoPP::Integer> deserializeIntVector(std::string serializedVector);
    std::vector<CryptoPP::Integer> negateVector(std::vector <CryptoPP::Integer> inputVector);
    std::vector<CryptoPP::Integer> initializeNullVector(int sizeOfVector);

    std::list<std::string> splitDataString(std::string data, std::string delimiter);
};


//Seed should be size 32+16 -> key + iv
CryptoPP::SecByteBlock PrgHelper::generateRandomFromSeed(CryptoPP::SecByteBlock seed, int l) {

    CryptoPP::CTR_Mode<CryptoPP::AES>::Encryption prng;
    prng.SetKeyWithIV(seed, 32, seed + 32, 16);

    CryptoPP::SecByteBlock t(l);
    prng.GenerateBlock(t, t.size());


    /*
    std::vector<int> vect;
    std::cout << "Die Länge vom Vektor vor dem Reesize: " << vect.size() << std::endl;
    vect.resize(t.size());  // Make room for elements
    std::memcpy(&vect[0], &t[0], vect.size());
    std::cout << "Die Länge vom Vektor nach dem Reesize: " << vect.size() << std::endl;
    for (int temp : vect) {
        std::cout << "Der Random Vektor enthält: " << temp << std::endl;
    }
    CryptoPP::SecByteBlock test(reinterpret_cast<const CryptoPP::byte*>(&vect[0]), vect.size());
    CryptoPP::Integer test1;
    test1.Decode(test.BytePtr(), test.SizeInBytes());
    std::cout << "Vector als ByteBlock als Integer lautet: " << test1 << std::endl;

    std::vector<CryptoPP::byte> vect2;
    vect2.resize(t.size());  // Make room for elements
    std::memcpy(&vect2[0], &t[0], vect2.size());
    for (CryptoPP::byte temp : vect2) {
        std::cout << "Der Random Byte Vektor enthält: " << temp << std::endl;
    }*/




    CryptoPP::Integer x;
    x.Decode(t.BytePtr(), t.SizeInBytes());


    std::string s;
    CryptoPP::Base64Encoder base64(new CryptoPP::StringSink(s));

    base64.Put(t, t.size());
    base64.MessageEnd();



    return t;
}



std::vector<CryptoPP::Integer> PrgHelper::transformByteBlockToIntegerVector(CryptoPP::SecByteBlock block) {
    std::vector<CryptoPP::byte> vector;
    std::vector<CryptoPP::Integer> intVector;
    vector.resize(block.size());  // Make room for elements
    std::memcpy(&vector[0], &block[0], vector.size());
    for (CryptoPP::byte temp : vector) {
        CryptoPP::Integer test(temp);
        intVector.push_back(test);
    }
    return intVector;
}

std::vector<CryptoPP::Integer> PrgHelper::addTwoIntegerVectors(std::vector<CryptoPP::Integer> firstVector, std::vector<CryptoPP::Integer> secondVector) {
    std::vector<CryptoPP::Integer> resultVector;
    for (int i = 0; i < firstVector.size(); i++) {
        CryptoPP::Integer x;
        x = firstVector[i] + secondVector[i];
        resultVector.push_back(x);
    }



    return resultVector;
}


std::string PrgHelper::serialzeIntVector(std::vector <CryptoPP::Integer> intVector) {
    std::string result;
    std::vector<CryptoPP::Integer> newVector;
    for (CryptoPP::Integer i : intVector) {
        std::ostringstream oss;
        oss << i;
        result += oss.str();
        result += ";";


        int test = std::stoi(oss.str());
        CryptoPP::Integer test2(test);
        newVector.push_back(test2);
    }

    return result;

}

std::list<std::string> PrgHelper::splitDataString(std::string data, std::string delimiter) {
    std::string s = data;
    std::list<std::string> splitData;
    size_t pos = 0;
    std::string token;
    while ((pos = s.find(delimiter)) != std::string::npos) {
        token = s.substr(0, pos);
        splitData.push_back(token);
        s.erase(0, pos + delimiter.length());
    }
    splitData.push_back(s);
    return splitData;

}

std::vector<CryptoPP::Integer> PrgHelper::deserializeIntVector(std::string serializedVector) {

    std::vector<CryptoPP::Integer> deserializedVector;

    std::list<std::string> tempList = splitDataString(serializedVector, ";");

    for (std::string iterator : tempList) {
        if (iterator != "") {
            int tempInt = std::stoi(iterator);

            CryptoPP::Integer tempInteger(tempInt);

            deserializedVector.push_back(tempInteger);

        }
    }


    return deserializedVector;
}

std::vector<CryptoPP::Integer> PrgHelper::negateVector(std::vector <CryptoPP::Integer> inputVector) {
    std::vector<CryptoPP::Integer> resultVector;
    for (CryptoPP::Integer i : inputVector) {
        CryptoPP::Integer negator("-1");
        CryptoPP::Integer negatedI = i * negator;
        resultVector.push_back(negatedI);
    }


    return resultVector;
}

std::vector<CryptoPP::Integer> PrgHelper::initializeNullVector(int sizeOfVector) {
    std::vector<CryptoPP::Integer> resultVector;
    for (int i = 0; i < sizeOfVector; i++) {
        CryptoPP::Integer temp("0");
        resultVector.push_back(temp);
    }

    return resultVector;
}

//******************************************************************Start actual client*************************************************************************************

class Client{
public:
    EcDiffieHellmanHelper ecDiffieHellman;
    std::tuple<CryptoPP::SecByteBlock, CryptoPP::SecByteBlock> ecKeyPair1;
    std::tuple<CryptoPP::SecByteBlock, CryptoPP::SecByteBlock> ecKeyPair2;
    std::string myBase64SecretKey1;
    std::list<std::string> myNeighboursList;
    std::string myId;

    int t;
    int amountNeighbours;

    //Only for step5
    std::list<std::string> a1;
    int l;
    std::map<std::string, std::string> idPk1Map;
    std::map<std::string, std::string> idPk2Map;
    std::string step5Message;
    //Also amountNeighbours
    std::string mySecretKey1Base64String;
    CryptoPP::SecByteBlock sk1ByteBlock;
    //Also myID
    std::string bBase64;
    std::string lable;
    std::string value;

    CryptoPP::SecByteBlock bByteBlock;

    Base64Helper base64;

    //Messages
    std::string step2Message;
    std::string step3Message;


    std::string debug;

    //Only step4Decode
    std::string step4Decoded;
    std::list<std::string> step4List;
    std::string myBase64SecretKey2;
    std::map<std::string, std::string> pk2Map;



    Client(){
        CryptoPP::SecByteBlock temp(32 + 16);
        bByteBlock = temp;
    }


    void step2(){
        ecKeyPair1 = ecDiffieHellman.generateDhKeys();
        ecKeyPair2 = ecDiffieHellman.generateDhKeys();
        std::string ecPublicKey1Base64 = base64.secByteBlockToBase64String(std::get<1>(ecKeyPair1));
        std::string ecPrivateKey1Base64 = base64.secByteBlockToBase64String(std::get<0>(ecKeyPair1));
        std::string ecPrivateKey2Base64 = base64.secByteBlockToBase64String(std::get<0>(ecKeyPair2));
        std::string ecPublicKey2Base64 = base64.secByteBlockToBase64String(std::get<1>(ecKeyPair2));

        step2Message =ecPrivateKey1Base64 + ";" + ecPrivateKey2Base64 + ";" + ecPublicKey1Base64 + ";" + ecPublicKey2Base64;


        std::cout << "Finished step2..." << std::endl;
        std::cout << "**************************************************" << std::endl;

    }

    void step3(){
        Base64Helper base64;

        //Generate a PRG seed bi
        CryptoPP::OS_GenerateRandomBlock(false, bByteBlock, bByteBlock.size());
        std::string b;
        b = base64.secByteBlockToBase64String(bByteBlock);


        //Compute share Hb
        std::vector<std::string> tempShareHbVector;
        ShamirSecretSharingHelper shamir;
        tempShareHbVector = shamir.generateShares(b, t, amountNeighbours);
        std::list<std::string> myBase64HbShares;
        for (std::string iterator : tempShareHbVector) {
            std::string encoded;
            encoded = base64.encodeString(iterator);
            myBase64HbShares.push_front(encoded);
        }


        step3Message = b + ";";
        //Compute share Hs
        std::string sk1Base64;
        sk1Base64 = myBase64SecretKey1;
        std::vector<std::string> tempShareHsVector;
        tempShareHsVector = shamir.generateShares(sk1Base64, t, amountNeighbours);
        std::list<std::string> myBase64HsShares;
        for (std::string iterator : tempShareHsVector) {
            std::string encoded;
            encoded = base64.encodeString(iterator);
            myBase64HsShares.push_front(encoded);
        }
        //Compute cij for each j in Ng
        for (std::string iterator : myNeighboursList) {
            std::string c;
            c = myId + ";" + iterator + ";" + myBase64HbShares.front() + ";" + myBase64HsShares.front();
            myBase64HbShares.pop_front();
            myBase64HsShares.pop_front();
            std::string cEncrypted;
            AESHelper aes;
            Sha2Helper hash;
            CryptoPP::SecByteBlock sk2ByteBlock = base64.base64StringToSecByteBlock(myBase64SecretKey2);
            CryptoPP::SecByteBlock pk2 = base64.base64StringToSecByteBlock(idPk2Map[iterator]);
            CryptoPP::SecByteBlock sharedSecret2 = ecDiffieHellman.generateSharedSecret(sk2ByteBlock, pk2);
            std::string sharedSecretString2(reinterpret_cast<const char*>(&sharedSecret2[0]), sharedSecret2.size());
            CryptoPP::SecByteBlock k = hash.hashMessage(sharedSecretString2);
            CryptoPP::SecByteBlock key(k.BytePtr(), 32);
            CryptoPP::SecByteBlock iv(k.BytePtr() + 32, 16);
            cEncrypted = aes.encode(c, key, iv);
            std::string cEncryptedBase64 = base64.encodeString(cEncrypted);



            step3Message +=  iterator + ";" + cEncryptedBase64 + ";";
        }

    }

    void step5(){
        PrgHelper prng;
        Sha2Helper hash;

        std::vector<CryptoPP::Integer> xVector;

        std::vector<CryptoPP::Integer> sumMVector = prng.initializeNullVector(l);
        std::vector<CryptoPP::Integer> deltaVector;

        sk1ByteBlock = base64.base64StringToSecByteBlock(myBase64SecretKey1);
        CryptoPP::Integer valueX((std::stoi(value)));



        xVector = prng.initializeNullVector(l);
        if(lable=="airport"){
            xVector[0] = valueX;
        }
        else if(lable=="bus"){
            xVector[1]=valueX;
        }
        else if(lable=="metro"){
            xVector[2]=valueX;
        }
        else if(lable=="metro_station"){
            xVector[3]=valueX;
        }
        else if(lable=="park"){
            xVector[4]=valueX;
        }
        else if(lable=="public_square"){
            xVector[5]=valueX;
        }
        else if(lable=="shopping_mall"){
            xVector[6]=valueX;
        }
        else if(lable=="street_pedestrian"){
            xVector[7]=valueX;
        }
        else if(lable=="street_traffic"){
            xVector[8]=valueX;
        }
        else if(lable=="tram"){
            xVector[9]=valueX;
        }


        //Generate PRG Seed si,j = KA(sk1i, pk1j)
        //Using sha3-384 to generate a seed with the correct size of 32+16 -> key + iv for aes in ctr mode
        for (std::string iterator : a1) {
            CryptoPP::SecByteBlock pk1 = base64.base64StringToSecByteBlock(idPk1Map[iterator]);
            std::cout << "Der base64 pk1 von: " << iterator << "lautet: " << idPk1Map[iterator] << std::endl;

            CryptoPP::SecByteBlock sharedSecret = ecDiffieHellman.generateSharedSecret(sk1ByteBlock, pk1);
            std::string sharedSecretString(reinterpret_cast<const char*>(&sharedSecret[0]), sharedSecret.size());
            CryptoPP::SecByteBlock s = hash.hashMessage(sharedSecretString);
            CryptoPP::SecByteBlock mByteBlock = prng.generateRandomFromSeed(s, l);

            std::vector<CryptoPP::Integer> mVector = prng.transformByteBlockToIntegerVector(mByteBlock);


            if (std::stoi(iterator) < std::stoi(myId)) {
                mVector = prng.negateVector(mVector);

            }
            std::cout << "M zwischen: " << iterator << " und: " << myId << " lautet: " << mVector[0] << std::endl;

            sumMVector = prng.addTwoIntegerVectors(mVector, sumMVector);


        }
        bByteBlock = base64.base64StringToSecByteBlock(bBase64);
        CryptoPP::SecByteBlock rByteBlock = prng.generateRandomFromSeed(bByteBlock, l);
        std::vector<CryptoPP::Integer> rVector = prng.transformByteBlockToIntegerVector(rByteBlock);
        for (CryptoPP::Integer test : rVector) {
            std::cout << "R als Vektor: " << test << std::endl;
        }

        std::vector<CryptoPP::Integer> yVector = prng.addTwoIntegerVectors(rVector, sumMVector);

/*
        if (myId == "0") {
            //yInteger += xInteger;
            yVector = prng.addTwoIntegerVectors(xVector, yVector);
        }*/
        yVector = prng.addTwoIntegerVectors(xVector, yVector);



        std::string yString = prng.serialzeIntVector(yVector);
        step5Message = "Step5;" + yString;



        std::cout << "Finished step5..." << std::endl;
        std::cout << "**************************************************" << std::endl;
    }


    void decodeStep4(){

        while(!step4List.empty()){
        //for(int a = 0; a<2; a++){
            AESHelper aes;
            Sha2Helper hash;
            step4List.pop_front();
            std::string i = step4List.front();
            step4List.pop_front();
            std::string encodedCBase64 = step4List.front();
            step4List.pop_front();
            std::string encodedC = base64.decodeString(encodedCBase64);
            CryptoPP::SecByteBlock sk2ByteBlock = base64.base64StringToSecByteBlock(myBase64SecretKey2);
            CryptoPP::SecByteBlock pk2 = base64.base64StringToSecByteBlock(idPk2Map[i]);
            //std::cout << "Der base64 pk2 von: " << i << "lautet: " << idPk2Map[i] << std::endl;

            //step4Decoded += "Der base64 pk2 von: " + i +  "lautet: " + idPk2Map[i];
            //step4Decoded += "Mein SecretKey2: " + myBase64SecretKey2;

            CryptoPP::SecByteBlock sharedSecret2 = ecDiffieHellman.generateSharedSecret(sk2ByteBlock, pk2);
            std::string sharedSecretString2(reinterpret_cast<const char*>(&sharedSecret2[0]), sharedSecret2.size());
            //std::cout << "shared Secret zwischen: " << i << " und " << myId << ": " << sharedSecretString2 << std::endl;
            CryptoPP::SecByteBlock k = hash.hashMessage(sharedSecretString2);
            CryptoPP::SecByteBlock key(k.BytePtr(), 32);
            CryptoPP::SecByteBlock iv(k.BytePtr() + 32, 16);

            std::string c = aes.decode(encodedC, key, iv);
            //std::cout << "C decoded: " << c << std::endl;

            //step4Decoded += "Der base64 pk2 von: " + i +  "lautet: " + idPk2Map[i] + "shared Secret zwischen: " + i + " und " + myId + ": " + sharedSecretString2 + " und c: " + c;
            //break;

            step4Decoded += c + ";";




        }

    }






};

std::list<std::string> splitDataString(std::string data, std::string delimiter) {
    std::string s = data;
    std::list<std::string> splitData;
    size_t pos = 0;
    std::string token;
    while ((pos = s.find(delimiter)) != std::string::npos) {
        token = s.substr(0, pos);
        splitData.push_back(token);
        s.erase(0, pos + delimiter.length());
    }
    splitData.push_back(s);
    return splitData;

}

//********************************************************Section for communicating with JAVA***********************************************************************

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_bachelormarcelheiselsecureaggregation_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */, jstring string) {

    std::string stringFromJava = env->GetStringUTFChars(string, 0);
    std::list<std::string> stringFromJavaList = splitDataString(stringFromJava, ";");

    std::string hello = "Hello from C++";

    if(stringFromJavaList.front() == "step2"){
        Client client;
        client.step2();
        return env->NewStringUTF(client.step2Message.c_str());
    }
    else if(stringFromJavaList.front() == "step3"){
        Client client;
        stringFromJavaList.pop_front();
        client.t = std::stoi(stringFromJavaList.front());
        stringFromJavaList.pop_front();
        client.amountNeighbours = std::stoi(stringFromJavaList.front());
        stringFromJavaList.pop_front();
        client.myBase64SecretKey1 = stringFromJavaList.front();
        stringFromJavaList.pop_front();
        client.myBase64SecretKey2 = stringFromJavaList.front();
        stringFromJavaList.pop_front();
        client.myId = stringFromJavaList.front();
        stringFromJavaList.pop_front();
        for(int i = 0; i<client.amountNeighbours; i++){
            client.myNeighboursList.push_front(stringFromJavaList.front());
            client.debug += stringFromJavaList.front();
            stringFromJavaList.pop_front();
        }
        for(int a = 0; a<client.amountNeighbours; a++){
            std::string tempNeighbour = stringFromJavaList.front();
            stringFromJavaList.pop_front();
            std::string tempPk2 = stringFromJavaList.front();
            stringFromJavaList.pop_front();
            client.idPk2Map.insert(std::pair<std::string, std::string>(tempNeighbour, tempPk2));
        }

        client.step3();



        return env->NewStringUTF(client.step3Message.c_str());
    }

    else if(stringFromJavaList.front() == "step5"){
        Client client;
        stringFromJavaList.pop_front();
        std::string lString = stringFromJavaList.front();
        stringFromJavaList.pop_front();
        client.lable = stringFromJavaList.front();
        stringFromJavaList.pop_front();
        client.value = stringFromJavaList.front();
        stringFromJavaList.pop_front();
        int sizeA1 = std::stoi(stringFromJavaList.front());
        stringFromJavaList.pop_front();
        for(int i = 0; i<sizeA1; i++){
            client.a1.push_front(stringFromJavaList.front());
            stringFromJavaList.pop_front();
        }
        //8;0;BNoAnIT22g3iFCat7PMm2FiYUqXP3u3nIfVgAhPRorKVsEAvgZ+caZ+HQALi+Su2gg0N2w0JnQ4gfOjh/Jf53fk=;1;BGGPFIaAhet+GHDK7qZxZT+ZHJDTSXhu+XQ6Q0DmJd8l2R4Uq7ess0ROdIV3tnnpKoA1yWzHXZh9W5AzTdaf+IE=;2;BONE638DNpsZHHSCqj8wLtM5Dt9qrj8udV1rO2TaADCr6JSPfX56amh7aWdiUpMWBph+j6puB2vdEv7GPGCZcuQ=


        client.amountNeighbours = std::stoi(stringFromJavaList.front());
        stringFromJavaList.pop_front();
        for(int i=0; i<client.amountNeighbours; i++){
            std::string id = stringFromJavaList.front();
            stringFromJavaList.pop_front();
            std::string pk1 = stringFromJavaList.front();
            stringFromJavaList.pop_front();
            client.idPk1Map.insert(std::pair<std::string, std::string>(id, pk1));
        }
        client.myBase64SecretKey1 = stringFromJavaList.front();
        stringFromJavaList.pop_front();
        client.myId = stringFromJavaList.front();
        stringFromJavaList.pop_front();
        client.bBase64 = stringFromJavaList.front();




        client.l = std::stoi(lString);
        client.step5();
        return env->NewStringUTF(client.step5Message.c_str());
    }
    else if(stringFromJavaList.front() == "DecodeStep4"){

        Client client;
        stringFromJavaList.pop_front();
        client.myId = stringFromJavaList.front();
        stringFromJavaList.pop_front();
        std::string stringAmountNeighbours = stringFromJavaList.front();
        client.amountNeighbours = std::stoi(stringAmountNeighbours);
        stringFromJavaList.pop_front();
        client.myBase64SecretKey2 = stringFromJavaList.front();
        stringFromJavaList.pop_front();

        while(stringFromJavaList.front() != "BeginPk2s"){
            std::string temp1 = stringFromJavaList.front();
            client.step4List.push_back(temp1);
            stringFromJavaList.pop_front();
        }
        stringFromJavaList.pop_front();
        for(int a = 0; a<client.amountNeighbours; a++){
            std::string tempNeighbour = stringFromJavaList.front();
            stringFromJavaList.pop_front();
            std::string tempPk2 = stringFromJavaList.front();
            stringFromJavaList.pop_front();
            client.idPk2Map.insert(std::pair<std::string, std::string>(tempNeighbour, tempPk2));
        }

        client.decodeStep4();


        //client.step4Decoded = debugString;
        return env->NewStringUTF(client.step4Decoded.c_str());
    }





    return env->NewStringUTF("We should never reach this!!!!!");
}

