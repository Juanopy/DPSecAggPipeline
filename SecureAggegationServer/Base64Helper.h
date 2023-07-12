#pragma once
#include <iostream>
#include <base64.h>
#include "cryptlib.h"


class Base64Helper
{
public:
	std::string secByteBlockToBase64String(CryptoPP::SecByteBlock secByteBlock);
	CryptoPP::SecByteBlock base64StringToSecByteBlock(std::string base64String);
	std::string encodeString(std::string string);
	std::string decodeString(std::string encoded);
};

