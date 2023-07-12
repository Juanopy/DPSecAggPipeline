#pragma once
#pragma warning(disable : 4996)

/*
	TCP Echo server example in winsock
	Live Server on port 8888
*/
#include <stdio.h>
#include <winsock2.h>
#include <iostream>
#include <list>
#include <map>
#include "ShamirSecretSharingHelper.h"
#include <vector>
#include "Base64Helper.h"
#include "PrgHelper.h"
#include "Sha2Helper.h"
#include "EcDiffieHellmanHelper.h"
#include "HyperGeometricHelper.h"
#include "HararyHelper.h"
#define PORT 8888
#define DEFAULT_BUFLEN 65536
#define gamma 0.1
#define delta 0.1
#define sigma 5
#define eta 5

#pragma comment(lib, "ws2_32.lib") //Winsock Library

class SocketServer {
public:
	std::map<std::string, SOCKET> idSocketMap;
	std::map<SOCKET, std::string> socketIdMap;
	std::list<std::string> clientIds;
public:
	int t;
	int THRESHOLD = 2000;
	int l;
	int n;
	int timeBetweenRounds;
	std::map<std::string, std::vector<std::string>> ngMap;
	std::list<std::string> a1;
	std::list<std::string> a2;
	std::list<std::string> a3;
	std::list<std::string> r2List;
	std::map<std::string, std::string> idPk1Map;
	std::map<std::string, std::string> idPk2Map;
	std::map<std::string, std::list<std::string>> step3MessagesMap;
	std::list<std::string> step5MessagesList;
	std::map<std::string, std::vector<std::string>> step7HbMap;
	std::map<std::string, std::vector<std::string>> step7HsMap;
	std::map<std::string, std::string> clientRecoveredSecretHbMap;
	std::map<std::string, std::string> clientRecoveredSecretHsMap;
	std::list<CryptoPP::SecByteBlock> recoveredRByteBlocksList;
	EcDiffieHellmanHelper ecDiffieHellmanHelper;

	SocketServer(int vectorSize, int amountClients) {
		std::cout << "Started Server..." << std::endl;
		l = vectorSize;
		n = amountClients;
		timeBetweenRounds = 3000;
	}

public:
	int start()
	{
		WSADATA wsa;
		SOCKET master, new_socket, client_socket[200], s;
		struct sockaddr_in server, address;
		int max_clients = 200, activity, addrlen, i, valread;

		//set of socket descriptors
		fd_set readfds;

		for (i = 0; i < max_clients; i++)
		{
			client_socket[i] = 0;
		}

		std::cout << "-SERVER: Initialising Winsock..." << std::endl;
		if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0)
		{
			std::cout << "Failed. Error Code: " << WSAGetLastError() << std::endl;
			exit(EXIT_FAILURE);
		}

		std::cout << "-SERVER: Initialised." << std::endl;

