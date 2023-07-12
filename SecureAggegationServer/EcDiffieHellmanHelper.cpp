#include "EcDiffieHellmanHelper.h"

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
