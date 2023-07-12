#pragma once

#include <boost\math\distributions\hypergeometric.hpp>
#include <cstdlib>
#include <boost\math\policies\policy.hpp>
#include <iostream>  
#include<cmath>
class HyperGeometricHelper
{
public:
	double computeCdfY(int n, int k, int t, double sigma);
	std::tuple<int, int> computeDegreeAndThreshold(int n, double gamma, double delta, int sigma, int eta);


};