		//Create a socket
		if ((master = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)) == INVALID_SOCKET)
		{
			std::cout << "Could not create socket. Error: " << WSAGetLastError() << std::endl;
			exit(EXIT_FAILURE);
		}

		std::cout << "-SERVER: Socket created." << std::endl;

		//Prepare the sockaddr_in structure
		server.sin_family = AF_INET;
		server.sin_addr.s_addr = INADDR_ANY;
		server.sin_port = htons(PORT);

		//Bind
		if (bind(master, (struct sockaddr*)&server, sizeof(server)) == SOCKET_ERROR)
		{
			std::cout << "Bind failed. Error: " << WSAGetLastError() << std::endl;
			exit(EXIT_FAILURE);
		}

		std::cout << "-SERVER: Bind done." << std::endl;
		std::cout << "******************************************" << std::endl;

		//Listen to incoming connections
		listen(master, SOMAXCONN);

		//Accept and incoming connection
		std::cout << "-SERVER: Waiting for incoming connections..." << std::endl;
		std::cout << "******************************************" << std::endl;

		addrlen = sizeof(struct sockaddr_in);

		while (TRUE)
		{
			//clear the socket fd set
			FD_ZERO(&readfds);

			//add master socket to fd set
			FD_SET(master, &readfds);

			//add child sockets to fd set
			for (i = 0; i < max_clients; i++)
			{
				s = client_socket[i];
				if (s > 0)
				{
					FD_SET(s, &readfds);
				}
			}

			//wait for an activity on any of the sockets, timeout is NULL , so wait indefinitely
			activity = select(0, &readfds, NULL, NULL, NULL);

			if (activity == SOCKET_ERROR)
			{
				std::cout << "Select call failed. Error: " << WSAGetLastError() << std::endl;
				exit(EXIT_FAILURE);
			}

			//If something happened on the master socket , then its an incoming connection
			if (FD_ISSET(master, &readfds))
			{
				if ((new_socket = accept(master, (struct sockaddr*)&address, (int*)&addrlen)) < 0)
				{
					perror("accept");
					exit(EXIT_FAILURE);
				}

				//inform user of socket number - used in send and receive commands
				std::cout << "-SERVER: New connection, socket is: " << new_socket << " port is: " << ntohs(address.sin_port) << std::endl;


				//add new socket to array of sockets
				for (i = 0; i < max_clients; i++)
				{
					if (client_socket[i] == 0)
					{
						client_socket[i] = new_socket;
						std::cout << "-SERVER: Adding to list of sockets at index: " << i << std::endl;
						break;
					}
				}
			}
			std::cout << "******************************************" << std::endl;
			//else its some IO operation on some other socket :)
			for (i = 0; i < max_clients; i++)
			{
				s = client_socket[i];
				//if client presend in read sockets             
				if (FD_ISSET(s, &readfds))
				{
					//get details of the client
					getpeername(s, (struct sockaddr*)&address, (int*)&addrlen);

					//Check if it was for closing , and also read the incoming message
					//recv does not place a null terminator at the end of the string (whilst printf %s assumes there is one).
					char recvbuf[DEFAULT_BUFLEN] = { 0 };
					int recvbuflen = DEFAULT_BUFLEN;
					valread = recv(s, recvbuf, recvbuflen, 0);

					if (valread == SOCKET_ERROR)
					{
						int error_code = WSAGetLastError();
						if (error_code == WSAECONNRESET)
						{
							//Somebody disconnected , get his details and print
							std::cout << "Host disconnected unexpectedly, port: " << ntohs(address.sin_port) << std::endl;

							//Close the socket and mark as 0 in list for reuse
							closesocket(s);
							client_socket[i] = 0;
						}
						else
						{
							std::cout << "recv failed with error code: " << error_code << std::endl;
						}
					}
					if (valread == 0)
					{
						//Somebody disconnected , get his details and print
						std::cout << "Host disconnected, port: " << ntohs(address.sin_port) << std::endl;

						//Close the socket and mark as 0 in list for reuse
						closesocket(s);
						client_socket[i] = 0;
					}

					//Handle the clients message
					else
					{
						std::cout << "-SERVER: Received message: " << recvbuf  << std::endl;
						std::cout << "Computing..." << std::endl;
						std::cout << "******************************************" << std::endl;
						chooseCorrectHandler(recvbuf, s);
					}
				}
			}
		}

		closesocket(s);
		WSACleanup();

		return 0;
	}


	void chooseCorrectHandler(char* buff, SOCKET s) {
		std::string data(buff);
		std::list<std::string> splitData = splitDataString(data);
		if (splitData.front() == "Step0") {
			splitData.pop_front();
			if (splitData.front() != "") {
				clientIds.push_front(splitData.front());
			}
			idSocketMap.insert(std::pair<std::string, SOCKET>(splitData.front(), s));
			socketIdMap.insert(std::pair<SOCKET, std::string>(s, splitData.front()));
		}
		if (splitData.front() == "Step2") {
			std::string id = socketIdMap[s];
			splitData.pop_front();
			std::string pk1 = splitData.front();
			std::string step2ServerMessage = "Step2Server;" + id + ";" + pk1;
			splitData.pop_front();
			std::string pk2 = splitData.front();
			step2ServerMessage += ";" + pk2;
			splitData.pop_front();
			idPk1Map.insert(std::pair<std::string, std::string>(id, pk1));
			idPk2Map.insert(std::pair<std::string, std::string>(id, pk2));
			broadcastToNeighbours(id, step2ServerMessage);
		}
		if (splitData.front() == "Step3") {
			splitData.pop_front();
			if (!(std::find(a1.begin(), a1.end(), socketIdMap[s]) != a1.end())) {
				a1.push_front(socketIdMap[s]);
			}
			std::string j = splitData.front();
			if (!step3MessagesMap.contains(j)) {
				std::list<std::string> tempList;
				step3MessagesMap.insert({ j, tempList });
			}
			step3MessagesMap[j].push_front(data);
		}
		if (splitData.front() == "Step5") {
			splitData.pop_front();
			a2.push_front(socketIdMap[s]);
			std::string temp;
			while (!splitData.empty()) {
				temp += splitData.front() + ";";
				splitData.pop_front();
			}
			step5MessagesList.push_front(temp);
		}

		if (splitData.front() == "Step7") {
			Base64Helper base64;
			splitData.pop_front();
			a3.push_front(socketIdMap[s]);
			splitData.pop_front();
			while (splitData.front() != "Hs") {
				std::string clientId = splitData.front();
				splitData.pop_front();
				std::string clientShareHb = splitData.front();
				if (!step7HbMap.contains(clientId)) {
					std::vector<std::string> tempList;
					step7HbMap.insert({ clientId, tempList });
				}
				step7HbMap[clientId].push_back(base64.decodeString(clientShareHb));
				splitData.pop_front();
			}
			splitData.pop_front();
			while (!splitData.empty()) {
				std::string clientId = splitData.front();
				splitData.pop_front();
				std::string clientShareHs = splitData.front();
				if (!step7HsMap.contains(clientId)) {
					std::vector<std::string> tempList;
					step7HsMap.insert({ clientId, tempList });
				}
				step7HsMap[clientId].push_back(base64.decodeString(clientShareHs));
				splitData.pop_front();
			}
		}
		

	}

	std::list<std::string> splitDataString(std::string data) {
		std::string delimiter = ";";
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

	void broadcastMessage(std::list <std::string> clients, std::string message) {
		const char* sendbuf = message.c_str();
		std::cout << "Attempting to broadcast Message: " << sendbuf << std::endl;
		for (std::string s : clients) {
			send(idSocketMap[s], sendbuf, (int)strlen(sendbuf), 0);
		}
	}

	void broadcastToNeighbours(std::string client, std::string message) {
		std::cout << "Attempting to broadcast Message: " << message << " to neighbours of: " << client << std::endl;
		for (std::string neighbour : ngMap[client]) {
			sendMessageToClient(neighbour, message);
		}
	}

	void sendMessageToClient(std::string client, std::string message) {
		const char* sendbuf = message.c_str();
		std::cout << "Attempting to send message: " << sendbuf << std::endl;
		send(idSocketMap[client], sendbuf, (int)strlen(sendbuf), 0);
	}


	void serverSendA1() {
		for (std::string client : a1) {
			std::string serverSendA1Message = "ServerA1";
			for (std::string neighbour : ngMap[client]) {
				if (std::find(a1.begin(), a1.end(), neighbour) != a1.end()) {
					serverSendA1Message += ";" + neighbour;
				}
			}
			sendMessageToClient(client, serverSendA1Message);
		}
	}


	void serverStep1() {
		HyperGeometricHelper hg;
		std::tuple<int, int> ktTuple = hg.computeDegreeAndThreshold(n, gamma, delta, sigma, eta);
		int k = std::get<0>(ktTuple);
		t = std::get<1>(ktTuple);
		THRESHOLD = t;
		HararyHelper harary(n, k);
		harary.generateHarary();
		harary.randomShuffleVector(harary.nVector);
		harary.generateGMap();
		harary.printNgMap();
		ngMap = harary.neighboursMap;

		for (std::string id : clientIds) {
			std::string step1ServerMessage = "Step1Server;";
			step1ServerMessage += std::to_string(t);
			for (std::string neighbour : ngMap[id]) {
				step1ServerMessage += ";" + neighbour;
			}
			std::cout << "T lautet: " << t << std::endl;
			std::cout << "The message lautet: " << step1ServerMessage << std::endl;
			sendMessageToClient(id, step1ServerMessage);

		}
	}



	void serverStep4() {
		std::string step4ServerMessage = "Step4Server";
		for (std::string iterator : a1) {
			for (std::string iterator2 : step3MessagesMap[iterator]) {
				step4ServerMessage += ";" + iterator2;
			}
			sendMessageToClient(iterator, step4ServerMessage);
			step4ServerMessage = "Step4Server";
		}
	}


	void serverStep6() {
		for (std::string client : a2) {
			std::string serverStep6Message;
			std::string r1 = "R1";
			std::string r2 = ";R2";
			for (std::string it : a2) {
				if (std::find(ngMap[client].begin(), ngMap[client].end(), it) != ngMap[client].end()) {
					r1 += ";" + it;
				}
			}
			for (std::string a1It : a1) {
				if (!(std::find(a2.begin(), a2.end(), a1It) != a2.end())) {
					if (std::find(ngMap[client].begin(), ngMap[client].end(), a1It) != ngMap[client].end()) {
						r2 += ";" + a1It;
						if (std::find(r2List.begin(), r2List.end(), a1It) == r2List.end()) {
							r2List.push_back(a1It);
						}
					}
				}
			}
			serverStep6Message = "Step6Server;" + r1 + r2;
			sendMessageToClient(client, serverStep6Message);
		}
	}

	boolean serverStep8() {
		Base64Helper base64;
		ShamirSecretSharingHelper shamir;
		PrgHelper prng;

		Sha2Helper hash;

		//Recover b and r
		for (std::string iterator : a2) {
			if (step7HbMap[iterator].size() < t) {
				std::cout << "Not enough Shares available. Aborting..." << std::endl;
				exit(0);
			}
			std::string recoveredBBase64 = shamir.reconstructSecret(t, step7HbMap[iterator]);
			std::cout << "Recovered Hb for client: " << iterator << ": " << recoveredBBase64 << std::endl;
			CryptoPP::SecByteBlock recoveredBByteBlock;
			recoveredBByteBlock = base64.base64StringToSecByteBlock(recoveredBBase64);
			CryptoPP::SecByteBlock recoveredRByteBlock;
			recoveredRByteBlock = prng.generateRandomFromSeed(recoveredBByteBlock, l);
			recoveredRByteBlocksList.push_front(recoveredRByteBlock);
			

		}

		std::vector<CryptoPP::Integer> finalSumVector = prng.initializeNullVector(l);
		for (std::string iterator2 : step5MessagesList) {
			std::cout << "Die step5Message list contains: " << iterator2 << std::endl;
			std::vector<CryptoPP::Integer> step5Vector = prng.deserializeIntVector(iterator2);
			finalSumVector = prng.addTwoIntegerVectors(step5Vector, finalSumVector);
		}

		//Recover sk1 and m
		std::vector<CryptoPP::Integer> globalSumMVector = prng.initializeNullVector(l);
		for (std::string iterator3 : r2List) {
			if (step7HsMap[iterator3].size() < t) {
				std::cout << "Not enough Shares Hs available. Aborting..." << std::endl;
				exit(0);
			}
			std::vector<CryptoPP::Integer> sumMVector = prng.initializeNullVector(l);
			std::string recoveredSk1Base64 = shamir.reconstructSecret(t, step7HsMap[iterator3]);
			CryptoPP::SecByteBlock recoveredSk1ByteBlock = base64.base64StringToSecByteBlock(recoveredSk1Base64);
			//Compute PRG Seed si,j
			for (std::string iterator4 : a1) {
				if (iterator3 != iterator4) {
					if (std::find(ngMap[iterator3].begin(), ngMap[iterator3].end(), iterator4) != ngMap[iterator3].end()) {
						CryptoPP::SecByteBlock pk1 = base64.base64StringToSecByteBlock(idPk1Map[iterator4]);
						CryptoPP::SecByteBlock sharedSecret = ecDiffieHellmanHelper.generateSharedSecret(recoveredSk1ByteBlock, pk1);
						std::string sharedSecretString(reinterpret_cast<const char*>(&sharedSecret[0]), sharedSecret.size());
						CryptoPP::SecByteBlock s = hash.hashMessage(sharedSecretString);
						CryptoPP::SecByteBlock mByteBlock = prng.generateRandomFromSeed(s, l);
						std::vector<CryptoPP::Integer> mVector = prng.transformByteBlockToIntegerVector(mByteBlock);
						if (std::stoi(iterator4) < std::stoi(iterator3)) {
							mVector = prng.negateVector(mVector);
						}
						std::cout << "m zwischen: " << iterator4 << " und " << iterator3 << " lautet: " << mVector[0] << std::endl;
						sumMVector = prng.addTwoIntegerVectors(mVector, sumMVector);

					}
				}
			}
			globalSumMVector = prng.addTwoIntegerVectors(sumMVector, globalSumMVector);
		}
		std::cout << "M insgesamt lautet: " << globalSumMVector[0] << std::endl;
		finalSumVector = prng.addTwoIntegerVectors(globalSumMVector, finalSumVector);
		
		//Compute the final sum
		for (CryptoPP::SecByteBlock iterator5 : recoveredRByteBlocksList) {
			std::vector<CryptoPP::Integer> recoveredRVector = prng.transformByteBlockToIntegerVector(iterator5);
			std::cout << "R als Vektor" << std::endl;
			for (CryptoPP::Integer test : recoveredRVector) {
				std::cout << test << ' ';
			}
			std::cout << std::endl;
			finalSumVector = prng.subtractTwoIntegerVectors(finalSumVector, recoveredRVector);
		}
		std::cout << "***************************************************************" << std::endl;

		for (CryptoPP::Integer i : finalSumVector) {
			std::cout << "Der finale Vector: " << i << std::endl;
		}
		return true;
		
	}

};