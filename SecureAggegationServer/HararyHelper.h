#pragma once

#include <string>
#include <iostream>
#include <cstdlib>      
#include <algorithm>
#include <vector>
#include <list>
#include <tuple>
#include <map>
#include "osrng.h"


class HararyHelper {
public:
	std::map<std::string, std::vector<int>> hararyMap;
	std::vector<int> nVector;
	std::vector<int> normalNVector;
	int n;
	int k;
	std::map<std::string, std::vector<std::string>> neighboursMap;

	HararyHelper(int amountUsers, int amountNeighbours);
	void printHararyMap();
	void oneHararyMap();
	void generateHarary();
	void hararyOdd();
	void printVector(std::vector<int> a);
	void randomShuffleVector(std::vector<int> toShuffleVector);
	void generateGMap();
	void printNgMap();
	int getIndex(std::vector<int> v, int K);
	std::vector<int> zeroVector();























};