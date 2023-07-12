#pragma once
#include<tuple>
#include"eccrypto.h"
#include"osrng.h"
#include"oids.h"

#include <iostream>
#include <iomanip>
class EcDiffieHellmanHelper
{
public:
	std::tuple<CryptoPP::SecByteBlock, CryptoPP::SecByteBlock> generateDhKeys();

	CryptoPP::SecByteBlock generateSharedSecret(CryptoPP::SecByteBlock privKey, CryptoPP::SecByteBlock pubKey);
};

