#pragma once

#include <iostream>
#include <vector>
#include "osrng.h"
#include "channels.h"
#include "ida.h"

class ShamirSecretSharingHelper
{
public:
	std::vector<std::string> generateShares(std::string secret, int thresh, int amountShares);
	std::string reconstructSecret(int thresh, std::vector<std::string> shares);

};

