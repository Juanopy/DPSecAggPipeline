
#include "SocketServer.cpp"
#include <thread>
#include <Windows.h>
#include "HararyHelper.h"
#include "HyperGeometricHelper.h"

int main(int argc, char* argv[]) {

    int l = 16; //std::stoi(argv[1]);
    int n = 1; //std::stoi(argv[2]);
    SocketServer server(l, n);
    std::thread serverListener(&SocketServer::start, &server);
    serverListener.detach();

    //Time to setup a connection with each client
    Sleep(10000);

    server.serverStep1();
    //Time for each client to finish step3
    Sleep(10000);
  
    if (server.a1.size() < (1 - delta) * n){
        std::cout << "Not enough users finished Step3. Only: " << server.a1.size() << ". Aborting..." << std::endl;
        exit(0);
    }
    server.serverStep4();
	server.serverSendA1();
	
    Sleep(5000);
    if (server.a2.size() < (1 - delta) * n) {
        std::cout << "Not enough users finished Step5. Only: " << server.a2.size() << ". Aborting..." << std::endl;
        exit(0);
    }
    server.serverStep6();
    
    Sleep(5000);
    if (server.a3.size() < (1 - delta) * n) {
        std::cout << "Not enough users finished Step7. Only: " << server.a3.size() << ". Aborting..." << std::endl;
        exit(0);
    }
    
    server.serverStep8();
	
    return 0;
}
